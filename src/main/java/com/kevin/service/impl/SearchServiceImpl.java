package com.kevin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.csvreader.CsvWriter;
import com.kevin.data.DataQueue;
import com.kevin.service.SearchService;
import com.kevin.task.FindSimilarDoc;
import com.kevin.task.FindSimilarDoc2;
import com.kevin.utils.CSVUtil;
import com.kevin.utils.FileUtil;
import com.kevin.utils.LinuxSCP2Util;
import com.kevin.utils.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

@Service("searchService")
public class SearchServiceImpl implements SearchService {
    Log log = LogFactory.getLog(SearchServiceImpl.class);

    private static final String finaldocid = "docid";
    private static final String title = "title";
    private static final String abs = "abs";
    private static final String claims = "claims";

    @Value("${es.jdbc.url}")
    private String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v13?cluster.name=patent";

    @Value("${csv.origin.dir.path}")
    private String csvorigindirpath = "/data/disk1/patent/Django/media/filelist/";

    @Value("${csv.out.dir.path}")
    private String csvoutdirpath = "/data/disk1/patent/Django/media/csvout/";

    @Value("${csv.result.dir.path}")
    private String csvresultdirpath = "/tmp/";

    @Value("${result.dir.path}")
    private String resultdirpath = "/data/disk1/patent/Django/media/csvout/";

    @Value("${datefilter.path}")
    private String datefilterpath = "G:\\software\\es\\cn_us_citation_publicdate_producedate";

    @Value("${threadpool.size}")
    private String threadpoolsize = "10";


    public static final String IP = "222.28.84.124";

    public static final int PORT = 22;

    public static final String USERNAME = "ky";

    public static final String PASSWORD = "12345";

    public static final String REMOTEURL = "/home/ky/suda_test/data/test";


