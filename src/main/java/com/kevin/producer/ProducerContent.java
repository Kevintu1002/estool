package com.kevin.producer;

import com.kevin.data.DataQueue;
import com.kevin.jna.ComLibrary;
import com.kevin.model.DocContent;
import com.kevin.utils.StringUtil;
import com.kevin.utils.XmlParser;
import com.sun.jna.Pointer;
import com.kevin.inte.DataRecord;

public class ProducerContent implements Runnable {
    private String type;
    private Pointer pointer;
    public ProducerContent(String type, Pointer pointer){
        this.pointer = pointer;
        this.type = type;
    }
    @Override
    public void run() {

        int retryTimes = 0;
        int num =1;
        while (true){
            String filepath = DataQueue.getFileQueue();
            if (filepath == null){
                if (retryTimes > 5){
                    System.out.println(Thread.currentThread()+"read file thread go to break");
                    break;
                }

                retryTimes++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }else {
                System.out.println("get data num "+num);
                retryTimes = 0;
                DocContent docContent = getDocument(filepath,type);
                if (docContent != null){

                    DataQueue.setDocContentQueue(docContent);
                }
            }
        }

    }

    private DocContent getDocument(String dataPointer,String type){

        if (type == "file"){

            return XmlParser.parse(dataPointer);
        }else {
            //TODO get document content by docid
            System.out.println("begin to read content====");
            String[] docIds = dataPointer.split("_");
            System.out.println(docIds[0]);
            DataRecord.ByValue val = ComLibrary.CQuerylibrary.DatabaseQueryManager_getContentById(pointer, docIds[0]);
            DocContent docContent = new DocContent();
            docContent.setDocId(docIds[0]);
            docContent.setAppId(docIds[1]);
            docContent.setTitle(val.title);
            docContent.setAbs(StringUtil.remove(val.abs));
            docContent.setClaims(StringUtil.remove(val.claim));
            docContent.setDesc(StringUtil.remove(val.desc));
            System.out.println("end to read content====");
            return docContent;
        }


    }
}
