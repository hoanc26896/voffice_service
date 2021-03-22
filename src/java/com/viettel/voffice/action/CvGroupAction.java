/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import com.viettel.voffice.controler.CvGroupController;
import org.json.JSONException;

/**
 * REST Web Service
 *
 * @author datnv5
 */
@Path("CvGroupAction")
public class CvGroupAction {

    public static final String ROOT_ACTION = "CvGroupAction";

    /**
     * <b>Tim kiem danh sach nhom nhan vien</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getListGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CvGroupController cvGroup = new CvGroupController();
        String result = cvGroup.getListGroup(req, data, isSecurity);
        return result;
    }

    /**
     *
     * <b>Tim kiem so luong nhom nhan vien</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getCountListGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountListGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CvGroupController cvGroup = new CvGroupController();
        String result = cvGroup.getCountListGroup(req, data, isSecurity);
        return result;
    }

    /**
     *
     * Tim kiem nhan vien theo nhom nhan vien
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("getListStaffOfGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String getListStaffOfGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.getListStaffOfGroup(req, data, isSecurity);
    }

    /**
     * <b>Them nhom don vi ca nhan</b>
     * `@author cuongnv
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     * @throws org.json.JSONException
     */
    @POST
    @Path("addCvGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String addCvGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.addCvGroup(req, data, isSecurity);
    }

    /**
     * <b>Tim kiem nhom ca nhan</b>
     *
     * @cuongnv
     * @param req
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("search")
    @Consumes("application/x-www-form-urlencoded")
    public String search(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.search(req, data, isSecurity);
    }

    /**
     * <b>Xoa nhom ca nhan</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("deleteCvGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String deleteCvGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.deleteCvGroup(req, data, isSecurity);
    }

    /**
     * <b>Edit nhom ca nhan</b>
     *
     * @author cuongnv
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("editCvGroup")
    @Consumes("application/x-www-form-urlencoded")
    public String editCvGroup(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.editCvGroup(req, data, isSecurity);
    }
    
    /**
     * Lay danh sach nhom ca nhan theo list id va type
     * 
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("getListCvGroupByListId")
    @Consumes("application/x-www-form-urlencoded")
    public String getListCvGroupByListId(@Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CvGroupController cvGroup = new CvGroupController();
        return cvGroup.getListCvGroupByListId(req, data, isSecurity);
    }
}
