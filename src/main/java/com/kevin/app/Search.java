package com.kevin.app;

import com.alibaba.fastjson.JSONObject;
import com.kevin.service.impl.SearchServiceImpl;

import java.util.List;

public class Search {

    public static void main(String[] args){
        String filename = args[0];
        String type = args[1];
        String num = args[2];
        SearchServiceImpl searchServiceImpl = new SearchServiceImpl();
        String result = searchServiceImpl.searchByFile(filename,type, null == num? 20 : Integer.parseInt(num));
        JSONObject returnjson = JSONObject.parseObject(result);
        if("1".equals(type)){
            String resultpath = returnjson.get("filepath")+"";
            System.out.println(resultpath);
        }else{
            List<String> resultpath = (List<String>)returnjson.get("filepath");
            String str ="";
            for (String ss :resultpath) {
                str += ss + ",";
            }
            System.out.println(str.substring(0,str.length() - 1));
        }

    }
}
