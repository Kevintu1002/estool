package com.kevin.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class ExcelUtil {
    // 读文件
    public static List readExcel(String filename) {
        //1.首先读取excel
        //2.将excel每一行存储到一个list中，并将这些list放到一个大的list中
        //3.将每一个list中的值做拼接
        File file = new File(filename);
        List<List<String>> ans = null;
        try {
            if (file.getName().endsWith("xlsx")) {
                //处理ecxel2007
                XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
                ans = readExcel(wb, 1, 1);
            } else {
                //处理ecxel2003
                HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
                ans = readExcel(wb, 1, 1);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return ans;
    }

    /**
     * 读取Excel
     *生成List<String>>
     * @param wb
     * @param rowNum
     * @param cellNum
     * @return
     * @throws Exception
     */
    public static List readExcel(Workbook wb, Integer rowNum, Integer cellNum) throws Exception {
        ArrayList<ArrayList<String>> ans = new ArrayList<ArrayList<String>>();
        for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
            Sheet sheet = wb.getSheetAt(numSheet);
            if (sheet == null) {
                continue;
            }
            // 对于每个sheet，读取其中的每一行
            for (int rn = rowNum; rn <= sheet.getLastRowNum(); rn++) {
                Row row = sheet.getRow(rn);
                if (row == null) continue;
                ArrayList<String> curarr = new ArrayList<String>();
                for (int cn = cellNum; cn < row.getLastCellNum(); cn++) {
                    Cell cell = row.getCell(cn);
                    if(StringUtil.empty(cell + "")){
                        continue;
                    }
                    curarr.add(cell + "");
                }
                if(curarr.size() > 0){
                    ans.add(curarr);
                }
            }
        }
        return ans;
    }
}
