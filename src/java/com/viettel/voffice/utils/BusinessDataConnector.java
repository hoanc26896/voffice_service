/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.database.dao.logAction.ActionLogMobileDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.businessdata.EntityDepartment;
import com.viettel.voffice.database.entity.businessdata.EntityImage;
import com.viettel.voffice.database.entity.businessdata.EntityReport;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author thanght6
 */
public class BusinessDataConnector {
    
    /** Log file */
    private static final Logger LOGGER = Logger.getLogger(BusinessDataConnector.class);
    
    /** Link service SLDH goc */
    private static final String ROOTURL = "http://10.60.7.140:8050/Revreportservice/api/";
//    private static final String ROOTURL = "http://10.60.108.110:8068/Revreportservice/api/";
                                           
    /** Tham so de goi service */
    private static final String AUTHENTICATION_PARAMETER = new String(Base64.decodeBase64(
            CommonUtils.getAppConfigValue("parameter.service.businessdata")));
    
    /**
     * <b>Tao tham so cho url</b><br>
     * 
     * @param map 
     * @return
     */
    public static String generateParameters(LinkedHashMap<String, Object> map) {
        
        String parameters = null;
        if (!CommonUtils.isEmpty(map)) {
            parameters = "";
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                parameters += "&" + entry.getKey() + "=" + entry.getValue();
            }
            // Bo dau & dau tien
            parameters = parameters.substring(1);
            // Them tham so bat buoc
            parameters += AUTHENTICATION_PARAMETER;
        }
        return parameters;
    }
    
    /**
     * <b>Gui request</b><br>
     * 
     * @param userGroup         thong tin user
     * @param function          ten chuc nang
     * @param parameters        tham so
     * @return 
     */
    public static Response sendRequest(EntityUserGroup userGroup,
            String function, String parameters) {
        
        Date startTime = new Date();
        String content = "ERROR: ";
        // Chuoi doi tuong tra ve
        Response response = null;
        String strUrl = ROOTURL + function;
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Add header request
            conn.setRequestMethod("POST");
            // Send post request
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(parameters.getBytes());            
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            // Success
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder strResponse = new StringBuilder();
		while ((inputLine = br.readLine()) != null) {
			strResponse.append(inputLine);
		}
		br.close();
                if (!CommonUtils.isEmpty(strResponse.toString())) {
                    response = parseResponse(strResponse.toString());
                    if (response != null && response.isSuccess()) {
                        if (!CommonUtils.isEmpty(response.getEntity())) {
                            content = "SUCCESS";
                        } else {
                            content += "entity is empty";
                        } 
                    } else {
                        LOGGER.error("sendRequest - Loi du lieu tra ve - response: "
                                + strResponse);
                        content += "response = " + strResponse.toString();
                    }
                } else {
                    LOGGER.error("sendRequest - Loi khong co du lieu tra ve!");
                    content += "response is null or empty ";
                }
            } else {
                LOGGER.error("sendRequest - Loi ket noi - responseCode: " + responseCode);
                content += "responseCode = " + responseCode;
            }
        } catch (Exception ex) {
            LOGGER.error("sendRequest - Exception!", ex);
            content += "exception";
        }
        Date endTime = new Date();
        ActionLogMobileDAO actionLogMobileDAO = new ActionLogMobileDAO();
        actionLogMobileDAO.insert(userGroup.getUserId1(), userGroup.getCardId(),
                startTime, endTime, "SLKD." + function,
                parameters.replace(AUTHENTICATION_PARAMETER, ""),
                content, null, null, null, null);
        return response;
    }
    
    /**
     * <b>Chuyen doi chuoi du lieu tra ve thanh doi tuong</b><br>
     * 
     * @param str       chuoi du lieu tra ve
     * @return 
     */
    public static Response parseResponse(String str) {
        
        Response response = null;
        try {
            JSONObject json = new JSONObject(str);
            // Lay doi tuong mo ta loi
            Gson gson = new Gson();
            ErrorCode errorCode = gson.fromJson(json.getString("errorCode"), ErrorCode.class);
            String entity = json.getString("entity");
            response = new Response(errorCode, entity);
        } catch (JSONException ex) {
            LOGGER.error("parseResponse - Exception!", ex);
        }
        return response;
    }
    
    public static boolean checkResponse(Response response) {
        
        return response != null && response.isSuccess()
                && !CommonUtils.isEmpty(response.getEntity());
    }
    
    /**
     * <b>Lay cay don vi</b><br>
     * 
     * @param userGroup     thong tin user
     * @return
     */
    public static List<EntityDepartment> getDepartment(EntityUserGroup userGroup) {
        
        String function = "getDepartment";
        // Tao tham so
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("staffCode", userGroup.getCardId());
        String parameters = generateParameters(map);
        if (CommonUtils.isEmpty(parameters)) {
            LOGGER.error("getDepartment - Loi tao tham so!");
            return null;
        }
        Response response = sendRequest(userGroup, function, parameters);
        if (!checkResponse(response)) {
            LOGGER.error("getDepartment - Loi gui request!");
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<EntityDepartment>>() {
        }.getType();
        List<EntityDepartment> listDepartment = gson.fromJson(response.getEntity(), type);
        // Cap nhat level
        if (!CommonUtils.isEmpty(listDepartment)) {
            // Cap nhat don vi khong co nut cha
            for (EntityDepartment department : listDepartment) {
                department.setIsLeaf(1);
                checkParentDepartment(department, listDepartment);
            }
            // Duyet danh sach tu nhung don vi khong co cha
            for (EntityDepartment department : listDepartment) {
                if (department.getLevel() != null && department.getLevel() == 0L) {
                    findChildDepartment(department, listDepartment);
                }
            }
        }
        return listDepartment;
    }
    
    /**
     * <b>Kiem tra don vi cha cua don vi hien tai</b><br>
     * 
     * @param currentDepartment         don vi hien tai
     * @param listDepartment            danh sach don vi
     */
    public static void checkParentDepartment(EntityDepartment currentDepartment,
            List<EntityDepartment> listDepartment) {
     
        if (currentDepartment.getParentId() == null) {
            currentDepartment.setLevel(0L);
        } else {
            boolean hasParent = false;
            for (EntityDepartment department : listDepartment) {
                if (currentDepartment.getParentId().equals(department.getId())) {
                    hasParent = true;
                    break;
                }
            }
            if (!hasParent) {
                currentDepartment.setLevel(0L);
            }
        }
    }
    
    /**
     * <b>Tim kiem don vi con</b><br>
     * 
     * @param currentDepartment         don vi hien tai
     * @param listDepartment            danh sach don vi
     */
    public static void findChildDepartment(EntityDepartment currentDepartment,
            List<EntityDepartment> listDepartment) {
        
        for (EntityDepartment department : listDepartment) {
            if (currentDepartment.getId().equals(department.getParentId())) {
                currentDepartment.setIsLeaf(0);
                department.setLevel(currentDepartment.getLevel() + 1);
                findChildDepartment(department, listDepartment);
            }
        }
    }
    
    /**
     * <b>Lay danh sach tieu chi cua don vi</b><br>
     * 
     * @param userGroup         thong tin user
     * @param departmentId      id don vi lay tieu chi
     * @return 
     */
    public static List<EntityReport> getListReport(EntityUserGroup userGroup,
            Long departmentId) {
        
        String function = "getListReport";
        // Tao tham so
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("departmentFolderId", departmentId);
        String parameters = generateParameters(map);
        if (CommonUtils.isEmpty(parameters)) {
            LOGGER.error("getListReport - Loi tao tham so!");
            return null;
        }
        Response response = sendRequest(userGroup, function, parameters);
        if (!checkResponse(response)) {
            LOGGER.error("getListReport - Loi gui request!");
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<EntityReport>>() {
        }.getType();
        List<EntityReport> listReport = gson.fromJson(response.getEntity(), type);
        if (!CommonUtils.isEmpty(listReport)) {
            // Cap nhat gia tri node la va level cho don vi goc
            for (EntityReport report : listReport) {
                report.setIsLeaf(1);
                checkParentReport(report, listReport);
            }
            for (EntityReport report : listReport) {
                if (report.getLevel() != null && report.getLevel() == 0) {
                    findChildReport(report, listReport);
                }                
            }
        }
        return listReport;
    }
    
    /**
     * <b>Kiem tra tieu chi cha cua tieu chi hien tai</b><br>
     * 
     * @param currentReport         tieu chi hien tai
     * @param listReport            danh sach tieu chi
     */
    public static void checkParentReport(EntityReport currentReport,
            List<EntityReport> listReport) {
        
        if (CommonUtils.isEmpty(currentReport.getReportCodeParent())) {
            currentReport.setLevel(0);
        } else {
            boolean hasParent = false;
            for (EntityReport report : listReport) {
                if (currentReport.getReportCodeParent().equals(report.getReportCode())) {
                    hasParent = true;
                    break;
                }
            }
            if (!hasParent) {
                currentReport.setLevel(0);
            }
        }
    }
    
    /**
     * <b>Tim kiem tieu chi con</b><br>
     * 
     * @param currentReport         tieu chi hien tai
     * @param listReport            danh sach tieu chi
     */
    public static void findChildReport(EntityReport currentReport,
            List<EntityReport> listReport) {
        
        for (EntityReport report : listReport) {
            if (currentReport.getReportCode().equals(report.getReportCodeParent())) {
                currentReport.setIsLeaf(0);
                report.setLevel(currentReport.getLevel() + 1);
                findChildReport(report, listReport);
            }
        }
    }
    
    /**
     * <b>Download anh</b><br>
     * 
     * @param userGroup         thong tin user
     * @param reportType        loai bieu do
     * @param imageId           id anh
     * @param date              ngay lay anh (yyyy/mm/dd)
     * @param departmentCode    ma don vi dang chon
     * @return 
     */
    public static EntityImage downloadImageReport(EntityUserGroup userGroup,
            Long reportType, Long imageId, String date, String departmentCode) {
        
        String function = "downloadImageReport";
        // Tao tham so
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("reportType", reportType);
        map.put("imageId", imageId);
        map.put("prdId", date);
        map.put("departmentCode", departmentCode);
        String parameters = generateParameters(map);
        if (CommonUtils.isEmpty(parameters)) {
            LOGGER.error("downloadImageReport - Loi tao tham so!");
            return null;
        }
        Response response = sendRequest(userGroup, function, parameters);
        if (response == null) {
            LOGGER.error("downloadImageReport - Loi gui request!");
            return null;
        }
        EntityImage image = new EntityImage();
        image.setErrorCode(response.getErrorCode().getErrCode());
        image.setErrorDescription(response.getErrorCode().getErrDesc());
        if (!CommonUtils.isEmpty(response.getEntity())) {
            image.setContent(Base64.decodeBase64(response.getEntity()));
        }        
        return image;
    }        
    
    /** Ma loi tra ve */
    public class ErrorCode {

        /** Ma loi */
        private Integer errCode;

        /** Mo ta loi */
        private String errDesc;

        private ErrorCode(Integer errCode, String errDesc) {
            this.errCode = errCode;
            this.errDesc = errDesc;
        }

        public int getErrCode() {
            return errCode;
        }

        public void setErrCode(Integer errCode) {
            this.errCode = errCode;
        }

        public String getErrDesc() {
            return errDesc;
        }

        public void setErrDesc(String errDesc) {
            this.errDesc = errDesc;
        }
    }

    /** Doi tuong tra ve */
    public static class Response {

        /** Ma loi */
        private ErrorCode errorCode;

        /** Doi tuong tra ve */
        private String entity;

        public Response(ErrorCode errorCode, String entity) {
            this.errorCode = errorCode;
            this.entity = entity;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }
        
        public boolean isSuccess() {
            return errorCode != null && errorCode.getErrCode() == 0;
        }
    }

    /**
     * <b>Lay danh sach anh trinh chieu</b><br>
     * 
     * @param userGroup     thong tin user
     * @return
     */
    public static List<EntityImage> getListSlide(EntityUserGroup userGroup) {
        
        String function = "getListSlide";
        // Tao tham so
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("staffCode", userGroup.getCardId());
        String parameters = generateParameters(map);
        if (CommonUtils.isEmpty(parameters)) {
            LOGGER.error("getDepartment - Loi tao tham so!");
            return null;
        }
        Response response = sendRequest(userGroup, function, parameters);
        if (!checkResponse(response)) {
            LOGGER.error("getDepartment - Loi gui request!");
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<EntityImage>>() {
        }.getType();
        List<EntityImage> listImage = gson.fromJson(response.getEntity(), type);
        return listImage;
    }
    
    /**
     * <b>Lay mo ta anh bieu do</b>
     * 
     * @param userGroup         doi tuong user
     * @param reportType        loai anh
     * @param imageId           id anh
     * @return 
     */
    public static EntityImage getImageReportDesc(EntityUserGroup userGroup,
            Long reportType, Long imageId) {
        
        String function = "getImageReportDesc";
        // Tao tham so
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("reportType", reportType);
        map.put("imageId", imageId);
        String parameters = generateParameters(map);
        if (CommonUtils.isEmpty(parameters)) {
            LOGGER.error("getImageReportDesc - Loi tao tham so!");
            return null;
        }
        Response response = sendRequest(userGroup, function, parameters);
        if (!checkResponse(response)) {
            LOGGER.error("getImageReportDesc - Loi gui request!");
            return null;
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<EntityImage>>() {
        }.getType();
        List<EntityImage> listImage = gson.fromJson(response.getEntity(), type);
        if (!CommonUtils.isEmpty(listImage)) {
            return listImage.get(0);
        } else {
            return null;
        }
    }
}
