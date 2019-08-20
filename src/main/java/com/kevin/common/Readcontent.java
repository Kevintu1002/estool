
package com.kevin.common;
import com.kevin.common.Data;

import java.io.*;

public class Readcontent extends Thread {

    private String filePath;
    private String charset;
    private int flag;

    public Readcontent(String filePath,String charset,int flag){

        this.filePath = filePath;
        this.charset = charset;
        this.flag = flag;
    }

    @Override
    public void run() {


        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filePath)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, charset));
            while (in.ready()) {
                String line = in.readLine();
                String[] contents = line.split("\t");
                String key = contents[0]+"_"+contents[1];

                StringBuilder sb = new StringBuilder();
                for (int i=2;i<contents.length;i++){
                    sb.append(StringUtil.remove(contents[i]));
                }

                Data.setData(key,sb.toString(),flag);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}
