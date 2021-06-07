package com.niewj.springboot.hbase.helper;


import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * HBase helper类
 */
public class HBaseHelper {
    private static final Logger LOG = LoggerFactory.getLogger(HBaseHelper.class);

    public final static String COLENDCHAR = String.valueOf(KeyValue.COLUMN_FAMILY_DELIMITER);// ":"列簇和列之间的分隔符
    private final static int COLUMN_MAX_VERSION = 1;
    private static HConnection conn = null;
    private static final Object GET_CONN_LOCK = new Object();
    private static HBaseAdmin hbaseAdmin = null;
    private static final Object GET_HBASE_ADMIN_LOCK = new Object();
    private static Configuration conf = null;

    private HBaseHelper() {
    }

    public static Configuration getConf() {
        if (conf == null) {
            synchronized (HBaseHelper.class) {
                if (conf == null) {
                    //conf = HBaseConfiguration.create();
                    Configuration hbaseConfig = new Configuration();
                    hbaseConfig.set("hbase.zookeeper.quorum", "127.0.0.1");
                    hbaseConfig.set("hbase.zookeeper.property.clientPort", "2181");
                    hbaseConfig.set("hbase.client.write.buffer", "6291456");
                    hbaseConfig.set("hbase.client.scanner.caching", "500");
                    conf = HBaseConfiguration.create(hbaseConfig);
                }
            }
        }

        return conf;
    }

