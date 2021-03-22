/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.controler.CorrectDocumentControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author datnv5
 */
@Path("CorrectDocument")
public class CorrectDocument {
    @POST
    @Path("checkCorrectDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String checkCorrectDocument(@Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        CorrectDocumentControler controller = new CorrectDocumentControler();
        String result = controller.checkCorrectDocument(request, data);
        return result;
    }
}
