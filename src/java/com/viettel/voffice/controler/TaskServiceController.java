/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.task.PersonTaskDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.task.EntityCommanderOrg;
import com.viettel.voffice.database.entity.task.EntityCommanderTree;
import com.viettel.voffice.database.entity.task.EntityEnforcement;
import com.viettel.voffice.database.entity.task.EntityRequestList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.task.TaskDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.document.EntityDocumentInStaff;
import com.viettel.voffice.database.entity.task.EntityEmpRating;
import com.viettel.voffice.database.entity.task.EntitySourceMap;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.elasticsearch.indexdata.IndexDocumentByType;
import com.viettel.voffice.threadmanager.ThreadPoolCommon;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 *
 * @author kiennt45
 */
public class TaskServiceController {

    public static final String ROOT_ACTION = "TaskService";
    // Log file
    private static final Logger LOGGER = Logger.getLogger(TaskServiceController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = TaskServiceController.class.getName();

    /**
     * <b>Them/Sua cong viec ca nhan</b>
     *
     * @param isSecurity
     * @param strData
     * @param req
     * @return
     */
    public String addTask(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay gia tri tu session
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                Long employeeId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                //Long orgId = dataSessionGR.getVof2_ItemEntityUser().getSysOrgId();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                SimpleDateFormat parFormatter = new SimpleDateFormat("yyyyMM");

                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TASK_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_NAME, String.class);
                hmParams.put(ConstantsFieldParams.TASK_CONTENT, String.class);
                hmParams.put(ConstantsFieldParams.TASK_START_TIME, String.class);
                hmParams.put(ConstantsFieldParams.TASK_END_TIME, String.class);
                //hmParams.put(ConstantsFieldParams.TASK_TARTGET, String.class);
                //hmParams.put(ConstantsFieldParams.TASK_SOURCE_TYPE, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_WEIGHT, String.class);
                hmParams.put(ConstantsFieldParams.TASK_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_COMMANDER_ID, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_ENFORCEMENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_UPDATE_FREQUENCY, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_FIELD_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_COMMAND_TYPE, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_PARENT_ID, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_ORG_ID, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_CREATED_DATE, String.class);
                hmParams.put(ConstantsFieldParams.TASK_COMPLETED_PERCENT, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_RESULT, String.class);
                //hmParams.put(ConstantsFieldParams.TASK_PARATITION_BY, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_PATH, String.class);
                hmParams.put(ConstantsFieldParams.TASK_RATING_POINT, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_RECEIVER, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_RECEIVER_COMMENT, String.class);
                //hmParams.put(ConstantsFieldParams.TASK_IS_CLOSED, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_TYPE2, Long.class);
                //hmParams.put(ConstantsFieldParams.TASK_IS_MAJOR, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_STATE, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_ACTION_LOG, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_PROCESS_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.STR_SOURCEID, Long.class);
                hmParams.put(ConstantsFieldParams.STR_SOURCE_NAME, String.class);
                hmParams.put(ConstantsFieldParams.STR_SOURCE_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.STR_OBJECT_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.STATUS, Long.class);
                //HaNH: Bo sung tham so de phan biet client WEB voi cac client khac
                hmParams.put(ConstantsFieldParams.IS_WEB, Integer.class);
                hmParams.put(ConstantsFieldParams.LIST_FILE_ATTACH, String.class);
                hmParams.put(ConstantsFieldParams.TASK_RECEIVER, Long.class);
                // LinhLN Start bo sung taskGroup, weight
                hmParams.put(ConstantsFieldParams.TASK_GROUP, Long.class);
                hmParams.put(ConstantsFieldParams.WEIGHT, Long.class);
                // LinhLN End bo sung taskGroup, weight
                //minhnq add
                hmParams.put(ConstantsFieldParams.TASK_PERIOD, String.class);
                hmParams.put("objectTypeTask", Long.class);
                hmParams.put(ConstantsFieldParams.TASK_WORK_LOCATION, String.class);
                hmParams.put(ConstantsFieldParams.TASK_KPI_INDEX, String.class);
                hmParams.put(ConstantsFieldParams.TASK_RECIPE, String.class);
                hmParams.put(ConstantsFieldParams.TASK_UNIT, String.class);
                hmParams.put(ConstantsFieldParams.TASK_TARGET_MIN, String.class);
                hmParams.put(ConstantsFieldParams.TASK_TARGET_EXPECT, String.class);
                hmParams.put(ConstantsFieldParams.TASK_TARGET_CHALLENGE, String.class);
                hmParams.put(ConstantsFieldParams.TASK_PERCENT, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_PERIOD_TYPE, Long.class);
                //minhnq end
                //100517 them thong tin co chuyen van ban hay khong
                //0: khong chuyen; 1 hoac null: chuyen
                Long isSend = null;
                if (FunctionCommon.jsonGetItem("isSend", strDataClient) != null
                        && !"".equals(FunctionCommon.jsonGetItem("isSend", strDataClient))) {
                    isSend = Long.parseLong(FunctionCommon.jsonGetItem("isSend", strDataClient).toString());
                }

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                //Begin HaNH
                //Danh sach file dinh kem
                List<EntityFileAttachment> listAttachFile = new LinkedList<>();
                //Danh sach nguon goc
                List<EntitySourceMap> listSourceMap = new LinkedList<>();
                //End HaNH
                Integer isWeb = (Integer) valueParams.get(ConstantsFieldParams.IS_WEB);
                Long taskId = (Long) valueParams.get(ConstantsFieldParams.TASK_ID);
                String taskName = (String) valueParams.get(ConstantsFieldParams.TASK_NAME);
                String content = (String) valueParams.get(ConstantsFieldParams.TASK_CONTENT);
                String startTime = ((String) valueParams.get(ConstantsFieldParams.TASK_START_TIME)).replace("\\", "");
                String endTime = ((String) valueParams.get(ConstantsFieldParams.TASK_END_TIME)).replace("\\", "");
                String target = content;
                Long taskSourceType = 1L;
                // LinhLN Start bo sung taskGroup, weight
                Long taskGroup = (Long) valueParams.get(ConstantsFieldParams.TASK_GROUP);       
                Long weight = (Long) valueParams.get(ConstantsFieldParams.WEIGHT);  // 
                // LinhLN End bo sung taskGroup, weight
                //minhnq add
                String period = (String) valueParams.get(ConstantsFieldParams.TASK_PERIOD);
                Long objectTypeTask = (Long) valueParams.get("objectTypeTask");
                String workLocation = (String) valueParams.get(ConstantsFieldParams.TASK_WORK_LOCATION);
                String kpiIndex = (String) valueParams.get(ConstantsFieldParams.TASK_KPI_INDEX);
                String recipe = (String) valueParams.get(ConstantsFieldParams.TASK_RECIPE);
                String unit = (String) valueParams.get(ConstantsFieldParams.TASK_UNIT);
                String targetMin = (String) valueParams.get(ConstantsFieldParams.TASK_TARGET_MIN);
                String targetExpect = (String) valueParams.get(ConstantsFieldParams.TASK_TARGET_EXPECT);
                String targetChallenge = (String) valueParams.get(ConstantsFieldParams.TASK_TARGET_CHALLENGE);
                Long percent = (Long) valueParams.get(ConstantsFieldParams.TASK_PERCENT);
                Long periodtype = (Long) valueParams.get(ConstantsFieldParams.TASK_PERIOD_TYPE);
                //minhnq
                Long taskType = (Long) valueParams.get(ConstantsFieldParams.TASK_TYPE);                             //1: Chức năng, 2 : Nề nếp
                Long commanderId = (Long) ((valueParams.get(ConstantsFieldParams.TASK_COMMANDER_ID) != null) ? valueParams.get(ConstantsFieldParams.TASK_COMMANDER_ID) : employeeId);                  //Id người giao việc
                Long updateFrequency = (Long) valueParams.get(ConstantsFieldParams.TASK_UPDATE_FREQUENCY);          //1:  Cập nhật theo ngày, 2: Cập nhật theo ngày tuần, 3: Cập nhật theo ngày tháng
                Long fieldId = (Long) valueParams.get(ConstantsFieldParams.TASK_FIELD_ID);                          //Id lĩnh vực
                Long commandType = (Long) valueParams.get(ConstantsFieldParams.TASK_COMMAND_TYPE);                  //Loai giao viec: 1: Cong viec duoc giao 2: Ca nhan de xuat 3: Cong Viec da ban giao 4: Cong viec phoi hop
                Long parentId = (Long) valueParams.get(ConstantsFieldParams.TASK_PARENT_ID);                      //Id công việc cha
                //Long parentId = null;
                //Long orgId = (Long) valueParams.get(ConstantsFieldParams.TASK_ORG_ID);                            //Id đơn vị của người giao việc
                Long completedPercent = (Long) ((valueParams.get(ConstantsFieldParams.TASK_COMPLETED_PERCENT) != null) ? valueParams.get(ConstantsFieldParams.TASK_COMPLETED_PERCENT) : 0L);//(Long) valueParams.get(ConstantsFieldParams.TASK_COMPLETED_PERCENT);    //% hoàn thành công việc
                Long isCompleted = 0L;//(Long) valueParams.get(ConstantsFieldParams.TASK_IS_COMPLETED);              //0 chua thanh cong viec, 1 da hoan thanh cong viec
                String createdDate = formatter.format(new Date());
                Long createdBy = employeeId;
                Long delFlag = 0L;
                String taskResult = (String) valueParams.get(ConstantsFieldParams.TASK_RESULT);
                String paratitionBy = parFormatter.format(new Date());                                              //Format ngay thang theo dinh dang yyyyMM
                String taskPath = "";                         //Path của công việc
                Long ratingPoint = (Long) valueParams.get(ConstantsFieldParams.TASK_RATING_POINT);                  //1: Kém 2: Cần cố gắng 3: Đạt yêu cầu 4: Khá 5: Tốt 6: Xuất sắc                Long receiver = (Long) valueParams.get(ConstantsFieldParams.TASK_RECEIVER);
                //HaNH: Bo sung cho WEB key receiver                                                                               //1: Chưa tiếp nhận, 2: Đã tiếp nhận, 3: Đã từ chối
                Long receiver = (Long) valueParams.get(ConstantsFieldParams.TASK_RECEIVER);
                String receiverComment = null;
                Long isClosed = null;                                                                               //0 chưa đóng, 1 công việc đã đóng
                Long taskType2 = (Long) valueParams.get(ConstantsFieldParams.TASK_TYPE2);                      //1: Công việc thương xuyên, 2: Công việc đột xuất

                //Long isMajor = (Long) valueParams.get(ConstantsFieldParams.TASK_IS_MAJOR);                          //Chon chu tri(=1) Phoi hop (=2)
                //Long enforcementId = (Long) valueParams.get(ConstantsFieldParams.TASK_ENFORCEMENT_ID);              //Id người thực hiện
                Long state = (Long) valueParams.get(ConstantsFieldParams.TASK_STATE);                               //1 - Draff, 2 - Reject, 3 - Approve
                Long actionLog = (Long) valueParams.get(ConstantsFieldParams.TASK_ACTION_LOG);                      //1 - Tao moi cong viec 2 - Sua cong viec 3 - Cap nhat tien do 4 - Chia nho cong viec 5 - Ban giao cong viec 6 - Phe duyet tien do cong viec
                Long processType = (Long) valueParams.get(ConstantsFieldParams.TASK_PROCESS_TYPE);                  //Loai update: 0. Them moi 1. Cap nhat tien do 2. Chia nho 3. Chuyen 4. Phe duyet tien do 5. Tu choi
                Long sourceId = null;                          //Id nguồn gốc
                String sourceName = null;                 //Tên nguồn gốc
                Long sourceType = (Long) valueParams.get(ConstantsFieldParams.STR_SOURCE_TYPE);                    //Loại nguồn gốc
                Long objectType = (Long) valueParams.get(ConstantsFieldParams.STR_OBJECT_TYPE);                  //Loại đối tượng
                List<Long> listTextId = new ArrayList<>();
                //HaNH: Chi tren WEB moi co key "listSourceMap"
                if (isWeb != null && isWeb.equals(1)) {
                    JSONArray arrListSourceMap = null;
                    JSONArray arrListFileAttach = null;
                    if (strDataClient.contains("\"listSourceMap\"")) {
                        arrListSourceMap = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_SOURCE_MAP, strDataClient);
                    }
                    if (strDataClient.contains("\"listFileAttach\"")) {
                        arrListFileAttach = FunctionCommon.jsonGetArray(ConstantsFieldParams.LIST_FILE_ATTACH, strDataClient);
                    }
                    if (arrListSourceMap != null && arrListSourceMap.length() > 0) {
                        EntitySourceMap sourceMapItem;
                        for (int s = 0; s < arrListSourceMap.length(); s++) {
                            //Get object cua tung doi tuong mang
                            JSONObject objSource = (JSONObject) arrListSourceMap.get(s);
                            sourceMapItem = new EntitySourceMap();
                            try {
                                sourceMapItem.setObjectType(objSource.getInt("objectType"));
                                sourceMapItem.setSourceType(objSource.getInt("sourceType"));
                                //Neu sua cong viec thi truyen key sourceMapId cua nhung nguon goc bi remove
                                if (objSource.has("sourceMapId")) {
                                    sourceMapItem.setSourceMapId(objSource.getLong("sourceMapId"));
                                }
                                //Neu nguon goc tu ke hoach ca nhan thi khong co sourceName
                                if (objSource.has("sourceName")) {
                                    sourceMapItem.setSourceName(objSource.getString("sourceName"));
                                }
                                //Neu la nguon goc khac (sourceType = 5) thi khong co sourceId
                                if (objSource.has("sourceId")) {
                                    Integer source_Type = objSource.getInt("sourceType");
                                    Long source_Id = objSource.getLong("sourceId");
                                    if(source_Type == 2){
                                        listTextId.add(source_Id);
                                    }
                                    sourceMapItem.setSourceId(source_Id);
                                }
                                //Neu la nguon goc tu van ban (SourceType = 2) thi moi co Do mat
                                if (objSource.has("confidential")) {
                                    sourceMapItem.setConfidential(objSource.getInt("confidential"));
                                }
                                //100517 them thong tin co chuyen van ban hay khong
                                if (objSource.has("isSend")) {
                                    sourceMapItem.setIsSend(objSource.getLong("isSend"));
                                }
                                listSourceMap.add(sourceMapItem);
                            } catch (JSONException e) {
                                LOGGER.error("Loi! JSONException", e);
                            }
                        }
                    }
                    //HaNH: Chi co tren WEB - Danh sach file dinh kem
                    if (arrListFileAttach != null && arrListFileAttach.length() > 0) {
                        EntityFileAttachment fileItem;
                        for (int f = 0; f < arrListFileAttach.length(); f++) {
                            //Get object cua tung doi tuong mang
                            JSONObject objFile = (JSONObject) arrListFileAttach.get(f);
                            //String fileName = objFile.getString("fileName");
                            fileItem = new EntityFileAttachment();
                            try {
                                Long fileId = null;
                                if (objFile.has("fileId")) {
                                    fileId = objFile.getLong("fileId");
                                    fileItem.setFileAttachmentId(fileId);
                                }
                                fileItem.setFileName(objFile.getString("fileName"));
                                fileItem.setFilePath(objFile.getString("filePath"));
                                listAttachFile.add(fileItem);
                            } catch (JSONException e) {
                                LOGGER.error("Loi! JSONException", e);
                            }
                        }
                    }
                } else {
                    sourceId = (Long) valueParams.get(ConstantsFieldParams.STR_SOURCEID);                          //Id nguồn gốc
                    sourceName = (String) valueParams.get(ConstantsFieldParams.STR_SOURCE_NAME);                 //Tên nguồn gốc
                    sourceType = (Long) valueParams.get(ConstantsFieldParams.STR_SOURCE_TYPE);                     //Loại nguồn gốc
                    objectType = (Long) valueParams.get(ConstantsFieldParams.STR_OBJECT_TYPE);                     //Loại đối tượng
                }
                Long status = (Long) ((valueParams.get(ConstantsFieldParams.STATUS) != null) ? valueParams.get(ConstantsFieldParams.STATUS) : 1L);

