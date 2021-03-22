/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viettel.digital.sign.cert.Synchronization;
import com.viettel.digital.sign.utils.SequenceNumber;
import com.viettel.voffice.basefield.FieldText;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.controler.signature.SequenceId;
import com.viettel.voffice.controler.signature.SignController;
import com.viettel.voffice.database.dao.document.DocOrgRepublishDAO;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.DocumentPublishedTmpDAO;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.dao.document.TextSearchDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.task.MissionSigningDAO;
import com.viettel.voffice.database.dao.text.HistoryChangeSignDAO;
import com.viettel.voffice.database.dao.text.TextCommonDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.dao.text.TextProcessDAO;
import com.viettel.voffice.database.dao.text.TextSignDAO;
import com.viettel.voffice.database.entity.EntityAttach;
import com.viettel.voffice.database.entity.EntityBriefMark;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityFilesAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityMarkAttachHistory;
import com.viettel.voffice.database.entity.EntityTextMark;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.document.EntityDocumentPublishedTmp;
import com.viettel.voffice.database.entity.log.EntityActionLogMobile;
import com.viettel.voffice.database.entity.task.EntityBriefInfo;
import com.viettel.voffice.database.entity.text.EntityHistoryChangeSign;
import com.viettel.voffice.database.entity.text.EntityMutiSms;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextCountStatus;
import com.viettel.voffice.database.entity.text.EntityTextProcess;
import com.viettel.voffice.database.entity.text.EntityTextRejectBefor;
import com.viettel.voffice.elasticsearch.search.ElasticTextControllerIndexData;
import com.viettel.voffice.elasticsearch.search.entity.document.EntityFileAttachInDocument;
import com.viettel.voffice.thread.DocumentThread;
import com.viettel.voffice.thread.TextThread;
import com.viettel.voffice.thread.TextThread.TextAttachFileOtherThread;
import com.viettel.voffice.thread.TextThread.TextAttachFileSignThread;
import com.viettel.voffice.thread.TextThread.TextAttachFromDocumentThread;
import com.viettel.voffice.thread.TextThread.TextDetailThread;
import com.viettel.voffice.thread.TextThread.TextDrafSignerThread;
import com.viettel.voffice.thread.TextThread.TextGetHistoryRejectText;
import com.viettel.voffice.thread.TextThread.TextSignerThread;
import com.viettel.voffice.thread.TextThread.ThreadCountTextByStatus;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

import java.util.Date;

/**
 *
 * @author vinhnq13
 */
public class TextController {

    public static final String ROOT_ACTION = "textAction";
    // Log file
    private static final Logger LOGGER = Logger.getLogger(TextController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = TextController.class.getName();

    /**
     * <b>Tim kiem van ban tren he thong 1</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String searchText(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {
        String[] keys = new String[] {
            ConstantsFieldParams.IS_COUNT,
            ConstantsFieldParams.IS_FINANCIAL,
            ConstantsFieldParams.TYPE,
            ConstantsFieldParams.KEYWORD,
            ConstantsFieldParams.REGISTER_NUMBER,
            ConstantsFieldParams.CODE,
            ConstantsFieldParams.TITLE,
            ConstantsFieldParams.DESCRIPTION,
            ConstantsFieldParams.TYPE_ID,
            ConstantsFieldParams.AREA_ID,
            ConstantsFieldParams.STATE,
            ConstantsFieldParams.FROM_DATE,
            ConstantsFieldParams.TO_DATE,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
            ConstantsFieldParams.FILLTER_TYPE,
            ConstantsFieldParams.REQUISITION_ID,
            "stateMark", "orgDocManagerIds"
        };
        Date startTime = new Date();
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        Long userIdVof2 = userGroup.getUserId2();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        Long userIdVof1 = null;
        List<Long> lstGroupVoffice1 = new ArrayList<>();
        List<Long> lstGroupSerecVoffice1 = new ArrayList<>();
        List<Long> lstGroupOnlyLeader1 = new ArrayList<>();
        if (user != null && user.getUserId() != null) {
            if (user.getLstGroupOnlyLeader1() != null && user.getLstGroupOnlyLeader1().size() > 0) {
                lstGroupOnlyLeader1.addAll(user.getLstGroupOnlyLeader1());
            }
            if (user.getListGroupIdLDVof1() != null && user.getListGroupIdLDVof1().size() > 0) {
                lstGroupVoffice1.addAll(user.getListGroupIdLDVof1());
            }
            if (user.getListGroupIdVTVof1() != null && user.getListGroupIdVTVof1().size() > 0) {
                lstGroupSerecVoffice1.addAll(user.getListGroupIdVTVof1());
            }
            userIdVof1 = user.getUserId();
        }

        //Thong tin don vi tren vof2.0
        List<Long> lstGroupVoffice2 = new ArrayList<>();
        List<Long> lstGroupSerecVoffice2 = new ArrayList<>();
        List<Long> lstGroupOnlyLeader2 = new ArrayList<>();
        if (userVof2.getListManagementOrg() != null && userVof2.getListManagementOrg().size() > 0) {
            lstGroupOnlyLeader2.addAll(userVof2.getListManagementOrg());
        }
        if (userVof2.getLstVhrOrgNotSecretary() != null && userVof2.getLstVhrOrgNotSecretary().size() > 0) {
            lstGroupVoffice2.addAll(userVof2.getLstVhrOrgNotSecretary());
        }
        if (userVof2.getListSecretaryVhrOrg() != null && userVof2.getListSecretaryVhrOrg().size() > 0) {
            lstGroupSerecVoffice2.addAll(userVof2.getListSecretaryVhrOrg());
        }
        List<String> listValue = userGroup.getListParamsFromClient();
        // Lay so luong
        String isCount = listValue.get(0);
        // Lay van ban tai chinh
        int isFinancial = -1;
        String strIsFinancial = listValue.get(1);
        if (!CommonUtils.isEmpty(strIsFinancial)) {
            isFinancial = Integer.parseInt(strIsFinancial);
        }
        // Loai van ban
        int type = 0;
        String strType = listValue.get(2);
        if (!CommonUtils.isEmpty(strType)) {
            type = Integer.parseInt(strType);
        }
        // Tu khoa tim kiem nhanh
        String keyword = listValue.get(3);
        // So dang ky
        String registerNumber = listValue.get(4);
        // Ma van ban
        String code = listValue.get(5);
        // Tieu de
        String title = listValue.get(6);
        // Noi dung
        String description = listValue.get(7);
        // Id the thuc van ban
        int typeId = -1;
        String strTypeId = listValue.get(8);
        if (!CommonUtils.isEmpty(strTypeId)) {
            typeId = Integer.parseInt(strTypeId);
        }
        // Id linh vuc van ban
        int areaId = -1;
        String strAreaId = listValue.get(9);
        if (!CommonUtils.isEmpty(strAreaId)) {
            areaId = Integer.parseInt(strAreaId);
        }
        // Trang thai
        int state = -1;
        String strState = listValue.get(10);
        if (!CommonUtils.isEmpty(strState)) {
            state = Integer.parseInt(strState);
        }
        // Tu ngay
        String fromDate = listValue.get(11);
        // Den ngay
        String toDate = listValue.get(12);

        // Vi tri lay ra
        Long startRecord = 0L;
        String strStartRecord = listValue.get(13);
        if (!CommonUtils.isEmpty(strStartRecord)) {
            startRecord = Long.parseLong(strStartRecord);
        }
        // So luong lay ra
        Long pageSize = 10L;
        String strPageSize = listValue.get(14);
        if (!CommonUtils.isEmpty(strPageSize)) {
            pageSize = Long.parseLong(strPageSize);
        }
        // Tim kiem theo van ban co 1.0 hay khong
        String isSearchTextAll = listValue.get(15);
        // Loc tim kiem theo van ban KTTS
        Long filterType = null;
        String strFilterType = listValue.get(16);
        if (!CommonUtils.isEmpty(strFilterType)) {
            filterType = Long.parseLong(strFilterType);
        }
        // tuanld
        Long requisitionId = null;
        String strRequisitionId = listValue.get(17);
        if (!CommonUtils.isEmpty(strRequisitionId)) {
            requisitionId = Long.parseLong(strRequisitionId);
        }
        // 201812-Pitagon: add
        Integer stateMark = null;
        String strStateMark = listValue.get(18);
        if (!CommonUtils.isEmpty(strStateMark)) {
        	stateMark = Integer.parseInt(strStateMark);
        }
        List<Long> orgDocManagerIds = new ArrayList<Long>();
        String orgDocManagerStr = listValue.get(19);
        if (!CommonUtils.isEmpty(orgDocManagerStr)) {
        	JSONArray jsonArr = new JSONArray(orgDocManagerStr);
        	if (jsonArr != null && jsonArr.length() > 0) {
				for (int index = 0; index < jsonArr.length(); index++) {
					orgDocManagerIds.add(jsonArr.getLong(index));
				}
        	}
        }
        
        TextSearchDAO textDAO = new TextSearchDAO();
        Object result;
        String strTypeSearch = "";
        if (type == Constants.Text.SearchType.MARK) {
        	result = textDAO.getTextMarkList(userIdVof1, userIdVof2, isCount, keyword, registerNumber,
                    code, title, description, typeId, areaId, stateMark != null ? stateMark : 0,
                    fromDate, toDate, startRecord, pageSize, requisitionId, orgDocManagerIds);
                strTypeSearch = "getTextMarkList";
        } else {
	        result = textDAO.searchText(userIdVof1, userIdVof2, isCount,
	                isFinancial, type, keyword, registerNumber, code,
	                title, description, typeId, areaId, state,
	                fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
	                lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
	                lstGroupSerecVoffice2, startRecord, pageSize, filterType,
	                isSearchTextAll, requisitionId, stateMark);
                strTypeSearch = "searchText";
        }
        String functionName = "textAction.searchText";
        EntityActionLogMobile logDB = new EntityActionLogMobile(userIdVof1, userGroup.getCardId(),
                startTime, new Date(), functionName, description + strTypeSearch, "", "All", null);
        LogUtils.insertActionLogMobile(userGroup.getUserId2(), logDB);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
    }

    /**
     * <b>Lay chi tiet van ban trinh ky</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String getTextDetail(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Session timeout
                if (userGroup.getVof2_ItemEntityUser() == null) {
                    LOGGER.error("getTextDetail (Lay chi tiet van ban trinh ky)"
                            + " - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                if (userGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = userGroup.getItemEntityUser().getUserId();
                }
                // Thong tin don vi tren vof2.0
                Long sysUserIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.IS_LISTFILE,
                    ConstantsFieldParams.TYPE,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                    ConstantsFieldParams.IS_VERSION_NEW
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));
                Long isListFile = Long.parseLong(listValue.get(1));
                String type = listValue.get(2);
                String isSearchAll = listValue.get(3);
                String isVersionNew = listValue.get(4);

                TextDAO textDAO = new TextDAO();
                EntityText result = textDAO.getTextDetail(request, textId, isListFile,
                        sysUserIdVof1, sysUserIdVof2, type, isSearchAll, isVersionNew);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
            } catch (Exception ex) {
                LOGGER.error("getTextDetail (Lay chi tiet van ban trinh ky) - Exception"
                        + " - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("getTextDetail (Lay chi tiet van ban trinh ky) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * Van thu tu choi xet duyet van ban
     */
    public String rejectSignDocByVTAction(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                List<Long> lstGroupSecretaryVof1 = new ArrayList<Long>();
                List<Long> lstGroupSecretaryVof2;
                if (entityUserGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = entityUserGroup.getItemEntityUser().getUserId();
                    lstGroupSecretaryVof1 = entityUserGroup.getItemEntityUser().getListGroupIdVTVof1();
                }
                Vof2_EntityUser entityUserVof2 = entityUserGroup.getVof2_ItemEntityUser();
                //Thong tin don vi tren vof2.0
                boolean isSecrectary = entityUserGroup.getVof2_ItemEntityUser().getIsSecrectaryVo2();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{ConstantsFieldParams.TEXT_ID, ConstantsFieldParams.comment,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL, ConstantsFieldParams.IS_COMMENT_FILE};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));
                String isCommentFile = listValue.get(3);

