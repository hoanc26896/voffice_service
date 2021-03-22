/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.entity.document;

/**
 *
 * @author tond
 */
public class UserEntityViewDocumentTypeSelect {
    private Long userId1;
    private Long userId2;
    String documentInStaffId;
    String processTime;
    String isProcessing;
    String isRead;
    String isMark;
    String staffId;
    String senderId2;
    String processTimeAndIsProcessing;
    String user12processTimeAndIsProcessing;
    String user12isProcessing;
    String user12isRead;
    String user12isMark;

    /**
     * 
     * @param userId1
     * @param userId2 
     * @param staffId 
     * @param senderId2 
     * @param documentInStaffId 
     * @param processTime 
     * @param isProcessing 
     * @param isRead 
     * @param isMark 
     */
    public UserEntityViewDocumentTypeSelect(Long userId1, Long userId2,String staffId, 
            String senderId2,String documentInStaffId,
            String processTime, String isProcessing,String isRead,String isMark) {
        this.userId1 = (userId1== null)? -158111L:userId1;
        this.userId2 = (userId2== null)? -158111L:userId2;
        this.documentInStaffId = (documentInStaffId == null)? "-158111":documentInStaffId;
        this.processTime = (processTime == null)? "-158111":processTime;
        this.isProcessing= (isProcessing == null)? "-158111":isProcessing;
        this.isRead = (isRead == null)? "-158111":isRead;
        this.isMark = (isMark  == null)? "-158111":isMark;
        this.processTimeAndIsProcessing = this.processTime + "AANNDD" + this.isProcessing;
        String strUserJoin = "USERONE" +this.userId1+ "USERTWO" + this.userId2 +"DATA";
        this.user12processTimeAndIsProcessing = strUserJoin + this.processTimeAndIsProcessing;
        this.user12isProcessing =strUserJoin +this.isProcessing;
        this.user12isRead= strUserJoin + this.isRead;
        this.user12isMark= strUserJoin + this.isMark;
        this.staffId = staffId;
        this.senderId2 = senderId2;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getSenderId2() {
        return senderId2;
    }

    public void setSenderId2(String senderId2) {
        this.senderId2 = senderId2;
    }

    
    
    public String getProcessTime() {
        return processTime;
    }

    public void setProcessTime(String processTime) {
        this.processTime = processTime;
    }

    public String getIsProcessing() {
        return isProcessing;
    }

    public void setIsProcessing(String isProcessing) {
        this.isProcessing = isProcessing;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public String getIsMark() {
        return isMark;
    }

    public void setIsMark(String isMark) {
        this.isMark = isMark;
    }

    
    
    public String getDocumentInStaffId() {
        return documentInStaffId;
    }

    public void setDocumentInStaffId(String documentInStaffId) {
        this.documentInStaffId = documentInStaffId;
    }

   
    
    public Long getUserId1() {
        return userId1;
    }

    public void setUserId1(Long userId1) {
        this.userId1 = userId1;
    }

    public Long getUserId2() {
        return userId2;
    }

    public void setUserId2(Long userId2) {
        this.userId2 = userId2;
    }
    
    
}
