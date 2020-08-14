package com.niewj.springboot;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo // 开启dubbo
@SpringBootApplication
public class OrderConsumerApp {

    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApp.class, args);
    }

}
