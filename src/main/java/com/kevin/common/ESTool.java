package com.kevin.common;

import com.bonc.usdp.sql4es.jdbc.ESConnection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ESTool {

    private static ESConnection esConnection=null;

    public static void init(){


        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");

            esConnection = (ESConnection) DriverManager
                    .getConnection("jdbc:sql4es://202.112.195.82:9300/patent8151413?cluster.name=patent");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void execute(String sql){
//        System.out.println(sql);
        Statement st=null;
        try {
            st = esConnection.createStatement();
            st.execute(sql);
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


    }

    public static void close(){

        if (esConnection != null){
            try {
                esConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
