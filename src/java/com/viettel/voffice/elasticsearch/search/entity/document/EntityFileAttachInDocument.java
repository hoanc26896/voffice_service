/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.entity.document;

/**
 *
 * @author datnv5
 */
public class EntityFileAttachInDocument {
    Long documentId;
    Long fileAttachmentId;
    String name;
    String attachment;
    String storage;
    String strContenFile;
    String strContenFileUnsign;
    // trich yeu noi dung van ban (TITLE)
    private String briefContent;
    // trich yeu noi dung van ban (TITLE)
    private String briefContentUnsign;
    // noi dung chi dao (CONTENT)
    private String directionContent;
    // noi dung chi dao (CONTENT)
    private String directionContentUnsign;
    // ma/ hieu van ban (CODE)
    private String documentCode;
    // so dang ky (REGISTER_NUMBER)
    private String registrationNumber;
    // nguoi ky (SIGNER)
    private String signer;
    // nguoi ky (SIGNER)
    private String signerUnsign;
    private String fileSize;
    private String pages;
    // ngay ban hanh (PROMULGATE_DATE)
    private String announceDate;
    // the thuc van ban
    private String documentTypeName;
    // the thuc van ban
    private String documentTypeNameUnsign;
    private Long fileOrder;
    private Long attachId;
    private Long type;

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public String getDocumentTypeNameUnsign() {
        return documentTypeNameUnsign;
    }

    public void setDocumentTypeNameUnsign(String documentTypeNameUnsign) {
        this.documentTypeNameUnsign = documentTypeNameUnsign;
    }

    public String getAnnounceDate() {
        return announceDate;
    }

    public void setAnnounceDate(String announceDate) {
        this.announceDate = announceDate;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getStrContenFile() {
        return strContenFile;
    }

    public void setStrContenFile(String strContenFile) {
        this.strContenFile = strContenFile;
    }

    public String getStrContenFileUnsign() {
        return strContenFileUnsign;
    }

    public void setStrContenFileUnsign(String strContenFileUnsign) {
        this.strContenFileUnsign = strContenFileUnsign;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getFileAttachmentId() {
        return fileAttachmentId;
    }

    public void setFileAttachmentId(Long fileAttachmentId) {
        this.fileAttachmentId = fileAttachmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getBriefContent() {
        return briefContent;
    }

    public void setBriefContent(String briefContent) {
        this.briefContent = briefContent;
    }

    public String getBriefContentUnsign() {
        return briefContentUnsign;
    }

    public void setBriefContentUnsign(String briefContentUnsign) {
        this.briefContentUnsign = briefContentUnsign;
    }

    public String getDirectionContent() {
        return directionContent;
    }

    public void setDirectionContent(String directionContent) {
        this.directionContent = directionContent;
    }

    public String getDirectionContentUnsign() {
        return directionContentUnsign;
    }

    public void setDirectionContentUnsign(String directionContentUnsign) {
        this.directionContentUnsign = directionContentUnsign;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getSignerUnsign() {
        return signerUnsign;
    }

    public void setSignerUnsign(String signerUnsign) {
        this.signerUnsign = signerUnsign;
    }

    public Long getAttachId() {
        return attachId;
    }

    public void setAttachId(Long attachId) {
        this.attachId = attachId;
    }

    public Long getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(Long fileOrder) {
        this.fileOrder = fileOrder;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }
}
