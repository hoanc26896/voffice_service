/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.staff.OrgCriteriaDAO;
import com.viettel.voffice.database.dao.staff.OrgCriteriaMapDAO;
import com.viettel.voffice.database.dao.staff.OrgCriteriaRatingDAO;
import com.viettel.voffice.database.dao.staff.OrgCriteriaRatingTotalDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityOrgCriteriaHistory;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.User.EntityOrgCriteria;
import com.viettel.voffice.database.entity.User.EntityOrgCriteriaMap;
import com.viettel.voffice.database.entity.User.EntityOrgCriteriaRating;
import com.viettel.voffice.database.entity.User.EntityOrgCriteriaRatingTotal;
import com.viettel.voffice.database.entity.User.EntityOrgCriteriaRatingTotalSigning;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author thanght6
 */
public class OrgController {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(OrgController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = OrgController.class.getName();

    /**
     * <b>Lay danh sach cong viec ca nhan</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListEmployeeOfOrganization(HttpServletRequest request,
            String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup entityUserGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!entityUserGroup.getCheckSessionOk()) {
            LOGGER.error("getListEmployeeOfOrganization - No session");
            return FunctionCommon.generateResponseJSON(entityUserGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Long userId = entityUserGroup.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
            LOGGER.error("getListEmployeeOfOrganization -  userId null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = entityUserGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = entityUserGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            OrgDAO orgDAO = new OrgDAO();
            List<EntityVhrEmployee> result = orgDAO.getListEmployeeOfOrganization(userId, orgId);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            LOGGER.error("getListEmployeeOfOrganization - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * <b>Ham kiem tra don vi cua user co van thu hay khong</b><br>
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String checkSecretaryByGroupId(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult = "";
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        OrgDAO orgDao = new OrgDAO();
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = null;
                String decryptedData = "";
                if (isSecurity != null && "1".equals(isSecurity)) {
                    strAesKeyDecode = dataSessionGR.getStrAesKey();
                    decryptedData = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                }
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(decryptedData);
                LogUtils.logFunctionalStart(log);

                //Lay prams client gui len de thuc hien yeu cau
                HashMap hmParams = new HashMap();
                hmParams.put("groupId", String.class);
                HashMap valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
//                System.out.println("Don vi check truyen vao:" + valueParams.get("groupId"));
                String strGroupId = (String) (valueParams.get("groupId") != null
                        ? valueParams.get("groupId") : "");
//                System.out.println("Don vi check truyen vao:" + strGroupId);
                if (strGroupId != null && !"0".equals(strGroupId)) {
                    int result = orgDao.checkSecretaryByGroupId(Long.parseLong(strGroupId));
//                    System.out.println("Ket qua check van thu :" + strGroupId + " " + result);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error(e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
//        System.out.println("Ket qua check van thu :" + strResult);
        return strResult;
    }

    /**
     * <b>Them moi hoac chinh sua tieu chi danh gia</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String updateOrgCriteria(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "listOrgCriteria"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "updateOrgCriteria - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Doi tuong chua thong tin tieu chi danh gia
            String strListOrgCriteria = listValue.get(0);
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<EntityOrgCriteria>>() {
            }.getType();
            List<EntityOrgCriteria> listOrgCriteria = gson.fromJson(strListOrgCriteria, type);
            OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
            int result = orgCriteriaDAO.updateOrgCriteria(userGroup.getUserId2(),
                    listOrgCriteria) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }        
    }
    
    /**
     * <b>Lay danh sach tieu chi</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getOrgCriteriaList(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "creatorOrgId",
            "orgCriteriaId",
            ConstantsFieldParams.ORG_ID,
            "ratingOrgId",
            ConstantsFieldParams.PERIOD,
            ConstantsFieldParams.TYPE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "getOrgCriteriaList - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID don vi tao tieu chi
            String strCreatorOrgId = listValue.get(0);
            Long creatorOrgId = null;
            if (!CommonUtils.isEmpty(strCreatorOrgId)) {
                creatorOrgId = Long.parseLong(strCreatorOrgId);
            }
            // ID tieu chi
            String strCriteriaId = listValue.get(1);
            Long orgCriteriaId = null;
            if (!CommonUtils.isEmpty(strCriteriaId)) {
                orgCriteriaId = Long.parseLong(strCriteriaId);
            }
            // ID don vi duoc cau hinh danh gia
            Long orgId = null;
            String strOrgId = listValue.get(2);
            if (!CommonUtils.isEmpty(strOrgId)) {
                orgId = Long.parseLong(strOrgId);
            }
            // ID don vi danh gia
            Long ratingOrgId = null;
            String strRatingOrgId = listValue.get(3);
            if (!CommonUtils.isEmpty(strRatingOrgId)) {
                ratingOrgId = Long.parseLong(strRatingOrgId);
            }
            // Ky danh gia yyyyMM
            String period = listValue.get(4);
            // Danh gia theo thang/qui/nam
            String type = listValue.get(5);
            OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
            List<EntityOrgCriteria> result = orgCriteriaDAO.getOrgCriteriaList(
                    userGroup, creatorOrgId, orgCriteriaId,
                    orgId, ratingOrgId, period, type);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }        
    }
    
    /**
     * <b>Lay chi tiet tieu chi</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getOrgCriteriaDetail(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "orgCriteriaId"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "getOrgCriteriaDetail - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            Long orgCriteriaId = Long.parseLong(listValue.get(0));
            OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
            EntityOrgCriteria result = orgCriteriaDAO.getOrgCriteriaDetail(
                    userGroup.getUserId2(), orgCriteriaId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }        
    }
    
    /**
     * <b>Cap nhat thong tin map tieu chi voi don vi</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String updateOrgCriteriaMap(HttpServletRequest request, String data) {
        
        String[] keys = new String[] { "listOrgCriteriaMap", "listRemovalOrgId", "type", "reason", "orgId" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateOrgCriteriaMap - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // Danh sach tieu chi cap nhat
            String strListOrgCriteriaMap = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityOrgCriteriaMap>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityOrgCriteriaMap> listOrgCriteriaMap = gson.fromJson(strListOrgCriteriaMap, type);
            // Danh sach id don vi loai bo cau hinh
            String strListRemovalOrgId = listValue.get(1);
            List<Long> listRemovalOrgId = new ArrayList<>();
            if (!CommonUtils.isEmpty(strListRemovalOrgId)) {
                JSONArray jaOrgId = new JSONArray(strListRemovalOrgId);
                for (int i = 0; i < jaOrgId.length(); i++) {
                    listRemovalOrgId.add(jaOrgId.getLong(i));
                }
            }
            Long typeCriteriaOrg = 1L;
            if (!CommonUtils.isEmpty(listValue.get(2))) {
                typeCriteriaOrg = Long.valueOf(listValue.get(2));
            }
            String reason = listValue.get(3);
            Long orgId = null;
            if (!CommonUtils.isEmpty(listValue.get(4))) {
                orgId = Long.valueOf(listValue.get(4));
            }

            OrgCriteriaMapDAO orgCriteriaMapDAO = new OrgCriteriaMapDAO();
            boolean result = orgCriteriaMapDAO.updateOrgCriteriaMap(userGroup.getUserId2(), listOrgCriteriaMap,
                    listRemovalOrgId, typeCriteriaOrg);
            if (result) {
                if (!CommonUtils.isEmpty(reason) && orgId != null) {
                    OrgCriteriaDAO dao = new OrgCriteriaDAO();
                    EntityOrgCriteriaHistory entity = new EntityOrgCriteriaHistory();
                    entity.setContent(reason);
                    entity.setOrgCriteriaMapId(orgId);
                    entity.setType(typeCriteriaOrg.intValue());
                    entity.setMapType(2);
                    dao.insertOrgCriteriaHistory(Arrays.asList(entity), userGroup.getUserId2());
                }
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, 0, userGroup);
            }
        } catch (Exception ex) {
            LOGGER.error("updateOrgCriteriaMap - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Lay danh sach don vi co tieu chi</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getOrgListWhichHaveCriteria(HttpServletRequest request, String data) {
        
        String[] keys = new String[] { "creatorOrgId", ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE, "type" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getOrgListWhichHaveCriteria - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Long creatorOrgId = null;
            String strCreatorOrgId = listValue.get(0);
            if (!CommonUtils.isEmpty(strCreatorOrgId)) {
                creatorOrgId = Long.parseLong(strCreatorOrgId);
            }
            // Vi tri ban ghi lay ra
            Long startRecord = null;
            String strStartRecord = listValue.get(1);
            if (!CommonUtils.isEmpty(strStartRecord)) {
                startRecord = Long.parseLong(strStartRecord);
            }
            // So luong lay ra
            Long pageSize = null;
            String strPageSize = listValue.get(2);
            if (!CommonUtils.isEmpty(strPageSize)) {
                pageSize = Long.parseLong(strPageSize);
            }
            Long type = null;
            String strType = listValue.get(3);
            if (!CommonUtils.isEmpty(strType)) {
                type = Long.parseLong(strType);
            }

            OrgCriteriaMapDAO orgCriteriaMapDAO = new OrgCriteriaMapDAO();
            List<EntityOrgCriteriaMap> listOrg = orgCriteriaMapDAO.getOrgListWhichHaveCriteria(userGroup.getUserId2(),
                    creatorOrgId, type, startRecord, pageSize);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listOrg, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getOrgListWhichHaveCriteria - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Cap nhat thong tin map tieu chi voi don vi</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String updateOrgCriteriaRating(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "listOrgCriteriaRating",
            "orgCriteriaRatingTotal"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("updateOrgCriteriaRating - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            Gson gson = new Gson();
            // Danh sach danh gia
            String strListOrgCriteriaRating = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityOrgCriteriaRating>>() {
            }.getType();
            List<EntityOrgCriteriaRating> listOrgCriteriaRating = gson.fromJson(
                    strListOrgCriteriaRating, type);
            // Danh gia tong hop
            EntityOrgCriteriaRatingTotal orgCriteriaRatingTotal = null;
            String strOrgCriteriaRatingTotal = listValue.get(1);
            if (!CommonUtils.isEmpty(strOrgCriteriaRatingTotal)) {
                orgCriteriaRatingTotal = gson.fromJson(strOrgCriteriaRatingTotal,
                        EntityOrgCriteriaRatingTotal.class);
                if (orgCriteriaRatingTotal != null) {
                    // Khong co diem dieu chinh -> gan diem dieu chinh bang diem tong hop
                    if (orgCriteriaRatingTotal.getAdjustmentPoint() == null) {
                        orgCriteriaRatingTotal.setAdjustmentPoint(orgCriteriaRatingTotal.getPoint());
                    }
                    // Khong co KI dieu chinh -> Gan KI dieu chinh bang KI tong hop
                    if (CommonUtils.isEmpty(orgCriteriaRatingTotal.getAdjustmentKI())) {
                        orgCriteriaRatingTotal.setAdjustmentKI(orgCriteriaRatingTotal.getKi());
                    }
                    if (!CommonUtils.isEmpty(listOrgCriteriaRating)) {
                        StringBuilder comment = new StringBuilder();
                        List<EntityOrgCriteriaRating> listRatingForBusiness = new ArrayList<>();
                        List<EntityOrgCriteriaRating> listRatingForMission = new ArrayList<>();
                        List<EntityOrgCriteriaRating> listRatingForGSM = new ArrayList<>();
                        List<EntityOrgCriteriaRating> listRatingForReward = new ArrayList<>();
                        for (EntityOrgCriteriaRating orgCriteriaRating : listOrgCriteriaRating) {
                            // Chi tieu san xuat kinh doanh
                            if (orgCriteriaRating.getSourceId() != null
                                    && orgCriteriaRating.getSourceId().equals(1L)) {
                                listRatingForBusiness.add(orgCriteriaRating);
                            }
                            if (orgCriteriaRating.getType() != null) {
                                // Nhiem vu
                                if (orgCriteriaRating.getType()
                                        == Constants.OrgCriteria.Type.MISSION) {
                                    listRatingForMission.add(orgCriteriaRating);
                                }
                                // GSM
                                if (orgCriteriaRating.getType()
                                        == Constants.OrgCriteria.Type.GSM) {
                                    listRatingForGSM.add(orgCriteriaRating);
                                }
                                // Diem cong
                                if (orgCriteriaRating.getType()
                                        == Constants.OrgCriteria.Type.REWARD_FOR_BUSINESS
                                        || orgCriteriaRating.getType()
                                        == Constants.OrgCriteria.Type.REWARD_FOR_MISSION) {
                                    listRatingForReward.add(orgCriteriaRating);
                                }
                            }
                        }
                        comment.append(generateComment("Chỉ tiêu sản xuất kinh doanh",
                                listRatingForBusiness, false, false));
                        comment.append(generateComment("Điểm thực hiện nhiệm vụ",
                                listRatingForMission, false, true));
                        comment.append(generateComment("Công tác GSM",
                                listRatingForGSM, false, false));
                        comment.append(generateComment("Điểm cộng",
                                listRatingForReward, true, false));
                        orgCriteriaRatingTotal.setRatingComment(comment.toString());
                    }
                    orgCriteriaRatingTotal.setIsLock(1);
                }
            }
            OrgCriteriaRatingDAO orgCriteriaRatingDAO = new OrgCriteriaRatingDAO();
            int result = orgCriteriaRatingDAO.updateOrgCriteriaRating(userGroup.getUserId2(),
                    listOrgCriteriaRating, orgCriteriaRatingTotal) ? 1 : 0;
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("updateOrgCriteriaMap - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Tao y kien cho tung loai chi tieu</b>
     * 
     * @param orgCriteriaTypeName           ten loai chi tieu
     * @param listOrgCriteriaRating         danh sach chi tieu     
     * @param isOnlyShowPoint               true: chi hien thi diem dat duoc
     * @param isShowResult                  true: hien thi ket qua thuc hien trong ky
     * @return 
     */
    private String generateComment(String orgCriteriaTypeName,
            List<EntityOrgCriteriaRating> listOrgCriteriaRating,
            boolean isOnlyShowPoint, boolean isShowResult) {
        
        StringBuilder comment = new StringBuilder();
        if (CommonUtils.isEmpty(orgCriteriaTypeName) || CommonUtils.isEmpty(listOrgCriteriaRating)) {
            LOGGER.error("generateComment - Loi du lieu dau vao!");
            return comment.toString();
        }
        Float point = 0F;
        Integer planPoint = 0;
        Float executeInPeriod = 0F;
        Float planInPeriod = 0F;
        boolean isHadComment = false, isAdd;
        OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
        for (EntityOrgCriteriaRating orgCriteriaRating : listOrgCriteriaRating) {
            isAdd = true;
            // Neu la tieu chi san xuat kinh doanh hoac tieu chi nhiem vu
            // -> Chi cong diem cac tieu chi cha
            if ((orgCriteriaRating.getSourceId() != null
                    && orgCriteriaRating.getSourceId().equals(1L))
                    || (orgCriteriaRating.getType() != null
                        && orgCriteriaRating.getType() == Constants.OrgCriteria.Type.MISSION)) {
                isAdd = !orgCriteriaDAO.checkParent(orgCriteriaRating.getOrgCriteriaMapId());
            }
            if (isAdd) {
                // Tinh tong diem dat duoc
                if (orgCriteriaRating.getPoint() != null) {
                    point += orgCriteriaRating.getPoint();
                }
                // Tinh tong quy diem
                if (orgCriteriaRating.getPlanPoint() != null) {
                    planPoint += orgCriteriaRating.getPlanPoint();
                }
                // Tinh tong thuc hien trong ky
                if (orgCriteriaRating.getExecuteInPeriod() != null) {
                    executeInPeriod += orgCriteriaRating.getExecuteInPeriod();
                }
                // Tinh tong ke hoach trong ky
                if (orgCriteriaRating.getPlanInPeriod() != null) {
                    planInPeriod += orgCriteriaRating.getPlanInPeriod();
                }
            }
            if (!CommonUtils.isEmpty(orgCriteriaRating.getRatingComment())) {
                comment.append(orgCriteriaRating.getRatingComment().replaceAll("<p>", "<p>+ "));
//                comment.append(orgCriteriaRating.getRatingComment());
                isHadComment = true;
            }
        }
//        comment.insert(0, "<br>");
        // Hien thi ket qua thuc hien trong ky
        if (isShowResult) {
            comment.insert(0, String.format("(Hoàn thành %d/%d nhiệm vụ).",
                    executeInPeriod.intValue(), planInPeriod.intValue()));
        }
        // Chi hien thi diem dat duoc
        if (isOnlyShowPoint) {
            comment.insert(0, String.format("%.2f điểm ", point));
        } // Hien thi ca quy diem
        else {
            comment.insert(0, String.format("%.2f/%d điểm ", point, planPoint));
        }
        comment.insert(0, String.format("<b>- %s: </b>", orgCriteriaTypeName));
        if (!isHadComment) {
            comment.append("<br>");
        }
        return comment.toString();
    }
    
    /**
     * <b>Lay danh sach tong hop danh gia</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String getOrgCriteriaRatingTotalList(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.ORG_ID,
            ConstantsFieldParams.PERIOD,
            ConstantsFieldParams.TYPE
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getOrgCriteriaRatingTotalList - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID don vi
            Long orgId = Long.parseLong(listValue.get(0));
            // Ky danh gia
            String period = listValue.get(1);
            Integer type = CommonUtils.isInteger(listValue.get(2)) ? Integer.parseInt(listValue.get(2)) : null;
            OrgCriteriaRatingTotalDAO orgCriteriaRatingTotalDAO = new OrgCriteriaRatingTotalDAO();
            List<EntityOrgCriteriaRatingTotal> result = orgCriteriaRatingTotalDAO.
                    getOrgCriteriaRatingTotalList(userGroup.getUserId2(), orgId, period, type);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("updateOrgCriteriaMap - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Tao van ban tu danh sach tong hop danh gia</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public Object createTextFromOrgCriteriaRatingTotalList(
            HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            "orgRatingSigning",
            ConstantsFieldParams.STATUS
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "createTextFromOrgCriteriaRatingTotalList - username: "
                + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Doi tuong chua thong tin danh sach danh gia don vi
            String strOrgRatingSigning = listValue.get(0);
            Gson gson = new Gson();
            EntityOrgCriteriaRatingTotalSigning orgRatingSigning = gson.fromJson(
                    strOrgRatingSigning, EntityOrgCriteriaRatingTotalSigning.class);
            // 0 - Save thong tin
            // 1 - Xem truoc file
            // 2 - Trinh ky
            // 3 - Xem van ban trinh ky
            Integer status = Integer.parseInt(listValue.get(1));
            // null - trinh ky van ban luon, 1 - xem truoc file trinh ky
            boolean isPreview = status == 1;
            OrgCriteriaRatingTotalDAO orgCriteriaRatingTotalDAO = new OrgCriteriaRatingTotalDAO();
            Object result = null;
            switch (status) {
                // Save thong tin
                case 0:
                    result = orgCriteriaRatingTotalDAO.save(userGroup.getUserId2(),
                            orgRatingSigning, false) ? 1 : 0;
                    break;
                // Xem truoc file
                case 1:
                // Trinh ky
                case 2:
                    result = orgCriteriaRatingTotalDAO.createTextFromOrgCriteriaRatingTotalList(
                            userGroup, orgRatingSigning, isPreview);
                    break;
                // Xem van ban trinh ky
                case 3:
                    result = orgCriteriaRatingTotalDAO.getTextInfo(userGroup.getUserId2(),
                            orgRatingSigning.getOrgId(), orgRatingSigning.getPeriod());
                    break;
            }
            // Loi server
            if (result == null) {
                LOGGER.error(errorDesc + "Loi tao van ban tu nhiem vu result = null!");
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            }
            // Xem truoc file trinh ky
            if (isPreview) {
                EntityFileAttachment fileInfo = (EntityFileAttachment) result;
                Response response = null;
                File file = new File(fileInfo.getAttachment());
                // File khong ton tai
                if (!file.exists()) {
                    LOGGER.error(errorDesc + "Loi file nhiem vu khong ton tai - path: "
                            + fileInfo.getAttachment());
                    return response;
                }
                InputStream inputStream = new InputStreamWithFileDeletion(file);
                Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                responseBuilder.header("File-Name", fileInfo.getName());
                response = responseBuilder.build();
                return response;
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
            }            
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    public String getOrgRatingId(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getOrgRatingId - Session timeout!");
            return null;
        }
        try {
            OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
            Long result = orgCriteriaDAO.getOrgRatingId();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getOrgRatingId - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    public String importOrgCriteria(HttpServletRequest request, String data) {

        String[] keys = new String[] { "listOrgCriteriaMap", "type" };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("importOrgCriteria - Session timeout!");
            return null;
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            String strListOrgCriteriaMap = listValue.get(0);
            Type type = new TypeToken<ArrayList<EntityOrgCriteria>>() {
            }.getType();
            Gson gson = new Gson();
            List<EntityOrgCriteria> listOrgCriteriaMap = gson.fromJson(strListOrgCriteriaMap, type);
            Integer typeCriteriaOrg = 1;
            if (!CommonUtils.isEmpty(listValue.get(1))) {
                typeCriteriaOrg = Integer.valueOf(listValue.get(1));
            }

            OrgCriteriaMapDAO orgCriteriaMapDAO = new OrgCriteriaMapDAO();
            boolean result = orgCriteriaMapDAO.importOrgCriteria(listOrgCriteriaMap, typeCriteriaOrg,
                    userGroup.getUserId2());
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, userGroup);
        } catch (Exception ex) {
            LOGGER.error("importOrgCriteria - Exception - username: " + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    public String getAllOrgCriteria(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = "getOrgCriteriaList - username: " + userGroup.getCardId() + " - ";
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return null;
        }
        try {
            OrgCriteriaDAO orgCriteriaDAO = new OrgCriteriaDAO();
            List<EntityOrgCriteria> result = orgCriteriaDAO.getAllOrgCriteria();
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
}
