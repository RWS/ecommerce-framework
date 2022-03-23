package com.sdl.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

/**
 * OData Localization Service
 *
 * @author nic
 */
@Component
@ConfigurationProperties(prefix = "localization")
public class LocalizationService implements com.sdl.ecommerce.api.LocalizationService {

    private String defaultLocale;

    // TODO: Make it possible to have other types than strings
    private Map<String,Map<String,String>> locales;

    private static InheritableThreadLocal<String> currentLocale = new InheritableThreadLocal<>();

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setLocales(Map<String, Map<String, String>> locales) {
        this.locales = locales;
    }

    public Map<String, Map<String, String>> getLocales() {
        return locales;
    }

    @Override
    public Map<URI, Object> getAllClaims() {
        // TODO: How to we get all context claims here??? Or maybe just a preselected set of claims???
        // This is primarily just needed for passing triggers to Fredhopper
        return null;
    }

    public static void setCurrentLocale(String locale) {
        currentLocale.set(locale);
    }

    @Override
    public String getLocale() {

        String locale = currentLocale.get();
        if ( locale == null ) {
            locale = defaultLocale;
        }
        return locale;
    }

    public boolean isLocaleDefined(String locale) {
        return this.locales.containsKey(locale);
    }

    @Override
    public String getLocalizedConfigProperty(String name) {
        Map<String,String> configProperties = locales.get(this.getLocale());
        if ( configProperties != null ) {
            return configProperties.get(name);
        }
        return null;
    }

}
