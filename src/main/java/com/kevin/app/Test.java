package com.kevin.app;

import com.kevin.utils.ShellUtils;

public class Test {

    public static void main(String[] args){

        String IP = "222.28.84.9";
        int PORT = 22;
        String USERNAME = "root";
        String PASSWORD = "nlproot";

        String command = "cd /data/disk1/patent/Django/media/csvout"+"\n"
                +"rm -rf *"+"\n"
                + "touch bbb.txt";

        boolean flag = ShellUtils.executeRemoteShell(IP,USERNAME,PASSWORD,command);

    }
}
