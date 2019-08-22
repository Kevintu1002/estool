package com.kevin.app;

import com.kevin.service.impl.SearchServiceImpl;

public class Search {

    public static void main(String[] args){
        String filename = args[0];
        String num = args[1];
        SearchServiceImpl searchServiceImpl = new SearchServiceImpl();
        searchServiceImpl.searchByFile(filename, null == num? 20 : Integer.parseInt(num));

    }
}
