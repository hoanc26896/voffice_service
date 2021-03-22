/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.TextReportController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import org.json.JSONException;

/**
 *
 * @author Hiendv2
 */
@Path("TextReportAction")
public class TextReportAction {

    /**
     * 1.Thong ke ca nhan bi tu choi van ban
     *
     * @param request
     * @param data du lieu gui len
     * @author hiendv2
     * @return
     */
    @POST
    @Path("getListStaffByRejectSign")
    @Consumes("application/x-www-form-urlencoded")
    public String getListStaffByRejectSign(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextReportController tc = new TextReportController();
        String result = tc.getListStaffByRejectSign(request, data, isSecurity);
        return result;
    }

    /**
     * 2. Lấy danh sách văn bản bị từ chối
     *
     * @param startDate :ngày bắt đầu lấy
     * @param endDate :ngày cuối lấy
     * @param userRejectId : id cá nhân đăng nhập
     * @return: Danh sach van ban do user dang nhap tu choi
     */
    @POST
    @Path("getLstDeatilDocRejectedByUserLogin")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstDeatilDocRejectedByUserLogin(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextReportController tc = new TextReportController();
        String result = tc.getLstDeatilDocRejectedByUserLogin(request, data, isSecurity);
        return result;
    }

    /**
     * 3. Biểu đồ đường Thống kê số lượng văn bản theo dạng đường
     *
     * @param request
     * @return
     * @throws ProtocolException
     */
    @POST
    @Path("getStatisticalLineReportReject")
    @Consumes("application/x-www-form-urlencoded")
    public String getStatisticalLineReportReject(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextReportController tc = new TextReportController();
        String result = tc.getStatisticalLineReportReject(request, data, isSecurity);
        return result;
    }

    /**
     * 4. danh sach van ban user được chọn bị từ chối ký
     *
     * @param request
     * @return
     * @throws ProtocolException
     */
    @POST
    @Path("getLstDetailDocRejectedOfStaff")
    @Consumes("application/x-www-form-urlencoded")
    public String getLstDetailDocRejectedOfStaff(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextReportController tc = new TextReportController();
        String result = tc.getLstDetailDocRejectedOfStaff(request, data, isSecurity);
        return result;
    }

    /**
     * bao cao van ban bi tu choi
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("reportTextRejectedDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String reportTextRejectedDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        TextReportController tc = new TextReportController();
        String result = tc.reportTextRejectedDetail(request, data, isSecurity);
        return result;
    }

    /**
     * Bao cao van ban bi tu choi tong hop
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("reportTextRejectedSumary")
    @Consumes("application/x-www-form-urlencoded")
    public String reportTextRejectedSumary(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        
        TextReportController controller = new TextReportController();
        return controller.reportTextRejectedSumary(request, data, isSecurity);
    }

    /**
     * <b>Bao cao chi tiet van ban ky 5 ngay</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("reportTimeSignText")
    @Consumes("application/x-www-form-urlencoded")
    public String reportTimeSignText(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        
        TextReportController controller = new TextReportController();
        return controller.reportTimeSignText(request, data, isSecurity);
    }
    
    /**
     * <b>Bao cao thoi gian xu ly van ban</b><br>
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("ReportTextProcessingTime")
    @Consumes("application/x-www-form-urlencoded")
    public String reportTextProcessingTime(
            @Context HttpServletRequest request,
            @FormParam("data") String data) throws JSONException {

        TextReportController controller = new TextReportController();
        return controller.reportTextProcessingTime(request, data);
    }
    
    /**
     * <b>Bao cao thoi gian xu ly van ban</b><br>
     *
     * @param request
     * @param data
     * @return
     * @throws JSONException
     */
    @POST
    @Path("ReportTextRejectionCount")
    @Consumes("application/x-www-form-urlencoded")
    public String reportTextRejectionCount(
            @Context HttpServletRequest request,
            @FormParam("data") String data) throws JSONException {

        TextReportController controller = new TextReportController();
        return controller.reportTextRejectionCount(request, data);
    }
}