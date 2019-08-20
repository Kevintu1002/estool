package com.kevin.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUtil {

    public static List<String> readFileContentToList(String inputFile,String split){

        List<String> contents = new ArrayList<String>();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(inputFile)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                String[] items = line.split(split);
                contents.add(items[1]);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents;

    }

    public static List<String> readFileContentToListByLine(String inputFile,String charset){

        List<String> contents = new ArrayList<String>();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(inputFile)));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, charset));// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (!StringUtil.empty(line)){

                    contents.add(line);
                }
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents;

    }

    public static List<String> readFileContentInDirToList(String inputDir,String charset){

        List<String> contents = new ArrayList<String>();
        try {
            File dir = new File(inputDir);
            for (File inputFile : dir.listFiles()){

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
                BufferedReader in = new BufferedReader(new InputStreamReader(bis, charset), 10 * 1024 * 1024);// 10M缓存
                StringBuilder article = new StringBuilder();
                while (in.ready()) {
                    String line = in.readLine();
                    article.append(line);
                }
                in.close();
                contents.add(article.toString());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents;

    }

    public static List<String> readFileContentToList(File inputFile,String charset){

        List<String> contents = new ArrayList<String>();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
            BufferedReader in = new BufferedReader(new InputStreamReader(bis, charset), 10 * 1024 * 1024);// 10M缓存
            while (in.ready()) {
                String line = in.readLine();
                if (!StringUtil.empty(line)){
                    String[] items = line.split(" ");
                    contents.add(items[1]);
                }
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents;
    }

    public static void writeContent(String outfilepath,String content){

        try {
            FileOutputStream fos = new FileOutputStream(outfilepath);
            BufferedWriter output = new BufferedWriter(
                    new OutputStreamWriter(fos, "utf-8"));
            output.write(content);
            output.close();
            fos.close();

        }catch (Exception e){

        }

    }

    public static void writeContent(String outputDir,String fileName,String content){
        System.out.println(outputDir);
        File file = new File(outputDir);
        if (!file.exists()){
            file.mkdirs();
        }
        String outfilepath = outputDir + File.separator+fileName;

        try {
            FileOutputStream fos = new FileOutputStream(outfilepath);
            BufferedWriter output = new BufferedWriter(
                    new OutputStreamWriter(fos, "utf-8"));
            output.write(content);
            output.close();
            fos.close();

        }catch (Exception e){
            e.printStackTrace();

        }

    }

    public static void main(String[] args){
    }
}
