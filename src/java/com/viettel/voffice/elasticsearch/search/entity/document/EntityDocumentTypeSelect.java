/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.entity.document;


import com.viettel.voffice.constants.FunctionCommon;
import java.util.List;

/**
 *
 * @author thanght6
 */
public class EntityDocumentTypeSelect {

    // Id van ban
    private  Long documentId;
    // Ma hieu van ban
    private String code;
    // Tieu de
    private String title;
    private String titleUnsign;
    
    private  String status;
     // Ngay ban hanh
    private String promulgateDate;
    // Nguoi ky
    private String signer;
    private String signerUnsign;
    // Cong van den hay di
    private   String isArrive;
     // Id do khan
    private  String priorityId;
      // Id hinh thuc van ban
    private  String typeId;
    // Id do mat
    private  String stypeId;
    // Id linh vuc
    private  String areaId;
    private   String isTransferedMoney;
    private   String signedPaper;
    // Trang thai doc van ban = confirm time
    private   String isRead = "-158111";
    // Thoi gian xu ly
    private String processTime = "-158111";
    private   String isProcessing = "-158111";
    // Ngay den
    private String receiveDate;
    private String isMark = "-158111";
    // Id nguoi chuyen cong van neu co
    private  String staffId;
    // Id nguoi gui tren he thong 2
    private  String senderId2;
    // Ten nguoi gui
    private String senderName;
    private String senderNameUnsign;
    //Email nguoi gui
    private String senderEmail;
    // Ten do khan
    private String priority;
    // Ten hinh thuc van ban
    private String type;
    // Ten do mat
    private String stype;
     // Ten linh vuc
    private String area;
    // Deadline can ban hanh lai
    private String deadLine;
    private   String sendType;
    //cuongnv::Loai van ban
    private     String documentType;
    private     String documentInStaffId;
    private     String commentContent;
    private     String documentCommentId;
//     Id nguoi nhan cong van neu co
    private  Long receiverId  = -158111L;
    private  Long receiverId2 = -158111L;
    List<UserEntityViewDocumentTypeSelect> listUserView;
    List<EntityFileAttachInDocument> listFileAttach;
    
    public static EntityDocumentTypeSelect setEntityDocument(EntityDocumentTypeSelect itemDocument){
        EntityDocumentTypeSelect itemResult  = itemDocument;
        if(itemResult.getIsRead() == null) itemResult.setIsRead("-158111");
        if(itemResult.getProcessTime()== null) itemResult.setProcessTime("-158111");
        if(itemResult.getIsProcessing()== null) itemResult.setIsProcessing("-158111");
        if(itemResult.getIsMark()== null) itemResult.setIsMark("-158111");
        if(itemResult.getReceiverId()== null) itemResult.setReceiverId(-158111L);
        if(itemResult.getReceiverId2()== null) itemResult.setReceiverId2(-158111L);
        itemResult.setTitleUnsign(FunctionCommon.removeAccent(itemResult.getTitle()));
        itemResult.setSignerUnsign(FunctionCommon.removeAccent(itemResult.getSigner()));
        itemResult.setSenderNameUnsign(FunctionCommon.removeAccent(itemResult.getSenderName()));
        return itemResult;
    }

    public String getTitleUnsign() {
        return titleUnsign;
    }

    public void setTitleUnsign(String titleUnsign) {
        this.titleUnsign = titleUnsign;
    }

    public String getSignerUnsign() {
        return signerUnsign;
    }

    public void setSignerUnsign(String signerUnsign) {
        this.signerUnsign = signerUnsign;
    }

    public String getSenderNameUnsign() {
        return senderNameUnsign;
    }

    public void setSenderNameUnsign(String senderNameUnsign) {
        this.senderNameUnsign = senderNameUnsign;
    }
    
    
    
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

   

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public  String getStatus() {
        return status;
    }

    public void setStatus( String status) {
        this.status = status;
    }

    public String getPromulgateDate() {
        return promulgateDate;
    }

    public void setPromulgateDate(String promulgateDate) {
        this.promulgateDate = promulgateDate;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public   String getIsArrive() {
        return isArrive;
    }

    public void setIsArrive(  String isArrive) {
        this.isArrive = isArrive;
    }

    public  String getPriorityId() {
        return priorityId;
    }

    public void setPriorityId( String priorityId) {
        this.priorityId = priorityId;
    }

    public  String getTypeId() {
        return typeId;
    }

    public void setTypeId( String typeId) {
        this.typeId = typeId;
    }

    public  String getStypeId() {
        return stypeId;
    }

    public void setStypeId( String stypeId) {
        this.stypeId = stypeId;
    }

    public  String getAreaId() {
        return areaId;
    }

    public void setAreaId( String areaId) {
        this.areaId = areaId;
    }

    public   String getIsTransferedMoney() {
        return isTransferedMoney;
    }

    public void setIsTransferedMoney(  String isTransferedMoney) {
        this.isTransferedMoney = isTransferedMoney;
    }

    public   String getSignedPaper() {
        return signedPaper;
    }

    public void setSignedPaper(  String signedPaper) {
        this.signedPaper = signedPaper;
    }

    public   String getIsRead() {
        return isRead;
    }

    public void setIsRead(  String isRead) {
        this.isRead = isRead;
    }

    public String getProcessTime() {
        return processTime;
    }

    public void setProcessTime(String processTime) {
        this.processTime = processTime;
    }

    public   String getIsProcessing() {
        return isProcessing;
    }

    public void setIsProcessing(  String isProcessing) {
        this.isProcessing = isProcessing;
    }

    public String getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public   String getIsMark() {
        return isMark;
    }

    public void setIsMark(  String isMark) {
        this.isMark = isMark;
    }

    public  String getStaffId() {
        return staffId;
    }

    public void setStaffId( String staffId) {
        this.staffId = staffId;
    }

    public  String getSenderId2() {
        return senderId2;
    }

    public void setSenderId2( String senderId2) {
        this.senderId2 = senderId2;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(String deadLine) {
        this.deadLine = deadLine;
    }

    public   String getSendType() {
        return sendType;
    }

    public void setSendType(  String sendType) {
        this.sendType = sendType;
    }

    public   String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(  String documentType) {
        this.documentType = documentType;
    }

    public  String getDocumentInStaffId() {
        return documentInStaffId;
    }

    public void setDocumentInStaffId( String documentInStaffId) {
        this.documentInStaffId = documentInStaffId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public  String getDocumentCommentId() {
        return documentCommentId;
    }

    public void setDocumentCommentId( String documentCommentId) {
        this.documentCommentId = documentCommentId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getReceiverId2() {
        return receiverId2;
    }

    public void setReceiverId2(Long receiverId2) {
        this.receiverId2 = receiverId2;
    }

    public List<UserEntityViewDocumentTypeSelect> getListUserView() {
        return listUserView;
    }

    public void setListUserView(List<UserEntityViewDocumentTypeSelect> listUserView) {
        this.listUserView = listUserView;
    }

    public List<EntityFileAttachInDocument> getListFileAttach() {
        return listFileAttach;
    }

    public void setListFileAttach(List<EntityFileAttachInDocument> listFileAttach) {
        this.listFileAttach = listFileAttach;
    }
}
