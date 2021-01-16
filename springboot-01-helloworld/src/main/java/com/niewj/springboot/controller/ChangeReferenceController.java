package com.niewj.springboot.controller;

import com.niewj.springboot.model.User;
import com.niewj.springboot.util.UnsafeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by niewj on 2020/9/21 21:25
 */
@Controller
@RequestMapping
public class ChangeReferenceController {

    @RequestMapping("/change")
    public void change(){
        User user1 = new User("user_1", 33);
        System.out.println(user1);

        System.out.println("修改中....");

//        changeRef(user1);

    }

    private static void changeRef(User user1, User user2 ) {
        // 通过反射得到theUnsafe对应的Field对象
        long objectOffset = UnsafeUtils.getObjectOffset(user1);
        UnsafeUtils.swapObject(user1, objectOffset, user2);
    }

    public static void main(String[] args) {
        User user1 = new User("user_1", 33);
        System.out.println(user1);

        System.out.println("修改中....");

        User user2 = new User("user_2", 10);

        changeRef(user1, user2);

        System.out.println(user1);

    }
}
