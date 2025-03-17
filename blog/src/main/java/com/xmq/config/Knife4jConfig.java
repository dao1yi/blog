package com.xmq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("博客系统接口文档")
                        .version("1.0")
                        .description("使用 Spring Boot 3 + Vue 3 开发的博客系统")
                        .contact(new Contact()
                                .name("作者")
                                .email("example@example.com")));
    }
} 