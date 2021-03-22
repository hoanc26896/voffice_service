/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.entity.EntityAttachment;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.elasticsearch.search.entity.ConfigEntity;
import com.viettel.voffice.elasticsearch.search.entity.document.EntityDocumentTypeSelect;
import com.viettel.voffice.solr.entity.FileEntity;
import com.viettel.voffice.solr.entity.GroupEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author datnv5
 */
public class ElasticSearchDocument {

    private static final Logger LOGGER = Logger.getLogger(ElasticSearchDocument.class);

    public ElasticSearchDocument() {

    }

    /**
     * tim kiem danh sach van ban va chuan hoa danh sach tra ve
     *
     * @param listOrgPath
     * @param keyword
     * @param startRecord
     * @param pageSize
     * @return
     */
    public List<GroupEntity> getDataSearch(List<String> listOrgPath,
            String keyword, Long startRecord, Long pageSize) {
        //loc va tach don vi de tim kiem van ban
        //1. loai bo nhung chuoi org path bi trung
        List<String> listOrgPathDistinct = replaceDuplicateOrgPath(listOrgPath) ;
                
        //cat chuoi de dua vao tim kiem
        List<String> listOrgPathAll = new ArrayList<>();
        for (String string : listOrgPathDistinct) {
            List<String> listParams = getListParams(string);
            if(listParams!=null && listParams.size()>0){
                listOrgPathAll.addAll(listParams);
            }
        }
        listOrgPathAll = replaceDuplicateOrgPath(listOrgPathAll);
        
        
        
        String strJsonSearch = ElasticCommon.jsonDocumentSearchGenarate(listOrgPathAll,
                keyword, startRecord, pageSize);
//        System.out.println("strJsonSearch=="+strJsonSearch);
        List<GroupEntity> result = ElasticSearchDocument.getDataFromServerElastic(strJsonSearch, false);
        return result;
    }

    /**
     * lay count
     *
     * @param listOrgPath
     * @param keyword
     * @return
     */
    public Long getDataCount(List<String> listOrgPath, String keyword) {
       List<String> listOrgPathDistinct = replaceDuplicateOrgPath(listOrgPath) ;
                
        //cat chuoi de dua vao tim kiem
        List<String> listOrgPathAll = new ArrayList<>();
        for (String string : listOrgPathDistinct) {
            List<String> listParams = getListParams(string);
            if(listParams!=null && listParams.size()>0){
                listOrgPathAll.addAll(listParams);
            }
        }
        listOrgPathAll = replaceDuplicateOrgPath(listOrgPathAll);
        
        
        
        String strJsonSearch = ElasticCommon.jsonDocumentSearchGenarate(listOrgPathAll,
                keyword, 0L, 2L);
        List<GroupEntity> result = ElasticSearchDocument.getDataFromServerElastic(strJsonSearch, true);
        Long count = 0L;
        if (result != null && result.size() > 0) {
            count = Long.valueOf(result.get(0).getDocumentId());
        }
        return count;
    }

    /**
     * thuc hien lay du lieu tim kiem
     * @param jsonSearch
     * @param isCout
     * @return 
     */
    public static List<GroupEntity> getDataFromServerElastic(String jsonSearch, Boolean isCout) {
        List<GroupEntity> listGroupResult = new ArrayList<>();
        try {
            //thuc hien kiem tra cau hinh xem  co tim kiem moi ko
            String result = "";
            ConfigEntity itemConfig = ElasticCommon.getConfigSearchElastic();
            if(itemConfig!=null && itemConfig.getStatus()==1){
                // cau hinh theo tim kiem moi
                List<String> listUrl = itemConfig.getListIpElastic();
                for (String ipElastic : listUrl) {
                    String url = "http://"+ ipElastic 
                    + StrElasticConstants.STR_SEARCH_DOCUMENT + "_search";
                    result = ConnectServer.sendRequestToServer(url, jsonSearch,itemConfig.getUserPass());
                    if(result != null && result.trim().length() > 0){
                        break;
                    }
                }
            }else{
                //neu khong cau hinh thi tim kiem theo elasticu
                String url = FunctionCommon.getPropertiesValue("url.elasticsearch.server")
                    + StrElasticConstants.STR_SEARCH_DOCUMENT + "_search";
                result = ConnectServer.sendRequestToServer(url, jsonSearch,null);
            }
            
            if (isCout) {
                Long count = (Long) ConnectServer.getJsonDataRequest(result, true);
                GroupEntity itemGroup = new GroupEntity(String.valueOf(count));
                listGroupResult.add(itemGroup);
                return listGroupResult;
            }
            JSONArray array = (JSONArray) ConnectServer.getJsonDataRequest(result, false);
            if(array == null){
                return null;
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonOb = array.getJSONObject(i).getJSONObject("_source");
                Gson gson = new Gson();
                GroupEntity groupEntity = gson.fromJson(jsonOb.toString(), GroupEntity.class);
                //lay heightlight cua phan dau danh muc
                JSONObject itemHightlight = array.getJSONObject(i);
                Map<String, List<String>> fieldInfoTable = getHeightlightContenHeader(itemHightlight);
                groupEntity.setHighlightTable(fieldInfoTable);
                //lay danh sach file dinh kem co heightlight
                List<FileEntity> listFileInfo = getListFileHeightlight(itemHightlight);
                groupEntity.setListFileInfo(listFileInfo);
                groupEntity.setIsRead("1");
                listGroupResult.add(groupEntity);
            }
        } catch (JSONException ex) {
            LOGGER.error("getDataFromServerElastic", ex);
        }
        return listGroupResult;
    }

