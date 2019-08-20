package com.kevin.verification;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.utils.FileUtil;
import com.kevin.utils.StringUtil;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Verification {

    public void evaluate(String inputfile){

        Map<String,Set<String>> res = getCompareResults(inputfile);

        ESConnection esConnection=null;
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection("jdbc:sql4es://202.112.195.82:9300/patent818?cluster.name=patent");

            int totalCompare = 0;
            int totalFound = 0;
            for (String curDocId:res.keySet()){
                List<String> contents = getContent(esConnection,curDocId);
                if (contents.size() == 0){
                    continue;
                }
                List<String> getSearchRes = getCompareDocIds(esConnection,contents);

                int validNum =0;
                int foundNum = 0;
                for (String compareDocId : res.get(curDocId)){
                    if (checkDocIsExisted(esConnection,compareDocId)){
                        validNum++;
                        if (getSearchRes.contains(compareDocId)){
                            foundNum++;
                        }
                    }

                }
                if (validNum != 0){
                    System.out.println("doc "+curDocId+" recall rate ="+foundNum*1.0/validNum);
                }else {
                    System.out.println("doc "+curDocId+" recall rate =0");
                }
                totalCompare = totalCompare + validNum;
                totalFound = totalFound + foundNum;
            }
            if (totalCompare != 0){
                System.out.println("total recall rate ="+totalFound*1.0/totalCompare);
            }else {
                System.out.println("total recall rate =0");
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
    }

    private boolean checkDocIsExisted(ESConnection esConnection,String docId){

        Statement st=null;
        List<String> contents = new ArrayList<>();
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from cn where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());
            return res.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
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

    private List<String> getCompareDocIds(ESConnection esConnection,List<String> contents){

        List<String> docIds = new ArrayList<>();
        Map<String,Float> scores = new HashMap<>();
        for (String content:contents){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("select docid,appid,_score from cn WHERE _search = 'title:(").append(content).append(") and abs:(")
                        .append(content).append(") and claims:(").append(content).append(") and description:(")
                        .append(content).append(") ' limit 2000");

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
        int length = list.size() > 2000?2000:list.size();
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


    private List<String> getContent(ESConnection esConnection,String docId){

        Statement st=null;
        List<String> contents = new ArrayList<>();
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from cn where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());

            while (res.next()){
                contents.add(StringUtil.remove(res.getString(1)));
                contents.add(StringUtil.remove(res.getString(2)));
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

    private Map<String,Set<String>> getCompareResults(String inputfile){
        Map<String,Set<String>> sets = new HashMap<>();
        List<String> contents = FileUtil.readFileContentToListByLine(inputfile,"UTF-8");

        for (String content :contents){
            String[] subContents = content.split("\t");
            String curDocId = subContents[0];
            String compareDocId = subContents[1];

            if (sets.containsKey(curDocId)){
                sets.get(curDocId).add(compareDocId);
            }else {
                Set<String> set = new HashSet<>();
                set.add(compareDocId);
                sets.put(curDocId,set);
            }
        }
        return sets;
    }
}
