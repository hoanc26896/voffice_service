/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.EncryptDecryptSignDocument;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 *
 * @author datnv5
 */
public class CorrectDocumentControler {

    private static final Logger LOGGER = Logger.getLogger(CorrectDocumentControler.class);
    
    public String checkCorrectDocument(HttpServletRequest request,
            String data) {
        String[] keys = new String[]{"typeDocument","idAttach"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        //thuc hien lay du lieu check loi van ban
        List<String> listValue = userGroup.getListParamsFromClient();
        String strTypeDocument =  listValue.get(0);
        String strIdAttach =  listValue.get(1);
        if(strTypeDocument.trim().equals("1")){
            //check noi dung file cho van ban ban hanh
            if(FunctionCommon.isNumeric(strIdAttach)){
                //id file dinh kem
                Long idAttach = Long.valueOf(strIdAttach);
                //lay thong tin file dinh kem cua van ban thong qua idFile
                 //getFileAttachInfo(idAttach);
                
            }
        }
        
        
        
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    null, userGroup);
    }
    
    //=====================thuc hien doc noi dung file========================
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
//                System.out.println(ex);
                LOGGER.error(ex.getMessage(), ex);
            } catch (Throwable ex) {
//                System.out.println(ex);
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return "";
    }
}