    public static HConnection getConn() {
        if (conn == null) {
            synchronized (GET_CONN_LOCK) {
                if (conn == null) {
                    try {
                        conn = HConnectionManager.createConnection(getConf());
                    } catch (Exception e) {
                        LOG.error("Exception->\n {}", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return conn;
    }

    public static HBaseAdmin getHBaseAdmin() {
        if (hbaseAdmin == null) {
            synchronized (GET_HBASE_ADMIN_LOCK) {
                if (hbaseAdmin == null) {
                    try {
                        hbaseAdmin = new HBaseAdmin(getConf());
                    } catch (Exception e) {
                        LOG.error("Exception->\n {}", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return hbaseAdmin;
    }

    public static HTableInterface getHTable(String tableName) {
        HTableInterface t = null;
        try {
            t = getConn().getTable(tableName);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            try {
                t = getConn().getTable(tableName);
            } catch (IOException e1) {
                LOG.error("Exception->\n {}", e);
                throw new RuntimeException("getHTable error, tableName:" + tableName, e1);
            }
        }

        return t;
    }

    public static void createHTable(String tableName, String fml) {
        String[] fmls = new String[]{fml};
        createHTable(tableName, fmls);
    }

    public static void createHTable(String tableName, String[] fmls) {
        try {
            if (getHBaseAdmin().tableExists(tableName)) {
                return;// 判断表是否已经存
            }
            HTableDescriptor htdesc = createHTDesc(tableName);
            // htdesc.setInMemory(true);
            for (int i = 0; i < fmls.length; i++) {
                String fml = fmls[i];
                addFamily(htdesc, fml, false);
            }
            getHBaseAdmin().createTable(htdesc);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("createHTable error, tableName:" + tableName, e);
        }
    }

    /**
     * 方法描述：删除一条记录的
     * 如果colName＝null，则删除列簇
     */
    public static void deleteColumn(String tableName, String rowKey, String family, String colName) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowId:" + rowKey + ",fml:" + family + ",colName:" + colName;

        HTableInterface hTable = null;
        try {
            Delete del = new Delete(rowKey.getBytes());

            if (colName == null || "".equals(colName)) {
                del.deleteFamily(family.getBytes());
            } else {
                del.deleteColumns(family.getBytes(), colName.getBytes());
            }

            hTable = getHTable(tableName);
            hTable.delete(del);
            hTable.flushCommits();
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("deleteColumn error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "deleteColumn " + info, start);
        }
    }

    public static void deleteColumns(String tableName, String rowKey, String family, List<String> columns) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey:" + rowKey;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            List<Delete> list = new ArrayList<Delete>();
            for (String column : columns) {
                Delete delete = new Delete(Bytes.toBytes(rowKey));
                delete.deleteColumns(Bytes.toBytes(family), Bytes.toBytes(column));
                list.add(delete);
            }
            hTable.delete(list);
        } catch (Exception e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("deleteColumns error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "deleteColumns " + info, start);
        }
    }

    /**
     * 方法描述：删除一行记
     *
     * @param tableName
     * @param rowKey
     * @throws IOException 返回值类void
     * @Exception 异常对象
     */
    public static void deleteRow(String tableName, String rowKey) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            List list = new ArrayList();
            Delete del = new Delete(rowKey.getBytes());
            list.add(del);
            hTable.delete(list);
            hTable.flushCommits();
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("deleteRow error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "deleteRow " + info, start);
        }
    }

    public static boolean isExist(String tableName, String rowKey) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            return hTable.exists(get);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("isExist error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "isExist " + info, start);
        }
    }

    public static Map<String, String> getValue(String tableName, String rowKey, String family, List<String> colNames) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey + ",family:" + family;

        HTableInterface hTable = null;
        Map<String, String> columnValueMap = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            Result result = hTable.get(get);
            if (result != null && !result.isEmpty() && colNames != null && colNames.size() > 0) {
                columnValueMap = new HashMap<String, String>();
                byte[] bytesValue = null;
                String value = null;
                for (String colName : colNames) {
                    bytesValue = result.getValue(family.getBytes(), colName.getBytes());
                    if (bytesValue != null) {
                        value = Bytes.toString(bytesValue);
                    } else {
                        value = "";
                    }
                    columnValueMap.put(colName, value);
                }
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnValue error, " + info, e);
        } finally {
            closeHTable(hTable);
            long end = System.currentTimeMillis();
            SlowLogUtils.logHBaseResTime(tableName, "getColumnValue " + info, end - start);
        }
        return columnValueMap;
    }

    public static String getValue(String tableName, String rowKey, String family, String colName) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey + ",family:" + family + ",colName:" + colName;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            Result result = hTable.get(get);
            byte[] b = result.getValue(family.getBytes(), colName.getBytes());
            if (b == null) {
                return "";
            } else {
                return Bytes.toString(b);
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getValue error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getValue " + info, start);
        }
    }

    public static byte[] getByteValue(String tableName, String rowKey, String family, String colName) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey + ",family:" + family + ",colName:" + colName;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            Result result = hTable.get(get);
            return result.getValue(family.getBytes(), colName.getBytes());
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getByteValue error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getByteValue " + info, start);
        }
    }

    /**
     * 方法描述：获得行记录
     *
     * @param tableName
     * @return
     * @throws IOException 返回值类Result
     * @Exception 异常对象
     */
    public static Result getResult(String tableName, String rowKey) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey;

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            result = hTable.get(get);
        } catch (IOException e) {
            LOG.error("getResult error, " + info, e);
            throw new RuntimeException("getResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResult " + info, start);
        }
        return result;
    }

    public static Result getResult(String tableName, String family, String rowKey) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family" + family + ",rowKey:" + rowKey;

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addFamily(Bytes.toBytes(family));
            result = hTable.get(get);
        } catch (IOException e) {
            LOG.error("getResult error, " + info, e);
            throw new RuntimeException("getResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResult " + info, start);
        }
        return result;
    }

    public static Result getResultInTwoFml(String tableName, String family1, String family2, String rowKey) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family1" + family1 + ",family2" + family2 + "rowKey:" + rowKey;

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addFamily(Bytes.toBytes(family1));
            get.addFamily(Bytes.toBytes(family2));
            result = hTable.get(get);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResult " + info, start);
        }
        return result;
    }

    public static List<Result> getResult(String tableName, String rowkeyStartPrefix, int maxResultCount, String family) throws Exception {
        List<Result> resultList = new ArrayList<Result>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            scan.setRowPrefixFilter(rowkeyStartPrefix.getBytes());
            if (StringUtils.isNotEmpty(family)) {
                scan.addFamily(Bytes.toBytes(family));
            }
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                if (result.isEmpty()) {
                    break;
                }
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("Exception->\n {}", e);
            }
            closeHTable(hTable);
        }
        return resultList;
    }

    public static List<Result> getResult(String tableName, String rowkeyStart, String rowkeyEnd, int maxResultCount, String family) throws Exception {
        List<Result> resultList = new ArrayList<Result>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotEmpty(rowkeyStart)) {
                scan.setStartRow(Bytes.toBytes(rowkeyStart));
            }
            if (StringUtils.isNotEmpty(rowkeyEnd)) {
                scan.setStopRow(Bytes.toBytes(rowkeyEnd));
            }
            if (StringUtils.isNotEmpty(family)) {
                scan.addFamily(Bytes.toBytes(family));
            }
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                if (result.isEmpty()) {
                    break;
                }
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("Exception->\n {}", e);
            }
            closeHTable(hTable);
        }
        return resultList;
    }

    /**
     * 根据rowkey起始字符串
     *
     * @param tableName
     * @param rowkeyStart
     * @param rowkeyEnd
     * @param maxResultCount
     * @return
     */
    public static List<Result> getResultsByRowkeyRange(String tableName, String rowkeyStart, String rowkeyEnd, int maxResultCount,
                                                       String family, String timestamp, String versionNum) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowkeyStart:" + rowkeyStart + ",rowkeyEnd:" + rowkeyEnd + ",maxResultCount:" + maxResultCount
                + ",family:" + family + ",timestamp:" + timestamp + ",versionNum:" + versionNum;

        List<Result> resultList = new ArrayList<Result>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotEmpty(rowkeyStart)) {
                scan.setStartRow(Bytes.toBytes(rowkeyStart));
            }
            if (StringUtils.isNotEmpty(rowkeyEnd)) {
                scan.setStopRow(Bytes.toBytes(rowkeyEnd));
            }
            if (StringUtils.isNotEmpty(family)) {
                scan.addFamily(Bytes.toBytes(family));
            }
            if (StringUtils.isNotEmpty(timestamp)) {
                scan.setTimeStamp(Long.parseLong(timestamp));
            }
            if (StringUtils.isNotEmpty(versionNum)) {
                scan.setMaxVersions(Integer.parseInt(versionNum));
            }
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                if (result.isEmpty()) {
                    break;
                }
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } catch (IOException e) {
            LOG.error("getRwoResultsByRowkeyRange error, " + info, e);
            throw new RuntimeException("getRwoResultsByRowkeyRange error, " + info, e);
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getRwoResultsByRowkeyRange " + info, start);
        }
        return resultList;
    }

    /**
     * 根据rowKeys批量获取查询数据
     *
     * @param tableName
     * @return
     */
    public static Result[] getResult(String tableName, List<String> rowKeys) {
        return getResult(tableName, rowKeys, null, null);
    }

    /**
     * 根据rowId批量获取查询数据，可指定查询的列族
     *
     * @param tableName
     * @param family    查询的列族
     * @return
     */
    public static Result[] getResult(String tableName, List<String> rowKeys, String family) {
        return getResult(tableName, rowKeys, family, null);
    }

    /**
     * 根据rowId批量获取查询数据，可指定一个列族和该列族下的部分列，查询结果中只包含指定列的数据
     *
     * @param tableName
     * @param family    查询的列族
     * @param columns   参数family下的列，如果指定了列，则必须指定列族family
     * @return
     */
    public static Result[] getResult(String tableName, List<String> rowKeys, String family, List<String> columns) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family" + family;

        if (rowKeys == null || rowKeys.size() == 0) {
            return null;
        }

        if (columns != null && columns.size() > 0) {
            if (family == null || family.equals("")) {
                throw new RuntimeException("参数异常，指定查询列的同时需指定列族名称！");
            }
        }

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            List<Get> gets = new ArrayList<Get>();
            byte[] f = (family == null || family.equals("")) ? null : family.getBytes();
            for (String row : rowKeys) {
                if (row == null) {
                    throw new RuntimeException("参数rowIDs中包含有null值");
                }

                Get get = new Get(row.getBytes());
                if (columns != null && columns.size() > 0) {
                    for (String col : columns) {
                        get.addColumn(f, col.getBytes());
                    }
                } else if (f != null) {
                    get.addFamily(f);
                }

                gets.add(get);
            }

            return hTable.get(gets);
        } catch (IOException e) {
            LOG.error("getResult error, " + info, e);
            throw new RuntimeException("getResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResult rowKeys count:" +
                    (rowKeys == null ? 0 : rowKeys.size()) + " " + family + " columns count:" + (columns == null ? 0 : columns.size()), start);
        }
    }

