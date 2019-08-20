package com.kevin.common;

import java.util.List;

public class Comsumer extends Thread {

    public void run(){

//        ESConnection esConnection=null;
//        Statement st=null;
        try {
//            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
//
//            esConnection = (ESConnection) DriverManager
//                    .getConnection("jdbc:sql4es://202.112.195.82:9300/patent?cluster.name=patent");


            int retryTimes = 0;
            while (true){
                if (Data.getDataQueueSize() == 0){
                    if (retryTimes >5 && Data.getDataSize() ==0){
                        System.out.println(Thread.currentThread()+"===========break");
                        break;
                    }

                    try {
                        Thread.sleep(2000);
                        retryTimes++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {

                    List<String> contents = Data.getData();
//                    System.out.println("get data from queue==="+contents);
//                    st = esConnection.createStatement();
                    StringBuilder sql = new StringBuilder("insert into cn (docid,appid,title,abs,claims,description) values (");
                    for (int i=0;i<contents.size();i++){
                        if (i==0){
                            String[] subContents = contents.get(i).split("_");
                            sql.append("'").append(subContents[0]).append("'").append(",").append("'").append(subContents[1]).append("'").append(",");
                        }else {
                            if (i == contents.size()-1){
                                sql.append("'").append(contents.get(i)).append("'");
                            }else {
                                sql.append("'").append(contents.get(i)).append("'").append(",");
                            }
                        }
                    }
                    sql.append(")");

                    ESTool.execute(sql.toString());

//                    st.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
//            if (st != null){
//                try {
//                    st.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }


        }



    }
}
