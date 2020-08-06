package com.niewj.springboot.controller;

import com.niewj.springboot.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by niewj on 2020/8/5 23:17
 */
@RestController
public class HelloController {

    @Autowired
    private Person person;

    @RequestMapping("/hello")
    public Object hello(){
        return person;
    }
}
