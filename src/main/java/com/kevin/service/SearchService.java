package com.kevin.service;

import org.springframework.web.multipart.MultipartFile;

public interface SearchService {

    public String search(String docId,int num);

    public String searchByFile(MultipartFile file,Integer num);

}
