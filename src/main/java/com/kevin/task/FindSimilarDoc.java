package com.kevin.task;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.queue.SemilarDocQueue;
import com.kevin.thread.SearchDocThread;
import com.kevin.utils.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;

public class FindSimilarDoc implements Callable<List<String[]>>{

    Log log = LogFactory.getLog(SearchDocThread.class);

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

    @Value("${csv.result.dir.path}")
    private String csvresultdirpath="/tmp/";

    @Value("${es.jdbc.url}")
    private String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v8?cluster.name=patent";

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
    public List<String[]> call() throws Exception {

        List<String[]> out = new ArrayList<>();
        //写入输出csv
        int  n = 1;
        if(null != outtype){
            Map<String,String> contents = getContents2(es_cn_Connection,docid);
            List<Map<String,String>> searchRes = getCompareDocIds2(esConnection,contents,num);

            for(Map<String,String> resdocid : searchRes){
                String[] docids = {sequence,docid,contents.get(outtype),n+"",resdocid.get(finaldocid),resdocid.get(outtype)};
                out.add(docids);
                n ++;
            }
        }else{
            Map<String,String> contents = getContents2(esConnection,docid);
            List<Map<String,String>> searchRes = getCompareDocIds2(esConnection,contents,num);

            for(Map<String,String> resdocid : searchRes){
                String[] docids = {sequence,docid,n+"",resdocid.get(finaldocid)};
                out.add(docids);
                n ++;
            }
        }

        try {
            SemilarDocQueue.push(out);
        }catch (Exception e){
            log.error("docid  =  "+ docid +"，push into queue error！");
        }
        return out;
    }

    private Map<String,String> getContents2(ESConnection esConnection, String docId){
        Map<String,String> detail = new HashMap<>(3);
        Statement st=null;
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());
            while (res.next()){
                detail.put(title, StringUtil.remove(res.getString(1)));
                detail.put(abs,StringUtil.remove(res.getString(2)));
                detail.put(claims,StringUtil.remove(res.getString(3)));
//                contents.add(res.getString(3));
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

    private List<Map<String,String>> getCompareDocIds2(ESConnection esConnection,Map<String,String> contentdetail,int num){

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
                sql.append("select docid,appid,_score,abs,claims,title from en WHERE _search = 'title:(").append(content).append(") or abs:(")
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
            String[] ids = key.split("_");

            for (int j=0;j<ids.length;j++){
                String id = ids[j];
                if (!StringUtil.empty(id)){
                    Map<String,String> detail = new HashMap<>(3);
                    detail.put(finaldocid,id);
                    detail.put(claims,key_value.get(claims)+"");
                    detail.put(abs,key_value.get(abs)+"");
                    docIds.add(detail);
                }
            }
        }
        return docIds;
    }
}
