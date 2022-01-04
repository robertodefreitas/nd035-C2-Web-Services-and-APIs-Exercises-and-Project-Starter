package com.udacity.vehicles.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
// only by version < 3.0.0
// http://localhost:8211/swagger-ui.html
@EnableSwagger2
// only by version > 3.0.0
// http://localhost:8211/swagger-ui/
//@EnableSwagger2WebMvc
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Vehicles API",
                "This API returns a list of cars.",
                "1.0",
                "http://url.de/tos",
                new Contact("Roberto de Freitas", "my.url.com", "my@email.com"),
                "License of API", "http://url.de/license", Collections.emptyList());
    }

}