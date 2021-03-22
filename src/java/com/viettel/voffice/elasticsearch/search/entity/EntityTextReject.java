/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search.entity;

import java.util.List;

/**
 *
 * @author datnv5
 */
public class EntityTextReject {

    Long textId;
    Long creatorId;
    List<Long> listUserSign;
    Long userReject;
    String dateCreate;
    String dateReject;
    String title;
    String conten;
    String contenFileSign;
    String contenFileSignUnsign;
    String strSumConten;
    Long priorityId; 
    String typeId;
    Long stypeId;
    String areaId;
    String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    

    public Long getTextId() {
        return textId;
    }

    public void setTextId(Long textId) {
        this.textId = textId;
    }
 
    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    

    public List<Long> getListUserSign() {
        return listUserSign;
    }

    public void setListUserSign(List<Long> listUserSign) {
        this.listUserSign = listUserSign;
    }

    public Long getUserReject() {
        return userReject;
    }

    public void setUserReject(Long userReject) {
        this.userReject = userReject;
    }

    public String getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(String dateCreate) {
        this.dateCreate = dateCreate;
    }

    public String getDateReject() {
        return dateReject;
    }

    public void setDateReject(String dateReject) {
        this.dateReject = dateReject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConten() {
        return conten;
    }

    public void setConten(String conten) {
        this.conten = conten;
    }

    public String getContenFileSign() {
        return contenFileSign;
    }

    public void setContenFileSign(String contenFileSign) {
        this.contenFileSign = contenFileSign;
    }

    public String getContenFileSignUnsign() {
        return contenFileSignUnsign;
    }

    public void setContenFileSignUnsign(String contenFileSignUnsign) {
        this.contenFileSignUnsign = contenFileSignUnsign;
    }

    public String getStrSumConten() {
        return strSumConten;
    }

    public void setStrSumConten(String strSumConten) {
        this.strSumConten = strSumConten;
    }

    public Long getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Long priorityId) {
        this.priorityId = priorityId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public void setStypeId(Long stypeId) {
        this.stypeId = stypeId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

}
