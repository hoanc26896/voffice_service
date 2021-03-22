/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.threadmanager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Lop quan ly thread chung cho chuong trinh
 *
 * @author datnv5
 */
public class ThreadPoolCommon {

    static final Logger LOGGER = Logger.getLogger(ThreadPoolCommon.class);
    static volatile ThreadPoolExecutor threadPoolExecutor = null;
    static volatile BlockingQueue<Runnable> blockingQueue = null;

    public static void putRunnable(Runnable runnable) {
        if (blockingQueue == null) {
            blockingQueue = new ArrayBlockingQueue<>(20, true);
        }
        try {
            blockingQueue.offer(runnable, 2, TimeUnit.SECONDS);
        } catch (InterruptedException intEx) {
            LOGGER.error(intEx);
        }
        if (ThreadPoolCommon.threadPoolExecutor == null) {
            RunThreadCommon runThreadCommon = new RunThreadCommon();
            runThreadCommon.start();
        }
    }
}

/**
 * Thread Dung de kich hoat chay danh sach Thread
 *
 * @author datnv5
 */
class RunThreadCommon extends Thread {

    static volatile boolean isRun = false;

    @Override
    public synchronized void run() {
        if (isRun) {
            return;
        }
        if (ThreadPoolCommon.threadPoolExecutor == null) {
            int corePoolSize = 50;
            int maxPoolSize = 100;
            long keepAlive = 3000;
            TimeUnit unit = TimeUnit.SECONDS;
            BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(corePoolSize, true);
            RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
            // creating the ThreadPoolExecutor
            ThreadPoolCommon.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAlive, unit, workQueue, rejectedExecutionHandler);
            // Let start all core threads initially
            int coreThread = ThreadPoolCommon.threadPoolExecutor.prestartAllCoreThreads();
//            System.out.println("Core Thread " + coreThread);
        }
        //Dummy data for Examiner
        isRun = true;
        while (true) {
            if (ThreadPoolCommon.blockingQueue != null && ThreadPoolCommon.blockingQueue.size() > 0) {
                ThreadPoolCommon.threadPoolExecutor.execute(ThreadPoolCommon.blockingQueue.poll());
            }
            try {
                Thread.sleep(20L);
            } catch (InterruptedException ex) {
                ThreadPoolCommon.LOGGER.error(ex.getMessage(), ex);
            }
        }
        
        // shut down the pool   
        // ThreadPoolCommon.threadPoolExecutor.shutdown();
    }
}
