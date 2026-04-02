package com.chainpass.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI chainPassOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(servers())
            .addSecurityItem(securityRequirement())
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
            .title("ChainPass API")
            .description("""
                ## 基于区块链的跨境数字身份与合规支付解决方案

                ### 核心功能
                - **DID系统**: 去中心化身份创建、验证、管理
                - **VC系统**: 可验证凭证签发、验证、吊销
                - **支付系统**: 跨境支付、钱包管理、汇率转换
                - **KYC系统**: 身份认证、合规审核

                ### 认证方式
                使用Bearer Token认证，登录后获取access_token，在请求头添加：
                ```
                Authorization: Bearer {access_token}
                ```
                """)
            .version("v2.0.0")
            .contact(new Contact()
                .name("ChainPass Team")
                .email("team@chainpass.io")
                .url("https://chainpass.io"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> servers() {
        return List.of(
            new Server()
                .url("http://localhost:" + serverPort)
                .description("开发服务器"),
            new Server()
                .url("https://api.chainpass.io")
                .description("生产服务器")
        );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT认证，格式: Bearer {token}");
    }
}