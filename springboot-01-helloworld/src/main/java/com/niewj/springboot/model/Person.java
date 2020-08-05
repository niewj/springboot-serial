package com.niewj.springboot.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 注意:
 * 1. 使用 @PropertySource时, @ConfigurationProperties(prefix="person") 亦不能省略!
 * 2. 指定 yml文件是不支持的!
 *
 * Created by niewj on 2020/8/5 0:01
 */
@Data
@Component
//@PropertySource("classpath:/person.yml") // 3 yml文件是不支持的!!
@PropertySource(value = {"classpath:/person.properties"}) // 1 指定自定义的 properties 配置文件
@ConfigurationProperties(prefix = "person") // 2
public class Person {
    private String lastName;
    private Integer age;
    private Boolean male;
    private Date birth;

    private Map<String, Object> maps;
    private List<Object> lists;
    private Dog dog;
}
