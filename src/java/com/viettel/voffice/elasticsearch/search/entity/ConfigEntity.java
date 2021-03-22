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
public class ConfigEntity {
    int status;
    List<String> listIpElastic;
    String userPass;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getListIpElastic() {
        return listIpElastic;
    }

    public void setListIpElastic(List<String> listIpElastic) {
        this.listIpElastic = listIpElastic;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
    
    
    
}