    public static List<String> getStringList(JSONArray jsonArray) {
        List<String> listString = null;
        if (jsonArray != null) {
            int length = jsonArray.length();
            listString = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                listString.add(jsonArray.optString(i));
            }
        }
        return listString;
    }

    /**
     * Lay highlight tren cac danh muc tieu de
     *
     * @param jsonObject
     * @return
     */
    static private Map<String, List<String>> getHeightlightContenHeader(JSONObject jsonObject) {
        Map<String, List<String>> fieldInfoTable = new HashMap<>();
        try {
            //dat gia tri vao heightlightTable
            if (!jsonObject.isNull("highlight")) {
                JSONObject jsonObHighlight = jsonObject.getJSONObject("highlight");
                if (!jsonObHighlight.isNull("executionRequirement")) {
                    JSONArray jsonHeightlight = jsonObHighlight.getJSONArray("executionRequirement");
                    List<String> listString = getStringList(jsonHeightlight);
                    fieldInfoTable.put("executionRequirement", listString);
                }
                if (!jsonObHighlight.isNull("documentCode")) {
                    JSONArray jsonHeightlight = jsonObHighlight.getJSONArray("documentCode");
                    List<String> listString = getStringList(jsonHeightlight);
                    fieldInfoTable.put("documentCode", listString);
                }
                if (!jsonObHighlight.isNull("sender")) {
                    JSONArray jsonHeightlight = jsonObHighlight.getJSONArray("sender");
                    List<String> listString = getStringList(jsonHeightlight);
                    fieldInfoTable.put("sender", listString);
                }
                if (!jsonObHighlight.isNull("executionRequirement")) {
                    JSONArray jsonHeightlight = jsonObHighlight.getJSONArray("executionRequirement");
                    List<String> listString = getStringList(jsonHeightlight);
                    fieldInfoTable.put("executionRequirement", listString);
                }
                if (!jsonObHighlight.isNull("signer")) {
                    JSONArray jsonHeightlight = jsonObHighlight.getJSONArray("signer");
                    List<String> listString = getStringList(jsonHeightlight);
                    fieldInfoTable.put("signer", listString);
                }
            }
        } catch (JSONException ex) {
            LOGGER.error("getHeightlightContenHeader", ex);
        }
        return fieldInfoTable;
    }

    /**
     * lay danh sach file dinh kem duoc heighlight
     *
     * @param itemHightlight
     * @return
     */
    private static List<FileEntity> getListFileHeightlight(JSONObject itemHightlight) {
        List<FileEntity> fileEntityResult = new ArrayList<>();
        try {

            if (itemHightlight.isNull("inner_hits")) {
                return null;
            }
            JSONObject jsonObHighlight = itemHightlight.getJSONObject("inner_hits");

            if (jsonObHighlight.isNull("listFileInfo")) {
                return null;
            }
            JSONObject arrListFileAttHl = jsonObHighlight.getJSONObject("listFileInfo");

            if (arrListFileAttHl.isNull("hits")) {
                return null;
            }
            JSONObject jsonhits = arrListFileAttHl.getJSONObject("hits");

            if (jsonhits.isNull("hits")) {
                return null;
            }
            JSONArray array = jsonhits.getJSONArray("hits");

            FileEntity itemFileEntity;
            for (int i = 0; i < array.length(); i++) {
                itemFileEntity = new FileEntity();
                JSONObject itemFile = array.getJSONObject(i);
                if (itemFile.isNull("highlight") || itemFile.isNull("_source")) {
                    break;
                }
                JSONObject fileIndexHeightLight = itemFile.getJSONObject("highlight");
                //add heightlight vao danh muc file
                boolean checkContenFile = false;
                if (!fileIndexHeightLight.isNull("listFileInfo.contenFile")) {
                    JSONArray jsonFileAttContenHL
                            = fileIndexHeightLight.getJSONArray("listFileInfo.contenFile");
                    List<String> listString = getStringList(jsonFileAttContenHL);
                    Map<String, List<String>> heightlightMap = new HashMap<>();
                    if (listString.size() > 0) {
                        heightlightMap.put("content", listString);
                        itemFileEntity.setHighlightTable(heightlightMap);
                        checkContenFile = true;
                    }
                }
                if (!checkContenFile
                        && !fileIndexHeightLight.isNull("listFileInfo.contenFileUnsign")) {
                    JSONArray jsonFileAttContenHL
                            = fileIndexHeightLight.getJSONArray("listFileInfo.contenFileUnsign");
                    List<String> listString = getStringList(jsonFileAttContenHL);
                    Map<String, List<String>> heightlightMap = new HashMap<>();
                    if (listString.size() > 0) {
                        heightlightMap.put("content", listString);
                        itemFileEntity.setHighlightTable(heightlightMap);
                    }
                }

                //lay source item
                JSONObject itemFileDetail = itemFile.getJSONObject("_source");
                String strFileId = null;
                String strAttachment = null;
                String strName = null;
                if (!itemFileDetail.isNull("listFileInfo")) {
                    JSONObject itemFileInfo = itemFileDetail.getJSONObject("listFileInfo");
                    if (!itemFileInfo.isNull("fileId")) {
                        strFileId = itemFileInfo.getString("fileId");
                    }
                    if (!itemFileInfo.isNull("filePath")) {
                        strAttachment = itemFileInfo.getString("filePath");
                    }
                    if (!itemFileInfo.isNull("fileName")) {
                        strName = itemFileInfo.getString("fileName");
                    }
                    if (!itemFileInfo.isNull("fileSize")) {
                        itemFileEntity.setFileSize(itemFileInfo.getString("fileSize"));
                    }
                    if (!itemFileInfo.isNull("pages")) {
                        itemFileEntity.setPages(itemFileInfo.getString("pages"));
                    }
                }

                itemFileEntity.setFileId(strFileId);
                itemFileEntity.setFileName(strName);
                itemFileEntity.setFilePath(strAttachment);
                fileEntityResult.add(itemFileEntity);
            }
        } catch (JSONException ex) {
            LOGGER.error("getListFileHeightlight", ex);
        }
        return fileEntityResult;
    }

    public static void main(String[] args) {
//        String a ="/148841/148842/149014/254921/259217/259218/";
//        String[] arrstring = a.split("/");
//        List<String> listParams = new ArrayList<>();
//        String strOrgId = "";
//        for (String arrstring1 : arrstring) {
//            arrstring1 = arrstring1.replace("/", "");
//            if(arrstring1.trim().length()>0 && FunctionCommon.isNumeric(arrstring1)){
//                strOrgId += "/" + arrstring1;
//                listParams.add(strOrgId +"/");
//            }
//        }
//        System.out.println("listParams" + listParams);
//        
//        ElasticSearchDocument elasticSearch = new ElasticSearchDocument();
//        Long a = elasticSearch.getDataCount(43109L, 495224L, "công văn",null,null);
//        Long aaa = elasticSearch.getDataCount(listParams, "công văn ban hanh lau roi");
//        System.out.println("a="+a);
//        System.out.println("a="+aaa);
        ElasticSearchDocument elasticSearch = new ElasticSearchDocument();
        List<EntityDocument> listDocument = elasticSearch.searchDocSort(
               43109L,495224L, "công văn",0L,100L,1);
      
//        System.out.println("listDocument"+listDocument.size());
//        
//        String aa = "\"toán thuế TNCN năm 2017 cho đơn vị\"";
//        List<String> bb = getPhraseStringSearch(aa);
//        System.out.println("bb"+bb.size());
    }
    
    /**
     * Tim kiem danh sach van ban
     * @param userId1
     * @param userId2
     * @param user2
     * @param type
     * @param status
     * @param document
     * @param adjacent
     * @param staffIds
     * @param startRecord
     * @param pageSize
     * @return 
     */
    public List<EntityDocument> getListSearchDocument(Long userId1, Long userId2,
            Vof2_EntityUser user2,
            Integer type, Integer status, Integer adjacent, EntityDocument document,
            String staffIds, Long startRecord, Long pageSize){
        ElasticSearchDocument elasticSearchDocument = new ElasticSearchDocument();
        
        String strSearch = (document!=null && document.getTitle().trim().length()>0)?document.getTitle():"";
        
       List<EntityDocument> listDocument = elasticSearchDocument.searchDocSort(
               userId1, userId2, strSearch,startRecord,pageSize,status);
        
        return listDocument;
    }
    

    //==============tim kiem van ban trong cac muc theo loai==============
    private List<EntityDocument> searchDocSort(Long userId1, Long userId2,
            String keySearch, Long startRecord, Long pageSize,Integer status) {
        StringBuilder strSearch = new StringBuilder();
        strSearch.append("{\"query\":{\"bool\":{\"must\":[");
        strSearch.append("{\"bool\": {");     
        //gep dieu kien tim kiem
        if(userId1 == null){
            userId1 = -158111L;
        }
        if(userId2 == null){
            userId2 = -158111L;
        }
        String strUserJoin =String.format("USERONE%dUSERTWO%dDATA", userId1,userId2);
        if(userId1.equals(-158111L)){
            strUserJoin = String.format("USERONE%dUSERTWO%dDATA", 0,userId2);
        }
        switch (status) {
            // Tat ca
            case Constants.Document.Status.ALL:
                if(userId1 > 0L){
                    strSearch.append("\"must\":[");
                    strSearch.append("{\"match\":{\"listUserView.userId1\":");
                    strSearch.append(userId1);
                    strSearch.append("}}");
                    strSearch.append(",{\"match\":{\"listUserView.userId2\":");
                    strSearch.append(userId2);
                    strSearch.append("}}");
                    strSearch.append("]");
                }else{
                    strSearch.append("\"must\":[");
                    strSearch.append("{\"match\":{\"listUserView.userId2\":");
                    strSearch.append(userId2);
                    strSearch.append("}}");
                    strSearch.append("]");
                }
                break;
            // Moi/Chua xu ly
            case Constants.Document.Status.NEW:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12isProcessing\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("-158111\"}}");
                strSearch.append("]");
                break;
            // Chua doc
            case Constants.Document.Status.UNREAD:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12isRead\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("0\"}}");
                strSearch.append("]");
                break;
            // Da doc
            case Constants.Document.Status.READ:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12isRead\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("1\"}}");
                strSearch.append("]");
                break;
            // Da xu ly
            case Constants.Document.Status.PROCESSING:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12isProcessing\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("1\"}}");
                strSearch.append("]");
                break;
            // Quan trong
            case Constants.Document.Status.IMPORTANT:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12isMark\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("1\"}}");
                strSearch.append("]");
                break;
            // Da luu
            case Constants.Document.Status.SAVED:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match_phrase\":{\"listUserView.user12processTimeAndIsProcessing\":\"");
                strSearch.append(strUserJoin);
                strSearch.append("-158111AANNDD1\"}}");
                strSearch.append("]");
                break;
            default:
                strSearch.append("\"must\":[");
                strSearch.append("{\"match\":{\"listUserView.userId1\":%d}}");
                strSearch.append(",{\"match\":{\"listUserView.userId2\":%d}}");
                strSearch.append("]");
                break;
        }
        
        strSearch.append("}}]");
        boolean isSearchNotNull = false;
        if(keySearch.trim().length()>0){
//            System.out.println("keySearch====="+keySearch);
            keySearch = keySearch.toLowerCase();
            List<String> listSearchPhrase = getPhraseStringSearch(keySearch);
            keySearch = FunctionCommon.replaceTextElasticSearchKey(keySearch);
            strSearch.append(",\"should\": [");
            //tim kiem uu tien dung voi chuoi dau vao
            StringBuilder searchQuery  = new StringBuilder();
            searchQuery.append("{\"match\": {\"documentId\": \"%s\"}}");
            searchQuery.append(",{\"match_phrase\": {\"code\": \"%s\"}}");
            searchQuery.append(",{\"match_phrase\": {\"title\": \"%s\"}}");
            searchQuery.append(",{\"match_phrase\": {\"titleUnsign\": \"%s\"}}");
            searchQuery.append(",{\"match_phrase\": {\"signer\": \"%s\"}}");
            searchQuery.append(",{\"match_phrase\": {\"signerUnsign\": \"%s\"}}");
            String strSearchParams = keySearch;
            String idDocument = "0";
            if(FunctionCommon.isNumeric(keySearch)){
                idDocument = keySearch.trim();
            }
            strSearch .append(String.format(searchQuery.toString(), idDocument
                    ,strSearchParams,strSearchParams,FunctionCommon.removeAccent(strSearchParams)
                    ,strSearchParams,FunctionCommon.removeAccent(strSearchParams)));
            
            //thuc hien tim kiem theo cum tu dung
            if(listSearchPhrase !=null && listSearchPhrase.size() >0){
                strSearch.append(",{\"bool\": {\"should\": [");
                Boolean firstFor = true;
                for (String string : listSearchPhrase) {
                    string = string.toLowerCase();
                    searchQuery  = new StringBuilder();
                    if(!firstFor){
                       searchQuery.append(","); 
                    }
                    firstFor = false;
                    searchQuery.append("{\"multi_match\" : {\"query\":\"%s\",\"type\":\"phrase\",");
                    searchQuery.append("\"fields\":[ \"code\", \"title\",\"signer\"],\"operator\":\"and\" }}");
                    String paramsQr = string.replace("\"", "");
                    String strQr = String.format(searchQuery.toString(), paramsQr);
                    
                    strSearch .append(strQr);
                    //loai tru nhung ky tu co trong chuoi tim kiem
                    keySearch = keySearch.toLowerCase().replace(string, "").replace("\"", "").replace("*", "");
                    
                    isSearchNotNull = true;
                }
                int countStringPhrase = listSearchPhrase.size();
                if(keySearch.trim().length() >0){
                    keySearch = keySearch.replace("\"", "");
                    //tim kiem voi cac tu con lai khong nam trong cum
                    searchQuery  = new StringBuilder();
                    searchQuery.append(",{\"simple_query_string\": {\"query\": \"%s\",");
                    searchQuery.append("\"default_operator\": \"and\",");
//                    searchQuery.append("\"minimum_should_match\": \"1\",");
                    searchQuery.append("\"fields\": [\"code^5\",\"title^4\",");
                    searchQuery.append("\"signer^3\",");
                    searchQuery.append("\"senderEmail\"]}}");
                    String strQr = String.format(searchQuery.toString(),keySearch);
                    strSearch .append(strQr);
                    countStringPhrase++;
                }
                
                String strCompress = String.format("],\"minimum_should_match\": \"%d\"}}",countStringPhrase);
                strSearch.append(strCompress);
                
                strSearch.append(",{\"bool\": {\"should\": [");
                firstFor = true;
                for (String string : listSearchPhrase) {
                    string = string.toLowerCase();
                    searchQuery  = new StringBuilder();
                    if(!firstFor){
                       searchQuery.append(","); 
                    }
                    firstFor = false;
//                    searchQuery.append("{\"match_phrase\": {\"titleUnsign\": \"%s\"}}");
//                    searchQuery.append(",{\"match_phrase\": {\"signerUnsign\": \"%s\"}}");
//                    searchQuery.append(",{\"match_phrase\": {\"senderNameUnsign\": \"%s\"}}");
                    searchQuery.append("{\"multi_match\" : {\"query\":\"%s\",\"type\":\"phrase\",");
                    searchQuery.append("\"fields\":[ \"titleUnsign\", \"signerUnsign\" ],\"operator\":\"and\" }}");
                   
                    String paramsQr = string.replace("\"", "");
                    String strQr = String.format(searchQuery.toString()
                            , FunctionCommon.removeAccent(paramsQr));
                    strSearch .append(strQr);
                }
                if(keySearch.trim().length() >0){
                    keySearch = keySearch.replace("\"", "");
                    //tim kiem voi cac tu con lai khong nam trong cum
                    searchQuery  = new StringBuilder();
                    searchQuery.append(",{\"simple_query_string\": {\"query\": \"%s\",");
                    searchQuery.append("\"default_operator\": \"and\",");
//                    searchQuery.append("\"minimum_should_match\": \"1\",");
                    searchQuery.append("\"fields\": [");
                    searchQuery.append("\"titleUnsign\",\"signerUnsign\",");
                    searchQuery.append("]}}");
                    String strQr = String.format(searchQuery.toString(),FunctionCommon.removeAccent(keySearch));
                    strSearch .append(strQr);
                }
                countStringPhrase = listSearchPhrase.size()+1;
                strCompress = String.format("],\"minimum_should_match\": \"%d\"}}",countStringPhrase);
                strSearch.append(strCompress);
            }
            
            if(keySearch.trim().length() >0 && (listSearchPhrase ==null || listSearchPhrase.size() <=0)){
                keySearch = keySearch.replace("\"", "");
                isSearchNotNull = true;
                //tim kiem voi cac tu con lai khong nam trong cum
                searchQuery  = new StringBuilder();
                searchQuery.append(",{\"simple_query_string\": {\"query\": \"%s\",");
                searchQuery.append("\"default_operator\": \"and\",");
//                searchQuery.append("\"minimum_should_match\": \"1\",");
                searchQuery.append("\"fields\": [\"code^5\",\"title^4\",");
                searchQuery.append("\"titleUnsign\",\"signer^3\",\"signerUnsign\",");
                searchQuery.append("\"senderName\",\"senderEmail\"]}}");
                String strQr = String.format(searchQuery.toString(),keySearch);
                strSearch .append(strQr);
            }
            strSearch.append(",{\"nested\":{\"path\": \"listFileAttach\",\"query\": {");
            strSearch.append("\"bool\": {\"should\": [");
            Boolean checkSearch = false;
            int shouldMatchFile =0;
            if(listSearchPhrase !=null && listSearchPhrase.size() >0){
                Boolean forFirst = true;
                strSearch.append("{\"bool\": {\"should\": [");
                for (String string : listSearchPhrase) {
                    searchQuery  = new StringBuilder();
                    checkSearch = true;
                    if(!forFirst){
                      searchQuery.append(",");  
                    }
                    searchQuery.append("{\"match_phrase\": {\"listFileAttach.strContenFile\": \"%s\"}}");
                    String strQr = String.format(searchQuery.toString(),string);
                    strSearch .append(strQr);
                    forFirst = false;
                }
                forFirst = true;
                strSearch.append(",{\"bool\": {\"should\": [");
                for (String string : listSearchPhrase) {
                    searchQuery  = new StringBuilder();
                    checkSearch = true;
                    if(!forFirst){
                      searchQuery.append(",");  
                    }
                    searchQuery.append("{\"match_phrase\": {\"listFileAttach.strContenFileUnsign\": \"%s\"}}");
                    String strQr = String.format(searchQuery.toString(),FunctionCommon.removeAccent(string));
                    strSearch .append(strQr);
                    forFirst = false;
                }
                int countStringPhrase = listSearchPhrase.size();
                String strCompress = String.format("],\"minimum_should_match\": \"%d\"}}",countStringPhrase);
                strSearch.append(strCompress);
                strCompress = String.format("],\"minimum_should_match\": \"%d\"}}",countStringPhrase+1);
                strSearch.append(strCompress);
                shouldMatchFile++;
            }
            if(keySearch.trim().length() >0){
                searchQuery  = new StringBuilder();
                if(checkSearch){
                    searchQuery.append(",");  
                }
                keySearch = keySearch.replace("\"", "");
                searchQuery.append("{\"simple_query_string\": {\"query\": \"%s\",");
                searchQuery.append("\"fields\": [\"listFileAttach.strContenFile^4\", \"listFileAttach.strContenFileUnsign\"],");
                searchQuery.append("\"default_operator\": \"and\"}}");
                String strQr = String.format(searchQuery.toString(),keySearch);
                strSearch .append(strQr);
                shouldMatchFile++;
            }
            String strShould = String.format("],\"minimum_should_match\": \"%d\"}}", shouldMatchFile);
            strSearch.append(strShould);
            strSearch.append(",\"inner_hits\": {\"_source\": {");
            strSearch.append("\"includes\": [\"listFileAttach.fileAttachmentId\",");
            strSearch.append("\"listFileAttach.name\", \"listFileAttach.attachment\",");
            strSearch.append("\"listFileAttach.storage\", \"listFileAttach.fileSize\", \"listFileAttach.pages\"]");
            strSearch.append("},\"highlight\": {");
            strSearch.append("\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"],\"fields\": {");
            strSearch.append("\"listFileAttach.strContenFile\": {\"fragment_size\" : 150, \"number_of_fragments\" : 3},\"listFileAttach.strContenFileUnsign\": {\"fragment_size\" : 150, \"number_of_fragments\" : 3}");
            strSearch.append("}}}}}");
            strSearch.append("],\"minimum_should_match\": \"1\"");
        }
        strSearch.append("}},\"sort\":[{\"promulgateDate\":{\"order\":\"desc\"}}],\"min_score\" : \"%d\",\"from\":\"%d\",\"size\":\"%d\"");
        strSearch.append(",\"highlight\": {");
        strSearch.append("\"pre_tags\": [\"<b>\"],\"post_tags\": [\"</b>\"],");
        strSearch.append("\"fields\": {");
        strSearch.append("\"documentId\":{},\"code\": {},\"title\": {},\"signer\": {}");
        strSearch.append(",\"titleUnsign\": {},\"signerUnsign\": {}");
        strSearch.append("}}");
        strSearch.append(",\"_source\": [\"documentId\", \"code\", \"title\" ,\"receiveDate\"");
        strSearch.append(",\"promulgateDate\", \"signer\", \"senderName\", \"commentContent\",\"priorityId\",\"typeId\",\"stypeId\",\"areaId\",\"priority\", \"type\" , \"stype\", \"area\"]");
        strSearch.append("}");

        int minScore = 0;
        String strS;
        if(isSearchNotNull){
            minScore = 3;
            strS = String.format(strSearch.toString(),minScore,startRecord,pageSize);
        }else{
            strS = String.format(strSearch.toString(),minScore,startRecord,pageSize);
        }
        String result = "";
        ConfigEntity itemConfig = ElasticCommon.getConfigSearchElastic();
        if(itemConfig!=null && itemConfig.getStatus()==1){
            // cau hinh theo tim kiem moi
            List<String> listUrl = itemConfig.getListIpElastic();
            for (String ipElastic : listUrl) {
                String url = "http://"+ ipElastic 
                + StrElasticConstants.STR_SEARCH_DOCUMENTSEARCHSORT + "_search";
                result = ConnectServer.sendRequestToServer(url, strS,itemConfig.getUserPass());
                if(result != null && result.trim().length() > 0){
                    break;
                }
            }
        }else{
            //neu khong cau hinh thi tim kiem theo elasticu
            String url = FunctionCommon.getPropertiesValue("url.elasticsearch.server")
                + StrElasticConstants.STR_SEARCH_DOCUMENTSEARCHSORT + "_search";
            result = ConnectServer.sendRequestToServer(url, strS, null);
        }
        
        
        
        
        Long count =  (Long) ConnectServer.getJsonDataRequest(result, true);
        JSONArray array = (JSONArray) ConnectServer.getJsonDataRequest(result, false);
        List<EntityDocument> listResult;
        if(array != null && array.length() > 0){
            listResult = new ArrayList<>();
        }else{
            return null;
        }
       
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonOb = array.getJSONObject(i).getJSONObject("_source");
                Gson gson = new GsonBuilder().create();
                EntityDocument entityDocumentDetail = gson.fromJson(jsonOb.toString(), EntityDocument.class);
                
                
                /*if(!jsonOb.isNull("listUserView")){
                    JSONArray jsonObListUserView = jsonOb.getJSONArray("listUserView");
                     System.out.println(" jsonObListUserView.length():"+ jsonObListUserView.length());
                    for (int j = 0; j < jsonObListUserView.length(); j++) {
                       JSONObject jsonObUserView = jsonObListUserView.getJSONObject(j);
                       Long userv1 = (!jsonObUserView.isNull("userId1")) ? jsonObUserView.getLong("userId1"):0L;
                       Long userv2 = (!jsonObUserView.isNull("userId2")) ? jsonObUserView.getLong("userId2"):0L;
                       String staffId = (!jsonObUserView.isNull("staffId")) ? jsonObUserView.getString("staffId"):null;
                       String senderId2 = (!jsonObUserView.isNull("senderId2")) ? jsonObUserView.getString("senderId2"):null;
                       String documentInStaffId = (!jsonObUserView.isNull("documentInStaffId")) ?jsonObUserView.getString("documentInStaffId"):"0";
                       Integer isProcessing = (!jsonObUserView.isNull("isProcessing")) ?jsonObUserView.getInt("isProcessing"):null;
                       String processTime = (!jsonObUserView.isNull("processTime")) ?jsonObUserView.getString("processTime"):"";
                       Integer isRead= (!jsonObUserView.isNull("isRead")) ?jsonObUserView.getInt("isRead"):0;
                       Integer isMark= (!jsonObUserView.isNull("isMark")) ?jsonObUserView.getInt("isMark"):0;
                       if(userv1.equals(userId1) || userv2.equals(userId2)){
                           Long docInStaff = Long.valueOf(documentInStaffId);
                           entityDocumentDetail.setDocumentInStaffId(docInStaff);
                           if(isProcessing!= null && isProcessing == -158111){
                               isProcessing = null;
                           }
                           if("-158111".equals(processTime)){
                              processTime = null;
                           }
                           entityDocumentDetail.setIsProcessing(isProcessing);
                           entityDocumentDetail.setProcessTime(processTime);
                           entityDocumentDetail.setIsRead(isRead);
                           Long staffIdLong = (staffId!=null && staffId.trim().length()>0)
                                   ? Long.valueOf(staffId):null;
                           Long senderId2Long = (senderId2!=null && senderId2.trim().length()>0) 
                                   ? Long.valueOf(senderId2):null;
                           entityDocumentDetail.setStaffId(staffIdLong);
                           entityDocumentDetail.setSenderId2(senderId2Long);
                           if(isMark == -158111){isMark=0;}
                           entityDocumentDetail.setIsMark(isMark);
                           break;
                       }
                    }
                }*/
                //thuc hien lay chi tiet noi dung trang thai theo van ban tu id van ban
                DocumentDAO documentDAO = new DocumentDAO();
                EntityDocumentTypeSelect itemProcessByUser = documentDAO.getProcessAndStatusDocByUser(
                        entityDocumentDetail.getDocumentId()
                        , userId1, userId2);
                
                if(itemProcessByUser!=null){
                    if(itemProcessByUser.getDocumentInStaffId()!= null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getDocumentInStaffId())){
                        Long idDocumentInStaff = Long.valueOf(itemProcessByUser.getDocumentInStaffId());
                        entityDocumentDetail.setDocumentInStaffId(idDocumentInStaff);
                    }else{
                        entityDocumentDetail.setDocumentInStaffId(null);
                    }
                    if(itemProcessByUser.getIsProcessing() !=null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getIsProcessing())){
                        Integer isProcessing = Integer.valueOf(itemProcessByUser.getIsProcessing());
                        entityDocumentDetail.setIsProcessing(isProcessing);
                    }else{
                        entityDocumentDetail.setIsProcessing(null);
                    }
                    
                    entityDocumentDetail.setProcessTime(itemProcessByUser.getProcessTime());
                    
                    if(itemProcessByUser.getIsRead()!=null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getIsRead())){
                        Integer isRead = Integer.valueOf(itemProcessByUser.getIsRead());
                        entityDocumentDetail.setIsRead(isRead);
                    }else{
                        entityDocumentDetail.setIsRead(0);
                    }
                    
                    if(itemProcessByUser.getStaffId()!= null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getStaffId())){
                        Long staffId = Long.valueOf(itemProcessByUser.getStaffId());
                        entityDocumentDetail.setStaffId(staffId);
                    }else{
                        entityDocumentDetail.setStaffId(null);
                    }
                    if(itemProcessByUser.getSenderId2()!=null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getSenderId2())){
                        Long senderId2 = Long.valueOf(itemProcessByUser.getSenderId2());
                        entityDocumentDetail.setSenderId2(senderId2);
                    }else{
                        entityDocumentDetail.setSenderId2(null);
                    }
                    if(itemProcessByUser.getSenderName()!=null ){
                        entityDocumentDetail.setSenderName2(itemProcessByUser.getSenderName());
                        entityDocumentDetail.setSenderName(itemProcessByUser.getSenderName());
                    }
                    
                    if(itemProcessByUser.getIsMark()!= null 
                            && FunctionCommon.isNumeric(itemProcessByUser.getIsMark())){
                        Integer isMark = Integer.valueOf(itemProcessByUser.getIsMark());
                        entityDocumentDetail.setIsMark(isMark);
                    }else{
                        entityDocumentDetail.setIsMark(null);
                    }      
                }
                if(count != null){
                    Integer countInt = Integer.valueOf(String.valueOf(count));
                    entityDocumentDetail.setTotal(countInt);
                }
                
                //gan thong tin tim kiem hightlight
                JSONObject itemObjectJson = array.getJSONObject(i);
                EntityDocument itemHightlight = new EntityDocument();
                if(itemObjectJson.has("highlight")){
                    //cac muc tim kiem co hien thi noi dung phu hop
                    JSONObject jsonObHightLight = itemObjectJson.getJSONObject("highlight");

                    if(jsonObHightLight.has("senderName")){
                        itemHightlight.setSenderName(jsonObHightLight.getString("senderName"));
                    }else if(jsonObHightLight.has("senderNameUnsign")){
                        itemHightlight.setSenderName(jsonObHightLight.getString("senderNameUnsign"));
                    }
                    if(jsonObHightLight.has("signer")){
                        itemHightlight.setSigner(jsonObHightLight.getString("signer"));
                    }else if(jsonObHightLight.has("signerUnsign")){
                        itemHightlight.setSigner(jsonObHightLight.getString("signerUnsign"));
                    }
                    
                    if(jsonObHightLight.has("code")){
                        itemHightlight.setCode(jsonObHightLight.getString("code"));
                    }
                    
                    if(jsonObHightLight.has("title")){
                        itemHightlight.setTitle(jsonObHightLight.getString("title"));
                    }else if(jsonObHightLight.has("titleUnsign")){
                        itemHightlight.setTitle(jsonObHightLight.getString("titleUnsign"));
                    }
                }
                if(itemObjectJson.has("inner_hits")){
                    List<EntityAttachment> listFileAttHightlight = new ArrayList<>();
                    //hightlight noi dung file
                    JSONObject jsonObHightLight = itemObjectJson.getJSONObject("inner_hits");
                    if(jsonObHightLight.has("listFileAttach")&&!jsonObHightLight.isNull("listFileAttach")){
                        JSONObject jsonObHightLightHits  = jsonObHightLight.getJSONObject("listFileAttach");
                        if(jsonObHightLightHits.has("hits") && !jsonObHightLightHits.isNull("hits")){
                            JSONObject jsonObHightLightHits1  = jsonObHightLightHits.getJSONObject("hits");
                            if(jsonObHightLightHits1.has("hits")&& !jsonObHightLightHits1.isNull("hits")){
                                JSONArray jsonArrList = jsonObHightLightHits1.getJSONArray("hits");
                                for (int j = 0; j < jsonArrList.length(); j++) {
                                    EntityAttachment itemFileAttHighthight = new EntityAttachment();
                                    JSONObject objFileHl  = jsonArrList.getJSONObject(j);
                                    //lay id file
                                    if(objFileHl.has("_source")&&!objFileHl.isNull("_source")){
                                        JSONObject itemFile = objFileHl.getJSONObject("_source");
                                        if(itemFile.has("listFileAttach")&&!itemFile.isNull("listFileAttach")){
                                           JSONObject itemFileChild = itemFile.getJSONObject("listFileAttach");
                                           if(itemFileChild.has("fileAttachmentId") 
                                                   && !itemFileChild.isNull("fileAttachmentId")){
                                               itemFileAttHighthight.setFileAttachmentId(itemFileChild.getLong("fileAttachmentId"));
                                           }
                                        }
                                    }
                                    //lay noi dung hightlight
                                    if(objFileHl.has("highlight")){
                                        JSONObject itemFile = objFileHl.getJSONObject("highlight");
                                        if(itemFile.has("listFileAttach.strContenFile") 
                                                && !itemFile.isNull("listFileAttach.strContenFile")){
                                           JSONArray itemFileChild = itemFile.getJSONArray("listFileAttach.strContenFile");
                                           itemFileAttHighthight.setStrHightligh(itemFileChild.toString().trim().replaceAll("\\s{2,}", "   "));
                                        }else if(itemFile.has("listFileAttach.strContenFileUnsign")){
                                           JSONArray itemFileChild = itemFile.getJSONArray("listFileAttach.strContenFileUnsign");
                                           itemFileAttHighthight.setStrHightligh(itemFileChild.toString().trim().replaceAll("\\s{2,}", "   "));
                                        }
                                    }
                                    listFileAttHightlight.add(itemFileAttHighthight);
                                }
                            }
                        }
                        itemHightlight.setFileAttachments(listFileAttHightlight);
                    }
                }
                entityDocumentDetail.setHightlightObj(itemHightlight);
                listResult.add(entityDocumentDetail);
                
            } catch (JSONException ex) {
                LOGGER.error(ex.getMessage(), ex);
//                System.out.println(ex);
            }
        }
        return listResult;
    }
    
    /**
     * cat cuoi cac don vi
     * @param string
     * @return 
     */
    private List<String> getListParams(String string) {
        String[] arrstring = string.split("/");
        List<String> listParams = new ArrayList<>();
        String strOrgId = "";
        for (String arrstring1 : arrstring) {
            arrstring1 = arrstring1.replace("/", "");
            if(arrstring1.trim().length()>0 && FunctionCommon.isNumeric(arrstring1)){
                strOrgId += "/" + arrstring1;
                listParams.add(strOrgId +"/");
            }
        }
        return listParams;
    }
    /**
     * loai bo chuoi trung nhau
     * @param listOrgPath
     * @return 
     */
    private List<String> replaceDuplicateOrgPath(List<String> listOrgPath) {
        List<String> listOrgDistinct = new ArrayList<>();
        for (String itemString : listOrgPath) {
            boolean check = false;
            for (String string : listOrgDistinct) {
                if(string.equals(itemString)){
                   check = true;
                   break;
                }
            }
            if(!check){
                listOrgDistinct.add(itemString);
            }
        }
        return listOrgDistinct;
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
}
