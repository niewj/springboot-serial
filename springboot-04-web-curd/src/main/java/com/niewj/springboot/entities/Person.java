package com.niewj.springboot.entities;

import lombok.Data;

import java.util.Date;

/**
 * Created by niewj on 2020/8/10 18:21
 */
@Data
public class Person {
    private int id;
    private String name;
    private Date birth;
    private Person father;
}
