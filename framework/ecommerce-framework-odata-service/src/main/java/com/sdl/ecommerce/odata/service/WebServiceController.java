package com.sdl.ecommerce.odata.service;

import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.edm.model.MetaType;
import com.sdl.odata.api.edm.model.Schema;
import com.sdl.odata.api.edm.model.Type;
import com.sdl.odata.api.edm.registry.ODataEdmRegistry;
import com.sdl.odata.controller.AbstractODataController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

/**
 * WebServiceController
 *
 * @author nic
 */
@Controller
@RequestMapping("/ecommerce.svc/**")
public class WebServiceController extends AbstractODataController {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceController.class);

    @Autowired
    private ODataEdmRegistry oDataEdmRegistry;

    @Autowired
    private ODataLocalizationService localizationService;

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {

        // Extract locale from the request URI
        //
        String requestUri = servletRequest.getRequestURI();
        LOG.info("Request: " + requestUri);
        StringTokenizer tokenizer = new StringTokenizer(requestUri, "/(?");
        tokenizer.nextToken();
        String locale = null;
        if ( tokenizer.hasMoreTokens() ) {
            locale = tokenizer.nextToken();
        }
        if ( locale != null && this.localizationService.isLocaleDefined(locale) ) {
            LOG.debug("Using locale: " + locale);

            // Check registered locales instead!!! Have LocaleManager that keeps track of locale specific configuration

            super.service(new LocaleRequestWrapper(servletRequest, locale), servletResponse);
        }
        else {
            super.service(servletRequest, servletResponse);
        }
    }

    protected boolean isType(String name) {
        try {
            EntityDataModel dataModel = oDataEdmRegistry.getEntityDataModel();
            for ( Schema schema : dataModel.getSchemas() ) {
                for (Type type : schema.getTypes() ) {
                    if ( type.getMetaType() == MetaType.ENTITY && type.getName().equals(name) ) {
                        return true;
                    }
                }
            }
        }
        catch ( Exception e ) {
            LOG.error("Could not get EDM type.", e);
        }
        return false;
    }

    /**
     * Simple request wrapper that handles the locale info on the request
     */
    static class LocaleRequestWrapper extends HttpServletRequestWrapper {

        private String requestUriWithoutLocale;
        private String locale;

        LocaleRequestWrapper(HttpServletRequest request, String locale) {
            super(request);
            this.requestUriWithoutLocale = request.getRequestURI().replace("/" + locale, "");
            this.locale = locale;
        }

        @Override
        public String getRequestURI() {
            return this.requestUriWithoutLocale;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> headerNames = Collections.list(super.getHeaderNames());
            headerNames.add("ECommerce-Locale");
            return Collections.enumeration(headerNames);
        }

        @Override
        public String getHeader(String name) {
            if ( name.equals("ECommerce-Locale") ) {
                return this.locale;
            }
            return super.getHeader(name);
        }
    }
}
