package com.niewj.springboot.hbase.helper;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.*;

/**
 * 下划线-驼峰自动转换版本工具:CaseFormat-auto
 *
 * @author niewj
 */
@Slf4j
public class CaseFormatHbaseHelper {

    public static String CF = "f";
    public static final String REF_ROW = "ref_rowkey";
    public static final String STARTKEY_SUFFIX = "_";
    public static final String STOPKEY_SUFFIX = "`";

    /**
     * 格式: 驼峰 自动转为 下划线
     *
     * @param basic
     * @param rowKey
     * @param hbaseTableName
     * @param <T>
     */
    public static <T> void save(T basic, String rowKey, String hbaseTableName) {
        if (basic == null) {
            log.error("结果为空, 跳过hbase存储.");
            return;
        }

        // save Hbase data
        Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String, String>>();
        dataMap.put(CF, getBasic(basic));

        HBaseHelper.insertAndUpdate(hbaseTableName, rowKey, dataMap);
    }

    /**
     * @param basic
     * @param rowKey
     * @param hbaseTableName
     * @param filteredSet    basic中需要忽略的字段（不存储hbase的字段）
     * @param <T>
     */
    public static <T> void saveWithFiltered(T basic, String rowKey, String hbaseTableName, Set<String> filteredSet) {
        if (basic == null) {
            log.error("结果为空, 跳过hbase存储.");
            return;
        }

        // save Hbase data
        Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String, String>>();
        dataMap.put(CF, putFilteredBasic(basic, filteredSet));

        HBaseHelper.insertAndUpdate(hbaseTableName, rowKey, dataMap);
    }

    /**
     * save ref
     */
    public static void saveRef(String refRowkey, String idUnqf, String hTableName) {
        if (StringUtils.isBlank(refRowkey) || StringUtils.isBlank(idUnqf)) {
            log.error("数据为空, 跳过hbase-ref存储.");
            return;
        }

        // 1. ref 表字段:rowkey: uuid_时间戳
        // save Hbase data
        Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String, String>>();

        Map map = new HashMap();
        map.put(REF_ROW, refRowkey);
        dataMap.put(CF, map);

        HBaseHelper.insertAndUpdate(hTableName, idUnqf, dataMap);
    }

    private static <T> Map putFilteredBasic(T vo, Set<String> filterSet) {
        Map<String, String> toAppendMap = new HashMap<>();

        // 1.获取对象字段名和值的map
        Map<String, String> fieldsMap = ObjectReflectUtils.getMap(vo);

        // 2. 有非下划线格式的字段,需要处理
        Iterator<Map.Entry<String, String>> iter = fieldsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();

            // 加入:忽略的字段列表
            if (filterSet != null && filterSet.contains(key)) {
                iter.remove();
                continue;
            }

            // 驼峰字段, 转换为下划线
            String colName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
            toAppendMap.put(colName, value);
            log.info("读取key的值并转换为下划线格式(hbase标准格式): {} --> {}", key, colName);

        }

