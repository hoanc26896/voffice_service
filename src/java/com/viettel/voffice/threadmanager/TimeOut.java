/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.threadmanager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * TimeOut class - used for stopping a thread that is taking too long
 *
 * @author Peter Goransson
 *
 */
public class TimeOut {
    public static void main(String[] args) throws Exception {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Future future = executor.submit(new Task());
//
//        try {
//            System.out.println("Started..");
//            System.out.println(future.get(3, TimeUnit.SECONDS));
//            System.out.println("Finished!");
//        } catch (TimeoutException e) {
//            future.cancel(true);
//            System.out.println("Terminated!");
//            System.exit(0);
//        }
//        executor.shutdownNow();
    }
}

class Task implements Runnable {
   

    @Override
    public void run() {
          for (int i = 0; i < 321321321; i++) {
//            System.out.println("fdafda"+i);
        }
    }
}