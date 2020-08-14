package com.niewj.springboot.service.impl;

import com.niewj.springboot.mall.model.UserAddress;
import com.niewj.springboot.mall.service.UserService;

import java.util.Arrays;
import java.util.List;

/**
 * Created by niewj on 2020/8/14 9:57
 */

/**
 * 4. 暴露的服务, interface供他人调用, 全路径接口名; ref引用实现类的bean id
 */
@org.apache.dubbo.config.annotation.Service // dubbo Service 声明
@org.springframework.stereotype.Service // spring Service
public class UserServiceImpl implements UserService {
    @Override
    public List<UserAddress> getAddresses(String userId) {

        System.out.println("@@--UserServiceImpl........");
        UserAddress addr1 = new UserAddress("1", "北京亦庄开发区1号", "小王", "010-10102010");
        UserAddress addr2 = new UserAddress("2", "北京亦庄开发区2号", "老王", "010-20102010");
        UserAddress addr3 = new UserAddress("3", "西安曲江创意谷1期", "小张", "029-29296259");
        UserAddress addr4 = new UserAddress("4", "西安曲江创意谷2期", "老张", "029-39296259");
        System.out.println("@@--getAddresses-version-调用");
        return Arrays.asList(addr1, addr2, addr3, addr4);
    }
}
