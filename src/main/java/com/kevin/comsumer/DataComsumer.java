package com.kevin.comsumer;

import com.kevin.producer.ProducerContent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataComsumer {

    public void comsume(String url,String indexName){

        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i=0;i<4;i++){
            executor.submit(new ComsumerContentBatch(url,indexName));
        }
        executor.shutdown();


    }

}
