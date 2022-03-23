using Sdl.Web.Common.Logging;
using Sdl.Web.Mvc.Configuration;
using System;
using System.IO;
using System.Net;
using System.Web;
using System.Web.Configuration;
using System.Web.Mvc;

namespace SDL.ECommerce.DXA.Controllers
{
    public class EditProxyController : System.Web.Mvc.Controller
    {
        private string endpointAddress = WebConfigurationManager.AppSettings["ecommerce-service-uri"].Replace("/ecommerce.svc", "");

        public ActionResult Http(string path)
        {
            string proxyUrl = HttpContext.Request.RawUrl.Replace("/edit-proxy", "");
            return GrabContent(proxyUrl, path);
        }

        private ActionResult GrabContent(string url, string path)
        {
            // TODO: Add some verification on the XPM session as well here...
            if ( !WebRequestContext.IsPreview )
            {
                return HttpNotFound("Edit proxy is not available for non-staging sites!");
            }

            // Create a request for the URL. 		
            //
            var req = HttpWebRequest.Create(endpointAddress + url) as HttpWebRequest;
            req.Method = HttpContext.Request.HttpMethod;

            
            foreach ( var headerName in HttpContext.Request.Headers.AllKeys)
            {
                if ( headerName.Equals("User-Agent") )
                {
                    req.UserAgent = HttpContext.Request.Headers[headerName];
                }
                else if (!headerName.Equals("Cookie") && !headerName.Equals("Cache-Control") )
                {
                    try
                    {
                        req.Headers.Add(headerName, HttpContext.Request.Headers[headerName]);
                        Log.Info("Setting header: " + headerName + " = " + HttpContext.Request.Headers[headerName]);
                    }
                    catch (ArgumentException) { }
                }          
            }         

            if (req.Method != "GET")
            {
                req.ContentType = HttpContext.Request.ContentType;             
                Request.InputStream.Position = 0;
                var requestStream = HttpContext.Request.InputStream;
                Stream webStream = null;
                try
                {
                    // Copy incoming request body to outgoing request
                    //
                    if (requestStream != null && requestStream.Length > 0)
                    {
                        req.ContentLength = requestStream.Length;
                        webStream = req.GetRequestStream();
                        requestStream.CopyTo(webStream);
                    }
                }
                finally
                {
                    if (null != webStream)
                    {
                        webStream.Flush();
                        webStream.Close();
                    }
                }
            }

            // If required by the server, set the credentials.
            // TODO: Set some kind of token here to make sure that it is only available on 
            req.Credentials = CredentialCache.DefaultCredentials;

            HttpWebResponse response = (HttpWebResponse)req.GetResponse();
            var contentType = response.ContentType;

            // Modify content type if is has not been set
            //
            if (String.IsNullOrEmpty(contentType) )
            {
                contentType = MimeMapping.GetMimeMapping(url);
            }
                
            if (String.IsNullOrEmpty(contentType))
            {
                Log.Error("Could not get content type from URL: " + url);
            }
            if ( contentType != null && ( contentType.StartsWith("text") || contentType.Contains("javascript") ) )
            {
                using (Stream dataStream = response.GetResponseStream())
                {
                    StreamReader reader = new StreamReader(dataStream);

                    // Read the markup content
                    //
                    string responseText = reader.ReadToEnd();
          
                    // Rewrite absolute links to the edit proxy
                    //
                    responseText = responseText.Replace("src=\"/", "src=\"/edit-proxy/");
                    HttpContext.Response.AddHeader("Content-Length", responseText.Length.ToString());
                    HttpContext.Response.ContentType = contentType;
                    HttpContext.Response.BinaryWrite(System.Text.Encoding.UTF8.GetBytes(responseText));
                    HttpContext.Response.End();
                    return Content("", contentType);                        
                }
            }
            else
            {
                using (var memoryStream = new MemoryStream())
                {
                    response.GetResponseStream().CopyTo(memoryStream);
                    HttpContext.Response.ContentType = contentType;
                    HttpContext.Response.BinaryWrite(memoryStream.ToArray());
                    HttpContext.Response.End();
                    return File(new byte[] { }, contentType);
                }
            }           
                
        }

    }
}