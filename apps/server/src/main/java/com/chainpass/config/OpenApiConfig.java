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
                ## 本地数字身份、签名凭证与沙盒多币种账本

                ### 核心功能
                - **DID系统**: `did:chainpass` 本地方法的身份创建、挑战签名验证和吊销
                - **凭证系统**: 自定义 Ed25519 签名 JSON 凭证的签发、验证和状态管理
                - **沙盒账本**: CNY、USD、ETH 测试额度与配置汇率换算；不连接真实资金
                - **审核流程**: 人工审核演示，通过后签发最小披露的审核结论凭证

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
        return List.of(new Server()
            .url("http://localhost:" + serverPort)
            .description("本地服务器"));
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
