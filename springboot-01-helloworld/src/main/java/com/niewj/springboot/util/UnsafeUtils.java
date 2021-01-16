package com.niewj.springboot.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by niewj on 2020/9/21 22:52
 */
public class UnsafeUtils {

    private static Unsafe unsafe;

    static {
        try {
            // 通过反射得到theUnsafe对应的Field对象
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true); // 设置该Field为可访问
            // 通过Field得到对应的对象，传入null是因为该Field为static
            unsafe = (Unsafe) field.get(null);
            System.out.println(unsafe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回jvm中给定地址的对象offset
     * @param o
     * @return
     */
    public static long getObjectOffset(Object o){

        Object[] array = new Object[] { o };

        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        int addressSize = unsafe.addressSize();
        long objectAddress;
        switch (addressSize) {
            case 4:
                objectAddress = unsafe.getInt(array, baseOffset);
                break;
            case 8:
                objectAddress = unsafe.getLong(array, baseOffset);
                break;
            default:
                throw new Error("unsupported address size: " + addressSize);
        }
        return (objectAddress);
    }

    public static void  swapObject(Object obj1, long obj1Offset, Object obj2){
         unsafe.getAndSetObject(obj1, obj1Offset, obj2);
    }

}
