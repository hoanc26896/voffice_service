package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.document.AnswerDocumentDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityWrittenResponse;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.LogUtils;

@SuppressWarnings("deprecation")
public class AnswerDocumentController {
	private static final Logger LOGGER = Logger.getLogger(AnswerDocumentController.class);
    private static final String CLASS_NAME = AnswerDocumentController.class.getName();
    
	/**
	 * Lay danh sach don vi yeu cau tra loi van ban
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public String getListGroupReceiverRequestResponse(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        //Lay thong tin user theo sessionId cua request
	        String[] keys = new String[]{ ConstantsFieldParams.DOCUMENT_ID, ConstantsFieldParams.TYPE };
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
	                data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            //danh sach cac key de lay du lieu tu client
	            List<String> listValue = userGroup.getListParamsFromClient();
	            String strDocumentId = listValue.get(0);
	            Long documentId = null;
	            if (!CommonUtils.isEmpty(strDocumentId)) {
	                documentId = Long.parseLong(strDocumentId);
	            }
	            String strType = listValue.get(1);
	            Integer type = null;
	            if (!CommonUtils.isEmpty(strType)) {
	            	type = Integer.parseInt(strType);
	            }
	            AnswerDocumentDAO docDao = new AnswerDocumentDAO();
	            List<EntityWrittenResponse> result = docDao.getListGroupReceiverRequestResponse(documentId, type, userGroup.getVof2_ItemEntityUser().getUserId());
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
	        } else {
	            //truong hop bị timeout
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
	                    null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	
	/**
	 * Them moi danh sach don vi duoc yeu cau tra loi bang van ban
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public String insertDocumentReply(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        //Lay thong tin user theo sessionId cua request
	        String[] keys = new String[]{ ConstantsFieldParams.DOCUMENT_ID, "listRequest", ConstantsFieldParams.TITLE };
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
	                data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            List<String> listValue = userGroup.getListParamsFromClient();
	            String strDocumentId = listValue.get(0);
	            Long documentId = null;
	            if (!CommonUtils.isEmpty(strDocumentId)) {
	                documentId = Long.parseLong(strDocumentId);
	            }
	            String strRequest = listValue.get(1);
	            List<EntityWrittenResponse> listRequest = null;
	            if (!CommonUtils.isEmpty(strRequest)) {
	            	Gson gson =  new GsonBuilder().setDateFormat("dd/MM/yyyy hh:mm:ss").create();
	            	Type listType = new TypeToken<ArrayList<EntityWrittenResponse>>() {
	                }.getType();
	                listRequest = gson.fromJson(strRequest, listType);
	            }
	            String title = listValue.get(2);
	            Long userId2 = userGroup.getUserId2();
	
	            AnswerDocumentDAO docDao = new AnswerDocumentDAO();
	            List<Long> orgIds = docDao.insertDocumentReply(listRequest, userId2, documentId);
	            if (!CommonUtils.isEmpty(orgIds)) {
	            	UserDAO userDao = new UserDAO();
            	    List<EntityVhrEmployee> users = userDao.getUserDocumentManagerOrg(orgIds);
            	    if (!CommonUtils.isEmpty(users)) {
            	    	CommonControler smsDAO = new CommonControler();
            	        for (EntityVhrEmployee entity : users) {
            	            smsDAO.sentSMS(title, userGroup.getUserId2(), entity.getEmployeeId(),
            	            		entity.getOrganizationId(), Constants.SMS_TEXT_CONFIG.ASK_FOR_REPLY, "", 101L);
            	        }
            	    }
	            }
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
	        } else {
	            //truong hop bị timeout
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	
	/**
	 * Thu hoi yeu cau 
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public String cancelDocumentReply(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        //Lay thong tin user theo sessionId cua request
	        String[] keys = new String[]{ ConstantsFieldParams.DOCUMENT_ID, ConstantsFieldParams.ORG_ID, ConstantsFieldParams.TITLE };
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
	                data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            List<String> listValue = userGroup.getListParamsFromClient();
	            
	            String strDocumentId = listValue.get(0);
	            Long documentId = null;
	            if (!CommonUtils.isEmpty(strDocumentId)) {
	                documentId = Long.parseLong(strDocumentId);
	            }
	            String strOrgId = listValue.get(1);
	            Long orgId = null;
	            if (!CommonUtils.isEmpty(strOrgId)) {
	            	orgId = Long.parseLong(strOrgId);
	            }
	            String title = listValue.get(2);
	
	            AnswerDocumentDAO docDao = new AnswerDocumentDAO();
	            boolean result = docDao.cancelDocumentReply(orgId, documentId);
	            if (result) {
	            	UserDAO userDao = new UserDAO();
            	    List<EntityVhrEmployee> users = userDao.getUserDocumentManagerOrg(Arrays.asList(orgId));
            	    if (!CommonUtils.isEmpty(users)) {
            	    	CommonControler smsDAO = new CommonControler();
            	        for (EntityVhrEmployee entity : users) {
            	            smsDAO.sentSMS(title, userGroup.getUserId2(), entity.getEmployeeId(),
            	            		entity.getOrganizationId(), Constants.SMS_TEXT_CONFIG.CANCEL_ASK_FOR_REPLY, "", 101L);
            	        }
            	    }
	            }
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
	        } else {
	            //truong hop bị timeout
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	
	/**
	 * Lay danh sach van ban can phai tra loi
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	public String getListAnswerDocument(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        String[] keys = new String[]{ "document", "startPage", "pageSize", "isCount" };
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            List<String> listValue = userGroup.getListParamsFromClient();
	            String strDocument = listValue.get(0);
	            EntityDocument doc = null;
	            Gson gson =  new GsonBuilder().create();
	            if (!CommonUtils.isEmpty(strDocument)) {
	            	doc = gson.fromJson(strDocument, EntityDocument.class);
	            }
	            String strStartPage = listValue.get(1);
	            Long startPage = null;
	            if (!CommonUtils.isEmpty(strStartPage)) {
	            	startPage = Long.valueOf(strStartPage);
	            }
	            String strPageSize = listValue.get(2);
	            Long pageLoad = null;
	            if (!CommonUtils.isEmpty(strPageSize)) {
	            	pageLoad = Long.valueOf(strPageSize);
	            }
	            boolean isCount = "1".equals(listValue.get(3));
	
	            AnswerDocumentDAO docDao = new AnswerDocumentDAO();
	            Object result = docDao.getListAnswerDocument(doc, startPage, pageLoad, isCount,
	            		userGroup.getVof2_ItemEntityUser().getListSecretaryVhrOrg(), userGroup.getUserId2());
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
	        } else {
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
	
	public String replyDocument(HttpServletRequest request, String data) {
		try {
			EntityLog log = new EntityLog(request, CLASS_NAME);
	        //Lay thong tin user theo sessionId cua request
	        String[] keys = new String[] {
	        		ConstantsFieldParams.DOCUMENT_ID,
	        		ConstantsFieldParams.ORG_ID, 
	        		ConstantsFieldParams.TITLE,
	        		ConstantsFieldParams.STR_OBJECT_ID,
	        		ConstantsFieldParams.STAFF_ID };
	        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
	                data, keys);
	        if (userGroup.getCheckSessionOk()) {
	        	String cardId = userGroup.getVof2_ItemEntityUser().getStrCardNumber();
	    		log.setUserName(cardId);
	    		LogUtils.logFunctionalStart(log);
	            List<String> listValue = userGroup.getListParamsFromClient();
	            
	            String strDocumentId = listValue.get(0);
	            String strOrgId = listValue.get(1);
	            Long orgId = null;
	            if (!CommonUtils.isEmpty(strOrgId)) {
	            	orgId = Long.parseLong(strOrgId);
	            }
	            String title = listValue.get(2);
	            String strId = listValue.get(3);
	            Long id = null;
	            if (!CommonUtils.isEmpty(strId)) {
	            	id = Long.parseLong(strId);
	            }
	            String strStaffId = listValue.get(4);
	            Long staffId = null;
	            if (!CommonUtils.isEmpty(strStaffId)) {
	            	staffId = Long.parseLong(strStaffId);
	            }
	            
	            if (CommonUtils.isEmpty(strDocumentId) || staffId == null || id == null || orgId == null) {
	            	return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
	            }

	            AnswerDocumentDAO docDao = new AnswerDocumentDAO();
	            boolean result = docDao.replyDocument(id, strDocumentId, userGroup.getVof2_ItemEntityUser().getUserId());
	            if (result && staffId != null) {
	            	CommonControler smsDAO = new CommonControler();
	            	smsDAO.sentSMS(title, userGroup.getUserId2(), staffId,
	            			orgId, Constants.SMS_TEXT_CONFIG.WRITTEN_RESPONSE, "", 101L);
	            }
	            LogUtils.logFunctionalEnd(log);
	            return FunctionCommon.responseResult(ErrorCode.SUCCESS, 1, userGroup);
	        } else {
	            //truong hop bị timeout
	            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
	        }
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID, null, null);
		}
    }
}
