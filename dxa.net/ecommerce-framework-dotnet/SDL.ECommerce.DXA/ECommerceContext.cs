using Sdl.Web.Mvc.Configuration;

using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Rest;

using System;
using System.Linq;
using System.Collections.Generic;
using System.Collections.Concurrent;
using System.Web;
using System.Web.Configuration;
using System.Web.Mvc;
using Sdl.Web.Common;
using Sdl.Web.Common.Configuration;
using Sdl.Web.Common.Logging;

namespace SDL.ECommerce.DXA
{

    /// <summary>
    /// E-Commerce Context
    /// </summary>
    public class ECommerceContext
    {
        public const string CURRENT_QUERY = "CurrentQuery";
        public const string QUERY_RESULT = "QueryResult";
        // TODO: Rename this to CATEGORY_URL_PREFIX (as it is only used to build category URLs)
        public const string URL_PREFIX = "UrlPrefix";
        public const string FACETS = "Facets";
        public const string PRODUCT = "Product";
        public const string CATEGORY = "Category";
        public const string ROOT_TITLE = "RootTitle";
        public const string SEARCH_PHRASE = "SearchPhrase";

        public const string ENVIRONMENT_REQUEST_PARAM = "ecom_env"; // TODO: Have this configurable?
        public const string ENVIRONMENT_SESSION_PARAM = "ECOM-ENVIRONMENT";

        private static readonly IDictionary<string, IECommerceClient> clients = new ConcurrentDictionary<string, IECommerceClient>();
        private static IDictionary<string, string> environments = null;

        private const int DEFAULT_CATEGORY_EXPIRY_TIMEOUT = 3600000;
        private const bool DEFAULT_CATEGORY_USE_SANITIZED_PATHNAMES = false;

        private static object _lock = new object();

        /// <summary>
        /// Client to E-Commerce services
        /// </summary>
        public static IECommerceClient Client => GetClient(WebRequestContext.Localization);

        /// <summary>
        /// GEt client to E-Commerce services via locale
        /// </summary>
        public static IECommerceClient GetClient(Localization localization)
        {
            var locale = GetLocale(localization);

            string cacheKey = locale;
            var currentEnvironment = GetEnvironment();
            if (currentEnvironment != null)
            {
                cacheKey += ":" + currentEnvironment;
            }
            IECommerceClient client = null;
            if (!clients.TryGetValue(cacheKey, out client))
            {
                lock (_lock)
                {
                    // Double-check so one other thread has already created the client
                    //
                    if (!clients.ContainsKey(cacheKey))
                    {
                        client = Create(locale, currentEnvironment);
                        clients.Add(cacheKey, client);
                    }
                    else
                    {
                        client = clients[cacheKey];
                    }
                }
            }
            return client;
        }

        /// <summary>
        /// Get locale from the localization.
        /// </summary>
        /// <param name="localization"></param>
        /// <returns></returns>
        private static string GetLocale(Localization localization)
        {
            // check first if locale has been specifially set in current publication
            //
            string locale = WebRequestContext.Localization.GetConfigValue("e-commerce.ecommerce-locale");
            if (locale == null)
            {
                // If not -> fallback on the locale in the DXA Localization instance
                // 
                locale = WebRequestContext.Localization.Culture;
            }
            return locale;
        }

        /// <summary>
        /// Create E-Commerce client for specified locale
        /// </summary>
        /// <param name="locale"></param>
        /// <returns></returns>
        private static IECommerceClient Create(string locale, string environment)
        {
            Log.Debug("Creating Commerce Client for environment: " + environment);
            string endpointAddress = null;
            if (environment != null)
            {
                environments.TryGetValue(environment, out endpointAddress);
            }

            if (endpointAddress == null)
            {
                endpointAddress = WebConfigurationManager.AppSettings["ecommerce-service-uri"];
            }

            var clientType = WebConfigurationManager.AppSettings["ecommerce-service-client-type"] ?? "odata";
            var categoryExpiryTimeoutStr = WebConfigurationManager.AppSettings["ecommerce-category-expiry-timeout"];
            int categoryExpiryTimeout = categoryExpiryTimeoutStr != null ? int.Parse(categoryExpiryTimeoutStr) : DEFAULT_CATEGORY_EXPIRY_TIMEOUT;
            var useSanitizedPathNamesStr = WebConfigurationManager.AppSettings["ecommerce-category-use-sanitized-pathnames"];
            bool useSanitizedPathNames = useSanitizedPathNamesStr != null ? bool.Parse(useSanitizedPathNamesStr) : DEFAULT_CATEGORY_USE_SANITIZED_PATHNAMES;

            if (clientType.Equals("odata"))
            {
                // TODO: Get token service data here as well
                return new OData.ECommerceClient(endpointAddress, locale, useSanitizedPathNames, DependencyResolver.Current.GetService);
            }

            if (clientType.Equals("rest"))
            {
                return new ECommerceClient(endpointAddress, locale, new DXACacheProvider(locale), categoryExpiryTimeout, useSanitizedPathNames, DependencyResolver.Current.GetService, environment);
            }

            throw new DxaException("Invalid client type configured for the E-Commerce service: " + clientType);
        }

