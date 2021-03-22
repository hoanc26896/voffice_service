package com.viettel.voffice.action;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.viettel.voffice.controler.DocumentSignController;
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
@Path("DocumentService")
public class DocumentSignService {

    @POST
    @Path("getListFields")
    @Consumes("application/x-www-form-urlencoded")
    public String getListFields(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getListFields(strSecurity, data, req);
        return strDataResponse;
    }

    @POST
    @Path("getListIndustry")
    @Consumes("application/x-www-form-urlencoded")
    public String getListIndustry(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getListIndustry(strSecurity, data, req);
        return strDataResponse;
    }

    @POST
    @Path("getTreeDepartSign")
    @Consumes("application/x-www-form-urlencoded")
    public String getTreeDepartSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getTreeDepartSign(strSecurity, data, req);
        return strDataResponse;
    }

    @POST
    @Path("getListUserSign")
    @Consumes("application/x-www-form-urlencoded")
    public String getListUserSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getListUserSign(strSecurity, data, req);
        return strDataResponse;
    }

//    @POST
//    @Path("getListDepartSign")
//    @Consumes("application/x-www-form-urlencoded")
//    public String getListDepartSign(@Context HttpServletRequest req,
//            @FormParam("data") String data,
//            @FormParam("isSecurity") String strSecurity) {
//        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, strSecurity);
//        DocumentSignController controller = new DocumentSignController();
//        String strDataResponse = controller.getListDepartSign(strSecurity, data, req);
//        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, strSecurity);
//        return strDataResponse;
//    }
    /**
     * Tao moi van ban trinh ky
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("addText")
    @Consumes("application/x-www-form-urlencoded")
    public String addText(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.addText(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * Trinh ky lai van ban bi huy  luong 
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("resignText")
    @Consumes("application/x-www-form-urlencoded")
    public String resignText(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.resignText(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * Trinh ky van ban
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("sendAndSign")
    @Consumes("application/x-www-form-urlencoded")
    public String sendAndSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.sendAndSign(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * Huy luong trinh ky --> Thay doi trang thai trinh ky
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("changeStateSign")
    @Consumes("application/x-www-form-urlencoded")
    public String changeStateSign(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.changeStateSign(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * Thay doi trang thai trinh kys
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getPublicDocumentTypeIdConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getPublicDocumentTypeIdConfig(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getPublicDocumentTypeIdConfig(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * Lay danh sach ca nhan trinh ky kem don vi
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getLitsUserSignWithRole")
    @Consumes("application/x-www-form-urlencoded")
    public String getLitsUserSignWithRole(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getLitsUserSignWithRole(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * ham lay danh sach don vi tien te
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("getListMoneyUnit")
    @Consumes("application/x-www-form-urlencoded")
    public String getListMoneyUnit(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.getListMoneyUnit(strSecurity, data, req);
        return strDataResponse;
    }

    /**
     * cap nhat van ban tai chinh
     *
     * @param req
     * @param data
     * @param strSecurity
     * @return
     */
    @POST
    @Path("transferMoneyAction")
    @Consumes("application/x-www-form-urlencoded")
    public String transferMoneyAction(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String strSecurity) {
        DocumentSignController controller = new DocumentSignController();
        String strDataResponse = controller.transferMoneyAction(strSecurity, data, req);
        return strDataResponse;
    }
}
