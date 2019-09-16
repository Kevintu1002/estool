package com.kevin.service.impl;

import com.kevin.utils.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaculateRightRateServiceImpl {

    Log log = LogFactory.getLog(CaculateRightRateServiceImpl.class);

    private String resultfilepath = "/Users/guyuefei/test/100_2000.txt";
    private String answerfilepath = "/Users/guyuefei/test/cn_cn_citation_find(1).txt";


    public String caculate(){
        try{
            List<String> results = FileUtil.readFileContentToListByLine(resultfilepath,"utf-8");
            List<String> answers = FileUtil.readFileContentToListByLine(answerfilepath,"utf-8");

            Map<String,List<String>> resultmaps = loadMap(results);
            Map<String,List<String>> answermaps = loadMap(answers);

            float rate = caculateRate(resultmaps,answermaps);

            System.out.println("======== 得到的正确率为："+ rate +" %");

        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 装载数据到Map中
     * @param results
     * @return
     * @throws Exception
     */
    public Map<String,List<String>> loadMap(List<String> results) throws Exception{
        Map<String,List<String>> resultmaps = new HashMap<>();
        results.forEach(result -> {
            String[] args = result.split("\t");
            String arg1 = args[0];
            if(resultmaps.containsKey(arg1)){
                resultmaps.get(arg1).add(args[1]);
            }else{
                List<String> ans = new ArrayList<>();
                ans.add(args[1]);
                resultmaps.put(arg1,ans);
            }

        });

        return resultmaps;
    }

    /**
     * 计算正确率
     * @param resultmaps
     * @param answermaps
     * @return
     * @throws Exception
     */
    public float caculateRate(Map<String,List<String>> resultmaps ,Map<String,List<String>> answermaps )  throws Exception{

        int n = 0 ;//分子
        int m = resultmaps.keySet().size();//分母
        for (String queskey : resultmaps.keySet()){
            List<String> anss = resultmaps.get(queskey);
            List<String> ansans = answermaps.get(queskey);

            if(null == ansans || ansans.size() == 0){
                System.out.println("===== 参考答案中不存在考题 ： " + queskey);
                continue;
            }

            boolean flag = findTheSame(anss,ansans);
            if(flag){
                n ++;
                System.out.println("========= 考题：" + queskey + ", 结果：" + flag);
            }
        }

        float rate = n / m;
        return rate;
    }

    /**
     * 找到匹配的
     * @param anss
     * @param ansans
     * @return
     * @throws Exception
     */
    public boolean findTheSame( List<String> anss ,List<String> ansans ) throws Exception {

        Boolean flag = false;

        for (String rightans : ansans) {
            if(anss.contains(rightans)){
                System.out.println("============ 匹配到的答案 ：" + rightans);
                flag = true;
                break;
            }
//            for (String s : anss) {
//                if(rightans.equals(s)){
//                    flag = true;
//                    break;
//                }
//            }
        }
        return flag;
    }


}
