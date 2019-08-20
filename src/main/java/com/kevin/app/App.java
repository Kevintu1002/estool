package com.kevin.app;

import com.kevin.comsumer.DataComsumer;
import com.kevin.producer.DataProducer;
import io.netty.util.NettyRuntime;

public class App {

    public static void setConfig(){
        int processors = 4;
        NettyRuntime.setAvailableProcessors(processors);
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args){


        String index = args[0];
        String inputDir = args[1];
        String dataType = args[2];
        String url = args[3];
        String indexName = args[4];

        DataProducer dataProducer = new DataProducer(index,inputDir,dataType);
        dataProducer.produce();
        DataComsumer dataComsumer = new DataComsumer();
        dataComsumer.comsume(url,indexName);


    }
}
