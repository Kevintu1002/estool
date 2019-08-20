package com.kevin.app;

import com.kevin.verification.Verification;
import io.netty.util.NettyRuntime;

public class VerifyData {

    public static void setConfig(){
        int processors = 32;
        NettyRuntime.setAvailableProcessors(processors);
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args){
        setConfig();

        String input = "";
        Verification verification = new Verification();
        verification.evaluate(input);

    }
}
