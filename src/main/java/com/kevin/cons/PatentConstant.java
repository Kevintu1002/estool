package com.kevin.cons;

public class PatentConstant {

    public static final String finaldocid = "docid";
    public static final String doctype_cn = "CN";
    public static final String title = "title";
    public static final String abs = "abs";
    public static final String claims = "claims";
    public static final String pdate = "pdate";
    public static final String doctype_en = "US";

    public static final String csvorigindirpath = "/data/disk1/patent/Django/media/filelist/";

    public static final String csvresultdirpath = "/tmp/";

    public static final String resultdirpath = "/data/disk1/patent/Django/media/csvout/";

    public static final String datefilterpath = "/data/disk1/patent/Django/media/cn_us_citation_publicdate_producedate";

    /**
     * 英文专利检索ES库
     */
    public static final String esjdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";

    /**
     * 中文专利检索ES库
     */
    public static final String cn_es_jdbcurl = "jdbc:sql4es://202.112.195.83:9300/patent821v9?cluster.name=patent";

    /**
     * 调用苏大服务前生成title、claim、abs文本位置，作为苏大服务的输入
     */
    public static final String csvoutdirpath = "/data/disk1/patent/Django/media/csvout/";
//    public static final String csvoutdirpath = "/Users/guyuefei/testout/";//本地测试

    /**
     * 中文专利调用google翻译生成结果
     */
    public static final String jsonfilepath = "/data/disk1/patent/Django/media/json/result.json";
//    public static final String jsonfilepath = "/Users/guyuefei/test/result.json";//本地测试
}
