package com.kevin.queue;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class SemilarDocQueue {

    private static final LinkedBlockingQueue<Object> linequeue = new LinkedBlockingQueue();

    public synchronized static  void push(List<String[]> line) throws Exception{
        linequeue.offer(line);
    }
    //出栈
    public synchronized static List<String[]> pop() throws Exception{
       return (List<String[]>)linequeue.poll();
    }

    public synchronized static  boolean isEmpty(){
        return linequeue.isEmpty();
    }

    public synchronized static int getSize(){
        return linequeue.size();
    }
}
