package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@EnableDubbo(scanBasePackages = "com.niewj.springboot") // 开启dubbo, 扫描API注解
@SpringBootApplication
//@ImportResource(locations = "classpath:consumer.xml")
public class OrderConsumerApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApp.class, args);
    }

}
