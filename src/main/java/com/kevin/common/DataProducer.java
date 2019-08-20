
package com.kevin.common;
import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class DataProducer {



    public void producer(String inputDir){

        File dir = new File(inputDir);
        for (File file: dir.listFiles()) {
            String fileName = file.getName();
            String inputPath = file.getAbsolutePath()+File.separator+fileName;
            Thread thread = new Readcontent(inputPath,"UTF-8",getIndex(fileName));
            thread.start();
        }

    }

    private int getIndex(String fileName){
        if (fileName.endsWith("title")){
            return 0;
        }else if (fileName.endsWith("abstract")){

            return 1;
        }else if (fileName.endsWith("claim")){

            return 2;
        }else {
            return 3;
        }
    }


}
