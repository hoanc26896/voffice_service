/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.DocumentException;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.InputStreamWithFileDeletion;
import com.viettel.voffice.database.dao.document.SignBriefcaseDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.document.EntityAttachBriefcase;
import com.viettel.voffice.database.entity.document.EntitySignBriefCase;
import com.viettel.voffice.database.entity.document.EntitySignBriefcaseSigner;
import com.viettel.voffice.database.entity.document.EntitySignBriefcaseStatus;
import com.viettel.voffice.utils.CommonUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * cap trinh ky
 *
 * @author hanhnq21
 */
public class SignBriefcaseControler {

    private static final Logger LOGGER = Logger.getLogger(SignBriefcaseControler.class);

    /**
     * xoa cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteSignBriefcase(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("deleteSignBriefcase - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("deleteSignBriefcase - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                Long userId = user2.getUserId();
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"signBriefcaseId"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strSignBriefcaseId = listValue.get(0);
                    if (!CommonUtils.isEmpty(strSignBriefcaseId)) {
                        Long signBriefcaseId = Long.parseLong(strSignBriefcaseId);//id cap trinh ky
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        Boolean delResult = signBriefcaseDAO.deleteSignBriefcase(signBriefcaseId, userId);
                        if (delResult) {
                            //xoa thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, delResult, aesKey);
                        } else {
                            //xoa khong thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                        }
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("deleteSignBriefcase - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay chi tiet cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getSignBriefcaseDetail(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getSignBriefcaseDetail - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getSignBriefcaseDetail - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"signBriefcaseId", "barcode"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strSignBriefcaseId = listValue.get(0);
                    Long signBriefcaseId = null;//id cap trinh ky
                    if (!CommonUtils.isEmpty(strSignBriefcaseId)) {
                        signBriefcaseId = Long.parseLong(strSignBriefcaseId);
                    }
                    String barcode = listValue.get(1);
                    if (signBriefcaseId != null || !CommonUtils.isEmpty(barcode)) {
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        EntitySignBriefCase signBriefCase = signBriefcaseDAO.getDetail(signBriefcaseId, barcode, user2.getUserId());
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, signBriefCase, aesKey);
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getSignBriefcaseDetail - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay trang thai cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSignBriefcaseStatus(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getListSignBriefcaseStatus - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getListSignBriefcaseStatus - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
//                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                List<EntitySignBriefcaseStatus> lstStatus = signBriefcaseDAO.getListSignBriefcaseStatus();
                if (lstStatus != null && !lstStatus.isEmpty()) {
                    //lay danh sach thanh cong
                    result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstStatus, aesKey);
                } else {
                    //co loi xay ra
                    result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay ma vach cu va moi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getBarcode(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getBarcode - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getBarcode - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"employeeId", "getOldBarcode"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    String strEmployeeId = listValue.get(0);
                    if (!CommonUtils.isEmpty(strEmployeeId)) {
                        Long employeeId = Long.parseLong(strEmployeeId);//id nguoi trinh
                        String strGetOldBarcode = listValue.get(1);
                        Boolean isGetOldBarcode = false;
                        if (strGetOldBarcode != null && "1".equals(strGetOldBarcode)) {
                            isGetOldBarcode = true;
                        }
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        String barCode = signBriefcaseDAO.getBarcode(employeeId, isGetOldBarcode);
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, barCode, aesKey);
                    } else {
                        //loi du lieu dau vao
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getBarcode - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * lay lanh dao cua tro ly
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getLeaderOfAssitant(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getBarcode - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getBarcode - user khong ton tai tren he thong 2");
                result = FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            } else {
                // Giai ma du lieu neu ma hoa
                String aesKey = null;
                if (isSecurity != null && "1".equals(isSecurity)) {
                    // Lay AES Key
                    aesKey = userGroup.getStrAesKey();
                    // Giai ma data client gui len
//                    data = SecurityControler.decodeDataByAes(aesKey, data);
                }
                SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                List<EntityVhrEmployee> lstLeader = signBriefcaseDAO.getLeaderByEmployee(user2.getUserId());
                result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstLeader, aesKey);
            }
        }
        return result;
    }

    /**
     * tim kiem cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListSignBriefcase(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("getListSignBriefcase - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("getListSignBriefcase - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"type",
                        "startRecord",
                        "pageSize",
                        "isCount",
                        "keyword",
                        "barcode",
                        "signerId",
                        "employeeId",
                        "status",
                        "dateStart",
                        "dateEnd",
                        "title"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //loai tim kiem
                    String strType = listValue.get(0);
                    Long type = null;
                    if (!CommonUtils.isEmpty(strType)) {
                        type = Long.valueOf(strType.trim());
                    }
                    //ban ghi bat dau
                    String strStartRecord = listValue.get(1);
                    Long startRecord = 0L;
                    if (!CommonUtils.isEmpty(strStartRecord)) {
                        startRecord = Long.valueOf(strStartRecord.trim());
                    }
                    //so ban ghi 1 trang
                    String strPageSize = listValue.get(2);
                    Long pageSize = 10L;
                    if (!CommonUtils.isEmpty(strPageSize)) {
                        pageSize = Long.valueOf(strPageSize.trim());
                    }
                    //lay so luong hoac tim kiem
                    String strIsCount = listValue.get(3);
                    Long isCount = 0L;
                    if (!CommonUtils.isEmpty(strIsCount)) {
                        isCount = Long.valueOf(strIsCount.trim());
                    }
                    String keyword = listValue.get(4);//tim kiem nhanh
                    String barcode = listValue.get(5);//ma vach
                    //nguoi ky
                    String strSignerId = listValue.get(6);
                    Long signerId = null;
                    if (!CommonUtils.isEmpty(strSignerId)) {
                        signerId = Long.valueOf(strSignerId.trim());
                    }
                    //nguoi trinh
                    String strEmployeeId = listValue.get(7);
                    Long employeeId = null;
                    if (!CommonUtils.isEmpty(strEmployeeId)) {
                        employeeId = Long.valueOf(strEmployeeId.trim());
                    }
                    //trang thai
                    String strStatus = listValue.get(8);
                    Long status = null;
                    if (!CommonUtils.isEmpty(strStatus)) {
                        status = Long.valueOf(strStatus.trim());
                    }
                    String dateStart = listValue.get(9);
                    String dateEnd = listValue.get(10);
                    String title = listValue.get(11);
                    if (type != null) {
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        Object lstSignBrief = signBriefcaseDAO.getListSignBriefcase(type, keyword, barcode,
                                signerId, employeeId, status, dateStart, dateEnd, startRecord, pageSize,
                                isCount, title, user2.getUserId());
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstSignBrief, aesKey);
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("getListSignBriefcase - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * cap nhat trang thai xu ly cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateStatusSignBriefcase(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("updateStatusSignBriefcase - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("updateStatusSignBriefcase - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"signBriefcaseId", "listSigner"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //id cap trinh ky
                    String strSignBriefcaseId = listValue.get(0);
                    Long signBriefcaseId = null;
                    if (!CommonUtils.isEmpty(strSignBriefcaseId)) {
                        signBriefcaseId = Long.valueOf(strSignBriefcaseId.trim());
                    }
                    String strListSigner = listValue.get(1);
                    List<EntitySignBriefcaseSigner> lstSigner = null;
                    if (!CommonUtils.isEmpty(strListSigner)) {
                        Type listEntitySignBriefcaseSignerType = new TypeToken<ArrayList<EntitySignBriefcaseSigner>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstSigner = gson.fromJson(strListSigner.trim(), listEntitySignBriefcaseSignerType);
                    }
                    if (signBriefcaseId != null && lstSigner != null) {
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        Boolean updateResult = signBriefcaseDAO.updateStatusSignBriefcase(lstSigner, signBriefcaseId, user2.getUserId());
                        result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, updateResult, aesKey);
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("updateStatusSignBriefcase - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * them sua cap trinh ky
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String addOrEditSignBriefcase(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("addOrEditSignBriefcase - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("addOrEditSignBriefcase - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"signBriefcase", "lstSigner", "fileSign", "lstFileAttachOther"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //thong tin cap trinh ky
                    String strSignBriefcase = listValue.get(0);
                    EntitySignBriefCase signBriefcase = null;
                    if (!CommonUtils.isEmpty(strSignBriefcase)) {
                        Gson gson = new Gson();
                        signBriefcase = gson.fromJson(strSignBriefcase.trim(), EntitySignBriefCase.class);
                    }
                    //thong tin nguoi ky
                    String strLstSigner = listValue.get(1);
                    List<EntitySignBriefcaseSigner> lstSigner = null;
                    if (!CommonUtils.isEmpty(strLstSigner)) {
                        Type listEntitySignBriefcaseSignerType = new TypeToken<ArrayList<EntitySignBriefcaseSigner>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstSigner = gson.fromJson(strLstSigner.trim(), listEntitySignBriefcaseSignerType);
                    }
                    //thong tin file ky
                    String strFileSign = listValue.get(2);
                    EntityAttachBriefcase fileSign = null;
                    if (!CommonUtils.isEmpty(strFileSign)) {
                        Gson gson = new Gson();
                        fileSign = gson.fromJson(strFileSign.trim(), EntityAttachBriefcase.class);
                    }
                    //thong tin file dinh kem
                    String strListFileAttachOther = listValue.get(3);
                    List<EntityAttachBriefcase> lstFileAttachOther = null;
                    if (!CommonUtils.isEmpty(strListFileAttachOther)) {
                        Type listEntityAttachBriefcaseType = new TypeToken<ArrayList<EntityAttachBriefcase>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstFileAttachOther = gson.fromJson(strListFileAttachOther.trim(), listEntityAttachBriefcaseType);
                    }
                    if (signBriefcase != null && !CommonUtils.isEmpty(lstSigner)) {
                        signBriefcase.setLstSigner(lstSigner);
                        signBriefcase.setFileSign(fileSign);
                        signBriefcase.setLstFileAttachOther(lstFileAttachOther);
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        int updateResult = signBriefcaseDAO.addOrEditSignBriefcase(signBriefcase, user2.getUserId());
                        if (updateResult == SignBriefcaseDAO.SUCCESS) {
                            //neu thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, updateResult, aesKey);
                        } else {
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, updateResult, aesKey);
                        }
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("addOrEditSignBriefcase - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * tro ly thay nguoi ky duyet
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String updateSigner(HttpServletRequest request,
            String data, String isSecurity) {
        String result;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            //het session
            LOGGER.error("updateSigner - No session");
            result = FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        } else {
            Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
            if ((user2 == null || user2.getUserId() == null)) {
                //khong ton tai tren he thong voffice 2
                LOGGER.error("updateSigner - user khong ton tai tren he thong 2");
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
                try {
                    JSONObject json = new JSONObject(data);
                    String[] keys = new String[]{"lstSigner"};
                    List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
                    //thong tin nguoi ky
                    String strLstSigner = listValue.get(0);
                    List<EntitySignBriefcaseSigner> lstSigner = null;
                    if (!CommonUtils.isEmpty(strLstSigner)) {
                        Type listEntitySignBriefcaseSignerType = new TypeToken<ArrayList<EntitySignBriefcaseSigner>>() {
                        }.getType();
                        Gson gson = new Gson();
                        lstSigner = gson.fromJson(strLstSigner.trim(), listEntitySignBriefcaseSignerType);
                    }
                    if (!CommonUtils.isEmpty(lstSigner)) {
                        SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                        int updateResult = signBriefcaseDAO.updateSigner(lstSigner, user2.getUserId());
                        if (updateResult == SignBriefcaseDAO.SUCCESS) {
                            //neu thanh cong
                            result = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, updateResult, aesKey);
                        } else {
                            result = FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, updateResult, aesKey);
                        }
                    } else {
                        result = FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                    }
                } catch (JSONException | NumberFormatException ex) {
                    //co loi xay ra
                    LOGGER.error("updateSigner - co loi xay ra - userId2: "
                            + user2.getUserId() + " - Exception: ", ex);
                    result = FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
                }
            }
        }
        return result;
    }

    /**
     * in ma vach
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response printBarCode(
            HttpServletRequest req, String data, String isSecurity) {
        Response response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            return response;
        }
        // Lay id user he thong 2
        Long userIdVof2 = 0L;
        if (userGroup.getVof2_ItemEntityUser() != null
                && userGroup.getVof2_ItemEntityUser().getUserId() != null) {
            userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        }
        if (userIdVof2 == null) {
            //loi du lieu dau vao
            LOGGER.error("printBarCode - userId: "
                    + userIdVof2 + " - Loi het session");
            return response;
        }
        // Giai ma du lieu neu ma hoa
        String aesKey;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{"signBriefcaseId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strSignBriefcaseId = listValue.get(0);
            Long signBriefcaseId = null;//id cap trinh ky
            if (!CommonUtils.isEmpty(strSignBriefcaseId)) {
                signBriefcaseId = Long.parseLong(strSignBriefcaseId);
            }
            if (signBriefcaseId != null) {
                SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                EntitySignBriefCase signBriefCase = signBriefcaseDAO.getSignBriefcaseDetail(signBriefcaseId, null, null);
                if (signBriefCase != null) {
                    //neu lay duoc thong tin ban ghi
                    String barcode = signBriefCase.getBarcode();
                    String tmpFilePath = signBriefcaseDAO.printBarCode(req, barcode, signBriefcaseId, userIdVof2);
                    if (tmpFilePath == null) {
                        LOGGER.error("printBarCode - File null hoac file khong ton tai");
                        LOGGER.error("tmpFilePath: " + tmpFilePath);
                        return response;
                    }
                    File file = new File(tmpFilePath);
                    // File khong ton tai
                    if (!file.exists()) {
                        LOGGER.error("printBarCode - File null hoac file khong ton tai");
                        LOGGER.error("tmpFilePath: " + tmpFilePath);
                        return response;
                    }
                    InputStream inputStream = new InputStreamWithFileDeletion(file);
                    Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                    responseBuilder.header("File-Name", barcode + ".doc");
                    if (file.length() > 0) {
                        responseBuilder.header("File-Size", file.length());
                    } else {
                        responseBuilder.header("File-Size", 0);
                    }
                    response = responseBuilder.build();
                } else {
                    return response;
                }
            } else {
                //loi du lieu dau vao
                LOGGER.error("printBarCode - userId: "
                        + userIdVof2 + " | signBriefcaseId: " + signBriefcaseId
                        + " - Loi du lieu dau vao");
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build();
                return response;
            }
        } catch (JSONException | NumberFormatException | FileNotFoundException ex) {
            LOGGER.error("printBarCode - Exception:", ex);
        }
        return response;
    }

    /**
     * download file cap trinh ky
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    public Response downloadFile(
            HttpServletRequest req, String data, String isSecurity) {
        Response response = null;
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(req);
        if (!userGroup.getCheckSessionOk()) {
            return response;
        }
        // Lay id user he thong 2
        Long userIdVof2 = 0L;
        if (userGroup.getVof2_ItemEntityUser() != null
                && userGroup.getVof2_ItemEntityUser().getUserId() != null) {
            userIdVof2 = userGroup.getVof2_ItemEntityUser().getUserId();
        }
        if (userIdVof2 == null) {
            //loi du lieu dau vao
            LOGGER.error("downloadFile - userId: "
                    + userIdVof2 + " - Loi het session");
            return response;
        }
        // Giai ma du lieu neu ma hoa
        String aesKey;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        InputStream inputStream = null;
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{"signBriefcaseId", "attachId"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strSignBriefcaseId = listValue.get(0);
            Long signBriefcaseId = null;//id cap trinh ky
            if (!CommonUtils.isEmpty(strSignBriefcaseId)) {
                signBriefcaseId = Long.parseLong(strSignBriefcaseId);
            }
            String strAttachId = listValue.get(1);
            Long attachId = null;//id file dinh kem
            if (!CommonUtils.isEmpty(strAttachId)) {
                attachId = Long.parseLong(strAttachId);
            }
            if (signBriefcaseId != null && attachId != null) {
                SignBriefcaseDAO signBriefcaseDAO = new SignBriefcaseDAO();
                EntityAttachBriefcase attach = signBriefcaseDAO.downloadFile(req, attachId,
                        signBriefcaseId, userIdVof2);
                if (attach == null) {
                    LOGGER.error("downloadFile - File null hoac file khong ton tai");
                    LOGGER.error("attachId: " + attachId);
                    return response;
                }
                String tmpFilePath = attach.getFilePath();
                String fileName = attach.getFileName();
                if (tmpFilePath == null) {
                    LOGGER.error("downloadFile - File null hoac file khong ton tai");
                    LOGGER.error("tmpFilePath: " + tmpFilePath);
                    return response;
                }
                File file = new File(tmpFilePath);
                // File khong ton tai
                if (!file.exists()) {
                    LOGGER.error("downloadFile - File null hoac file khong ton tai");
                    LOGGER.error("tmpFilePath: " + tmpFilePath);
                    return response;
                }
                inputStream = new InputStreamWithFileDeletion(file);
                Response.ResponseBuilder responseBuilder = Response.ok(inputStream);
                responseBuilder.header("File-Name", fileName);
                if (file.length() > 0) {
                    responseBuilder.header("File-Size", file.length());
                } else {
                    responseBuilder.header("File-Size", 0);
                }
                response = responseBuilder.build();
            } else {
                //loi du lieu dau vao
                LOGGER.error("downloadFile - userId: "
                        + userIdVof2 + " | signBriefcaseId: " + signBriefcaseId
                        + " - Loi du lieu dau vao");
                Response.ResponseBuilder responseBuilder = Response.ok();
                responseBuilder.header("filePermission", "0");
                response = responseBuilder.build();
                return response;
            }
        } catch (JSONException | NumberFormatException | IOException | DocumentException ex) {
            LOGGER.error("downloadFile - Exception:", ex);
        }
        return response;
    }
}