    @Override
    public String search(String docId,int num) {

        ESConnection esConnection=null;
        try {
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
            List<String> contents = getContents(esConnection,docId);
            List<String> searchRes = getCompareDocIds(esConnection,contents,num);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("docIds",searchRes);
            return jsonObject.toJSONString();
        }catch (Exception e){

            e.printStackTrace();
        }finally {
            if (esConnection != null){
                try {
                    esConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public String searchByFile(String filename,String type,Integer num) {
        JSONObject returnjson = new JSONObject();
        InputStream ins = null;
        Reader in = null;
        Map<String,String> docIds = new HashMap<>();
        try{
//            String absolutefilepath = csvorigindirpath + filename;
            String absolutefilepath = filename;

            log.info("====== The input file name is ："+absolutefilepath);

            if("3".equals(type)){//测试中文
                long start = System.currentTimeMillis();
                Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
                ESConnection esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
                ESConnection escnConnection = (ESConnection) DriverManager.getConnection(cn_es_jdbcurl);
                List<String> lines = FileUtil.readFileContentToListByLine(absolutefilepath,"utf-8");

                returnjson.put("filepath",outCsv4(esConnection,escnConnection,lines,num,abs));

                long end = System.currentTimeMillis();

                //执行linux脚本
                LinuxSCP2Util scp = LinuxSCP2Util.getInstance(IP, PORT,
                        USERNAME,PASSWORD);
                scp.putFile(csvoutdirpath, "*", REMOTEURL, null);

                String command = "cd /home/ky/suda_test/ "+"\n"
                        +"source ./venv/bin/activate"+"\n"
                        + "python zhuanli_matching_attention_three_csv.py\n";


                System.out.println("耗时：" + (end - start) / 1000 + " s");
                return returnjson.toJSONString();

            }
            String filesubffix = filename.substring(filename.lastIndexOf(".")).toUpperCase();
            docIds = CSVUtil.readCsvFile(absolutefilepath);
//            if(filesubffix.contains("CSV")){
//                docIds = CSVUtil.readCsvFile(absolutefilepath);
//            }else if(filesubffix.contains("XLSX")){
//                XSSFWorkbook workbook = new XSSFWorkbook(new File(absolutefilepath));
//                docIds = ExcelUtil.readExcel(workbook,0,0);
//            }else if(filesubffix.contains("XLS")){
//                HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(absolutefilepath));
//                docIds = ExcelUtil.readExcel(workbook,0,0);
//            }else{
//                returnjson.put("code","0000");
//                returnjson.put("flag",false);
//                returnjson.put("message","暂不支持该种文件类型");
//                return returnjson.toJSONString();
//            }
            if(null == num) num = 10;
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            ESConnection esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);

            if("1".equals(type)){
                long start = System.currentTimeMillis();

//                String filepath = outCsv_back(esConnection,docIds,num);//单线程处理

                String filepath = outCsv1(esConnection,docIds,num);
                returnjson.put("filepath",filepath);

                long end = System.currentTimeMillis();
                System.out.println("耗时：" + (end - start) / 1000 + " s");
            }else{
                /**
                 * 生成导出CSV
                 */
                List<String> filepaths = new ArrayList<>(2);
                filepaths.add(outCsv2(esConnection,docIds,num,abs));
                filepaths.add(outCsv2(esConnection,docIds,num,claims));
                returnjson.put("filepath",filepaths);
            }
            return returnjson.toJSONString();

        }catch (Exception e){
            log.error(e.getMessage(),e);
        }finally {
            try{
                ins.close();
                in.close();
            }catch (Exception e){

            }

        }
        return null;
    }

    @Override
    public String getResultFile(HttpServletRequest request, HttpServletResponse response, String filename) {
        String absolutefilepath = resultdirpath + filename;
        response.setContentType("application/force-download");// 设置强制下载不打开
        response.addHeader("Content-Disposition", "attachment;fileName=" + filename);// 设置文件名
        byte[] buffer = new byte[1024];
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(absolutefilepath));
            OutputStream os = new BufferedOutputStream(response.getOutputStream());
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }

            os.flush();
            os.close();
            return "下载成功";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * type=1 多线程处理
     * @param esConnection
     * @param docIds
     * @param num
     * @return
     * @throws Exception
     */
    private String outCsv1(ESConnection esConnection,Map<String,String> docIds,Integer num) throws Exception{
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String absoluteoutpath = csvresultdirpath +"sim_result.csv";

        /**
         * 1、首先解析csv文件，将序号、docid装入队列中
         * 2、线程读取队列中额数据，进行处理，并将处理结果存入结果数据的队列中
         * 3、当所以线程执行完毕，从结果队列中取出数据，写入到输出文件中
         */
        CsvWriter csvWriter = new CsvWriter(absoluteoutpath);
        Set<String> keynums = docIds.keySet();

        List<Future<List>> results = new LinkedList<Future<List>>();
        ThreadPoolExecutor excutor = new ThreadPoolExecutor(5, Integer.parseInt(threadpoolsize), 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for(String m : keynums){
            FindSimilarDoc findSimilarDoc = new FindSimilarDoc(esConnection,m,docIds.get(m),num,null);
            Future<List> result =  excutor.submit(findSimilarDoc);
            results.add(result);
        }

        for(Future<List> doc : results){
            List<String[]> docrows = doc.get();
            docrows.forEach(docrow -> {
                try{
                    csvWriter.writeRecord(docrow);
                }catch (Exception e){
                    log.error("输出文件过程出现错误，内容为："+docrow);
                }

            });
        }

        csvWriter.close();
        return  absoluteoutpath;
    }

    /**
     * type = 1 单线程处理
     * @param esConnection
     * @param docIds
     * @param num
     * @return
     * @throws Exception
     */
    private String outCsv1_back(ESConnection esConnection,Map<String,String> docIds,Integer num) throws Exception{
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String absoluteoutpath = csvresultdirpath +"sim_result.csv";
        CsvWriter csvWriter = new CsvWriter(absoluteoutpath);
        Set<String> keynums = docIds.keySet();
        for(String m : keynums){
            Map<String,String> contents = getContents2(esConnection,docIds.get(m));
            List<Map<String,String>> searchRes = getCompareDocIds2(esConnection,contents,num);
            //写入输出csv
            int  n = 1;
            for(Map<String,String> resdocid : searchRes){
                String[] docids = {m,docIds.get(m),n+"",resdocid.get(finaldocid)};
                csvWriter.writeRecord(docids);
                n ++;
            }
        }
        csvWriter.close();
        return  absoluteoutpath;
    }

    /**
     * type = 2 单线程处理 生成两个csv文件
     * @param esConnection
     * @param docIds
     * @param num
     * @param type
     * @return
     * @throws Exception
     */
    private String outCsv2(ESConnection esConnection,Map<String,String> docIds,Integer num,String type) throws Exception{
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String absoluteoutpath = csvoutdirpath + type +"_"+ uuid +".csv";
        CsvWriter csvWriter = new CsvWriter(absoluteoutpath);
        Set<String> keynums = docIds.keySet();
        for(String m : keynums){
            Map<String,String> contents = getContents2(esConnection,docIds.get(m));
            List<Map<String,String>> searchRes = getCompareDocIds2(esConnection,contents,num);
            //写入输出csv
            int n = 1;
            for(Map<String,String> resdocid : searchRes){
                String[] docids = {m,docIds.get(m),contents.get(type),n+"",resdocid.get(finaldocid),resdocid.get(type)};
                csvWriter.writeRecord(docids);
                n ++;
            }
        }
        csvWriter.close();
        return absoluteoutpath;
    }

    /**
     * type = 3 多线程处理，生成最终文件
     * @param esConnection
     * @param docIds
     * @param num
     * @return
     * @throws Exception
     */
    private String outCsv3(ESConnection esConnection,List<String> docIds,Integer num) throws Exception{
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String absoluteoutpath = csvresultdirpath +"sim_result.csv";

        /**
         * 1、首先解析csv文件，将序号、docid装入队列中
         * 2、线程读取队列中额数据，进行处理，并将处理结果存入结果数据的队列中
         * 3、当所以线程执行完毕，从结果队列中取出数据，写入到输出文件中
         */
        CsvWriter csvWriter = new CsvWriter(absoluteoutpath);

        List<Future<List>> results = new LinkedList<Future<List>>();
        ThreadPoolExecutor excutor = new ThreadPoolExecutor(8, Integer.parseInt(threadpoolsize), 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        int m = 1;
        for(String docid : docIds){
            FindSimilarDoc findSimilarDoc = new FindSimilarDoc(esConnection,m+"",docid,num,null);
            Future<List> result =  excutor.submit(findSimilarDoc);
            results.add(result);
            m ++;
        }

        for(Future<List> doc : results){
            List<String[]> docrows = doc.get();
            docrows.forEach(docrow -> {
                try{
                    csvWriter.writeRecord(docrow);
                }catch (Exception e){
                    log.error("输出文件过程出现错误，内容为："+docrow);
                }

            });
        }

        csvWriter.close();
        return  absoluteoutpath;
    }

    /**
     * type = 3 多线程处理 生成中间两个csv文件
     * @param docIds
     * @param num
     * @param type
     * @return
     * @throws Exception
     */
    private List<String> outCsv4(ESConnection esConnection,ESConnection escnConnection,List<String> docIds,Integer num,String type) throws Exception{
        String uuid = UUID.randomUUID().toString().replace("-", "");
//        String absoluteoutpath = csvoutdirpath + type +"_"+ uuid +".tsv";

        List<Future<Map>> results = new LinkedList<Future<Map>>();
        ThreadPoolExecutor excutor = new ThreadPoolExecutor(Integer.parseInt(threadpoolsize), Integer.parseInt(threadpoolsize), 0,
                TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        int m = 1;
        Map<String,List<String>> datefilter = FileUtil.readDateFilter(datefilterpath);
        for(String docid : docIds){
           String pdate =  datefilter.get(docid).get(0);
            FindSimilarDoc2 findSimilarDoc2 = new FindSimilarDoc2(esConnection,escnConnection,m+"",docid,num,pdate);
            Future<Map> result =  excutor.submit(findSimilarDoc2);
            results.add(result);
            m ++;
        }

        StringBuilder titlebuilder = new StringBuilder("");
        StringBuilder claimbuilder = new StringBuilder("");
        StringBuilder absbuilder = new StringBuilder("");
        Map<String,List<String>> map = null;
        String titlepath = csvoutdirpath + title +".tsv";
        String claimpath = csvoutdirpath + claims +".tsv";
        String abspath = csvoutdirpath + abs +".tsv";

        if(results.size() > 10){
            //考题数
            int n = results.size() % 10 == 0 ? results.size() / 10 :  results.size() / 10 + 1;

            //前10个数写入文件
            for(int j = 0 ; j < 10; j ++){
                map =  results.get(j).get();
                List<String> titlesss = map.get(title);
                for(String content : titlesss){
                    titlebuilder.append(content);
                }

                List<String> abssss = map.get(abs);
                for(String content : abssss){
                    absbuilder.append(content);
                }

                List<String> claimssss = map.get(claims);
                for(String content : claimssss){
                    claimbuilder.append(content);
                }
            }

            FileUtil.writeContent(titlepath,titlebuilder.toString());
            FileUtil.writeContent(claimpath,claimbuilder.toString());
            FileUtil.writeContent(abspath,absbuilder.toString());

            titlebuilder = new StringBuilder("");
            claimbuilder = new StringBuilder("");
            absbuilder = new StringBuilder("");

            for(int i = 1;i < n ; i ++){//每10个一组进行一轮遍历
                for(int j = i * 10; j < (i+1) * 10; j ++){
                    if(j >= n){
                        break;
                    }
                    map =  results.get(j).get();
                    List<String> titlesss = map.get(title);
                    for(String content : titlesss){
                        titlebuilder.append(content);
                    }

                    List<String> abssss = map.get(abs);
                    for(String content : abssss){
                        absbuilder.append(content);
                    }

                    List<String> claimssss = map.get(claims);
                    for(String content : claimssss){
                        claimbuilder.append(content);
                    }
                }

                FileUtil.writeContentAppend(titlepath,titlebuilder.toString());
                FileUtil.writeContentAppend(claimpath,claimbuilder.toString());
                FileUtil.writeContentAppend(abspath,absbuilder.toString());

                //追加文件内容
                titlebuilder = new StringBuilder("");
                claimbuilder = new StringBuilder("");
                absbuilder = new StringBuilder("");

            }
        }else{//小于10
            for(Future<Map> doc : results){
                map = doc.get();
                List<String> titlesss = map.get(title);
                for(String content : titlesss){
                    titlebuilder.append(content);
                }

                List<String> abssss = map.get(abs);
                for(String content : abssss){
                    absbuilder.append(content);
                }

                List<String> claimssss = map.get(claims);
                for(String content : claimssss){
                    //截取claim
                    if(content.length() > 5000){
                        content = content.substring(0,5000);
                    }
                    claimbuilder.append(content);
                }
                map = null;
            }
            FileUtil.writeContent(titlepath,titlebuilder.toString());
            FileUtil.writeContent(claimpath,claimbuilder.toString());
            FileUtil.writeContent(abspath,absbuilder.toString());
        }



        List<String> paths = new ArrayList<>(3);
        paths.add(titlepath);
        paths.add(claimpath);
        paths.add(abspath);
        return paths;
    }

    private List<Map<String,String>> getCompareDocIds2(ESConnection esConnection,Map<String,String> contentdetail,int num){

        List<Map<String,String>> docIds = new ArrayList<>();
        Map<String,Map<String,Object>> scores = new HashMap<>();
        List<String> contents2 = new ArrayList<>();
        contents2.add(contentdetail.get(title));
        contents2.add(contentdetail.get(abs));
        contents2.add(contentdetail.get(claims));

        log.info("========== get compare docid ===============");
        for (String content:contents2){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("select docid,appid,_score,abs,claims from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                        .append(content).append(") or claims:(").append(content).append(") or description:(")
                        .append(content).append(") ' limit "+num);

                ResultSet rs = st.executeQuery(sql.toString());
                while (rs.next()){

                    String docId = rs.getString(1);
                    if (StringUtil.empty(docId)){
                        docId = "";
                    }
                    String appId = rs.getString(2);
                    float score = rs.getFloat(3);
                    String abs = rs.getString(4);
                    String claims = rs.getString(5);
                    String key = appId+"_"+docId;
                    if (scores.containsKey(key)){
                        float sco = (Float) scores.get(key).get("score");
                        scores.get(key).put("score",sco+score);
                    }else {
                        Map<String,Object> scoremap = new HashMap<>(4);
                        scoremap.put(abs,abs);
                        scoremap.put(claims,claims);
                        scoremap.put("score",score);
                        scores.put(key,scoremap);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }finally {
                if (st != null){
                    try {
                        st.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<Map.Entry<String, Map<String,Object>>> list = new LinkedList<Map.Entry<String, Map<String,Object>>>(scores.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String, Map<String,Object>>>() {
            @Override
            public int compare(Map.Entry<String, Map<String,Object>> o1,
                               Map.Entry<String, Map<String,Object>> o2) {
                return ((Float)o2.getValue().get("score")).compareTo( (Float)o2.getValue().get("score"));
            }
        });
        int length = list.size() > num?num:list.size();
        for (int i=0;i<length;i++){
            String key = list.get(i).getKey();
            Map<String,Object> key_value = list.get(i).getValue();
            String[] ids = key.split("_");

            for (int j=0;j<ids.length;j++){
                String id = ids[j];
                if (!StringUtil.empty(id)){
                    Map<String,String> detail = new HashMap<>(3);
                    detail.put(finaldocid,id);
                    detail.put(claims,key_value.get(claims)+"");
                    detail.put(abs,key_value.get(abs)+"");
                    docIds.add(detail);
                }
            }
        }
        return docIds;
    }


    private List<String> getCompareDocIds(ESConnection esConnection,List<String> contents,int num){

        List<String> docIds = new ArrayList<>();
        Map<String,Float> scores = new HashMap<>();
        for (String content:contents){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
                sql.append("select docid,appid,_score from en WHERE _search = 'title:(").append(content).append(") or abs:(")
                        .append(content).append(") or claims:(").append(content).append(") or description:(")
                        .append(content).append(") ' limit "+num);

                ResultSet rs = st.executeQuery(sql.toString());
                while (rs.next()){

                    String docId = rs.getString(1);
                    if (StringUtil.empty(docId)){
                        docId = "";
                    }
                    String appId = rs.getString(2);
                    float score = rs.getFloat(3);
                    String key = appId+"_"+docId;
                    if (scores.containsKey(key)){
                        float sco = scores.get(key);
                        scores.put(key,sco+score);
                    }else {
                        scores.put(key,score);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (st != null){
                    try {
                        st.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        List<Map.Entry<String, Float>> list = new LinkedList<Map.Entry<String, Float>>(scores.entrySet());
        Collections.sort(list,new Comparator<Map.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<String, Float> o1,
                               Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo( o1.getValue());
            }
        });
        int length = list.size() > num?num:list.size();
        for (int i=0;i<length;i++){
            String key = list.get(i).getKey();
            String[] ids = key.split("_");

            for (int j=0;j<ids.length;j++){
                String id = ids[j];
                if (!StringUtil.empty(id)){
                    docIds.add(id);
                }
            }
        }
        return docIds;
    }

    private Map<String,String> getContents2(ESConnection esConnection, String docId){
        Map<String,String> detail = new HashMap<>(3);
        Statement st=null;
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());
            while (res.next()){
                detail.put(title,StringUtil.remove(res.getString(1)));
                detail.put(abs,StringUtil.remove(res.getString(2)));
                detail.put(claims,StringUtil.remove(res.getString(3)));
//                contents.add(res.getString(3));
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }finally {
            if (st != null){
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("=========== get origin patent content by docid ,docid is："+ docId + ",detail is :" + detail.toString());
        return detail;

    }

    private List<String> getContents(ESConnection esConnection, String docId){

        //TODO get document content by docid

        Statement st=null;
        List<String> contents = new ArrayList<>();
        try {
            st = esConnection.createStatement();

            String sql = "select title,abs,claims from en where docid='"+docId+"'";
            ResultSet res = st.executeQuery(sql.toString());

            while (res.next()){
                contents.add(StringUtil.remove(res.getString(1)));
                contents.add(StringUtil.remove(res.getString(2)));
                contents.add(StringUtil.remove(res.getString(3)));
//                contents.add(res.getString(3));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (st != null){
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return contents;

    }
}
