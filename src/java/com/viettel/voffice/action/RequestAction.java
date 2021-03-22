/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.voffice.action;

import com.viettel.voffice.controler.RequestController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author kiennt45
 */
@Path("requestAction")
public class RequestAction {
    /**
     * Lay danh sach yeu cau/kien nghi
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.getListRequestAdvance(isSecurity, data, request);
    }

    /**
     * Tim kiem nang cao danh sach yeu cau/kien nghi
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListRequestAdvance")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRequestAdvance(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.getListRequestAdvance(isSecurity, data, request);
    }

    /**
     * Lay danh sach chi tiet kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getRequestDetail")
    @Consumes("application/x-www-form-urlencoded")
    public String getRequestDetail(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.getRequestDetail(isSecurity, data, request);
    }

    /**
     * Them moi kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String addRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.addRequest(isSecurity, data, request);
    }

    /**
     * Xoa kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.deleteRequest(isSecurity, data, request);
    }

    /**
     * Tu giai quyet cap 1 kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("addResolve")
    @Consumes("application/x-www-form-urlencoded")
    public String addResolve(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.addResolve(isSecurity, data, request);
    }

    /**
     * Chuyen kien nghi de xuat len cap tren
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("forwardLevel")
    @Consumes("application/x-www-form-urlencoded")
    public String forwardLevel(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.forwardLevel(isSecurity, data, request);
    }

    /**
     * Xac nhan giai quyet kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("confirmSolutionRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String confirmSolutionRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.confirmSolutionRequest(isSecurity, data, request);
    }

    /**
     * Dong kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("closeRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String closeRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.closeRequest(isSecurity, data, request);
    }

    /**
     * Giao viec kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("assignRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String assignRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.assignRequest(isSecurity, data, request);
    }

    /**
     * Giao viec kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("sendRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String sendRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.sendRequest(isSecurity, data, request);
    }

    /**
     * Lay quyen kien nghi de xuat
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getRoleRequest")
    @Consumes("application/x-www-form-urlencoded")
    public String getRoleRequest(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.getRoleRequest(isSecurity, data, request);
    }

    /**
     * Lay nguon goc tu lich su xu ly
     * @param request
     * @param data : du lieu client gui len
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getRequestId")
    @Consumes("application/x-www-form-urlencoded")
    public String getRequestId(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        RequestController controller = new RequestController();
        return controller.getRequestId(isSecurity, data, request);
    }
    
    @POST
    @Path("UpdateRequestEmpConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String updateRequestEmpConfig(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        RequestController controller = new RequestController();
        return controller.updateRequestEmpConfig(request, data);
    }
    
    @POST
    @Path("GetListRequestEmpConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getListRequestEmpConfig(@Context HttpServletRequest request,
            @FormParam("data") String data) {
        
        RequestController controller = new RequestController();
        return controller.getListRequestEmpConfig(request, data);
    }
}