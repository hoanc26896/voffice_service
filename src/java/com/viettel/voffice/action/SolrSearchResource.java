/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.SolrSearchController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.utils.LogUtils;
import org.json.JSONException;

/**
 * REST Web Service
 *
 * @author thanght6
 */
@Path("solrSearch")
public class SolrSearchResource {

    public static final String ROOT_ACTION = "solrSearch";

    /**
     * Lay so luong cong van tim kiem duoc
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getCountItem")
    @Consumes("application/x-www-form-urlencoded")
    public String getCountItem(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.getCountDocument(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach cong van
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getListItem")
    @Consumes("application/x-www-form-urlencoded")
    public String getListItem(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.getListDocument(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     *
     * @param request
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getStatusUser")
    @Consumes("application/x-www-form-urlencoded")
    public String getStatusUser(@Context HttpServletRequest request) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, "", "");
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.getStatusUser(request);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, "", "");
        return result;
    }

    /**
     * Lay so luong employee tim kiem duoc
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getEmployeeCount")
    @Consumes("application/x-www-form-urlencoded")
    public String getEmployeeCount(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.getCountEmployee(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * Lay danh sach employee
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("getEmployeeList")
    @Consumes("application/x-www-form-urlencoded")
    public String getEmployeeList(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.getListEmployee(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }

    /**
     * index employee list
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    @POST
    @Path("indexEmployee")
    @Consumes("application/x-www-form-urlencoded")
    public String indexEmployee(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) throws JSONException {
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, isSecurity);
        SolrSearchController ssc = new SolrSearchController();
        String result = ssc.indexEmployee(request, data, isSecurity);
        LogUtils.writeLog(request, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, isSecurity);
        return result;
    }
}