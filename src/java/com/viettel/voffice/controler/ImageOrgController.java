package com.viettel.voffice.controler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.ImageOrgDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.entity.EntityConfigImage;
import com.viettel.voffice.database.entity.EntityImageOrg;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.FileUtils;
import com.viettel.voffice.utils.LogUtils;

/**
 * 
 * @author 201812-Pitagon
 *
 */
@SuppressWarnings("deprecation")
public class ImageOrgController {

	private static final Logger LOGGER = Logger.getLogger(ImageOrgController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = ImageOrgController.class.getName();
    
    /**
     * <b> 201812-Pitagon:Them anh con dau don vi</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	public String addImageOrg(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.IMAGE_ORG,
                ConstantsFieldParams.TYPE
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strListStaffImageSign = listValue.get(0);
            // typeCheck la type: 1 la insert, 2 la update
            String typeCheck = listValue.get(1);
            if (CommonUtils.isEmpty(strListStaffImageSign)) {
                LOGGER.error("addImageOrg - username: " + userGroup.getCardId()
                        + " - Loi khong co thong tin imageOrg!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            
            //Tunghd Add
            List<EntityImageOrg> lstMarkInfo = new ArrayList<EntityImageOrg>();
            String lstImageOrg = listValue.get(0);
            if (!CommonUtils.isEmpty(lstImageOrg)) {
                Gson gson =  new GsonBuilder().create();
                Type listMark = new TypeToken<ArrayList<EntityImageOrg>>(){}.getType();
                lstMarkInfo = gson.fromJson(lstImageOrg, listMark);
            }
            
//            Type listStaffImageSignType = new TypeToken<ArrayList<EntityImageOrg>>() {
//            }.getType();
//            List<EntityImageOrg> listStaffImageSign = gson.fromJson(
//                    strListStaffImageSign, listStaffImageSignType);
            ImageOrgDAO dao = new ImageOrgDAO();
//            int result = 0;
            lstMarkInfo = dao.addImageOrg(userGroup.getUserId2(), lstMarkInfo, Long.valueOf(typeCheck));
            if (lstMarkInfo.isEmpty()) { 
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, null,
                        userGroup.getStrAesKey());
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstMarkInfo,
                        userGroup.getStrAesKey());
            }
        } catch (Exception ex) {
            LOGGER.error("addSignImage - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
	
    /**
     * @author TungHD
     * Insert cau hinh anh con dau vao database
     * addConfigImage
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	public String addConfigImage(HttpServletRequest request,
			String data, String isSecurity) {
		
		EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
				request, data, isSecurity, CLASS_NAME);
		if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
			LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
			return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
		}
		try {
			JSONObject json = new JSONObject(userGroup.getData());
			String[] keys = new String[]{
					ConstantsFieldParams.IMAGE_CONFIG,
					"status"
			};
			List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
			String strListOrgImageMark = listValue.get(0);
			String strStatus = listValue.get(1);
			int status = Integer.valueOf(strStatus);
			if (CommonUtils.isEmpty(strListOrgImageMark)) {
				LOGGER.error("addImageOrg - username: " + userGroup.getCardId()
						+ " - Loi khong co thong tin imageOrg!");
				return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
						null, null);
			}
//            Gson gson = new Gson();
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			/*EntityImageOrg imageOrg = gson.fromJson(
            		strListStaffImageSign, EntityImageOrg.class);*/
			
			Type listStaffImageSignType = new TypeToken<EntityConfigImage>() {
			}.getType();
			EntityConfigImage configImage = gson.fromJson(
			        strListOrgImageMark, listStaffImageSignType);
			ImageOrgDAO dao = new ImageOrgDAO();
			int result = 0;
			if (dao.addConfigImageOrg(userGroup.getUserId2(), configImage, status )) {
				result = 1;
			}
			return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result,
					userGroup.getStrAesKey());
		} catch (Exception ex) {
			LOGGER.error("addSignImage - username: " + userGroup.getCardId()
					+ " - Exception!", ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
					null, null);
		}
	}
	/**
	 * @author TungHD
	 * Lay ra cau hinh anh con dau tu database
	 * @param request
	 * @param data
	 * @param isSecurity
	 * @return
	 */
	public String getConfigImage(HttpServletRequest request,
			String data, String isSecurity) {
		
		EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
				request, data, isSecurity, CLASS_NAME);
		if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
			LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
			return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
		}
		try {
			JSONObject json = new JSONObject(userGroup.getData());
			String[] keys = new String[]{
					"orgId",
					"groupType",
			};
			List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
			String strListStaffImageSign = listValue.get(0);
			if (CommonUtils.isEmpty(strListStaffImageSign)) {
				LOGGER.error("addImageOrg - username: " + userGroup.getCardId()
						+ " - Loi khong co thong tin imageOrg!");
				return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
						null, null);
			}
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			Long orgId = null;
			Long groupType = null;
			if(!CommonUtils.isEmpty(listValue.get(0))){
				orgId = Long.parseLong(listValue.get(0));
				groupType = Long.parseLong(listValue.get(1));
			}
			ImageOrgDAO dao = new ImageOrgDAO();
			List<EntityConfigImage> entity = new ArrayList<EntityConfigImage>();
			entity = dao.getConfigImageOrg(userGroup.getUserId2(), orgId, groupType);
			return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entity,
					userGroup.getStrAesKey());
		} catch (Exception ex) {
			LOGGER.error("addSignImage - username: " + userGroup.getCardId()
					+ " - Exception!", ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
					null, null);
		}
	}

	/**
     * <b> 201812-Pitagon:upload anh con dau don vi</b>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	public String uploadImageOrg(HttpServletRequest request,
            String data, String isSecurity) {

        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                ConstantsFieldParams.IMAGE_ORG
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strListStaffImageSign = listValue.get(0);
            if (CommonUtils.isEmpty(strListStaffImageSign)) {
                LOGGER.error("uploadImageOrg - username: " + userGroup.getCardId()
                        + " - Loi khong co thong in imageOrg!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            Gson gson = new Gson();
            EntityImageOrg imageOrg = gson.fromJson(
            		strListStaffImageSign, EntityImageOrg.class);
            if( FileUtils.addImageOrg(userGroup.getUserId2(), imageOrg)){
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 1,
                    userGroup.getStrAesKey());
            }else{
            	return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, 0,
                        userGroup.getStrAesKey());
            }
        } catch (JSONException | JsonSyntaxException ex) {
            LOGGER.error("addSignImage - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
	
    public String getOrgMarkList(HttpServletRequest request, String data) {
        
        String[] keys = new String[] {
            ConstantsFieldParams.NAME,
            ConstantsFieldParams.CODE,
            ConstantsFieldParams.PATH_ORG,
            ConstantsFieldParams.KEYWORD,
            ConstantsFieldParams.START_RECORD,
            ConstantsFieldParams.PAGE_SIZE,
            ConstantsFieldParams.IS_COUNT
        };
        
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        if (!userGroup.getCheckSessionOk()) {
            LOGGER.error("getOrgMarkList - Session timeout!");
            return null;
        }
        try {
            LogUtils.logFunctionalStart(userGroup.getKpiLog());
        
            List<String> listValue = userGroup.getListParamsFromClient();
            String name = listValue.get(0);
            String code = listValue.get(1);
            String path = listValue.get(2);
            String keyword = listValue.get(3);
            Long startRecord = !CommonUtils.isEmpty(listValue.get(4)) ? Long.parseLong(listValue.get(4)) : 0L;
            Long pageSize = !CommonUtils.isEmpty(listValue.get(5)) ? Long.parseLong(listValue.get(5)) : 0L;
            String isCount = listValue.get(6);
            
            ImageOrgDAO dao = new ImageOrgDAO();
            Object result = dao.getOrgMarkList(keyword, name, code, path, "1".equals(isCount), startRecord, pageSize);
            
            LogUtils.logFunctionalEnd(userGroup.getKpiLog());
            return FunctionCommon.responseResult(ErrorCode.SUCCESS, result, userGroup);
        } catch (Exception ex) {
            LOGGER.error("getOrgMarkList - Exception - username: "+ userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    
    /**
     * @author DatDC
     * Tim anh con dau theo don vi va loai anh
     * findByConditionImageOrg
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
	public String findByConditionImageOrg(HttpServletRequest request,
			String data, String isSecurity) {
		
		EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
				request, data, isSecurity, CLASS_NAME);
		if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
			LOGGER.error("findByConditionImageOrg - " + userGroup.getEnumErrCode().toString());
			return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
		}
		try {
			JSONObject json = new JSONObject(userGroup.getData());
			String[] keys = new String[]{
					"orgId",
					"groupType",
			};
			List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
			String strListStaffImageSign = listValue.get(0);
			if (CommonUtils.isEmpty(strListStaffImageSign)) {
				LOGGER.error("findByConditionImageOrg - username: " + userGroup.getCardId()
						+ " - Loi khong co thong tin imageOrg!");
				return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
						null, null);
			}
			Long orgId = null;
			Long groupType = null;
			if(!CommonUtils.isEmpty(listValue.get(0))){
				orgId = Long.parseLong(listValue.get(0));
				groupType = Long.parseLong(listValue.get(1));
			}
			EntityImageOrg imageOrg = new EntityImageOrg();
            imageOrg.setImageOrgId(null);
            imageOrg.setVhrOrgId(orgId);
            imageOrg.setGroupType(groupType);
			ImageOrgDAO dao = new ImageOrgDAO();
			List<EntityImageOrg> entity = new ArrayList<EntityImageOrg>();
			entity = dao.findByConditionImageOrg(imageOrg, 0, 0);
			return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entity,
					userGroup.getStrAesKey());
		} catch (Exception ex) {
			LOGGER.error("findByConditionImageOrg - username: " + userGroup.getCardId()
					+ " - Exception!", ex);
			return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
					null, null);
		}
	}
	
	/**
	 * @author Tunghd
	 * Lay ra danh sach anh con dau cung loai hop le, khi khong co anh con dau mac dinh
	 * @param request
	 * @param data
	 * @param isSecurity
	 * @return
	 */
	public String getLstImageOther(HttpServletRequest request,
            String data, String isSecurity) {
        
        EntityUserGroup userGroup = EntityUserGroup.getUserGroupBySessionIdOfRequest(
                request, data, isSecurity, CLASS_NAME);
        if (userGroup.getEnumErrCode() != ErrorCode.SUCCESS) {
            LOGGER.error("addSignImage - " + userGroup.getEnumErrCode().toString());
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        try {
            JSONObject json = new JSONObject(userGroup.getData());
            String[] keys = new String[]{
                    "orgId",
                    "groupType",
            };
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            String strListStaffImageSign = listValue.get(0);
            if (CommonUtils.isEmpty(strListStaffImageSign)) {
                LOGGER.error("addImageOrg - username: " + userGroup.getCardId()
                        + " - Loi khong co thong tin imageOrg!");
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }
            Long orgId = null;
            Long groupType = null;
            if(!CommonUtils.isEmpty(listValue.get(0))){
                orgId = Long.parseLong(listValue.get(0));
                groupType = Long.parseLong(listValue.get(1));
            }
            ImageOrgDAO dao = new ImageOrgDAO();
            List<EntityImageOrg> entity = dao.getLstImageOther(userGroup.getUserId2(), orgId, groupType);
            return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, entity,
                    userGroup.getStrAesKey());
        } catch (Exception ex) {
            LOGGER.error("addSignImage - username: " + userGroup.getCardId()
                    + " - Exception!", ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }
}
