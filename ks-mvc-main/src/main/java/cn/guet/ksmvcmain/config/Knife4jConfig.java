package cn.guet.ksmvcmain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Knife4jConfig {

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {

        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("技巧宝典系统API(Mvc)")
                        .version("1.0")
                        .description("用于快速调试API")
                        .termsOfService("https://www.guetzjb.cn")
                        .license(new License().name("Apache 2.0").url("https://www.guetzjb.cn"))
                        .summary("~~~~~~~~~😀"));
    }
}
