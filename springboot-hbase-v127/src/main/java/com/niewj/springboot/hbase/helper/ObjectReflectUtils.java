package com.niewj.springboot.hbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by   on 2017/9/22.
 */
public class ObjectReflectUtils {
    private static Logger logger = LoggerFactory.getLogger(ObjectReflectUtils.class);
    public static DateFormat DF_ALL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static DateFormat DF_DAY = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据属性名获取属性值
     */
    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            Object value = method.invoke(o);
            return value;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据对象获取map值
     *
     * @param obj
     * @return
     */
    public static Map<String, String> getMap(Object obj) {
        Map<String, String> map = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < fields.length; ++i) {
            try {
                if (!"serialVersionUID".equals(fields[i].getName())) {
                    Object value = getFieldValueByName(fields[i].getName(), obj);
                    if (value != null) {
                        if (fields[i].getType().getSimpleName().equals("Date")) {
                            map.put(fields[i].getName(), sdf.format(((Date) value)));
                        } else {
                            map.put(fields[i].getName(), "" + String.valueOf(value));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ObjectReflectExtUtils.getMap error," + fields[i].getName());
            }
        }

        return map;
    }

    public static Map<String, String> getMap(Object obj, Map<String, String> dfDayMap) {
        Map<String, String> map = new HashMap<String, String>();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (!"serialVersionUID".equals(fields[i].getName())) {
                    Object value = getFieldValueByName(fields[i].getName(), obj);
                    if (value != null) {
                        if (fields[i].getType().getSimpleName().equals("Date")) {
                            if (dfDayMap != null && dfDayMap.get(fields[i].getName()) != null) {
                                map.put(fields[i].getName(), DF_DAY.format((Date) value));
                            } else {
                                map.put(fields[i].getName(), DF_ALL.format((Date) value));
                            }
                        } else {
                            map.put(fields[i].getName(), "" + String.valueOf(value));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ObjectReflectUtils.getMap error," + fields[i].getName());
            }
        }
        return map;
    }

    /**
     * 根据key value返回对象
     *
     * @param bean
     * @param key
     * @param value
     * @return
     */
    public static Object getBeanByKeyValue(Object bean, String key, String value) {

        if (value == null) {
            return bean;
        }
//        Class obj = (Class) bean.getClass();
        Field[] fields = bean.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; ++i) {
            Field f = fields[i];
            f.setAccessible(true);

            try {
                if (!"serialVersionUID".equals(f.getName()) && f.getName().equals(key)) {
                    if (f.getType().getSimpleName().equalsIgnoreCase("Date")) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            // yyyy-MM-dd HH:mm:ss 格式存储
                            f.set(bean, sdf.parse(value));
                        } catch (Exception var13) {
                            ;
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Long")) {
                        try {
                            f.set(bean, Long.parseLong(value));
                        } catch (Exception var12) {
                            ;
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Double")) {
                        try {
                            f.set(bean, Double.parseDouble(value));
                        } catch (Exception var11) {
                            ;
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Integer")) {
                        try {
                            f.set(bean, Integer.parseInt(value));
                        } catch (Exception var10) {
                            ;
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Boolean")) {
                        try {
                            f.set(bean, Boolean.parseBoolean(value));
                        } catch (Exception var9) {
                            ;
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("BigDecimal")) {
                        try {
                            f.set(bean, new BigDecimal(value));
                        } catch (Exception var8) {
                            ;
                        }
                    } else {
                        f.set(bean, value);
                    }
                    break;
                }
            } catch (IllegalAccessException var14) {
                logger.error("ObjectReflectUtils.getBeanByKeyValue error,{}", var14);
            }
        }

        return bean;
    }

    public static Object getBeanByKeyValue(Object bean, String key, String value, Map<String, String> dfDayMap) {
        if (value == null) {
            return bean;
        }
        Class obj = (Class) bean.getClass();
        Field[] fields = bean.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            try {
                if (!"serialVersionUID".equals(f.getName()) && f.getName().equals(key)) {
                    if (f.getType().getSimpleName().equalsIgnoreCase("Date")) {
                        try {
                            if (dfDayMap != null && dfDayMap.get(fields[i].getName()) != null) {
                                f.set(bean, DF_DAY.parse(value));
                            } else {
                                f.set(bean, DF_ALL.parse(value));
                            }
                        } catch (Exception e) {
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Long")) {
                        try {
                            f.set(bean, Long.parseLong(value));
                        } catch (Exception e) {
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Double")) {
                        try {
                            f.set(bean, Double.parseDouble(value));
                        } catch (Exception e) {
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Integer")) {
                        try {
                            f.set(bean, Integer.parseInt(value));
                        } catch (Exception e) {
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("Boolean")) {
                        try {
                            f.set(bean, Boolean.parseBoolean(value));
                        } catch (Exception e) {
                        }
                    } else if (f.getType().getSimpleName().equalsIgnoreCase("BigDecimal")) {
                        try {
                            f.set(bean, new BigDecimal(value));//BigDecimal.parseBoolean(value));
                        } catch (Exception e) {
                        }
                    } else {
                        f.set(bean, value);
                    }
                    break;
                }
            } catch (IllegalAccessException e) {
                logger.error("ObjectReflectUtils.getBeanByKeyValue error,{}", e);
            }
        }
        return bean;
    }

    /**
     * 返回的map中的所有字段都是下划线格式的
     *
     * @param obj
     * @return
     */
    public static Map<String, String> getFormatedMap(Object obj) {
        Map<String, String> databaseFormatedLineMap = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < fields.length; ++i) {
            try {
                if (!"serialVersionUID".equals(fields[i].getName())) {
                    Object value = getFieldValueByName(fields[i].getName(), obj);
                    if (value != null) {
                        if (fields[i].getType().getSimpleName().equals("Date")) {
                            // HbaseConvertUtil.toDatabaseLine把驼峰转下划线
                            databaseFormatedLineMap.put(HbaseConvertUtil.toDatabaseLine(fields[i].getName()), sdf.format(((Date) value)));
                        } else {
                            databaseFormatedLineMap.put(HbaseConvertUtil.toDatabaseLine(fields[i].getName()), String.valueOf(value));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("ObjectReflectExtUtils.getMap error," + fields[i].getName());
            }
        }

        return databaseFormatedLineMap;
    }


}
