/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search;

import com.google.gson.Gson;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.elasticsearch.search.entity.ConfigEntity;
import com.viettel.voffice.elasticsearch.search.entity.EmployeeEntity;
import com.viettel.voffice.elasticsearch.search.entity.EmployeeEntityHeightLight;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tim kiem danh ba tren bang vhr
 * @author datnv5
 */
public class ElasticSearchVhrEmp {
private static final org.apache.log4j.Logger LOGGER = 
        org.apache.log4j.Logger.getLogger(ElasticSearchVhrEmp.class);
    /**
     * lay danh sach nguoi dung theo dieu kien tim kiem
     * @param strJsonSearch
     * @param aClass
     * @return 
     */
    private static List<? extends Object> getDataListEmployee(
            String strJsonSearch,  Class<?> classOfT) {
        
        String result = "";
        ConfigEntity itemConfig = ElasticCommon.getConfigSearchElastic();
        if(itemConfig!=null && itemConfig.getStatus()==1){
            // cau hinh theo tim kiem moi
            List<String> listUrl = itemConfig.getListIpElastic();
            for (String ipElastic : listUrl) {
                String url = "http://"+ ipElastic 
                + StrElasticConstants.STR_SEARCH_VHREMPLOYEE;
                result = ConnectServer.sendRequestToServer(url, strJsonSearch,itemConfig.getUserPass());
                if(result != null && result.trim().length() > 0){
                    break;
                }
            }
        }else{
            //neu khong cau hinh thi tim kiem theo elasticu
            String url = FunctionCommon.getPropertiesValue("url.elasticsearch.server21")
                + StrElasticConstants.STR_SEARCH_VHREMPLOYEE;
            result = ConnectServer.sendRequestToServer(url, strJsonSearch, null);
        }
        
        
        JSONArray array =  (JSONArray) ConnectServer.getJsonDataRequest(result, false);
        List<Object> listResult = new ArrayList<>();
        for(int i = 0 ; i < array.length() ; i++){
            try {
                JSONObject jsonOb = array.getJSONObject(i).getJSONObject("_source");
                if(!array.getJSONObject(i).isNull("highlight")){
                    JSONObject jsonObHeightlight = array.getJSONObject(i)
                            .getJSONObject("highlight");
                    jsonOb.put("itemHightLight", jsonObHeightlight);
                }else{
                    JSONObject jsonObHightL = new JSONObject();
                    jsonOb.put("itemHightLight", jsonObHightL);
                }
                Gson gson = new Gson();
                Object itemUser = gson.fromJson(jsonOb.toString(),classOfT);
                listResult.add(itemUser);
            } catch (JSONException ex) {
                LOGGER.error("getDataListEmployee", ex);
            }
        }
        return listResult;
    }
    /**
     * Tim kiem danh sach nhan vien
     * @param keyword
     * @param listPathGroup
     * @param startRecord
     * @param pageSize
     * @return 
     */
    public List<EmployeeEntity>  getDataSearch( String keyword, List<String> listPathGroup,
            Long startRecord, Long pageSize){
        List<EmployeeEntity> listResult = new ArrayList<>();
//        keyword = keyword.replace("\"", "");
        if(keyword.trim().length()<=0 && listPathGroup.size() >0
                && ("/148841/148842/").equals(listPathGroup.get(0))){
            return listResult;
        }
        if("SEARCHALLSEARCHALLSEARCHALLSEARCHALLSEARCHALL".equals(keyword)){
            keyword = "";
        }
        String strJsonSearch = ElasticCommon.jsonVhrEmployeeSearchGenarate(keyword,listPathGroup,
                startRecord, pageSize);
//        System.out.println("strJsonSearch==="+strJsonSearch);
        List<EmployeeEntity> result = (List<EmployeeEntity>) 
                ElasticSearchVhrEmp.getDataListEmployee(strJsonSearch, EmployeeEntity.class);
        keyword = FunctionCommon.replaceTextElasticSearchKey(keyword);
        for (int i = 0; i < result.size(); i++) {
            EmployeeEntity itemEmployeeEntity = result.get(i);
            EmployeeEntityHeightLight itemHighlight = itemEmployeeEntity.getItemHightLight();
            List<String> listHightlight = new ArrayList<>();
             if(itemHighlight.getFullNameUnsign()!= null){
                //add full name unsign hightlight
//                List<String> listHl = ConnectServer.getArrHightlight(
//                        itemHighlight.getFullNameUnsign().toString());
                String strNameArr[] = itemEmployeeEntity.getFullName().split(" ");
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
               for (String itemArrOne : arrOne) {
                   String arr[] = FunctionCommon.removeAccent(itemArrOne).split(" ");
                    for (String stringHl : arr) {
                        stringHl = stringHl.trim();
                        String strNameUnsignArr[] = itemEmployeeEntity.getFullNameUnsign().toLowerCase().split(" ");
                         for (int j = 0; j<strNameUnsignArr.length;++j) {
                            String strNameUnsignItem = strNameUnsignArr[j];
                            int position = strNameUnsignItem.indexOf(stringHl.toLowerCase());
                            if(position >= 0){
                                String strHightlight = strNameArr[j].substring(position ,position + stringHl.length());
                                stringHl = stringHl.toLowerCase();
                                if(stringHl.trim().length() > 0 && FunctionCommon.removeAccent(keyword).contains(stringHl)){
                                    listHightlight.add(strHightlight);
                                }
                            }
                         }
                    }
               }
            }else if(itemHighlight.getFullName() != null){
                //add full name hightlight
//                List<String> listHl = ConnectServer.getArrHightlight(
//                        itemHighlight.getFullName().toString());
//               listHightlight.addAll(listHl);
               
                String arr[] = keyword.split(" ");
                String strFullname = itemHighlight.getFullName().toString().toLowerCase();
                List<String> listHl= new ArrayList<>();
                for (String stringHl : arr) {
//                    int position = itemEmployeeEntity.getFullName().toLowerCase().indexOf(stringHl.toLowerCase());
//                    String strHightlight = itemEmployeeEntity.getFullName().substring(position,position + stringHl.length());
                    stringHl = stringHl.toLowerCase();
                    if(stringHl.trim().length() > 0 && strFullname.contains(stringHl)){
                        listHl.add(stringHl);
                    }
                }
                listHightlight.addAll(listHl);
            }
            if(itemHighlight.getBirthDay()!= null){
                 List<String> listHl = ConnectServer.getArrHightlight(
                        itemHighlight.getBirthDay().toString());
                 listHightlight.addAll(listHl);
            }
            
            if(itemHighlight.getGroupNameUnsign()!= null){
                String strNameArr[] = itemEmployeeEntity.getGroupName().split(" ");
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
                for (String itemArrOne : arrOne) {
                   String arr[] = FunctionCommon.removeAccent(itemArrOne).split(" ");
                    for (String stringHl : arr) {
                        stringHl = stringHl.trim();
                        String strNameUnsignArr[] = itemEmployeeEntity.getGroupNameUnsign().toLowerCase().split(" ");
                         for (int j = 0; j<strNameUnsignArr.length;++j) {
                            String strNameUnsignItem = strNameUnsignArr[j];
                            int position = strNameUnsignItem.indexOf(stringHl.toLowerCase());
                            if(position >= 0){
                                String strHightlight = strNameArr[j].substring(position ,position + stringHl.length());
                                stringHl = stringHl.toLowerCase();
                                if(stringHl.trim().length() > 0 && FunctionCommon.removeAccent(keyword).contains(stringHl)){
                                    listHightlight.add(strHightlight);
                                }
                            }
                         }
                    }
               }
            }else if(itemHighlight.getGroupName() != null){
                String arr[] = keyword.split(" ");
                String strFullname = itemHighlight.getGroupName().toString().toLowerCase();
                List<String> listHl= new ArrayList<>();
                for (String stringHl : arr) {
                    stringHl = stringHl.toLowerCase();
                    if(stringHl.trim().length() > 0 && strFullname.contains(stringHl)){
                        listHl.add(stringHl);
                    }
                }
                listHightlight.addAll(listHl);
            }
            if(itemHighlight.getPositionUnsign()!= null){
                String strNameArr[] = itemEmployeeEntity.getPosition().split(" ");
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
                for (String itemArrOne : arrOne) {
                   String arr[] = FunctionCommon.removeAccent(itemArrOne).split(" ");
                    for (String stringHl : arr) {
                        stringHl = stringHl.trim();
                        String strNameUnsignArr[] = itemEmployeeEntity.getPositionUnsign().toLowerCase().split(" ");
                         for (int j = 0; j<strNameUnsignArr.length;++j) {
                            String strNameUnsignItem = strNameUnsignArr[j];
                            int position = strNameUnsignItem.indexOf(stringHl.toLowerCase());
                            if(position >= 0){
                                String strHightlight = strNameArr[j].substring(position ,position + stringHl.length());
                                stringHl = stringHl.toLowerCase();
                                if(stringHl.trim().length() > 0 && FunctionCommon.removeAccent(keyword).contains(stringHl)){
                                    listHightlight.add(strHightlight);
                                }
                            }
                         }
                    }
               }
            }else if(itemHighlight.getPosition() != null){
                String arr[] = keyword.split(" ");
                String strFullname = itemHighlight.getPosition().toString().toLowerCase();
                List<String> listHl= new ArrayList<>();
                for (String stringHl : arr) {
                    stringHl = stringHl.toLowerCase();
                    if(stringHl.trim().length() > 0 && strFullname.contains(stringHl)){
                        listHl.add(stringHl);
                    }
                }
                listHightlight.addAll(listHl);
            }
           
            if(itemHighlight.getLoginName()!= null){
//                 List<String> listHl = ConnectServer.getArrHightlight(
//                        itemHighlight.getLoginName().toString());
//                 listHightlight.addAll(listHl);
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
                for (String itemArrOne : arrOne) {
                    String arr[] = itemArrOne.split(" ");
                    String strLoginName = itemHighlight.getLoginName().toString().toLowerCase();
                    List<String> listHl= new ArrayList<>();
                    for (String stringHl : arr) {
                        stringHl = stringHl.toLowerCase();
                        if(stringHl.trim().length() > 0 && strLoginName.contains(stringHl)){
                            listHl.add(stringHl);
                        }
                    }
                    listHightlight.addAll(listHl);
               }
             }
            if(itemHighlight.getEmail()!= null){
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
                for (String itemArrOne : arrOne) {
                    String arr[] = itemArrOne.split(" ");
                    String email = itemHighlight.getEmail().toString()
                                  .replace("AAAAAAAAAAACONG", "@").toLowerCase();
                    List<String> listHl= new ArrayList<>();
                    for (String stringHl : arr) {
                        stringHl = stringHl.toLowerCase();
                        if(stringHl.trim().length() > 0 && email.contains(stringHl)){
                            listHl.add(stringHl);
                        }
                    }
                    listHightlight.addAll(listHl);
                }
             }
            if(itemHighlight.getMobile()!= null){
//                 List<String> listHl = ConnectServer.getArrHightlight(
//                        itemHighlight.getMobile().toString());
//                 listHightlight.addAll(listHl);
                String arrOne[] = FunctionCommon.removeAccent(keyword).split("\\*");
                for (String itemArrOne : arrOne) {
                    String arr[] = itemArrOne.split(" ");
                    String strMobile = itemHighlight.getMobile().toString().toLowerCase();
                    List<String> listHl= new ArrayList<>();
                    for (String stringHl : arr) {
                        stringHl = stringHl.toLowerCase();
                        if(stringHl.trim().length() > 0 && strMobile.contains(stringHl)){
                            listHl.add(stringHl);
                        }
                    }
                    listHightlight.addAll(listHl);
                }
            }
            if(itemEmployeeEntity.getEmail()!=null){
                itemEmployeeEntity.setEmail(itemEmployeeEntity.getEmail()
                        .replace("AAAAAAAAAAACONG", "@"));
            }
            if(keyword.trim().length() > 0){
               itemEmployeeEntity.setHighlighting(listHightlight);
            }else{
                listHightlight = new ArrayList<>();
                itemEmployeeEntity.setHighlighting(listHightlight);
            }
            
            //set gia tri tra ve client khi khong co du lieu
            if(itemEmployeeEntity.getEmail()== null) itemEmployeeEntity.setEmail("");
            if(itemEmployeeEntity.getMobile()== null) itemEmployeeEntity.setMobile("");
            if(itemEmployeeEntity.getPosition()== null) itemEmployeeEntity.setPosition("");
            listResult.add(itemEmployeeEntity);
        }
        return listResult;
    }
    
    public static void main(String[] args) {
        ElasticSearchVhrEmp a = new ElasticSearchVhrEmp();
       List<String> listPathGroup = new ArrayList<>();
       listPathGroup.add("148841");
       listPathGroup.add("148842");
        List<EmployeeEntity> json = a.getDataSearch("hai ha", listPathGroup,0L, 15L);
//        System.out.println("json"+json);
    }
}
