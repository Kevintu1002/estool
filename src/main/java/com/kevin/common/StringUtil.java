package com.kevin.common;

public class StringUtil {

    public static String remove(String content){
        content = content.replaceAll("\\\\","");
        content = content.replaceAll(" ", "");
        content = content.replaceAll("\\s*", "");
        content = content.replaceAll("'", "''");
        return content;
    }
}
