package com.kevin.common;

public class App {

    public static void main(String[] args){
        String inputDir = "J:\\kevin\\mpp\\test";

        ESTool.init();

        DataProducer producer = new DataProducer();
        producer.producer(inputDir);

        int ThreadNum = 40;

        for (int i = 0;i<ThreadNum;i++){
            Thread comsumer = new Comsumer();
            comsumer.start();
        }

//        ESTool.close();

    }
}
