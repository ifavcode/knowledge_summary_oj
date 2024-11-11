package com.example.ksfluxmain.config;


import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig {

    @Bean
    Map<Integer, FluxSink<Object>> fluxSinkMap() {
        return new HashMap<>();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .callTimeout(120, TimeUnit.SECONDS).build();
    }

}