//    /**
//     * 根据组合条件获得结果集列表
//     *
//     * @param tableName
//     * @param compareOpEntityList
//     * @return
//     */
//    public static List<Result> getResultsByCondition(String tableName, List<CompareOpEntity> compareOpEntityList) {
//        return getResultsByCondition(tableName, compareOpEntityList, Integer.MAX_VALUE);
//    }
//
//    /**
//     * 根据组合条件获得结果集列表
//     *
//     * @param tableName
//     * @param compareOpEntityList
//     * @param maxResultCount      获得结果列表数量的最大值
//     * @return
//     */
//    public static List<Result> getResultsByCondition(String tableName, List<CompareOpEntity> compareOpEntityList, int maxResultCount) {
//        long start = System.currentTimeMillis();
//        String info = "tableName:" + tableName + ",compareOpEntityList count:" + (compareOpEntityList == null ? 0 : compareOpEntityList.size())
//                + ",maxResultCount:" + maxResultCount;
//
//        List<Result> resultList = new ArrayList<Result>();
//        HTableInterface hTable = null;
//        ResultScanner resultScanner = null;
//        int count = 0;
//        try {
//            hTable = getHTable(tableName);
//            List<Filter> filters = new ArrayList<Filter>();
//            for (CompareOpEntity compareOpEntity : compareOpEntityList) {
//                Filter filter = new SingleColumnValueFilter(Bytes.toBytes(compareOpEntity.getFamilyName()), Bytes.toBytes(compareOpEntity
//                        .getColumnName()), compareOpEntity.getCompareOp(), Bytes.toBytes(compareOpEntity.getCompareValue()));
//                filters.add(filter);
//            }
//            FilterList filterList1 = new FilterList(filters);
//            Scan scan = new Scan();
//            scan.setFilter(filterList1);
//            resultScanner = hTable.getScanner(scan);
//            for (Result result : resultScanner) {
//                resultList.add(result);
//                count++;
//                if (count > maxResultCount) {
//                    break;
//                }
//            }
//        } catch (IOException e) {
//            LOG.error("getResultsByCondition error, " + info, e);
//            throw new RuntimeException("getResultsByCondition error, " + info, e);
//        } finally {
//            try {
//                if (resultScanner != null) {
//                    resultScanner.close();
//                }
//            } catch (Exception e) {
//                LOG.error("", e);
//            }
//            closeHTable(hTable);
//            SlowLogUtils.logHBaseResTime(tableName, "getResultsByCondition " + info, start);
//        }
//        return resultList;
//    }

    /**
     * 根据rowkey范围只返回其对应的rowkey(最大查询记录条数100000)
     *
     * @param tableName
     * @param rowkeyStart
     * @param rowkeyEnd
     * @return
     */
    public static List<String> getTableRowkeysByRowkeyRange(String tableName, String rowkeyStart, String rowkeyEnd, boolean isCache) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowkeyStart:" + rowkeyStart + ",rowkeyEnd:" + rowkeyEnd + ",isCache:" + isCache;

        if (StringUtils.isEmpty(rowkeyStart) || StringUtils.isEmpty(rowkeyEnd)) {
            throw new IllegalArgumentException("rowkeyStart or rowkeyEnd is null");
        }

        List<String> resultList = new ArrayList<String>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            scan.setCacheBlocks(isCache);
            scan.setStartRow(Bytes.toBytes(rowkeyStart));
            scan.setStopRow(Bytes.toBytes(rowkeyEnd));
            Filter filter = new FirstKeyOnlyFilter();
            scan.setFilter(filter);
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                String rowKey = Bytes.toString(result.getRow());
                LOG.debug("rowKey = {}", rowKey);
                resultList.add(rowKey);
                count++;
                if (count > 100000) {
                    break;
                }
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getTableRowkeysByRowkeyRange error, " + info, e);
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("Exception->\n {}", e);
            }
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getTableRowkeysByRowkeyRange " + info, start);
        }
        return resultList;
    }

    /**
     * 根据rowkey起始字符串
     *
     * @param tableName
     * @param rowkeyStart
     * @param rowkeyEnd
     * @param maxResultCount
     * @return
     */
    public static List<Result> getResultsByRowkeyRange(String tableName, String rowkeyStart, String rowkeyEnd, int maxResultCount) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowkeyStart:" + rowkeyStart + ",rowkeyEnd:" + rowkeyEnd + ",maxResultCount:" + maxResultCount;

        List<Result> resultList = new ArrayList<Result>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            if (StringUtils.isNotEmpty(rowkeyStart)) {
                scan.setStartRow(Bytes.toBytes(rowkeyStart));
            }
            if (StringUtils.isNotEmpty(rowkeyEnd)) {
                scan.setStopRow(Bytes.toBytes(rowkeyEnd));
            }
            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getRwoResultsByRowkeyRange error, " + info, e);
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("Exception->\n {}", e);
            }
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getRwoResultsByRowkeyRange " + info, start);
        }
        return resultList;
    }

    public static Result getResultByRowkeyRangeTime(String tableName, String family, String rowKey, long startTime, long endTime) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey:" + rowKey + ",startTime:" + startTime + ",endTime" + endTime;

        Result result = null;
        HTableInterface hTable = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);

            Get g = new Get(Bytes.toBytes(rowKey));
            g.addFamily(Bytes.toBytes(family));

            if (startTime > 0 && endTime > 0) {
                g.setTimeRange(startTime, endTime);
            }
            result = hTable.get(g);

            return result;
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getResultByRowkeyRangeTime error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResultByRowkeyRangeTime " + info, start);
        }
    }

    public static List<Result> getResultByCondition(String tableName, String family, FilterList filterList, int maxResultCount) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",filterList count:" + filterList == null ? "null" : filterList.toString()
                + ",maxResultCount:" + maxResultCount;

        List<Result> resultList = null;
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            Scan scan = new Scan();
            scan.setFilter(filterList);
            resultScanner = hTable.getScanner(scan);

            for (Result result : resultScanner) {
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getResultByCondition error," + info, e);
        } finally {
            try {
                if (resultScanner != null) {
                    resultScanner.close();
                }
            } catch (Exception e) {
                LOG.error("Exception->\n {}", e);
            }
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResultByCondition " + info, start);
        }
        return resultList;
    }

    /**
     * 获取确定行的特定列的所有版本值
     *
     * @param tableName
     * @return
     * @throws IOException
     */
    public static List<KeyValue> getAllValues(String tableName, String rowKey, String family, String colName) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",colName:" + colName;

        Result result = null;
        HTableInterface hTable = null;
        List<KeyValue> keyValues = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.setMaxVersions(COLUMN_MAX_VERSION);
            result = hTable.get(get);
            keyValues = result.getColumn(Bytes.toBytes(family), Bytes.toBytes(colName));
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnAllValues error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getColumnAllValues " + info, start);
        }
        return keyValues;
    }

    public static Result getResult(String tableName, String family, String rowKey, String... colNames) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",colNames count:" + (colNames == null ? 0 : colNames.length);

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            for (String col : colNames) {
                get.addColumn(Bytes.toBytes(family), Bytes.toBytes(col));
            }
            result = hTable.get(get);

        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnsResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getColumnsResult " + info, start);
        }
        return result;
    }

    public static Result getResultByColumnPrefixs(String tableName, String family, String rowKey, String... colPrefixs) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",colPrefixs count:" + (colPrefixs == null ? 0 : colPrefixs.length);

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            byte[][] prefixes = Bytes.toByteArrays(colPrefixs);
            MultipleColumnPrefixFilter filter = new MultipleColumnPrefixFilter(prefixes);
            Get get = new Get(Bytes.toBytes(rowKey));// 根据rowkey查询
            get.setFilter(filter);
            result = hTable.get(get);
        } catch (Exception ex) {
            LOG.error("Exception->\n {}", ex);
            throw new RuntimeException("getResultByColumnPrefixs error," + info, ex);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResultByColumnPrefixs " + info, start);
        }
        return result;

    }

    public static Result getResult(String tableName, String family, String rowKey, String colName) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",colName" + colName;

        Result result = null;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addColumn(Bytes.toBytes(family), Bytes.toBytes(colName));
            result = hTable.get(get);

        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnResult error, " + info, e);
        } finally {
            closeHTable(hTable);
            long end = System.currentTimeMillis();
            SlowLogUtils.logHBaseResTime(tableName, "getColumnResult " + info, start);
        }
        return result;
    }

    /**
     * 根据表明，rowKey，列簇，获取该列簇下所有值
     *
     * @param tableName
     * @param family
     * @return
     */
    public static List<KeyValue> getValues(String tableName, String rowKey, String family) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",family" + family;

        Result result = null;
        HTableInterface hTable = null;
        List<KeyValue> keyValues = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            // old.setMaxVersions(COLUMN_MAX_VERSION);
            get.addFamily(Bytes.toBytes(family));
            result = hTable.get(get);
            KeyValue[] kvs = result.raw();
            if (kvs.length > 0) {
                keyValues = new ArrayList<KeyValue>();
                Collections.addAll(keyValues, kvs);
            }
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnValues error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getColumnValues " + info, start);
        }
        return keyValues;
    }

    public static List<KeyValue> getValues(String tableName, String rowKey, String family, String startTime, String endTime) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",family" + family + ",startTime:" + startTime + ",endTime:" + endTime;

        Result result = null;
        HTableInterface hTable = null;
        List<KeyValue> keyValues = null;
        try {
            hTable = getHTable(tableName);
            Get get = new Get(rowKey.getBytes());
            get.addFamily(Bytes.toBytes(family));
            get.setTimeRange(convertStr2Date(startTime).getTime(), convertStr2Date(endTime).getTime());
            result = hTable.get(get);
            KeyValue[] kvs = result.raw();
            if (kvs.length > 0) {
                keyValues = new ArrayList<KeyValue>();
                Collections.addAll(keyValues, kvs);
            }
        } catch (Exception e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("getColumnValues error," + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getColumnValues " + info, start);
        }
        return keyValues;
    }

    public static void insertAndUpdate(String tableName, String rowKey, String family, String colName, String value) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",family" + family + ",colName:" + colName + ",value:" + value;
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Put p = new Put(Bytes.toBytes(rowKey));

            p.add(Bytes.toBytes(family), (colName != null ? Bytes.toBytes(colName) : null), Bytes.toBytes(value));
            hTable.put(p);
        } catch (IOException e) {
            LOG.error("insertAndUpdate error, " + info, e);
            throw new RuntimeException("insertAndUpdate error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate " + info, start);
        }
    }

    public static void insertAndUpdate(String tableName, String rowKey, String family, String colName, String value, long timestamp) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",family:" + family + ",rowKey" + rowKey + ",family" + family + ",colName:" + colName + ",value:" + value + ", timestamp:" + timestamp;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Put p = new Put(Bytes.toBytes(rowKey), timestamp);
            p.add(Bytes.toBytes(family), (colName != null ? Bytes.toBytes(colName) : null), Bytes.toBytes(value));
            hTable.put(p);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("insertAndUpdate error, " + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate " + info, start);
        }
    }

    public static void deleteRows(String tableName, final List<String> keys) {
        final List<Delete> ks = new ArrayList<Delete>();
        for (String key : keys) {
            ks.add(new Delete(key.getBytes()));
        }
        long start = System.currentTimeMillis();
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            hTable.delete(ks);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("deleteRows error, ", e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate ", start);
        }
    }

    //    public static void insertAndUpdate(String tableName, String rowKey, InsertData insertData) {
    public static void insertAndUpdate(String tableName, String rowKey) {
        long start = System.currentTimeMillis();
//        String info = "tableName:" + tableName + ",rowKey:" + rowKey + ",insertData:" + insertData.toString();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Put p = new Put(Bytes.toBytes(rowKey));
//            insertData.addData(p);
            hTable.put(p);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("insertAndUpdate error," + info, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate " + info, start);
        }
    }

    /**
     * 插入更新操作
     *
     * @param tableName       表名
     * @param familyAndValues 列簇值map，外层key是ColumnFamily key，内层key是Column key
     *                        当ColumnFamily作为整列时，key"
     * @throws IOException
     */
    public static void insertAndUpdate(String tableName, String rowKey, Map<String, Map<String, String>> familyAndValues) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowKey:" + rowKey;

        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            Put p = new Put(Bytes.toBytes(rowKey));
            Set<String> keySet = familyAndValues.keySet();
            for (String family : keySet) {
                Map<String, String> colunmMap = (Map<String, String>) familyAndValues.get(family);
                if (colunmMap.containsKey("")) {
                    p.add(Bytes.toBytes(family), Bytes.toBytes(""), Bytes.toBytes((String) colunmMap.get("")));
                    continue;
                } else {
                    Set<String> keySet2 = colunmMap.keySet();
                    for (String qualifier : keySet2) {
                        String value = (String) colunmMap.get(qualifier);
                        if (value == null)
                            value = "";
                        p.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
                    }
                }
            }

            hTable.put(p);
        } catch (IOException e) {
            LOG.error("insertAndUpdate error, " + info, e);
            throw new RuntimeException(e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate " + info, start);
        }
    }

    public static void insertAndUpdate(String tableName, List<PutInfo> infos) {
        if (infos == null) {
            return;
        }

        long start = System.currentTimeMillis();
        HTableInterface hTable = null;
        try {
            hTable = getHTable(tableName);
            List<Put> puts = new ArrayList<Put>(infos.size());
            for (PutInfo putInfo : infos) {
                Put p = new Put(Bytes.toBytes(putInfo.getRowKey()));
                puts.add(p);
                Map<String, Map<String, String>> familyAndValues = putInfo.getFamilyAndValues();
                Set<String> keySet = familyAndValues.keySet();
                for (String family : keySet) {
                    Map<String, String> colunmMap = (Map<String, String>) familyAndValues.get(family);
                    if (colunmMap.containsKey("")) {
                        p.add(Bytes.toBytes(family), Bytes.toBytes(""), Bytes.toBytes((String) colunmMap.get("")));
                        continue;
                    } else {
                        Set<String> keySet2 = colunmMap.keySet();
                        for (String qualifier : keySet2) {
                            String value = (String) colunmMap.get(qualifier);
                            if (value == null)
                                value = "";
                            p.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
                        }
                    }
                }

            }
            hTable.put(puts);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("insertAndUpdate error, tableName:" + tableName, e);
        } finally {
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "insertAndUpdate", start);
        }
    }

    /**
     * 删除表的列族
     */
    public static void removeFamily(String tableName, String family) {
        long start = System.currentTimeMillis();

        try {
            String tmp = fixColName(family);
            if (getHBaseAdmin().isTableAvailable(tableName)) {
                getHBaseAdmin().disableTable(tableName);
            }

            getHBaseAdmin().deleteColumn(tableName, tmp);
            getHBaseAdmin().enableTable(tableName);
        } catch (IOException e) {
            LOG.error("Exception->\n {}", e);
            throw new RuntimeException("removeFamily error, tableName:" + tableName + ",family:" + family, e);
        } finally {
            SlowLogUtils.logHBaseResTime(tableName, "removeFamily,family:" + family, start);
        }
    }

    private static void addFamily(HTableDescriptor htdesc, String family, final boolean readonly) {
        htdesc.addFamily(createHCDesc(family));
        htdesc.setReadOnly(readonly);
    }

    private static HColumnDescriptor createHCDesc(String family) {
        String tmp = fixColName(family);
        byte[] colNameByte = Bytes.toBytes(tmp);
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(colNameByte);
        hColumnDescriptor.setMaxVersions(COLUMN_MAX_VERSION);
        return hColumnDescriptor;
    }

    private static String fixColName(String family, String colName) {
        if (colName == null || colName.trim().length() == 0) {
            return family;
        }

        int index = family.indexOf(COLENDCHAR);
        if (index == -1) {
            family += COLENDCHAR + colName;
        }

        return family;
    }

    private static String fixColName(String family) {
        return fixColName(family, null);
    }

    /**
     * 创建表的描述
     *
     * @param tableName
     * @return
     * @throws Exception
     */
    private static HTableDescriptor createHTDesc(final String tableName) {
        return new HTableDescriptor(tableName);
    }

    /**
     * 判断一张表中是否存在某个列族，如果没有则创建该列族
     *
     * @param tbName
     * @param family
     */
    public static void addColumn(String tbName, String family) {
        long start = System.currentTimeMillis();
        try {
            HTableDescriptor desc = getHBaseAdmin().getTableDescriptor(tbName.getBytes());
            HColumnDescriptor[] cdesc = desc.getColumnFamilies();
            for (HColumnDescriptor hd : cdesc) {
                String fml = hd.getNameAsString();
                if (family.equals(fml)) {
                    return;
                }
            }
            HColumnDescriptor col = new HColumnDescriptor(family);
            getHBaseAdmin().disableTable(tbName);
            getHBaseAdmin().addColumn(tbName, col);
            getHBaseAdmin().enableTable(tbName);
        } catch (Exception e) {
            LOG.error("addColumn error, tableName:" + tbName + ",family:" + family, e);
        } finally {
            SlowLogUtils.logHBaseResTime(tbName, "addColumn, family:" + family, start);
        }
    }

