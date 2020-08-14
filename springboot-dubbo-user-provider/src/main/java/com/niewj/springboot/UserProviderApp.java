package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@EnableDubbo(scanBasePackages = "com.niewj.springboot") // 开启dubbo
@SpringBootApplication
//@ImportResource(locations = "classpath:provider.xml")
public class UserProviderApp {

    public static void main(String[] args) {
        SpringApplication.run(UserProviderApp.class, args);
    }

}
