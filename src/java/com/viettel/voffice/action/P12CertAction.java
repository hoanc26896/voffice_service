/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import com.viettel.voffice.controler.P12CertControler;

/**
 *
 * @author hanhnq21
 */
@Path("P12CertAction")
public class P12CertAction {

    /**
     * tim kiem chung thu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("search")
    @Consumes("application/x-www-form-urlencoded")
    public String search(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        P12CertControler dc = new P12CertControler();
        String result = dc.actionSearch(request, data, isSecurity);
        return result;
    }

    /**
     * huy chung thu
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("cancelRegCert")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelRegCert(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        P12CertControler dc = new P12CertControler();
        String result = dc.actionCancelRegCert(request, data, isSecurity);
        return result;
    }

    /**
     * huy chung thu tren web
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("actionCancelRegCertWeb")
    @Consumes("application/x-www-form-urlencoded")
    public String actionCancelRegCertWeb(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        P12CertControler dc = new P12CertControler();
        String result = dc.actionCancelRegCertWeb(request, data, isSecurity);
        return result;
    }
}