//    /**
//     * 根据rowkey起始字符串 和 列组合条件获得结果集列表
//     *
//     * @param tableName
//     * @param rowkeyStart
//     * @param rowkeyEnd
//     * @param compareOpEntityList
//     * @param maxResultCount
//     * @return
//     */
//    public static List<Result> getResultsByRowkeyRangeAndCondition(String tableName, String rowkeyStart, String rowkeyEnd,
//                                                                   List<CompareOpEntity> compareOpEntityList, long maxResultCount) {
//        return getResultsByRowkeyRangeAndCondition(tableName, rowkeyStart, rowkeyEnd, compareOpEntityList, FilterList.Operator.MUST_PASS_ALL,
//                maxResultCount);
//    }
//
//    /**
//     * 根据rowkey起始字符串 和 列组合条件获得结果集列表
//     *
//     * @param tableName
//     * @param rowkeyStart
//     * @param rowkeyEnd
//     * @param compareOpEntityList
//     * @param operator
//     * @param maxResultCount
//     * @return
//     */
//    public static List<Result> getResultsByRowkeyRangeAndCondition(String tableName, String rowkeyStart, String rowkeyEnd,
//                                                                   List<CompareOpEntity> compareOpEntityList, FilterList.Operator operator, long maxResultCount) {
//        long start = System.currentTimeMillis();
//        String info = "tableName:" + tableName + ",rowkeyStart:" + rowkeyStart + ",rowkeyEnd:" + rowkeyEnd + ",maxResultCount:" + maxResultCount;
//
//        List<Result> resultList = new ArrayList<Result>();
//        HTableInterface hTable = null;
//        ResultScanner resultScanner = null;
//        int count = 0;
//        try {
//            hTable = getHTable(tableName);
//            List<Filter> filters = new ArrayList<Filter>();
//            for (CompareOpEntity compareOpEntity : compareOpEntityList) {
//                Filter filter = new SingleColumnValueFilter(Bytes.toBytes(compareOpEntity.getFamilyName()), Bytes.toBytes(compareOpEntity
//                        .getColumnName()), compareOpEntity.getCompareOp(), Bytes.toBytes(compareOpEntity.getCompareValue()));
//                filters.add(filter);
//            }
//            FilterList filterList1 = new FilterList(operator, filters);
//
//            Scan scan = new Scan();
//            if (StringUtils.isNotEmpty(rowkeyStart)) {
//                scan.setStartRow(Bytes.toBytes(rowkeyStart));
//            }
//            if (StringUtils.isNotEmpty(rowkeyEnd)) {
//                scan.setStopRow(Bytes.toBytes(rowkeyEnd));
//            }
//            if (filterList1 != null) {
//                scan.setFilter(filterList1);
//            }
//            resultScanner = hTable.getScanner(scan);
//            for (Result result : resultScanner) {
//                count++;
//                if (count > maxResultCount) {
//                    break;
//                }
//                resultList.add(result);
//            }
//        } catch (IOException e) {
//            LOG.error("getResultsByRowkeyRangeAndCondition error, " + info, e);
//            throw new RuntimeException("getResultsByRowkeyRangeAndCondition erorr, " + info, e);
//        } finally {
//            if (resultScanner != null) {
//                resultScanner.close();
//            }
//            closeHTable(hTable);
//            SlowLogUtils.logHBaseResTime(tableName, "getResultsByRowkeyRangeAndCondition " + info, start);
//        }
//        return resultList;
//    }

    public static List<Result> getResultsByRowkeyRangeAndColumnNames(String tableName, String rowkeyStart, String rowkeyEnd, List<String> columnNames,
                                                                     long maxResultCount) {
        long start = System.currentTimeMillis();
        String info = "tableName:" + tableName + ",rowkeyStart:" + rowkeyStart + ",rowkeyEnd:" + rowkeyEnd + ",maxResultCount:" + maxResultCount;

        List<Result> resultList = new ArrayList<Result>();
        HTableInterface hTable = null;
        ResultScanner resultScanner = null;
        int count = 0;
        try {
            hTable = getHTable(tableName);
            String[] columnArray = new String[columnNames.size()];
            columnNames.toArray(columnArray);

            byte[][] prefixes = Bytes.toByteArrays(columnArray);
            MultipleColumnPrefixFilter filter = new MultipleColumnPrefixFilter(prefixes);

            Scan scan = new Scan();
            scan.setFilter(filter);

            scan.setStartRow(Bytes.toBytes(rowkeyStart));
            scan.setStopRow(Bytes.toBytes(rowkeyEnd));
            if (LOG.isDebugEnabled()) {
                LOG.debug("rowkeyStart:{},rowkeyEnd:{},columnNames:{},columnArray:{}", rowkeyStart, rowkeyEnd,
                        JSONUtil.safeToJson(columnNames), JSONUtil.safeToJson(columnArray));
            }

            resultScanner = hTable.getScanner(scan);
            for (Result result : resultScanner) {
                LOG.debug("count:" + count);
                count++;
                if (count > maxResultCount) {
                    break;
                }
                resultList.add(result);
            }
        } catch (IOException e) {
            LOG.error("getResultsByRowkeyRangeAndColumnNames error, " + info, e);
            throw new RuntimeException("getResultsByRowkeyRangeAndColumnNames error, " + info, e);
        } finally {
            if (resultScanner != null) {
                resultScanner.close();
            }
            closeHTable(hTable);
            SlowLogUtils.logHBaseResTime(tableName, "getResultsByRowkeyRangeAndColumnNames " + info, start);
        }
        return resultList;

    }

    private static void closeHTable(HTableInterface hTable) {
        if (hTable != null) {
            try {
                hTable.close();
            } catch (IOException e) {
                LOG.error("Exception: close hTable error, tableName:" + hTable.getName(), e);
            }
        }
    }

    /**
     * 字符串改为时间
     *
     * @param date
     * @return
     */
    public static Date convertStr2Date(String date) {
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time = null;
        try {
            time = formatDate.parse(date.trim());
        } catch (ParseException e) {
            LOG.error("Exception->\n {}", e);
        }
        return time;
    }

    /**
     * 多行 多列批量插入数据封装
     */
    @Data
    static class PutInfo {
        private String rowKey;
        // <f:<column:value>>
        private Map<String, Map<String, String>> familyAndValues;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        //例子，如果要运行main函数测试hbase，需要在项目的classpath根目录下放hbase的配置文件：hbase-site.xml文件
        //createHTable("upa_data", "fc1");
        String uuid = "6a4fddcfa5ea4f439ede256db4dfbc00";
        Map<String, String> data = new HashMap<String, String>();
        data.put("name", "liudehua");
        data.put("idCard", "123456789012345");
        data.put("bankCard", "62358809208567222");
        data.put("data", "{'key','value'}");
        Map<String, Map<String, String>> insertData = new HashMap<String, Map<String, String>>();
        insertData.put("fc1", data);
        //HBaseHelper.insertAndUpdate("upa_data", uuid+"20171015",insertData);
        List<Result> result = HBaseHelper.getResultsByRowkeyRange("upa_data", "6a4fddcfa5ea4f439ede256db4dfbc0020171000", "6a4fddcfa5ea4f439ede256db4dfbc0020171016", Integer.MAX_VALUE, "fc1", null, null);
        System.out.println(result.size());
        for (Result r : result) {
            System.out.println(new String(r.getValue("fc1".getBytes(), "bankCard".getBytes())) + "##########" + new String(r.getRow()));


        }
//		insertAndUpdate("test_hbase_helper", "1", "f", "col", "testV");
//		System.out.println("get result:"+getValue("test_hbase_helper", "1", "f", "col"));
//		List<Result> rs = getResultsByRowkeyRange("test_hbase_helper","0","z",100);
//		for(Result r : rs) {
//			System.out.println("scan result:"+Bytes.toString(r.getValue("f".getBytes(), "col".getBytes())));
//		}
//		deleteColumn("test_hbase_helper", "1", "f", "col");
//		System.out.println("delete after,"+getValue("test_hbase_helper", "1", "f", "col"));
    }

}
