package org.jeecg.modules.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 通用工具
 * @author: jeecg-boot
 */
@Slf4j
public class ToolUtils {

    /**
     * 从对象A copy到 对象B
     *
     * @param ob    A
     * @param clazz B.class
     * @return B
     */
    public static <T> T copy(Object ob, Class<T> clazz) {
        String oldOb = JSON.toJSONString(ob);
        return JSON.parseObject(oldOb, clazz);
    }

    /**
     * 从List<A> copy到List<B>
     *
     * @param list  List<B>
     * @param clazz B
     * @return List<B>
     */
    public static <T> List<T> copyList(List<?> list, Class<T> clazz) {
        if (CollectionUtil.isEmpty(list)) {
            return Lists.newArrayList();
        }
        String oldOb = JSON.toJSONString(list);
        return JSON.parseArray(oldOb, clazz);
    }

    /**
     * 字符串返回数组
     *
     * @param str
     * @param split
     */
    public static List<String> str2List(String str, String split) {
        List<String> list = Lists.newArrayList();
        for (String s : str.split(split)) {
            list.add(s);
        }
        return list;
    }

    /**
     * 差集：获取a不在b中的元素
     *
     * @param a
     * @param b
     */
    public static <T> List<T> diffSet(List<T> a, List<T> b) {
        if (CollUtil.isEmpty(a)) {
            a = Lists.newArrayList();
        }
        if (CollUtil.isEmpty(b)) {
            b = Lists.newArrayList();
        }
        List<T> finalB = b;
        return a.stream().filter(x -> !finalB.contains(x)).collect(Collectors.toList());
    }

    /**
     * 数组返回字符串
     *
     * @param rex
     * @param list
     */
    public static String list2Str(String rex, List<Object> list) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            if (Objects.isNull(list.get(i))) {
                continue;
            }
            if (i >= list.size() - 1) {
                result += (Convert.toStr(list.get(i)));
            } else {
                result += Convert.toStr(list.get(i)).concat(rex);
            }
        }
        return result;
    }

    /**
     * 计算两个日期每个月天数
     *
     * @param startDateStr yyyy-MM-dd
     * @param endDateStr   yyyy-MM-dd
     * @return
     */
    public static Map<String, Long> calcDays(String startDateStr, String endDateStr) {
        Date startDate = DateUtil.parse(startDateStr, "yyyy-MM-dd");
        Date endDate = DateUtil.parse(endDateStr, "yyyy-MM-dd");
        String startMonthStr = DateUtil.format(startDate, "yyyy-MM");
        String endMonthStr = DateUtil.format(endDate, "yyyy-MM");
        Date startMonth = DateUtil.parse(DateUtil.format(startDate, "yyyy-MM"), "yyyy-MM");
        Date endMonth = DateUtil.parse(DateUtil.format(endDate, "yyyy-MM"), "yyyy-MM");
        Map<String, Long> result = Maps.newHashMap();
        while (startMonth.compareTo(endMonth) <= 0) {
            String month = DateUtil.format(startMonth, "yyyy-MM");
            // 月份等于当前开始日期月份则计算开始日期到月底的天数
            if (month.equals(startMonthStr)) {
                Date monthEnd = DateUtil.endOfMonth(startMonth);
                Long days = DateUtil.betweenDay(startDate, monthEnd, Boolean.FALSE) + 1;
                result.put(month, days);
            }
            // 月份不等于起始日期月份和结束日期月份
            if (!month.equals(startMonthStr) && !month.equals(endMonthStr)) {
                Long days = DateUtil.betweenDay(startMonth, DateUtil.endOfMonth(startMonth), Boolean.FALSE) + 1;
                result.put(month, days);
            }
            // 月份等于当前结束日期月份则计算结束月份第一天到结束日期天数
            if (month.equals(endMonthStr)) {
                Date monthStart = DateUtil.beginOfMonth(startMonth);
                Long days = DateUtil.betweenDay(monthStart, endDate, Boolean.FALSE) + 1;
                result.put(month, days);
            }
            // 加一个月
            startMonth = DateUtil.offsetMonth(startMonth, 1);
        }
        return result;
    }

    /**
     * 计算表达式
     *
     * @param expression
     * @return
     */
    public static boolean evaluateBooleanExpression(String expression) {
        // 使用ScriptEngineManager来获取JavaScript的脚本引擎
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        try {
            // 执行表达式并获取结果
            Object result = engine.eval(expression);
            // 将结果转换为布尔值
            boolean boolResult = Boolean.parseBoolean(result.toString());
            return boolResult;
        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * hmacSha256加密签名
     *
     * @param data
     * @param secret
     * @return
     */
    @SneakyThrows
    public static String hmacSha256(String data, String secret) {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] bytes = sha256_HMAC.doFinal(data.getBytes());
        return byteArrayToHexString(bytes).toUpperCase();
    }

    /**
     * @param b
     * @return
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append('0');
            }
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }
}
