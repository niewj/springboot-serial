package com.niewj.springboot.mall.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户地址
 */
@Data
@AllArgsConstructor
public class UserAddress implements Serializable {
	private String userId;
    private String address; //用户地址
    private String username; //收货人
    private String phone; //电话号码
}