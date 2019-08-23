package com.kevin.controller;

import com.kevin.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
     * @param filename 输入
     *  @param type 类型：1 - 不用苏大服务  2-使用苏大服务
     * @return
     */
    @GetMapping(value = "/searchByFile",produces = "application/json;charset=UTF-8")
    public String searchByFile(@RequestParam(value = "filename",required = true) String filename,
                               @RequestParam(value = "type",required = false) String type,
                               @RequestParam(value = "num",required = false) Integer num){
        if(null == type){
            type = "1";
        }
        return searchService.searchByFile(filename,type,num);
    }

    /**
     * 下载文件
     * @param filename
     * @return
     */
    @GetMapping(value = "/getResultFile",produces = "application/json;charset=UTF-8")
    public String getResultFile(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(value = "filename",required = true) String filename){
        return searchService.getResultFile(request,response,filename);
    }
}
