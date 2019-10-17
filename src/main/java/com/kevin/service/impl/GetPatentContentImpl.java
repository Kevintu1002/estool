package com.kevin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.csvreader.CsvWriter;
import com.kevin.cons.PatentConstant;
import com.kevin.service.SearchService;
import com.kevin.service.util.GoogleTranslate;
import com.kevin.task.FindSimilarDoc;
import com.kevin.task.FindSimilarDoc2;
import com.kevin.utils.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GetPatentContentImpl{
    Log log = LogFactory.getLog(GetPatentContentImpl.class);

    @Value("${es.jdbc.url}")
    private String esjdbcurl = PatentConstant.esjdbcurl;
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = PatentConstant.cn_es_jdbcurl;

    @Value("${csv.origin.dir.path}")
    private String csvorigindirpath = PatentConstant.csvorigindirpath;

    @Value("${csv.out.dir.path}")
    private String csvoutdirpath = PatentConstant.csvoutdirpath;

    @Value("${csv.result.dir.path}")
    private String csvresultdirpath = PatentConstant.csvresultdirpath;

    @Value("${result.dir.path}")
    private String resultdirpath = PatentConstant.resultdirpath;

    @Value("${datefilter.path}")
    private String datefilterpath = PatentConstant.datefilterpath;

    @Value("${cn.json.path}")
    private String jsonfilepath = PatentConstant.jsonfilepath;

    @Value("${threadpool.size}")
    private String threadpoolsize = "10";


    public static final String IP = "222.28.84.124";

    public static final int PORT = 22;

    public static final String USERNAME = "ky";

    public static final String PASSWORD = "12345";

    public static final String REMOTEURL = "/home/ky/suda_test/data/test";


    public String getContent(String absolutefilepath,String absoluteoutpath) {

        ESConnection esConnection=null;
        Set<String> docIds = new HashSet<>();
        CsvWriter csvWriter = new CsvWriter(absoluteoutpath);
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);

            log.info("====== The input file name is ："+absolutefilepath);
            long start = System.currentTimeMillis();
            docIds =FileUtil.readFileDocId(absolutefilepath,"utf-8");

            String[] title = new String[4];
            title[0] = "考题id";
            title[1] = "title";
            title[2] = "abs";
            title[3] = "claims";
            title[3] = "description";
            csvWriter.writeRecord(title);

            for(String docid : docIds){
                List<String> contents = getContents(esConnection,docid);
                String[] content = new String[4];
                content[0] = docid;
                content[1] = contents.get(0);
                content[2] = contents.get(1);
                content[3] = contents.get(2);
                content[4] = contents.get(3);
                csvWriter.writeRecord(content);
            }


        }catch (Exception e){

            e.printStackTrace();
        }finally {
            if (esConnection != null){
                try {
                    esConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private List<String> getContents(ESConnection esConnection, String docId){

        //TODO get document content by docid

        Statement st=null;
        List<String> contents = new ArrayList<>();
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims,description from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());

            while (res.next()){
                contents.add(StringUtil.remove2(res.getString(1)));
                contents.add(StringUtil.remove2(res.getString(2)));
                contents.add(StringUtil.remove2(res.getString(3)));
                contents.add(StringUtil.remove2(res.getString(4)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (st != null){
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return contents;

    }
}
