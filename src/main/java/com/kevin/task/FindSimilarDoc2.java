package com.kevin.task;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.service.util.PatentSearchUtil;
import com.kevin.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.DriverManager;
import java.util.*;
import java.util.concurrent.Callable;

public class FindSimilarDoc2 implements Callable<Map>{

    Log log = LogFactory.getLog(FindSimilarDoc2.class);

    private ESConnection esConnection;
    private ESConnection es_cn_Connection;
    private String sequence;
    private String docid;
    private Integer num;
    private String date;
    private String doctype;

    private static final String finaldocid = "docid";
    private static final String title = "title";
    private static final String abs = "abs";
    private static final String claims = "claims";
    private static final String pdate = "pdate";

    @Value("${es.jdbc.url}")
    private String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";
    @Value("${csv.out.dir.path}")
    private String csvoutdirpath = "/data/disk1/patent/Django/media/csvout/";

    public FindSimilarDoc2(ESConnection esConnection, ESConnection es_cn_Connection, String doctype,
                           String sequence, String docid, Integer num,String date){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.es_cn_Connection = es_cn_Connection;
        this.docid = docid;
        this.num = num;
        this.date = date;
        this.doctype = doctype;
    }

    @Override
    public Map call() throws Exception {
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
        //输出tsv文件
        System.out.println("==================== get origin file content ===================");
        Map<String,String> contents ;
        if("CN".equals(doctype)){
            contents = PatentSearchUtil.getContents2(es_cn_Connection,docid);
        }else{
             contents = PatentSearchUtil.getContents2(esConnection,docid);
        }
        System.out.println("==================== get search file content ===================");
        List<Map<String,String>> searchRes = PatentSearchUtil.getCompareDocIds2(esConnection,contents,num);

        StringBuilder titlebuilder = new StringBuilder("");
        StringBuilder claimbuilder = new StringBuilder("");
        StringBuilder absbuilder = new StringBuilder("");
        for(Map<String,String> resdocid : searchRes){
            titlebuilder.append(sequence + "\t");
            titlebuilder.append(docid + "\t");
            titlebuilder.append(contents.get(title) + "\t");
            titlebuilder.append(n + "\t");
            titlebuilder.append(resdocid.get(finaldocid) + "\t");
            titlebuilder.append(resdocid.get(title) + "\n");

            claimbuilder.append(sequence + "\t");
            claimbuilder.append(docid + "\t");
            claimbuilder.append(contents.get(claims) + "\t");
            claimbuilder.append(n + "\t");
            claimbuilder.append(resdocid.get(finaldocid) + "\t");
            claimbuilder.append(resdocid.get(claims) + "\n");

            absbuilder.append(sequence + "\t");
            absbuilder.append(docid + "\t");
            absbuilder.append(contents.get(abs) + "\t");
            absbuilder.append(n + "\t");
            absbuilder.append(resdocid.get(finaldocid) + "\t");
            absbuilder.append(resdocid.get(abs) + "\n");
            n ++;
        }

        String titlepath = csvoutdirpath + title +".tsv";
        String claimpath = csvoutdirpath + claims +".tsv";
        String abspath = csvoutdirpath + abs +".tsv";

        FileUtil.writeContentAppend(titlepath,titlebuilder.toString());
        FileUtil.writeContentAppend(claimpath,claimbuilder.toString());
        FileUtil.writeContentAppend(abspath,absbuilder.toString());

        return null;
    }

}
