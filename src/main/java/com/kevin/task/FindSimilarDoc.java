package com.kevin.task;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.utils.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;

public class FindSimilarDoc implements Callable<List>{

    Log log = LogFactory.getLog(FindSimilarDoc.class);

    private ESConnection esConnection;
    private ESConnection es_cn_Connection;
    private String sequence;
    private String docid;
    private Integer num;
    private String outtype;

    private static final String finaldocid = "docid";
    private static final String title = "title";
    private static final String abs = "abs";
    private static final String claims = "claims";
    private static final String pdate = "pdate";

    @Value("${csv.result.dir.path}")
    private String csvresultdirpath="/tmp/";
    @Value("${es.jdbc.url}")
    private String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v13?cluster.name=patent";


    public  FindSimilarDoc (ESConnection esConnection, String sequence, String docid, Integer num,String outtype){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.docid = docid;
        this.num = num;
        this.outtype = outtype;
    }
    public  FindSimilarDoc (ESConnection esConnection,ESConnection es_cn_Connection, String sequence, String docid, Integer num,String outtype){
        this.sequence = sequence;
        this.esConnection = esConnection;
        this.es_cn_Connection = es_cn_Connection;
        this.docid = docid;
        this.num = num;
        this.outtype = outtype;
    }

    @Override
    public List call() throws Exception {
        //写入输出csv
        int  n = 1;

        List<String[]> out = new ArrayList<>();
        Map<String,String> contents = getContents2(esConnection,docid);
        List<Map<String,String>> searchRes = getCompareDocIds2(esConnection,contents,num);

        for(Map<String,String> resdocid : searchRes){
            String[] docids = {sequence,docid,n+"",resdocid.get(finaldocid)};
            out.add(docids);
            n ++;
        }
        return out;

    }

    private Map<String,String> getContents2(ESConnection esConnection, String docId){
        Map<String,String> detail = new HashMap<>(3);
        Statement st=null;

        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims,pdate from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());
            while (res.next()){
                detail.put(title, res.getString(1));
                detail.put(abs,res.getString(2));
                detail.put(claims,res.getString(3));
                detail.put(pdate,res.getString(4));
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }finally {
            if (st != null){
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("=========== get origin patent content by docid ,docid is："+ docId + ",detail is :" + detail.toString());
        return detail;

    }

    private List<Map<String,String>> getCompareDocIds2(ESConnection esConnection,Map<String,String> contentdetail,int num) throws Exception{
        List<Map<String,String>> docIds = new ArrayList<>();
        Map<String,Map<String,Object>> scores = new HashMap<>();
        List<String> contents2 = new ArrayList<>();
        contents2.add(contentdetail.get(title));
        contents2.add(contentdetail.get(abs));
        contents2.add(contentdetail.get(claims));

        log.info("========== get compare docid ===============");
        for (String content:contents2){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                if(StringUtil.empty(contentdetail.get(pdate))){
                    sql.append("select docid,appid,_score,abs,claims,title from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                            .append(content).append(") or claims:(").append(content).append(") or description:(")
                            .append(content).append(") 'limit "+num);
                }else{
                    sql.append("select docid,appid,_score,abs,claims,title from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                            .append(content).append(") or claims:(").append(content).append(") or description:(")
                            .append(content).append(") ' and pdate <= ")
                            .append(contentdetail.get(pdate)).append(" limit "+num);
                }

                ResultSet rs = st.executeQuery(sql.toString());
                while (rs.next()){

                    String docId = rs.getString(1);
                    if (StringUtil.empty(docId)){
                        docId = "";
                    }
                    String appId = rs.getString(2);
                    float score = rs.getFloat(3);
                    String abs2 = rs.getString(4);
                    String claims2 = rs.getString(5);
                    String title2 = rs.getString(6);
                    String key = appId+"_"+docId;
                    if (scores.containsKey(key)){
                        float sco = (Float) scores.get(key).get("score");
                        scores.get(key).put("score",sco+score);
                    }else {
                        Map<String,Object> scoremap = new HashMap<>(4);
                        scoremap.put(title,title2);
                        scoremap.put(abs,abs2);
                        scoremap.put(claims,claims2);
                        scoremap.put("score",score);
                        scores.put(key,scoremap);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(),e);
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

        List<Map.Entry<String, Map<String,Object>>> list = new LinkedList<Map.Entry<String, Map<String,Object>>>(scores.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String, Map<String,Object>>>() {
            @Override
            public int compare(Map.Entry<String, Map<String,Object>> o1,
                               Map.Entry<String, Map<String,Object>> o2) {
                return ((Float)o2.getValue().get("score")).compareTo( (Float)o2.getValue().get("score"));
            }
        });
        int length = list.size() > num?num:list.size();
        for (int i=0;i<length;i++){
            String key = list.get(i).getKey();
            Map<String,Object> key_value = list.get(i).getValue();

            String id = key.split("_")[1];
            if (!StringUtil.empty(id)){
                Map<String,String> detail = new HashMap<>(3);
                detail.put(finaldocid,id);
                detail.put(claims,key_value.get(claims)+"");
                detail.put(abs,key_value.get(abs)+"");
                detail.put(title,key_value.get(title)+"");
                docIds.add(detail);
            }

        }
        return docIds;
    }
}
