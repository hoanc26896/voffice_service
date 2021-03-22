/*
 * ResourceBundleUtils.java
 *
 * Created on September 18, 2007, 11:01 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**
 *
 * @author Vu Thi Thu Huong
 */
public class ResourceBundleUtils {

    private static final Logger logger = Logger.getLogger(ResourceBundleUtils.class);

    public static final String SPLIT_CHARACTER = ";";
    static private ResourceBundle rb = null;
    /**
     * 2012-11-29 chucvq ten file cau hinh (khong baogom phan mo rong)
     */
    private String configFileName = "config";

    public static ResourceBundleUtils getInstance(String configFileName) {
        ResourceBundleUtils instance = new ResourceBundleUtils();
        instance.setConfigFileName(configFileName);
        return instance;

    }

    /**
     * Tra ve gia tri so nguyen cua 1 key
     *
     * @param key
     * @return
     */
    public int getNumber(String key) {
        /**
         * 2012-11-29 chucvq them cau hinh
         */
        ResourceBundle rb = ResourceBundle.getBundle(configFileName);

        String smsCountStr = rb.getString(key);

        int smsCount = -1;

        try {
            smsCount = Integer.valueOf(smsCountStr).intValue();
        } catch (NumberFormatException numberFormatException) {
        }

        return smsCount;
    }

    /**
     * Lay gia tri cua 1 key
     */
    public String getValue(String key) {
        ResourceBundle rb = ResourceBundle.getBundle(configFileName);
        return rb.getString(key);
    }

    /**
     * Creates a new instance of ResourceBundleUtils
     */
    public ResourceBundleUtils() {
    }

    /**
     * @thanhnv @param key
     * @return
     */
    public static synchronized String getValueByKey(String key) {
        rb = ResourceBundle.getBundle("config");
        return rb.getString(key);
    }

    public static synchronized String getAttachmentTypeLabel(Long type) {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("attachment.label." + type.toString());
    }

