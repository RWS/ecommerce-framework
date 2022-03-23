package com.sdl.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Localization Controller
 *
 * @author nic
 */
@Controller
@Slf4j
public class LocalizationController {

    @Autowired
    private ServletContext servletContext;

    // TODO: Can we somehow have that part of the model???

    @RequestMapping(value = "/ecommerce.svc/graphql/v1/{locale}", method = RequestMethod.POST)
    public void serviceGraphQL(@PathVariable  String locale, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        log.debug("Handling locale: " + locale);
        LocalizationService.setCurrentLocale(locale);
        try {
            servletContext.getRequestDispatcher("/ecommerce.svc/v1/graphql").forward(servletRequest, servletResponse);
        }
        finally {
            LocalizationService.setCurrentLocale(null);
        }
    }

    @RequestMapping(value = "/ecommerce.svc/rest/v1/{locale}/{entity:category|product|editmenu|cart}/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public void serviceRest(@PathVariable String locale, @PathVariable String entity, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        log.debug("Handling locale: " + locale + " Request: " + servletRequest.getRequestURI());
        LocalizationService.setCurrentLocale(locale);

        // TODO: Validate locale so it is defined in the service

        String requestUri = servletRequest.getRequestURI().replace("/" + locale, "");
        try {
            servletContext.getRequestDispatcher(requestUri).forward(servletRequest, servletResponse);
        }
        finally {
            LocalizationService.setCurrentLocale(null);
        }
    }
}
