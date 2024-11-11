package com.example.ksfluxmain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserFlux {

    private FluxSink<Object> sink;

    private Flux<String> flux;
}
