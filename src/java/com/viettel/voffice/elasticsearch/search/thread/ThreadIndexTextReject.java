/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.thread;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.elasticsearch.search.ElasticCommon;
import com.viettel.voffice.elasticsearch.search.ElasticTextControllerIndexData;
import com.viettel.voffice.elasticsearch.search.StrElasticConstants;
import com.viettel.voffice.elasticsearch.search.entity.ConfigEntity;
import java.util.List;
import org.apache.log4j.Logger;


/**
 *
 * @author datnv5
 */
public class ThreadIndexTextReject implements Runnable {
    private static Logger log = Logger.getLogger(ThreadIndexTextReject.class);
    private Thread t;
   private String threadName;
   private Long textId;
   private boolean isrun = false;
   public ThreadIndexTextReject(Long textId_) {
      threadName = "TextIndex" + textId_;
      textId = textId_;
   }
   
   @Override
   public void run() {
      indexTextWhenRejectSign(textId);
   }
   
   public void start () {
      if (t == null && !isrun) {
         t = new Thread (this, threadName);
         t.start ();
      }
   }
   
   public  void indexTextWhenRejectSign(Long textId) {
        if(isrun) return;
        isrun = true;
        String strDataIndex = ElasticTextControllerIndexData.getDetailTextReject(textId);
        
        String result;
        ConfigEntity itemConfig = ElasticCommon.getConfigSearchElastic();
        if(itemConfig!=null && itemConfig.getStatus()==1){
            // cau hinh theo tim kiem moi
            List<String> listUrl = itemConfig.getListIpElastic();
            for (String ipElastic : listUrl) {
                //neu khong cau hinh thi tim kiem theo elasticu
                String strServerIndexDocument 
                    = "http://"+ ipElastic 
                    + StrElasticConstants.STR_INDEXTEXT_REJECT
                    + textId;
                result = ConnectServer.sendRequestToServer(strServerIndexDocument,
                        strDataIndex,itemConfig.getUserPass());
                if(result != null && result.trim().length() > 0){
                    break;
                }
            }
        }else{
            //neu khong cau hinh thi tim kiem theo elasticu
            String strServerIndexDocument 
                = FunctionCommon.getPropertiesValue(
                        "url.elasticsearch.server")
                + StrElasticConstants.STR_INDEXTEXT_REJECT
                + textId;
            ConnectServer.sendRequestToServer(strServerIndexDocument, strDataIndex,null);
        }
        isrun = false;
    }
}