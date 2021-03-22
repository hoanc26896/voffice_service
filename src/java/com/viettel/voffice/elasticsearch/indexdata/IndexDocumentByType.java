/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.elasticsearch.indexdata;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.elasticsearch.connectserver.ConnectServer;
import com.viettel.voffice.elasticsearch.search.StrElasticConstants;
import com.viettel.voffice.elasticsearch.search.entity.document.EntityDocumentTypeSelect;
import com.viettel.voffice.elasticsearch.search.entity.document.EntityFileAttachInDocument;
import com.viettel.voffice.utils.EncryptDecryptSignDocument;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.apache.log4j.Logger;

/**
 *
 * @author datnv5
 */
public class IndexDocumentByType implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(IndexDocumentByType.class);
    String name;
    Long idDocument;

    public IndexDocumentByType(Long idDocument) {
//        System.out.println("idDocument:"+idDocument);
        this.idDocument = idDocument;
        this.name = "IndexDocumentByType_" + String.valueOf(idDocument);
    }

    @Override
    public void run() {
        //thuc hien update lai danh dau van ban chua duoc danh index
        DocumentDAO documentDAO = new DocumentDAO();
        documentDAO.updateDocIndexToNoIndex(idDocument);
        
        // Read data from elastic
        //su dung de danh index luon cho van ban
        /*String strServerIndexDocument
                    = FunctionCommon.getPropertiesValue(
                            "url.elasticsearch.server")
                    + StrElasticConstants.STR_SEARCH_DOCUMENTSEARCHSORT
                    + idDocument;
        String searchData = ConnectServer.sendRequestToServerGet(strServerIndexDocument);
        EntityDocumentTypeSelect itemDocumentTypeSelect = 
                (EntityDocumentTypeSelect) ConnectServer.getJsonFromDataGet(
                        searchData,EntityDocumentTypeSelect.class);
        boolean isUpdate = itemDocumentTypeSelect != null;
        
        DocumentDAO documentDAO = new DocumentDAO();
        EntityDocumentTypeSelect itemDocDetail
                = documentDAO.getDocumentDetailIndex(idDocument,isUpdate);
        // Goi lay noi dung file 
        if (!isUpdate) {
            itemDocDetail = detechContenFileDoc(itemDocDetail);
        } else {
            itemDocumentTypeSelect.setListUserView(itemDocDetail.getListUserView());
            itemDocDetail = itemDocumentTypeSelect;
        }
        if (itemDocDetail != null) {
            //convert object to Json
            strServerIndexDocument += "?pretty";
            String jsonData = FunctionCommon.convertObjectToStringJson(itemDocDetail);
            String strResult = ConnectServer.sendRequestToServer(strServerIndexDocument, jsonData);
            LOGGER.info("IndexDocumentByType: " + strResult);
        } else {
            LOGGER.info("Khong co nguoi xem van ban: " + idDocument);
        }*/
    }

    /**
     * tach va doc noi dung file
     * @param itemDocDetail
     * @return 
     */
    private EntityDocumentTypeSelect detechContenFileDoc(EntityDocumentTypeSelect itemDocDetail) {
        EntityDocumentTypeSelect itemDocumentResult = itemDocDetail;
        if (itemDocDetail != null) {
            List<EntityFileAttachInDocument> listItemFileAt = itemDocDetail.getListFileAttach();
            for (int i = 0; i<listItemFileAt.size(); ++i) {
                try {
                    EntityFileAttachInDocument itemFileAt = listItemFileAt.get(i);
                    String strStorage = itemFileAt.getStorage();
                    String exportFolder;
                    if (strStorage != null) {
                        exportFolder = FunctionCommon.getPropertiesValue(strStorage);
                    } else {
                        exportFolder = FunctionCommon.getPropertiesValue("storage_null");
                    }
                    String realPath = exportFolder + itemFileAt.getAttachment();
                    String storageTemp = FunctionCommon.getPropertiesValue("storageName_saveFileTmp");
                    String temFile = FunctionCommon.getPropertiesValue(storageTemp)
                            + File.separator + itemFileAt.getFileAttachmentId() 
                            + "_" + FunctionCommon.genTokenCode() + "_"
                            + (new Date()).getTime() + "_tmp.pdf";
                    EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                    SecretKey key = ef.getKey();
                    FileInputStream fis = new FileInputStream(realPath);
                    FileOutputStream fos = new FileOutputStream(temFile);
                    ef.decrypt(key, fis, fos);
                    File decryptedFile = new File(temFile);
                    if (decryptedFile.exists()) {
                        String strContenFile = FunctionCommon.readContenFile(temFile);
                        listItemFileAt.get(i).setStrContenFile(strContenFile);
                        listItemFileAt.get(i).setStrContenFileUnsign(
                                FunctionCommon.removeAccent(strContenFile));
                        decryptedFile.delete();
                    }
                } catch (Exception ex) {
                    LOGGER.info(ex);
                } catch (Throwable ex) {
                    LOGGER.info(ex);
                }

            }
            itemDocumentResult.setListFileAttach(listItemFileAt);
        }
        return itemDocumentResult;
    }
}
