package com.niewj.springboot.controller;

import com.niewj.springboot.entities.Person;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * Created by niewj on 2020/8/7 9:14
 */
@Controller
public class HelloController {

    @RequestMapping("/hello")
    @ResponseBody
    public Object hello() {
        return "hello";
    }

    @RequestMapping("/success")
    public String success(HttpServletRequest request, Map<String, Object> map) {
        Person oldJack = new Person();
        oldJack.setId(0);
        oldJack.setName("oldJack");
        oldJack.setBirth(new Date(new Date().getTime() - 1261440000000L));
        oldJack.setFather(null);

        Person john = new Person();
        john.setId(1);
        john.setName("John");
        john.setBirth(new Date(new Date().getTime() - 567648000000L));
        john.setFather(oldJack);

        request.setAttribute("v1", "HereReplaceOriginalString");
        request.setAttribute("john", john);
        return "success";
    }
}