        /// <summary>
        /// Get request E-Commerce property
        /// </summary>
        /// <param name="key"></param>
        /// <returns></returns>
        public static object Get(string key)
        {
            return HttpContext.Current == null ? null : HttpContext.Current.Items["ECOM-" + key];
        }

        /// <summary>
        /// Set request E-Commerce property
        /// </summary>
        /// <param name="key"></param>
        /// <param name="value"></param>
        /// <returns></returns>
        public static object Set(string key, object value)
        {
            if (HttpContext.Current != null)
            {
                HttpContext.Current.Items["ECOM-" + key] = value;
            }
            return value;
        }

        public static void SetEnvironment(string environment)
        {
            if (HttpContext.Current?.Session != null)
            {
                HttpContext.Current.Session.Add(ENVIRONMENT_SESSION_PARAM, environment);
            }
            else
            {
                // If session is not enabled -> store it a cookie instead
                //
                if (HttpContext.Current != null)
                {
                    var envCookie = HttpContext.Current.Request.Cookies[ENVIRONMENT_SESSION_PARAM];
                    if (envCookie != null)
                    {
                        envCookie.Value = environment;
                        HttpContext.Current.Response.SetCookie(envCookie);
                    }
                    else
                    {
                        envCookie = new HttpCookie(ENVIRONMENT_SESSION_PARAM, environment);
                        HttpContext.Current.Response.Cookies.Add(envCookie);
                    }
                }
            }
        }

        public static string GetEnvironment()
        {
            var environments = Environments;
            if (HttpContext.Current != null)
            {
                var environment = HttpContext.Current.Request.QueryString[ENVIRONMENT_REQUEST_PARAM];
                if (environment != null)
                {
                    if (environments.Contains(environment))
                    {
                        SetEnvironment(environment);
                        return environment;
                    } 
                }
                if (HttpContext.Current?.Session != null)
                {
                    var environmentInSession = (string) HttpContext.Current.Session[ENVIRONMENT_SESSION_PARAM];
                    if (environmentInSession != null)
                    {
                        return environmentInSession;
                    }
                }
                else
                {
                    var envCookie = HttpContext.Current.Request.Cookies[ENVIRONMENT_SESSION_PARAM];
                    if (envCookie != null)
                    {
                        return envCookie.Value;
                    }
                }
                
            }
            if (environments != null && environments.Count > 0)  {
                // Pick the first one as default
                //
                return environments[0];
            }
            return null;
        }

        public static IList<string> Environments
        {
            get
            {
                if (environments == null)
                {
                    environments = new Dictionary<string, string>();
                    var envConfig = WebConfigurationManager.AppSettings["ecommerce-service-environments"];
                    if (envConfig != null)
                    {
                        var parts = envConfig.Split(new char[] { ' ', '=', ';' }, StringSplitOptions.RemoveEmptyEntries);
                        for (int i = 0; i < parts.Length; i = i + 2)
                        {
                            var envName = parts[i];
                            if (i + 1 >= parts.Length)
                            {
                                break;
                            }
                            var envUrl = parts[i + 1];
                            environments.Add(envName, envUrl);
                        }
                    }
                }
                return environments.Keys.ToList();
            }
        }

        /// <summary>
        /// Localize path
        /// </summary>
        /// <param name="url"></param>
        /// <returns></returns>
        public static string LocalizePath(string url)
        {
            string path = WebRequestContext.Localization.Path;
            if (!String.IsNullOrEmpty(path))
            {
                if (path.EndsWith("/"))
                {
                    url = path + (url.StartsWith("/") ? url.Substring(1) : url);
                }
                else
                {
                    url = path + (url.StartsWith("/") ? url : '/' + url);
                }
            }
            return url;
        }

        public static bool ShowEditUrl(IEditable editableItem)
        {
            return WebRequestContext.IsPreview && editableItem != null && !String.IsNullOrEmpty(editableItem.EditUrl);
        }

        public static ICart Cart
        {
            get
            {
                if (HttpContext.Current != null)
                {
                    return HttpContext.Current.Session["ECOM-Cart"] as ICart;
                }
                return null;
            }
            set
            {
                if (HttpContext.Current != null)
                {
                    HttpContext.Current.Session["ECOM-Cart"] = value;
                }
            }
        }

        /// <summary>
        /// Get edit URL for an editable item. All requests are passed through the edit proxy. 
        /// </summary>
        /// <param name="editableItem"></param>
        /// <returns></returns>
        public static string EditUrl(IEditable editableItem)
        {
            return "/edit-proxy" + editableItem.EditUrl;
        }

        /// <summary>
        /// Get edit menu
        /// </summary>
        public static IEditMenu EditMenu
        {
            get
            {
                var currentQuery = Get(CURRENT_QUERY) as Query;
                if (currentQuery != null && WebRequestContext.IsPreview )
                {
                    return Client.EditService.GetInContextMenuItems(currentQuery);
                }
                return null;
            }
        }
    }
}
