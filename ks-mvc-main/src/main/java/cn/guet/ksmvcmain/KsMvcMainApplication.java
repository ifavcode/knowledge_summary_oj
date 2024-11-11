package cn.guet.ksmvcmain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication(scanBasePackages = {"com.example.kscommon","cn.guet.ksmvcmain"})
@MapperScan(basePackages = "cn.guet.ksmvcmain.mapper")
public class KsMvcMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(KsMvcMainApplication.class, args);
    }

}
