package com.niewj.springboot.hbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created on 2017/11/14.
 */
public abstract class Caculate {
    private static Logger logger = LoggerFactory.getLogger(Caculate.class);

    /**
     * 根据当前时间获取前一段时间点
     *
     * @param cdate
     * @param period
     * @param calendar
     * @return
     */
    private static long getTime(Date cdate, Long period, int calendar) {
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(cdate);
        now.add(calendar, -period.intValue());
        return now.getTimeInMillis();
    }

    private static String getRowKeyWithTime(String id, Date cDate, int calendar, Long period) {
        if (cDate == null) {
            cDate = new Date();
        }
        StringBuffer sb = new StringBuffer(id);
        return sb.append("_").append(getTime(cDate, period, calendar)).toString();
    }

    public static String getRowKeyWithTime(String id, Long period) {
        return getRowKeyWithTime(id, null, Calendar.HOUR_OF_DAY, period);
    }

}
