package com.kevin.common;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Data {

    public static Map<String,Map<Integer,String>> data = new HashMap<String, Map<Integer,String>>();
    public static Queue<List<String>> dataQueue = new LinkedBlockingQueue<List<String>>();

    public static synchronized void setData(String key,String content,int index){
        if (data.containsKey(key)){

            data.get(key).put(index,content);
            if (data.get(key).size() == 4){
                List<String> resItem = new ArrayList<String>();
                resItem.add(key);
                resItem.addAll(transMapToList(data.get(key)));
                dataQueue.offer(resItem);
                data.remove(key);
            }
        }else {
            Map<Integer,String> contents = new HashMap<Integer, String>();
            contents.put(index,content);
            data.put(key,contents);
        }
    }

    public static synchronized List<String> getData(){
        return dataQueue.poll();
    }

    public static synchronized int getDataSize(){
        return data.size();
    }

    public static synchronized int getDataQueueSize(){
        return dataQueue.size();
    }

    private static List<String> transMapToList(Map<Integer,String> contents){

        String[] res = new String[4];
        for (Integer key : contents.keySet()){
            res[key]=contents.get(key);
        }

        return Arrays.asList(res);

    }
}
