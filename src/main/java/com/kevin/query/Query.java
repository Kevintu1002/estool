package com.kevin.query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bonc.usdp.sql4es.jdbc.ESConnection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Query {

    private ESConnection esConnection=null;
    public Query(){
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");

            esConnection = (ESConnection) DriverManager
                    .getConnection("jdbc:sql4es://202.112.195.82:9300/patent815?cluster.name=patent");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public String query(String sql){

        Statement st=null;
        try {
            st = esConnection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            JSONArray jsonArray = new JSONArray();
            while (rs.next()){

                String docId = rs.getString(1);
                String appId = rs.getString(2);
                String title = rs.getString(3);
                String abs = rs.getString(4);
                String claims = rs.getString(5);
                String desc = rs.getString(6);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("docId",docId);
//                jsonObject.put("appId",appId);
//                jsonObject.put("title",title);
//                jsonObject.put("abs",abs);
//                jsonObject.put("claims",claims);
//                jsonObject.put("desc",desc);
                jsonArray.add(jsonObject);
            }
            return jsonArray.toJSONString();
        } catch (Exception e) {
            System.out.println(sql);
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
        return "";

    }

    public void Close(){
        if (esConnection != null){
            try {
                esConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){

        Query query = new Query();
        String sql = "SELECT docid,appid,title,abs,claims,description FROM cn WHERE _search =" +
                " 'title:(一种彩色微透镜阵列制备方法，其特征在于，所述方法至少包括：提供基底；通过热熔工艺在基底上制备微透镜阵列母版；" +
                "将聚二甲基硅氧烷黏稠液覆盖在所述母版上，制得带有凹形微透镜阵列的聚二甲基硅氧烷软印章；将聚二甲基硅氧烷软印章上的微透镜阵" +
                "列结构复制在另一基底的颜色光阻剂上，制得微透镜阵列；重复上述步骤，利用不同的颜色光阻剂，可以制备不同颜色的微透镜阵列。)" +
                "and abs:(一种彩色微透镜阵列制备方法，其特征在于，所述方法至少包括：提供基底；通过热熔工艺在基底上制备微透镜阵列母版；将聚二甲基硅氧烷黏稠液覆盖在所述母版上，制得带有凹形微透镜阵列的聚二甲基硅氧烷软印章；将聚二甲基硅氧烷软印章上的微透镜阵列结构复制在另一基底的颜色光阻剂上，制得微透镜阵列；重复上述步骤，利用不同的颜色光阻剂，可以制备不同颜色的微透镜阵列。)" +
                " and claims(一种彩色微透镜阵列制备方法，其特征在于，所述方法至少包括：提供基底；通过热熔工艺在基底上制备微透镜阵列母版；将聚二甲基硅氧烷黏稠液覆盖在所述母版上，制得带有凹形微透镜阵列的聚二甲基硅氧烷软印章；将聚二甲基硅氧烷软印章上的微透镜阵列结构复制在另一基底的颜色光阻剂上，制得微透镜阵列；重复上述步骤，利用不同的颜色光阻剂，可以制备不同颜色的微透镜阵列。) " +
                "and description(一种彩色微透镜阵列制备方法，其特征在于，所述方法至少包括：提供基底；通过热熔工艺在基底上制备微透镜阵列母版；将聚二甲基硅氧烷黏稠液覆盖在所述母版上，制得带有凹形微透镜阵列的聚二甲基硅氧烷软印章；将聚二甲基硅氧烷软印章上的微透镜阵列结构复制在另一基底的颜色光阻剂上，制得微透镜阵列；重复上述步骤，利用不同的颜色光阻剂，可以制备不同颜色的微透镜阵列。)'" +
                " limit 100";
        String res =query.query(sql);
        query.Close();
        System.out.println(res);

    }
}



