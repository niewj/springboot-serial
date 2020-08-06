package com.niewj.springboot.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by niewj on 2020/8/5 23:20
 */
@Data
@Component
@ConfigurationProperties(prefix = "person")
public class Person {

    private Integer age;
    private String lastName;
    private boolean student;
    private String location;

    private List<String> hobbies;
}
