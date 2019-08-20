package com.kevin.producer;

import com.kevin.data.DataQueue;
import com.kevin.jna.ComLibrary;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataProducer {

    private String index;
    private String input;
    private String type;
    private Pointer pointer;
//    private Queue<String> fileQueue = new LinkedBlockingQueue<String>();
    public DataProducer(String index,String input,String type){
        this.index = index;
        this.input = input;
        this.type = type;
        init();
    }

    private void init(){
        this.pointer = ComLibrary.CQuerylibrary.DatabaseQueryManager_new(this.index,this.input);
    }

    public void produce(){

        getFileQueue(this.type);

        ExecutorService executor = Executors.newFixedThreadPool(1);

        for (int i=0;i<1;i++){
            executor.submit(new ProducerContent(type,pointer));
        }
        executor.shutdown();
    }

    private void getFileQueue(String type){

        if (type == "file"){
            File dir = new File(input);

            for (File subDir : dir.listFiles()){
                if (subDir.isDirectory()){

                    for (File subsubdir:subDir.listFiles()){
                        if (subsubdir.isDirectory()){
                            for (File file:subsubdir.listFiles()){
                                DataQueue.setFileQueue(file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }else {

            //TODO get docIDs from database
            System.out.println("begin to getAllId====");
            PointerByReference pidList = new PointerByReference();
            PointerByReference aidList = new PointerByReference();

            IntByReference size = new IntByReference();
            ComLibrary.CQuerylibrary.DatabaseQueryManager_getAllId(pointer, pidList, aidList, size);
            System.out.println("end to getAllId====");
            String[] pids = pidList.getValue().getStringArray(0, size.getValue());
            String[] aids = aidList.getValue().getStringArray(0, size.getValue());
            System.out.println("begin to read all id====");
            for (int i=0;i<pids.length;i++){
                String docId = pids[i]+"_"+aids[i];
                DataQueue.setFileQueue(docId);
            }
            System.out.println("end to read all id ====");

        }


    }
}
