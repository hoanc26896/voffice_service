/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.report.MissionReportDAO;
import com.viettel.voffice.database.entity.EntityMissionReport;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.task.EntityMissionNorm;
import com.viettel.voffice.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 *
 * @author pm1_os06
 */
public class MissionReportController {

    /**
     * Log loi
     */
    private static final Logger logger = Logger.getLogger(BriefManagementController.class);

    private static final int RETURN_TRUE = 1;

    /**
     * laays ra list nhaan vieen
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListEmployee(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            "sysOrganizationId",
            "fromDate",
            "toDate",
            "typeView",
            "lstEmpId",
            ConstantsFieldParams.IS_COUNT

        };
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListEmployee - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        Long startRecord = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            startRecord = Long.parseLong(listValue.get(0));
        }
        Long pageSize = 10L;
        if (!CommonUtils.isEmpty(listValue.get(1))) {
            pageSize = Long.parseLong(listValue.get(1));
        }
        Long sysOrganizationId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(2))) {
            sysOrganizationId = Long.parseLong(listValue.get(2));
        }
        String fromDate = null;
        if (!CommonUtils.isEmpty(listValue.get(3))) {
            fromDate = listValue.get(3);
        }
        String toDate = null;
        if (!CommonUtils.isEmpty(listValue.get(4))) {
            toDate = listValue.get(4);
        }
        Long typeView = 0L;
        if (!CommonUtils.isEmpty(listValue.get(5))) {
            typeView = Long.parseLong(listValue.get(5));
        }
        String stEmpId = listValue.get(6);
        List<Long> lstEmpId = new ArrayList<>();
        if (!CommonUtils.isEmpty(stEmpId)) {
            String[] arrEmpId = stEmpId.split(",");
            for (String arrEmpId1 : arrEmpId) {
                try {
                    lstEmpId.add(Long.parseLong(arrEmpId1));
                } catch (Exception e) {
                }
            }
        }
        Integer isCount = 0;
        if (!CommonUtils.isEmpty(listValue.get(7))) {
            isCount = Integer.valueOf(listValue.get(7));
        }
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = employeeDao.getListEmployee(startRecord, pageSize, isCount, sysOrganizationId, fromDate, toDate, typeView, lstEmpId);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }

    /**
     * laays ra danh sach don vi
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListGroup(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"sysCode"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        //Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListGroup - Session timeout!");
            return FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = dataSessionGR.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            logger.error("getListStaffByRejectSign - (Thong ke vanban bi tu choi) - Khong co"
                    + " thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        MissionReportDAO employeeDao = new MissionReportDAO();
        String sysCode = listValue.get(0);
        Object result = employeeDao.getListGroup(userIdVof2, sysCode);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);

    }

    /**
     * danh sach don vi thuc hien
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListPerformGroup(String security, String data, HttpServletRequest request) {

        String[] keys;
        keys = new String[]{};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListPerformGroup - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = employeeDao.getListPerformGroup();
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }

    /**
     * danh sach nguoi tao theo don vi
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListCreatBy(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"typeView", "sysOrganizationId"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListCreatBy - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long typeView = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            typeView = Long.parseLong(listValue.get(0));
        }
        Long sysOrganizationId = null;
        if (!CommonUtils.isEmpty(listValue.get(1))) {
            sysOrganizationId = Long.parseLong(listValue.get(1));
        }
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = employeeDao.getListCreatBy(typeView, sysOrganizationId);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }

    /**
     * danhsach don vi thu hien va nhiem vu
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListMissionOfGroup(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"assignId", "listPerform", "date", ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE, ConstantsFieldParams.IS_COUNT};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListMissionOfGroup - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            assignId = Long.parseLong(listValue.get(0));
        }
        String srList = listValue.get(1);
        List<Long> listPerform = new ArrayList<>();
        if (!CommonUtils.isEmpty(srList)) {
            String[] arrList = srList.split(",");
            for (String arrEmpId1 : arrList) {
                try {
                    listPerform.add(Long.parseLong(arrEmpId1));
                } catch (NumberFormatException e) {
                }
            }
        }
        String date = listValue.get(2);
        Long startRecord = 0L;
        if (!CommonUtils.isEmpty(listValue.get(3))) {
            startRecord = Long.parseLong(listValue.get(3));
        }
        Long pageSize = 10L;
        if (!CommonUtils.isEmpty(listValue.get(4))) {
            pageSize = Long.parseLong(listValue.get(4));
        }
        Integer isCount = 0;
        if (!CommonUtils.isEmpty(listValue.get(5))) {
            isCount = Integer.valueOf(listValue.get(5));
        }
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = employeeDao.getListMissionOfGroup(startRecord, pageSize, isCount, assignId, listPerform, date);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }

    /**
     * danhsach don vi thu hien va nhiem vu theo quy
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListQuarterMissionOfGroup(String security, String data, HttpServletRequest request) {
    	String[] keys;
        keys = new String[]{"assignId", "listPerform", "quarter", "year", ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE, ConstantsFieldParams.IS_COUNT};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListMissionOfGroup - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            assignId = Long.parseLong(listValue.get(0));
        }
        String srList = listValue.get(1);
        List<Long> listPerform = new ArrayList<>();
        if (!CommonUtils.isEmpty(srList)) {
            String[] arrList = srList.split(",");
            for (String arrEmpId1 : arrList) {
                try {
                    listPerform.add(Long.parseLong(arrEmpId1));
                } catch (NumberFormatException e) {
                }
            }
        }
        Integer quarter = Integer.parseInt(listValue.get(2));
        String year = listValue.get(3);
        Long startRecord = 0L;
        if (!CommonUtils.isEmpty(listValue.get(4))) {
            startRecord = Long.parseLong(listValue.get(4));
        }
        Long pageSize = 10L;
        if (!CommonUtils.isEmpty(listValue.get(5))) {
            pageSize = Long.parseLong(listValue.get(5));
        }
        Integer isCount = 0;
        if (!CommonUtils.isEmpty(listValue.get(6))) {
            isCount = Integer.valueOf(listValue.get(6));
        }
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = employeeDao.getListQuarterMissionOfGroup(startRecord, pageSize, isCount, assignId, listPerform, quarter, year);
    	return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    /**
     * view nhiem vu chuwa hoan thanh
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String viewMissionReport(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"typeMission", "groupId", "assignId", "date"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("viewMissionReport - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long typeMission = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            typeMission = Long.parseLong(listValue.get(0));
        }
        Long groupId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(1))) {
            groupId = Long.parseLong(listValue.get(1));
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(2))) {
            assignId = Long.parseLong(listValue.get(2));
        }
        String date = listValue.get(3);
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result;
        //nhiemj vu trong tap doan
        if (typeMission.equals(5L) || typeMission.equals(6L)) {
            result = this.filterView(typeMission, groupId, assignId, date);
        } else {
            result = employeeDao.viewMissionReport(typeMission, groupId, assignId, date);
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    /**
     * view nhiem vu chuwa hoan thanh trong quy
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String viewQuarterMissionReport(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"typeMission", "groupId", "assignId", "quarter", "year"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("viewMissionReport - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long typeMission = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            typeMission = Long.parseLong(listValue.get(0));
        }
        Long groupId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(1))) {
            groupId = Long.parseLong(listValue.get(1));
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(2))) {
            assignId = Long.parseLong(listValue.get(2));
        }
        Integer quarter = Integer.parseInt(listValue.get(3));
        String year = listValue.get(4);
        MissionReportDAO employeeDao = new MissionReportDAO();
        Object result = null;
        //nhiemj vu trong tap doan
        if (typeMission.equals(5L) || typeMission.equals(6L)) {
            result = this.filterQuarterView(typeMission, groupId, assignId, quarter, year);
        } else {
            result = employeeDao.viewQuarterMissionReport(typeMission, groupId, assignId, quarter, year);
        }
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    /**
     * view nhiem vu chuwa hoan thanh
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListMissionReport(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"assignId", "listPerform", "date"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListMissionOfGroup - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            assignId = Long.parseLong(listValue.get(0));
        }
        String srList = listValue.get(1);
        List<Long> listPerform = new ArrayList<>();
        if (!CommonUtils.isEmpty(srList)) {
            String[] arrList = srList.split(",");
            for (String arrEmpId1 : arrList) {
                try {
                    listPerform.add(Long.parseLong(arrEmpId1));
                } catch (NumberFormatException e) {
                    logger.error(e);
                }
            }
        }
        String date = listValue.get(2);
        
        MissionReportDAO employeeDao = new MissionReportDAO();
        List<EntityMissionReport> result = employeeDao.getListMissionReport(assignId, listPerform, date);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    /**
     * lay chi tiet danh sach cong viec bao cao theo quy
     *
     * @param security
     * @param data
     * @param request
     * @return
     */
    public String getListQuarterMissionReport(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"assignId", "listPerform", "quarter", "year"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("getListMissionOfGroup - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        Long assignId = 0L;
        if (!CommonUtils.isEmpty(listValue.get(0))) {
            assignId = Long.parseLong(listValue.get(0));
        }
        String srList = listValue.get(1);
        List<Long> listPerform = new ArrayList<>();
        if (!CommonUtils.isEmpty(srList)) {
            String[] arrList = srList.split(",");
            for (String arrEmpId1 : arrList) {
                try {
                    listPerform.add(Long.parseLong(arrEmpId1));
                } catch (NumberFormatException e) {
                    logger.error(e);
                }
            }
        }
        Integer quarter = Integer.parseInt(listValue.get(2));
        String year = listValue.get(3);
        
        MissionReportDAO employeeDao = new MissionReportDAO();
        List<EntityMissionReport> result = employeeDao.getListQuarterMissionReport(assignId, listPerform, quarter, year);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    /**
     * trường hợp view nhiệm vụ trong tập đoàn
     *
     * @param typeMission
     * @param groupId
     * @param assignId
     * @param date
     * @return
     */
    private List<EntityMissionReport> filterView(Long typeMission, Long groupId, Long assignId, String date) {
        List<EntityMissionReport> result = new ArrayList<>();
        List<EntityMissionReport> listAll = new ArrayList<>();
        List<EntityMissionReport> listOut = new ArrayList<>();
        MissionReportDAO employeeDao = new MissionReportDAO();
        if (typeMission.equals(5L)) {
            listAll = employeeDao.viewMissionReport(1L, groupId, assignId, date);
            listOut = employeeDao.viewMissionReport(3L, groupId, assignId, date);
        } else {
            listAll = employeeDao.viewMissionReport(2L, groupId, assignId, date);
            listOut = employeeDao.viewMissionReport(4L, groupId, assignId, date);
        }
        for (EntityMissionReport all : listAll) {
            boolean temp = true;
            for (EntityMissionReport out : listOut) {
                if (all.getMissionId().equals(out.getMissionId())) {
                    temp = false;
                }
            }
            if (temp) {
                result.add(all);
            }
        }
        return result;
    }
    
    /**
     * trường hợp view nhiệm vụ trong tập đoàn theo quy
     *
     * @param typeMission
     * @param groupId
     * @param assignId
     * @param date
     * @return
     */
    private List<EntityMissionReport> filterQuarterView(Long typeMission, Long groupId, Long assignId, Integer quarter, String year) {
        List<EntityMissionReport> result = new ArrayList<>();
        List<EntityMissionReport> listAll = new ArrayList<>();
        List<EntityMissionReport> listOut = new ArrayList<>();
        MissionReportDAO employeeDao = new MissionReportDAO();
        if (typeMission.equals(5L)) {
            listAll = employeeDao.viewQuarterMissionReport(1L, groupId, assignId, quarter, year);
            listOut = employeeDao.viewQuarterMissionReport(3L, groupId, assignId, quarter, year);
        } else {
            listAll = employeeDao.viewQuarterMissionReport(2L, groupId, assignId, quarter, year);
            listOut = employeeDao.viewQuarterMissionReport(4L, groupId, assignId, quarter, year);
        }
        for (EntityMissionReport all : listAll) {
            boolean temp = true;
            for (EntityMissionReport out : listOut) {
                if (all.getMissionId().equals(out.getMissionId())) {
                    temp = false;
                }
            }
            if (temp) {
                result.add(all);
            }
        }
        return result;
    }
    
    public String findMissionNorm(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"isCount", "missionNorm", "startPage", "pageLoad", "getParent"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("findMissionNorm - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        
        boolean isCount = "1".equals(listValue.get(0));
        String missionNorm = listValue.get(1);
        Gson gson = new Gson();
        EntityMissionNorm entity = gson.fromJson(missionNorm, EntityMissionNorm.class);
        Long startPage = null;
        if (!CommonUtils.isEmpty(listValue.get(2))) {
        	startPage = Long.parseLong(listValue.get(2));
        }
        Long pageLoad = null;
        if (!CommonUtils.isEmpty(listValue.get(3))) {
        	pageLoad = Long.parseLong(listValue.get(3));
        }
        boolean getParent = "1".equals(listValue.get(4));
        Vof2_EntityUser userVof2 = dataSessionGR.getVof2_ItemEntityUser();
        
        MissionReportDAO dao = new MissionReportDAO();
        Object result = dao.findMissionNorm(entity, startPage, pageLoad, isCount, userVof2.getUserId(), getParent);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    
    public String insertMissionNorm(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"missionNorm"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("insertMissionNorm - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        
        String missionNorm = listValue.get(0);
        Gson gson = new Gson();
        EntityMissionNorm entity = gson.fromJson(missionNorm, EntityMissionNorm.class);
        
        MissionReportDAO dao = new MissionReportDAO();
        boolean result = dao.insertMissionNorm(entity);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,  result ? 1 : 0, dataSessionGR);
    }
    
    public String updateMissionNorm(String security, String data, HttpServletRequest request) {
        String[] keys;
        keys = new String[]{"missionNorm"};
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(request, data, keys);
        List<String> listValue = dataSessionGR.getListParamsFromClient();
        // Session timeout
        if (!dataSessionGR.getCheckSessionOk()) {
            logger.error("updateMissionNorm - Session timeout!");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }
        
        String missionNorm = listValue.get(0);
        Gson gson = new Gson();
        EntityMissionNorm entity = gson.fromJson(missionNorm, EntityMissionNorm.class);
        
        MissionReportDAO dao = new MissionReportDAO();
        boolean result = dao.updateMissionNorm(entity);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result ? 1 : 0, dataSessionGR);
    }
}