                //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
                List<EntityFileAttachment> strListFileSign = new ArrayList<>();
                JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, data);

                if (arrListFileSign != null && arrListFileSign.length() > 0) {
                    for (int i = 0; i < arrListFileSign.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_PATH)) {
                            filePath = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_PATH).trim();
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);
                        file.setFileAttachmentId(fileAttachmentId);

                        strListFileSign.add(file);
                    }
                }
                //End

                if (isSecrectary) {
                    TextSignDAO tSignDao = new TextSignDAO();
                    //kiem tra xem trang thai van ban co phai la cho ky
                    List<EntityText> lstText = tSignDao.getInfoText(textId);
                    //datnv5: kiem tra va chan doc file ky khi tong giam doc vua ky file
                    SignController signController = new SignController();
                    boolean isNotReadDoc = signController.getDoNotReadTextDuringSign(entityUserVof2.getUserId(), textId);
                    if(isNotReadDoc){
                        //trong qua trinh xu ly ky cua lanh dao truo
                         return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 
                                 Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN , strAesKeyDecode);
                    }
                    if (!CommonUtils.isEmpty(lstText)
                            && lstText.get(0).getStatus() != null
                            && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                        //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_SIGN_NOT_PERMISS,
                                false, strAesKeyDecode);
                        return strResult;
                    }
                    //Luu file dinh kem
                    Long text_process_id = tSignDao.addFilesSign(strListFileSign, textId, dataSessionGR, 0);
                    if (text_process_id >= 1L) {
                        lstGroupSecretaryVof2 = entityUserGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg();
                        String comment = listValue.get(1);
                        String isSearchAll = listValue.get(2);
                        TextDAO tDAO = new TextDAO();
                        String result = tDAO.rejectSignDocByVTAction(textId, sysUserIdVof1,
                                entityUserVof2, lstGroupSecretaryVof1,
                                lstGroupSecretaryVof2, comment, isSearchAll,
                                text_process_id, isCommentFile);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        //datnv5: index to elastic text reject accept by screctory
                        ElasticTextControllerIndexData.indexTextWhenRejectSign(textId);
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                    } else {
                        return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, strResult, strAesKeyDecode);
                    }
                }
            } catch (JSONException | NumberFormatException ex) {
                LOGGER.error(ex.getMessage(), ex);
                LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

   

    /**
     * Chuyen ky nhay
     */
    public String transferToPreSigner(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                List<Long> lstGroupIdLDVof1 = new ArrayList<>();

                if (entityUserGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = entityUserGroup.getItemEntityUser().getUserId();
                    if (entityUserGroup.getItemEntityUser().getListGroupIdLDVof1() != null
                            && entityUserGroup.getItemEntityUser().getListGroupIdLDVof1().size() > 0) {
                        lstGroupIdLDVof1.addAll(entityUserGroup.getItemEntityUser().getListGroupIdLDVof1());
                    }

                }
                Vof2_EntityUser entityUserVof2 = entityUserGroup.getVof2_ItemEntityUser();
                //Thong tin don vi tren vof2.0
                List<Long> lstGroupVoffice2 = new ArrayList<>();
                if (entityUserGroup.getVof2_ItemEntityUser().getLstVhrOrgNotSecretary() != null
                        && entityUserGroup.getVof2_ItemEntityUser().getLstVhrOrgNotSecretary().size() > 0) {
                    lstGroupVoffice2.addAll(entityUserGroup.getVof2_ItemEntityUser().getLstVhrOrgNotSecretary());
                }

                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{ConstantsFieldParams.comment, ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.groupIdAdd, ConstantsFieldParams.lstPreSigern,
                    ConstantsFieldParams.lstSignDraff,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                String comment = listValue.get(0);
                Long textId = Long.parseLong(listValue.get(1));
                String groupIdAddVof2 = listValue.get(2);
                String lstPreSigernVof2 = listValue.get(3);
                String lstSignDraffVof2 = listValue.get(4);
                // Tim kiem theo van ban co 1.0 hay khong
                String isSearchTextAll = listValue.get(5);
                //Danh sach nguoi ky nhay tren 1.0
                String groupIdAddVof1 = "";
                String lstPreSigernVof1 = "";
                TextDAO tDAO = new TextDAO();
                //Check ds chuyen ky nhay co map giua db1 voi db2 hay chua
//                boolean isMap = false;
//                String[] arrPreSign = lstPreSigernVof2.split(";");
//                if (arrPreSign != null && arrPreSign.length > 0) {
//                    for (int i = 0; i < arrPreSign.length; i++) {
//                        List<Long> staffCreateId = new ArrayList<Long>();
//                        Long valueId = (arrPreSign[i] != null && arrPreSign[i].trim().length() > 0) ? Long.valueOf(arrPreSign[i].trim()) : null;
//                        if (valueId != null) {
//                            staffCreateId.add(valueId);
//                        }
//                        UserDAO userDAO1 = new UserDAO();
//                        List<EntityUserGroup> listUser = userDAO1.selectAndMapUser(staffCreateId, false);
//                        System.out.println("list Value:" + listUser.toString());
//                        EntityUser item = listUser.get(0).getItemEntityUser();
//                        if (item != null && listUser.size() == arrPreSign.length) {
//                            isMap = true;
//                            if (i == 0 || i == (arrPreSign.length - 1)) {
//                                lstPreSigernVof1 = (item.getUserId() != null) ? item.getUserId().toString() : "-11";
//                                groupIdAddVof1 = (item.getGroupId() != null) ? item.getGroupId().toString() : "-11";
//                            } else {
//                                lstPreSigernVof1 += item.getUserId().toString() + ";";
//                                groupIdAddVof1 += item.getGroupId().toString() + ";";
//                            }
//                        } else {
//                            isMap = false;
//                            break;
//                        }
//                    }
//                }

                boolean result = tDAO.transferToPreSigner(comment, textId, sysUserIdVof1,
                        entityUserVof2, lstGroupIdLDVof1, lstGroupVoffice2,
                        groupIdAddVof1, groupIdAddVof2, lstPreSigernVof1,
                        lstPreSigernVof2, lstSignDraffVof2, isSearchTextAll);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);

//                if (isMap) {
//                    boolean result = tDAO.transferToPreSigner(comment, textId, sysUserIdVof1,
//                            entityUserVof2, lstGroupOnlyLeader1, lstGroupVoffice2,
//                            groupIdAddVof1, groupIdAddVof2, lstPreSigernVof1,
//                            lstPreSigernVof2, lstSignDraffVof2,isSearchTextAll);
//                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
//                } else {
//                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, ErrorCode.ERR_NOT_SYNC, strAesKeyDecode);
//                }
            } catch (JSONException | NumberFormatException ex) {
                LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
                LOGGER.error("Exception transferToPreSigner:", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * <b>Chinh sua thong tin van ban trinh ky khi van thu xet duyet/ban
     * hanh</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateSigner(HttpServletRequest request,
            String data, String isSecurity) {

        String errorDesc = "updateSigner (Cap nhat thong tin van ban khi van thu xet duyet/ban hanh) - ";
        String[] keys = new String[]{
            ConstantsFieldParams.ENTITY_TEXT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return FunctionCommon.responseResult(ErrorCode.NO_SESSION, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Chuoi van ban
            String strText = listValue.get(0);
            Gson gson = new Gson();
            // Parse chuoi strText sang doi tuong EntityText
            EntityText text = gson.fromJson(strText, EntityText.class);
            TextDAO textDAO = new TextDAO();
            Object result = textDAO.updateSignerByVT(userGroup.getVof2_ItemEntityUser().getUserId(),
                    text);
            // Loi server
            if (result == null) {
                LOGGER.error(errorDesc + "result = null - username: " + userGroup.getCardId());
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Thay nguoi ky</b>
     *
     * @author thanght6
     * @since Jun 15, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateListSigner(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.LIST_NEW_SIGNER
        };
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateListSigner - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        if (!userGroup.checkUserId()) {
            LOGGER.error("updateListSigner - user hoac userId tren he thong 1&2 null!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Chuoi nguoi ky moi
            String strNewSigner = listValue.get(0);
            // Parse chuoi nguoi ky moi thanh danh sach
            Type listType = new TypeToken<ArrayList<EntityTextProcess>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityTextProcess> listNewSigner = gson.fromJson(strNewSigner, listType);
            // Thay nguoi ky
            TextProcessDAO textProcessDAO = new TextProcessDAO();
            List<EntityText> result = textProcessDAO.updateListSigner(userGroup.getUserId1(),
                    userGroup.getUserId2(), userGroup.getName2(), listNewSigner);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("updateListSigner - Exception - username: " + userGroup.getCardId(),
                    ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * thienng1 Ban hanh van ban thu cong
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String documentPromulgate(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Lay session
                EntityUserGroup entityUserGroup = dataSessionGR;

                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                Long sysUserId = null;
                Long orgId = null;
                Long v2UserId = null;
                Long v2GroupId = null;
                Vof2_EntityUser v2User = entityUserGroup.getVof2_ItemEntityUser();
                if (v2User != null) {
                    v2UserId = v2User.getUserId();
                    v2GroupId = v2User.getSysOrgId();
                }
                //Lay thong tin user tren vof1
                if (entityUserGroup.getItemEntityUser() != null) {
                    sysUserId = entityUserGroup.getItemEntityUser().getUserId();
                    orgId = entityUserGroup.getItemEntityUser().getGroupId();
                }
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                //lay thong tin tu client
                if (data == null) {
                    return null;
                }
                JSONObject json = new JSONObject(data);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                // lay danh sach nguoi trinh ky
                if (json.getString("entityDocument") == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                String valueDocument = json.getString("entityDocument");
                if (valueDocument == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
                EntityDocument entityDocument = gson.fromJson(valueDocument, EntityDocument.class);
                //
                String valueTextId = json.getString("textId");
                Long textId = gson.fromJson(valueTextId, Long.class);
                //danh sach cac file dinh kem can xoa
                List<Long> lstAttachOrther = null;
                if (json.has("lstAttachOrther") && json.getString("lstAttachOrther") != null) {
                    String valueListAttach = json.getString("lstAttachOrther");
                    Type listType = new TypeToken<ArrayList<Long>>() {
                    }.getType();
                    lstAttachOrther = gson.fromJson(valueListAttach, listType);
                }
                // Chia nho van ban khi ban hanh
                List<EntityDocument> listChildDocument = null;
                if (json.has("listChildDocument") && json.getString("listChildDocument") != null) {
                	String strListChildDocument = json.getString("listChildDocument");
                	Type type = new TypeToken<ArrayList<EntityDocument>>() {
                    }.getType();
                    listChildDocument = gson.fromJson(strListChildDocument, type);
                }
                
                //140217 sua bo kiem tra so dang ky
//                //sua goi y so dang ky
//                boolean checkNumberRegister = FunctionCommon.isNumeric(entityDocument.getRegisterNumber());
//                if (checkNumberRegister) {
//                    //kiem tra so dang ky co ton tai tren he thong
//                    TextCommonDAO textCommonDAO = new TextCommonDAO();
//                    Boolean registerNumberIsNew = textCommonDAO.checkNumberManual(textId,
//                            Long.valueOf(entityDocument.getRegisterNumber()),
//                            entityDocument.getStypeId());
//                    if (registerNumberIsNew) {
//                        return FunctionCommon.generateResponseJSON(ErrorCode.TEXT_EXIST_NUMBER_MANUAL, null, null);
//                    }
//                }

//                String[] keys = new String[]{
//                    ConstantsFieldParams.IS_AUTO,
//                    ConstantsFieldParams.PUBLISHER_ID,
//                    ConstantsFieldParams.DOCUMENT_ID,
//                    ConstantsFieldParams.SUMMARY,
//                    ConstantsFieldParams.PUBLISHED_DATE,
//                    ConstantsFieldParams.EXPIRED_DATE,
//                    ConstantsFieldParams.BUILT_GROUP_ID,
//                    ConstantsFieldParams.ORG_LEVEL,
//                    ConstantsFieldParams.DEAD_LINE_REPUBLISH, // Lay the deadline de luu du lieu
//                    ConstantsFieldParams.SCOPE_NAME // Lay the deadline de luu du lieu
//                };
//                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                //Bien luu trang thai cong bo tu dong khi ban hanh thu cong hay khong
//                boolean setAutoPublish = false;
                // Lay danh sach pham vi cong bo
//                List<Long> docScopeIds = new ArrayList<Long>();
//                if (!json.isNull(ConstantsFieldParams.DOC_SCOPE_IDS)) {
//                    JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.DOC_SCOPE_IDS);
//                    if (jsonArray != null && jsonArray.length() > 0) {
//                        JSONObject jsonObject;
//                        Long docScopeId;
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            jsonObject = jsonArray.getJSONObject(i);
//                            if (!jsonObject.isNull(ConstantsFieldParams.SCOPE_ID)) {
//                                docScopeId = jsonObject.getLong(ConstantsFieldParams.SCOPE_ID);
////                            System.out.println("Danh sach pham vi:" + docScopeId);
//                                docScopeIds.add(docScopeId);
//                            }
//                        }
//                    }
//                    // 0: Cong bo thu cong
//                    // 1: Cong bo tu dong
//                    int isAuto = 1;
//                    // Id nguoi cong bo
//                    Long publisherId = 0L;
//                    String strPublisherId = listValue.get(1);
//                    if (!CommonUtils.isEmpty(strPublisherId)) {
//                        publisherId = Long.parseLong(strPublisherId);
//                    }
//                    // Id van ban
//                    Long documentId = Long.parseLong(listValue.get(2));
//
//                    // Tom tat
//                    String summary = listValue.get(3);
//
//                    // Ngay cong bo
//                    String publishedDate = listValue.get(4);
//
//                    // Ngay het hieu luc
//                    String expiredDate = listValue.get(5);
//
//                    // Id don vi xay dung
//                    Long builtGroupId = Long.parseLong(listValue.get(6));
//
//                    // Level don vi
//                    Integer levelPublishedGroup = 0;
//                    // Han ban hanh lai van ban
//                    String deadLineRepublish = listValue.get(8);
//                    // Ten pham vi khac
//                    String scopeNameOrther = listValue.get(9);
//                    // Danh sach id van ban thay the them vao
//                    List<Long> listAdditionalAlternativeDocumentId = null;
//                    if (!json.isNull(ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS)) {
//                        jsonArray = json.getJSONArray(ConstantsFieldParams.ADDITIONAL_ALTERNATIVE_DOCUMENT_IDS);
//                        if (jsonArray != null && jsonArray.length() > 0) {
//                            listAdditionalAlternativeDocumentId = new ArrayList<Long>();
//                            JSONObject jsonObject;
//                            Long additionalAlternativeDocumentId;
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                jsonObject = jsonArray.getJSONObject(i);
//                                if (!jsonObject.isNull(ConstantsFieldParams.DOCUMENT_ID)) {
//                                    additionalAlternativeDocumentId = jsonObject.getLong(ConstantsFieldParams.DOCUMENT_ID);
//                                    listAdditionalAlternativeDocumentId.add(additionalAlternativeDocumentId);
//                                }
//                            }
//                        }
//                    }
//
//                    // Lay van ban goc ::cuongnv
//                    EntityDocument eBaseDoc = new EntityDocument();
//                    JSONObject inObj = new JSONObject(data);
//                    if (inObj.has(ConstantsFieldParams.BASE_DOCUMENT)) {
//                        String str = inObj.getString(ConstantsFieldParams.BASE_DOCUMENT);
//                        if (str != null) {
//                            inObj = new JSONObject(str);
//                            if (inObj.has(ConstantsFieldParams.DOCUMENT_ID) && inObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
//                                eBaseDoc.setDocumentId(Long.parseLong(inObj.getString(ConstantsFieldParams.DOCUMENT_ID).trim()));
//                                eBaseDoc.setSysOrganizationId(Long.parseLong(inObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
//                            } else {
//                                eBaseDoc = null;
//                            }
//                        } else {
//                            eBaseDoc = null;
//                        }
//                    } else {
//                        eBaseDoc = null;
//                    }
//
//                    // Lay danh sach don vi pham vi ::cuongnv
//                    List<EntityVhrOrg> listOrgNeedRepublish = new ArrayList<>();
//                    JSONArray arrOrgNeedRepublish = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_ORG_NEED_REPUBLISH, data);
//
//                    if (arrOrgNeedRepublish != null && arrOrgNeedRepublish.length() > 0) {
//                        for (int i = 0; i < arrOrgNeedRepublish.length(); i++) {
//                            JSONObject innerObj = (JSONObject) arrOrgNeedRepublish.get(i);
//                            if (innerObj.has(ConstantsFieldParams.SYS_ORGANIZATION_ID)) {
//                                EntityVhrOrg eVhr = new EntityVhrOrg();
//                                eVhr.setSysOrganizationId(Long.parseLong(innerObj.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID).trim()));
//                                listOrgNeedRepublish.add(eVhr);
//                            }
//                        }
//                    }
//                    //Hiendv2 bo sung path don vi trong pham vi
//                    // Lay danh sach pham vi cong bo
//                    List<String> strOrgPath = new ArrayList<String>();
//                    List<Long> vhrOrgIds = new ArrayList<>();
//                    if (!json.isNull(ConstantsFieldParams.SCOPE_HAVE_ORGS)) {
//                        jsonArray = json.getJSONArray(ConstantsFieldParams.SCOPE_HAVE_ORGS);
//                        if (jsonArray != null && jsonArray.length() > 0) {
//                            JSONObject jsonObject;
//                            String orgPath;
//                            Long orgIds;
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                jsonObject = jsonArray.getJSONObject(i);
//                                if (!jsonObject.isNull(ConstantsFieldParams.PATH_ORG)) {
//                                    orgPath = jsonObject.getString(ConstantsFieldParams.PATH_ORG);
//                                    orgIds = Long.parseLong(jsonObject.getString(ConstantsFieldParams.SYS_ORGANIZATION_ID));
//
////                            System.out.println("Danh sach pham vi:" + docScopeId);
//                                    strOrgPath.add(orgPath);
//                                    vhrOrgIds.add(orgIds);
//                                }
//                            }
//                        }
//                    }
//                    DocumentDAO documentDAO = new DocumentDAO();
//                    int resUpdateTmpPublish = documentDAO.publishDocument(isAuto,
//                            sysUserId, orgId, v2GroupId, publisherId,
//                            documentId, summary, publishedDate, expiredDate, builtGroupId,
//                            levelPublishedGroup, listAdditionalAlternativeDocumentId, docScopeIds,
//                            strOrgPath, vhrOrgIds, listOrgNeedRepublish,
//                            eBaseDoc, deadLineRepublish, scopeNameOrther);
//                    if (resUpdateTmpPublish > 0) {
//                        setAutoPublish = true;
//                    }
//                }

                TextDAO textDAO = new TextDAO();
                Long result = textDAO.updateDocumentPromulgate(sysUserId, orgId,
                        lstAttachOrther, textId, entityDocument, false, 
                        v2UserId, v2GroupId);

                // Loi server
                if (result == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                } else {
                    //sua goi y so dang ky
                    //datnv5: bo sung chen so ban hanh bang tay vao bang danh so bang tay
                    textDAO.insertDocNumberManual(textId, sysUserId, entityDocument);
                    
                    if (!CommonUtils.isEmpty(listChildDocument)) {
                    	DocumentDAO documentDAO = new DocumentDAO();
                    	List<EntityFileAttachInDocument> listAttach = documentDAO.getListFileAttachDoc(result);
                    	Map<Long, Long> mapAttach = new HashMap<>();
                    	if (!CommonUtils.isEmpty(listAttach)) {
                    		for (EntityFileAttachInDocument att : listAttach) {
                    			mapAttach.put(att.getAttachId(), att.getFileAttachmentId());
                    		}
                    	}
                    	for (EntityDocument childDocument : listChildDocument) {
                    		if (!CommonUtils.isEmpty(childDocument.getAttachFiles())) {
            	                for (EntityFilesAttachment att : childDocument.getAttachFiles()) {
            	                	att.setFileAttachmentId(mapAttach.get(att.getAttachId()));
            	                }
                    		}
                    		
                    		if (!CommonUtils.isEmpty(childDocument.getListAttachment())) {
            	                for (EntityFilesAttachment att : childDocument.getListAttachment()) {
            	                	att.setFileAttachmentId(mapAttach.get(att.getAttachId()));
            	                }
                    		}
                    	}
                    	
                        documentDAO.splitDocument(v2UserId, result, listChildDocument);
                    }
                    
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                //Logger.getLogger(StaffGroupController.class.getName()).log(Level.SEVERE, null, ex);
//                LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * <b>Huy ban hanh van ban</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String cancelDocumentPublish(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.ENTITY_TEXT
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("cancelDocumentPublish [Huy ban hanh van ban] - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu client gui len
            List<String> listValue = userGroup.getListParamsFromClient();
            String strText = listValue.get(0);
            if (CommonUtils.isEmpty(strText)) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            Gson gson = new Gson();
            EntityText text = gson.fromJson(strText, EntityText.class);
            TextDAO textDAO = new TextDAO();
            int result = textDAO.cancelDocumentPublish(userGroup.getUserId2(),
                    userGroup.getName2(), text);
            // Loi server
            if (result != 1) {
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("cancelDocumentPublish [Huy ban hanh van ban] - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * Cap nhat trang thai ky duyet van ban
     */
    public String updateDatabaseSign(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            Long textId = null;
            Long userIdVof2 = null;
            Boolean isSignParallel = false;//ky song song
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                //Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly // Lay userId tren he thong 1
                Long sysUserId = null;
                Long sysUserIdVof1 = null;
                List<Long> lstGroupSecretaryVof1 = new ArrayList<Long>();
                List<Long> lstGroupSecretaryVof2 = new ArrayList<Long>();

                if (entityUserGroup.getItemEntityUser() != null) {
                    // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                    sysUserId = entityUserGroup.getItemEntityUser().getUserId();

                    sysUserIdVof1 = entityUserGroup.getItemEntityUser().getUserId();
                    lstGroupSecretaryVof1 = entityUserGroup.getItemEntityUser().getListGroupIdVTVof1();
                }
                Vof2_EntityUser entityUserVof2 = entityUserGroup.getVof2_ItemEntityUser();
                lstGroupSecretaryVof2 = entityUserVof2.getListSecretaryVhrOrg();
                boolean isSecrectary = entityUserGroup.getVof2_ItemEntityUser().getIsSecrectaryVo2();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.comment,
                    ConstantsFieldParams.simVersion,
                    ConstantsFieldParams.TEXT_ATTACH_ID,
                    ConstantsFieldParams.PATH_ATTACH_ID,
                    ConstantsFieldParams.SIGN_TYPE,
                    ConstantsFieldParams.newStorage,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                    "classificationTypeAssessor"
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                textId = Long.parseLong(listValue.get(0));
                String comment = listValue.get(1);
                Long simVersion = Long.parseLong(listValue.get(2));
//                String newPathFile = listValue.get(4);
//                Long attachId = Long.valueOf(listValue.get(3));
                String signType = listValue.get(5);
                String newStorage = listValue.get(6);
                String isSearchAll = listValue.get(7);
                String strClassificationTypeAssessor = listValue.get(8);
                Integer classificationTypeAssessor = null;
                if (!CommonUtils.isEmpty(strClassificationTypeAssessor)) {
                    classificationTypeAssessor = Integer.parseInt(strClassificationTypeAssessor);
                }
                TextSignDAO tSignDao = new TextSignDAO();
                //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
                List<EntityFileAttachment> strListFileSign = new ArrayList<>();
                JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, data);

                if (arrListFileSign != null && arrListFileSign.length() > 0) {
                    for (int i = 0; i < arrListFileSign.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_PATH)) {
                            filePath = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_PATH).trim();
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);
                        file.setFileAttachmentId(fileAttachmentId);

                        strListFileSign.add(file);
                    }
                }
                //End
                boolean result = false;
                if (signType != null) {
                    //kiem tra xem trang thai van ban co phai la cho ky
                    //datnv5: kiem tra xem dieu kien neu la van ban duoc tgd xu ly thi xu ly ky lại
                    SignController signController = new SignController();
                    boolean isNotReadDoc = signController.getDoNotReadTextDuringSign(entityUserVof2.getUserId(), textId);
                    if(isNotReadDoc){
                        //trong qua trinh xu ly ky cua lanh dao truo
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 
                                 Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN , strAesKeyDecode);
                    }
                    
                    boolean isUserConfigDuringSign = isUserInDuringSing(entityUserVof2.getUserId(), textId);
                    if(!isUserConfigDuringSign){
                        List<EntityText> lstText = tSignDao.getInfoText(textId);
                        if (!CommonUtils.isEmpty(lstText)
                                && lstText.get(0).getStatus() != null
                                && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                            //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                            strResult = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_SIGN_NOT_PERMISS,
                                    result, strAesKeyDecode);
                            return strResult;
                        }
                    }
                    
                    if ("0".equals(signType) && isSecrectary) {
                        //Cap nhat trang thai xet duyet
                        Long text_process_id = tSignDao.addFilesSign(strListFileSign, textId, dataSessionGR, 0);
                        if (text_process_id >= 1L) {
                            result = tSignDao.updateDatabaseByVT(textId, null, sysUserIdVof1, entityUserVof2,
                                    lstGroupSecretaryVof1, lstGroupSecretaryVof2, comment,
                                    simVersion, null, newStorage, isSearchAll, 1, text_process_id);
                        }
                    } else {
                        Long text_process_id = tSignDao.addFilesSign(strListFileSign, textId, dataSessionGR, Integer.parseInt(signType));
                        if (text_process_id >= 1L) {
                            //kiem tra co bi khoa ky song song khong
                            userIdVof2 = entityUserVof2.getUserId();
                            //lay trang thai ky song song
                            List<EntityText> lstStateSignTextParallel = tSignDao.getListStateSignParallel(textId);
                            if (!CommonUtils.isEmpty(lstStateSignTextParallel)) {
                                isSignParallel = true;
                            }
                            if (isSignParallel) {
                                //neu la ky song 
                                Boolean isLockSignParallel = tSignDao.isLockSignParallel(lstStateSignTextParallel, userIdVof2);
                                if (isLockSignParallel) {
                                    //neu la khoa ky song song thi bao loi
                                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_LOCK_SIGN_STATE,
                                            result, strAesKeyDecode);
                                    return strResult;
                                } else {
                                    //Cap nhat trang thai ky duyet
                                    DocumentSignDAO DocumentSignDAO = new DocumentSignDAO();
                                    DocumentSignDAO.updateSignParallelNext(textId, userIdVof2,
                                            Constants.TextSignParallel.LOCK_SIGN_STATE, null);//khoa ky song song
                                    List<EntityMutiSms> lstMutiSms = new ArrayList<>();
                                    result = tSignDao.updateDatabaseAffterSign(textId, null, sysUserId,
                                            entityUserVof2, comment, simVersion, null,
                                            signType, newStorage, lstMutiSms);

                                }
                            } else {
                                //neu la ky thuong
                                List<EntityMutiSms> lstMutiSms = new ArrayList<>();
                                result = tSignDao.updateDatabaseAffterSign(textId, null, sysUserId,
                                        entityUserVof2, comment, simVersion, null,
                                        signType, newStorage, lstMutiSms);
                            }
                        }
                    }
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                if (result) {
                    MissionSigningDAO missionSigningDAO = new MissionSigningDAO();
                    missionSigningDAO.update(dataSessionGR.getUserId2(), textId,
                            classificationTypeAssessor);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, result, strAesKeyDecode);
                }
            } catch (Exception ex) {
                LOGGER.error("Loi ham updateDatabaseSign", ex);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            } finally {
                if (isSignParallel) {
                    //neu la ky song song thi mo khoa
                    TextSignDAO tSignDao = new TextSignDAO();
                    tSignDao.unlockSignTextParallel(textId, userIdVof2);//mo khoa ky song song
                }
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * Tu choi ky - vinhnq13
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String rejectSignDocument(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                if (entityUserGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = entityUserGroup.getItemEntityUser().getUserId();
                }
                Vof2_EntityUser entityUserVof2 = entityUserGroup.getVof2_ItemEntityUser();
                //Thong tin don vi tren vof2.0
//                Long sysUserIdVof2 = entityUserVof2.getUserId();
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
//                LOGGER.error("Du lieu dau vao ham tu choi ky: " + data);
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.comment,
                    ConstantsFieldParams.rejectType,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                    ConstantsFieldParams.IS_COMMENT_FILE};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));
                String comment = listValue.get(1);
                String rejectType = listValue.get(2);
                String isSearchAll = listValue.get(3);
                String isCommentFile = listValue.get(4);
                //Begin::Lay danh sach file duoc dinh kem khi ky::cuongnv
                List<EntityFileAttachment> strListFileSign = new ArrayList<>();
                JSONArray arrListFileSign = FunctionCommon.jsonGetArray(ConstantsFieldParams.REQ_LST_ATTACHMENT_FILES, data);

                if (arrListFileSign != null && arrListFileSign.length() > 0) {
                    for (int i = 0; i < arrListFileSign.length(); i++) {
                        JSONObject innerObj = (JSONObject) arrListFileSign.get(i);
                        Long fileAttachmentId = null;
                        String name = "";
                        String filePath = "";

//                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_ID)) {
//                            fileAttachmentId = Long.parseLong(innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_ID).trim());
//                        }
                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_NAME)) {
                            name = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_NAME).trim();
                        }

                        if (innerObj.has(ConstantsFieldParams.TEXT_ATTACH_PATH)) {
                            filePath = innerObj.getString(ConstantsFieldParams.TEXT_ATTACH_PATH).trim();
                        }

                        EntityFileAttachment file = new EntityFileAttachment();
                        file.setFilePath(filePath);
                        file.setName(name);
                        file.setFileAttachmentId(fileAttachmentId);

                        strListFileSign.add(file);
                    }
                }
                //End
                TextSignDAO tSignDao = new TextSignDAO();
                //kiem tra xem trang thai van ban co phai la cho ky
                //datnv5: kiem tra xem dieu kien neu la van ban duoc tgd xu ly thi xu ly ky lại
                 SignController signController = new SignController();
                boolean isNotReadDoc = signController.getDoNotReadTextDuringSign(entityUserVof2.getUserId(), textId);
                if(isNotReadDoc){
                    //trong qua trinh xu ly ky cua lanh dao truo
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 
                             Constants.SIGN_RESULT_CODE.STATUS_DELAY_SIGN , strAesKeyDecode);
                }
                boolean isUserConfigDuringSign = isUserInDuringSing(entityUserVof2.getUserId(), textId);
                if(!isUserConfigDuringSign){
                    List<EntityText> lstText = tSignDao.getInfoText(textId);
                    if (!CommonUtils.isEmpty(lstText)
                            && lstText.get(0).getStatus() != null
                            && !lstText.get(0).getStatus().equals((long) Constants.Text.State.PROCESSING)) {
                        //neu trang thai van ban khac dang xu ly thi tra ve loi va khong cho ky
                        strResult = FunctionCommon.generateResponseJSON(ErrorCode.TEXT_SIGN_NOT_PERMISS,
                                false, strAesKeyDecode);
                        return strResult;
                    }
                }
                //Luu file dinh kem
                Long text_process_id = 0L;
                if (rejectType != null) {
                    text_process_id = tSignDao.addFilesSign(strListFileSign, textId, dataSessionGR, 1);
                }
                if (text_process_id >= 1L) {
                    TextDAO tDAO = new TextDAO();
                    String result = tDAO.rejectSignDocument(textId, sysUserIdVof1,
                            entityUserVof2, comment, rejectType, isSearchAll, isCommentFile);
                    //datnv5: danh index file tu choi ky chinh
                    ElasticTextControllerIndexData.indexTextWhenRejectSign(textId);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, strResult, strAesKeyDecode);
                }

            } catch (JSONException | NumberFormatException ex) {
                LOGGER.error("Error rejectSignDocument: ", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * lay danh dach nguoi ky duyet van ban
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String getListSigner(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(request);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                // Session time out
                EntityUserGroup entityUserGroup = dataSessionGR;
                if (entityUserGroup.getItemEntityUser() == null && entityUserGroup.getVof2_ItemEntityUser() == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.SESSION_TIME_OUT, null, null);
                }

                // Lay danh sach don vi ma user la lanh dao hoac thu truong don vi hoac tro ly
                Long sysUserId = entityUserGroup.getVof2_ItemEntityUser().getUserId();

                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = entityUserGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                String cardId = entityUserGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);

                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{ConstantsFieldParams.TEXT_ID, ConstantsFieldParams.TYPE};
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));
                String type = listValue.get(1);
//                System.out.println("getListSigner: " + listValue.toString());
                boolean isFromDoc = false;
                if (!CommonUtils.isEmpty(type)) {
                    isFromDoc = true;
                }

                TextDAO tDAO = new TextDAO();
                List<EntityText> result = tDAO.getTextListUserSignByAssign(textId,
                        sysUserId, isFromDoc, true, null);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        return strResult;
    }

    /**
     * <b>Tao thread de dem so luong van ban ky</b><br/>
     *
     * @author thanght6
     * @since May 21, 2016
     * @param userId1 Id nguoi dung tren he thong 1
     * @param userId2 Id nguoi dung tren he thong 2
     * @param countType Cach dem
     * @param documentDAO Doi tuong truy van bang document
     * @return
     */
    public List<ThreadCountTextByStatus> getCountTextStatusAll(Long userIdVof1, Long userIdVof2, int isFinancial,
            int type, String keyword, String registerNumber, String code, String title,
            String description, int typeId, int areaId, int state, String fromDate,
            String toDate, List<Long> lstGroupLeaderVof1, List<Long> lstGroupLeaderVof2,
            List<Long> lstGroupOnlyLeader1, List<Long> lstGroupOnlyLeader2,
            List<Long> lstGroupSecretaryVof1, List<Long> lstGroupSecretaryVof2,
            Long startRecord, Long pageSize, Long filterType, String isSearchAll, TextSearchDAO textSearchDAO, Long requisitionId) {

        List<ThreadCountTextByStatus> listThread = new ArrayList<ThreadCountTextByStatus>();

        ThreadCountTextByStatus thread;
        //Diem theo van ban ky duyet
        if (type == Constants.Text.SearchType.SIGNED) {
            // Dem tat ca van ban ky duyet
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.TEXT_SIGN_ALL_FOR_MOBILE, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);
            // Dem cho ky duyet
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.NOT_HANDLE, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);
            // Dem cho ky nhay
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.INITIAL_SIGNING, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);
            //Dem da ky duyet
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.LEADER_SIGNED, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);

            listThread.add(thread);
            //Dem da ky nhay
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.INITIAL_SIGNED, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);
            //Dem da tu choi ky duyet
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.LEADER_REJECTED, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);
            //Dem da tu choi ky nhay
            thread = new ThreadCountTextByStatus(userIdVof1, userIdVof2, isFinancial,
                    type, keyword, registerNumber, code, title,
                    description, typeId, areaId, Constants.TextProcess.State.INITIAL_REJECTED, fromDate,
                    toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2,
                    lstGroupSecretaryVof1, lstGroupSecretaryVof2,
                    startRecord, pageSize, filterType, isSearchAll,
                    textSearchDAO, requisitionId);
            listThread.add(thread);

        }
        return listThread;
    }

    /**
     *
     * Dem thong ke so luong cac loai van ban tren menu trai
     *
     */
    public String countTextSignAll(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        Object result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        List<Long> lstGroupVoffice1 = new ArrayList<Long>();
        List<Long> lstGroupSerecVoffice1 = new ArrayList<Long>();
        List<Long> lstGroupOnlyLeader1 = new ArrayList<Long>();
        if (user != null && user.getUserId() != null) {
            if (user.getLstGroupOnlyLeader1() != null && user.getLstGroupOnlyLeader1().size() > 0) {
                lstGroupOnlyLeader1.addAll(user.getLstGroupOnlyLeader1());
            }
            if (user.getListGroupIdLDVof1() != null && user.getListGroupIdLDVof1().size() > 0) {
                lstGroupVoffice1.addAll(user.getListGroupIdLDVof1());
            }
            if (user.getListGroupIdVTVof1() != null && user.getListGroupIdVTVof1().size() > 0) {
                lstGroupSerecVoffice1.addAll(user.getListGroupIdVTVof1());
            }
            userIdVof1 = user.getUserId();
        }
        //Thong tin don vi tren vof2.0
        List<Long> lstGroupVoffice2 = new ArrayList<Long>();
        List<Long> lstGroupSerecVoffice2 = new ArrayList<Long>();
        List<Long> lstGroupOnlyLeader2 = new ArrayList<Long>();
        if (userVof2.getListManagementOrg() != null && userVof2.getListManagementOrg().size() > 0) {
            lstGroupOnlyLeader2.addAll(userVof2.getListManagementOrg());
        }
        if (userVof2.getLstVhrOrgNotSecretary() != null && userVof2.getLstVhrOrgNotSecretary().size() > 0) {
            lstGroupVoffice2.addAll(userVof2.getLstVhrOrgNotSecretary());
        }
        if (userVof2.getListSecretaryVhrOrg() != null && userVof2.getListSecretaryVhrOrg().size() > 0) {
            lstGroupSerecVoffice2.addAll(userVof2.getListSecretaryVhrOrg());
        }

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.IS_FINANCIAL,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.REGISTER_NUMBER,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.DESCRIPTION,
                ConstantsFieldParams.TYPE_ID,
                ConstantsFieldParams.AREA_ID,
                ConstantsFieldParams.STATE,
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TO_DATE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                ConstantsFieldParams.FILLTER_TYPE,
                ConstantsFieldParams.REQUISITION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay van ban tai chinh
            int isFinancial = -1;
            String strIsFinancial = listValue.get(1);
            if (!CommonUtils.isEmpty(strIsFinancial)) {
                isFinancial = Integer.parseInt(strIsFinancial);
            }
            // Loai van ban (ky duyet/ xet duyet/ban hanh)
            int type = 0;
            String strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
                type = Integer.parseInt(strType);
            }
            // Tu khoa tim kiem nhanh
            String keyword = listValue.get(3);
            // So dang ky
            String registerNumber = listValue.get(4);
            // Ma van ban
            String code = listValue.get(5);
            // Tieu de
            String title = listValue.get(6);
            // Noi dung
            String description = listValue.get(7);
            // Id the thuc van ban
            int typeId = -1;
            String strTypeId = listValue.get(8);
            if (!CommonUtils.isEmpty(strTypeId)) {
                typeId = Integer.parseInt(strTypeId);
            }
            // Id linh vuc van ban
            int areaId = -1;
            String strAreaId = listValue.get(9);
            if (!CommonUtils.isEmpty(strAreaId)) {
                areaId = Integer.parseInt(strAreaId);
            }
            // Trang thai
            int state;
            String strState = listValue.get(10);
            if (!CommonUtils.isEmpty(strState)) {
//                state = Integer.parseInt(strState);
            }
            // Tu ngay
            String fromDate = listValue.get(11);
            // Den ngay
            String toDate = listValue.get(12);

            // Vi tri lay ra
            Long startRecord = null;
            // So luong lay ra
            Long pageSize = null;
            // Tim kiem theo van ban co 1.0 hay khong
            String isSearchTextAll = listValue.get(15);
            // Loc tim kiem theo van ban KTTS
            Long filterType = null;
            String strFilterType = listValue.get(16);
            if (!CommonUtils.isEmpty(strFilterType)) {
                filterType = Long.parseLong(strFilterType);
            }
            // tuanld
            Long requisitionId = null;
            String strRequisitionId = listValue.get(17);
            if (!CommonUtils.isEmpty(strRequisitionId)) {
                requisitionId = Long.parseLong(strRequisitionId);
            }
            // Khai bao Map ky duyet
            Map<String, Integer> resStatusSignMap = new HashMap<String, Integer>();
            //Cho ky duyet
            resStatusSignMap.put(FieldText.countTextWaitSign,
                    Constants.TextProcess.State.NOT_HANDLE);
            //Da ky duyet
            resStatusSignMap.put(FieldText.countTextSigned,
                    Constants.TextProcess.State.LEADER_SIGNED);
            //Da tu choi ky
            resStatusSignMap.put(FieldText.countTextrejectd,
                    Constants.TextProcess.State.LEADER_REJECTED);
            //            // vÄƒn báº£n cÃ¡ nhÃ¢n kÃ½ bá»‹ lÃ£nh Ä‘áº¡o tá»« chá»‘i
            resStatusSignMap.put(FieldText.countTextSignRejectByLeader,
                    Constants.TextProcess.State.TEXT_SEARCH_TYPE_LEADER_DENNY_SIGNED);
