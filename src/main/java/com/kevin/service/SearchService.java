package com.kevin.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SearchService {

    public String search(String docId,int num);

    public String searchByFile(String file,String type,Integer num);

    public String getResultFile(HttpServletRequest request, HttpServletResponse response,  String file);

}
