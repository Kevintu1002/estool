package com.kevin.task;

import com.alibaba.fastjson.JSON;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.cons.PatentConstant;
import com.kevin.service.util.PatentSearchUtil;
import com.kevin.utils.FileUtil;
import com.kevin.utils.JsonFileUtil;
import com.kevin.utils.StringUtil;
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
    private boolean googleflag;

    @Value("${es.jdbc.url}")
    private String esjdbcurl = PatentConstant.esjdbcurl;
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = PatentConstant.cn_es_jdbcurl;
    @Value("${csv.out.dir.path}")
    private String csvoutdirpath = PatentConstant.csvoutdirpath;
    private static final String jsonfilepath = PatentConstant.jsonfilepath;

    public FindSimilarDoc2(ESConnection esConnection, ESConnection es_cn_Connection, String doctype,
                           String sequence, String docid, Integer num,String date, boolean googleflag){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.es_cn_Connection = es_cn_Connection;
        this.docid = docid;
        this.num = num;
        this.date = date;
        this.doctype = doctype;
        this.googleflag = googleflag;
    }

    @Override
    public Map call() throws Exception {
        //写入输出csv
        int  n = 1;

        if(null == esConnection || esConnection.isClosed()){
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
        }
        if((null == es_cn_Connection || es_cn_Connection.isClosed()) && PatentConstant.doctype_cn.equals(doctype) && ! googleflag){
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            es_cn_Connection = (ESConnection) DriverManager.getConnection(cn_es_jdbcurl);
        }
        //输出tsv文件
        System.out.println("==================== get origin file content ===================");
        Map<String,String> contents = new HashMap<>();

        if(PatentConstant.doctype_cn.equals(doctype)){
            if(googleflag){
                //调用翻译工具获得内容
                try{
                    String jsonstr = JsonFileUtil.ReadJsonFileToJsonString(jsonfilepath);
                    Map contentdetail = (Map) JSON.parse(jsonstr);
                    contents = (Map<String, String>) contentdetail.get(docid);
                    if(null == contents || StringUtil.empty(contents.get(PatentConstant.claims))
                            || StringUtil.empty(contents.get(PatentConstant.abs))){
                        contents = PatentSearchUtil.getContents2(es_cn_Connection,docid);
                    }
                }catch (Exception e){
                    System.out.println(e.getLocalizedMessage());
                    contents = PatentSearchUtil.getContents2(es_cn_Connection,docid);
                }
            }else{
                contents = PatentSearchUtil.getContents2(es_cn_Connection,docid);
            }
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
            titlebuilder.append(contents.get(PatentConstant.title) + "\t");
            titlebuilder.append(n + "\t");
            titlebuilder.append(resdocid.get(PatentConstant.finaldocid) + "\t");
            titlebuilder.append(resdocid.get(PatentConstant.title) + "\n");

            claimbuilder.append(sequence + "\t");
            claimbuilder.append(docid + "\t");
            claimbuilder.append(contents.get(PatentConstant.claims) + "\t");
            claimbuilder.append(n + "\t");
            claimbuilder.append(resdocid.get(PatentConstant.finaldocid) + "\t");
            claimbuilder.append(resdocid.get(PatentConstant.claims) + "\n");

            absbuilder.append(sequence + "\t");
            absbuilder.append(docid + "\t");
            absbuilder.append(contents.get(PatentConstant.abs) + "\t");
            absbuilder.append(n + "\t");
            absbuilder.append(resdocid.get(PatentConstant.finaldocid) + "\t");
            absbuilder.append(resdocid.get(PatentConstant.abs) + "\n");
            n ++;
        }

        String titlepath = csvoutdirpath + PatentConstant.title +".tsv";
        String claimpath = csvoutdirpath + PatentConstant.claims +".tsv";
        String abspath = csvoutdirpath + PatentConstant.abs +".tsv";

        FileUtil.writeContentAppend(titlepath,titlebuilder.toString());
        FileUtil.writeContentAppend(claimpath,claimbuilder.toString());
        FileUtil.writeContentAppend(abspath,absbuilder.toString());

        return null;
    }

}
