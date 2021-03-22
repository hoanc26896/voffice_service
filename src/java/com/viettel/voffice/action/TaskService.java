/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.TaskServiceController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.json.JSONException;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.utils.LogUtils;

/**
 *
 * @author kiennt45
 */
@Path("TaskService")
public class TaskService {

    public static final String ROOT_ACTION = "TaskService";

    /**
     * Creates a new instance of MettingResource
     */
    public TaskService() {
    }

    @POST
    @Path("addTask")
    @Consumes("application/x-www-form-urlencoded")
    public String addTask(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.addTask(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListCommander")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCommander(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getTreeCommander(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getTreeEnforcement")
    @Consumes("application/x-www-form-urlencoded")
    public String getTreeEnforcement(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getTreeEnforcement(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRequest(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getListRequest(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("getListPropose")
    @Consumes("application/x-www-form-urlencoded")
    public String getListPropose(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getListPropose(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    @POST
    @Path("forwardRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String forwardRequest(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.forwardRequest(strSecurity, data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
        return strDataResponse;
    }

    /**
     * Huy danh gia KI ca nhan
     *
     * @author SonDN
     * @param request
     * @param data du lieu gui len
     * @return
     */
    @POST
    @Path("deleteKIemp")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteKIemp(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) throws JSONException {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.deleteKIemp(req, data, strSecurity);
        return strDataResponse;
    }

    /**
     * <b>Lay Ki ca nhan</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 2/2/2017
     */
    @POST
    @Path("getKIEmp")
    @Consumes("application/x-www-form-urlencoded")
    public String getKIEmp(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getKIEmp(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Danh sach cong viec duoc danh gia cua nv</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 2/2/2017
     */
    @POST
    @Path("getDetailKIEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String getDetailKIEmployee(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getDetailKIEmployee(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Kiểm tra đơn vị đã đánh giá KI chưa</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 6/2/2017
     */
    @POST
    @Path("checkKIOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String checkKIOrg(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.checkKIOrg(strSecurity, data, req);
        return strDataResponse;
    }
    /**
     * <b>Kiểm tra đơn vị đã đánh giá KI chưa</b>
     *
     * @author DuanNV
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 20/10/2017
     */
    @POST
    @Path("checkEmpl")
    @Consumes("application/x-www-form-urlencoded")
    public String checkEmpl(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.checkEmpl(strSecurity, data, req);
        return strDataResponse;
    }
    
    /**
     *  <b>Lấy ra điểm min max cấu hình tỉ lệ</b>
     *
     * @author DuanNV
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 20/10/2017
     */
    @POST
    @Path("getListRatioPoint")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRatioPoint(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getListRatioPoint(strSecurity, data, req);
        return strDataResponse;
    }
    
    /**
     * <b>Kiểm tra đơn vị da trinh ky chua</b>
     *
     * @author DuanNV
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 20/10/2017
     */
    @POST
    @Path("isCheckPermissionSign")
    @Consumes("application/x-www-form-urlencoded")
    public String isCheckPermissionSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.isCheckPermisstionSign(strSecurity, data, req);
        return strDataResponse;
    }
     /**
     * <b>Kiểm tra đơn vị da trinh ky chua</b>
     *
     * @author DuanNV
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 20/10/2017
     */
    @POST
    @Path("isCheckStatusSign")
    @Consumes("application/x-www-form-urlencoded")
    public String isCheckStatusSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.isCheckStatusSign(strSecurity, data, req);
        return strDataResponse;
    }
    /**
     * <b>Kiểm tra đơn vị đã đánh giá KI chưa</b>
     * <b>Get diem don vi KI danh gia</b>
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 6/2/2017
     */
    @POST
    @Path("checkPointUnit")
    @Consumes("application/x-www-form-urlencoded")
    public String checkPointUnit(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.checkPointUnit(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Them moi, cap nhat KI don vi</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 6/2/2017
     */
    @POST
    @Path("updateKIOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String updateKIOrg(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.updateKIOrg(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Lấy danh sách nhân viên đánh giá KI</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 6/2/2017
     */
    @POST
    @Path("getListEmpKI")
    @Consumes("application/x-www-form-urlencoded")
    public String getListEmpKI(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getListEmpKI(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Lấy ty le nv theo KI don vi</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 17/2/2017
     */
    @POST
    @Path("percentKI")
    @Consumes("application/x-www-form-urlencoded")
    public String percentKI(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.percentKI(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Kiem tra don vi co phai don vi con thap nhat hay cap trung gian</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 22/2/2017
     */
    @POST
    @Path("isLeafOrg")
    @Consumes("application/x-www-form-urlencoded")
    public String isLeafOrg(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.isLeafOrg(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Huy ky danh gia KI</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 22/2/2017
     */
    @POST
    @Path("unResignKIEmp")
    @Consumes("application/x-www-form-urlencoded")
    public String unResignKIEmp(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.unResignKIEmp(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Ky,Ghi danh gia ki</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 3/3/2017
     */
    @POST
    @Path("signKI")
    @Consumes("application/x-www-form-urlencoded")
    public String signKI(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.signKI(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * <b>Lay danh sach file ky</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 4/3/2017
     */
    @POST
    @Path("getListFilesKI")
    @Consumes("application/x-www-form-urlencoded")
    public String getListFilesKI(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getListFilesKI(strSecurity, data, req);
        return strDataResponse;
    }
     //DuanNV them   danh sach don vi danh gia nhan vien
      /**
     * danh sach don vi danh gia nhan vien
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListSysRoleEvaluate")
    @Consumes("application/x-www-form-urlencoded")
    public String getListSysRoleEvaluate(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TaskServiceController controller = new TaskServiceController();
        return controller.getListSysRoleEvaluate(request, data, isSecurity);
        
    }
    // End
    
    /**
     * <b>Trinh ky</b>
     *
     * @author pm1_os20
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 28/10/2017
     */
    @POST
    @Path("updateRequisitionDirect")
    @Consumes("application/x-www-form-urlencoded")
    public String updateRequisitionDirect(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.updateRequisitionDirect(strSecurity, data, req);
        return strDataResponse;
    }
    
    /**
     * Lay file ky de hien thi
     *
     * @author pm1_os20
     * @param req
     * @param data
     * @param strSecurity
     * @return
     * @since 28/10/2017
     */
    @POST
    @Path("getFilesKIToView")
    @Consumes("application/x-www-form-urlencoded")
    public String getFilesKIToView(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        TaskServiceController controller = new TaskServiceController();
        String strDataResponse = controller.getFilesKIToView(strSecurity, data, req);
        return strDataResponse;
    }
}