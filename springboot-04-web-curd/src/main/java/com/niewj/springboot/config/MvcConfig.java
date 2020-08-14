package com.niewj.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by niewj on 2020/8/7 16:32
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 浏览器发请求 / 请求来到 login 视图;
        registry.addViewController("/").setViewName("login");
        // 浏览器发请求 /test 请求来到 success 视图;
        registry.addViewController("/test").setViewName("success");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.add
    }
}
