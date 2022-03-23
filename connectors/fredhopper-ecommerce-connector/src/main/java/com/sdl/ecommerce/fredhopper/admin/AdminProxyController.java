package com.sdl.ecommerce.fredhopper.admin;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

/**
 * Admin Proxy Controller
 *
 * Proxies requests to Fredhopper Business Manager and filter out unnecessary HTML.
 *
 * @author nic
 */
@Controller
@RequestMapping("/fh-edit")
public class AdminProxyController {

    // TODO: Migrate to Http Client 4.5.
    // - There is an issue with some AJAX post requests that does not work with 4.5, therefore we use 3.1 for this controller
    //

    private static final Logger LOG = LoggerFactory.getLogger(AdminProxyController.class);

    static final String ADMIN_URL = "/fredhopper/admin";

    private String fredhopperAdminUrl;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${fredhopper.adminserver.url:#{null}}")
    private String fredhopperBaseUrl = "http://localhost:8180";

    @Value("${fredhopper.access.username:#{null}}")
    private String accessUsername = null;
    @Value("${fredhopper.access.password:#{null}}")
    private String accessPassword = null;

    @Value("${fredhopper.admin.username:#{null}}")
    private String username;
    @Value("${fredhopper.admin.password:#{null}}")
    private String password;

    @Value("${fredhopper.edit.cacheDir:#{null}}")
    private String cacheDirectory;

    @Value("${fredhopper.version:8.5}")
    private double fredhopperVersion;

    private long sessionTimeout = 1 * 60 * 1000; // TODO: Have configurable
    private long lastAccessTime;
    private boolean isInitialized = false;

    private HttpClient client;
    private MultiThreadedHttpConnectionManager connectionManager;

    private Tika tika = new Tika();

    @PostConstruct
    public void initialize() throws IOException {

        if ( StringUtils.isNotEmpty(this.fredhopperBaseUrl) ) {
            this.fredhopperAdminUrl = this.fredhopperBaseUrl + ADMIN_URL;
            this.connectionManager =
                    new MultiThreadedHttpConnectionManager();
            this.client = new HttpClient(connectionManager);

            if (this.accessUsername != null && !this.accessUsername.isEmpty()) {
                Credentials credentials = new UsernamePasswordCredentials(this.accessUsername, this.accessPassword);
                client.getState().setCredentials(AuthScope.ANY, credentials);
            }
            this.login();
            this.lastAccessTime = System.currentTimeMillis();
            this.isInitialized = true;
        }
    }

    /**
     * Login into Fredhopper Business Manager.
     *
     * @throws IOException
     */
    protected void login() throws IOException {
        GetMethod method = new GetMethod(fredhopperAdminUrl + "/login.fh?username=" + this.username + "&password=" + this.password);
        try {
            int statusCode = client.executeMethod(method);
            LOG.debug("Fredhopper admin login status: " + statusCode);
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Could not login into Fredhopper. Status Code: " + statusCode);
            }
        }
        finally {
            method.releaseConnection();
        }
    }

    /**
     * Check session if it is
     * @param request
     * @throws IOException
     */
    protected boolean checkSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // TODO: This has to be check in the DXA module instead
        //if ( !isInXPMSessionPreview(request) ) {
        //    throw new IOException("No active XPM session found!");
        //}

        if ( !isInitialized ) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // TODO: Check if no admin credential -> Give an error
        if ( this.lastAccessTime + this.sessionTimeout < System.currentTimeMillis() ) {
            login();
        }

