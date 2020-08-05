package com.niewj.springboot.controller;

import com.niewj.springboot.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by niewj on 2020/8/4 18:08
 */
@Controller
public class HelloController {

    @RequestMapping("/hello")
    @ResponseBody
    public Object hello(){
        return new User("niewj", 33);
    }
}
