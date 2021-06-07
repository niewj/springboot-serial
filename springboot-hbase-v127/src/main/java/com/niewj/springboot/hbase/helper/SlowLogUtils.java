package com.niewj.springboot.hbase.helper;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录慢日志
 */
public class SlowLogUtils {
    //private static final Logger LOG = LoggerFactory.getLogger(LogConstants.LOG_TYPE_SLOW);//打印到单独的文件
    private static final Logger LOG = LoggerFactory.getLogger(SlowLogUtils.class);

    public static final int DEFAULT_HTTP_RESPONSE_TIME = 300;//ms
    public static final int DEFAULT_HBASE_RESPONSE_TIME = 300;//ms
    public static final int DEFAULT_NEO4J_RESPONSE_TIME = 200;//ms
    public static final int DEFAULT_REDIS_RESPONSE_TIME = 20;//ms

    private static final int LOG_URL_MAX_LENGTH = 300;
    private static final int LOG_INFO_MAX_LENGTH = 300;
    private static final String RESPONSE_TIME_STR = "response time >";
    private static final String OVER_LENGTH_SIGN = "...";
    private static final Gson gson = new Gson();
    ;

    private SlowLogUtils() {
    }


    /**
     * 日志记录超过@{LOG_URL_MAX_LENGTH} ms的http请求
     *
     * @param url，超过300字节长度会被截取
     */
    public static void logHttpResTime(String url, long startTime) {
        long resTime = System.currentTimeMillis() - startTime;
        if (resTime > DEFAULT_HTTP_RESPONSE_TIME) {
            if (!StringUtils.isBlank(url) && url.length() > LOG_URL_MAX_LENGTH) {
                url = url.substring(0, LOG_URL_MAX_LENGTH) + OVER_LENGTH_SIGN;
            }
            LOG.warn("*url {}  " + RESPONSE_TIME_STR + " " + DEFAULT_HTTP_RESPONSE_TIME + " ms , {} ms", url, resTime);
        } else {
            LOG.debug("*url {}  " + RESPONSE_TIME_STR + " " + DEFAULT_HTTP_RESPONSE_TIME + " ms , {} ms", url, resTime);
        }
    }

    /**
     * 日志记录超过@{DEFAULT_REDIS_RESPONSE_TIME}ms的redis请求
     *
     * @param ip
     * @param port
     * @param db
     * @param info，通常包含redis命令和key信息，超过300字节长度会被截取
     */
    public static void logRedisResTime(String ip, int port, int db, String info, long startTime) {
        long resTime = System.currentTimeMillis() - startTime;
        if (resTime > DEFAULT_REDIS_RESPONSE_TIME) {
            LOG.warn("*redis {} {} {} {} " + RESPONSE_TIME_STR + " " +
                    DEFAULT_REDIS_RESPONSE_TIME + " ms , {} ms", ip, port, db, cutString(info), resTime);
        }
    }

    /**
     * 日志记录超过@{DEFAULT_REDIS_RESPONSE_TIME}ms的redis请求
     *
     * @param info，通常包含redis命令和key信息，超过300字节长度会被截取
     */
    public static void logClusterRedisResTime(String info, long startTime) {
        long resTime = System.currentTimeMillis() - startTime;
        if (resTime > DEFAULT_REDIS_RESPONSE_TIME) {
            LOG.warn("*redis {} " + RESPONSE_TIME_STR + " " +
                    DEFAULT_REDIS_RESPONSE_TIME + " ms , {} ms", cutString(info), resTime);
        }
    }

    /**
     * 日志记录超过${DEFAULT_HBASE_RESPONSE_TIME}ms的hbase请求
     *
     * @param table hbase表名称
     * @param info  hbase表操作的命令信息，比如：get row， scan startrow endrow等，，超过300字节长度会被截取
     */
    public static void logHBaseResTime(String table, String info, long startTime) {
        long resTime = System.currentTimeMillis() - startTime;
        if (resTime > DEFAULT_HBASE_RESPONSE_TIME) {
            LOG.warn("*hbase table {} {} " + RESPONSE_TIME_STR + " " + DEFAULT_HBASE_RESPONSE_TIME + " ms , {} ms", table, cutString(info), resTime);
        }
    }

    private static String cutString(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }

        if (source.length() > LOG_INFO_MAX_LENGTH) {
            source = source.substring(0, LOG_INFO_MAX_LENGTH) + OVER_LENGTH_SIGN + "info length:" + source.length();
        }

        return source;
    }

}