//            //Tat ca khong bao gom van ban ca nha bi lanh dao tu choi
            resStatusSignMap.put(FieldText.countTextAll,
                    Constants.TextProcess.State.TEXT_SIGN_ALL_FOR_MOBILE);

//            //Cho ky nhay
            resStatusSignMap.put(FieldText.countTextWaitingInitial,
                    Constants.TextProcess.State.INITIAL_SIGNING);
//            //Da ky nhay
            resStatusSignMap.put(FieldText.countTextSignedInitial,
                    Constants.TextProcess.State.INITIAL_SIGNED);
//            //Da tu choi ky nhay
            resStatusSignMap.put(FieldText.countTextRejectInitial,
                    Constants.TextProcess.State.INITIAL_REJECTED);

            //Khai bao map doi tuong cho xet duyet
            Map<String, Integer> resStatusSecretaryMap = new HashMap<String, Integer>();
            //Tat ca xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryAll,
                    Constants.TextProcess.State.TEXT_SIGN_ALL);
            //Cho xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryWaitSign,
                    Constants.TextProcess.State.NOT_HANDLE);
            //Da xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretarySigned,
                    Constants.TextProcess.State.SECRETARY_SIGNED);
            //Da tu choi xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryReject,
                    Constants.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_VT_REJECTED);

