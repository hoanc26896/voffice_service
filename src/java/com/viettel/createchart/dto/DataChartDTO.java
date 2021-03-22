/*
 * thực hiện chứa dữ liệu item cho biểu đồ
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.createchart.dto;

/**
 *
 * @author datnv5
 */
public class DataChartDTO {
    String code;//ma cua item data chart
    Double value;//gia trị của itemdata
    String columnTitle;//ten cua cac cot
    String columnCode;//ma cac cot

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColumnCode() {
        return columnCode;
    }

    public void setColumnCode(String columnCode) {
        this.columnCode = columnCode;
    }

    public String getColumnTitle() {
        return columnTitle;
    }

    public void setColumnTitle(String columnTitle) {
        this.columnTitle = columnTitle;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

}
