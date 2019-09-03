package com.kevin.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        content = content.replaceAll("^", "");
        content = content.replaceAll(";", "");
        content = content.replaceAll("==", "");
        content = content.replaceAll("--", "");
        content = content.replaceAll("%", "");
        content = content.replaceAll("[\\{\\}]", "");

        return content;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

}
