package com.kevin.data;

import com.kevin.model.DocContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataQueue {

    private static Queue<String> fileQueue = new LinkedBlockingQueue<String>();

    private static Queue<DocContent> docContentQueue = new LinkedBlockingQueue<DocContent>();

    public static void setFileQueue(String file){
        fileQueue.offer(file);
    }

    public static synchronized String getFileQueue(){
        if (fileQueue.size() == 0){
            return null;
        }
        return fileQueue.poll();
    }

    public static synchronized void setDocContentQueue(DocContent docContent){
        docContentQueue.offer(docContent);
    }

    public static synchronized DocContent getDocContentQueue(){
        if (docContentQueue.size() == 0){
            return null;
        }
        return docContentQueue.poll();
    }

    public static synchronized List<DocContent> getDocContentQueue(int size){
        if (docContentQueue.size() == 0 || size <=0){
            return null;
        }
        List<DocContent> docs = new ArrayList<>();
        for (int i=0;i<size;i++){
            docs.add(docContentQueue.poll());
            if (docContentQueue.size() == 0){
                break;
            }
        }
        return docs;
    }



}
