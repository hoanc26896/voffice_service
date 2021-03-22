package com.viettel.voffice.threadmanager;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author datnv5
 */
public class RunnableDemo implements Runnable {
   String name = "";
    public RunnableDemo(String name){
       this.name = name;
   }
   @Override
   public void run() {
//       int idx = 1;
       for (int i = 0; i < 150; i++) {
//           System.out.println("Hello from RunnableDemo "+this.name+": " + String.valueOf(i));
       }
   }
 
}