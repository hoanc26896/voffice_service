/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search;

/**
 *
 * @author datnv5
 */
public class StrElasticConstants {
    public static final String STR_INDEXTEXT_REJECT = "/textsigndata/indexreject/";
    //tim kiem kho cong van
    public static final String STR_SEARCH_DOCUMENT ="/documentpublished/documentdata/";
    
    //tim kiem nguoi dung
    public static final String STR_SEARCH_VHREMPLOYEE ="/sysuser/vhrempindex/_search";
    //tim kiem nguoi dung
    public static final String STR_SEARCH_DOCUMENTINSTAFF ="/document/documentinstaff/_search";
    
    //tim kiem van ban theo loai va menu trong tim kiem cho mobile
    public static final String STR_SEARCH_DOCUMENTSEARCHSORT = "/documentfilter/documentdata/";
}
