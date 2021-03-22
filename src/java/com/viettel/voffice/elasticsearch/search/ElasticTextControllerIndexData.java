/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.search;

import com.viettel.voffice.elasticsearch.search.entity.EntityTextReject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.elasticsearch.search.thread.ThreadIndexTextReject;
import com.viettel.voffice.elasticsearch.search.thread.ThreadTextReSign;
import com.viettel.voffice.utils.EncryptDecryptSignDocument;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

/**
 * thuc hien cac noi dung index trong qua trinh xu ly
 * @author datnv5
 */
public class ElasticTextControllerIndexData {
    
    private static final org.apache.log4j.Logger LOGGER = 
            org.apache.log4j.Logger.getLogger(ElasticTextControllerIndexData.class);
    
    public static  boolean updateTextRejectToReSign(Long idText,int state){
        ThreadTextReSign threadIndex = new ThreadTextReSign(idText,state);
        threadIndex.start();
        return true;
    }
    /**
     * thuc hien index truc tiep khi nguoi dung tu choi ky van ban
     * @param idText
     * @return 
     */
    public static synchronized boolean indexTextWhenRejectSign(Long idText){
        ThreadIndexTextReject threadIndex = new ThreadIndexTextReject(idText);
        threadIndex.start();
        return true;
    }
    
    /**
     * Lay chi tiet van ban bi tu choi ky de index vao kho
     * @param idText
     * @return 
     */
    public static String getDetailTextReject(Long idText){
        //chi tiet van ban reject
        TextDAO textDAO = new TextDAO();
        EntityText itemTextInfo = textDAO.getTextByID(idText);
        //lay file ky chinh
        AttachDAO attachDAO = new AttachDAO();
        List<EntityFileAttachment> listFileAtt = attachDAO.getListMainSigningFile(idText);
        //lay danh sach nguoi trinh ky
        List<EntityText> listUser = textDAO.getTextListUserSign(idText, 123L, null);
        List<Long> listUserSign = null;
        if(listUser!=null && listUser.size()>0){
            listUserSign = new ArrayList<>();
            for (EntityText listUser1 : listUser) {
                if(listUser1!=null){
                    listUserSign.add(listUser1.getEmpVhrId());
                    if(listUser1.getStatus()!= null && listUser1.getStatus().equals(2L) 
                            && listUser1.getSignatureType()!= null 
                            && !"2".equals(listUser1.getSignatureType())){
                        break;
                    }
                }
            }
        }
        
        EntityTextReject entityTextReject = new EntityTextReject();
        entityTextReject.setTextId(idText);
        entityTextReject.setTitle(itemTextInfo.getTitle());
        entityTextReject.setConten(itemTextInfo.getContent());
        entityTextReject.setDateCreate(itemTextInfo.getCreateDate());
        entityTextReject.setCreatorId(itemTextInfo.getCreatorId());
        //xet them the loai van ban
        entityTextReject.setPriorityId(itemTextInfo.getPriorityId());
        entityTextReject.setTypeId(itemTextInfo.getTypeId());
        entityTextReject.setStypeId(itemTextInfo.getStypeId());
        entityTextReject.setAreaId(itemTextInfo.getAreaString());
        entityTextReject.setCode(itemTextInfo.getTextCode());
        //doc file text lay noi dung file
        String strContenFile = readFileGetConten(listFileAtt);
        entityTextReject.setContenFileSign(strContenFile);
//        String strSum = itemTextInfo.getTitle() 
//                + "        " + itemTextInfo.getContent()+
//                  "        " + strContenFile;
//        
//        entityTextReject.setStrSumConten(strSum);
        
        entityTextReject.setListUserSign(listUserSign);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String strDataIndex = gson.toJson(entityTextReject);
        return strDataIndex;
    }
    
    public static void main(String[] args) {
        ElasticTextControllerIndexData.getDetailTextReject(82898L);
    }

    /**
     * doc noi dung file
     * @param listFileAtt
     * @return 
     */
    private static String readFileGetConten(List<EntityFileAttachment> listFileAtt) {
        if(listFileAtt != null && listFileAtt.size() > 0){
            try {
                EntityFileAttachment itemFileAt = listFileAtt.get(0);
                String strStorage = itemFileAt.getStorage();
                String exportFolder;
                if(strStorage != null){
                    exportFolder = FunctionCommon.getPropertiesValue(strStorage);
                }else{
                    exportFolder = FunctionCommon.getPropertiesValue("storage_null");
                }
                
                String realPath = exportFolder + itemFileAt.getFilePath();
                String storageTemp = FunctionCommon.getPropertiesValue("storageName_saveFileTmp");
                String temFile  = FunctionCommon.getPropertiesValue(storageTemp)
                            + File.separator + FunctionCommon.genTokenCode() + "_"
                                + (new Date()).getTime() + "_tmp.pdf";
                EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                SecretKey key = ef.getKey();
                try (FileInputStream fis = new FileInputStream(realPath); 
                        FileOutputStream fos = new FileOutputStream(temFile)) {
                    ef.decrypt(key, fis, fos);
                }
                File decryptedFile = new File(temFile);
                if (decryptedFile.exists()) {
                    //file da duoc giai ma
                    //doc noi dung file va dua vao thanh noi dung van ban
                    String strContenFile = FunctionCommon.readContenFile(temFile);
                    decryptedFile.delete();
                    return strContenFile;
                }
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
//                System.out.println(ex);
            } catch (Throwable ex) {
                LOGGER.error(ex.getMessage(), ex);
//                System.out.println(ex);
            }
        }
        return "";
    }
}
