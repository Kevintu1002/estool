package com.kevin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.csvreader.CsvWriter;
import com.kevin.model.DocContent;
import com.kevin.service.SearchService;
import com.kevin.utils.CSVUtil;
import com.kevin.utils.ExcelUtil;
import com.kevin.utils.StringUtil;
import com.kevin.utils.XmlParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
    Log log = LogFactory.getLog(SearchServiceImpl.class);

    @Value("${es.jdbc.url}")
    private String esjdbcurl;

    @Value("${csv.out.dirpath}")
    private String csvoutdirpath;


    @Override
    public String search(String docId,int num) {

        ESConnection esConnection=null;
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
            List<String> contents = getContents(esConnection,docId);
            List<String> searchRes = getCompareDocIds(esConnection,contents,num);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docIds",searchRes);
            return jsonObject.toJSONString();
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

    @Override
    public String searchByFile(MultipartFile file,Integer num) {
        JSONObject returnjson = new JSONObject();
        InputStream ins = null;
        Reader in = null;
        List<String> docIds = new ArrayList<>();
        try{
            String filename = file.getOriginalFilename();
            filename = filename.substring(filename.lastIndexOf(".")).toUpperCase();
            if(filename.contains("CSV")){
                ins = file.getInputStream();
                in = new BufferedReader(new InputStreamReader(ins));
                docIds = CSVUtil.readCsvFile(in);
            }else if(filename.contains("XLSX")){
                XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
                docIds = ExcelUtil.readExcel(workbook,0,0);
            }else if(filename.contains("XLS")){
                HSSFWorkbook workbook = new HSSFWorkbook(file.getInputStream());
                docIds = ExcelUtil.readExcel(workbook,0,0);
            }else{
                returnjson.put("code","0000");
                returnjson.put("flag",false);
                returnjson.put("message","暂不支持该种文件类型");
                return returnjson.toJSONString();
            }
            if(null == num) num = 10;
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            ESConnection esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
            CsvWriter csvWriter = new CsvWriter("findResDocids.csv");
            for(String docid : docIds){
                List<String> contents = getContents(esConnection,docid);
                List<String> searchRes = getCompareDocIds(esConnection,contents,num);
                //写入输出csv
                for(String resdocid : searchRes){
                    String[] docids = {docid,resdocid};
                    csvWriter.writeRecord(docids);
                }
            }
            csvWriter.close();
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            try{
                ins.close();
                in.close();
            }catch (Exception e){

            }

        }
        return null;
    }

    private List<Map<String,String>> getCompareDocIds2(ESConnection esConnection,List<String> contents,int num){

        List<String> docIds = new ArrayList<>();
        Map<String,Float> scores = new HashMap<>();
        for (String content:contents){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("select docid,appid,_score,abs,claims from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                        .append(content).append(") or claims:(").append(content).append(") or description:(")
                        .append(content).append(") ' limit "+num);

                ResultSet rs = st.executeQuery(sql.toString());
                while (rs.next()){

                    String docId = rs.getString(1);
                    if (StringUtil.empty(docId)){
                        docId = "";
                    }
                    String appId = rs.getString(2);
                    float score = rs.getFloat(3);
                    String abs = rs.getString(4);
                    String claims = rs.getString(5);
                    String key = appId+"_"+docId;
                    if (scores.containsKey(key)){
                        float sco = scores.get(key);
                        scores.put(key,sco+score);
                    }else {
                        scores.put(key,score);
                    }
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
        }

        List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(scores.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1,
                               Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo( o1.getValue());
            }
        });
        int length = list.size() > num?num:list.size();
        for (int i=0;i<length;i++){
            String key = list.get(i).getKey();
            String[] ids = key.split("_");

            for (int j=0;j<ids.length;j++){
                String id = ids[j];
                if (!StringUtil.empty(id)){
                    docIds.add(id);
                }
            }
        }
        return null;
    }


    private List<String> getCompareDocIds(ESConnection esConnection,List<String> contents,int num){

        List<String> docIds = new ArrayList<>();
        Map<String,Float> scores = new HashMap<>();
        for (String content:contents){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("select docid,appid,_score from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                        .append(content).append(") or claims:(").append(content).append(") or description:(")
                        .append(content).append(") ' limit "+num);

                ResultSet rs = st.executeQuery(sql.toString());
                while (rs.next()){

                    String docId = rs.getString(1);
                    if (StringUtil.empty(docId)){
                        docId = "";
                    }
                    String appId = rs.getString(2);
                    float score = rs.getFloat(3);
                    String key = appId+"_"+docId;
                    if (scores.containsKey(key)){
                        float sco = scores.get(key);
                        scores.put(key,sco+score);
                    }else {
                        scores.put(key,score);
                    }
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
        }

        List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(scores.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1,
                               Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo( o1.getValue());
            }
        });
        int length = list.size() > num?num:list.size();
        for (int i=0;i<length;i++){
            String key = list.get(i).getKey();
            String[] ids = key.split("_");

            for (int j=0;j<ids.length;j++){
                String id = ids[j];
                if (!StringUtil.empty(id)){
                    docIds.add(id);
                }
            }
        }
        return docIds;
    }


    private List<String> getContents(ESConnection esConnection, String docId){

        //TODO get document content by docid

        Statement st=null;
        List<String> contents = new ArrayList<>();
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());

            while (res.next()){
                contents.add(StringUtil.remove(res.getString(1)));
                contents.add(StringUtil.remove(res.getString(2)));
                contents.add(StringUtil.remove(res.getString(3)));
//                contents.add(res.getString(3));
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