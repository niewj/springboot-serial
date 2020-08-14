package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.OrderService;
import com.niewj.springboot.mall.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by niewj on 2020/8/14 10:04
 */
@Slf4j
@org.springframework.stereotype.Service
public class OrderServiceImpl implements OrderService {

    /**
     * dubbo Reference, 指向服务提供方
     * 3. 为远程服务生成代理, 然后就可以像使用本地接口一样使用了
     */
    @Reference(check = false)
    private UserService userService;

    @Override
    public List<UserAddress> prepareOrder(String userId) {
        System.out.println("用户id：" + userId);
        //1、查询用户的收货地址
        List<UserAddress> addressList = userService.getAddresses(userId);

        if (CollectionUtils.isEmpty(addressList)) {
            log.error("addressList is empty");
        }
        List<UserAddress> userAddresses = addressList.stream().filter(addr -> addr.getUserId().equals(userId)).collect(Collectors.toList());
        System.out.println("过滤地址后返回");
        return userAddresses;
    }
}