    public static synchronized String getTTLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.locktt");
    }

    public static synchronized String getCTLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.lockct");
    }

    public static synchronized String getTCTLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.locktct");
    }

    public static synchronized String getTTLockTimeConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.locktimett");
    }

    public static synchronized String getCTLockTimeConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.locktimect");
    }

    public static synchronized String getTCTLockTimeConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.locktimetct");
    }

    public static synchronized String getTCTUnLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.unlocktct");
    }

    public static synchronized String getCTUnLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.unlockct");
    }

    public static synchronized String getTTUnLockConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("meeting.unlocktt");
    }

    public static synchronized String getSMSTIMEConfig() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SMSTIME");
    }

    public static synchronized int getDistanceDate() {
        rb = ResourceBundle.getBundle("config");
        return Integer.parseInt(rb.getString("date.distance"));
    }

    public static synchronized int getPageSizeOnDisplayTable() {
        rb = ResourceBundle.getBundle("config");
        return Integer.parseInt(rb.getString("displayTable.pagesize"));
    }

    public static synchronized String getApplicationResource(String key) {
        rb = ResourceBundle.getBundle("com.viettel.config.Language_vi_VN");
        return rb.getString(key);
    }

    public static synchronized String getApplicationResource(String key, String requestLocale) throws Exception {
        rb = ResourceBundle.getBundle("com.viettel.config.Language_" + requestLocale);
        return rb.getString(key);
    }

    public static synchronized String getConfigureResource(String key) {
        rb = ResourceBundle.getBundle("config");
        return rb.getString(key);
    }

    public static synchronized String getPathToFolderUpload() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("folder_upload");
    }

    public static synchronized String getPathToUpload() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("path_to_upload");
    }

    public static synchronized long getMaxLengthOfAutoNumber() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("autoNumber.maxlength"));
    }

    public static synchronized long getAutoNumberTypeOfImpReq() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("autoNumber.type.impReq"));
    }

    public static synchronized List getListByKey(String key) {
        rb = ResourceBundle.getBundle("config");
        String[] temp = rb.getString(key).split(SPLIT_CHARACTER);
        List result = new ArrayList();
        for (int i = 0; i < temp.length; i++) {
            result.add(Long.valueOf(temp[i]));
        }
        return result;
    }

    public static synchronized List getListModuleId(String key) {
        rb = ResourceBundle.getBundle("module");
        String[] temp = rb.getString(key).split(SPLIT_CHARACTER);
        List result = new ArrayList();
        for (int i = 0; i < temp.length; i++) {
            result.add(Long.valueOf(temp[i]));
        }
        return result;
    }

    public static synchronized Long getMeetingReqAttachmentType() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("attachment.type.meeting.req"));
    }

    public static synchronized Long getMeetingCloseAttachmentType() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("attachment.type.meeting.close"));
    }

    public static synchronized Long getDocReqAttachmentType() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("attachment.type.doc.req"));
    }

    /* parameter for SMS */
    public static synchronized String getSMSDefaultSessionId() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("DEFAULT_SESSION_ID");
    }

    public static synchronized String getMmInviteSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("INVITE_MEETING");
    }

    public static synchronized String getMmDeleteSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("DELETE_MEETING");
    }

    public static synchronized String getUmMutualSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("UM_MUTUAL_POINT");
    }

    public static synchronized String getWfArriveSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("WORKFLOW_ARRIVE");
    }

    public static synchronized String getWfSuspendSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("WORKFLOW_SUSPEND");
    }

    public static synchronized String getWfResumeSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("WORKFLOW_RESUME");
    }

    public static synchronized String getWfAbortSyntax() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("WORKFLOW_ABORT");
    }

    public static synchronized String getMmCompanyMail() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("COMP_MEETING_MAIL");
    }

    public static synchronized String getMmCentersMail() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("CENT_MEETING_MAIL");
    }

    public static synchronized Integer getMaxLengthSMS() {
        rb = ResourceBundle.getBundle("sms");
        return Integer.parseInt(rb.getString("MAX_SMS_LENGTH"));
    }

    public static synchronized String getMailTitle() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("MAIL_TITLE");
    }

    public static synchronized String getMailContent() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("MAIL_CONTENT");
    }

    public static synchronized String getTestTitle() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("TEST_TITLE");
    }

    public static synchronized String getSMSSender() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SENDER");
    }

    public static synchronized String getSMSServiceId() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SERVICE_ID");
    }

    public static synchronized String getSMSUser() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("ID");
    }

    public static synchronized String getSMSPass() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("PASS");
    }

    public static synchronized String getSMSStatusNotCharge() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("STATUS_NOT_CHARGE");
    }

    public static synchronized String getMessageMeetingSubjectPrefix() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("prefix.message.meeting.subject");
    }

    public static synchronized String getMeetingTimePrefix() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("prefix.message.meeting.time");
    }

    public static synchronized String getMeetingLocationPrefix() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("prefix.message.meeting.location");
    }

    public static synchronized int getMeetingTimeReq() {
        rb = ResourceBundle.getBundle("config");
        return Integer.parseInt(rb.getString("prefix.message.meeting.time.req"));
    }

    public static synchronized String getStartMeetingTime() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("start.meeting.time");
    }

    public static synchronized String getEndMeetingTime() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("end.meeting.time");
    }

    public static synchronized String getResource(String key) {
        rb = ResourceBundle.getBundle("config");
        return rb.getString(key);
    }

    public static synchronized String getCas(String key) {
        rb = ResourceBundle.getBundle("cas");
        return rb.getString(key);
    }

    /*
     * Author:      Tuanpv
     * CreateDate:  15/10/2008
     * Purpose:     Lấy ra roleID của Admin phụ trách bộ phận chăm sóc khách hàng
     */
    public static synchronized List getVaiTroNguoiDungViettelTelecom() {
        rb = ResourceBundle.getBundle("config");
        String value = rb.getString("role.user.vietteltelecom");
        String arrValue[] = value.split(",");
        List lstValue = new ArrayList();
        if (arrValue.length > 0) {
            for (int index = 0; index < arrValue.length; index++) {
                lstValue.add(Long.parseLong(arrValue[index]));
            }
        }
        return lstValue;
    }

    /*
     * Author:      Tuanpv
     * CreateDate:  15/10/2008
     * Purpose:     Lấy ra roleID của nhân viên chăm sóc khách hàng vietteltelecom
     */
    public static synchronized List getVaiTroNguoiDungChamSocKhachHangTinh() {
        rb = ResourceBundle.getBundle("config");
        String value = rb.getString("role.user.cskhtinh");
        String arrValue[] = value.split(",");
        List lstValue = new ArrayList();
        if (arrValue.length > 0) {
            for (int index = 0; index < arrValue.length; index++) {
                lstValue.add(Long.parseLong(arrValue[index]));
            }
        }
        return lstValue;
    }

    /*
     * Author:      Phongnv9
     * CreateDate:  07/07/2010
     * Purpose:     Lấy ra dia chi toi ung dung voffice
     */
    public static synchronized String getHostAddress() {
        rb = ResourceBundle.getBundle("cas_en_US");
        return rb.getString("service");
    }

    /*
     * Author:      Tuanpv
     * CreateDate:  15/10/2008
     * Purpose:     Lấy ra roleID của trưởng phòng chăm sóc khách hàng tỉnh
     */
    public static synchronized List getVaiTroGiaoDichVienCuaHang() {
        rb = ResourceBundle.getBundle("config");
        String value = rb.getString("role.user.giaodichvien");
        String arrValue[] = value.split(",");
        List lstValue = new ArrayList();
        if (arrValue.length > 0) {
            for (int index = 0; index < arrValue.length; index++) {
                lstValue.add(Long.parseLong(arrValue[index]));
            }
        }
        return lstValue;
    }

    public static synchronized String getConfigContent(String key) {
        rb = ResourceBundle.getBundle("config");
        return rb.getString(key);
    }

    public static synchronized String getVaiTroNhanVien() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.nhanvien.id");
    }

    public static synchronized long getVaiTroLanhDao() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.lanhdao.id"));
    }

    public static synchronized long getVaiTroQuanTri() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.admin.id"));
    }

    public static synchronized long getVaiTroTruongPhongCty() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.truongphongCty.id"));
    }

    public static synchronized long getTrangThaiHoanThanh() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.id"));
    }

    public static synchronized long getTrangThaiDaChuyenTiep() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.forwarded.id"));
    }

    public static synchronized long getTrangThaiDangThucHien() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.inprogress.id"));
    }

    public static synchronized long getTrangThaiDuocChapNhan() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.accepted.id"));
    }

    public static synchronized long getTrangThaiDaPheDuyet() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.dapheduyet.id"));
    }

    public static synchronized long getTrangThaiDauTien() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.first"));
    }

    public static synchronized long getTrangThaiDaThayNguoiThucHien() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.changed.id"));
    }

    public static synchronized long getFunctionID(String key) {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString(key));
    }

    public static synchronized long getTrangThaiTamDung() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.suspend.id"));
    }

    public static synchronized long getTrangThaiHuyBo() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.cancel.id"));
    }

    public static synchronized long getTrangThaiTuChoiDuyet() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("status.tuchoiduyet.id"));
    }

    public static synchronized long getRootGroup() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("group.root"));
    }

    public static synchronized long getBranchRootGroup() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("group.branchRoot"));
    }

    public static synchronized String getDefaultBranchCommentContent() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("documentInStaff.defaultBranchCommentContent");
    }

    public static synchronized long getAssignTask() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("function.listAssignTask"));
    }

    public static synchronized long getCreatedTask() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("function.listCreatedTask"));
    }

    public static synchronized long getPromoteOfStaff() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("function.listIsPromoteOfStaff"));
    }

    public static synchronized long getPromoteTask() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("function.listTaskIsPromote"));
    }

    public static synchronized long getVaiTroLanhDaoCongTy() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.lanhdaocongty.id"));
    }

    public static synchronized String[] getListEnabelForwardStatus() {

        rb = ResourceBundle.getBundle("config");
        String strStatus = rb.getString("status.enableForwardList.id");
        return strStatus.split(";");
    }

    public static synchronized String getDocumentDeleteNumber() {

        rb = ResourceBundle.getBundle("config");
        String strStatus = rb.getString("document.delete");
        return strStatus;
    }

    public static synchronized String getDocumentExpiry() {
        rb = ResourceBundle.getBundle("config");
        String strStatus = rb.getString("document.expiry");
        return strStatus;
    }

    public static synchronized long getVaiTroQuanTriVanBan() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.quantrivanban.id"));
    }

    public static synchronized String getMessageContent(int type) {
        rb = ResourceBundle.getBundle("config");
        String strContent = null;
        switch (type) {
            case 1:
                strContent = rb.getString("message.content.assign");
                break;
            case 2:
                strContent = rb.getString("message.content.document");
                break;
            case 3:
                strContent = rb.getString("message.content.refuse");
                break;
        }

        return strContent;
    }

    public static synchronized long getLoaiBiKyLuat() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("message.descipline"));
    }

    public static synchronized long getLoaiNhanCongVan() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("message.document"));
    }

    public static synchronized long getLoaiNhanCongVanByPriority() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("message.document.priority"));
    }

    public static synchronized String getKyTuVietTatLoaiCongViec() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("abbreviate.task");
    }

    public static synchronized String getKyTuVietTatLoaiCongVan() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("abbreviate.document");
    }

    public static synchronized String getKyTuVietTatLoaiKyLuat() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("abbreviate.descipline");
    }

    public static synchronized long getNumberDayOutOfDate() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("day.outOfDate.number"));
    }

    public static synchronized String getPathToFileUpload() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("file_upload");
    }

    public static synchronized String[] getExtFileConvert() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("ext_file_convert").split(";");
    }

    public static synchronized long getVaiTroPhoPhong() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.phophong"));
    }

    public static synchronized String getVaiTroDaiLy() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.daily.id");
    }

    public static synchronized long getVanThuCtyId() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("vanthu.cty.id"));
    }

    public static synchronized String getFilePath(String key) {
        rb = ResourceBundle.getBundle("fileConfig");
        return rb.getString(key);
    }

    public static synchronized int getDateLockKI() {
        rb = ResourceBundle.getBundle("config");
        return Integer.parseInt(rb.getString("date.lock.ki"));
    }

    public static synchronized String getActionInsert() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.insert"));
    }

    public static synchronized String getActionView() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.view"));
    }

    public static synchronized String getActionUpdate() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.update"));
    }

    public static synchronized String getActionDelete() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.delete"));
    }

    public static synchronized String getActionSuccess() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.success"));
    }

    public static synchronized String getActionError() {
        rb = ResourceBundle.getBundle("config");
        return String.valueOf(rb.getString("action.error"));
    }

    public static synchronized Long getTaskPersonalId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.TaskPersonal"));
    }

    public static synchronized Long getTaskGroupId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.TaskGroup"));
    }

    public static synchronized Long getScheduleMeetingId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.ScheduleMeeting"));
    }

    public static synchronized Long getTaskWfId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.TaskWf"));
    }

    public static synchronized Long getDocumentId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.Document"));
    }

    public static synchronized Long getCommunicateId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("vsaid.Comunicate"));
    }

    public static synchronized Long getMaxAttachFileSize() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("max.attachFile.size"));
    }

    public static synchronized Long getVofficeVsaId() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("voffice.vsaid"));
    }
    // UM HUNGTM

    public static synchronized String getUmCD_TC_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CD_TC_END");
    }

    public static synchronized String getUmCD_DX_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CD_DX_END");
    }

    public static synchronized String getUmCD_PD_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CD_PD_END");
    }

    public static synchronized String getUmKT_DK_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.KT_DK_END");
    }

    public static synchronized String getUmKT_DX_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.KT_DX_END");
    }

    public static synchronized String getUmKT_PD_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.KT_PD_END");
    }

    public static synchronized String getUmINNOV_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.INNOV_END");
    }

    public static synchronized String getUmCONST_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CONST_END");
    }

    public static synchronized String getUmMUTUAL_DKEndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.MUTUAL_DK_END");
    }

    public static synchronized String getUmMUTUAL_PDEndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.MUTUAL_PD_END");
    }

    public static synchronized String getUmCV_DK_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CV_DK_END");
    }

    public static synchronized String getUmCV_PD_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CV_PD_END");
    }

    public static synchronized String getUmCV_GV_EndConfig() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("um.CV_GV_END");
    }

    public static synchronized int getVofficePortVanban() {
        rb = ResourceBundle.getBundle("config");
        return Integer.parseInt(rb.getString("voffice.hostvanban.port"));
    }

    public static synchronized String getVofficeHostVanban() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("voffice.hostvanban");
    }

    public static synchronized String getVofficeUserVanban() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("voffice.hostvanban.user");
    }

    public static synchronized String getVofficePassVanban() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("voffice.hostvanban.pass");
    }

    public static synchronized String getWaitTime() {

        rb = ResourceBundle.getBundle("config");
        return rb.getString("wf.waitTime");
    }

    public static synchronized long getDeptAdminRoleId() {

        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("role.deptAdmin.id"));
    }

    public static synchronized String getListRoleId4DeptAdmin() {

        rb = ResourceBundle.getBundle("config");

//        String listStr = rb.getString("role.deptAdmin.listId");
//        StringTokenizer strToken = new StringTokenizer(listStr, ",");
//        List<Long> roleIdList = new ArrayList<Long>();
//        while(strToken.hasMoreTokens()){
//
//            roleIdList.add(Long.valueOf(strToken.nextToken()));
//        }
        return rb.getString("role.deptAdmin.listId");
    }

    /*VietHD them phan resource quan ly phien ban
     * file resource: version.properties
     */
    public static synchronized String getVersionInfoByKey(String key) {
        rb = ResourceBundle.getBundle("version");
        return rb.getString(key);
    }
    /*
     * HieuBV begin add code 10/05/2010
     * Quan tri lich hop
     */

    public static synchronized String getnvDuyetLichTuanTrungTam() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.nvduyetlichtuantrungtam.id");
    }

    public static synchronized String getnvDuyetLichTuanCongTy() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.nvduyetlichtuancongty.id");
    }

    public static synchronized String getnvDuyetLichTuanTapDoan() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.nvduyetlichtuantapdoan.id");
    }

    public static synchronized String getSMSAddLichHop() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SMS_MEETING_ADD");
    }

    public static synchronized String getSMSUpdateLichHop() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SMS_MEETING_UPDATE");
    }
    /*
     * HieuBV end add code 10/05/2010
     */

    /**
     * ChucVQ@viettel.com.vn 2010/05/24 Add code cho chuc nang "Canh bao tinh
     * trang cong viec cua phong to chuc lao dong"
     */
    /**
     * Tra ve ma SYNTAX cua canh bao tinh trng cong viec bang SMS va Email
     *
     * @return
     */
    public static synchronized String getSMSWarningTaskStatus() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("SYNTAXID_SMS_WARNING_TASK_STATUS");
    }

    /**
     * ChucVQ@viettel.com.vn 2010/06/22 Dua cau hinh mail server ra ngoai
     */
    public static synchronized String getHostMail() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("voffice.hostemail");
    }

    public static synchronized String[] getListGroupNewReportTemplate() {

        rb = ResourceBundle.getBundle("config");
        String str = rb.getString("voffice.newReportTemplate.groupId");

        return str.split(",");
    }

    /**
     * Diem chuan
     *
     * @return
     */
    public static synchronized long getStandardPoint() {

        rb = ResourceBundle.getBundle("config");
        String str = rb.getString("standardPoint");

        return Long.parseLong(str);
    }

    /**
     * Diem thuong
     *
     * @return
     */
    public static synchronized long getRewardPoint() {

        rb = ResourceBundle.getBundle("config");
        String str = rb.getString("rewardPoint");

        return Long.parseLong(str);
    }

    /**
     * Diem phat
     *
     * @return
     */
    public static synchronized long getPunishPoint() {

        rb = ResourceBundle.getBundle("config");
        String str = rb.getString("punishPoint");

        return Long.parseLong(str);
    }
    /*
     * Thoi gian cap nhat thong tin khi dang nhap
     * Tinh theo thang
     */

    public static synchronized int getTimeToUpdateInfo() {

        rb = ResourceBundle.getBundle("config");
        String str = rb.getString("timeToupdateInfo");
        return Integer.parseInt(str);
    }

    public static synchronized String getViettelCa() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("rootCACertFile");
    }

    public static synchronized String getSuccessVerifyMessage() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("verifySuccessfull");
    }

    public static synchronized String getNotSuccessVerifyMessage() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("verifyNotSuccessfull");
    }

    public static synchronized String getNotSuccessFilePass() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("verifyFileHasPass");
    }

    public static synchronized String getNotVerifyUsbCode() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("verifyUsbCode");
    }
