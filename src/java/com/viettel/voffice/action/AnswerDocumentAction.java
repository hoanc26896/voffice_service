package com.viettel.voffice.action;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.viettel.voffice.controler.AnswerDocumentController;

@Path("answerDocumentAction")
public class AnswerDocumentAction {
    
    @POST
    @Path("getListGroupReceiverRequestResponse")
    @Consumes("application/x-www-form-urlencoded")
    public String getListGroupReceiverRequestResponse(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	AnswerDocumentController controller = new AnswerDocumentController();
        return controller.getListGroupReceiverRequestResponse(request, data);
    }
    
    @POST
    @Path("insertDocumentReply")
    @Consumes("application/x-www-form-urlencoded")
    public String insertDocumentReply(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	AnswerDocumentController controller = new AnswerDocumentController();
        return controller.insertDocumentReply(request, data);
    }
    
    @POST
    @Path("cancelDocumentReply")
    @Consumes("application/x-www-form-urlencoded")
    public String cancelDocumentReply(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	AnswerDocumentController controller = new AnswerDocumentController();
        return controller.cancelDocumentReply(request, data);
    }
    
    @POST
    @Path("getListAnswerDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String getListAnswerDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	AnswerDocumentController controller = new AnswerDocumentController();
        return controller.getListAnswerDocument(request, data);
    }
    
    @POST
    @Path("replyDocument")
    @Consumes("application/x-www-form-urlencoded")
    public String replyDocument(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
    	AnswerDocumentController controller = new AnswerDocumentController();
        return controller.replyDocument(request, data);
    }
}