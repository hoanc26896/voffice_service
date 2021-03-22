/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.log4j.Logger;

import com.viettel.voffice.thread.ThreadExcuteAfterSigned;

/**
 *
 * @author datnv5
 */
public class ThreadWriteLogsNewFile implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ThreadWriteLogsNewFile.class);
    
    private Thread t;
    private  String threadName;
    private  String dataWrite;


    ThreadWriteLogsNewFile(String data) {
        threadName = FunctionCommon.getNumberAndDotFromString(FunctionCommon.dateShow(new Date(),false)+FunctionCommon.IPPORTSERVICE);
        dataWrite =  data;
    }

    @Override
    public void run() {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            //doc duong dan file luu tru log
            String strFolder = FunctionCommon.getPropertiesValue("logs.file.newfileupload");
            Path path = Paths.get(strFolder);
            if(!Files.exists(path)){
                Files.createDirectories(path);
            }
            String pathFull = strFolder + "/" + FunctionCommon.getNumberAndDotFromString(
                    FunctionCommon.dateShow(new Date(),false)+ FunctionCommon.IPPORTSERVICE) + ".txt";
            File file = new File(pathFull);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.newLine();
            bw.write(dataWrite);
        } catch (IOException e) {
//            System.out.println(e);
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
//                System.out.println(ex);
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
