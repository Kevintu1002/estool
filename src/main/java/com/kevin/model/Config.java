package com.kevin.model;

import com.kevin.jna.ComLibrary;
import com.kevin.utils.FileUtil;
import com.sun.jna.Pointer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static Pointer queryPointer;

    public static String url;

    public static String index;

    public static void loadConfig(String configPath){

        List<String> configs = FileUtil.readFileContentToListByLine(configPath,"UTF-8");

        Map<String,String> configMap = new HashMap<>();
        for (String config: configs) {
            String[] keyAndValue = config.split("=");
            configMap.put(keyAndValue[0],keyAndValue[1]);
        }

        url = configMap.get("config.es.ip")+":"+configMap.get("config.es.port");
        index = configMap.get("config.es.index");

        String indexPtah = configMap.get("config.indexPath");
        String dataPath = configMap.get("config.dataPath");

        System.out.println(indexPtah);

//        queryPointer = ComLibrary.CQuerylibrary.DatabaseQueryManager_new(indexPtah,dataPath);



    }

}
