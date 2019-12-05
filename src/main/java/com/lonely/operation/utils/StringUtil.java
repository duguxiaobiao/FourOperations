package com.lonely.operation.utils;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author ztkj-hzb
 * @Date 2019/9/19 11:03
 * @Description
 */
public class StringUtil {

    /**
     * 首字符大写
     *
     * @param str
     * @return
     */
    public static String upperCase(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }


    /**
     * 判断指定字符串 是否满足 list集合的格式  比如 clazz.student[L0].stuname
     *
     * @param replaceStr
     * @return
     */
    public static boolean regularMatchingListOrArrayFormat(String replaceStr) {
        return regularMatchingListFormat(replaceStr) || regularMatchingArrayFormat(replaceStr);
    }

    /**
     * 判断指定字符串 是否满足 list集合的格式  比如 clazz.student[L0].stuname
     *
     * @param replaceStr
     * @return
     */
    public static boolean regularMatchingListFormat(String replaceStr) {
        String regex = "[\\S\\s]*\\[L\\d?][\\S\\s]*";
        return replaceStr.matches(regex);
    }

    /**
     * 判断指定字符串 是否满足 list集合的格式  比如 clazz.student[L0].stuname
     *
     * @param replaceStr
     * @return
     */
    public static boolean regularMatchingArrayFormat(String replaceStr) {
        String regex = "[\\S\\s]*\\[A\\d?][\\S\\s]*";
        return replaceStr.matches(regex);
    }

    /**
     * 将集合待处理的前部分获取  例如 clazz.student[?0].stuname  -->  clazz.student
     *
     * @param fullStr
     * @param replaceStr
     * @return
     */
    public static String subStringListFormat(String fullStr, String replaceStr) {
        int indexOf = fullStr.indexOf(replaceStr);
        return fullStr.substring(0, indexOf);
    }

    /**
     * 去除左右 [ ]
     *
     * @param handlerStr
     * @return
     */
    public static String removeLeftAndRightMiddleBrackets(String handlerStr) {
        if (handlerStr.charAt(0) == '[') {
            handlerStr = handlerStr.substring(1);
        }
        if (handlerStr.charAt(handlerStr.length() - 1) == ']') {
            handlerStr = handlerStr.substring(0, handlerStr.length() - 1);
        }
        return handlerStr;
    }

    /**
     * 获取随机后缀
     *
     * @return
     */
    public static String getRandomAlphabetic() {
        return RandomStringUtils.randomAlphabetic(2);
    }
}
