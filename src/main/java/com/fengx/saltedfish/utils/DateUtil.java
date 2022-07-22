package com.fengx.saltedfish.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * 日期工具类
 * 使用： LocalDateTime
 */
public class DateUtil {

    public static final String DEF_FMT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DEF_FMT_DATE = "yyyy-MM-dd";
    private static final ThreadLocal<Long> TIMER = new ThreadLocal<>();

    /**
     * 日期转英文格式
     *
     * @param localDate LocalDate
     * @return 15 Mar 2020
     */
    public static String date2EnglishFormat(LocalDate localDate) {
        DateTimeFormatter ymdEn = DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH);
        return localDate.format(ymdEn);
    }
    /**
     * 时间转换成中文意思来表示
     *
     * @param localDateTime LocalDateTime
     * @return String
     */
    public static String dateTime2ChinaDesc(LocalDateTime localDateTime) {
        LocalDateTime now = LocalDateTime.now();
        // 时间间隔
        long millis = Duration.between(localDateTime, now).toMillis() / 1000;
        long days = millis / (60 * 60 * 24);
        long hours = (millis % (60 * 60 * 24)) / (60 * 60);
        long minutes = (millis % (60 * 60)) / (60);
        long seconds = (millis % (60));
        if (days > 0) {
            long years = days / 365;
            if (years > 0) {
                return years + "年前";
            }
            // 半年
            if ((days / 365 * 2) > 0) {
                return "半年前";
            }
            // 月
            long month = days / 30;
            if (month > 0) {
                return month + "个月前";
            }
            if (days >= 15) {
                return "半月前";
            }
            // 天
            return days + "天前";
        }
        if (hours > 0) {
            if (hours > 12) {
                return "半天前";
            } else {
                return hours + "小时前";
            }
        }
        if (minutes > 0) {
            return minutes + "分钟前";
        }
        if (seconds > 30) {
            return "半分钟前";
        }
        if (seconds >= 1) {
            return millis + "秒前";
        }
        return "刚刚";
    }

    //================================== 计时 单位统一毫秒 =====================================

    /**
     * 开始计时
     */
    public static void startTimer() {
        TIMER.set(nowLog());
    }

    /**
     * 中途获取计时
     */
    public static long currentTimer() {
        if (TIMER.get() != null) {
            return nowLog() - TIMER.get();
        }
        return 0;
    }

    /**
     * 结束计时
     */
    public static long endTimer() {
        if (TIMER.get() != null) {
            long time = nowLog() - TIMER.get();
            TIMER.remove();
            return time;
        }
        return 0;
    }

    //================================== 获取时间 =========================================

    public static LocalDateTime string2LocalDateTime(String dateTime) {
        return format(dateTime, DEF_FMT_DATE_TIME);
    }

    /**
     * 获取现在时间, 时间戳
     */
    public static long nowLog() {
        return System.currentTimeMillis();
    }

    /**
     * 获取短格式yyyy-MM-dd的String时间
     * @return String
     */
    public static String nowShortStr() {
        return format(LocalDateTime.now(), DEF_FMT_DATE);
    }

    /**
     * 获取短时间String
     * yyyy-MM-dd
     * @param dateTime 时间
     * @return String
     */
    public static String shortStr(LocalDateTime dateTime) {
        return format(dateTime, DEF_FMT_DATE);
    }

    /**
     * 短时间获取LocalDateTime
     * @param strDate String yyyy-MM-dd
     * @return LocalDateTime
     */
    public static LocalDateTime shortDate(String strDate) {
        return format(strDate, DEF_FMT_DATE);
    }

    //================================== 格式化 =========================================

    /**
     * 格式转换
     * @param dateTime 时间
     * @param pattern 格式
     * @return String
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dtf.format(dateTime);
    }

    /**
     * 格式转换
     * @param strDate 时间
     * @param pattern 格式
     * @return String
     */
    public static LocalDateTime format(String strDate, String pattern) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(pattern).parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1).parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .toFormatter();
        return LocalDateTime.parse(strDate, formatter);
    }
}