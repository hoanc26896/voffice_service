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
public class EmployeeEntity {
    // user id 
    private String staffId;
    // full name
    private String fullName;
    // full name
    private String fullNameUnsign;
    // phone number
    private String mobile;
    // employee code
    private String loginName;
    // email
    private String email;
    // Ngay sinh
    private String birthDay;
    // gioi tinh
    private String sex;
    // chuc danh
    private String position;
    // chuc danh
    private String positionUnsign;
    // id don vi
    private String groupId;
    // path don vi
    private String groupPath;
    // danh sach don vi
    private String groupName;
    private String groupNameUnsign;
    // trang thai
    private String activate;
    
    EmployeeEntityHeightLight itemHightLight;
    // highlighting
    private List<String> highlighting;
   
    
    
    /**
     *
     */
    public EmployeeEntity() {
        this.staffId = null;
        this.fullName = null;
        this.mobile = null;
        this.loginName = null;
        this.email = null;
        this.birthDay = null;
        this.sex = null;
        this.position = null;
        this.groupId = null;
        this.groupPath = null;
        this.groupName = null;
        this.highlighting = null;
    }

    /**
     * 
     * @param staffId
     * @param fullName
     * @param mobile
     * @param loginName
     * @param email
     * @param birthDay
     * @param sex
     * @param groupId
     * @param groupPath
     * @param groupName
     * @param position 
     */
    public EmployeeEntity(String staffId, String fullName, String mobile, String loginName, String email, String birthDay, String sex, String groupId,
            String groupPath, String groupName, String position) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.mobile = mobile;
        this.loginName = loginName;
        this.email = email;
        this.birthDay = birthDay;
        this.sex = sex;
        this.groupId = groupId;
        this.groupPath = groupPath;
        this.groupName = groupName;
        this.position = position;
        this.highlighting = null;
    }

    public EmployeeEntityHeightLight getItemHightLight() {
        return itemHightLight;
    }

    public void setItemHightLight(EmployeeEntityHeightLight itemHightLight) {
        this.itemHightLight = itemHightLight;
    }

    public String getPositionUnsign() {
        return positionUnsign;
    }

    public void setPositionUnsign(String positionUnsign) {
        this.positionUnsign = positionUnsign;
    }
    
   

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullNameUnsign() {
        return fullNameUnsign;
    }

    public void setFullNameUnsign(String fullNameUnsign) {
        this.fullNameUnsign = fullNameUnsign;
    }

    public String getGroupNameUnsign() {
        return groupNameUnsign;
    }

    public void setGroupNameUnsign(String groupNameUnsign) {
        this.groupNameUnsign = groupNameUnsign;
    }
             
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getActivate() {
        return activate;
    }

    public void setActivate(String activate) {
        this.activate = activate;
    }    
    
    public List<String> getHighlighting() {
        return highlighting;
    }

    public void setHighlighting(List<String> highlighting) {
        this.highlighting = highlighting;
    }
}
