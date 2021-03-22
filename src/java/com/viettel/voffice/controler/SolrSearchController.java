/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.elasticsearch.search.ElasticSearchVhrEmp;
import com.viettel.voffice.solr.DocumentSearch;
import com.viettel.voffice.solr.EmployeeSearch;
import com.viettel.voffice.solr.IndexingEmployee;
import com.viettel.voffice.solr.UserConfiguration;
import com.viettel.voffice.solr.entity.EmployeeEntity;
import com.viettel.voffice.solr.entity.GroupEntity;
import com.viettel.voffice.utils.LogUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author thanght6
 */
public class SolrSearchController {

    private static final String ROOT_PATH = "/148841/148842/";
    public static final String ROOT_ACTION = "solrSearch";

    // Log file
    private static final Logger logger = Logger.getLogger(SolrSearchController.class);

    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = SolrSearchController.class.getName();

    /**
     * Lay so luong cong van tim kiem
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountDocument(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Kiem tra session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null) && (user2 == null || user2.getUserId() == null)) {
            logger.error("getCountItem - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1
        Long userId1 = (user1 != null) ? user1.getUserId() : null;
        // Id nguoi dung tren he thong 2
        Long userId2 = (user2 != null) ? user2.getUserId() : null;
        // Lay danh sach duong dan don vi tren he thong 2
        List<String> orgPaths = null;
        if (user2 != null) {
            orgPaths = user2.getListOrgPath() != null ? user2.getListOrgPath() : new ArrayList<String>();
        }
        System.err.println("\r\nuserId1: " + String.valueOf(userId1) + ", userId2: " + String.valueOf(userId2));
        if (orgPaths != null) {
            System.err.println("orgPathSize: " + orgPaths.size());
            for (String path : orgPaths) {
                System.err.println("org path: " + path);
            }
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
            String[] keys = new String[]{ConstantsFieldParams.KEYWORD, ConstantsFieldParams.TYPE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String keyword = listValue.get(0);
            String permistion = listValue.get(1);
            Integer category = (permistion != null) && (!permistion.trim().isEmpty()) ? Integer.parseInt(permistion.trim()) : null;
//            DocumentSearch solrSearching = DocumentSearch.getInstance(UserConfiguration.DOCUMENT_COLLECTION, new String[]{UserConfiguration.SOLR_SLAVE1_URL, UserConfiguration.SOLR_SLAVE2_URL});
            DocumentSearch solrSearching = DocumentSearch.getInstance(UserConfiguration.DOCUMENT_COLLECTION, new String[]{UserConfiguration.SOLR_MASTER_URL});
            Long result = solrSearching.getNumberOfGroup(orgPaths, userId1, userId2, keyword, category);
//            ElasticSearchDocument elasticSearch = new ElasticSearchDocument();
//            Long result  = elasticSearch.getDataCount(orgPaths, keyword);
            
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error("getCountDocument - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Lay danh sach cong van tim kiem
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListDocument(HttpServletRequest request, String data, String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        // Kiem tra session
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NO_SESSION, null, null);
        }

        // Neu user hoac user id tren he thong 1&2 null
        // -> Tra ve thong bao khong co quyen
        EntityUser user1 = userGroup.getItemEntityUser();
        Vof2_EntityUser user2 = userGroup.getVof2_ItemEntityUser();
        if ((user1 == null || user1.getUserId() == null) && (user2 == null || user2.getUserId() == null)) {
            logger.error("getListDocument - user hoac userId tren he thong 1&2 null");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Id nguoi dung tren he thong 1
        Long userId1 = (user1 != null) ? user1.getUserId() : null;
        // Id nguoi dung tren he thong 2
        Long userId2 = (user2 != null) ? user2.getUserId() : null;
        // Lay danh sach duong dan don vi tren he thong 2
        List<String> orgPaths = null;
        if (user2 != null) {
            orgPaths = user2.getListOrgPath() != null ? user2.getListOrgPath() : new ArrayList<String>();
        }
        System.err.println("\r\nuserId1: " + String.valueOf(userId1) + ", userId2: " + String.valueOf(userId2));
        if (orgPaths != null) {
            System.err.println("orgPathSize: " + orgPaths.size());
            for (String path : orgPaths) {
                System.err.println("org path: " + path);
            }
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
            String[] keys = new String[]{
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.TYPE,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Tu khoa tim kiem
            String keyword = listValue.get(0);
            // Loai doi tuong tim kiem
//            String permistion = listValue.get(1);
//            Integer category = (permistion != null) && (!permistion.trim().isEmpty()) ? Integer.parseInt(permistion.trim()) : null;

            // Ban ghi bat dau
            int startRecord = 0;
            if (listValue.get(2) != null && !"".equals(listValue.get(2).trim())) {
                startRecord = Integer.parseInt(listValue.get(2));
            }
            // So luong lay ra
            int pageSize = 10;
            if (listValue.get(3) != null && !"".equals(listValue.get(3).trim())) {
                pageSize = Integer.parseInt(listValue.get(3));
            }
            String permistion = listValue.get(1);
            Integer category = (permistion != null) && (!permistion.trim().isEmpty()) ? Integer.parseInt(permistion.trim()) : null;
//            DocumentSearch solrSearching = DocumentSearch.getInstance(UserConfiguration.DOCUMENT_COLLECTION, new String[]{UserConfiguration.SOLR_SLAVE1_URL, UserConfiguration.SOLR_SLAVE2_URL});
            DocumentSearch solrSearching = DocumentSearch.getInstance(UserConfiguration.DOCUMENT_COLLECTION, new String[]{UserConfiguration.SOLR_MASTER_URL});
            List<GroupEntity> entities = solrSearching.searchByCondition(orgPaths,
                    userId1, userId2, keyword, category, startRecord, pageSize);
            
//            ElasticSearchDocument elasticSearch = new ElasticSearchDocument();
//            List<GroupEntity> entities = elasticSearch.getDataSearch(orgPaths,keyword,Long.valueOf(startRecord), Long.valueOf(pageSize));
            
            
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (entities == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entities, aesKey);
            }
        } catch (JSONException ex) {
            logger.error("getStatusUser - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * thuc hien lay quyen phan loai user phục vu hien thi menu tim kiem
     *
     * @param req
     * @return
     */
    public String getStatusUser(HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        String strResult;
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                LogUtils.logFunctionalStart(log);

                Long staffIdV2 = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                DocumentDAO documentDAO = new DocumentDAO();
                Boolean isLeader = documentDAO.getStatusUser(staffIdV2);
                Integer strResut = 0;
                if (isLeader) {
                    strResut = 1;
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        strResut, strAesKeyDecode);
            } catch (Exception ex) {
                logger.error("getStatusUser - Exception:", ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * Lay so luong employee
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getCountEmployee(HttpServletRequest request, String data, String isSecurity) {

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
            String[] keys = new String[]{ConstantsFieldParams.STR_PARENT_ID, ConstantsFieldParams.KEYWORD};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String parentId = listValue.get(0);
            String keyword = listValue.get(1);

            List<String> orgPaths = new ArrayList<String>();
            OrgDAO orgDAO = new OrgDAO();
            List<Long> departmentIds = new ArrayList<Long>();
            String orgIds[] = parentId.split(",");
            if ((orgIds != null) && (orgIds.length > 0)) {
                String template = "^([\\d]+)_";
                Pattern pattern = Pattern.compile(template);
                for (String orgId : orgIds) {
                    orgId = orgId.trim();
                    Matcher matcher = pattern.matcher(orgId);
                    if (matcher.find()) {
                        orgId = matcher.group(1);
                    }
                    try {
                        departmentIds.add(Long.parseLong(orgId));
                    } catch (NumberFormatException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
            if (departmentIds.size() > 0) {
                List<EntityVhrOrg> deparments = orgDAO.getOrgById(departmentIds);
                if ((deparments != null) && (deparments.size() > 0)) {
                    for (EntityVhrOrg entity : deparments) {
                        if (!orgPaths.contains(entity.getPath())) {
                            orgPaths.add(entity.getPath());
                        }
                    }
                }
            }
            if (orgPaths.isEmpty()) {
                orgPaths.add(SolrSearchController.ROOT_PATH);
            }
//            EmployeeSearch employeeSearch = EmployeeSearch.getInstance(UserConfiguration.EMPLOYEE_COLLECTION, new String[]{UserConfiguration.SOLR_SLAVE1_URL, UserConfiguration.SOLR_SLAVE2_URL});
            EmployeeSearch employeeSearch = EmployeeSearch.getInstance(UserConfiguration.EMPLOYEE_COLLECTION, new String[]{UserConfiguration.SOLR_MASTER_URL});

            Long result = employeeSearch.getNumberOfEmployee(orgPaths, keyword);
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException ex) {
            logger.error("getCountEmployee - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * Lay danh sach cong van tim kiem
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String getListEmployee(HttpServletRequest request, String data, String isSecurity) {

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
            String[] keys = new String[]{
                ConstantsFieldParams.STR_PARENT_ID,
                ConstantsFieldParams.KEYWORD,
                ConstantsFieldParams.START_RECORD,
                ConstantsFieldParams.PAGE_SIZE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String parentId = listValue.get(0);
            // Tu khoa tim kiem
            String keyword = listValue.get(1);
            keyword = keyword.replaceAll("“", "\"").replaceAll("”", "\"");
//            System.out.println("keywordkeyword11="+keyword);
            // Ban ghi bat dau
            int startRecord = 0;
            if ((listValue.get(2) != null) && (!"".equals(listValue.get(2).trim()))) {
                startRecord = Integer.parseInt(listValue.get(2).trim());
            }
            // So luong lay ra
            int pageSize = 10;
            if ((listValue.get(3) != null) && (!"".equals(listValue.get(3).trim()))) {
                pageSize = Integer.parseInt(listValue.get(3).trim());
            }

            List<String> orgPaths = new ArrayList<>();
            OrgDAO orgDAO = new OrgDAO();
            List<Long> departmentIds = new ArrayList<>();
            String orgIds[] = parentId.split(",");
            if ((orgIds != null) && (orgIds.length > 0)) {
                String template = "^([\\d]+)_";
                Pattern pattern = Pattern.compile(template);
                for (String orgId : orgIds) {
                    orgId = orgId.trim();
                    Matcher matcher = pattern.matcher(orgId);
                    if (matcher.find()) {
                        orgId = matcher.group(1);
                    }
                    try {
                        departmentIds.add(Long.parseLong(orgId));
                    } catch (NumberFormatException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
                if (departmentIds.size() > 0) {
                    List<EntityVhrOrg> deparments = orgDAO.getOrgById(departmentIds);
                    if ((deparments != null) && (deparments.size() > 0)) {
                        for (EntityVhrOrg entity : deparments) {
                            if (!orgPaths.contains(entity.getPath())) {
                                orgPaths.add(entity.getPath());
                            }
                        }
                    }
                }
            }
            if (orgPaths.isEmpty()) {
                orgPaths.add(SolrSearchController.ROOT_PATH);
            }
//            EmployeeSearch employeeSearch = EmployeeSearch.getInstance(UserConfiguration.EMPLOYEE_COLLECTION, new String[]{UserConfiguration.SOLR_MASTER_URL});
//            List<EmployeeEntity> entities = employeeSearch.searchByCondition(orgPaths,
//                    keyword, startRecord, pageSize);
            ElasticSearchVhrEmp a = new ElasticSearchVhrEmp();
            List<com.viettel.voffice.elasticsearch.search.entity.EmployeeEntity> 
                    entities = a.getDataSearch(keyword,orgPaths, Long.valueOf(startRecord)
                    , Long.valueOf(pageSize));
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            if (entities == null) {
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entities, aesKey);
            }
        } catch (JSONException ex) {
            logger.error("getListEmployee - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        }
    }

    /**
     * index employee list
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String indexEmployee(HttpServletRequest request, String data, String isSecurity) {

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
//        System.out.println("indexing data client gui len: " + data);
        try {
            // Lay du lieu client gui len
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{ConstantsFieldParams.LIST_EMPLOYEE};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // danh sach employee
            String value = listValue.get(0);
//            System.out.println("data: " + value);
            Type employeeType = new TypeToken<ArrayList<EmployeeEntity>>() {
            }.getType();
            Gson gson = new Gson();
            List<EmployeeEntity> entities = gson.fromJson(value, employeeType);

            boolean valid = false;
            if ((entities != null) && (entities.size() > 0)) {
                IndexingEmployee employeeManager = IndexingEmployee.getInstance(UserConfiguration.EMPLOYEE_INDEXING_URL);
                valid = employeeManager.addEmployee(entities);
            }
//            System.out.println("valid: " + String.valueOf(valid));
            // Ghi log ket thuc chuc nang
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, valid ? 1 : 0, aesKey);
        } catch (JSONException ex) {
            logger.error("indexEmployee - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
        } catch (IOException ex) {
            logger.error("indexEmployee - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        } catch (SolrServerException ex) {
            logger.error("indexEmployee - Exception:", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
