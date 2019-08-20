package com.kevin.app;

import com.kevin.comsumer.DataComsumer;
import com.kevin.producer.DataProducer;
import io.netty.util.NettyRuntime;

public class InsertData {

    public static void setConfig(){
        int processors = 32;
        NettyRuntime.setAvailableProcessors(processors);
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args){
        setConfig();
        String inputDir = "J:\\kevin\\patentdata";
        String dataType = "file";
        String url = "202.112.195.82:9300";
        String indexName = "patent819";

        DataProducer dataProducer = new DataProducer(null,inputDir,dataType);
        dataProducer.produce();
        DataComsumer dataComsumer = new DataComsumer();
        dataComsumer.comsume(url,indexName);
    }

}
