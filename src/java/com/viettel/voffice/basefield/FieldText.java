package com.viettel.voffice.basefield;

/**
 * lớp
 *
 * @author datnv5
 *
 * Đặt tên chữ đều là chữ hoa Riêng các field trả về client đặt định danh đúng
 * định danh text trả về
 */
public class FieldText {
    //lấy tất cả trạng thái văn bản

    public static final String statusAll = "statusAll";
    //lấy count văn bản chờ ký
    public static final String countTextWaitSign = "countTextWaitSign";
    public static final String countTextSigned = "countTextSigned";
    public static final String countTextrejectd = "countTextrejectd";
    public static final String countTextWaitingInitial = "countTextWaitingInitial";
    public static final String countTextSignedInitial = "countTextSignedInitial";
    public static final String countTextRejectInitial = "countTextRejectInitial";
    public static final String countTextSignRejectByLeader = "countTextSignRejectByLeader";
    public static final String countTextAll = "countTextAll";
    public static final String countTextWaitSignMoney = "countTextWaitSignMoney";
    //lấy count văn bản chờ xét duyệt
    public static final String countTextSecretaryAll = "countTextSecretaryAll";
    public static final String countTextSecretaryWaitSign = "countTextSecretaryWaitSign";
    public static final String countTextSecretarySigned = "countTextSecretarySigned";
    public static final String countTextSecretaryReject = "countTextSecretaryReject";
    //======================================================================
    //Khai báo danh sách thread  thực cần thực hiện để lấy thông tin chi tiết
    // lấy thông tin  chi tiết ban đầu
    public static final String FUNCTION_GETDETAIL = "FUNCTION_GETDETAIL";
    //lấy thông tin ký nháy
    public static final String FUNCTION_GETSIGNINITIALINFOR = "FUNCTION_GETSIGNINITIALINFOR";
    //Lấy danh sách file ký chính
    public static final String FUNCTION_GETFILESIGNMAIN = "FUNCTION_GETFILESIGNMAIN";
    //lấy danh sách file ký đính kèm
    public static final String FUNCTION_GETFILESIGNOTHER = "FUNCTION_GETFILESIGNOTHER";
    //lấy danh sách file đính kèm từ công văn
    public static final String FUNCTION_GETFILEFROMDOC = "FUNCTION_GETFILEFROMDOC";
    //lấy danh sách cá nhân ký duyệt
    public static final String FUNCTION_GETLISTUSERSIGN = "FUNCTION_GETLISTUSERSIGN";
    //======================================================================
    //danh sách các key group trả về
    public static final String RESULT_KEYDETAILTEXT = "textViewDetail";
    public static final String RESULT_KEYFILESIGNMAIN = "fileSignMain";
    public static final String RESULT_KEYFILESIGNORTHER = "fileSignOrther";
    public static final String RESULT_KEYFILEATTACHDOC = "fileAttachDoc";
    public static final String RESULT_KEYLISTUSERSIGN = "listUserSign";
    //======================================================================
    //danh sách các trường dữ liệu trả về client
    public static final String textId = "textId";
    //tiêu đề văn bản
    public static final String title = "title";
    //nội dung văn bản
    public static final String description = "description";
    //số đăng ký
    public static final String registerNumber = "registerNumber";
    //mã hiệu văn bản
    public static final String code = "code";
    public static final String typeId = "typeId";
    public static final String stypeId = "stypeId";
    public static final String areaId = "areaId";
    public static final String state = "state";
    public static final String documentId = "documentId";
    public static final String officeSender = "officeSender";
    public static final String officePublishedId = "officePublishedId";
    public static final String officeSenderId = "officeSenderId";
    public static final String createDate = "createDate";
    public static final String creatorId = "creatorId";
    public static final String publishDate = "publishDate";
    public static final String priorityId = "priorityId";
    public static final String signLevel = "signLevel";
    public static final String isDeleted = "isDeleted";
    public static final String cancelReason = "cancelReason";
    public static final String recieverPlace = "recieverPlace";
    public static final String officePublishedName = "officePublishedName";
    public static final String sTypeName = "sTypeName";
    public static final String typeName = "typeName";
    public static final String areaName = "areaName";
    public static final String creatorName = "creatorName";
    public static final String loginName = "loginName";
    public static final String priorityName = "priorityName";
    //comment chỉ đạo ký nháy
    public static final String assignerComment = "assignerComment";
    public static final String assignerName = "assignerName";
    //file đính kèm
    public static final String attachId = "attachId";
    public static final String path = "path";
    public static final String fileSize = "fileSize";
    public static final String name = "name";
    public static final String attachment = "attachment";
    public static final String fileAttachmentId = "fileAttachmentId";
    //============== thanght6-Triển khai ngày 15/08 =================
    //===================Bổ sung thêm email trả về===================
    public static final String email = "email";
    public static final String mobile = "mobile";
    public static final String assignerEmail = "assignerEmail";
    public static final String assignerMobile = "assignerMobile";
}
