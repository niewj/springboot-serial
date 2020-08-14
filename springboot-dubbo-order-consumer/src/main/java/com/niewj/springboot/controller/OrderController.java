package com.niewj.springboot.controller;

import com.niewj.springboot.mall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by niewj on 2020/8/14 0:41
 */
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/order")
    public Object order(@RequestParam("userId") String userId){
        return orderService.prepareOrder(userId);
    }
}
