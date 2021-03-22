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
public class EmployeeEntityHeightLight {
    
    // full name
    private List<String> fullName;
    // full name
    private List<String> fullNameUnsign;
    // phone number
    private List<String> mobile;
    // employee code
    private List<String> loginName;
    // email
    private List<String> email;
    // Ngay sinh
    private List<String> birthDay;
    // chuc danh
    private List<String> position;
     // chuc danh
    private List<String> positionUnsign;
    // danh sach don vi
    private List<String> groupName;
    private List<String> groupNameUnsign;

    public List<String> getFullName() {
        return fullName;
    }

    public void setFullName(List<String> fullName) {
        this.fullName = fullName;
    }

    public List<String> getFullNameUnsign() {
        return fullNameUnsign;
    }

    public void setFullNameUnsign(List<String> fullNameUnsign) {
        this.fullNameUnsign = fullNameUnsign;
    }

    public List<String> getMobile() {
        return mobile;
    }

    public void setMobile(List<String> mobile) {
        this.mobile = mobile;
    }

    public List<String> getLoginName() {
        return loginName;
    }

    public void setLoginName(List<String> loginName) {
        this.loginName = loginName;
    }

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public List<String> getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(List<String> birthDay) {
        this.birthDay = birthDay;
    }

    public List<String> getPosition() {
        return position;
    }

    public void setPosition(List<String> position) {
        this.position = position;
    }

    public List<String> getGroupName() {
        return groupName;
    }

    public void setGroupName(List<String> groupName) {
        this.groupName = groupName;
    }

    public List<String> getGroupNameUnsign() {
        return groupNameUnsign;
    }

    public void setGroupNameUnsign(List<String> groupNameUnsign) {
        this.groupNameUnsign = groupNameUnsign;
    }

    public List<String> getPositionUnsign() {
        return positionUnsign;
    }

    public void setPositionUnsign(List<String> positionUnsign) {
        this.positionUnsign = positionUnsign;
    }
    
    
    
    
}
