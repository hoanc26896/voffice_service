/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.threadmanager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author datnv5
 */
public class Test {
    public static void main(String[] args) {
        RunnableDemo runnableDemo = new RunnableDemo("11111");
        ThreadPoolCommon.putRunnable(runnableDemo);
//        ThreadPoolCommon.runThreadPool();
         
        int a = 2;
        while (true) {            
            runnableDemo = new RunnableDemo("11111:" + a);
            ThreadPoolCommon.putRunnable(runnableDemo);
            ++a;
            if(a==40){
                break;
            }
            if(a%5==0){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
}
