package com.sdl.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * ServiceContainer
 *
 * To avoid problems when quering on slashes, please use the following JVM arguments:
 * -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true
 *
 * @author nic
 */
@SpringBootApplication
@ComponentScan (basePackages = {"com.sdl.ecommerce.service"})
@ImportResource("classpath*:/META-INF/ecommerce-connector.xml")
public class ServiceContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceContainer.class);

    public static void main(String[] args) {

        LOG.info("Starting E-Commerce container");
        SpringApplication springApplication = new SpringApplication(ServiceContainer.class);
        //springApplication.setShowBanner(false);
        springApplication.run(args);
        LOG.info("E-Commerce Service application container started");

    }

}
