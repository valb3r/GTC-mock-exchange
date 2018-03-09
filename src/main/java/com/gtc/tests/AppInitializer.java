package com.gtc.tests;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by Valentyn Berezin on 08.03.18.
 */
@EnableScheduling
@EntityScan("com.gtc.model.provider.domain")
@SpringBootApplication(scanBasePackages = {
        "com.gtc.tests.service",
        "com.gtc.tests.controller",
        "com.gtc.tests.config"
})
public class AppInitializer {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication app = new SpringApplication(AppInitializer.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
