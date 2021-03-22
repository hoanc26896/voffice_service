package com.viettel.voffice.elasticsearch.search.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntityDocumentDetail {

    private Long id;
    private Long state;
    private Long typeId;
    private String documentTypeName;
    private String briefContent;
    private String directionContent;
    private String executionRequirement;
    private String announceDate;
    private String documentCode;
    private String registrationNumber;
    private String signer;
    private String sender;
    private Date promulgateDate;
    private SimpleDateFormat simpleDateFormat;

    public Long getTypeId() {
        return this.typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getDocumentTypeName() {
        return this.documentTypeName;
    }

    public void setDocumentTypeName(String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public String getAnnounceDate() {
        return this.announceDate;
    }

    public void setAnnounceDate(String announceDate) {
        this.announceDate = announceDate;
    }

    public String getBriefContent() {
        return this.briefContent;
    }

    public void setBriefContent(String briefContent) {
        this.briefContent = briefContent;
    }

    public String getDirectionContent() {
        return this.directionContent;
    }

    public void setDirectionContent(String directionContent) {
        this.directionContent = directionContent;
    }

    public String getDocumentCode() {
        return this.documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getExecutionRequirement() {
        return this.executionRequirement;
    }

    public void setExecutionRequirement(String executionRequirement) {
        this.executionRequirement = executionRequirement;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getState() {
        return this.state;
    }

    public void setState(Long state) {
        this.state = state;
    }

    public String getRegistrationNumber() {
        return this.registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSigner() {
        return this.signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public Date getPromulgateDate() {

        if ((this.announceDate == null) || (this.announceDate.trim().length() == 0)) {
            return this.promulgateDate;
        }
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        }
        try {
            this.promulgateDate = simpleDateFormat.parse(this.announceDate);
        } catch (ParseException localParseException) {
        }
        return this.promulgateDate;
    }

    public void setPromulgateDate(Date promulgateDate) {
        this.promulgateDate = promulgateDate;
    }
}
