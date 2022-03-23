package com.sdl.ecommerce.dxa;

import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.localization.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Localization Redirect Interceptor
 *
 * @author nic
 */
@Component
public class LocalizationRedirectInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private WebRequestContext webRequestContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Localization localization = webRequestContext.getLocalization();
        String requestUri = request.getRequestURI();
        String pathPrefix = localization.getPath();
        if ( requestUri.startsWith(pathPrefix) ) {
            requestUri = requestUri.replaceFirst(pathPrefix, "");
            // TODO: Have these prefixes configurable
            if ( requestUri.startsWith("/c") || requestUri.startsWith("/p") || requestUri.startsWith("/search") || requestUri.startsWith("/ajax/cart") ) {
                // Forward request to custom E-Commerce page controllers
                //
                servletContext.getRequestDispatcher(requestUri).forward(request, response);
                return false;
            }
        }
        return true;
    }
}