                //Chuan hoa lai thoi gian
                startTime = formatter.format(formatter.parse(startTime));
                endTime = formatter.format(formatter.parse(endTime));
                taskName = taskName.trim();
                content = content.trim();
                JSONArray arrEnforcement;
                if (strDataClient.contains("\"listEnforcement\""));
                arrEnforcement = FunctionCommon.jsonGetArray(ConstantsFieldParams.TASK_LIST_ENFORCEMENT, strDataClient);

                List<EntityEnforcement> listEnforcement = new LinkedList<EntityEnforcement>();                
                if (arrEnforcement != null && arrEnforcement.length() > 0) {
                    for (int i = 0; i < arrEnforcement.length(); i++) {
                        try {
                            //Get object cua tung doi tuong mang
                            JSONObject innerObj = (JSONObject) arrEnforcement.get(i);
                            Long enforcementId = innerObj.getLong(ConstantsFieldParams.TASK_ENFORCEMENT_ID);
                            Long isMajor = innerObj.getLong(ConstantsFieldParams.TASK_IS_MAJOR);
                            Long orgId = innerObj.getLong(ConstantsFieldParams.TASK_ORG_ID);
                            Long commandTypeEnf = null;
                            if (innerObj.has(ConstantsFieldParams.TASK_COMMAND_TYPE)) {
                                commandTypeEnf = innerObj.getLong(ConstantsFieldParams.TASK_COMMAND_TYPE);
                            }
                            //Nếu client ko gửi lên người thực hiện thì mặc định lấy user đăng nhập
                            if (enforcementId == null) {
                                enforcementId = employeeId;
                            }
                            EntityEnforcement enforcementItem = new EntityEnforcement();
                            enforcementItem.setEnforcementId(enforcementId);
                            enforcementItem.setIsMajor(isMajor);
                            enforcementItem.setOrgId(orgId);
                            enforcementItem.setCommandType(commandTypeEnf);
                            listEnforcement.add(enforcementItem);
                        } catch (JSONException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                    Collections.sort(listEnforcement);
//                    System.out.println("List size:" + arrEnforcement.length() + " ----" + listEnforcement.size());
//                    System.out.println("Input Data:" + taskName + " ----" + content + "----" + endTime + "----" + startTime + "----");
                } else {
//                    if (isWeb != null && isWeb.equals(1)) {
//                        return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
//                    }
                    LOGGER.info("addTask - username: " + dataSessionGR.getCardId()
                            + " - Khong chon nguoi thuc hien!");
                    EntityEnforcement enforcementItem = new EntityEnforcement();
                    enforcementItem.setEnforcementId(employeeId);
                    enforcementItem.setIsMajor(1L);
                    enforcementItem.setOrgId(dataSessionGR.getVof2_ItemEntityUser().getSysOrgId());
                    enforcementItem.setCommandType(commandType);
                    listEnforcement.add(enforcementItem);
                }
                //kiem tra du lieu dau vao
                if ( content == null
                        || content.isEmpty() || endTime.isEmpty()
                        || listEnforcement.size() <= 0) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
                }

                PersonTaskDAO taskDao = new PersonTaskDAO();

                //Nếu client ko gửi lên người giao thì mặc định lấy user đăng nhập
                if (commanderId == null) {
                    commanderId = employeeId;
                }
                // Bo nghiep vu nguoi giao tao moi/sua viec ma tien do 100% thi cap nhat is_completed = 1
//                if (commanderId != null && commanderId.equals(employeeId)) {
//                    if (completedPercent.equals(100L)) {
//                        isCompleted = 1L;
//                    }
//                }
                if (taskId == null) {
                    //Tạo mới công việc
                    Integer result = taskDao.addTask(taskName, content, startTime,
                            endTime, target, taskSourceType, weight, taskType,
                            commanderId, updateFrequency, fieldId, commandType,
                            parentId, completedPercent, isCompleted, createdDate,
                            createdBy, delFlag, taskResult, paratitionBy,
                            taskPath, ratingPoint, receiver, receiverComment,
                            isClosed, taskType2, state, actionLog, processType,
                            listEnforcement, sourceId, sourceName, sourceType,
                            objectType, status, isWeb, listSourceMap, listAttachFile,
                            cardId, taskGroup, period, objectTypeTask, workLocation,
                            kpiIndex, recipe, unit, targetMin, targetExpect,
                            targetChallenge, percent, periodtype);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                    if (result > 0) {
                        //datnv5: update kho index van ban neu co
                        if(listSourceMap.size() > 0){
                            for (EntitySourceMap entitySourceMap : listSourceMap) {
                                if(entitySourceMap.getSourceType() == 2){
                                    IndexDocumentByType indexDocumentByType 
                                            = new IndexDocumentByType(entitySourceMap.getSourceId());
                                    ThreadPoolCommon.putRunnable(indexDocumentByType);
                                }
                            }
                        }
                        if(sourceType!=null && sourceType == 2){
                            IndexDocumentByType indexDocumentByType 
                                            = new IndexDocumentByType(sourceId);
                                    ThreadPoolCommon.putRunnable(indexDocumentByType);
                        }
                        
                        
                        //100517 chuyen van ban cho TT, LD, VT don vi
                        //neu sua viec co nguon goc tu van ban
                        sendDocToPersonTask(isWeb, listSourceMap, dataSessionGR.getItemEntityUser(),
                                dataSessionGR.getVof2_ItemEntityUser(), sourceType,
                                objectType, isSend, listEnforcement, sourceId);
                        //<editor-fold defaultstate="collapsed" desc="Gui tin nhan giao viec">
                        //Lay config noi dung tin nhan multi language
                        String strMess;
                        SmsDAO smsDao = new SmsDAO();
                        CommonControler controler = new CommonControler();
                        List<String> listMess = new ArrayList<>();

                        listMess.add(smsDao.gettAliasNameByUserId(commanderId)); //Ten nguoi giao
                        listMess.add(taskName);

                        // Duyet tung nguoi thuc hien cong viec
                        for (int i = 0; i < listEnforcement.size(); i++) {
                            // Khong gui tin nhan cho nguoi thuc hien la nguoi tao viec
                            if (!listEnforcement.get(i).getEnforcementId().equals(employeeId)) {
                                String receipent = smsDao.getPhoneNumberRecv(
                                        listEnforcement.get(i).getEnforcementId());
                                strMess = controler.getStrMessConfig(listMess, 3L,
                                        Constants.SMS_TEXT_CONFIG.PERSON_TASK_ASSGIN,
                                        listEnforcement.get(i).getEnforcementId());
                                // Insert vao bang sms_master de gui tin nhan
                                smsDao.addMsgToSmsMaster(receipent, strMess,
                                        employeeId, commanderId,
                                        listEnforcement.get(i).getEnforcementId(),
                                        11L, Constants.SMS_TEXT_INTERCEPT.RECEIVEDTASK_DOTASK);
                            }
                        }
                        //</editor-fold>

                        if (isWeb != null && isWeb.equals(1)) {
                            //Begin::cuongnv::cap nhat xu ly vb va gui tin nhan bao xu ly vb cho tro ly tren web
                            //Sua theo yeu cau anh Luanvd
                            if (!CommonUtils.isEmpty(listSourceMap)) {
                                for (EntitySourceMap sm : listSourceMap) {
                                    sourceType = sm.getSourceType().longValue();
                                    objectType = sm.getObjectType().longValue();
                                    if (sourceType != null && sourceType == 4L && objectType != null && objectType == 1L) {
                                        // Lay userId tren he thong 1
//                                        Long userIdVof1 = (dataSessionGR.getItemEntityUser() != null) ? dataSessionGR.getItemEntityUser().getUserId() : 0L;
                                        Long userIdVof1 = null;
                                        DocumentDAO doc = new DocumentDAO();

                                        // <b> kiem tra xem van ban co gui sms cho tro ly khong? </b>
                                        // author: OS/sonnd
                                        // begin
                                        Long assistantId = 0L;
                                        Long leaderId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                                        List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, sm.getSourceId(), 0);
                                        boolean isSendSms;
                                        if (lstStaff != null && !CommonUtils.isEmpty(lstStaff)) {
                                            assistantId = lstStaff.get(0).getStaffId2();
                                        } else {
//                                            isSendSms = false;
                                        }
                                        if (assistantId.equals(0L)) {
                                            isSendSms = false;
                                        } else if (smsDao.checkDocumentSendByAssistant(assistantId, leaderId, sm.getSourceId()) != 1) {
                                            isSendSms = false;
//                                        } else if ((smsDao.checkAssistantOfLeader(assistantId, leaderId) == 1)) {
//                                            isSendSms = true;
                                        } else {
                                            isSendSms = true;
                                        }
                                        LOGGER.error("Giao viec tu van ban tren web: " + isSendSms);
                                        // end kiem tra truoc khi cap nhat trang thai xu ly.
                                        //Cap nhat trang thai xu ly
                                        // <b> sau khi cap nhat thanh cong, se thuc hien gui sms </b>
                                        // author: OS/sonnd
                                        // begin
                                        if (doc.updateDocumentProcessing(sm.getSourceId(), userIdVof1, employeeId, null) == 1) {
                                            LOGGER.error("updateDocumentProcessing: ");
                                            List<EntityVhrEmployee> lstPerform = new ArrayList();
                                            EntityDocument documentEntity = smsDao.getDocument(sourceId);

                                            for (EntityEnforcement enforceMentTemp : listEnforcement) {
                                                EntityVhrEmployee emp = new EntityVhrEmployee();
                                                emp.setEmployeeId(enforceMentTemp.getEnforcementId());
                                                lstPerform.add(emp);
                                            }
                                            if (isSendSms && !CommonUtils.isEmpty(lstPerform)
                                                    && documentEntity != null) {
                                                LOGGER.error("sendSmsAssistantDocument: " + lstPerform.size());
                                                smsDao.sendSmsAssistantDocument(lstPerform,
                                                        null, null, null, assistantId, taskName,
                                                        dataSessionGR.getVof2_ItemEntityUser().getAliasName(),
                                                        documentEntity.getTitle(),
                                                        Constants.SMS_TEXT_INTERCEPT.SECRETARY_LEADERHANDLEDOC);
                                            }
                                        }
                                        // end cap nhat va gui tin nhan.
                                    }
                                }
                            }
                            //End
                        } else {
                            //Danh cho mobile
                            //<editor-fold defaultstate="collapsed" desc="Xu ly neu tao nhiem vu co nguon goc tu van bản thì chuyển văn bản đó sang trạng thái đã xử lý">
                            if (sourceType != null && sourceType == 4L && objectType != null && objectType == 1L) {
                                // Lay userId tren he thong 1
//                                Long userIdVof1 = (dataSessionGR.getItemEntityUser() != null) ? dataSessionGR.getItemEntityUser().getUserId() : 0L;
                                Long userIdVof1 = null;
                                DocumentDAO doc = new DocumentDAO();

                                // <b> kiem tra xem van ban co gui sms cho tro ly khong? </b>
                                // author: OS/sonnd
                                // begin
                                Long assistantId = 0L;
                                Long leaderId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                                List<EntityDocumentInStaff> lstStaff = smsDao.getDocumentStaff(leaderId, sourceId, 0);
                                boolean isSendSms;
                                if (lstStaff != null && !CommonUtils.isEmpty(lstStaff)) {
                                    assistantId = lstStaff.get(0).getStaffId2();
                                } else {
//                                    isSendSms = false;
                                }
                                if (assistantId.equals(0L)) {
                                    isSendSms = false;
                                } else if (smsDao.checkDocumentSendByAssistant(assistantId, leaderId, sourceId) != 1) {
                                    isSendSms = false;
//                                } else if ((smsDao.checkAssistantOfLeader(assistantId, leaderId) == 1)) {
//                                    isSendSms = true;
                                } else {
                                    isSendSms = true;
                                }
                                // end kiem tra truoc khi cap nhat trang thai xu ly.
                                //Cap nhat trang thai xu ly
                                // <b> sau khi cap nhat thanh cong, se thuc hien gui sms </b>
                                // author: OS/sonnd
                                // begin
                                if (doc.updateDocumentProcessing(sourceId, userIdVof1, employeeId, null) == 1) {

                                    List<EntityVhrEmployee> lstPerform = new ArrayList();
                                    EntityDocument documentEntity = smsDao.getDocument(sourceId);

                                    for (EntityEnforcement enforceMentTemp : listEnforcement) {
                                        EntityVhrEmployee emp = new EntityVhrEmployee();
                                        emp.setEmployeeId(enforceMentTemp.getEnforcementId());
                                        lstPerform.add(emp);
                                    }
                                    if (isSendSms && !CommonUtils.isEmpty(lstPerform)
                                            && documentEntity != null) {
                                        smsDao.sendSmsAssistantDocument(lstPerform,
                                                null, null, null, assistantId, taskName,
                                                dataSessionGR.getVof2_ItemEntityUser().getAliasName(),
                                                documentEntity.getTitle(),
                                                Constants.SMS_TEXT_INTERCEPT.SECRETARY_LEADERHANDLEDOC);
                                    }
                                }
                                // end cap nhat va gui tin nhan.
                            }
                        }
                        //</editor-fold>
                    }
                } else {
                    //lay chi tiet cong viec
                    TaskDAO taskDAO = new TaskDAO();
                    EntityTask taskObj = taskDAO.getTaskDetail(taskId, employeeId, "vi");
                    //Sửa công việc
                    Integer result = taskDao.editTaskById(taskId, taskName,
                            content, startTime, endTime, target, taskSourceType,
                            weight, taskType, commanderId, updateFrequency,
                            fieldId, commandType, parentId, completedPercent,
                            isCompleted, createdDate, createdBy, delFlag,
                            taskResult, paratitionBy, taskPath, ratingPoint,
                            receiver, receiverComment, isClosed, taskType2,
                            state, actionLog, processType, listEnforcement,
                            sourceId, sourceName, sourceType, objectType, status,
                            isWeb, listSourceMap, listAttachFile, cardId, taskGroup, period, 
                            objectTypeTask,workLocation,kpiIndex,recipe,unit,targetMin,targetExpect,targetChallenge,percent,periodtype);

                    //<editor-fold defaultstate="collapsed" desc="Gui tin nhan giao viec">
                    if (result > 0) {
                        //100517 chuyen van ban cho TT, LD, VT don vi
                        //neu sua viec co nguon goc tu van ban
                        if (!listSourceMap.isEmpty()) {
                            //neu co nguon goc thi chuyen cho van ban trong nguon goc
                            sendDocToPersonTask(isWeb, listSourceMap, dataSessionGR.getItemEntityUser(),
                                    dataSessionGR.getVof2_ItemEntityUser(), sourceType,
                                    objectType, isSend, listEnforcement, sourceId);
                        } else {
                            //neu khong sua nguon goc thi lay nguon goc cu
                            List<Vof2_EntityUser> listStaff = new ArrayList<>();
                            Vof2_EntityUser vof2EntityUser1;
                            for (EntityEnforcement enforceMentTemp : listEnforcement) {
                                vof2EntityUser1 = new Vof2_EntityUser();
                                vof2EntityUser1.setUserId(enforceMentTemp.getEnforcementId());
                                listStaff.add(vof2EntityUser1);
                            }
                            TaskController taskController = new TaskController();
                            taskController.sendDocToPersonTask(taskId, listStaff, dataSessionGR.getItemEntityUser(),
                                    dataSessionGR.getVof2_ItemEntityUser(), taskObj.getTaskPath());
                        }
                        //Lay config noi dung tin nhan multi language
                        String strMess;
                        SmsDAO smsDao = new SmsDAO();
                        CommonControler controler = new CommonControler();
                        List<String> listMess = new ArrayList<>();
                        listMess.add(smsDao.gettAliasNameByUserId(commanderId)); //Ten nguoi giao
                        listMess.add(taskName);
                        Long enforcementOldId = taskObj.getEnforcementId();
                        Long enforcementNewId;
                        String receipent;
                        List<String> listObj = new ArrayList<>();
                        listObj.add(taskObj.getContent());
                        // Duyet tung nguoi thuc hien cong viec
                        for (int i = 0; i < listEnforcement.size(); i++) {
                            enforcementNewId = listEnforcement.get(i).getEnforcementId();
                            receipent = smsDao.getPhoneNumberRecv(enforcementNewId);
                            // Thay nguoi thuc hien
                            if (enforcementNewId != null && (enforcementOldId == null
                                    || !enforcementOldId.equals(enforcementNewId))) {                                
                                strMess = controler.getStrMessConfig(listMess, 3L,
                                        Constants.SMS_TEXT_CONFIG.PERSON_TASK_ASSGIN, enforcementNewId);
                                // Insert vao bang sms_master
                                smsDao.addMsgToSmsMaster(receipent, strMess, employeeId,
                                        commanderId, enforcementNewId, 11L,
                                        Constants.SMS_TEXT_INTERCEPT.RECEIVEDTASK_DOTASK);
                            } // Khong thay doi nguoi thuc hien
                            // Nguoi chinh sua la nguoi giao
                            else if (taskObj.getCommanderId() != null
                                    && taskObj.getCommanderId().equals(employeeId)) {                                
                                strMess = controler.getStrMessConfig(listObj, 3L,
                                        Constants.SMS_TEXT_CONFIG.ASSIGNER_UPDATE_TASK, enforcementOldId);
                                // Insert vao bang sms_master
                                smsDao.addMsgToSmsMaster(receipent, strMess, employeeId,
                                        commanderId, enforcementNewId, 11L,
                                        Constants.SMS_TEXT_INTERCEPT.RECEIVEDTASK_DOTASK);
                            }                            
                        }
                    }
                    //</editor-fold>
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (ParseException | JSONException ex) {
                LOGGER.error("addTask - ParseException | JSONException!", ex);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            } catch (Exception ex) {
                LOGGER.error("addTask - Exception!", ex);
                return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * lay ra cay don vi chon nguoi giao viec
     *
     * @param none, hien tai ko su dung ham nay
     * @return
     */
    public String getListCommander(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TASK_NAME, String.class);

                Long employeeId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                Long orgId = dataSessionGR.getVof2_ItemEntityUser().getAdOrgId();
                String orgName = dataSessionGR.getVof2_ItemEntityUser().getSysOrgName();
                Boolean isLeader = false;       //Danh dau xem no co phai la Lanh dao/Thu truong hay khong
                Boolean isAssistant = false;    //Danh dau xem no co phai la Tro Ly hay khong
                Boolean isEmployee = false;     //Danh dau xem no co phai la Nhan Vien hay khong
                Integer count = 0;

                List<Long> listManagement = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<Long> listAssistant = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();
                Map<Long, String> hmRole = dataSessionGR.getVof2_ItemEntityUser().getHmRole();
                String listRole = hmRole.get(dataSessionGR.getVof2_ItemEntityUser().getSysRoleId());

                if (listManagement != null && listManagement.size() > 0) {
                    isLeader = true;

                    count = count + listManagement.size();
                }

                if (listAssistant != null && listAssistant.size() > 0) {
                    isAssistant = true;

                    count = count + listAssistant.size();
                }

                if (listRole != null && listRole.split(";").length > count) {
                    isEmployee = true;
                }

                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                PersonTaskDAO taskDao = new PersonTaskDAO();

                EntityCommanderTree sourceItem = (EntityCommanderTree) taskDao.getListCommander(employeeId, isLeader, isAssistant, isEmployee, orgId, orgName);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * lay ra cay don vi chon nguoi giao viec
     *
     * @param type: 1, 2 (1 là lấy danh sách đơn vị, 2 lấy danh sách cá nhân)
     * listParent: parentId, roleId
 * @return
     */
    public String getTreeCommander(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                Long employeeId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                Long orgId = dataSessionGR.getVof2_ItemEntityUser().getSysOrgId();
                String orgName = dataSessionGR.getVof2_ItemEntityUser().getSysOrgName();
                String fullName = dataSessionGR.getVof2_ItemEntityUser().getFullName();
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
                //Begin::cuongnv::11/1/2017::su dung popup lay danh sach tren web
                hmParams.put("user", String.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.IS_WEB, Integer.class);
                hmParams.put(ConstantsFieldParams.ENFORCEMENT_ID, Integer.class);
                //End
//                hmParams.put(ConstantsFieldParams.STR_PARENT_ID, Long.class);
//                hmParams.put(ConstantsFieldParams.STR_ROLE_ID, Long.class);
                //hmParams.put(ConstantsFieldParams.STR_ORG_PARENT_ID, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long type = (Long) valueParams.get(ConstantsFieldParams.TYPE);              //1 là lấy danh sách đơn vị, 2 lấy danh sách cá nhân
                String strSearch = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
                List<EntityCommanderOrg> listOrg = new LinkedList<>();
                EntityCommanderOrg commanderOrg;

                //Begin::cuognv::11/1/2017
                String user = (String) valueParams.get("user");
                Long startRecord = (Long) valueParams.get(ConstantsFieldParams.START_RECORD);
                Long pageSize = (Long) valueParams.get(ConstantsFieldParams.PAGE_SIZE);
                Integer isCount = (Integer) valueParams.get(ConstantsFieldParams.IS_COUNT);
                //HaNH Bo sung tham so de phan biet client WEB voi cac client khac
                Integer isWeb = (Integer) valueParams.get(ConstantsFieldParams.IS_WEB);
                Vof2_EntityUser userVof2 = null;
                if (!CommonUtils.isEmpty(user)) {
                    try {
                        Gson gson = new Gson();
                        userVof2 = gson.fromJson(user, Vof2_EntityUser.class);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
//                System.out.println("strDataClient: " + strDataClient);
                JSONObject json = new JSONObject(strDataClient);
                List<Long> listEnforcement = new ArrayList<>();
                if (json.has(ConstantsFieldParams.TASK_LIST_ENFORCEMENT)) {
                    JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.TASK_LIST_ENFORCEMENT);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                listEnforcement.add(Long.parseLong(jsonArray.get(i).toString()));
                            } catch (NumberFormatException e) {
                            }
                        }
                    }
                }
                //End
                JSONArray arrParent = FunctionCommon.jsonGetArray(ConstantsFieldParams.TASK_LIST_PARENT, strDataClient);   //Truyền lên list parent id lấy danh sách cá nhân, nếu = null lấy đơn vị hiện tại của đơn vị user đăng nhập

                if (arrParent != null && arrParent.length() > 0) {
                    for (int i = 0; i < arrParent.length(); i++) {
                        //Get object cua tung doi tuong mang
                        JSONObject innerObj = (JSONObject) arrParent.get(i);
                        commanderOrg = new EntityCommanderOrg();

                        Long parentId = innerObj.getLong(ConstantsFieldParams.STR_PARENT_ID);
                        Long roleId = null;

                        if (!innerObj.isNull(ConstantsFieldParams.STR_ROLE_ID) && (innerObj.getString(ConstantsFieldParams.STR_ROLE_ID) == null ? "" != null : !"".equals(innerObj.getString(ConstantsFieldParams.STR_ROLE_ID)))) {
                            roleId = innerObj.getLong(ConstantsFieldParams.STR_ROLE_ID);
                        }

                        commanderOrg.setParentId(parentId);
                        commanderOrg.setRoleId(roleId);

                        listOrg.add(commanderOrg);
                    }
                } else {
                    //Khi danh sach don vi duoc chon rong, thi chon mac dinh don vi getSysOrgId check role cua no
                    StringBuilder query = new StringBuilder();
                    CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                    List<Object> params = new ArrayList<>();

                    commanderOrg = new EntityCommanderOrg();

                    query.append("select SYS_ROLE_ID FROM user_role u where u.SYS_ORGANIZATION_ID = ? AND u.SYS_USER_ID = ? AND rownum = 1");
                    params.add(orgId);
                    params.add(employeeId);

                    Long roleId = Long.parseLong(cmd.excuteSqlGetValOnConditionListParams(query, params).toString());

                    commanderOrg.setParentId(orgId);
                    commanderOrg.setRoleId(roleId);

                    listOrg.add(commanderOrg);
                }

                Boolean isLeader = false;       //Danh dau xem no co phai la Lanh dao/Thu truong hay khong
                Boolean isAssistant = false;    //Danh dau xem no co phai la Tro Ly hay khong
                Boolean isEmployee = false;     //Danh dau xem no co phai la Nhan Vien hay khong
                Integer count = 0;

                List<Long> listManagement = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<Long> listAssistant = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();
                List<Long> listSysOrgId = dataSessionGR.getVof2_ItemEntityUser().getListEmployeeOrg();
                Map<Long, String> hmRole = dataSessionGR.getVof2_ItemEntityUser().getHmRole();
                String listRole = hmRole.get(dataSessionGR.getVof2_ItemEntityUser().getSysRoleId());

                if (listManagement != null && listManagement.size() > 0) {
                    isLeader = true;

                    count = count + listManagement.size();
                }

                if (listAssistant != null && listAssistant.size() > 0) {
                    isAssistant = true;

                    count = count + listAssistant.size();
                }

                if (listRole.split(";").length > count) {
                    isEmployee = true;
                }
                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                PersonTaskDAO taskDao = new PersonTaskDAO();
                // Datdc them tham so
                Object sourceItem = taskDao.getTreeCommander(strSearch, type, employeeId, listOrg, isLeader,
                        isAssistant, isEmployee, orgId, orgName, fullName, isCount, startRecord, pageSize, userVof2, isWeb,
                        listManagement, listAssistant, listSysOrgId, listEnforcement);

                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (JSONException | NumberFormatException e) {
                LOGGER.error("getTreeCommander: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * lay ra cay don vi chon nguoi thực hiện
     *
     * @param
     * @return
     */
    public String getTreeEnforcement(String isSecurity, String strData, HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        //Toan bo cac ham khi thuc hien deu phai chay qua ham nay tru ham login va get RSA key
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //Lay prams client gui len de thuc hien yeu cau
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
//                System.out.println("getTreeEnforcement: " + strDataClient);
                LogUtils.logFunctionalStart(log);
                Long employeeId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                Long orgId = dataSessionGR.getVof2_ItemEntityUser().getSysOrgId();
                String orgName = dataSessionGR.getVof2_ItemEntityUser().getSysOrgName();
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.TASK_COMMANDER_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.STR_ORG_PARENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_IS_CONFIG, String.class);
                hmParams.put(ConstantsFieldParams.TASK_ORG_LEAD, Long.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
                //Begin::cuongnv::12/1/2017::su dung popup lay danh sach tren web
                hmParams.put("user", String.class);
                hmParams.put(ConstantsFieldParams.IS_WEB, Integer.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                //Doi tuong sinh cay
                hmParams.put(ConstantsFieldParams.TYPE_TASK, Integer.class);
                //End
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 1L);                      //1 là lấy danh sách đơn vị, 2 lấy danh sách cá nhân

                Long commanderId = (Long) ((valueParams.get(ConstantsFieldParams.TASK_COMMANDER_ID) != null) ? valueParams.get(ConstantsFieldParams.TASK_COMMANDER_ID) : employeeId);  //Id người giao việc
                Long orgParentId = (Long) ((valueParams.get(ConstantsFieldParams.STR_ORG_PARENT_ID) != null) ? valueParams.get(ConstantsFieldParams.STR_ORG_PARENT_ID) : null); //null: lấy cây ban đầu, truyền vào id: của đơn vị để lấy con
                String isConfig = (String) ((valueParams.get(ConstantsFieldParams.TASK_IS_CONFIG) != null) ? valueParams.get(ConstantsFieldParams.TASK_IS_CONFIG) : "0");   //0 - Ko duoc cau hinh giao viec, 1 - Cau hinh giao viec
                Long orgLeadId = (Long) ((valueParams.get(ConstantsFieldParams.TASK_ORG_LEAD) != null) ? valueParams.get(ConstantsFieldParams.TASK_ORG_LEAD) : null);
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 10L);
                String strSearch = (String) ((valueParams.get(ConstantsFieldParams.KEYWORD) != null) ? valueParams.get(ConstantsFieldParams.KEYWORD) : "");
                Integer typeTask = (Integer) ((valueParams.get(ConstantsFieldParams.TYPE_TASK) != null) ? valueParams.get(ConstantsFieldParams.TYPE_TASK) : 0);
                List<EntityCommanderOrg> listOrg = new LinkedList<>();

                JSONArray arrParent = FunctionCommon.jsonGetArray(ConstantsFieldParams.TASK_LIST_PARENT, strDataClient);   //Truyền lên list parent id lấy danh sách cá nhân, nếu = null lấy đơn vị hiện tại của đơn vị user đăng nhập

                if (arrParent != null && arrParent.length() > 0) {
                    for (int i = 0; i < arrParent.length(); i++) {
                        //Get object cua tung doi tuong mang
                        JSONObject innerObj = (JSONObject) arrParent.get(i);

                        EntityCommanderOrg commanderOrg = new EntityCommanderOrg();
                        Long parentId = null;
                        Long roleId = null;
                        Long orglead = null;    //0 - la lanh dao phong ban, > 0 la id don vi ma no lanh dao, null la nhan vien
                        String config = "0";    //0 - Ko duoc cau hinh giao viec, 1 - Cau hinh giao viec

                        if (!innerObj.isNull(ConstantsFieldParams.STR_PARENT_ID) && innerObj.getString(ConstantsFieldParams.STR_PARENT_ID).trim().length() > 0) {
                            parentId = innerObj.getLong(ConstantsFieldParams.STR_PARENT_ID);
                        }

                        if (!innerObj.isNull(ConstantsFieldParams.STR_ROLE_ID) && innerObj.getString(ConstantsFieldParams.STR_ROLE_ID).trim().length() > 0) {
                            roleId = innerObj.getLong(ConstantsFieldParams.STR_ROLE_ID);
                        }

                        if (!innerObj.isNull(ConstantsFieldParams.TASK_ORG_LEAD) && innerObj.getString(ConstantsFieldParams.TASK_ORG_LEAD).trim().length() > 0) {
                            orglead = innerObj.getLong(ConstantsFieldParams.TASK_ORG_LEAD);
                        }

                        if (!innerObj.isNull(ConstantsFieldParams.TASK_IS_CONFIG) && innerObj.getString(ConstantsFieldParams.TASK_IS_CONFIG).trim().length() > 0) {
                            config = innerObj.getString(ConstantsFieldParams.TASK_IS_CONFIG);
                        }

                        commanderOrg.setParentId(parentId);
                        commanderOrg.setRoleId(roleId);
                        commanderOrg.setOrgLead(orglead);
                        commanderOrg.setIsConfig(config);

                        listOrg.add(commanderOrg);
                    }
                }

                //Begin::cuognv::12/1/2017
                List<Long> listManagement = dataSessionGR.getVof2_ItemEntityUser().getListManagementOrg();
                List<Long> listAssistant = dataSessionGR.getVof2_ItemEntityUser().getListAssistantOrg();
                List<Long> listSysOrgId = dataSessionGR.getVof2_ItemEntityUser().getListEmployeeOrg();
                if (listManagement == null) {
                    listManagement = new ArrayList<>();
                }
                if (listAssistant == null) {
                    listAssistant = new ArrayList<>();
                }
                if (listSysOrgId == null) {
                    listSysOrgId = new ArrayList<>();
                }
                String user = (String) valueParams.get("user");
                Integer isCount = (Integer) valueParams.get(ConstantsFieldParams.IS_COUNT);
                Integer isWeb = (Integer) valueParams.get(ConstantsFieldParams.IS_WEB);
                Vof2_EntityUser userVof2 = null;
                if (!CommonUtils.isEmpty(user)) {
                    try {
                        Gson gson = new Gson();
                        userVof2 = gson.fromJson(user, Vof2_EntityUser.class);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                if (CommonUtils.isEmpty(listOrg)) {
                    EntityCommanderOrg ec = new EntityCommanderOrg();
                    ec.setParentId(dataSessionGR.getVof2_ItemEntityUser().getSysOrgId());
                    listOrg.add(ec);
                }
                //End
                //Thuc hien chen code lay du lieu va thao tac tra ve o day
                PersonTaskDAO taskDao = new PersonTaskDAO();
                // Datdc Tach ra dung ham khac
                Object sourceItem = taskDao.getTreeEnforcementUpGrade(strSearch, commanderId, type, employeeId, listOrg,
                        orgId, orgName, orgParentId, orgLeadId, isConfig, startRecord, pageSize, isCount, userVof2,
                        isWeb, listManagement, listAssistant, listSysOrgId, typeTask);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, sourceItem, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("getTreeEnforcement: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            //cac truong hop loi say ra trong qua trinh thao tac
            //bao gom: mat session, khong ton tai doi tuong, session timeout
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return FunctionCommon.getResultForClient(strResult);
    }

    /**
     * lay ra danh sach kien nghi/de xuat
     *
     * @param userId
     * @return
     */
    public String getListRequest(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //lay gia tri luu trong seasion
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.KEYWORD, String.class);
                hmParams.put(ConstantsFieldParams.START_RECORD, Long.class);
                hmParams.put(ConstantsFieldParams.PAGE_SIZE, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, String.class);
                hmParams.put(ConstantsFieldParams.STATUS, String.class);
                hmParams.put(ConstantsFieldParams.START_DATE_FROM, String.class);
                hmParams.put(ConstantsFieldParams.START_DATE_TO, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                String strSearch = (String) valueParams.get(ConstantsFieldParams.KEYWORD);
                Long type = (Long) ((valueParams.get(ConstantsFieldParams.TYPE) != null) ? valueParams.get(ConstantsFieldParams.TYPE) : 1L);      //1 Lay yeu cau nhan, 2 Lay yeu cau gui
                Long startRecord = (Long) ((valueParams.get(ConstantsFieldParams.START_RECORD) != null) ? valueParams.get(ConstantsFieldParams.START_RECORD) : 0L);
                Long pageSize = (Long) ((valueParams.get(ConstantsFieldParams.PAGE_SIZE) != null) ? valueParams.get(ConstantsFieldParams.PAGE_SIZE) : 10L);
                String isCount = (String) ((valueParams.get(ConstantsFieldParams.IS_COUNT) != null) ? valueParams.get(ConstantsFieldParams.IS_COUNT) : "0");
                String status = (String) valueParams.get(ConstantsFieldParams.STATUS);
                String startDate = (String) valueParams.get(ConstantsFieldParams.START_DATE_FROM);
                String toDate = (String) valueParams.get(ConstantsFieldParams.START_DATE_TO);

                if (status == null || status.isEmpty() || startDate == null || startDate.isEmpty() || toDate == null || toDate.isEmpty()) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }

                //Chuan hoa lai thoi gian
                startDate = formatter.format(formatter.parse(startDate));
                toDate = formatter.format(formatter.parse(toDate));

                PersonTaskDAO taskDao = new PersonTaskDAO();

                if ("0".equals(isCount)) {
                    List<EntityRequestList> listItem = (List<EntityRequestList>) taskDao.getListRequest(userId, strSearch, type, startRecord, pageSize, isCount, status, startDate, toDate);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listItem, strAesKeyDecode);
                } else {
                    Long result = (Long) taskDao.getListRequest(userId, strSearch, type, startRecord, pageSize, isCount, status, startDate, toDate);

                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("getListRequest: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Lay danh sach nguoi giai quyet</b>
     * 
     * @param isSecurity
     * @param data
     * @param request
     * @return 
     */
    public String getListPropose(String isSecurity, String data,
            HttpServletRequest request) {

        String[] keys = new String[] {
            ConstantsFieldParams.RECIPIENT_ORG_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        String errorDesc = String.format(Constants.Common.LOG_SYNTAX, "getListPropose",
                userGroup.getUserId2(), "userId1", userGroup.getUserId1());
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error(errorDesc + "Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        try {
            // Parse du lieu tu client
            List<String> listValue = userGroup.getListParamsFromClient();
            // Don vi giai quyet hien tai
            Long recipientOrgId = null;
            String strRecipientOrgId = listValue.get(0);
            if (!CommonUtils.isEmpty(strRecipientOrgId)) {
                recipientOrgId = Long.parseLong(strRecipientOrgId);
            }
            PersonTaskDAO taskDao = new PersonTaskDAO();
            List<EntityVhrEmployee> listItem = taskDao.getListPropose(userGroup.getUserId2(),
                    recipientOrgId, false);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, listItem, userGroup);
        } catch (Exception e) {
            LOGGER.error(errorDesc + "Exception!", e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * thuc hien Chuyen tiep yeu cau, kien nghi
     *
     * @param userId
     * @return
     */
    public String forwardRequest(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //lay gia tri luu trong seasion
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                //lay gia tri client gui len
                HashMap<String, Object> hmParams = new HashMap<String, Object>();
                hmParams.put(ConstantsFieldParams.REQUEST_ID, Long.class);
                hmParams.put(ConstantsFieldParams.REQUEST_TITLE, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_CONTENT, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_EFFECTIVE_DATE, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_EXPIRED_DATE, String.class);
                hmParams.put(ConstantsFieldParams.REQUEST_LEVEL, Long.class);
                hmParams.put(ConstantsFieldParams.REQUEST_RECIPITENT_ID, Long.class);
                hmParams.put(ConstantsFieldParams.REQUEST_STATUS, Long.class);

                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);

                Long requestId = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_ID) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_ID) : null);
                String title = (String) valueParams.get(ConstantsFieldParams.REQUEST_TITLE);
                String content = (String) valueParams.get(ConstantsFieldParams.REQUEST_CONTENT);
                String effectiveDate = ((String) valueParams.get(ConstantsFieldParams.REQUEST_EFFECTIVE_DATE)).replace("\\", "");
                String expiredDate = ((String) valueParams.get(ConstantsFieldParams.REQUEST_EXPIRED_DATE)).replace("\\", "");
                Long requestLevel = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_LEVEL) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_LEVEL) : 2);
                Long recipitentId = (Long) valueParams.get(ConstantsFieldParams.REQUEST_RECIPITENT_ID);
                Long status = (Long) ((valueParams.get(ConstantsFieldParams.REQUEST_STATUS) != null) ? valueParams.get(ConstantsFieldParams.REQUEST_STATUS) : 3);

                String createdDate = formatter.format(new Date());
                effectiveDate = formatter.format(formatter.parse(effectiveDate));
                expiredDate = formatter.format(formatter.parse(expiredDate));

                if (title == null || title.isEmpty() || requestId == null || recipitentId == null) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }

                //Kiem tra xem no da duoc chuyen chua
                CommonDataBaseDaoVO2 cmd = new CommonDataBaseDaoVO2();
                List<Object> params = new ArrayList<Object>();
                StringBuilder requestQuery = new StringBuilder();
                requestQuery.append("select STATUS from REQUEST where REQUEST_ID = ?");
                params.add(requestId);
                String curStatus = cmd.excuteSqlGetValOnConditionListParams(requestQuery, params).toString();

                if (!"1".equals(curStatus)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NOT_FORWARD_REQUEST, null, null);
                }

                PersonTaskDAO taskDao = new PersonTaskDAO();

                Integer result = taskDao.forwardRequest(title, content, effectiveDate, expiredDate, requestLevel, status, recipitentId, userId, createdDate, userId, requestId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS,
                        result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_ERROR, ErrorCode.INPUT_INVALID, e.getMessage(), strData, isSecurity);
                LOGGER.error("forwardRequest: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Huy danh gia</b><br>
     *
     * @author SonDN
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String deleteKIemp(HttpServletRequest request, String data,
            String isSecurity) {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("deleteKIemp (Huy KI ca nhan) "
                    + "- Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("deleteKIemp (Huy KI ca nhan) "
                    + "- Khong co thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
//        Gson gson = new Gson();
        Long userId = user.getUserId();
        // Lay ra ma nhan vien
        String cardId = user.getStrCardNumber();
        log.setUserName(cardId);
        // Lay danh sach don vi ma user co vai tro thu truong/lanh dao
        List<Long> listManagementOrgId = user.getListManagementOrg();
        // Khong phai thu truong/lanh dao
        if (CommonUtils.isEmpty(listManagementOrgId)) {
            LOGGER.error("deleteKIemp - User khong co vai tro thu truong/lanh dao - username: "
                    + cardId);
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.PERIOD
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            Long orgId = Long.parseLong(listValue.get(0));
            String period = listValue.get(1);

            PersonTaskDAO taskDao = new PersonTaskDAO();
            Integer result = taskDao.deleteKi(orgId, period, userId);
            LogUtils.logFunctionalEnd(log);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
        } catch (JSONException | NumberFormatException ex) {
            LOGGER.error("deleteKIemp (Huy KI ca nhan) "
                    + "- Exception - username: " + cardId + "\ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Lay Ki ca nhan</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 2/2/2017
     */
    public String getKIEmp(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                //lay gia tri luu trong seasion
                Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                TaskDAO taskDAO = new TaskDAO();
                List<EntityEmpRating> result = taskDAO.getKIEmp(userId, period);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.getKIemp: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }

        return strResult;
    }

    /**
     * <b>Danh sach cong viec duoc danh gia cua nv</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 2/2/2017
     */
    public String getDetailKIEmployee(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID, Long.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long employeeId = (Long) valueParams.get(ConstantsFieldParams.MEETING_ASSISTANT_EMPLOYEE_ID);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                TaskDAO taskDAO = new TaskDAO();
                EntityVhrEmployee result = taskDAO.getDetailKIEmployee(employeeId, period,orgId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.getDetailKIEmployee: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Kiểm tra đơn vị đã đánh giá KI chưa</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 6/2/2017
     */
    public String checkKIOrg(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_STATUS, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                Long status = (Long) valueParams.get(ConstantsFieldParams.TASK_STATUS);
                TaskDAO taskDAO = new TaskDAO();
                Integer result = taskDAO.checkKIOrg(orgId, period, status);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.CheckKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
     /**
     * <b>Kiểm tra Nhan vien đã đánh giá KI chưa</b>
     *
     * @author DuanNV
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 20/10/2017
     */
    public String checkEmpl(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("updateKIOrg - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                Long userId = user.getUserId();
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                TaskDAO taskDAO = new TaskDAO();
                Integer result = taskDAO.checkEmpl(orgId, period,userId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.CheckKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
    
     /**
     * <b>Lay ra diem min max cau hinh ty le</b>
     *
     * @author DuanNV
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 20/10/2017
     */
    public String getListRatioPoint(String isSecurity, String strData,
            HttpServletRequest req) {

        String[] keys = new String[] {
            ConstantsFieldParams.PERIOD,
            ConstantsFieldParams.ORG_ID
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(req, strData, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getListRatioPoint - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        // Kiem tra user id tren he thong 2
        Vof2_EntityUser user = userGroup.getVof2_ItemEntityUser();
        if (user == null || user.getUserId() == null) {
            LOGGER.error("getListRatioPoint - Khong co thong tin user tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        Long orgId = user.getSysOrgId();
        try {
            // Parse du lieu client truyen len
            List<String> listValuse = userGroup.getListParamsFromClient();
            String period = listValuse.get(0);
            String strOrgId = listValuse.get(1);
            if (!CommonUtils.isEmpty(strOrgId)) {
                orgId = Long.parseLong(strOrgId);
            }
            TaskDAO taskDAO = new TaskDAO();
            // Datdc them tham so isYear
            Object result = taskDAO.getListRatioPoint(period, orgId);
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception e) {
            LOGGER.error("getListRatioPoint - Exception - username: "
                    + userGroup.getCardId(), e);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * <b>Kiểm tra Nhan vien đã trinh ky chua</b>
     *
     * @author DuanNV
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 20/10/2017
     */
    public String isCheckStatusSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                TaskDAO taskDAO = new TaskDAO();
                Integer result = taskDAO.isCheckStatusSign(orgId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.CheckKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
    
     /**
     * <b>Kiểm tra acc co quyen trinh ky ko?</b>
     *
     * @author DuanNV
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 20/10/2017
     */
    public String isCheckPermisstionSign(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                TaskDAO taskDAO = new TaskDAO();
                Integer result = taskDAO.isCheckPermisstionSign(orgId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.CheckKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
    
    /**
     *  <b>get diem dien vi tu KI da danh gia</b>
     * DuanNV
     * @param isSecurity
     * @param strData
     * @param req
     * @return 
     */
     public String checkPointUnit(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.TASK_STATUS, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                Long status = (Long) valueParams.get(ConstantsFieldParams.TASK_STATUS);
                TaskDAO taskDAO = new TaskDAO();
                Double result = taskDAO.checkPointUnit(orgId, period, status);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.CheckKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Them moi, cap nhat KI don vi</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 6/2/2017
     */
    public String updateKIOrg(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("updateKIOrg - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                Long userId = user.getUserId();
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.KI, Integer.class);
                hmParams.put(ConstantsFieldParams.POINT, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                Integer ki = (Integer) valueParams.get(ConstantsFieldParams.KI);
                Double point = (Double) Double.parseDouble((String)(valueParams.get(ConstantsFieldParams.POINT)));
                TaskDAO taskDAO = new TaskDAO();
                Integer result = taskDAO.updateKIOrg(orgId, period, ki, userId, point);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.updateKIOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Lấy danh sách nhân viên đánh giá KI</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 14/2/2017
     */
    public String getListEmpKI(String isSecurity, String strData,
            HttpServletRequest req) {
        
        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("getListEmpKI - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.IS_COUNT, Integer.class);
                hmParams.put(ConstantsFieldParams.TOTAL_UNIT, Integer.class);
                hmParams.put(ConstantsFieldParams.CONTRACT, Integer.class);
                
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                Integer isCount = (Integer) valueParams.get(ConstantsFieldParams.IS_COUNT);
                Integer contractTypeDefault = (Integer) valueParams.get(ConstantsFieldParams.CONTRACT);
                Integer totalUnit = (Integer) valueParams.get(ConstantsFieldParams.TOTAL_UNIT);
                TaskDAO taskDAO = new TaskDAO();
                Object result = taskDAO.getListEmpKI(orgId,totalUnit,contractTypeDefault, period, isCount, user.getUserId());
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.getListEmpKI: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
 
    /**
     * <b>Lấy ty le nv theo KI don vi</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 17/2/2017
     */
    public String percentKI(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("percentKI - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.KI, Integer.class);
                hmParams.put(ConstantsFieldParams.COUNT, Integer.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                Integer ki = (Integer) valueParams.get(ConstantsFieldParams.KI);
                Integer count = (Integer) valueParams.get(ConstantsFieldParams.COUNT);
                TaskDAO taskDAO = new TaskDAO();
                String result = taskDAO.percentKI(orgId, period, ki, count);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.percentKI: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Kiem tra don vi co phai don vi con thap nhat hay cap trung gian</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 22/2/2017
     */
    public String isLeafOrg(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("isLeafOrg - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                if (orgId == null || orgId == 0) {
                    LOGGER.error("TaskService.isLeafOrg - Loi du lieu - data: " + strDataClient);
                    strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
                } else {
                    TaskDAO taskDAO = new TaskDAO();
                    Object result = taskDAO.isLeafOrg(orgId);
                    strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                }
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.isLeafOrg: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Huy ky danh gia KI</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 22/2/2017
     */
    public String unResignKIEmp(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("unResignKIEmp - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                Long userId = user.getUserId();
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                PersonTaskDAO ptaskDAO = new PersonTaskDAO();
                Object result = ptaskDAO.unResignKIEmp(orgId, period, userId);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.unResignKIEmp: ", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Ky danh gia KI</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 22/2/2017
     */
    public String signKI(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            //lay key Aes
            String strAesKeyDecode = dataSessionGR.getStrAesKey();
            String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
            // Lay ma nhan vien
            String cardId = dataSessionGR.getCardId();
            Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
            if (user == null || user.getUserId() == null) {
                LOGGER.error("TaskService.signKI - Khong co thong tin user tren he thong 2!");
                return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
            }
            log.setUserName(cardId);
            Long userId = user.getUserId();
            // Ghi log bat dau chuc nang
            log.setParamList(strDataClient);
            LogUtils.logFunctionalStart(log);
            try {
                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.SIGN_TYPE, Long.class);
                hmParams.put(ConstantsFieldParams.IS_SAVE, Long.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long signType = (Long) valueParams.get(ConstantsFieldParams.SIGN_TYPE);
                Long isSave = (Long) valueParams.get(ConstantsFieldParams.IS_SAVE);
                JSONObject json = new JSONObject(strDataClient);
                List<EntityEmpRating> listKI = new ArrayList<>();
                if (json.has(ConstantsFieldParams.LIST_KI)) {
                    JSONArray jsonArray = json.getJSONArray(ConstantsFieldParams.LIST_KI);
                    Gson gson = new Gson();
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                EntityEmpRating empKI = gson.fromJson(jsonArray.get(i).toString(), EntityEmpRating.class);
                                listKI.add(empKI);
                            } catch (JsonSyntaxException e) {
                                LOGGER.error("TaskService.signKI-data : " + strDataClient, e);
                            }
                        }
                    }

                }
                if (CommonUtils.isEmpty(listKI)) {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA, null, null);
                }
                PersonTaskDAO ptaskDAO = new PersonTaskDAO();
                Object result = ptaskDAO.signKI(userId, signType, listKI, isSave);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.signKI-data : " + strDataClient, e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * <b>Lay danh sach file ky</b>
     *
     * @author cuongnv
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 4/3/2017
     */
    public String getListFilesKI(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("TaskService.getListFilesKI - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                PersonTaskDAO ptaskDAO = new PersonTaskDAO();
                Object result = ptaskDAO.getListFilesKI(orgId, period);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.getListFilesKI:", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }

    /**
     * chuyen van ban cho nguoi thuc hien cong viec
     *
     * @param isWeb
     * @param listSourceMap
     * @param userVof1
     * @param userVof2
     * @param sourceType
     * @param objectType
     * @param isSend
     * @param listEnforcement
     * @param sourceId
     */
    void sendDocToPersonTask(Integer isWeb, List<EntitySourceMap> listSourceMap,
            EntityUser userVof1, Vof2_EntityUser userVof2, Long sourceType, Long objectType,
            Long isSend, List<EntityEnforcement> listEnforcement, Long sourceId) {
        if (listEnforcement == null || listEnforcement.isEmpty()
                || ((listSourceMap == null || listSourceMap.isEmpty()) && sourceId == null)) {
            return;
        }
        Long sourceTypeTmp;
        Long objectTypeTmp;
        Long isSendTmp;
        try {
            List<Vof2_EntityUser> listStaff = new ArrayList<>();
            Vof2_EntityUser vof2EntityUser1;
            for (EntityEnforcement enforcement1 : listEnforcement) {
                vof2EntityUser1 = new Vof2_EntityUser();
                vof2EntityUser1.setUserId(enforcement1.getEnforcementId());
                listStaff.add(vof2EntityUser1);
            }
            if (isWeb != null && isWeb.equals(1)) {
                //neu la web
                if (!CommonUtils.isEmpty(listSourceMap)) {
                    for (EntitySourceMap sm : listSourceMap) {
                        sourceTypeTmp = sm.getSourceType().longValue();
                        objectTypeTmp = sm.getObjectType().longValue();
                        isSendTmp = sm.getIsSend();
                        if (sourceTypeTmp == 4L && objectTypeTmp == 1L
                                && (isSendTmp == null || isSendTmp == 1L)
                                && sm.getSourceMapId() == null) {
                            //thuc hien chuyen van ban
                            DocumentDAO docDAO = new DocumentDAO();
                            docDAO.sendDocumentToStaffFromTask(sm.getSourceId(), listStaff, null,
                                    userVof1, userVof2);
                        }
                    }
                }
            } else {
                //neu la mobile
                if (sourceType != null && sourceType == 4L
                        && objectType != null && objectType == 1L
                        && (isSend == null || isSend == 1L)) {
                    DocumentDAO docDAO = new DocumentDAO();
                    docDAO.sendDocumentToStaffFromTask(sourceId, listStaff, null,
                            userVof1, userVof2);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("sendDocToPersonTask: ", ex);
        }
    }
     // DuanNV them Danh sach don vi danh gia don vi
      /*danh sach don vi quyen luu tru*/
    public String getListSysRoleEvaluate(HttpServletRequest req, String strData, String isSecurity) {
        EntityUserGroup dataSessionGR = FunctionCommon.getDataFromClient(req, strData, null);
        // Khong co session
        if (!dataSessionGR.getCheckSessionOk()) {
           LOGGER.error("getListSysRoleOrg - No session");
            return FunctionCommon.responseResult(dataSessionGR.getEnumErrCode(), null, null);
        }

        // Kiem tra user id tren he thong 2
        Long userId = dataSessionGR.getVof2_ItemEntityUser().getUserId();
        if (userId == null) {
           LOGGER.error("getListSysRoleOrg - userId null");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }

         TaskDAO taskDAO = new TaskDAO();
        List<EntityVhrOrg> result = taskDAO.getListSysRoleEvaluate(userId);       
        return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, dataSessionGR);
    }
    // End.
    
    /**
     * <b>Trinh ky</b>
     *
     * @author pm1_os20
     * @param isSecurity
     * @param data
     * @param request
     * @return
     * @since 28/10/2017
     */
    public String updateRequisitionDirect(String isSecurity, String data,
            HttpServletRequest request) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("updateRequisitionDirect - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(),
                    null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.CONTRACT,
                ConstantsFieldParams.ORG_ID,
                ConstantsFieldParams.PERIOD,
                ConstantsFieldParams.TEXT_TITLE,
                ConstantsFieldParams.TEXT_LST_STAFF,
                ConstantsFieldParams.KI,
                ConstantsFieldParams.TYPE_TASK,
                ConstantsFieldParams.LIST_KI
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Thong tin bien ban hop
            String strContractType = listValue.get(0);
            String strOrgId = listValue.get(1);
            String period = listValue.get(2);
            String title = listValue.get(3);
            String strStaff = listValue.get(4);
            String strKi = listValue.get(5);
            String strTypeTask = listValue.get(6);
            String strListKI = listValue.get(7);
            
            Long orgId = null;
            if(strOrgId != null) {
                orgId = Long.valueOf(strOrgId);
            }
            
            Long contractType = null;
            if(strContractType != null) {
                contractType = Long.valueOf(strContractType);
            }

            
            List<EntityStaff> listStaff = new ArrayList<>();
            Gson gson = new Gson();
            
            if (!CommonUtils.isEmpty(strStaff)) {
                JSONArray arrJsonStaff = new JSONArray(strStaff);
                if (arrJsonStaff.length() > 0) {
                    for (int i = 0; i < arrJsonStaff.length(); i++) {
                        JSONObject job = arrJsonStaff.getJSONObject(i);                                
                        EntityStaff staff = gson.fromJson(String.valueOf(job), EntityStaff.class);
                        if (staff != null) {
                            listStaff.add(staff);
                        }
                    }
                }
            }
            
            List<EntityEmpRating> listEmpRating = new ArrayList<>();
            if (!CommonUtils.isEmpty(strListKI)) {
                JSONArray arrJson = new JSONArray(strListKI);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONObject job = arrJson.getJSONObject(i);                                
                        EntityEmpRating empRating = gson.fromJson(String.valueOf(job), EntityEmpRating.class);
                        if (empRating != null) {
                            listEmpRating.add(empRating);
                        }
                    }
                }
            }
            
            List<String> lstKi = new ArrayList<>();
            if (!CommonUtils.isEmpty(strKi)) {
                JSONArray arrJson = new JSONArray(strKi);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            lstKi.add(arrJson.getString(i));
                        }
                    }
                }
            }
            
            List<String> lstTypeTask = new ArrayList<>();
            if (!CommonUtils.isEmpty(strTypeTask)) {
                JSONArray arrJson = new JSONArray(strTypeTask);
                if (arrJson.length() > 0) {
                    for (int i = 0; i < arrJson.length(); i++) {
                        if (arrJson.get(i) != null) {
                            lstTypeTask.add(arrJson.getString(i));
                        }
                    }
                }
            }

           PersonTaskDAO ptaskDAO = new PersonTaskDAO();
            Long result = ptaskDAO.updateRequisitionDirect(userGroup, listStaff, contractType, title, orgId, period, lstKi, lstTypeTask, listEmpRating);
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("updateRequisitionDirect - username: "
                    + userGroup.getCardId() + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
    
    /**
     * Lay file ky de hien thi
     *
     * @author pm1_os20
     * @param req
     * @param strData
     * @param isSecurity
     * @return
     * @since 28/10/2017
     */
    public String getFilesKIToView(String isSecurity, String strData,
            HttpServletRequest req) {

        EntityLog log = new EntityLog(req, CLASS_NAME);
        String strResult;
        EntityUserGroup dataSessionGR = FunctionCommon.getStatusSession(req);
        if (dataSessionGR.getCheckSessionOk()) {
            try {
                //lay key Aes
                String strAesKeyDecode = dataSessionGR.getStrAesKey();
                String strDataClient = SecurityControler.decodeDataByAes(strAesKeyDecode, strData);
                // Lay ma nhan vien
                String cardId = dataSessionGR.getCardId();
                Vof2_EntityUser user = dataSessionGR.getVof2_ItemEntityUser();
                if (user == null || user.getUserId() == null) {
                    LOGGER.error("TaskService.getFilesKIToView - Khong co thong tin user tren he thong 2!");
                    return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
                }
                log.setUserName(cardId);
                // Ghi log bat dau chuc nang
                log.setParamList(strDataClient);
                LogUtils.logFunctionalStart(log);

                HashMap<String, Object> hmParams = new HashMap<>();
                hmParams.put(ConstantsFieldParams.CONTRACT, Long.class);
                hmParams.put(ConstantsFieldParams.ORG_ID, Long.class);
                hmParams.put(ConstantsFieldParams.PERIOD, String.class);
                HashMap<String, Object> valueParams = FunctionCommon.getListParamsClient(hmParams, strData, dataSessionGR);
                Long contract = (Long) valueParams.get(ConstantsFieldParams.CONTRACT);
                Long orgId = (Long) valueParams.get(ConstantsFieldParams.ORG_ID);
                String period = (String) valueParams.get(ConstantsFieldParams.PERIOD);
                PersonTaskDAO ptaskDAO = new PersonTaskDAO();
                Object result = ptaskDAO.getFilesKIToView(contract, orgId, period);
                strResult = FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, strAesKeyDecode);
                // Ghi log ket thuc chuc nang
                LogUtils.logFunctionalEnd(log);
            } catch (Exception e) {
                LOGGER.error("TaskService.getFilesKIToView:", e);
                strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
            }
        } else {
            strResult = FunctionCommon.generateResponseJSON(dataSessionGR.getEnumErrCode(), null, null);
        }
        return strResult;
    }
}
