package com.kevin.controller;

import com.kevin.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping(value = "/search")
public class SearchController {


    @Autowired
    SearchService searchService;

    @GetMapping(value = "/searchById",produces = "application/json;charset=UTF-8")
    public String search(@RequestParam(value = "docId",required = true) String docId,
                         @RequestParam(value = "num",required = false) Integer num){

        System.out.println(docId);
        if (null == num) num = 10;
        docId = searchService.search(docId,num);
        return docId;
    }


    /**
     * 接受输入CSV文件
     * @param file
     * @return
     */
    @PostMapping(value = "/searchByFile",produces = "application/json;charset=UTF-8")
    public String searchByFile(@RequestParam(value = "file",required = true) MultipartFile file,
                               @RequestParam(value = "num",required = false) Integer num){
        return searchService.searchByFile(file,num);
    }


}
