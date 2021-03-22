package com.viettel.voffice.action;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.viettel.voffice.controler.FileControler;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.controler.SignBriefcaseControler;
import com.viettel.voffice.controler.TemplateController;
import com.viettel.voffice.controler.TextMarkSyncControler;
import com.viettel.voffice.utils.LogUtils;
import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/Files")
public class FileService {

    public static final String ROOT_ACTION = "/Files";

    @POST
    @Path("/Download")
    @Consumes("application/x-www-form-urlencoded")
    public Response getFile(@Context HttpServletRequest req,
            @FormParam("data") String data) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, "");
        FileControler fileCt = new FileControler();
        Response result = fileCt.getFileFromAttachId(data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, "");
        return result;
    }

    /**
     * Thuc hien dang nhap he thong
     *
     * @param req
     * @param data
     * @return
     */
    @POST
    @Path("/FileSize")
    @Consumes("application/x-www-form-urlencoded")
    public String getFileSize(@Context HttpServletRequest req,
            @FormParam("data") String data) {
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_START, ErrorCode.SUCCESS, null, data, "");
        FileControler fileSize = new FileControler();
        String result = fileSize.getFileSizeAndPage(data, req);
        LogUtils.writeLog(req, ROOT_ACTION, Constants.ACT_STOP, ErrorCode.SUCCESS, null, data, "");
        return result;
    }

    @POST
    @Path("/DownloadContentFile")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadContentFile(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        FileControler fc = new FileControler();
        Response result = fc.downloadContentFile(req, data, isSecurity);
        return result;
    }

    //Files.downloadContentFileCommentSign
    @POST
    @Path("/downloadContentFileCommentSign")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadContentFileCommentSign(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        FileControler fc = new FileControler();
        Response result = fc.downloadContentFileCommentSign(req, data, isSecurity);
        return result;
    }

    @POST
    @Path("/getInfoFile")
    @Consumes("application/x-www-form-urlencoded")
    public String getInfoFile(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        FileControler fc = new FileControler();
        String result = fc.getInfoFile(req, data, isSecurity);

        return result;
    }

    /**
     * <b>Upload file tam</b>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @param request
     * @param data
     * @param is
     * @param fileDetail
     * @return
     */
    @POST
    @Path("/UploadTmpFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadTmpFile(
            @Context HttpServletRequest request,
            @FormDataParam("data") String data,
            @FormDataParam("file") InputStream is,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        FileControler fc = new FileControler();
        return fc.uploadTmpFile(request, data, is, fileDetail);
    }

    /**
     * <b>Download file anh nhan vien</b><br>
     *
     * @param request
     * @param cardId Ma nhan vien
     * @param size Kich thuoc anh
     * @return
     */
    @GET
    @Path("/DownloadStaffImage/{cardId}/{size}")
    public Response downloadStaffImage(
            @Context HttpServletRequest request,
            @PathParam("cardId") String cardId,
            @PathParam("size") String size) {
        FileControler fileControler = new FileControler();
        return fileControler.downloadStaffImage(request, cardId, size);
    }

    /**
     * download file
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("/downloadTextMarkContentFile")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadTextMarkContentFile(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TextMarkSyncControler controler = new TextMarkSyncControler();
        Response result = controler.downloadTextMarkContentFile(req, data, isSecurity);
        return result;
    }

    /**
     * in barcode
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("/printBarCode")
    @Consumes("application/x-www-form-urlencoded")
    public Response printBarCode(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler controler = new SignBriefcaseControler();
        Response result = controler.printBarCode(req, data, isSecurity);
        return result;
    }

    /**
     * download file
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("/downloadFileSignBriefCase")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadFileSignBriefCase(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        SignBriefcaseControler controler = new SignBriefcaseControler();
        Response result = controler.downloadFile(req, data, isSecurity);
        return result;
    }

    /**
     * <b>Download file anh hien thi ngay le tet</b><br>
     *
     * @param request
     * @param imageName Ten anh hien thi
     * @return
     */
    @GET
    @Path("/downloadImageIconApp/{imageName}")
    public Response downloadImageIconApp(
            @Context HttpServletRequest request,
            @PathParam("imageName") String imageName) {
        FileControler fileControler = new FileControler();

        return fileControler.downloadImageIconApp(request, imageName);
    }

    /**
     * <b>Download file anh bao cao van ban ky</b><br>
     *
     * @param request
     * @param imageName Ten anh hien thi
     * @return
     */
    @GET
    @Path("/downloadImageReport/{imgName}")
    public Response downloadImageReport(
            @Context HttpServletRequest request,
            @PathParam("imgName") String imageName) {
        FileControler fileControler = new FileControler();

        return fileControler.downloadImageReport(request, imageName);
    }

    @POST
    @Path("/updateFilePageFileSize")
    @Consumes("application/x-www-form-urlencoded")
    public String updateFilePageFileSize(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        FileControler fc = new FileControler();
        String result = fc.updateFilePageFileSize(req, data, isSecurity);
        return result;
    }

    @POST
    @Path("/PreviewMeetingMinutes")
    @Consumes("application/x-www-form-urlencoded")
    public Response previewMeetingMinutes(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        FileControler controller = new FileControler();
        return controller.previewMeetingMinutes(request, data, isSecurity);
    }
    
    /**
     * View file bao cao KI
     *  
     * @author pm1_os20
     * @param request
     * @param data
     * @param isSecurity
     * @return 
     * @since 28/10/2017
     */
    @POST
    @Path("/PreviewEmpRatingReport")
    @Consumes("application/x-www-form-urlencoded")
    public Object previewEmpRatingReport(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {

        FileControler controller = new FileControler();
        return controller.previewEmpRatingReport(request, data, isSecurity);
    }

    /**
     * <b>Download anh chu ky</b><br>
     *
     * @param request
     * @param staffImageSignId
     * @return
     */
    @GET
    @Path("/DownloadSignatureImage/{staffImageSignId}")
    public Response downloadSignatureImage(
            @Context HttpServletRequest request,
            @PathParam("staffImageSignId") String staffImageSignId) {
        FileControler controller = new FileControler();
        return controller.downloadSignatureImage(request, staffImageSignId);
    }
    
    /**
     * Pitagon-2018
     * <b>Download anh con dau don vi</b><br>
     *
     * @param request
     * @param staffImageSignId
     * @return
     */
    @GET
    @Path("/downloadImageOrg/{imageOrgId}")
    public Response downloadImageOrg(
            @Context HttpServletRequest request,
            @PathParam("imageOrgId") String imageOrgId) {
        FileControler controller = new FileControler();
        return controller.downloadImageOrg(request, imageOrgId);
    }

    @POST
    @Path("/PreviewAppendixChat")
    @Consumes("application/x-www-form-urlencoded")
    public Response previewAppendixChat(
            @Context HttpServletRequest request,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        FileControler controller = new FileControler();
        return controller.previewAppendixChat(request, data, isSecurity);
    }

    /**
     * download file bieu mau
     *
     * @param req
     * @param data
     * @param isSecurity
     * @return
     */
    @POST
    @Path("/downloadFileTemplate")
    @Consumes("application/x-www-form-urlencoded")
    public Response downloadFileTemplate(
            @Context HttpServletRequest req,
            @FormParam("data") String data,
            @FormParam("isSecurity") String isSecurity) {
        TemplateController tc = new TemplateController();
        Response result = tc.downloadFile(req, data, isSecurity);
        return result;
    }
    
    /**
     * <b>Download anh</b><br>
     *
     * @param request
     * @param imageId 
     * @return
     */
    @GET
    @Path("/DownloadImage/{imageId}")
    public Response downloadSignatureImage(
            @Context HttpServletRequest request,
            @PathParam("imageId") Long imageId) {
        
        FileControler controller = new FileControler();
        return controller.downloadImage(request, imageId);
    }
    
    //datnv5 thuc hien download file theo byterange
    
//    @GET
//    @Consumes("application/x-www-form-urlencoded")
//    @Path("/get-file")
//    public Response getFile(@Context HttpServletRequest request){
//       FileService fileService = new FileService();
//       return fileService.getFileByRange(request);
//    }

    @GET
    @Path("/DownloadSignatureImageByCardId/{cardId}/{signedDate: [0-9]{8}}")
    public Response downloadSignatureImageByCardId(
            @Context HttpServletRequest request,
            @PathParam("cardId") String cardId,
            @PathParam("signedDate") String signedDate) {
        FileControler controller = new FileControler();
        return controller.downloadSignatureImageByCardId(request, cardId, signedDate);
    }
}
