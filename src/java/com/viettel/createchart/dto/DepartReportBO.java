/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.createchart.dto;

/**
 *
 * @author datnv5
 */
public class DepartReportBO {
    String intId = "";
    String parentId = "";
    String strName = "";
    String strCodeName = "";
    String code = "";
    String inchart = "";

    public String getStrCodeName() {
        return strCodeName;
    }

    public void setStrCodeName(String strCodeName) {
        this.strCodeName = strCodeName;
    }

    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInchart() {
        return inchart;
    }

    public void setInchart(String inchart) {
        this.inchart = inchart;
    }

    public String getIntId() {
        return intId;
    }

    public void setIntId(String intId) {
        this.intId = intId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }
}