        return toAppendMap;
    }

    private static <T> Map getBasic(T basic) {
        Map<String, String> toAppendMap = new HashMap<>();

        // 1.获取对象字段名和值的map
        Map<String, String> fieldsMap = ObjectReflectUtils.getMap(basic);

        // 2. 有非下划线格式的字段,需要处理
        Iterator<Map.Entry<String, String>> iter = fieldsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            String value = entry.getValue();

            // 驼峰字段, 转换为下划线
            String colName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, key);
            toAppendMap.put(colName, value);
            log.info("读取key的值并转换为下划线格式(hbase标准格式): {} --> {}", key, colName);
        }

        return toAppendMap;
    }

    public static <T> T queryBasicByIdunqf(String idUnqf, String refTable, String basicTable, Class clazz) {
        // 1. 查 basic 表: [rowkey=idUnqf]
        T basic = null;
        try {
            // 1. 查取 idUnqp_time
            Result hRef = HBaseHelper.getResult(refTable, idUnqf);
            if (hRef.isEmpty()) {
                return null;
            }

            // 2. 查询 ref表, 获取 idUnqp_time
            String ref_rowkey = new String(getBytes(hRef.getValue(CF.getBytes("UTF-8"), REF_ROW.getBytes("UTF-8"))), "UTF-8");
            if (StringUtils.isBlank(ref_rowkey)) {
                return null;
            }

            // 3. 查询 basic 表 [rowkey=idUnqp_time]
            Result hBasic = HBaseHelper.getResult(basicTable, ref_rowkey);
            basic = getBasicFromHbase(hBasic, clazz);

            if (basic != null) {
                log.info("basic_query_from_hbase:{}", JSONUtil.safeToJson(basic));
            }

        } catch (Exception e) {
            throw new RuntimeException("queryBasicByIdunqf Error");
        }

        return basic;
    }

    private static byte[] getBytes(byte[] bytes) {
        if (bytes == null) {
            bytes = new byte[0];
        }
        return bytes;
    }

    public static <T> T queryResultByIdunqf(String idUnqf, String hTableName, Class clazz) {
        // 1. 查 basic 表: [rowkey=idUnqf]
        T vo = null;
        try {
            // 1. 查询 , 获取 idUnqp_time
            Result hBasic = HBaseHelper.getResult(hTableName, idUnqf);

            vo = getBasicFromHbase(hBasic, clazz);

            if (vo != null) {
                log.info("result_query_from_hbase:{}", JSONUtil.safeToJson(vo));
            }
        } catch (Exception e) {
            throw new RuntimeException("queryResultByIdunqf Error");
        }

        return vo;
    }

    /***
     * idUnqp+范围查询: 小写自动转换为驼峰java
     * @param idUnqp
     * @param period
     * @return
     */
    public static <T> List<T> queryBasicListByRange(String hTableName, Class clazz, String idUnqp, Long period) {
        String startKey = Caculate.getRowKeyWithTime(idUnqp, period);
        String stopKey = Caculate.getRowKeyWithTime(idUnqp, 0L);

        List<Result> resultFromHbase = null;
        try {
            // 1. 查basic
            resultFromHbase = HBaseHelper.getResult(hTableName, startKey, stopKey, 10, CF);
        } catch (Exception e) {
            log.error("get_basic_byidunqp_fromhbase_caseFormat table={} error,idUnqp:{}, period:{} \n Exception", hTableName, idUnqp, period, e);
        }
        if (CollectionUtils.isEmpty(resultFromHbase)) {
            log.info("get_basic_byidunqp_fromhbase_caseFormat from hbase is null table={}, idUnqp:{},period:{}", hTableName, idUnqp, period);
            return null;
        }

        List<T> basics = Lists.newArrayList();

        try {
            for (Result dbResult : resultFromHbase) {
                T t = (T) clazz.newInstance();
                if (dbResult != null && dbResult.size() > 0) {
                    NavigableMap<byte[], byte[]> familyMap = dbResult.getFamilyMap(Bytes.toBytes(CF));
                    if (familyMap != null && familyMap.size() > 0) {
                        for (byte[] key : familyMap.keySet()) {
                            // 字段名需要映射为驼峰的:
                            String field = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, Bytes.toString(key));
                            String value = Bytes.toString(familyMap.get(key));
                            t = (T) ObjectReflectUtils.getBeanByKeyValue(t, field, value);
                        }
                    }
                }
                basics.add(t);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return basics;
    }

    /**
     * 重载 queryByRange() 指定rowkey前缀, 查询 prefix_到prifix` 之间的数据, 也就是说查询 下划线开头到结束的数据
     *
     * @param hTableName
     * @param clazz
     * @param rowkeyPrefix
     * @param <T>
     * @return
     */
    public static <T> List<T> queryByRange(String hTableName, Class clazz, String rowkeyPrefix) {
        String startKey = rowkeyPrefix + STARTKEY_SUFFIX;
        String stopKey = rowkeyPrefix + STOPKEY_SUFFIX;
        return queryByRange(hTableName, clazz, startKey, stopKey);
    }

    /**
     * 指定rowkey起始和截止值, 查询区间
     *
     * @param hTableName
     * @param clazz
     * @param startKey
     * @param stopKey
     * @param <T>
     * @return
     */
    public static <T> List<T> queryByRange(String hTableName, Class clazz, String startKey, String stopKey) {
        List<Result> resultFromHbase = null;
        try {
            // 1. 查basic
            resultFromHbase = HBaseHelper.getResult(hTableName, startKey, stopKey, 10, CF);
        } catch (Exception e) {
            log.error("get_by_range_fromhbase table={} error, startkey:{}, stopkey:{} \n Exception", hTableName, startKey, stopKey, e);
        }
        if (CollectionUtils.isEmpty(resultFromHbase)) {
            log.info("get_by_range_fromhbase from hbase is null table={}, startkey:{}, stopKey:{}", hTableName, startKey, stopKey);
            return null;
        }

        List<T> voList = Lists.newArrayList();

        try {
            for (Result dbResult : resultFromHbase) {
                T t = (T) clazz.newInstance();
                if (dbResult != null && dbResult.size() > 0) {
                    NavigableMap<byte[], byte[]> familyMap = dbResult.getFamilyMap(Bytes.toBytes(CF));
                    if (familyMap != null && familyMap.size() > 0) {
                        for (byte[] key : familyMap.keySet()) {
                            // 字段名需要映射为驼峰的:
                            String fValue = Bytes.toString(familyMap.get(key));
                            String fName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, Bytes.toString(key));
                            t = (T) ObjectReflectUtils.getBeanByKeyValue(t, fName, fValue);
                        }
                    }
                }
                voList.add(t);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return voList;
    }

    private static <T> T getBasicFromHbase(Result result, Class clazz) {
        T dim = null;
        if (result != null && result.size() > 0) {
            try {
                dim = (T) clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(Bytes.toBytes(CF));
            if (familyMap == null || familyMap.isEmpty()) {
                return dim;
            }

            for (byte[] key : familyMap.keySet()) {
                String fName = Bytes.toString(key);
                String fValue = Bytes.toString(familyMap.get(key));
                // hbase中的下划线字段, 改为驼峰的
                fName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fName);
                dim = (T) ObjectReflectUtils.getBeanByKeyValue(dim, fName, fValue);
            }
        }
        return dim;
    }
}
