package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.ImageOrgController;
import com.viettel.voffice.utils.LogUtils;

/**
 * 
 * @author: 201812-Pitagon 
 *
 */
@Path("imageOrgAction")
public class ImageOrgAction {

	public static final String ROOT_ACTION = "imageOrgAction";
	
	/**
     * 201812-Pitagon:them moi anh con dau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addImageOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String addImageOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageOrgController imageController = new ImageOrgController();
        String result = imageController.addImageOrg(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    /**
     * TungHD insert cau hinh con dau
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addConfigImage")
    @Consumes("application/x-www-form-urlencoded")
    public String addConfigImage(@Context HttpServletRequest request,
    		@FormParam("data") String data,
    		@FormParam("isSecurity") String isSecurity) {
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
    	ImageOrgController imageController = new ImageOrgController();
    	String result = imageController.addConfigImage(request, data, isSecurity);
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
    	return result;
    }
    
    
    /**
     * TungHd lay thong tin con dau cau hinh mac dinh
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getConfigImage")
    @Consumes("application/x-www-form-urlencoded")
    public String getConfigImage(@Context HttpServletRequest request,
    		@FormParam("data") String data,
    		@FormParam("isSecurity") String isSecurity) {
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
    	ImageOrgController imageController = new ImageOrgController();
    	String result = imageController.getConfigImage(request, data, isSecurity);
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
    	return result;
    }
    
    /**
     * 201812-Pitagon: upload file anh con dau
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("uploadImageOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String uploadImageOrg(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageOrgController imageController = new ImageOrgController();
        String result = imageController.uploadImageOrg(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("getOrgMarkList")
    @Consumes("application/x-www-form-urlencoded")
    public String getOrgMarkList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageOrgController imageController = new ImageOrgController();
        String result = imageController.getOrgMarkList(request, data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * TungHd tim con dau theo id anh va id don vi
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("findByConditionImageOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String findByConditionImageOrg(@Context HttpServletRequest request,
    		@FormParam("data") String data,
    		@FormParam("isSecurity") String isSecurity) {
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
    	ImageOrgController imageController = new ImageOrgController();
    	String result = imageController.findByConditionImageOrg(request, data, isSecurity);
    	LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
    	return result;
    }
    
    /**
     * TungHD lay anh khac con hieu luc cua don vi do theo groupType
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getLstImageOther")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstImageOther(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ImageOrgController imageController = new ImageOrgController();
        String result = imageController.getLstImageOther(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}
