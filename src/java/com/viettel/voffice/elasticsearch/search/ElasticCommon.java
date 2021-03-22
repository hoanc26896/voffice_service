/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.ConfigParameterDAO;
import com.viettel.voffice.elasticsearch.search.entity.ConfigEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author datnv5
 */
public class ElasticCommon {
    private static final Logger LOGGER = Logger.getLogger(ElasticCommon.class);
    /**
     * Genarate string json search document
     * @param listOrgPathAll
     * @param keyword
     * @param startRecord
     * @param pageSize
     * @return 
     */
    public static synchronized String  jsonDocumentSearchGenarate(
            List<String> listOrgPathAll,String keyword, Long startRecord, Long pageSize){
        String jsonStringResult = "";
        try {
            if(keyword!=null && keyword.trim().length()>0){            
                keyword = keyword.replace("\"", "\\\"").replace("“", "\\\"").replace("”", "\\\"");
            }
            StringBuilder sql = new StringBuilder();
            sql.append("{\"_source\": [\"listPathOrg\", \"documentId\", \"documentType\", \"briefContent\"");
            sql.append(",\"sender\",\"status\",\"promulgationDate\",\"expirationDate\"");
            sql.append(",\"directionContent\", \"announceDate\", \"documentCode\", \"registrationNumber\", \"signer\"],");
            sql.append("\"highlight\": {\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"],\"fields\": {\"*\": {}}},");
            sql.append("\"from\": \"%d\",\"size\": \"%d\",\"query\": {");
            sql.append("\"bool\": {");
            
            sql.append("\"must\": [{\"bool\": {\"should\": [");
            //lap tim kiem cho danh sach don vi
            boolean first = true;
            for (String string : listOrgPathAll) {
                String strQuery;
                if(first){
                     strQuery ="{\"match_phrase\": {\"listPathOrg\": \"%s\"}}";
                     sql.append(String.format(strQuery, string));
                }else{
                     strQuery =",{\"match_phrase\": {\"listPathOrg\": \"%s\"}}";
                     sql.append(String.format(strQuery, string));
                }
                first = false;
            }
            sql.append("],\"minimum_should_match\": \"1\"}}]");
            
            sql.append(",\"should\": [");
            //tim kiem chinh  xac
            sql.append("{\"match_phrase\": {\"signer\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"signerUnsign\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"sender\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"senderUnsign\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"briefContent\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"briefContentUnsign\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"documentCode\": \"%s\"}}");
            sql.append(",{\"match_phrase\": {\"registrationNumber\": \"%s\"}}");
            //tim kiem co dau
//            sql.append(",{\"match\": {\"briefContent\": \"%s\"}}");
//            sql.append(",{\"match\": {\"directionContent\": \"%s\"}}");
//            sql.append(",{\"match\": {\"documentCode\": \"%s\"}}");
//            sql.append(",{\"match\": {\"registrationNumber\": \"%s\"}}");
//            sql.append(",{\"match\": {\"signer\": \"%s\"}}");
//            sql.append(",{\"match\": {\"sender\": \"%s\"}}");
            //tim kiem khong dau
//            sql.append(",{\"match\": {\"briefContentUnsign\": \"%s\"}}");
//            sql.append(",{\"match\": {\"directionContentUnsign\": \"%s\"}}");
//            sql.append(",{\"match\": {\"documentCodeUnsign\": \"%s\"}}");
//            sql.append(",{\"match\": {\"signerUnsign\": \"%s\"}}");
//            sql.append(",{\"match\": {\"senderUnsign\": \"%s\"}}");
            //tim kiem bang multi query
             sql.append(",{\"multi_match\": {\"query\": \"%s\",");
             sql.append("\"type\": \"most_fields\",");
             sql.append("\"fields\": [\"signer\",\"signerUnsign\",");
             sql.append("\"sender\",\"senderUnsign\",\"briefContent\",");
             sql.append("\"briefContentUnsign\",\"directionContentUnsign\",\"documentCodeUnsign\",");
             sql.append("\"directionContent\",\"documentCode\",\"registrationNumber\",\"signer\"]}}");


            //tim kiem cho file dinh kem
            sql.append(",{\"nested\": {\"path\": \"listFileInfo\",\"query\": ");
            sql.append("{\"bool\": {\"must\": [{\"simple_query_string\": {\"query\": \"%s\",");
            sql.append("\"fields\": [\"listFileInfo.contenFile\", \"listFileInfo.contenFileUnsign\"],");
            sql.append("\"default_operator\": \"or\"}}]}},\"inner_hits\": {\"_source\": ");
            sql.append("{\"includes\": [\"listFileInfo.fileId\",\"listFileInfo.filePath\",");
            sql.append("\"listFileInfo.fileName\",\"listFileInfo.storage\",\"listFileInfo.pages\",\"listFileInfo.fileSize\"]}");
            sql.append(",\"highlight\": {\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"],\"fields\": {");
            sql.append("\"listFileInfo.contenFile\": {},\"listFileInfo.contenFileUnsign\": {}");
            sql.append("}}}}}");
            sql.append("],\"minimum_should_match\": \"1\"");
            sql.append("}},\"min_score\": 15}");
            String str = String.format(sql.toString(), startRecord, pageSize
                    ,keyword,keyword,keyword,keyword,keyword
                    ,keyword,keyword,keyword,keyword,keyword);
            JSONObject json = new JSONObject(str);
            jsonStringResult = json.toString();
        } catch (JSONException ex) {
            LOGGER.error("jsonDocumentSearchGenarate", ex);
        }
        return jsonStringResult;
    }
    
    /**
     * Tim kiem nguoi dung trong he thong
     * @param keyword
     * @param listPathGroup
     * @param startRecord
     * @param pageSize
     * @return 
     */
    public static synchronized String  jsonVhrEmployeeSearchGenarate(
            String keyword,List<String> listPathGroup , Long startRecord, Long pageSize){
        String jsonStringResult = "";
        if(keyword == null || keyword.trim().length()<=0){
            //lay tat ca danh sach nhan vien
            StringBuilder strQueryNone = new StringBuilder();
            strQueryNone.append("{\"from\":\"%d\",\"size\":\"%d\",\"sort\": [");
            strQueryNone.append("{\"emp_level\": {\"order\": \"asc\"}}],");
            strQueryNone.append("\"query\": {\"bool\": {\"must\" : [");
            strQueryNone.append("{ \"match\": {\"activate\" : 1 } }");
            //ghep dieu kien tim kiem don vi
            if(listPathGroup!=null && listPathGroup.size()>0){
                strQueryNone.append(",{\"bool\":{\"should\": [");
                boolean first = true;
                for (String strItemPathGroup : listPathGroup) {
                    if(!first){
                        strQueryNone.append(",");
                    }
                    String strPathOrg = "{\"match_phrase\": {\"groupPath\": \"%s\"}}";
                    strPathOrg = String.format(strPathOrg, strItemPathGroup);
                    strQueryNone.append(strPathOrg);
                    first = false;
                }
                strQueryNone.append("],\"minimum_should_match\": \"1\"}}");
            }
            strQueryNone.append("]}}}");
            return  String.format(strQueryNone.toString(),startRecord,pageSize);
        }
        try {
            if(keyword.trim().length()>0){            
                keyword = keyword.replace("“", "\\\"").replace("”", "\\\"");
                keyword = keyword.replace("@", "AAAAAAAAAAACONG");
            }
            
            String arrKeySearch[] = keyword.replace("\"", "")
                    .replaceAll("\\s{2,}"," ").split(" ");
            StringBuilder strQuery = new StringBuilder();
            if(arrKeySearch.length<=0){
                String queryEmty = "{\"from\":\"%d\",\"size\":\"%d\",\"sort\": [{\"_score\": ";
                queryEmty += "{\"order\": \"desc\"}},{\"lastNameUnsign.raw\": {\"order\": \"asc\"}}]}";
                strQuery.append(String.format(queryEmty,startRecord,pageSize));
            }else{
                strQuery.append("{\"query\": {\"bool\": {\"should\": [");
                //gan dieu kien tim kiem danh ba
                String strwhere;
                    strwhere = "{\"match_phrase\": { \"lastName\": \"%s\" }}";
                    strwhere += ",{ \"match_phrase\": { \"lastNameUnsign\": \"%s\" }}";
                strQuery.append(String.format(strwhere,keyword.replace("\"", "")
                        ,FunctionCommon.removeAccent(keyword).replace("\"", "")));
                
                    strwhere = ",{\"match_phrase\": { \"fullName\": \"%s\" }}";
                    strwhere += ",{ \"match_phrase\": { \"fullNameUnsign\": \"%s\" }}";
                strQuery.append(String.format(strwhere,keyword.replace("\"", "")
                        ,FunctionCommon.removeAccent(keyword).replace("\"", "")));
                
                //thuc hien tach cac cum tu tim kiem trong dau double quotes
                
                List<String> listPhrase = getPhraseStringSearch(keyword);
                if(listPhrase != null && listPhrase.size() >0){
                    for (int i = 0; i < listPhrase.size(); i++) {
                        String strSearchPhrase = listPhrase.get(i);
                        //tim kiem chinh xac theo cum tu
                        strwhere = ",{ \"match_phrase\": { \"fullName\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"mobile\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"email\": \"%sAAAAAAAAAAACONGviettel.com.vn\"}}";
                        strwhere += ",{ \"match_phrase\": { \"email\": \"%sAAAAAAAAAAACONGviettelpost.com.vn\"}}";
                        strwhere += ",{ \"match_phrase\": { \"position\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"birthDay\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"groupName\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"fullNameUnsign\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"positionUnsign\": \"%s\" }}";
                        strwhere += ",{ \"match_phrase\": { \"groupNameUnsign\": \"%s\" }}";
                        String email = strSearchPhrase.replace("AAAAAAAAAAACONGviettel.com.vn", "").replace("sAAAAAAAAAAACONGviettelpost.com.vn", "");
                        strQuery.append(String.format(strwhere,strSearchPhrase
                                ,strSearchPhrase,email,email
                                ,strSearchPhrase,strSearchPhrase,strSearchPhrase
                                ,FunctionCommon.removeAccent(strSearchPhrase)
                                ,FunctionCommon.removeAccent(strSearchPhrase)
                                ,FunctionCommon.removeAccent(strSearchPhrase)));
                        
                        keyword = keyword.replace(strSearchPhrase, "");
                    }
                    keyword = keyword.replace("\"", "");
                    arrKeySearch = keyword.replace("\"", "")
                            .replaceAll("\\s{2,}"," ").split(" ");
                }
                
                //1. uu tien tim kiem dung
                for (String string : arrKeySearch) {
                    if(string!=null && string.trim().length() >0 ){
                        String email = string.replace("AAAAAAAAAAACONGviettel.com.vn", "")
                                .replace("sAAAAAAAAAAACONGviettelpost.com.vn", "");
                        strwhere = ",{\"multi_match\": {\"query\": \"*%sAAAAAAAAAAACONG*\","
                                + "\"type\": \"phrase_prefix\",\"fields\": "
                                + "[\"email\"]}}";
                        strwhere += ",{\"multi_match\": {\"query\": \"%s\","
                                + "\"type\": \"phrase_prefix\",\"fields\": "
                                + "[\"fullName\",\"fullNameUnsign\"]}}";
                        //if(!first)strQuery.append(",");
                        char lastString = string.charAt(string.length()-1);
                        if(lastString == '*'){
                            string = string.substring(0, string.length()-1);
                        }
                        strQuery.append(String.format(strwhere,email,string));
                    }
                    //first = false;
                }
                //2.uu tien tim kiem theo mobile,email,loginName
                for (String string : arrKeySearch) {
                    if(string!=null && string.trim().length() >0 ){
                        String email = string.replace("AAAAAAAAAAACONGviettel.com.vn", "")
                                .replace("sAAAAAAAAAAACONGviettelpost.com.vn", "");
                        if(email.trim().length()>0){
                            strwhere = ",{\"query_string\": {\"query\": \"%s*\","
                                    + "\"fields\": [\"mobile\",\"email^4\",\"loginName^3\"],"
                                    + "\"default_operator\": \"or\"}}";
                            char lastString = string.charAt(string.length()-1);
                            if(lastString == '*'){
                                string = string.substring(0, string.length()-1);
                            }
                            strQuery.append(String.format(strwhere,email));
                        }
                    }
                }
                //2.uu tien tim kiem theo mobile,email,loginName
                for (String string : arrKeySearch) {
                    if(string!=null && string.trim().length() >0 ){
                        String email = string.replace("AAAAAAAAAAACONGviettel.com.vn", "").replace("sAAAAAAAAAAACONGviettelpost.com.vn", "");
                        if(email.trim().length()>0){
                            strwhere = ",{\"query_string\": {\"query\": \"*%s*\","
                                    + "\"fields\": [\"mobile\",\"email^3\",\"loginName^4\"],"
                                    + "\"default_operator\": \"or\"}}";
                            char lastString = string.charAt(string.length()-1);
                            if(lastString == '*'){
                                string = string.substring(0, string.length()-1);
                            }
                            strQuery.append(String.format(strwhere,email));
                        }
                    }
                }
                //3.cac dieu kien tim kiem query khong uu tien
                if(keyword.trim().length()>0){
                    keyword = keyword.replace("\"", "");
                    strwhere = ",{\"multi_match\": {\"query\": \"%s\","
                            + "\"type\": \"cross_fields\",\"operator\": \"and\",\"fields\": "
                            + "[\"fullName^4\", \"position\","
                            + "\"birthDay\", \"groupName\"]}}";
                    strQuery.append(String.format(strwhere,keyword));
                    //
                    strwhere = ",{\"multi_match\": {\"query\": \"%s\","
                            + "\"type\": \"cross_fields\",\"operator\": \"and\",\"fields\": [\"fullNameUnsign^4\", "
                            + "\"positionUnsign\",\"birthDay\", \"groupNameUnsign\"]}}";
                    keyword = FunctionCommon.removeUnsign(keyword);
                    strQuery.append(String.format(strwhere,keyword));
                }
                
                strQuery.append("],\"minimum_should_match\": \"1\",");
                strQuery.append("\"must\": [");
                strQuery.append("{\"match_phrase\": {\"activate\": \"1\" }}");
                
                //ghep dieu kien tim kiem don vi
                if(listPathGroup!=null&&listPathGroup.size()>0){
                    strQuery.append(",{\"bool\":{\"should\": [");
                    boolean first = true;
                    for (String strItemPathGroup : listPathGroup) {
                        if(!first){
                            strQuery.append(",");
                        }
                        String strPathOrg = "{\"match_phrase\": {\"groupPath\": \"%s\"}}";
                        strPathOrg = String.format(strPathOrg, strItemPathGroup);
                        strQuery.append(strPathOrg);
                        first = false;
                    }
                    strQuery.append("],\"minimum_should_match\": \"1\"}}");
                }
                //end tim kiem theo don vi
                
                strQuery.append("]}}");
                if(keyword.trim().length() <=0){
                    strQuery.append(",\"sort\": [{\"lastNameUnsign.raw\": {\"order\": \"asc\"}}],");
                }else{
                    strQuery.append(",\"sort\": [{\"_score\": {\"order\": \"desc\"}}");
                    strQuery.append(",{\"lastNameUnsign.raw\": {\"order\": \"asc\"}}],"); 
                }
                
                strQuery.append("\"highlight\": {\"pre_tags\": [\"<b>\"],");
                strQuery.append("\"post_tags\": [\"</b>\"],\"fields\": {\"*\": {}}},");
                strQuery.append("\"from\": \"%d\",\"size\": \"%d\"}");
            }
            String str = String.format(strQuery.toString(),startRecord,pageSize);
            JSONObject json = new JSONObject(str);
            jsonStringResult = json.toString();
        } catch (JSONException ex) {
            LOGGER.error("jsonDocumentSearchGenarate", ex);
        }
        return jsonStringResult;
    }
    /**
     * tach thanh cac tu khoa tim kiem chinh xac theo cum tu giua double quotes
     * @param keyword
     * @return 
     */
    private static List<String> getPhraseStringSearch(String keyword){
        List<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile("\\\"(.*?)\\\"");
        Matcher regexMatcher = regex.matcher(keyword);

        while (regexMatcher.find()) {
           matchList.add(regexMatcher.group(1));
        }
        return matchList;
    }
    public static void main(String[] args) {
        ConfigEntity aa = getConfigSearchElastic();
//        System.out.println(aa);
    }
    
    
    public static ConfigEntity getConfigSearchElastic(){
        ConfigParameterDAO configParameterDAO = new ConfigParameterDAO();
        String strConfig = configParameterDAO.getValueFromConfigDataBase("ELASTICSEARCH2");
        Object result =  FunctionCommon.convertJsonToObject(strConfig, ConfigEntity.class);
        if(result != null){
            return (ConfigEntity) result;
        }else{
            return null;
        }
    }
}
