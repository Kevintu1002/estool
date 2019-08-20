package com.kevin.es;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EsTool {

    public final static String HOST = "202.112.195.82";
    // http请求的端口是9200，客户端是9300
    public final static int PORT = 9300;

    public static TransportClient client = null;

    public static TransportClient getConnection(){

        Settings settings = Settings.builder().put("cluster.name", "patent").build();
        // 创建client
        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return client;
    }


    public static void close(){
        if (client != null){
            try {
                client.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }



}
