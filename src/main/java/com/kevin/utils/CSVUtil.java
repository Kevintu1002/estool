package com.kevin.utils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVUtil {

    /**
     * 读取csv文件，返回Map数组，每一个map是一行记录，key对应的标题，value为值
     * @param filePath
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static List<Map<String,String>> readCsvFile(String filePath, String charsetName) throws IOException{
        // 第一参数：读取文件的路径 第二个参数：分隔符 第三个参数：字符集
        CsvReader csvReader = new CsvReader(filePath, ',', Charset.forName(charsetName));
        // 如果你的文件没有表头，这行不用执行
        // 这行不要是为了从表头的下一行读，也就是过滤表头
        csvReader.readHeaders();
        List<Map<String,String>> returnlist = new ArrayList<>();
        // 读取每行的内容
        while (csvReader.readRecord()) {
            /**
             * 获取内容的两种方式
             * 1. 通过下标获取  System.out.print(csvReader.get(2));
             * 2. 通过表头的文字获取 System.out.println(" " + csvReader.get("稿件正文"));
             */
            Map<String,String> line = new HashMap<>();
             String[] headers = csvReader.getHeaders();
             for(String header : headers){
                 line.put(header,csvReader.get(header));
             }
             returnlist.add(line);
        }
        return returnlist;
    }

    public static List<String>  readCsvFile(String filePath) throws IOException{
        // 第一参数：读取文件的路径 第二个参数：分隔符 第三个参数：字符集
        CsvReader csvReader = new CsvReader(filePath, ',');
        // 如果你的文件没有表头，这行不用执行
        // 这行不要是为了从表头的下一行读，也就是过滤表头
        List<String> returnlist = new ArrayList<>();
        // 读取每行的内容
        while (csvReader.readRecord()) {
            /**
             * 获取内容的两种方式
             * 1. 通过下标获取  System.out.print(csvReader.get(2));
             * 2. 通过表头的文字获取 System.out.println(" " + csvReader.get("稿件正文"));
             */
            returnlist.add(csvReader.get(0));
        }
        return returnlist;
    }

    /**
     *
     * @param reader
     * @return
     * @throws IOException
     */
    public static List<String> readCsvFile(Reader reader) throws IOException{
        // 第一参数：读取文件的路径 第二个参数：分隔符 第三个参数：字符集
        CsvReader csvReader = new CsvReader(reader, ',');
        // 如果你的文件没有表头，这行不用执行
        // 这行不要是为了从表头的下一行读，也就是过滤表头
        csvReader.readHeaders();
        List<String> returnlist = new ArrayList<>();
        // 读取每行的内容
        while (csvReader.readRecord()) {
            /**
             * 获取内容的两种方式
             * 1. 通过下标获取  System.out.print(csvReader.get(2));
             * 2. 通过表头的文字获取 System.out.println(" " + csvReader.get("稿件正文"));
             */
            Map<String,String> line = new HashMap<>();
            String[] headers = csvReader.getHeaders();
            for(String header : headers){
                returnlist.add(csvReader.get(header));
            }
        }
        return returnlist;
    }


    /**
     * @param filePath 文件路径
     * @param charsetName 设置字符集
     * @return 字符串列表
     */
    public static void writeCsvCharset(String filePath, String charsetName) throws IOException{
        // 第一参数：新生成文件的路径 第二个参数：分隔符 第三个参数：字符集
        CsvWriter csvWriter = new CsvWriter(filePath, ',', Charset.forName(charsetName));

        // 表头和内容
        String[]  headers = {"姓名", "年龄", "性别"};
        String[] content = {"张三", "18", "男"};

        // 写表头和内容，因为csv文件中区分没有那么明确，所以都使用同一函数，写成功就行
        csvWriter.writeRecord(headers);
        csvWriter.writeRecord(content);

        // 关闭csvWriter
        csvWriter.close();
    }


}