//    linhdx

    public static synchronized String getSignServer() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("signServer");
    }

    public static synchronized String getSignServerMulti() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("signMultiServer");
    }

    // DatVQ
    public static synchronized String getSignServers() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("checkSignedServer");
    }

    public static synchronized String getTextPrepareSign() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("TEXT_PREPARE_SIGN");
    }

    public static synchronized String getTextNotSigned() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("TEXT_NOT_SIGNED");
    }

    public static synchronized String getTextSigned() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("TEXT_SIGNED");
    }

    public static synchronized String getVanKeyStore() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("vanKeyStore");
    }

    public static synchronized String getVanKeyStorePass() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("vanKeyStorePass");
    }

    /**
     * Lay so luong user lon nhat co the selected dc Neu ko co cau hinh thi lay
     * gia tri mac dinh 3000
     */
    public static synchronized Long getMaxUserCanSelect() {
        String maxUserS = ResourceBundleUtils.getValueByKey("soluong.nguoidung.lonnhat.duocchon");
        Long maxUser = null;
        try {
            maxUser = Long.valueOf(maxUserS);
        } catch (Exception ex) {
            logger.error(ex);
            maxUser = 3000L;
        }
        return maxUser;

    }

    //HanhNQ21 2012-08-05 them file size up load file so lieu tong hop
    public static synchronized String getSummaryDataFileUploadSize() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("summaryDataFileUploadSize");
    }

    public static synchronized String getSummaryDataGroupIsMessage() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("summaryData.GroupIdMessage");
    }

    //HanhNQ21 2012-08-05 them file size up load file so lieu tong hop
    public static synchronized Long getMaxAttachFileSizeSummaryData() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("summaryData.max.attachFile.size"));
    }

    public static synchronized Long getLowerYearFinanceSummaryData() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("financeSummaryData.LowerYear"));
    }
    //End HanhNQ21 2012-08-05 them file size up load file so lieu tong hop

    //Hiendv 2012-08-12 them file size up load file gui file
    public static synchronized Long getMaxFileSizeSend() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("sendFile.max.attachFile.size"));
    }
    //Hiendv 2012-08-12 them file size up load file gui file

    public static synchronized String getServerletUrl() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("serverlet.url");
    }

    public static synchronized String getServerletDownFileUrl() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("serverlet.dowloadFile.url");
    }

    public static synchronized String getServerletSendFileUrl() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("serverlet.sendFile.url");
    }

    /**
     * Lay so luong user lon nhat co the selected dc Neu ko co cau hinh thi lay
     * gia tri mac dinh 20
     */
    public static synchronized Long getMaxUserSelectSendFile() {
        String maxUserS = ResourceBundleUtils.getValueByKey("maxUser.sendfile.select");
        Long maxUser = null;
        try {
            maxUser = Long.valueOf(maxUserS);
        } catch (Exception ex) {
            logger.error(ex);
            maxUser = 20L;
        }
        return maxUser;

    }

    //Hiendv 20-11-2012 them cau hinh role KI lanh dao
    public static synchronized String getVaiTroLanhdaoKI() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("role.ki.lanhdao");
    }

    public static synchronized Long getMaxCapTrinhKiFileSize() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("captrinhki.maxFile"));
    }

    public static synchronized Long getMaxTotalCapTrinhKiFileSize() {
        rb = ResourceBundle.getBundle("config");
        return Long.valueOf(rb.getString("captrinhki.maxTotalFile"));
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    //hanhnq21 110614 lay thong tin loai storage
    public static synchronized String getStorageName() {
        String storageName = null;
        rb = ResourceBundle.getBundle("config");
        try {
            storageName = rb.getString("storageName");
        } catch (Exception ex) {
            logger.error(ex);
        }
        return storageName;
    }

    public static synchronized String getTextAffterSign() {
        rb = ResourceBundle.getBundle("sms");
        return rb.getString("TEXT_AFFTER_SIGNED");
    }

    public static synchronized long getRootGroupVTT() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("group.vtt"));
    }

    public static synchronized long getRootGroupChiNhanhVTT() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("group.cn.vtt"));
    }

    public static synchronized long getIdGroupBGDTD() {
        rb = ResourceBundle.getBundle("config");
        return Long.parseLong(rb.getString("group.id.bgdtd"));
    }

    public static synchronized String getLstIdGroupBGDTD() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("group.list.id.bgdtd");
    }

    public static synchronized String getLstUserGroupBGDTD() {
        rb = ResourceBundle.getBundle("config");
        return rb.getString("group.list.user.bgdtd");
    }
}
