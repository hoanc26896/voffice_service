package com.viettel.createchart;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author datnv5
 */
public class FunctionCommon {
    /**
     * Convert loai date ve date cua sql oracal
     * @param date
     * @return
     */
    public static java.sql.Date convertJavaDateToSqlDate(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }

    /**
     * thực hiện lấy folder chứa ảnh
     * @param strDepId
     * @param pathFolderA
     * @param fileName
     * @return
     */
    public static String getUrlRealStore(String strDepId,String pathFolderA,
            String fileName) {
        //to chuc duong dan file luu tru anh tai client
        //ThuMucGoc/nam/thang/ngay/donvi/loaibaocao/filename
       DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
       Date date = new Date();
      
       String strNowDate = dateFormat.format(date);        
       String strYear = strNowDate.substring(0,4).trim();
       String strMonth = String.valueOf(Integer.valueOf(
               strNowDate.substring(4, 6).trim()));
       String strDay = String.valueOf(Integer.valueOf(
               strNowDate.substring(6, 8).trim()));
       String pathFolder = String.format(pathFolderA + "%s/%s/%s/%s",
               strYear.trim(),strMonth.trim(),strDay.trim(),strDepId.trim());

       File dir = new File(pathFolder);
       if(dir.exists()){
           
       }else{
           dir.mkdirs();
           //dir.mkdir();
       }

        String path = String.format("%s/%s/%s/%s",strYear.trim(),strMonth.trim(),
                strDay.trim(),strDepId.trim());
        return path;
    }

    /**
     * thuc hien xoa folder chua anh theo duong dan
     * @param folder
     */
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
