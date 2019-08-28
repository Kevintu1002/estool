package com.kevin.utils;

public class StringUtil {

    public static String remove(String content){
        content = content.replaceAll("\\\\","");
        content = content.replaceAll(" ", "");
        content = content.replaceAll("\\s*", "");


        content = content.replaceAll("'", "");
        content = content.replaceAll("~", "");
        return content;
    }

    /**
     * 验证字符串是否为空
     *
     * @param param
     * @return
     */
    public static boolean empty(String param) {
        return param == null || "".equals(param) || param.trim().length() < 1 || param.equals("null");
    }

    public static String remove2(String content){
        content = content.replaceAll("'", "");
        content = content.replaceAll("\"", "");
//        content = content.replaceAll("\\(", "");
//        content = content.replaceAll("\\）", "");
        content = content.replaceAll("[\\\\(\\\\)]", "");
        content = content.replaceAll("[\\\\<\\\\>]", "");
        content = content.replaceAll("[\\[\\]]", "");
        content = content.replaceAll(":", "");
        content = content.replaceAll("/", "");
        content = content.replaceAll("-", "");
        content = content.replaceAll("\\+", "");
        content = content.replaceAll("=", "");

        content = content.replaceAll(";", "");
        content = content.replaceAll("==", "");
        content = content.replaceAll("--", "");
        content = content.replaceAll("%", "");
        content = content.replaceAll("[\\{\\}]", "");

        return content;
    }

}
