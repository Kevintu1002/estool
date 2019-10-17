package com.kevin.app;

import com.kevin.service.impl.GetPatentContentImpl;

public class GetPatentContent {

    public static void main(String[] args){

        String absolutefilepath = args[0];
        String absoluteoutpath = args[1];
        GetPatentContentImpl searchServiceImpl = new GetPatentContentImpl();
        searchServiceImpl.getContent(absolutefilepath,absoluteoutpath);

    }
}
