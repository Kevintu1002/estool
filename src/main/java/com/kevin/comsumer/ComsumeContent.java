package com.kevin.comsumer;

import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.kevin.data.DataQueue;
import com.kevin.model.DocContent;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ComsumeContent implements Runnable{


    private String url;
    private String indexName;
    public ComsumeContent(String url,String indexName){

        this.url = url;
        this.indexName = indexName;
    }
    @Override
    public void run() {
        ESConnection esConnection=null;
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection("jdbc:sql4es://"+url+"/"+indexName+"?cluster.name=patent");

            int retryTimes = 0;
            while (true){
                DocContent docContent = DataQueue.getDocContentQueue();
                if (docContent == null){
                    if (retryTimes > 5){
                        System.out.println(Thread.currentThread()+"comsumer file thread go to break");
                        break;
                    }

                    retryTimes++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }else {
                    retryTimes = 0;

                    Statement st=null;
                    StringBuilder sql = new StringBuilder("insert into cn (docid,appid,category,title,abs,claims,description) values (");
                    try {
                        st = esConnection.createStatement();


                        sql.append("'").append(docContent.getDocId()).append("'").append(",");
                        sql.append("'").append(docContent.getAppId()).append("'").append(",");
                        sql.append("'").append(docContent.getCategory()).append("'").append(",");
                        sql.append("'").append(docContent.getTitle()).append("'").append(",");
                        sql.append("'").append(docContent.getAbs()).append("'").append(",");
                        sql.append("'").append(docContent.getClaims()).append("'").append(",");
                        sql.append("'").append(docContent.getDesc()).append("'").append(")");


                        st.execute(sql.toString());
                    } catch (Exception e) {
                        System.out.println(sql.toString());
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
            }
        } catch (Exception e) {
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
}
