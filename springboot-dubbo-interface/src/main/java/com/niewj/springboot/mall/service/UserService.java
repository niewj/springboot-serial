package com.niewj.springboot.mall.service;

import com.niewj.springboot.mall.model.UserAddress;

import java.util.List;

/**
 * 用户服务
 */
public interface UserService {
	
	/**
	 * 按照用户id返回所有的收货地址
	 * @param userId
	 * @return
	 */
	List<UserAddress> getAddresses(String userId);

}
