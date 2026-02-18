package com.slack.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaRouteConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String excluded = "api|ws|api-docs|swagger-ui\\.html|swagger-ui";
        registry.addViewController("/{path:^(?!(?:" + excluded + ")$).*$}")
            .setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!(?:" + excluded + ")$).*$}/**/{subpath:[^.]*}")
            .setViewName("forward:/index.html");
    }
}
