package com.kevin.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bonc.usdp.sql4es.jdbc.ESConnection;
import com.csvreader.CsvWriter;
import com.kevin.cons.PatentConstant;
import com.kevin.service.SearchService;
import com.kevin.service.util.GoogleTranslate;
import com.kevin.service.util.PatentSearchUtil;
import com.kevin.task.FindSimilarDoc;
import com.kevin.task.FindSimilarDoc2;
import com.kevin.utils.*;
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

    @Value("${es.jdbc.url}")
    private String esjdbcurl = PatentConstant.esjdbcurl;
    @Value("${es.cn.jdbc.url}")
    private String cn_es_jdbcurl = PatentConstant.cn_es_jdbcurl;

    @Value("${csv.origin.dir.path}")
    private String csvorigindirpath = PatentConstant.csvorigindirpath;

    @Value("${csv.out.dir.path}")
    private String csvoutdirpath = PatentConstant.csvoutdirpath;

    @Value("${csv.result.dir.path}")
    private String csvresultdirpath = PatentConstant.csvresultdirpath;

    @Value("${result.dir.path}")
    private String resultdirpath = PatentConstant.resultdirpath;

    @Value("${datefilter.path}")
    private String datefilterpath = PatentConstant.datefilterpath;

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
        /**
         * type = 1 不用苏大服务进行精选  type = 2 使用苏大服务进行精选
         */
        JSONObject returnjson = new JSONObject();
        InputStream ins = null;
        Reader in = null;
        Map<String,String> docIds = new HashMap<>();
        try{
//            String absolutefilepath = csvorigindirpath + filename;
            String absolutefilepath = filename;

            log.info("====== The input file name is ："+absolutefilepath);
            long start = System.currentTimeMillis();
            System.out.println("======= input type :"+type);
//            List<String> lines = FileUtil.readFileContentToListByLine(absolutefilepath,"utf-8");
            docIds = CSVUtil.readCsvFile(absolutefilepath);

            System.out.println("======= read file success  = "+docIds.toString());
            if(docIds.get("2").contains(PatentConstant.doctype_cn)){
                /**
                 * 中文doc，查询处理
                 */
                System.out.println("=================== 处理中文专利查询 =====================");
                long googlestart = System.currentTimeMillis();
                boolean googleflag = GoogleTranslate.getGoogleTranslate(absolutefilepath);
                long googleend = System.currentTimeMillis();
                System.out.println("=================== 得到谷歌翻译结果 ： "+googleflag+" ,  耗时： "+(googleend - googlestart) /1000 +" s =============");
                List<String> filepaths = DocSearchHandler(docIds,num,type,PatentConstant.doctype_cn,googleflag);
                returnjson.put("filepath",filepaths);
            }else{
                /**
                 * 英文doc，查询处理
                 */
                System.out.println("=================== 处理英文专利查询 =====================");
                if(null == num) num = 10;
                List<String> filepath = DocSearchHandler(docIds,num,type,PatentConstant.doctype_en,false);
                returnjson.put("filepath",filepath);
            }
            if("2".equals(type)){
                System.out.println("java 程序执行完毕，开始执行ssh");
//                执行linux脚本
                LinuxSCP2Util scp = LinuxSCP2Util.getInstance(IP, PORT,
                        USERNAME,PASSWORD);
                scp.putFile(csvoutdirpath+ PatentConstant.title +".tsv", PatentConstant.title +".tsv", REMOTEURL, null);
                scp.putFile(csvoutdirpath + PatentConstant.claims +".tsv", PatentConstant.claims +".tsv", REMOTEURL, null);
                scp.putFile(csvoutdirpath + PatentConstant.abs +".tsv", PatentConstant.abs +".tsv", REMOTEURL, null);

                String command = "cd /home/ky/suda_test/ "+"\n"
                        +"source ./venv/bin/activate"+"\n"
                        + "python zhuanli_matching_attention_three_csv.py \n";

                boolean flag = ShellUtils.executeRemoteShell(IP,USERNAME,PASSWORD,command);
            }

            long end = System.currentTimeMillis();
            System.out.println("耗时：" + (end - start) / 1000 + " s");
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
     * 处理专利文本
     * @param docIds
     * @param num
     * @param type
     * @return
     * @throws Exception
     */
    private List<String> DocSearchHandler(Map<String,String> docIds,Integer num,String type,String doctype,boolean googleflag) throws Exception{
        List<String> paths = new ArrayList<>(3);
        /**
         * type = 1 不用苏大服务进行精选  type = 2 使用苏大服务进行精选
         */
        if("1".equals(type)){
            paths.add(noUseSuDaService(docIds,num,doctype,googleflag));
        }else{
            String titlepath = csvoutdirpath + PatentConstant.title +".tsv";
            String claimpath = csvoutdirpath + PatentConstant.claims +".tsv";
            String abspath = csvoutdirpath + PatentConstant.abs +".tsv";

            useSuDaService(docIds,num,doctype,titlepath,claimpath,abspath,googleflag);

            paths.add(titlepath);
            paths.add(claimpath);
            paths.add(abspath);
        }
        return paths;
    }

    /**
     * 不使用苏大服务
     * @param docIds
     * @param num
     * @return
     */
    private String noUseSuDaService(Map<String,String> docIds,Integer num,String doctype,boolean googleflag){
        String absoluteoutpath = csvresultdirpath +"sim_result.csv";
        try{
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            ESConnection esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
            ESConnection escnConnection = null;
            if(PatentConstant.doctype_cn.equals(doctype)){
                escnConnection = (ESConnection) DriverManager.getConnection(cn_es_jdbcurl);
            }

            /**
             * 1、首先解析csv文件，将序号、docid装入队列中
             * 2、线程读取队列中的数据，进行处理，并将处理结果存入结果数据的队列中
             * 3、当所有线程执行完毕，从结果队列中取出数据，写入到输出文件中
             */
            CsvWriter csvWriter = new CsvWriter(absoluteoutpath);
            Set<String> keynums = docIds.keySet();

            List<Future<List>> results = new LinkedList<Future<List>>();
            ThreadPoolExecutor excutor = new ThreadPoolExecutor(5, Integer.parseInt(threadpoolsize), 0,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

            for(String m : keynums){
                FindSimilarDoc findSimilarDoc = new FindSimilarDoc(esConnection,escnConnection,m,docIds.get(m),num,doctype,googleflag);
                Future<List> result =  excutor.submit(findSimilarDoc);
                results.add(result);
            }

            excutor.shutdown();

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
            esConnection.close();
            if(null != escnConnection){
                escnConnection.close();
            }
        }catch (Exception e){
            System.out.println("======= no use SuDa service occuer error ===");
            log.error(e.getMessage(),e);
        }
        return absoluteoutpath;

    }

    /**
     * 使用苏大服务
     * @param docIds
     * @param num
     * @param titlepath
     * @param claimpath
     * @param abspath
     */
    private void useSuDaService(Map<String,String> docIds,Integer num,String doctype,String titlepath,
                                String claimpath,String abspath,boolean flag){
        try{
            Class.forName("com.bonc.usdp.sql4es.jdbc.ESDriver");
            ESConnection esConnection = (ESConnection) DriverManager.getConnection(esjdbcurl);
            ESConnection escnConnection = null;
            String uuid = UUID.randomUUID().toString().replace("-", "");

            File file1 = new File(titlepath);
            if(file1.exists()){
                file1.delete();
            }
            File file2 = new File(claimpath);
            if(file2.exists()){
                file2.delete();
            }
            File file3 = new File(abspath);
            if(file3.exists()){
                file3.delete();
            }

            Set<String> keynums = docIds.keySet();

            if(PatentConstant.doctype_cn.equals(doctype)){
                escnConnection = (ESConnection) DriverManager.getConnection(cn_es_jdbcurl);
            }

            List<Future<Map>> results = new LinkedList<Future<Map>>();
            ThreadPoolExecutor excutor = new ThreadPoolExecutor(Integer.parseInt(threadpoolsize), Integer.parseInt(threadpoolsize), 0,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());

//        Map<String,List<String>> datefilter = FileUtil.readDateFilter(datefilterpath);
            for(String m : keynums){
//           String pdate =  datefilter.get(docid).get(0);
                FindSimilarDoc2 findSimilarDoc2 = new FindSimilarDoc2(esConnection,escnConnection,doctype,m+"",docIds.get(m),num,null,flag);
                Future<Map> result =  excutor.submit(findSimilarDoc2);
                results.add(result);
            }

            excutor.shutdown();

            while (true){
                System.out.println("============== 主线程在等待 ============");
                if(excutor.isTerminated()){
                    System.out.println("============== 所有线程执行完毕 ============");
                    break;
                }
            }

            esConnection.close();
            if(null != escnConnection){
                escnConnection.close();
            }

        }catch (Exception e){
            System.out.println("======= use SuDa service occuer error ===");
            log.error(e.getMessage(),e);
        }


    }


    private List<String> getCompareDocIds(ESConnection esConnection,List<String> contents,int num){

        List<String> docIds = new ArrayList<>();
        Map<String,Float> scores = new HashMap<>();
        for (String content:contents){
            Statement st=null;
            try {
                st = esConnection.createStatement();
                StringBuilder sql = new StringBuilder();
//                sql.append("select docid,appid,_score from en WHERE _search = 'title:(").append(content).append(") or abs:(")
//                        .append(content).append(") or claims:(").append(content).append(") or description:(")
//                        .append(content).append(") ' limit "+num);

                sql.append("select docid,appid,_score from en WHERE _search = ' abs:(")
                        .append(content).append(") or claims:(").append(content).append(") ' limit "+num);

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
