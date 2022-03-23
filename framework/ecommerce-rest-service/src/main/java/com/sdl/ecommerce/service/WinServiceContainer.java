package com.sdl.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

public final class WinServiceContainer
{
    private static final Logger LOG = LoggerFactory.getLogger(WinServiceContainer.class);

    public static void start(String[] args)
    {
        LOG.info("Starting E-Commerce Application container");

        SpringApplication springApplication = new SpringApplication(new Object[] { ServiceContainer.class });
        //springApplication.setShowBanner(false);
        springApplication.run(args);

        LOG.info("Spring application container started");
    }

    public static void stop(String[] args)
    {
        LOG.info("Stopping Spring Application container");
        System.exit(0);
    }

    public static void main(String[] args) {
        if ("start".equals(args[0]))
            start(args);
        else if ("stop".equals(args[0]))
            stop(args);
    }
}