        return true;
    }

    protected boolean isInXPMSessionPreview(HttpServletRequest request) {
        Boolean accessedViaXPM = (Boolean) request.getSession().getAttribute("__InXPMSession");
        return Boolean.TRUE.equals(accessedViaXPM);
    }

    /**
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/**", produces = {MediaType.ALL_VALUE})
    public void proxyAssets(HttpServletRequest request, HttpServletResponse response) throws IOException {

        boolean sessionOk = this.checkSession(request, response);
        if ( !sessionOk ) {
            return;
        }

        LOG.debug("Proxy request: " + request.getRequestURI());

        final String requestPath = request.getRequestURI().replaceFirst("/fh-edit", "");
        final String fredhopperUrl = fredhopperAdminUrl + requestPath + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        final boolean isAjax = request.getHeader("x-requested-with") != null;

        if ( !isAjax ) {

            if ( this.readCachedResource(fredhopperUrl, response) ) {
                LOG.debug("Using cached resource for URL: " + fredhopperUrl);
                response.flushBuffer();
                return;
            }
        }

        HttpMethodBase method = new GetMethod(fredhopperUrl);

        try {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement().toLowerCase();
                if (headerName.startsWith("wicket") || headerName.startsWith("x-") || headerName.startsWith("accept") || headerName.startsWith("user-agent")) {
                    method.setRequestHeader(headerName, request.getHeader(headerName));
                }
            }

            int statusCode = client.executeMethod(method);
            LOG.debug("Status code: " + statusCode);

            if (statusCode == HttpStatus.SC_OK) {

                Header contentEncoding = method.getResponseHeader("Content-Encoding");
                Header contentType = method.getResponseHeader("Content-Type");
                if ( contentType != null ) {
                    response.setContentType(contentType.getValue());
                }
                if (requestPath.endsWith(".fh")) {

                    String htmlBody;
                    if (contentEncoding != null && contentEncoding.getValue().equals("gzip")) {
                        GZIPInputStream zipStream = new GZIPInputStream(method.getResponseBodyAsStream());
                        htmlBody = IOUtils.toString(zipStream);
                    } else {
                        htmlBody = method.getResponseBodyAsString();
                    }

                    // Process HTML
                    //
                    htmlBody = this.processHtml(htmlBody, request);
                    //response.setContentType("text/html");
                    response.getWriter().write(htmlBody);

                } else {

                    // TODO: REFACTOR BELOW CODE

                    if (contentEncoding != null && contentEncoding.getValue().equals("gzip")) {
                        if (isAjax) {
                            GZIPInputStream zipStream = new GZIPInputStream(method.getResponseBodyAsStream());
                            String htmlBody = IOUtils.toString(zipStream);
                            htmlBody = htmlBody.replaceAll("\\.jsp", ".jspfh");  // TODO: Do we need to do this? Only probably when having the connectors co-located with DXA.Java
                            htmlBody = htmlBody.replaceAll("src=\"../../preview/", "src=\"/preview/");
                            response.getWriter().write(htmlBody);
                        } else {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            IOUtils.copy(new GZIPInputStream(method.getResponseBodyAsStream()), byteArrayOutputStream);
                            byte[] responseData = byteArrayOutputStream.toByteArray();
                            this.storeCachedResources(fredhopperUrl, responseData);
                            // TODO: Force the browser to cache the CSS/JS/image resources
                            response.getOutputStream().write(responseData);

                        }
                    } else {
                        if (isAjax) {
                            String htmlBody = IOUtils.toString(method.getResponseBodyAsStream());
                            htmlBody = htmlBody.replaceAll("\\.jsp", ".jspfh");  // TODO: Do we need to do this? Only probably when having the connectors co-located with DXA.Java
                            htmlBody = htmlBody.replaceAll("src=\"../../preview/", "src=\"/preview/");
                            response.getWriter().write(htmlBody);
                        }
                        else {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            IOUtils.copy(method.getResponseBodyAsStream(), byteArrayOutputStream);
                            byte[] responseData = byteArrayOutputStream.toByteArray();
                            this.storeCachedResources(fredhopperUrl, responseData);
                            response.getOutputStream().write(responseData);
                        }
                    }
                }
                response.flushBuffer();
            } else {
                response.sendError(statusCode);
            }

            if (requestPath.endsWith(".fh")) {
                synchronized (this) {
                    this.lastAccessTime = System.currentTimeMillis();
                }
            }
        }
        finally {
            method.releaseConnection();
        }

    }

    /**
     * Proxy POST requests (primarly AJAX requests) from the inline edit popup
     * @param request
     * @param response
     * @throws IOException
     */
    // TODO: Merge this method with the GET method
    @RequestMapping(method = RequestMethod.POST, value = "/**", produces = {MediaType.ALL_VALUE})
    public void postHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {

        boolean sessionOk = this.checkSession(request, response);
        if ( !sessionOk ) {
            return;
        }

        LOG.debug("Proxy POST request: " + request.getRequestURI());

        final String requestPath = request.getRequestURI().replaceFirst("/fh-edit", "");
        final String fredhopperUrl = fredhopperAdminUrl + requestPath + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        PostMethod method = new PostMethod(fredhopperUrl);

        try {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement().toLowerCase();
                if (headerName.startsWith("wicket") || headerName.startsWith("x-") || headerName.startsWith("accept") || headerName.startsWith("user-agent")) {
                    method.setRequestHeader(headerName, request.getHeader(headerName));
                }
            }

            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                method.setParameter(paramName, request.getParameter(paramName));
            }
            int statusCode = client.executeMethod(method);
            LOG.debug("POST status code: " + statusCode);
            if (statusCode == HttpStatus.SC_OK) {
                Header contentType = method.getResponseHeader("Content-Type");
                response.setContentType(contentType.getValue());
                Header contentEncoding = method.getResponseHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equals("gzip")) {
                    IOUtils.copy(new GZIPInputStream(method.getResponseBodyAsStream()), response.getOutputStream());
                } else {
                    IOUtils.copy(method.getResponseBodyAsStream(), response.getOutputStream());
                }
                response.flushBuffer();
            } else {
                response.sendError(statusCode);
            }
        }
        finally {
            method.releaseConnection();
        }
    }


    /**
     * Process the HTML received from Fredhopper GUI. Top and left navigation is removed and as well some
     * buttons that does not make sense in context of the inline edit popup.
     *
     * @param html
     * @param request
     * @return processed HTML
     */
    protected String processHtml(final String html, final HttpServletRequest request) {

        // TODO: List view will be improved in later version to fully support AJAX reload when selection scope etc
        //
        boolean listView = request.getParameter("list") != null;

        // First do some global find-replace
        //
        String processedHtml = html.replaceAll("\\.\\./\\.\\./heatmap", "/heatmap");
        processedHtml = processedHtml.replaceAll("if \\(InMethod &&", "if (false && InMethod &&");

        // Then manipulate the HTML (remove divs etc)
        //
        Document htmlDoc = Jsoup.parse(processedHtml);
        this.removeElementsWithId(htmlDoc, "header");
        this.removeElementsWithId(htmlDoc, "lhsLeftContainer");
        if ( !listView ) {
            this.removeElementsWithClass(htmlDoc, "scope-selection-container");
        }
        this.removeElementsWithId(htmlDoc, "synonymSaveAndCloseButton");
        this.removeElementsWithId(htmlDoc, "saveAsButton");
        this.removeElementsWithId(htmlDoc, "synonymFlagButton");
        this.addStyleToElementsWithId(htmlDoc, "content", "top: 0px;");
        this.addStyleToElementsWithId(htmlDoc, "verticalsplitpanelRightPanel", "margin-left: 0px;");

        this.addEditPopupJavascript(htmlDoc);
        //htmlDoc.body().append("<script src='/system/assets/scripts/fredhopper-edit-popup.js'></script>");

        return htmlDoc.html();
    }

    protected void removeElementsWithId(Document htmlDoc, String id) {
        Elements elements = htmlDoc.body().select("#" + id);
        for ( Element element : elements )  {
            element.remove();
        }
    }

    protected void removeElementsWithClass(Document htmlDoc, String cssClass) {
        Elements elements = htmlDoc.body().select("." + cssClass);
        for ( Element element : elements )  {
            element.remove();
        }
    }

    protected void addStyleToElementsWithId(Document htmlDoc, String id, String style) {
        Elements elements = htmlDoc.body().select("#" + id);
        for ( Element element : elements )  {
            if ( element.hasAttr("style") ) {
                String currentStyle = element.attr("style");
                currentStyle += ";" + style;
                element.attr("style", currentStyle);
            }
            else {
                element.attr("style", style);
            }
        }
    }

    protected void addEditPopupJavascript(Document htmlDoc) {

        StringBuilder jsModifications = new StringBuilder();

        jsModifications.append("<script>$(document).ready(function() {\n");

        if (fredhopperVersion < 8.4) {

            // Remove buttons that does not make sense from the edit popup view
            //
            jsModifications.append(
            "    $(\".toolbar-button-container[title='Archive']\").remove();\n" +
            "    $(\".edit .toolbarShowLabelsDropDown\").remove();\n" +
            "    $(\".toolbar-button-container[title='Assign/remove labels']\").remove();\n");

            // Add events on the save and cancel buttons to close the popup (to avoid end up in the list view)
            //
            jsModifications.append(
            "    $(\"#synonymSaveButton\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "    });\n" +
            "    $(\"#synonymCancelButton\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "    });\n" +
            "    $(\".toolbar-button-container[title='Delete']\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "    });\n");

        }
        else {

            // Modifications for Fredhopper 8.4/8.5 GUI
            //
            jsModifications.append(
            "    var tweakButtons = function() {" +
            "      $(\".save-and-close-button\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "      });\n" +
            "      $(\".save-button\").remove();\n" +
            "      $(\".edit .labels-dropdown-button\").remove();\n" +
            "      $(\".delete-button\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "      });\n" +
            "      $(\".cancel-button\").mouseup(function () {\n" +
            "        setTimeout(function() {\n" +
            "            window.parent.$('.mfp-close').click();\n" +
            "        }, 250);\n" +
            "      });\n" +
            "    };" +
            "    tweakButtons();" +
            "    var activatePressed = function() {\n" +
            "       setTimeout(function() {\n" +
            "           tweakButtons();\n" +
            "           $(\".deactivate-button\").mouseup(deactivatePressed);\n" +
            "       }, 400);\n };" +
            "    var deactivatePressed = function() {\n" +
            "       setTimeout(function() {\n" +
            "           tweakButtons();\n" +
            "           $(\".activate-button\").mouseup(activatePressed);\n" +
            "       }, 400);\n };" +
            "     $(\".deactivate-button\").mouseup(deactivatePressed);"

            );

        }

        jsModifications.append("});</script>");
        htmlDoc.body().append(jsModifications.toString());

        if (fredhopperVersion >= 8.4) {
            htmlDoc.head().append(
                    "<style>" +
                    " #main-container { zoom: 80%; }" +
                    " .k-window { zoom: 80%; }" +
                    " .k-window .wicket-modal { zoom: 100%; }" +
                    " .wicket-modal { zoom: 80%; }" +
                    "</style>");
        }


    }

    /**
     * Read cached Fredhopper resource (CSS,JS,images etc) to optimize the experience of
     * the inline edit popup.
     *
     * @param fredhopperUrl
     * @param response
     * @return
     */
    protected boolean readCachedResource(String fredhopperUrl, HttpServletResponse response) {
        try {
            File cachedAsset = this.getLocalFilename(fredhopperUrl);
            if ( cachedAsset.exists() ) {
                String contentType = tika.detect(cachedAsset);
                if ( contentType.equals("text/x-matlab") ) {
                    contentType = "application/javascript";
                }
                response.setContentType(contentType);
                IOUtils.copy(new FileInputStream(cachedAsset), response.getOutputStream());
                return true;
            }
        }
        catch ( IOException e ) {
            LOG.error("Could read cached resource for URL: " + fredhopperUrl, e);
        }
        return false;
    }

    /**
     * Store a Fredhopper GUI resource (CSS,JS,images etc) on file system to optimize
     * the experience in the inlined edit popup.
     * @param fredhopperUrl
     * @param data
     */
    protected void storeCachedResources(String fredhopperUrl, byte[] data) {
        try {
            File cachedAsset = this.getLocalFilename(fredhopperUrl);
            if ( !cachedAsset.getParentFile().exists() ) {
                cachedAsset.getParentFile().mkdirs();
            }
            if ( cachedAsset.exists() ) {
                cachedAsset.delete();
            }
            IOUtils.copy(new ByteArrayInputStream(data), new FileOutputStream(cachedAsset));
        }
        catch ( IOException e ) {
            LOG.error("Could not store cached resource for URL: " + fredhopperUrl, e);
        }
    }

    /**
     * Get local filename for a cached resource
     * @param fredhopperUrl
     * @return
     */
    protected File getLocalFilename(String fredhopperUrl) {

        // TODO: Use alternative paths here when running it on Spring Boot??

        String filename = fredhopperUrl.replace(fredhopperBaseUrl, "").
                replace("/fredhopper/admin/wicket/resource", "").
                replace("/", "_").
                replace("?", "_").
                replace("=", "_");
        if ( this.cacheDirectory != null ){
            return new File(StringUtils.join(new String[]{
                    this.cacheDirectory, "fredhopper", "assets", filename
            }, File.separator));
        }
        else {
            return new File(StringUtils.join(new String[]{
                    webApplicationContext.getServletContext().getRealPath("/"), "BinaryData", "fredhopper", "assets", filename
            }, File.separator));
        }
    }

}

