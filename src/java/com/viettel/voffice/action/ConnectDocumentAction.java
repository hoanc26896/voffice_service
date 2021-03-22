package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.controler.ConnectDocumentController;
import com.viettel.voffice.utils.LogUtils;

@Path("connectDocumentAction")
public class ConnectDocumentAction {

    public static final String ROOT_ACTION = "connectDocumentAction";
    
    @POST
    @Path("getListConnectDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getListConnectDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectDocumentController controller = new ConnectDocumentController();
        String result = controller.getListConnectDocument(request, data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("addStateConnectDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String addStateConnectDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectDocumentController controller = new ConnectDocumentController();
        String result = controller.addStateConnectDocument(request, data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
    
    @POST
    @Path("getListConnectDocOutDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getListConnectDocOutDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        ConnectDocumentController controller = new ConnectDocumentController();
        String result = controller.getListConnectDocOutDetail(request, data);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}
