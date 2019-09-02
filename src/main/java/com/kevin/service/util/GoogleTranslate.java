package com.kevin.service.util;

import com.kevin.utils.LinuxSCP2Util;
import com.kevin.utils.ShellUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GoogleTranslate {
   private static Log log = LogFactory.getLog(GoogleTranslate.class);

    public static final String IP = "222.28.84.165";

    public static final int PORT = 22;

    public static final String USERNAME = "ky";

    public static final String PASSWORD = "12345";

    public static final String REMOTEURL = "/home/ky/patent_search/CHN/";


    public static boolean getGoogleTranslate(String absolutefilepath){

        try{
            LinuxSCP2Util scp = LinuxSCP2Util.getInstance(IP, PORT,
                    USERNAME,PASSWORD);
            scp.putFile(absolutefilepath,  "CN_ID_TEST_Samples.csv", REMOTEURL, null);

            String command = "source  /ect/profile \n" +
                    "source /home/ky/patent_search/venv/bin/activate \n" +
                    "cd /home/ky/patent_search/CHN \n" +
                    "python patent_crawl.py";
            boolean flag = ShellUtils.executeRemoteShell(IP,USERNAME,PASSWORD,command);
            return flag;
        }catch (Exception e){
            System.out.println("====== ");
            log.error(e.getMessage(),e);
        }

        return false;
    }

}
