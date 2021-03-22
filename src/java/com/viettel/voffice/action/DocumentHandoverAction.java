/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.DocumentHandoverController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author cuongnv
 */
@Path("DocumentHandoverAction")
public class DocumentHandoverAction {

    /**
     * <b>Tim kiem van ban de ban giao</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("searchDocumentHandover")
    @Consumes("application/x-www-form-urlencoded")
    public String searchDocumentHandover(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentHandoverController dc = new DocumentHandoverController();
        return dc.searchDocumentHandover(request, data, isSecurity);     
    }

    /**
     * <b>Ban giao van ban</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("handOverDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String handOverDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentHandoverController dc = new DocumentHandoverController();
        return dc.handOverDocument(request, data, isSecurity);
    }

    /**
     * <b>Lich su ban giao</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("historyDocumentHandover")
    @Consumes("application/x-www-form-urlencoded")
    public String historyDocumentHandover(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentHandoverController dc = new DocumentHandoverController();
        return dc.historyDocumentHandover(request, data, isSecurity);
    }

    /**
     * <b>Xuat bao cao danh sach van ban di den don vi</b>
     *
     * @author cuongnv
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("exportReportDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String exportReportDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        DocumentHandoverController dc = new DocumentHandoverController();
        return dc.exportReportDocument(request, data, isSecurity);
    }

    /**
     * <b>Thong tin chi tiet van ban da ban giao</b>
     *
     * @author HaNH
     * @param request
     * @param data
     * @param isSecurity
     * @return
     *
     */
    @POST
    @Path("getDetailDocumentHandover")
    @Consumes("application/x-www-form-urlencoded")
    public String getDetailDocumentHandover(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity
    ) {
        DocumentHandoverController dc = new DocumentHandoverController();
        return dc.getDetailDocumentHandover(request, data, isSecurity);
    }
    
}
