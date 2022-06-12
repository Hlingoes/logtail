package com.eit.hoppy.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;

/**
 * description: 日期工具类
 *
 * @author Hlingoes
 * @date 2022/6/11 20:55
 * @citation 原文链接：https://blog.csdn.net/qq_40992690/article/details/105562602
 */
public class DateTimeHelper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取当前日期字符串
     *
     * @return
     */
    public static String nowDateStr() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 获取当前日期时间字符串
     *
     * @return
     */
    public static String nowDateTimeStr() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    /**
     * Date 转 LocalDate
     *
     * @return
     */
    public static LocalDate localDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date
     * @return
     */
    public static LocalDateTime localDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Date 转 日期字符串
     *
     * @param date
     * @return
     */
    public static String dateStr(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DATE_FORMATTER);
    }

    /**
     * Date 转 日期时间字符串
     *
     * @param date
     * @return
     */
    public static String dateTimeStr(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DATE_TIME_FORMATTER);
    }


    /**
     * LocalDate 转 Date
     *
     * @param localDate
     * @return
     */
    public static Date fromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDateTime 转 Date
     *
     * @param localDateTime
     * @return
     */
    public static Date fromLocalDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate 转 日期字符串
     *
     * @param localDate
     * @return
     */
    public static String dateStr(LocalDate localDate) {
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * LocalDateTime 转 日期时间字符串
     *
     * @param localDateTime
     * @return
     */
    public static String dateTimeStr(LocalDateTime localDateTime) {
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 日期时间字符串 转 LocalDateTime
     *
     * @param dateTimeStr
     * @return
     */
    public static LocalDateTime localDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    /**
     * 日期字符串 转 LocalDate
     *
     * @param dateStr
     * @return
     */
    public static LocalDate localDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 日期字符串 转 Date
     *
     * @param dateStr
     * @return
     */
    public static Date fromDateStr(String dateStr) {
        return Date.from(LocalDate.parse(dateStr, DATE_FORMATTER).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 日期时间字符串 转 Date
     *
     * @param dateStr
     * @return
     */
    public static Date fromDateTimeStr(String dateStr) {
        return Date.from(LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER).atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取上个月的第一天
     *
     * @return
     */
    public static LocalDate getLastMonthFirstDay() {
        return LocalDate.now().with((temporal) -> temporal.with(DAY_OF_MONTH, 1).plus(-1, MONTHS));
    }

    /**
     * 获取上个月的最后一天
     *
     * @return
     */
    public static LocalDate getLastMonthLastDay() {
        return LocalDate.now().plus(-1, MONTHS)
                .with(temporal -> temporal.with(DAY_OF_MONTH, temporal.range(DAY_OF_MONTH).getMaximum()));
    }

    /**
     * 获取当月的第一天
     *
     * @return
     */
    public static LocalDate getMonthFirstDay() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取当月最后一天
     *
     * @return
     */
    public static LocalDate getMonthLastDay() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取下个月的第一天
     *
     * @return
     */
    public static LocalDate getNextMonthFirstDay() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextMonth());
    }

    /**
     * 获取下个月的最后一天
     *
     * @return
     */
    public static LocalDate getNextMonthLastDay() {
        return LocalDate.now()
                .plus(1, MONTHS)
                .with(temporal -> temporal.with(DAY_OF_MONTH, temporal.range(DAY_OF_MONTH).getMaximum()));
    }


    /**
     * 获取去年第一天
     *
     * @return
     */
    public static LocalDate getLastYearFirstDay() {
        return LocalDate.now()
                .with((temporal) -> temporal.with(DAY_OF_YEAR, 1).plus(-1, YEARS));
    }

    /**
     * 获取去年最后一天
     *
     * @return
     */
    public static LocalDate getLastYearLastDay() {
        return LocalDate.now()
                .plus(-1, YEARS)
                .with(temporal -> temporal.with(DAY_OF_YEAR, temporal.range(DAY_OF_YEAR).getMaximum()));
    }

    /**
     * 获取今年第一天
     *
     * @return
     */
    public static LocalDate getYearFirstDay() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取今年最后一天
     *
     * @return
     */
    public static LocalDate getYearLastDay() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 获取明年第一天
     *
     * @return
     */
    public static LocalDate getNextYearFirstDay() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfNextYear());
    }

    /**
     * 获取明年最后一天
     *
     * @return
     */
    public static LocalDate getNextYearLastDay() {
        return LocalDate.now()
                .plus(1, YEARS)
                .with(temporal -> temporal.with(DAY_OF_YEAR, temporal.range(DAY_OF_YEAR).getMaximum()));
    }

    /**
     * 获取上个星期第一天
     *
     * @return
     */
    public static LocalDate getLastWeekFirstDay() {
        return LocalDate.now()
                .plus(-1, WEEKS)
                .with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMinimum()));
    }

    /**
     * 获取上个星期最后一天
     *
     * @return
     */
    public static LocalDate getLastWeekLastDay() {
        return LocalDate.now()
                .plus(-1, WEEKS)
                .with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMaximum()));
    }

    /**
     * 获取当前星期第一天
     *
     * @return
     */
    public static LocalDate getWeekFirstDay() {
        return LocalDate.now().with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMinimum()));
    }

    /**
     * 获取当前星期最后一天
     *
     * @return
     */
    public static LocalDate getWeekLastDay() {
        return LocalDate.now().with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMaximum()));
    }

    /**
     * 获取下个星期第一天
     *
     * @return
     */
    public static LocalDate getNextWeekFirstDay() {
        return LocalDate.now()
                .plus(1, WEEKS)
                .with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMinimum()));
    }

    /**
     * 获取下个星期最后一天
     *
     * @return LocalDate
     */
    public static LocalDate getNextWeekLastDay() {
        return LocalDate.now()
                .plus(1, WEEKS)
                .with(temporal -> temporal.with(DAY_OF_WEEK, temporal.range(DAY_OF_WEEK).getMaximum()));
    }

    /**
     * 获取某年某月的第一天
     *
     * @param year
     * @param month
     * @return
     */
    public static LocalDate getSomeYearMonthFirstDay(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * 获取某年某月的最后一天
     *
     * @param year
     * @param month
     * @return
     */
    public static LocalDate getSomeYearMonthLastDay(int year, int month) {
        return LocalDate.of(year, month, 1)
                .with(temporal -> temporal.with(DAY_OF_MONTH, temporal.range(DAY_OF_MONTH).getMaximum()));
    }

}
