package cn.guet.ksgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
public class KsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(KsGatewayApplication.class, args);
    }


    @RequestMapping("/")
    public String home(Model model) {
        return "index";
    }
}
