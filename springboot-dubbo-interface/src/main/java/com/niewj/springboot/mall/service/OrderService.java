package com.niewj.springboot.mall.service;

import com.niewj.springboot.mall.model.UserAddress;

import java.util.List;

public interface OrderService {
	
	/**
	 * 初始化订单
	 * @param userId
	 */
	List<UserAddress> prepareOrder(String userId);

}
