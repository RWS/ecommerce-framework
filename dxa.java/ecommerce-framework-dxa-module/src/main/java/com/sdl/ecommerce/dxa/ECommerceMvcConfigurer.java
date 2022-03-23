package com.sdl.ecommerce.dxa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * MVC Configurer.
 * Sets up additional configuration such as interceptors etc.
 *
 * @author nic
 */
@Configuration
public class ECommerceMvcConfigurer extends WebMvcConfigurerAdapter {

    @Autowired
    private LocalizationRedirectInterceptor localizationRedirectInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PreviewDetectionInterceptor());
        registry.addInterceptor(localizationRedirectInterceptor);
    }
}