// khai bÃ¡o danh sÃ¡ch thread thá»±c hiá»‡n count
            ExecutorService pool = Executors.newFixedThreadPool(resStatusSignMap
                    .size());
            List<ThreadCountTextByStatus> lstCountTextStatus = new ArrayList<TextThread.ThreadCountTextByStatus>();
            TextSearchDAO textSearch;
            ThreadCountTextByStatus thCountTextStatus;
            switch (type) {
                // Neu la loai van ban ky duyet
                case Constants.Text.SearchType.SIGNED:

                    for (Map.Entry<String, Integer> entry : resStatusSignMap.entrySet()) {
                        textSearch = new TextSearchDAO();
                        thCountTextStatus = new ThreadCountTextByStatus(userIdVof1, userIdVof2,
                                isFinancial, type, keyword, registerNumber, code,
                                title, description, typeId, areaId, entry.getValue(),
                                fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                                lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                                lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                                isSearchTextAll, textSearch, requisitionId);
                        lstCountTextStatus.add(thCountTextStatus);

                    }

                    break;
                // Neu la loai van ban xet duyet
                case Constants.Text.SearchType.SECRETARY_SIGN:
                    for (Map.Entry<String, Integer> entry : resStatusSecretaryMap.entrySet()) {
                        textSearch = new TextSearchDAO();
                        thCountTextStatus = new ThreadCountTextByStatus(userIdVof1, userIdVof2,
                                isFinancial, type, keyword, registerNumber, code,
                                title, description, typeId, areaId, entry.getValue(),
                                fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                                lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                                lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                                isSearchTextAll, textSearch, requisitionId);
                        lstCountTextStatus.add(thCountTextStatus);
                    }
                    break;
            }

            // thá»±c hiá»‡n cÃ¡c hÃ m Ä‘áº¿m sá»‘ lÆ°á»£ng trong thread
            for (int i = 0; i < lstCountTextStatus.size(); ++i) {
                pool.execute(lstCountTextStatus.get(i));
            }
            // Doi tat ca cac thread trong pool thuc hien xong thi shutdown pool
            pool.shutdown();
            // Shutdown pool neu sau 20s ma van con thread chua thuc hien xong
            pool.awaitTermination(Constants.DateTime.TIMEOUT_OF_THREAD_IN_SECOND_UNIT,
                    TimeUnit.SECONDS);
            EntityTextCountStatus textCount = new EntityTextCountStatus();
            Integer count;
            for (ThreadCountTextByStatus thread : lstCountTextStatus) {
                // Loai danh sach van ban
                type = thread.getType();
                // Trang thai van ban trong danh sach van ban nguoi dung nhan
                state = thread.getStatus();
                // So luong dem duoc
                count = thread.getCount();

                // Gan so luong van ban tuong ung voi tung loai va trang thai
                switch (type) {
                    // Neu la loai van ban ky duyet
                    case Constants.Text.SearchType.SIGNED:
                        switch (state) {
                            // Tat ca
                            case Constants.TextProcess.State.TEXT_SIGN_ALL_FOR_MOBILE:
//                                System.out.println("-------1.Tat ca --------------: " + count);
                                textCount.setCountTextAll(count);
                                break;
                            // Cho ky duyet
                            case Constants.TextProcess.State.NOT_HANDLE:
//                                System.out.println("-------2.Cho ky duyet --------------: " + count);
                                textCount.setCountTextWaitSign(count);
                                break;
                            // Da ky duyet
                            case Constants.TextProcess.State.LEADER_SIGNED:
//                                System.out.println("-------3.Da ky duyet --------------: " + count);
                                textCount.setCountTextSigned(count);
                                break;
                            // Tu choi ky duyet
                            case Constants.TextProcess.State.LEADER_REJECTED:
//                                System.out.println("-------4.Da tu choi --------------: " + count);
                                textCount.setCountTextrejectd(count);
                                break;
                            // Cho ky nhay
                            case Constants.TextProcess.State.INITIAL_SIGNING:
//                                System.out.println("-------5.Cho ky nhay--------------: " + count);
                                textCount.setCountTextWaitingInitial(count);
                                break;
                            // Da ky nhay
                            case Constants.TextProcess.State.INITIAL_SIGNED:
//                                System.out.println("-------6.Da ky nhay--------------: " + count);
                                textCount.setCountTextSignedInitial(count);
                                break;
                            // Da tu choi ky nhay
                            case Constants.TextProcess.State.INITIAL_REJECTED:
//                                System.out.println("-------7.Da tu choi ky nhay--------------: " + count);
                                textCount.setCountTextRejectInitial(count);
                                break;
                            // Van ban bi lanh dao tu choi
                            case Constants.TextProcess.State.TEXT_SEARCH_TYPE_LEADER_DENNY_SIGNED:
//                                System.out.println("-------8.Bi lanh dao tu choi--------------: " + count);
                                textCount.setCountTextSignRejectByLeader(count);
                                break;
                        }
                        break;
                    // Neu la loai van ban xet duyet
                    case Constants.Text.SearchType.SECRETARY_SIGN:
                        switch (state) {
                            // Tat ca
                            case Constants.TextProcess.State.TEXT_SIGN_ALL:
                                textCount.setCountTextSecretaryAll(count);
                                break;
                            // Cho xet duyet
                            case Constants.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_NOT_RESPOND:
                                textCount.setCountTextSecretaryWaitSign(count);
                                break;
                            // Da xet duyet
                            case Constants.TextProcess.State.SECRETARY_SIGNED:
                                textCount.setCountTextSecretarySigned(count);
                                break;
                            // Tu choi xet duyet
                            case Constants.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_VT_REJECTED:
                                textCount.setCountTextSecretaryReject(count);
                                break;
                        }
                        break;
                }
            }
            result = textCount;
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, ex.getMessage(), data, isSecurity);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Dem thong ke so luong cac loai van ban tren menu trai</b><br>
     * + Dem so luong van ban cho ky duyet + Dem so luong van ban cho xet duyet
     * + Dem so luong van ban chua xu ly + Dem so luong van ban cho ky tai chinh
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String getCountHome(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        Object result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        List<Long> lstGroupVoffice1 = new ArrayList<Long>();
        List<Long> lstGroupSerecVoffice1 = new ArrayList<Long>();
        List<Long> lstGroupOnlyLeader1 = new ArrayList<Long>();
        if (user != null && user.getUserId() != null) {
            if (user.getLstGroupOnlyLeader1() != null && user.getLstGroupOnlyLeader1().size() > 0) {
                lstGroupOnlyLeader1.addAll(user.getLstGroupOnlyLeader1());
            }
            if (user.getListGroupIdLDVof1() != null && user.getListGroupIdLDVof1().size() > 0) {
                lstGroupVoffice1.addAll(user.getListGroupIdLDVof1());
            }
            if (user.getListGroupIdVTVof1() != null && user.getListGroupIdVTVof1().size() > 0) {
                lstGroupSerecVoffice1.addAll(user.getListGroupIdVTVof1());
            }

            userIdVof1 = user.getUserId();
        }
        //Thong tin don vi tren vof2.0
        List<Long> lstGroupVoffice2 = new ArrayList<Long>();
        List<Long> lstGroupSerecVoffice2 = new ArrayList<Long>();
        List<Long> lstGroupOnlyLeader2 = new ArrayList<Long>();
        if (userVof2.getListManagementOrg() != null && userVof2.getListManagementOrg().size() > 0) {
            lstGroupOnlyLeader2.addAll(userVof2.getListManagementOrg());
        }
        if (userVof2.getLstVhrOrgNotSecretary() != null && userVof2.getLstVhrOrgNotSecretary().size() > 0) {
            lstGroupVoffice2.addAll(userVof2.getLstVhrOrgNotSecretary());
        }
        if (userVof2.getListSecretaryVhrOrg() != null && userVof2.getListSecretaryVhrOrg().size() > 0) {
            lstGroupSerecVoffice2.addAll(userVof2.getListSecretaryVhrOrg());
        }

        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.IS_FINANCIAL,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.REGISTER_NUMBER,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.DESCRIPTION,
                ConstantsFieldParams.TYPE_ID,
                ConstantsFieldParams.AREA_ID,
                ConstantsFieldParams.STATE,
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TO_DATE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                ConstantsFieldParams.FILLTER_TYPE,
                ConstantsFieldParams.REQUISITION_ID,};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay van ban tai chinh
            int isFinancial;
            String strIsFinancial = listValue.get(1);
            if (!CommonUtils.isEmpty(strIsFinancial)) {
//                isFinancial = Integer.parseInt(strIsFinancial);
            }
            // Loai van ban (ky duyet/ xet duyet/ban hanh)
            int type;
            String strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
