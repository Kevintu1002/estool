package com.blcultra.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class LineQueue {

    private static final LinkedBlockingQueue<Object> linequeue = new LinkedBlockingQueue();

    public synchronized static  void push(String line) throws Exception{
        linequeue.offer(line);
    }
    //出栈
    public synchronized static String pop() throws Exception{
       return (String)linequeue.poll();
    }

    public synchronized static  boolean isEmpty(){
        return linequeue.isEmpty();
    }

    public synchronized static int getSize(){
        return linequeue.size();
    }
}
