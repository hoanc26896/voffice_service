/*
 * Doi tuong chua ten cac duong trong bieu do,
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.createchart.dto;

/**
 *
 * @author datnv5
 */
public class TitleChartDTO {
    String title;//tên chú thích đường
    String code;//mã chú thích đường

    
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
}
