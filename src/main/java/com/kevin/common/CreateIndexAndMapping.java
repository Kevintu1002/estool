package com.kevin.common;

import com.bonc.usdp.sql4es.jdbc.ESConnection;

import java.sql.*;

public class CreateIndexAndMapping {


    public static void main(String[] args) {
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ESConnection esConnection = (ESConnection) DriverManager
                    .getConnection("jdbc:sql4es://202.112.195.82:9300/patent816test?cluster.name=patent");

            Statement st = esConnection.createStatement();

//            String sql = "delete from cn";
            String sql = "insert into cn (docid,appid,title,abs,claims,description) values ('1','1','一种(种植牙)','一种种植牙','一种种植牙','一种种植牙')";

//            st.execute(sql);

            PreparedStatement pstmt = esConnection.prepareStatement(sql);
            pstmt.execute();
            esConnection.close();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {


        }
    }


}
