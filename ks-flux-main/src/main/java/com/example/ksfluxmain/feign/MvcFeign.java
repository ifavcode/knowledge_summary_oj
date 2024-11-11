package com.example.ksfluxmain.feign;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "remoteMvcFeign", value = "ks-mvc-main")
public interface MvcFeign {

}
