package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.viettel.voffice.controler.OrgCriteriaController;

@Path("orgCriteria")
public class OrgCriteriaAction {

    @POST
    @Path("insertOrgCriteriaConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String insertOrgCriteriaConfig(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        OrgCriteriaController oc = new OrgCriteriaController();
        return oc.insertOrgCriteriaConfig(isSecurity, data, request);
    }

    @POST
    @Path("getDetailOrgCriteriaConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getDetailOrgCriteriaConfig(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        OrgCriteriaController oc = new OrgCriteriaController();
        return oc.getDetailOrgCriteriaConfig(isSecurity, data, request);
    }

    @POST
    @Path("getListOrgCriteriaConfig")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrgCriteriaConfig(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        OrgCriteriaController oc = new OrgCriteriaController();
        return oc.getListOrgCriteriaConfig(isSecurity, data, request);
    }

    @POST
    @Path("getListOrgCriteriaHistory")
    @Consumes("application/x-www-form-urlencoded")
    public String getListOrgCriteriaHistory(@Context HttpServletRequest request, @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        OrgCriteriaController oc = new OrgCriteriaController();
        return oc.getListOrgCriteriaHistory(isSecurity, data, request);
    }
}
