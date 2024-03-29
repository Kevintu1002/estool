package com.kevin.task;

import com.alibaba.fastjson.JSON;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.cons.PatentConstant;
import com.kevin.service.util.PatentSearchUtil;
import com.kevin.utils.JsonFileUtil;
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
    private boolean googleflag;
    private String jsonstr;

    @Value("${es.jdbc.url}")
    private String esjdbcurl = PatentConstant.esjdbcurl;
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = PatentConstant.cn_es_jdbcurl;
    @Value("${cn.json.path}")
    private String jsonfilepath = PatentConstant.jsonfilepath;

    public  FindSimilarDoc (ESConnection esConnection,ESConnection es_cn_Connection, String sequence,
                            String docid, Integer num,String doctype, boolean googleflag,String jsonstr
                            ){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.es_cn_Connection = es_cn_Connection;
        this.docid = docid;
        this.num = num;
        this.doctype = doctype;
        this.googleflag = googleflag;
        this.jsonstr = jsonstr;
    }

    /**
     * 不用苏大服务进行精选
     * @return
     * @throws Exception
     */
    @Override
    public List call(){
        List<String[]> out = new ArrayList<>();
        try{
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
            Map<String,String> contents = new HashMap<>();
            if(PatentConstant.doctype_cn.equals(doctype)){
                //从中文es库中查找内容
                if(googleflag){
                    //调用翻译工具获得内容
                    try{
                        Map contentdetail = (Map) JSON.parse(jsonstr);
                        contents = (Map<String, String>) contentdetail.get(docid);
                        if(null == contents ){
                            System.out.println("=============== 获得的claims 、abs 为空 =========");
                            for(int m = 1;m <= num;m ++){
                                String[] docids = {sequence,docid,m+"",""};
                                out.add(docids);
                            }
                            return out;
                        }
                    }catch (Exception e){
                        System.out.println(e.getLocalizedMessage());
                        for(int m = 1;m <= num;m ++){
                            String[] docids = {sequence,docid,m+"",""};
                            out.add(docids);
                        }
                        return out;
                    }

                }else{
                    System.out.println("=========== 调用google 翻译失败 =========");
                    for(int m = 1;m <= num;m ++){
                        String[] docids = {sequence,docid,m+"",""};
                        out.add(docids);
                    }
                    return out;
                }
            }else{
                contents = PatentSearchUtil.getContents2(esConnection,docid);
            }

            List<Map<String,String>> searchRes = PatentSearchUtil.getCompareDocIds2(esConnection,contents,num);

            for(Map<String,String> resdocid : searchRes){
                String[] docids = {sequence,docid,n+"",resdocid.get(PatentConstant.finaldocid)};
                out.add(docids);
                n ++;
            }
            return out;

        }catch (Exception e){
            System.out.println("============== 线程处理docid："+docid+"  出现问题 =========");
            System.out.println(e.getMessage());
            for(int m = 1;m <= num;m ++){
                String[] docids = {sequence,docid,m+"",""};
                out.add(docids);
            }
            return out;
        }

    }
}
