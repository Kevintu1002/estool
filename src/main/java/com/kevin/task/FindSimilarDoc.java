package com.kevin.task;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.service.util.PatentSearchUtil;
import org.springframework.beans.factory.annotation.Value;

import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.Callable;

public class FindSimilarDoc implements Callable<List>{

    private ESConnection esConnection;
    private ESConnection es_cn_Connection;
    private String sequence;
    private String docid;
    private Integer num;
    private String doctype;

    private static final String finaldocid = "docid";
    @Value("${es.jdbc.url}")
    private String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";

    public  FindSimilarDoc (ESConnection esConnection,ESConnection es_cn_Connection, String sequence, String docid, Integer num,String doctype){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.es_cn_Connection = es_cn_Connection;
        this.docid = docid;
        this.num = num;
        this.doctype = doctype;
    }

    /**
     * 不用苏大服务进行精选
     * @return
     * @throws Exception
     */
    @Override
    public List call() throws Exception {
        //写入输出csv
        int  n = 1;

        if(null == esConnection || esConnection.isClosed()){
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
        }
        if((null == es_cn_Connection || es_cn_Connection.isClosed()) && "CN".equals(doctype)){
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            es_cn_Connection = (ESConnection) DriverManager.getConnection(cn_es_jdbcurl);
        }

        List<String[]> out = new ArrayList<>();
        Map<String,String> contents ;
        if("CN".equals(doctype)){
            contents = PatentSearchUtil.getContents2(es_cn_Connection,docid);
        }else{
            contents = PatentSearchUtil.getContents2(esConnection,docid);
        }

        List<Map<String,String>> searchRes = PatentSearchUtil.getCompareDocIds2(esConnection,contents,num);

        for(Map<String,String> resdocid : searchRes){
            String[] docids = {sequence,docid,n+"",resdocid.get(finaldocid)};
            out.add(docids);
            n ++;
        }
        return out;

    }
}