//                type = Integer.parseInt(strType);
            }
            // Tu khoa tim kiem nhanh
            String keyword = listValue.get(3);
            // So dang ky
            String registerNumber = listValue.get(4);
            // Ma van ban
            String code = listValue.get(5);
            // Tieu de
            String title = listValue.get(6);
            // Noi dung
            String description = listValue.get(7);
            // Id the thuc van ban
            int typeId = -1;
            String strTypeId = listValue.get(8);
            if (!CommonUtils.isEmpty(strTypeId)) {
                typeId = Integer.parseInt(strTypeId);
            }
            // Id linh vuc van ban
            int areaId = -1;
            String strAreaId = listValue.get(9);
            if (!CommonUtils.isEmpty(strAreaId)) {
                areaId = Integer.parseInt(strAreaId);
            }
            // Trang thai
            int state;
            String strState = listValue.get(10);
            if (!CommonUtils.isEmpty(strState)) {
//                state = Integer.parseInt(strState);
            }
            // Tu ngay
            String fromDate = listValue.get(11);
            // Den ngay
            String toDate = listValue.get(12);

            // Vi tri lay ra
            Long startRecord = null;
            // So luong lay ra
            Long pageSize = null;
            // Tim kiem theo van ban co 1.0 hay khong
            String isSearchTextAll = listValue.get(15);
            // Loc tim kiem theo van ban KTTS
            Long filterType = null;
            String strFilterType = listValue.get(16);
            if (!CommonUtils.isEmpty(strFilterType)) {
                filterType = Long.parseLong(strFilterType);
            }

            // tuanld tim kiem theo id van ban 
            Long requisitionId = null;
            String strRequisitionId = listValue.get(17);
            if (!CommonUtils.isEmpty(strRequisitionId)) {
                requisitionId = Long.parseLong(strRequisitionId);
            }
            // Khai bao Map ky duyet
            Map<String, Integer> resStatusSignMap = new HashMap<String, Integer>();
            // Cho ky duyet
            resStatusSignMap.put(FieldText.countTextWaitSign,
                    Constants.TextProcess.State.NOT_HANDLE);
            // Da ky duyet
            resStatusSignMap.put(FieldText.countTextSigned,
                    Constants.TextProcess.State.LEADER_SIGNED);
            // Da tu choi ky
            resStatusSignMap.put(FieldText.countTextrejectd,
                    Constants.TextProcess.State.LEADER_REJECTED);
            // Cho ky nhay
            resStatusSignMap.put(FieldText.countTextWaitingInitial,
                    Constants.TextProcess.State.INITIAL_SIGNING);
            // Da ky nhay
            resStatusSignMap.put(FieldText.countTextSignedInitial,
                    Constants.TextProcess.State.INITIAL_SIGNED);
            // Da tu choi ky nhay
            resStatusSignMap.put(FieldText.countTextRejectInitial,
                    Constants.TextProcess.State.INITIAL_REJECTED);
            // Van ban ca nhan ky bi lanh dao tu choi
            resStatusSignMap.put(FieldText.countTextSignRejectByLeader,
                    Constants.TextProcess.State.TEXT_SEARCH_TYPE_LEADER_DENNY_SIGNED);
            // Tat ca khong bao gom van ban ca nha bi lanh dao tu choi
            resStatusSignMap.put(FieldText.countTextAll,
                    Constants.TextProcess.State.TEXT_SIGN_ALL_FOR_MOBILE);

            // Khai bao map doi tuong cho xet duyet
            Map<String, Integer> resStatusSecretaryMap = new HashMap<String, Integer>();
            // Tat ca xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryAll,
                    Constants.TextProcess.State.TEXT_SIGN_ALL);
            // Cho xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryWaitSign,
                    Constants.TextProcess.State.NOT_HANDLE);
            // Da xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretarySigned,
                    Constants.TextProcess.State.SECRETARY_SIGNED);
            // Da tu choi xet duyet
            resStatusSecretaryMap.put(FieldText.countTextSecretaryReject,
                    Constants.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_VT_REJECTED);

            // Khai bÃ¡o danh sÃ¡ch thread thá»±c hiá»‡n count
            TextSearchDAO textSearch;
            ThreadCountTextByStatus thCountTextStatus;
            List<ThreadCountTextByStatus> lstCountTextStatus = new ArrayList<TextThread.ThreadCountTextByStatus>();
            //Theard cong van chua xu ly
            DocumentDAO documentDAO = new DocumentDAO();
            DocumentThread.DocumentCountThread threadDoc;
            ExecutorService pool = Executors.newFixedThreadPool(resStatusSignMap
                    .size());

            //1. Dem so luong van ban cho ky duyet
            // state =0, type =3,isFinancial=0
            textSearch = new TextSearchDAO();
            thCountTextStatus = new ThreadCountTextByStatus(userIdVof1, userIdVof2,
                    0, Constants.Text.SearchType.SIGNED, keyword, registerNumber, code,
                    title, description, typeId, areaId, Constants.TextProcess.State.NOT_HANDLE,
                    fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                    lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                    isSearchTextAll, textSearch, requisitionId);
            lstCountTextStatus.add(thCountTextStatus);
            //2. Dem so luong van ban cho ky tai chinh
            // state =0, type =3, isFinancial=1
            textSearch = new TextSearchDAO();
            thCountTextStatus = new ThreadCountTextByStatus(userIdVof1, userIdVof2,
                    1, Constants.Text.SearchType.SIGNED, keyword, registerNumber, code,
                    title, description, typeId, areaId, Constants.TextProcess.State.NOT_HANDLE,
                    fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                    lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                    isSearchTextAll, textSearch, requisitionId);
            lstCountTextStatus.add(thCountTextStatus);
            //3. Dem so luong van ban xet duyet
            // state =0, type =2, isFinancial=-1
            if (lstGroupSerecVoffice1.size() > 0
                    || lstGroupSerecVoffice2.size() > 0) {
                textSearch = new TextSearchDAO();
                thCountTextStatus = new ThreadCountTextByStatus(userIdVof1, userIdVof2,
                        -1, Constants.Text.SearchType.SECRETARY_SIGN, keyword, registerNumber, code,
                        title, description, typeId, areaId, Constants.TextProcess.State.NOT_HANDLE,
                        fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                        lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                        lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                        isSearchTextAll, textSearch, requisitionId);

                lstCountTextStatus.add(thCountTextStatus);
            }
            //4. Dem so luong cong van chua xu ly
            // Dem van ban nguoi dung nhan duoc va chua xu ly
            threadDoc = new DocumentThread.DocumentCountThread(userIdVof1, userIdVof2,
                    Constants.Document.Type.RECEIVER,
                    Constants.Document.Status.NEW, documentDAO, null);
            // thá»±c hiá»‡n cÃ¡c hÃ m Ä‘áº¿m sá»‘ lÆ°á»£ng trong thread
            for (int i = 0; i < lstCountTextStatus.size(); ++i) {
                pool.execute(lstCountTextStatus.get(i));
            }
            // Thuc hien goi ham lay so luong van ban chua xu ly
            pool.execute(threadDoc);
            // Doi tat ca cac thread trong pool thuc hien xong thi shutdown pool
            pool.shutdown();
            // Shutdown pool neu sau 20s ma van con thread chua thuc hien xong
            pool.awaitTermination(Constants.DateTime.TIMEOUT_OF_THREAD_IN_SECOND_UNIT,
                    TimeUnit.SECONDS);
            EntityTextCountStatus textCount = new EntityTextCountStatus();
            Integer count;
            for (ThreadCountTextByStatus thread : lstCountTextStatus) {
                // Loai danh sach van ban
                type = thread.getType();
                // Trang thai van ban trong danh sach van ban nguoi dung nhan
                state = thread.getStatus();
                // So luong dem duoc
                count = thread.getCount();
                //Loai tai chinh 
                isFinancial = thread.getFinancial();
                // Gan so luong van ban tuong ung voi tung loai va trang thai
                switch (type) {
                    // Neu la loai van ban ky duyet
                    case Constants.Text.SearchType.SIGNED:
                        switch (state) {
                            // Cho ky duyet khong bao gom tai chinh
                            case Constants.TextProcess.State.NOT_HANDLE:
                                if (isFinancial == 0) {
                                    textCount.setCountTextWaitSign(count);
                                }
                                if (isFinancial == 1) {
                                    textCount.setCountTextWaitSignMoney(count);
                                }
                                break;
                        }
                        break;
                    // Neu la loai van ban xet duyet
                    case Constants.Text.SearchType.SECRETARY_SIGN:
                        switch (state) {
                            // Cho xet duyet
                            case Constants.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_NOT_RESPOND:
                                textCount.setCountTextSecretaryWaitSign(count);
                                break;
                        }
                        break;
                }
            }
            //So luong van ban chua xy ly
            textCount.setDocProcessing(threadDoc.getCount());
            result = textCount;
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Ham lay so luong cac box modul ky dien tu trang chu web</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountTextDashboard(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            LOGGER.error("getCountTextDashboard (Lay so luong van ban cho man hinh trang chu)"
                    + " - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();

        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        List<Long> lstGroupVoffice1 = new ArrayList<>();
        List<Long> lstGroupSerecVoffice1 = new ArrayList<>();
        List<Long> lstGroupOnlyLeader1 = new ArrayList<>();
        if (user != null && user.getUserId() != null) {
            // chua lay duoc cac don vi ma user dang nhap co quyen nhan vien
            if (user.getLstGroupOnlyLeader1() != null && user.getLstGroupOnlyLeader1().size() > 0) {
                lstGroupOnlyLeader1.addAll(user.getLstGroupOnlyLeader1());
            }
            if (user.getListGroupIdLDVof1() != null && user.getListGroupIdLDVof1().size() > 0) {
                lstGroupVoffice1.addAll(user.getListGroupIdLDVof1());
            }
            if (user.getListGroupIdVTVof1() != null && user.getListGroupIdVTVof1().size() > 0) {
                lstGroupSerecVoffice1.addAll(user.getListGroupIdVTVof1());
            }
            userIdVof1 = user.getUserId();
        }
        // Thong tin don vi tren vof2.0
        List<Long> lstGroupVoffice2 = new ArrayList<>();
        List<Long> lstGroupSerecVoffice2 = new ArrayList<>();
        List<Long> lstGroupOnlyLeader2 = new ArrayList<>();
        List<Long> lstGroupRoleEmployee2 = new ArrayList<>();
        if (userVof2.getListEmployeeOrg() != null && userVof2.getListEmployeeOrg().size() > 0) {
            lstGroupRoleEmployee2.addAll(userVof2.getListEmployeeOrg());
        }
        if (userVof2.getListManagementOrg() != null && userVof2.getListManagementOrg().size() > 0) {
            lstGroupOnlyLeader2.addAll(userVof2.getListManagementOrg());
        }
        if (userVof2.getLstVhrOrgNotSecretary() != null && userVof2.getLstVhrOrgNotSecretary().size() > 0) {
            lstGroupVoffice2.addAll(userVof2.getLstVhrOrgNotSecretary());
        }
        if (userVof2.getListSecretaryVhrOrg() != null && userVof2.getListSecretaryVhrOrg().size() > 0) {
            lstGroupSerecVoffice2.addAll(userVof2.getListSecretaryVhrOrg());
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.IS_COUNT,
                ConstantsFieldParams.IS_FINANCIAL,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.REGISTER_NUMBER,
                ConstantsFieldParams.CODE,
                ConstantsFieldParams.TITLE,
                ConstantsFieldParams.DESCRIPTION,
                ConstantsFieldParams.TYPE_ID,
                ConstantsFieldParams.AREA_ID,
                ConstantsFieldParams.STATE,
                ConstantsFieldParams.FROM_DATE,
                ConstantsFieldParams.TO_DATE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE,
                ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                ConstantsFieldParams.FILLTER_TYPE,
                ConstantsFieldParams.REQUISITION_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay van ban tai chinh
            int isFinancial = -1;
            String strIsFinancial = listValue.get(1);
            if (!CommonUtils.isEmpty(strIsFinancial)) {
                isFinancial = Integer.parseInt(strIsFinancial);
            }
            // Loai van ban (ky duyet/ xet duyet/ban hanh)
            int type;
            String strType = listValue.get(2);
            if (!CommonUtils.isEmpty(strType)) {
//                type = Integer.parseInt(strType);
            }
            // Tu khoa tim kiem nhanh
            String keyword = listValue.get(3);
            // So dang ky
            String registerNumber = listValue.get(4);
            // Ma van ban
            String code = listValue.get(5);
            // Tieu de
            String title = listValue.get(6);
            // Noi dung
            String description = listValue.get(7);
            // Id the thuc van ban
            int typeId = -1;
            String strTypeId = listValue.get(8);
            if (!CommonUtils.isEmpty(strTypeId)) {
                typeId = Integer.parseInt(strTypeId);
            }
            // Id linh vuc van ban
            int areaId = -1;
            String strAreaId = listValue.get(9);
            if (!CommonUtils.isEmpty(strAreaId)) {
                areaId = Integer.parseInt(strAreaId);
            }
            // Trang thai
            int state;
            String strState = listValue.get(10);
            if (!CommonUtils.isEmpty(strState)) {
//                state = Integer.parseInt(strState);
            }
            // Tu ngay
            String fromDate = listValue.get(11);
            // Den ngay
            String toDate = listValue.get(12);

            // Vi tri lay ra
            Long startRecord = null;
            // So luong lay ra
            Long pageSize = null;
            // Tim kiem theo van ban co 1.0 hay khong
            String isSearchTextAll = listValue.get(15);
            // Loc tim kiem theo van ban KTTS
            Long filterType = null;
            String strFilterType = listValue.get(16);
            if (!CommonUtils.isEmpty(strFilterType)) {
                filterType = Long.parseLong(strFilterType);
            }

            Long requisitionId = null;
            String strRequisitionId = listValue.get(17);
            if (!CommonUtils.isEmpty(strRequisitionId)) {
                requisitionId = Long.parseLong(strRequisitionId);
            }

            EntityTextCountStatus textCount = new EntityTextCountStatus();
            TextSearchDAO textSearchDAO = new TextSearchDAO();
            // Neu user co quyen van thu
            // -> Dem so luong van ban cho xet duyet
            if (lstGroupSerecVoffice2.size() > 0) {
                type = Constants.Text.SearchType.SECRETARY_SIGN;
                state = Constants.TextProcess.State.NOT_HANDLE;
                int count;
                Object countText = textSearchDAO.searchText(userIdVof1, userIdVof2, "1",
                        isFinancial, type, keyword, registerNumber, code,
                        title, description, typeId, areaId, state,
                        fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                        lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                        lstGroupSerecVoffice2, startRecord, pageSize, filterType, 
                        isSearchTextAll, requisitionId, null);
                try {
                    count = Integer.parseInt(countText.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
//                    e.printStackTrace();
                    count = -1;
                }
                textCount.setCountTextSecretaryWaitSign(count);
            }
            // Neu la nhan vien
            // Dem so luong van ban cho ky duyet
            if (lstGroupSerecVoffice2.isEmpty() || lstGroupRoleEmployee2.size() > 0
                    || lstGroupOnlyLeader2.size() > 0 || lstGroupVoffice2.size() > 0) {
                type = Constants.Text.SearchType.SIGNED;
                state = Constants.TextProcess.State.NOT_HANDLE;
                int count1;
                Object countText1 = textSearchDAO.searchText(userIdVof1, userIdVof2, "1",
                        isFinancial, type, keyword, registerNumber, code,
                        title, description, typeId, areaId, state,
                        fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                        lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                        lstGroupSerecVoffice2, startRecord, pageSize, filterType, 
                        isSearchTextAll, requisitionId, null);
                try {
                    count1 = Integer.parseInt(countText1.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    count1 = -1;
                }
                textCount.setCountTextWaitSign(count1);
            }

            // Dem so luong van ban cho ky nhay
            type = Constants.Text.SearchType.SIGNED;
            state = Constants.TextProcess.State.INITIAL_SIGNING;
            int count2;
            Object countText2 = textSearchDAO.searchText(userIdVof1, userIdVof2, "1",
                    isFinancial, type, keyword, registerNumber, code,
                    title, description, typeId, areaId, state,
                    fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                    lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                    lstGroupSerecVoffice2, startRecord, pageSize, filterType,
                    isSearchTextAll, requisitionId, null);
            try {
                count2 = Integer.parseInt(countText2.toString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
//                e.printStackTrace();
                count2 = -1;
            }
            textCount.setCountTextWaitingInitial(count2);
            // Neu la van thu hoac lanh dao/thu truong thi co quyen ban hanh
            // Dem so luong van ban cho ban hanh (da ky)
            if (lstGroupOnlyLeader2.size() > 0 || lstGroupSerecVoffice2.size() > 0) {
                type = Constants.Text.SearchType.PUBLISHED_SIGN;
                state = Constants.Text.State.APPROVED;
                int count3;
                Object countText3 = textSearchDAO.searchText(userIdVof1, userIdVof2, "1",
                        isFinancial, type, keyword, registerNumber, code,
                        title, description, typeId, areaId, state,
                        fromDate, toDate, lstGroupVoffice1, lstGroupVoffice2,
                        lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSerecVoffice1,
                        lstGroupSerecVoffice2, startRecord, pageSize, filterType, 
                        isSearchTextAll, requisitionId, null);
                try {
                    count3 = Integer.parseInt(countText3.toString());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
//                    e.printStackTrace();
                    count3 = -1;
                }
                textCount.setCountTextSigned(count3);
            }
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, textCount, aesKey);
        } // Loi server
        catch (Exception ex) {
            LOGGER.error("getCountTextDashboard (Lay so luong van ban cho man hinh trang chu)"
                    + " - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String synchonizeCertificate(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Kiem tra session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        // Kiem tra xem co ma hoa du lieu ko
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Lay du lieu client gui len
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{ConstantsFieldParams.STAFF_CERT_INFO};
            List<String> values = FunctionCommon.getValuesFromJSON(json, keys);
            if ((values != null) && (values.size() > 0)) {
                Gson gson = new Gson();
                EntityUser user = gson.fromJson(values.get(0), EntityUser.class);
                if ((user != null) && (user.getUserId() != null)
                        && (user.getStrCANumber() != null) && (user.getSimVersion() != null)) {
                    boolean valid = false;
                    SequenceNumber.getInstance(new SequenceId());
                    Synchronization synchronization = Synchronization.getInstance();
//                    String prefixCode = user.getStrEmail();
//                    if (prefixCode.indexOf("@") > 0) {
//                        prefixCode = prefixCode.substring(0, prefixCode.indexOf("@"));
//                    }
                    String prefixCode = user.getUserId().toString();
                    String serial = synchronization.synchonizeCertificate(prefixCode, user.getStrCANumber(), user.getSimVersion());
                    if (serial != null) {
                        boolean updatable = true;
                        UserDAO userDAO = new UserDAO();
                        Vof2_EntityUser staff = userDAO.vof2_getUserInforVHRById(user.getUserId());
                        if (staff == null) {
//                            staff.setStrCANumber(user.getStrCANumber());
                            updatable = false;
                        } else {
                            staff.setCaSerial(serial);
                            staff.setStrCANumber(user.getStrCANumber());
                            staff.setSimCaVersion(user.getSimVersion());
                        }

                        valid = userDAO.synchronizeStaffInformation(staff, updatable);
                    }
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, valid ? synchronization.getErrorCode() : -1, aesKey);
                } else {
//                    System.out.println("Loi: ");
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }
            } else {
//                System.out.println("Loi2: ");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("synchonizeCertificate - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCertificateSynchronization(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Kiem tra session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
        // Kiem tra xem co ma hoa du lieu ko
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String userCardId = userGroup.getCardId();
        log.setUserName(userCardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            // Lay du lieu client gui len
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{ConstantsFieldParams.STAFF_CARD_ID};
            List<String> values = FunctionCommon.getValuesFromJSON(json, keys);
            if ((values != null) && (values.size() > 0)) {
                String cardId = values.get(0);
                UserDAO userDAO = new UserDAO();
//                EntityUser staff = userDAO.getUserInforByCardId(cardId);
                Vof2_EntityUser user = userDAO.vof2_GetUserInforVHRByCardId(cardId);
//                if ((user != null) && (user.getStrCardNumber() != null) && (user.getStrEmail() != null)
//                        && (user.getStrCANumber() != null) && (user.getSimVersion() != null)) {

                if (user != null) {
                    EntityUser staff = new EntityUser();
                    staff.setUserId(user.getUserId());
                    staff.setStrCASerial(user.getCaSerial());
                    staff.setStrCANumber(user.getStrCANumber());
                    staff.setSimVersion(user.getSimCaVersion());
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, staff, aesKey);

                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (JSONException ex) {
            LOGGER.error("getCertificateSynchronization - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * lay so dang ky van ban tu dong
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListRegisterNumber(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getListRegisterNumber - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getListRegisterNumber - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                // Lay ma nhan vien
                String cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{ConstantsFieldParams.ORG_ID, ConstantsFieldParams.TYPE_ID};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strOrgId = listValue.get(0);//id don vi ban hanh
                    String strTypeId = listValue.get(1);//hinh thuc van ban
                    if (!CommonUtils.isEmpty(strOrgId) && !CommonUtils.isEmpty(strTypeId)) {
                        Long orgId = Long.parseLong(strOrgId);
                        Long typeId = Long.parseLong(strTypeId);
                        //lay thong tin chi tiet van ban
                        TextCommonDAO textCommonDAO = new TextCommonDAO();
                        List<EntityText> lstRegNumber = textCommonDAO.getListRegisterNumber(orgId, typeId, null);
                        if (!CommonUtils.isEmpty(lstRegNumber)) {
                            //thanh cong
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstRegNumber, aesKey);
                        } else {
                            //khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getListRegisterNumber - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * <b>Lay chi tiet van ban trinh ky su dung thread</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String getTextDetailThread(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {
        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (userGroup.getCheckSessionOk()) {
            String cardId = "";
            try {
                // Session timeout
                if (userGroup.getVof2_ItemEntityUser() == null) {
                    LOGGER.error("getTextDetailThread (Lay chi tiet van ban trinh ky)"
                            + " - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Lay userId tren he thong 1
                Long sysUserIdVof1 = null;
                if (userGroup.getItemEntityUser() != null) {
                    sysUserIdVof1 = userGroup.getItemEntityUser().getUserId();
                }
                // Thong tin don vi tren vof2.0
                Long sysUserIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
                if (sysUserIdVof2 == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW,
                            null, null);
                }
                // Kiem tra xem co ma hoa du lieu ko
                String strAesKeyDecode = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    strAesKeyDecode = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(strAesKeyDecode, data);
                }
                // Lay ma nhan vien
                cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                JSONObject json = new JSONObject(data);
                String[] keys = new String[]{
                    ConstantsFieldParams.TEXT_ID,
                    ConstantsFieldParams.IS_LISTFILE,
                    ConstantsFieldParams.TYPE,
                    ConstantsFieldParams.IS_SEARCH_TEXT_ALL,
                    ConstantsFieldParams.IS_VERSION_NEW
                };
                List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                Long textId = Long.parseLong(listValue.get(0));
                Long isListFile = Long.parseLong(listValue.get(1));
                String type = listValue.get(2);
                String isSearchAll = listValue.get(3);
                String isVersionNew = listValue.get(4);

                EntityText result;
                // Tao pool quan ly thread co kich thuoc bang so luong thread da tao o tren
                ExecutorService pool;
                TextDetailThread textDetailThread;//thread lay chi tiet van ban trinh ky
                TextSignerThread textSignerThread;//thread lay danh sach nguoi ky
                TextDrafSignerThread textDrafSignerThread;//lay danh sach ky nhay
                TextAttachFileSignThread textAttachFileSignThread;//thread lay danh sach file ky
                TextAttachFileOtherThread textAttachFileOtherThread;//thread lay danh sach file dinh kem
                TextAttachFromDocumentThread textAttachFromDocumentThread;//thread lay danh sach cong van dinh kem
                TextGetHistoryRejectText textHistoryRejectThread;
                if (isListFile.equals(1L)) {
                    //neu la chi lay file van ban trinh ky
                    textDetailThread = null;
                    textSignerThread = null;
                    textDrafSignerThread = null;
                    textHistoryRejectThread = null;
                    pool = Executors.newFixedThreadPool(3);
                    // Thuc thi tung thread trong pool
                    //thread lay danh sach file ky
                    textAttachFileSignThread = new TextAttachFileSignThread(textId);
                    pool.execute(textAttachFileSignThread);

                    //thread lay danh sach file dinh kem
                    textAttachFileOtherThread = new TextAttachFileOtherThread(textId, sysUserIdVof2);
                    pool.execute(textAttachFileOtherThread);

                    //thread lay danh sach cong van dinh kem
                    textAttachFromDocumentThread = new TextAttachFromDocumentThread(textId, sysUserIdVof2, isVersionNew);
                    pool.execute(textAttachFromDocumentThread);

                } else {
                    //lay ca chi tiet va file
                    pool = Executors.newFixedThreadPool(7);
                    // Thuc thi tung thread trong pool
                    //thread lay chi tiet van ban trinh ky
                    textDetailThread = new TextDetailThread(textId,sysUserIdVof2);
                    pool.execute(textDetailThread);

                    //thread lay danh sach nguoi ky
                    textSignerThread = new TextSignerThread(textId, sysUserIdVof2, type);
                    pool.execute(textSignerThread);

                    //thread lay danh sach nguoi chuyen ky nhay
                    textDrafSignerThread = new TextDrafSignerThread(textId, sysUserIdVof1, sysUserIdVof2, isSearchAll, userGroup);
                    pool.execute(textDrafSignerThread);

                    //thread lay danh sach file ky
                    textAttachFileSignThread = new TextAttachFileSignThread(textId);
                    pool.execute(textAttachFileSignThread);

                    //thread lay danh sach file dinh kem
                    textAttachFileOtherThread = new TextAttachFileOtherThread(textId, sysUserIdVof2);
                    pool.execute(textAttachFileOtherThread);

                    //thread lay danh sach cong van dinh kem
                    textAttachFromDocumentThread = new TextAttachFromDocumentThread(textId, sysUserIdVof2, isVersionNew);
                    pool.execute(textAttachFromDocumentThread);

                    //thread lay lich su tu choi ky
                    textHistoryRejectThread = new TextGetHistoryRejectText(textId, sysUserIdVof2);
                    pool.execute(textHistoryRejectThread);
                }
                // Doi tat ca thread trong pool thuc hien xong thi shutdown pool
                // (Neu khong shutdown se dan den hien tuong cac thread trong pool van duoc duy tri
                // mac du da thuc hien xong cong viec va khi co request moi lai thuc hien tao pool
                // va thread lam cho so luong thread tang dot bien, cao tai server)
                pool.shutdown();
                // Sau 20s ma van con thread chua thuc hien xong
                // -> Thuc hien kill thread va shutdown pool
                pool.awaitTermination(Constants.DateTime.TIMEOUT_OF_THREAD_IN_SECOND_UNIT,
                        TimeUnit.SECONDS);

                //lay du lieu tu thread vao data
                if (textDetailThread != null) {
                    //neu la lay ca chi tiet va file
                    result = textDetailThread.getText();
                    result.setIsProcessText(0);
                     //datnv5: tra ve them truong hop  xac dinh van ban dang trong thoi gian xu ly cua tgd
                    TextSearchDAO textSearchDAO = new TextSearchDAO();
                    boolean isRejectDuringSign = textSearchDAO.getEmpIdConfigRejectAffterSign(sysUserIdVof2);
                    if(isRejectDuringSign){
                        //neu la tai khoan duoc tu choi van ban sau khi ky
                        double valueConfig = 15;
                        if(result.getMinuteDuringTime() !=null && isRejectDuringSign){
                            double valueTimeMinute = result.getMinuteDuringTime();
                            String valueTimeConfig = FunctionCommon.getPropertiesValue("text.signed.intimeprocess");
                            if(valueTimeConfig!=null && valueTimeConfig.trim().length()>0){
                                try {
                                    valueConfig = Double.valueOf(valueTimeConfig);
                                } catch (NumberFormatException e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                            if(valueTimeMinute < valueConfig){
                                result.setIsProcessText(1);
                            }
                        }
                    }
                } else {
                    //neu chi lay file
                    result = new EntityText();
                }
                if (result != null) {
                    //lay thong tin nguoi ky
                    if (textSignerThread != null) {
                        EntityText textTmp = textSignerThread.getText();
                        if (textTmp != null) {
                            result.setIsCreateSignWeb2(textTmp.getIsCreateSignWeb2());
                            result.setListSubmitter(textTmp.getListSubmitter());
                            result.setListInnitialSigner(textTmp.getListInnitialSigner());
                            result.setListReviewer(textTmp.getListReviewer());
                        }
                    }
                    //lay thong tin nguoi ky nhay
                    if (textDrafSignerThread != null) {
                        EntityText textTmp = textDrafSignerThread.getText();
                        if (textTmp != null) {
                            result.setLstStaffSend(textTmp.getLstStaffSend());
                            result.setAssignerName(textTmp.getAssignerName());
                            result.setAssignerEmail(textTmp.getAssignerEmail());
                            result.setAssignerMobile(textTmp.getAssignerMobile());
                            result.setAssignerComment(textTmp.getAssignerComment());
                            result.setListTextReceiverGroup(textTmp.getListTextReceiverGroup());
                            result.setListMeeting(textTmp.getListMeeting());
                        }
                    }
                    //lay thong tin lich su tu choi
                    if (textHistoryRejectThread != null) {
                        List<EntityTextRejectBefor> listReject = textHistoryRejectThread.getListReject();
                        if (listReject != null) {
                            result.setLstTextRejectBefor(listReject);
                        }
                    }

                    result.setFileMainSign(textAttachFileSignThread.getLstAttach());
                    result.setFileAttachFromSign(textAttachFileOtherThread.getLstAttach());
                    result.setFileAttachFromDoc(textAttachFromDocumentThread.getLstText());
                    AttachDAO attachDAO = new AttachDAO();
                    result.setListAttachTemplate(attachDAO.getListAttachTemplate(
                            sysUserIdVof2, textId, true));
                    // Lay thong tin cong bo tu dong
                    if (type != null && "0".equals(type)) {
                        //Neu la xem chi tiet tren man hinh trinh ky web
                        if (result.getAutoPublicText() != null && "1".equals(result.getAutoPublicText())) {
                            //Kiem tra neu van ban la cong bo thu dong thi lay thong tin
                            DocumentPublishedTmpDAO documentPublishedTmpDAO = new DocumentPublishedTmpDAO();
                            EntityDocumentPublishedTmp documentPublishedTmp = documentPublishedTmpDAO.getDocumentPublishedTmpByTextId(sysUserIdVof2, textId);

                            //lay thong tin sua tu dong cong bo
                            DocOrgRepublishDAO docOrgRepublishDAO = new DocOrgRepublishDAO();
                            EntityDocument tmpBaseDoc = docOrgRepublishDAO.getBaseDocData(textId, null);
                            if (tmpBaseDoc != null) {
                                documentPublishedTmp.setBaseDocument(tmpBaseDoc);
                            }
                            List<EntityVhrOrg> tmpOrganizationAdd = docOrgRepublishDAO.getOganizationAdded(textId, null);
                            if (tmpOrganizationAdd != null && tmpOrganizationAdd.size() > 0) {
                                documentPublishedTmp.setListOrgNeedRepublish(tmpOrganizationAdd);
                            }
                            String tmpDeadLine = docOrgRepublishDAO.getDeadLine(textId, null);
                            if (tmpDeadLine != null) {
                                documentPublishedTmp.setDeadLineRepublish(tmpDeadLine);
                            }
                            //ket thuc sua thong tin cong bo
                            result.setDocumentPublishedTmp(documentPublishedTmp);
                        }
                    }
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
            } catch (Exception ex) {
                LOGGER.error("getTextDetailThread (Lay chi tiet van ban trinh ky) - Exception"
                        + " - username: " + cardId + "\ndata: " + data, ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
        } else {
            LOGGER.error("getTextDetailThread (Lay chi tiet van ban trinh ky) - Session timeout!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }
    }

    /**
     * lay danh sach nguoi dang cho ky cho SMO
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSignerNext(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getListSignerNext - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getListSignerNext - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                // Lay ma nhan vien
                String cardId = userGroup.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(data);
                LogUtils.logFunctionalStart(log);
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{
                        ConstantsFieldParams.TEXT_ID,
                        ConstantsFieldParams.EMP_ID};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strTextId = listValue.get(0);//id van ban
                    String strEmpId = listValue.get(1);//id user vua ky
                    Long signerId = user2.getUserId();//Hien tai bien nay chua dung den, cu tao san the nay thoi, sau dung thi dung
                    if (!CommonUtils.isEmpty(strEmpId)) {
                        signerId = Long.parseLong(strEmpId);
                    }
                    if (!CommonUtils.isEmpty(strTextId)) {
                        Long textId = Long.parseLong(strTextId);
                        //signerId = Long.parseLong(strEmpId);
                        //lay thong tin chi tiet van ban
                        TextCommonDAO textCommonDAO = new TextCommonDAO();
                        List<EntityText> listUser = textCommonDAO.getListSignerNext(textId, signerId);
                        if (!CommonUtils.isEmpty(listUser)) {
                            //Convert danh sach ca nhan cho ky de tra ve
                            UserDAO ud = new UserDAO();
                            List<EntityVhrEmployee> listEmployee = new ArrayList<>();
                            for (EntityText entityText : listUser) {
                                EntityVhrEmployee employee = ud.getEmployeeById(entityText.getEmpVhrId());
                                listEmployee.add(employee);
                            }
                            //thanh cong
                            // Ghi log ket thuc chuc nang
                            LogUtils.logFunctionalEnd(log);
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listEmployee, aesKey);
                        } else {
                            //khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getListSignerNext - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * <b>Van thu Thay anh nguoi ky</b>
     *
     * @author thanght6
     * @since Jun 15, 2016
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateSignImageBySecrectary(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Lay thong tin user theo sessionId cua request
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);

        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateSignImageBySecrectary - No session");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user2 == null || user2.getUserId() == null)) {
            LOGGER.error("updateSignImageBySecrectary - user hoac userId tren he thong");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1

        // Id nguoi dung tren he thong 2
        Long userId2 = user2.getUserId();

        // Giai ma du lieu neu ma hoa
        String aesKey;
        // Lay AES Key
        aesKey = userGroup.getStrAesKey();
        // Giai ma data client gui len
        data = SecurityControler.decodeDataByAes(aesKey, data);
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.LIST_NEW_SIGNER
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Chuoi nguoi ky moi
            String strNewSigner = listValue.get(0);
            // Parse chuoi nguoi ky moi thanh danh sach
            Type listType = new TypeToken<ArrayList<EntityTextProcess>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityTextProcess> listNewSigner = gson.fromJson(strNewSigner, listType);

            // Thay anh nguoi ky
            TextDAO textDAO = new TextDAO();
            int result = textDAO.updateSignImageBySecrectary(listNewSigner, userId2);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("updateSignImageBySecrectary  - userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (Exception ex) {
            LOGGER.error("updateSignImageBySecrectary- userId2: "
                    + userId2 + " - Exception: ", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String identifyObjectByUser(HttpServletRequest request, String data,
            String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup
                .getUserGroupBySessionIdOfRequest(request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("identifyObjectByUser (Xac dinh doi tuong theo user) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.STR_OBJECT_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Integer type = Integer.parseInt(listValue.get(0));
            Long objectId = Long.parseLong(listValue.get(1));
            Object result = null;
            if (type == 1) {
                TextDAO dao = new TextDAO();
                result = dao.identifyTextByUser(userGroup, objectId);
            }
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("identifyObjectByUser (Xac dinh doi tuong theo user) - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Them file dinh kem cho van ban</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addAttachmentForText(HttpServletRequest request, String data,
            String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup
                .getUserGroupBySessionIdOfRequest(request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("addAttachmentForText (Them file dinh kem cho van ban) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.TEXT
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strText = listValue.get(0);
            Gson gson = new Gson();
            EntityText text = gson.fromJson(strText, EntityText.class);
            int result = 0;
            TextDAO dao = new TextDAO();
            if (dao.addAttachmentForText(userGroup.getUserId2(), text)) {
                result = 1;
            }
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("identifyObjectByUser (Xac dinh doi tuong theo user) - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay lich su thay doi nguoi ky</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getHistoryOfSignerChange(HttpServletRequest request, String data,
            String isSecurity) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_PROCESS_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getHistoryOfSignerChange (Lay lich su thay doi nguoi ky) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textProcessId = Long.parseLong(listValue.get(0));
            HistoryChangeSignDAO dao = new HistoryChangeSignDAO();
            List<EntityHistoryChangeSign> listHistory = dao.getHistoryOfSignerChange(
                    userGroup.getUserId2(), textProcessId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listHistory, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getHistoryOfSignerChange (Lay lich su thay doi nguoi ky) - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Kiem tra van ban cho ky cua nhan vien</b>
     *
     * @param request
     * @param data
     * @return
     */
    public String checkTextWaitingForSignOfUser(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.EMP_ID,
            ConstantsFieldParams.ORG_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("checkTextWaitingForSignOfUser (Kiem tra van ban cho ky cua nhan vien) - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long empId = Long.parseLong(listValue.get(0));
            Long orgId = Long.parseLong(listValue.get(1));
            TextProcessDAO textProcessDAO = new TextProcessDAO();
            int result = 0;
            if (textProcessDAO.checkTextWaitingForSignOfUser(userGroup.getUserId2(), empId, orgId)) {
                result = 1;
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("checkTextWaitingForSignOfUser (Kiem tra van ban cho ky cua nhan vien) - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    // 201812-Pitagon: add
    public String getOrgMarkedList(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            ConstantsFieldParams.STATE,
            ConstantsFieldParams.STATUS
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getOrgMarkedList - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            Integer state = !CommonUtils.isEmpty(listValue.get(1)) ? Integer.parseInt(listValue.get(1)) : null;
            if (textId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            boolean checkActive = "1".equals(listValue.get(2));
            
            TextDAO dao = new TextDAO();
            List<EntityTextMark> result = dao.getOrgMarkedList(textId, state, checkActive);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getOrgMarkedList - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    // 201812-Pitagon: add
    public String askForSeal(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            "orgMarkList"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("askForSeal - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            if (textId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            List<EntityVhrOrg> orgMarkList = new ArrayList<EntityVhrOrg>();
            String strOrgMarkList = listValue.get(1);
            JSONArray jsonArray = new JSONArray(strOrgMarkList);
            if (jsonArray != null && jsonArray.length() > 0) {
            	Gson gson = new Gson();
            	JSONObject obj;
                EntityVhrOrg org;
                for (int i = 0; i < jsonArray.length(); i++) {
                    obj = jsonArray.getJSONObject(i);
                    org = gson.fromJson(obj.toString(), EntityVhrOrg.class);
                    orgMarkList.add(org);
                }
            }
            
            if (CommonUtils.isEmpty(orgMarkList)) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            TextDAO dao = new TextDAO();
            boolean result = dao.askForSeal(textId, orgMarkList);
            if (result) {
            	List<EntityTextMark> orgMarkedList = dao.getOrgMarkedList(textId, 1, false);
            	EntityText text = dao.getTextByID(textId);
            	if (!CommonUtils.isEmpty(orgMarkedList) && text != null) {
            		CommonControler smsDAO = new CommonControler();
            	    List<Long> orgIds = new ArrayList<>();
            	    for (EntityTextMark entity : orgMarkedList) {
            	        orgIds.add(entity.getOrgId());
            	    }
            	    
            	    UserDAO userDao = new UserDAO();
            	    List<EntityVhrEmployee> users = userDao.getUserDocumentManagerOrg(orgIds);
            	    if (!CommonUtils.isEmpty(users)) {
            	        for (EntityVhrEmployee entity : users) {
            	            smsDAO.sentSMS(text.getTitle(), userGroup.getUserId2(), entity.getEmployeeId(),
            	                    null, Constants.SMS_TEXT_CONFIG.ASK_FOR_REAL, "", 101L);
            	        }
            	    }
            	}
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, userGroup);
        } catch (Exception ex) {
            LOGGER.error("askForSeal - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    // 201812-Pitagon: add
    public String rejectMark(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            ConstantsFieldParams.COMMENT,
            ConstantsFieldParams.TEXT_PROCESS_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("rejectMark - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            Long textProcessId = !CommonUtils.isEmpty(listValue.get(2)) ? Long.parseLong(listValue.get(2)) : null;
            if (textId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            String comment = listValue.get(1);
            
            TextDAO dao = new TextDAO();
            boolean result = dao.rejectMark(textId, textProcessId, comment);
            if (result) {
                EntityText text = dao.getTextByID(textId);
                if (text != null) {
                    Long orgId = dao.getOrgIdMarkDoc(textProcessId);
                    CommonControler smsDAO = new CommonControler();
                    smsDAO.sentSMS(text.getTitle(), userGroup.getUserId2(), text.getCreatorId(), 
                            orgId, Constants.SMS_TEXT_CONFIG.REJECT_MARK, comment, 101L);
                }
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, userGroup);
        } catch (Exception ex) {
            LOGGER.error("rejectMark - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    // 201812-Pitagon: add
    public String markDocumentByOrg(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT_ID,
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.ORG_NAME,
            "groupType"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("markDocumentByOrg - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long textId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            Long orgId = !CommonUtils.isEmpty(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            String orgName = listValue.get(2);
            Long groupType = !CommonUtils.isEmpty(listValue.get(3)) ? Long.parseLong(listValue.get(3)) : null;
            if (textId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            TextDAO dao = new TextDAO();
            com.viettel.voffice.database.entity.document.EntityText text = new com.viettel.voffice.database.entity.document.EntityText();
            Long result = dao.markDocumentByOrg(orgId, orgName, textId, groupType);
            text.setTextProcessId(result);
            if (result != null && result.intValue() > 0) {
            	OrgDAO orgDao = new OrgDAO();
            	List<EntityVhrOrg> orgs = orgDao.getOrgById(Arrays.asList(orgId));
            	String taxCode = null;
            	if (!CommonUtils.isEmpty(orgs)) {
            		taxCode = orgs.get(0).getBusinessCode();
            	}
            	text.setTaxCode(taxCode);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, text, userGroup);
        } catch (Exception ex) {
            LOGGER.error("markDocumentByOrg - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    /**
     * @author Tunghd
     * Insert thong tin dong dau vao bang text_mark vs dong dau xac nhan
     * @param request
     * @param data
     * @return
     */
    public String markDocumentByOrgForConfirm(HttpServletRequest request, String data) {
        
        String[] keys = new String[]{
                ConstantsFieldParams.DOCUMENT_ID,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.ORG_NAME,
                "groupType"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("markDocumentByOrg - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long documentId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            Long orgId = !CommonUtils.isEmpty(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            String orgName = listValue.get(2);
            Long groupType = !CommonUtils.isEmpty(listValue.get(3)) ? Long.parseLong(listValue.get(3)) : null;
            if (documentId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            TextDAO dao = new TextDAO();
            com.viettel.voffice.database.entity.document.EntityText text = new com.viettel.voffice.database.entity.document.EntityText();
            Long result = dao.markDocumentByOrgForConfirm(orgId, orgName, documentId, groupType);
            text.setTextProcessId(result);
            if (result != null && result.intValue() > 0) {
                OrgDAO orgDao = new OrgDAO();
                List<EntityVhrOrg> orgs = orgDao.getOrgById(Arrays.asList(orgId));
                String taxCode = null;
                if (!CommonUtils.isEmpty(orgs)) {
                    taxCode = orgs.get(0).getBusinessCode();
                }
                text.setTaxCode(taxCode);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, text, userGroup);
        } catch (Exception ex) {
            LOGGER.error("markDocumentByOrg - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    // 201901-Pitagon: add
    public String getListOrgPermissionMark(HttpServletRequest request, String data) {

        String[] keys = new String[]{
                "groupType"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("markDocumentByOrg - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long groupType = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
        	OrgDAO dao = new OrgDAO();
        	List<EntityVhrOrg> results = dao.getListOrgPermissionMark(userGroup.getUserId2(), groupType);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, results, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getListOrgPermissionMark - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
     /**
     * datnv5: kiem tra van ban neu la user dang trong qua trinh xu ly thi co the ky lai
     * @param userId
     * @param textId
     * @return 
     */
    private  boolean isUserInDuringSing(Long userId, Long textId) {
        TextSearchDAO textSearchDAO = new TextSearchDAO();
        return textSearchDAO.isUserConfigAndDuringProcess(userId, textId);
    }
    
    /**
     * @author Tunghd
     * Lay ra TextID tu DocumentId
     * @param request
     * @param data
     * @return
     */
    public String getTextIdByDocumentId(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.DOCUMENT_ID,
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getTextIdByDocumentId - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long documentId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            if (documentId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            TextDAO dao = new TextDAO();
            Long result = dao.getTextIdByDocumentId(documentId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getOrgMarkedList - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * @author Tunghd
     * Insert thong tin dong dau vao bang Brief_Mark
     * @param request
     * @param data
     * @return
     */
    
    public String markDocumentByOrgForBrief(HttpServletRequest request, String data) {
        
        String[] keys = new String[]{
                ConstantsFieldParams.BRIEF_ID,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.ORG_NAME,
                "briefDocumentId",
                "groupType"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("markDocumentByOrg - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long briefId = !CommonUtils.isEmpty(listValue.get(0)) ? Long.parseLong(listValue.get(0)) : null;
            Long orgId = !CommonUtils.isEmpty(listValue.get(1)) ? Long.parseLong(listValue.get(1)) : null;
            String orgName = listValue.get(2);
            Long briefDocumentId = !CommonUtils.isEmpty(listValue.get(3)) ? Long.parseLong(listValue.get(3)) : null;
            Long groupType = !CommonUtils.isEmpty(listValue.get(4)) ? Long.parseLong(listValue.get(4)) : null;
            if (briefId == null) {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
            
            TextDAO dao = new TextDAO();
            com.viettel.voffice.database.entity.document.EntityText text = new com.viettel.voffice.database.entity.document.EntityText();
            Long result = dao.markDocumentByOrgForBrief(orgId, orgName, briefId, briefDocumentId, groupType);
            text.setTextProcessId(result);
            if (result != null && result.intValue() > 0) {
                OrgDAO orgDao = new OrgDAO();
                List<EntityVhrOrg> orgs = orgDao.getOrgById(Arrays.asList(orgId));
                String taxCode = null;
                if (!CommonUtils.isEmpty(orgs)) {
                    taxCode = orgs.get(0).getBusinessCode();
                }
                text.setTaxCode(taxCode);
            }
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, text, userGroup);
        } catch (Exception ex) {
            LOGGER.error("markDocumentByOrgForBrief - Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
	
	/**
	 * @author minhnq
	 * xu ly rollback dong dau don vi
	 * @param request
	 * @param data
	 * @return
	 */
  	public String rollBackDauDonVi(HttpServletRequest request, String data) {
  		String[] keys = new String[] {
  				"requisition"
  	        };
  			EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
  	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
  	        if (!userGroup.getCheckSessionOk()) {
  	            LOGGER.error("checkPermissionDoc - Session timeout!");
  	            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
  	        }
  	        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//  	        if (user == null || user.getUserId() == null) {
//  	            LOGGER.error("cancelPublish - user hoac userId tren he thong 1 null");
//  	            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//  	        }
  	        Long userId = user == null ? null : user.getUserId();
  		try {
  			String aesKey = null;
              // Lay AES Key
              aesKey = entityUserGroup.getStrAesKey();
              // Giai ma data client gui len
              data = SecurityControler.decodeDataByAes(aesKey, data);
  				JSONObject json = new JSONObject(data);
  	            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
  	            String strText = listValue.get(0);
  	            // Parse chuoi strDocument sang doi tuong van ban
  	            Gson gson = new Gson();
  	            EntityText text = gson.fromJson(strText, EntityText.class);
  	            TextDAO textDao = new TextDAO();
  	            //lay ra textmark cua don vi hien van ban hien tai
  	            List<EntityTextMark> lstTextMark = textDao.getListTextMark(text.getId(),null, false);
  	            List<Long> lstOrgSec = new ArrayList<>();
  	            if (null != user) {
  	                lstOrgSec = user.getListSecretaryVhrOrg();
  	            }
  	            EntityTextMark txtmark = new EntityTextMark();
                if (lstTextMark != null && !CommonUtils.isEmpty(lstOrgSec)) {
                    for (EntityTextMark att : lstTextMark) {
                        if (lstOrgSec.contains(att.getOrgId())) {
                            txtmark = att;
                        }
                    }
                }
                ArrayList<String> currentOrg = new ArrayList<>();
                currentOrg.add(txtmark.getOrgId().toString());
  	            // lay ra dc các text mark của các đơn vị đóng dấu đóng dấu sau
  	            List<EntityTextMark> lstTextMarkAfter = textDao.getListTextMark(text.getId(),txtmark.getActionDate(), false);
  	            List<EntityTextMark> lstTextMarkAfterCurrent = textDao.getListTextMark(text.getId(),txtmark.getActionDate(), true);
  	            //Tunghd add
  	            List<EntityTextMark> lstTextMarkSMS = new ArrayList<EntityTextMark>();
  	            ArrayList<String> lstorgs = new ArrayList<>();
  	            if(!CommonUtils.isEmpty(lstTextMarkAfterCurrent)){
  	                lstTextMarkSMS.addAll(lstTextMarkAfterCurrent);
	  	          	for(EntityTextMark tm: lstTextMarkAfterCurrent){
	  	              lstorgs.add(tm.getOrgId().toString());
	  	            }
  	            }
  	            // tung textmark se co orgID se lay dc markAttachHistory theo tung don vi
  	            List<Long> lstOrgsRollBack = new ArrayList<Long>();
  	            for(EntityTextMark tm: lstTextMarkAfter){
  	              lstOrgsRollBack.add(tm.getOrgId());
  	            }
  	            if(!CommonUtils.isEmpty(lstorgs)){
  	            	List<EntityMarkAttachHistory> lstMAH = textDao.getListMAH(lstorgs,text.getId());
	  	            if(!CommonUtils.isEmpty(lstMAH)){
		  	            for(EntityMarkAttachHistory mah : lstMAH ){
		  	            	// xoa storage
		  	            	String markUploadStorage = mah.getStorageBefore();
		                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
			            	String pathMark = markUploadStorageFolder+ File.separator + mah.getPathBefore();
			            	File tmp = new File(pathMark);
			            	tmp.delete();
			            	textDao.deleteMAH(mah.getId());
		  	            	textDao.rollbackMarkLocation(mah.getAttachId());
		  	            	//xoa cung mark attach history
		  	            	textDao.deleteMAH(mah.getId());
		  	            	//end xoa
		  	            }
	  	            }
  	            }
  	            //rollback ve mark attach history hien tai
  	            List<EntityMarkAttachHistory> lstMAHCurrent = textDao.getListMAH(currentOrg,text.getId());
  	            for(EntityMarkAttachHistory mah: lstMAHCurrent)
  	            {
  	            	List<EntityAttach> attachmentXN = textDao.getListFileAttachment(mah.getAttachId());
  	            	if(attachmentXN!= null){
		            		String markUploadStorage = attachmentXN.get(0).getStorage();
		                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
			            	String pathMark = markUploadStorageFolder+ File.separator + attachmentXN.get(0).getPath();
			            	File tmp = new File(pathMark);
			            	tmp.delete();
			            	List<EntityAttach> attachXN = textDao.getListFileAttach(attachmentXN.get(0).getAttachId());
		  	            	if(attachXN!= null){
					            	tmp.delete();
				            	}
		            	}
  	            	List<EntityAttach> attach = textDao.getListFileAttach(mah.getAttachId());
  	            	if(attach!= null){
		            		String markUploadStorage = attach.get(0).getStorage();
		                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
			            	String pathMark = markUploadStorageFolder+ File.separator + attach.get(0).getPath();
			            	File tmp = new File(pathMark);
			            	tmp.delete();
		            	}
  	            	textDao.rollbackAttach(mah.getAttachId(), mah.getPathBefore());
  	            	textDao.rollbackFilesAttachment(mah.getAttachId(), mah.getPathBefore());
  	            	textDao.rollbackMarkLocation(mah.getAttachId());
  	            	//xoa cung mark attach history
  	            	textDao.deleteMAH(mah.getId());
  	            }
	  	        for(EntityTextMark tm: lstTextMarkAfter){
	  	             textDao.rollBackTextMark(tm.getId(),userId);
	  	        }
	  	        textDao.rollBackText(text.getId());
	  	        
	  	        //minhnq rollback dong dau don vi voi van ban da dong dau xac nhan
	  	        Long docId = textDao.getDocIdByTextId(text.getId());
	  	        	// check da dong dau xacnhan chua
	  	        if(0L!=docId){
		  	        if(textDao.checkDDXN(docId,txtmark.getActionDate())){
		  	        	DocumentDAO docDao = new DocumentDAO();
		  	        	List<EntityTextMark> textMark = docDao.getListTextMark(docId,null);
		  	        	//Tunghd
		  	        	lstTextMarkSMS.addAll(textMark);
			            for(EntityTextMark tm: textMark){
			              lstorgs.add(tm.getOrgId().toString());
			            }
			            List<EntityMarkAttachHistory> lstMAH = docDao.getListMAH(lstorgs,docId);
			            for(EntityMarkAttachHistory mah :lstMAH ){
			            	docDao.deleteMAH(mah.getId());
			            	docDao.rollbackMarkLocation(mah.getAttachId());
			            	
		  	            }
			            docDao.rollBackTextMark(docId, userId);	
		  	        }
	  	        }
	  	        
	  	        //Tunghd add Gui SMS
                if(!CommonUtils.isEmpty(lstTextMarkSMS)){
                	List<Long> lstOrgsRollBackSMS = new ArrayList<Long>();
                	for(EntityTextMark sms : lstTextMarkSMS){
                	    if(!lstOrgsRollBackSMS.contains(sms.getOrgId())){
                	        lstOrgsRollBackSMS.add(sms.getOrgId());
                	    }
                    }
                    CommonControler smsDAO = new CommonControler();
                    UserDAO userDao = new UserDAO();
                    List<EntityVhrEmployee> employees = userDao.getSecretaryListRollback(userId, lstOrgsRollBackSMS);
                    List<EntityVhrEmployee> employeesTmp = new ArrayList<>();
                    employeesTmp.addAll(employees);
                    if(!employees.isEmpty()){
                        for(EntityVhrEmployee emp : employees){
                            if(emp.getEmployeeId().equals(userGroup.getUserId2()) || emp.getEmployeeId().equals(userGroup.getUserId1()))
                                employeesTmp.remove(emp);
                        }
                    }
                    if(!employeesTmp.isEmpty()){
                        smsDAO.sentSMSRollback(text.getTitle(), userGroup.getUserId2(), Constants.SMS_TEXT_CONFIG.ROLLBACK_MARK_DOC, 101L, employeesTmp, txtmark.getOrgId());
                    }
                }
  	            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, true, aesKey);
  	        
  		} catch (Exception ex) {
  			LOGGER.error(ex.getMessage(), ex);
  			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
  		}
      }
  	/**
  	 * @author minhnq
  	 * xu ly rollback dong dau Ho So
  	 * @param request
  	 * @param data
  	 * @return
  	 */
  	public String rollBackBrief(HttpServletRequest request, String data) {
  		String[] keys = new String[] {
  				"brief"
  	        };
  			EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
  	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
  	        if (!userGroup.getCheckSessionOk()) {
  	            LOGGER.error("checkPermissionDoc - Session timeout!");
  	            return FunctionCommon.responseResult(ErrorCode.SESSION_TIME_OUT, null, null);
  	        }
  	        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
//  	        if (user == null || user.getUserId() == null) {
//  	            LOGGER.error("cancelPublish - user hoac userId tren he thong 1 null");
//  	            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
//  	        }
  	        Long userId = user == null ? null : user.getUserId();
  		try {
  			String aesKey = null;
              // Lay AES Key
              aesKey = entityUserGroup.getStrAesKey();
              // Giai ma data client gui len
              data = SecurityControler.decodeDataByAes(aesKey, data);
  			JSONObject json = new JSONObject(data);
  	            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
  	            String strText = listValue.get(0);
  	            // Parse chuoi strDocument sang doi tuong van ban
  	            Gson gson = new Gson();
  	            EntityBriefInfo brief = gson.fromJson(strText, EntityBriefInfo.class);
  	            TextDAO textDao = new TextDAO();
//  	            List<EntityTextMark> lstTextMark = textDao.getListTextMark(brief.getBriefId(),null);
  	            List<Long> lstOrgSec = new ArrayList<>();
  	            if (null != user) {
  	                lstOrgSec = user.getListSecretaryVhrOrg();
  	            }
  	            List<EntityMarkAttachHistory> lstMAH = textDao.getListMAHBrief(brief.getBriefId(),brief.getBdBriefDocumentId(),lstOrgSec);
  	            	for(EntityMarkAttachHistory mah :lstMAH ){
  	            		List<EntityAttach> attachXN = textDao.getListFileAttachBrief(mah.getAttachId());
  		            	if(attachXN!= null){
  		            		String markUploadStorage = attachXN.get(0).getStorage();
  		                    String markUploadStorageFolder = com.viettel.voffice.utils.FileUtils.getStorageFolder(markUploadStorage);
  			            	String pathMark = markUploadStorageFolder+ File.separator + attachXN.get(0).getPath();
  			            	File tmp = new File(pathMark);
  			            	tmp.delete();
  		            	}
	            	textDao.rollbackBriefFilesAttachment(mah.getAttachId(), mah.getPathBefore()); 	
	            	textDao.rollbackMarkLocation(mah.getAttachId());
	            	textDao.deleteMAH(mah.getId());
	            }
  	            textDao.rollbackBriefMark(brief.getBdBriefDocumentId(),userId);
  	            
  	            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, true, aesKey);
  	        
  		} catch (Exception ex) {
  			LOGGER.error(ex.getMessage(), ex);
  			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
  		}
      }
  	
}
