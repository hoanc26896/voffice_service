/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.thread;

import com.viettel.voffice.database.dao.text.TextDAO;


/**
 *
 * @author datnv5
 */
public class ThreadTextReSign implements Runnable {
   private Thread t;
   private final String threadName;
   private final Long textId;
   private final int state;
   public ThreadTextReSign(Long textId_,int state_) {
      threadName = "ThreadTextReSign" + textId_;
      textId = textId_;
      state = state_;
   }
   
   @Override
   public void run() {
      textReSign(textId,state);
   }
   
   public void start () {
      if (t == null) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
   /**
    * thuc hien update lai gia tri cua van ban bi tu choi
    * thanh trang thai duoc trinh ky lai
    * @param textId 
     * @param state_ 
    */
   public  void textReSign(Long textId,int state_) {
       TextDAO textDAO = new TextDAO();
       textDAO.updateStateTextRejectToReSign(textId,state_);
   }
}