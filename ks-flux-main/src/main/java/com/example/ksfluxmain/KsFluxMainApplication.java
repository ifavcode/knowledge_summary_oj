package com.example.ksfluxmain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(scanBasePackages = {"com.example.ksfluxmain", "com.example.kscommon"})
@MapperScan(basePackages = "com.example.ksfluxmain.mapper")
@EnableFeignClients(basePackages = "com.example.ksfluxmain.feign")
public class KsFluxMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(KsFluxMainApplication.class, args);
    }

}
