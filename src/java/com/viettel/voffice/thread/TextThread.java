/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.thread;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.dao.document.TextSearchDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.meeting.MeetingWeekDAO;
import com.viettel.voffice.database.dao.staff.UserRoleDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.dao.text.TextReceiverGroupDAO;
import com.viettel.voffice.database.dao.text.TextSignDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityUserRole;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.text.EntityFilesCommentSign;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextReceiverGroup;
import com.viettel.voffice.database.entity.text.EntityTextRejectBefor;
import com.viettel.voffice.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Thread van ban
 *
 * @author thanght6
 * @since May 10, 2016
 */
public class TextThread {

    /**
     * Thread dem so luong van ban ky trang home
     */
    public static class ThreadCountTextByStatus extends Thread {

        // Log file
        private static final Logger LOGGER = Logger.getLogger(ThreadCountTextByStatus.class);
        
        private int count = 0;
        private final TextSearchDAO textSearchDAO;
        //chuoi gia tri tra ve client
        private String keyResultName;
        Boolean isArchival;
        private final Long userIdVof1;
        private final Long userIdVof2;
        private int financial;
        private int type;
        private final String keyword;
        private final String description;
        private final String registerNumber;
        private final String code;
        private final String title;
        private final int typeId;
        private final int areaId;
        private int status;
        private final String fromDate;
        private final String toDate;
        private final List<Long> lstGroupLeaderVof1 = new ArrayList<>();
        private final List<Long> lstGroupLeaderVof2 = new ArrayList<>();
        private final List<Long> lstGroupOnlyLeader1 = new ArrayList<>();
        private final List<Long> lstGroupOnlyLeader2 = new ArrayList<>();
        private final List<Long> lstGroupSecretaryVof1 = new ArrayList<>();
        private final List<Long> lstGroupSecretaryVof2 = new ArrayList<>();
        private final Long startRecord;
        private final Long pageSize;
        private final Long filterType;
        private final String isSearchAll;
        private final Long requisitionId;
        
        public ThreadCountTextByStatus(Long userIdVof1, Long userIdVof2, int isFinancial,
                int type, String keyword, String registerNumber, String code, String title,
                String description, int typeId, int areaId, int state, String fromDate,
                String toDate, List<Long> lstGroupLeaderVof1, List<Long> lstGroupLeaderVof2,
                List<Long> lstGroupOnlyLeader1, List<Long> lstGroupOnlyLeader2,
                List<Long> lstGroupSecretaryVof1, List<Long> lstGroupSecretaryVof2,
                Long startRecord, Long pageSize, Long filterType, String isSearchAll,
                TextSearchDAO textSearchDAO, Long requisitionId) {
            
            this.userIdVof1 = userIdVof1;
            this.userIdVof2 = userIdVof2;
            this.financial = isFinancial;
            this.type = type;
            this.keyword = keyword;
            this.registerNumber = registerNumber;
            this.code = code;
            this.title = title;
            this.description = description;
            this.typeId = typeId;
            this.areaId = areaId;
            this.status = state;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.lstGroupLeaderVof1.addAll(lstGroupLeaderVof1);
            this.lstGroupLeaderVof2.addAll(lstGroupLeaderVof2);
            this.lstGroupOnlyLeader1.addAll(lstGroupOnlyLeader1);
            this.lstGroupOnlyLeader2.addAll(lstGroupOnlyLeader2);
            this.lstGroupSecretaryVof1.addAll(lstGroupSecretaryVof1);
            this.lstGroupSecretaryVof2.addAll(lstGroupSecretaryVof2);
            this.startRecord = startRecord;
            this.pageSize = pageSize;
            this.filterType = filterType;
            this.isSearchAll = isSearchAll;
            this.textSearchDAO = textSearchDAO;
            // tuanld 
            this.requisitionId = requisitionId;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public String getKeyResultName() {
            return keyResultName;
        }
        
        public int getType() {
            return type;
        }
        
        public void setType(int type) {
            this.type = type;
        }
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        public int getFinancial() {
            return financial;
        }
        
        public void setFinancial(int financial) {
            this.financial = financial;
        }
        
        @Override
        public void run() {
            try {

                // Dem so luong van ban
                Object countText = textSearchDAO.searchText(userIdVof1, userIdVof2, "1",
                        financial, type, keyword, registerNumber, code,
                        title, description, typeId, areaId, status,
                        fromDate, toDate, lstGroupLeaderVof1, lstGroupLeaderVof2,
                        lstGroupOnlyLeader1, lstGroupOnlyLeader2, lstGroupSecretaryVof1,
                        lstGroupSecretaryVof2, startRecord, pageSize, filterType, isSearchAll,
                        requisitionId, null);
                // 16:19 05/11/2016
                // Fix loi logic do Sonar quet
//                if (countText == null) {
//                    count = 0;
//                }
//                try {
//                    count = Integer.parseInt(countText.toString());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    count = -1;
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                count = -1;
//            }
                if (countText != null) {
                    try {
                        count = Integer.parseInt(countText.toString());
                    } catch (Exception ex) {
                        count = -1;
                        LOGGER.error("countText: " + countText + " - Exception!", ex);
                    }
                } else {
                    count = 0;
                }
            } catch (Exception ex) {
                count = -1;
                LOGGER.error("countText (Dem so luong van ban) - userIdVof1: "
                        + userIdVof1 + " - Exception!", ex);
            }
        }
        
        public void stopThred() {
            this.stopThred();
        }
    }

    /**
     * Thread chi tiet van ban
     */
    public static class TextDetailThread extends Thread {
        
        private Long textId;
        private EntityText text;
        private Long userIdVof2;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public EntityText getText() {
            return text;
        }
        
        public void setText(EntityText text) {
            this.text = text;
        }
        
        public TextDetailThread(Long textId,Long sysUserIdVof2) {
            this.textId = textId;
            this.userIdVof2 = sysUserIdVof2;
        }
        
        @Override
        public void run() {
            TextDAO textDao = new TextDAO();
            text = textDao.getTextViewDetail(userIdVof2,textId);
        }
    }

    /**
     * lay file ky chinh
     */
    public static class TextAttachFileSignThread extends Thread {
        
        private Long textId;
        private List<EntityFileAttachment> lstAttach;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public List<EntityFileAttachment> getLstAttach() {
            return lstAttach;
        }
        
        public void setLstAttach(List<EntityFileAttachment> lstAttach) {
            this.lstAttach = lstAttach;
        }
        
        public TextAttachFileSignThread(Long textId) {
            this.textId = textId;
        }
        
        @Override
        public void run() {
            AttachDAO attachDAO = new AttachDAO();
            lstAttach = attachDAO.getListMainSigningFile(textId);
        }
    }

    /**
     * lay file dinh kem
     */
    public static class TextAttachFileOtherThread extends Thread {
        
        private Long textId;
        private Long userId;
        private List<EntityFileAttachment> lstAttach;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public List<EntityFileAttachment> getLstAttach() {
            return lstAttach;
        }
        
        public void setLstAttach(List<EntityFileAttachment> lstAttach) {
            this.lstAttach = lstAttach;
        }
        
        public TextAttachFileOtherThread(Long textId, Long userId) {
            this.textId = textId;
            this.userId = userId;
        }
        
        @Override
        public void run() {
            TextDAO textDao = new TextDAO();
            lstAttach = textDao.getTextAttachSignOrther(null, textId, userId);
        }
    }

    /**
     * lay file dinh kem
     */
    public static class TextAttachFromDocumentThread extends Thread {
        
        private Long textId;
        private Long userId;
        private String isVersionNew;
        private List<EntityText> lstText;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getIsVersionNew() {
            return isVersionNew;
        }
        
        public void setIsVersionNew(String isVersionNew) {
            this.isVersionNew = isVersionNew;
        }
        
        public List<EntityText> getLstText() {
            return lstText;
        }
        
        public void setLstText(List<EntityText> lstText) {
            this.lstText = lstText;
        }
        
        public TextAttachFromDocumentThread(Long textId, Long userId, String isVersionNew) {
            this.textId = textId;
            this.userId = userId;
            this.isVersionNew = isVersionNew;
        }
        
        @Override
        public void run() {
            TextDAO textDao = new TextDAO();
            lstText = textDao.getTextListAttachFromDocument(null, textId, userId, isVersionNew);
        }
    }

    /**
     * lay thong tin nguoi ky
     */
    public static class TextSignerThread extends Thread {
        
        private Long textId;
        private EntityText text;
        private Long sysUserIdVof2;
        private String type;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public EntityText getText() {
            return text;
        }
        
        public void setText(EntityText text) {
            this.text = text;
        }
        
        public Long getSysUserIdVof2() {
            return sysUserIdVof2;
        }
        
        public void setSysUserIdVof2(Long sysUserIdVof2) {
            this.sysUserIdVof2 = sysUserIdVof2;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public TextSignerThread(Long textId, Long sysUserIdVof2, String type) {
            this.textId = textId;
            this.sysUserIdVof2 = sysUserIdVof2;
            this.type = type;
        }
        
        @Override
        public void run() {
            EntityText result = new EntityText();
            TextDAO textDao = new TextDAO();
            if (type != null && "0".equals(type)) {
                //Neu la xem chi tiet tren man hinh trinh ky web
                List<EntityText> lstSign = textDao.getTextListUserSignByAssign(textId, sysUserIdVof2, false,
                        false, null);
                //120516 them thong tin ten don vi ban hanh tren vhr
                if (lstSign != null && lstSign.size() > 0) {
                    //lay thong tin trinh ky tren web2
                    EntityText textTmp;
                    Long isCreateSignWeb2 = 1L;//la trinh ky tren con 2
                    String signatureType;
                    for (EntityText lstSign1 : lstSign) {
                        textTmp = lstSign1;
                        signatureType = textTmp.getSignatureType();
                        if (signatureType != null
                                && signatureType.equals(String.valueOf(Constants.Text.State.APPROVED))
                                && (textTmp.getIsCreateSignWeb2() == null || textTmp.getIsCreateSignWeb2().equals(0L))) {
                            //neu 1 nguoi ky chinh tren con 1 thi danh dau trinh ky tren con 1
                            isCreateSignWeb2 = 0L;
                            break;
                        }
                    }
                    result.setIsCreateSignWeb2(isCreateSignWeb2);
                    if (isCreateSignWeb2.equals(1L)) {
                        //neu la trinh ky tren con 2 
                        //thi moi lay them thong tin don vi tren VHR
                        textDao.updateOgrVhrNamr(lstSign);
                    }
                }
                // Aug 28, 2017 Kiem tra xem nguoi ky co bi thay doi don vi khong
                UserRoleDAO userRoleDAO = new UserRoleDAO();
                userRoleDAO.checkRoleByUser(lstSign);
                textDao.updateOgrVhrNamr(lstSign);
                result.setListSubmitter(lstSign);
                List<EntityText> lstFlashSign = textDao.getTextListUserSign(textId, sysUserIdVof2, null);
                // Lay files dinh kem khi ky ::Begin::cuongnv
                if (!CommonUtils.isEmpty(lstFlashSign)) {
                    setFilesCommentSign(lstFlashSign);//lay danh sach file co comment
                }
                // End
                result.setListInnitialSigner(textDao.convertListSignType(lstFlashSign, 3));
            } else {
                // Xem chi tiet tren man hinh mobile
                List<EntityText> lstSign = textDao.getTextListUserSign(textId, sysUserIdVof2, null);
                setFilesCommentSign(lstSign);//lay danh sach file co comment
                textDao.updateOgrVhrNamr(lstSign);
                UserRoleDAO userRoleDAO = new UserRoleDAO();
                userRoleDAO.checkRoleByUser(lstSign);
                result.setListSubmitter(textDao.convertListSignTypeNew(lstSign, 1));
                result.setListReviewer(textDao.convertListSignTypeNew(lstSign, 2));
                result.setListInnitialSigner(textDao.convertListSignTypeNew(lstSign, 3));
                // End
            }
            // thanght6 - 17/06/2016
            // Cap nhat trang thai cho phep doi hay khong
            // doi voi tung nguoi ky trong danh sach ky chinh
            textDao.updateStatusForReplaceSigner(result.getListSubmitter());
            this.text = result;
        }

        /**
         * lay thong tin file ky co comment
         *
         * @param lstSign
         */
        void setFilesCommentSign(List<EntityText> lstSign) {
            if (lstSign != null && lstSign.size() > 0) {
                List<Long> lstTextProcessId = new ArrayList<>();//danh sach textProcessId
                for (EntityText etext : lstSign) {
                    if (etext.getTextProcessId() != null) {
                        lstTextProcessId.add(Long.parseLong(etext.getTextProcessId()));
                    }
                }
                if (lstTextProcessId.size() > 0) {
                    //lay thong tin file co comment
                    TextSignDAO textSignDAO = new TextSignDAO();
                    List<EntityFilesCommentSign> lstFile1 = textSignDAO.getFilesSignByListTextProcessId(lstTextProcessId);
                    //cap nhat danh sach file cho tung nguoi
                    if (lstFile1 != null && lstFile1.size() > 0) {
                        Long textProcessId;
                        for (EntityText etext : lstSign) {
                            List<EntityFilesCommentSign> lstFileByProcess = new ArrayList<>();
                            if (etext.getTextProcessId() != null) {
                                textProcessId = Long.parseLong(etext.getTextProcessId());
                                //xu ly lay file ky cho tung nguoi
                                for (EntityFilesCommentSign file1 : lstFile1) {
                                    if (file1.getTextProcessId() != null
                                            && file1.getTextProcessId().equals(textProcessId)) {
                                        lstFileByProcess.add(file1);
                                    }
                                }
                            }
                            etext.setLstFilesCommentSign(lstFileByProcess);
                        }
                    } else {
                        for (EntityText etext : lstSign) {
                            List<EntityFilesCommentSign> lstFileByProcess = new ArrayList<>();
                            etext.setLstFilesCommentSign(lstFileByProcess);
                        }
                    }
                }
            }
        }
    }

    /**
     * lay danh sach ky nhay
     */
    public static class TextDrafSignerThread extends Thread {
        
        private Long textId;
        private EntityText text;
        private Long sysUserIdVof1;
        private Long sysUserIdVof2;
        private String isSearchAll;
        private EntityUserGroup userGroup;
        
        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public EntityText getText() {
            return text;
        }
        
        public void setText(EntityText text) {
            this.text = text;
        }
        
        public Long getSysUserIdVof1() {
            return sysUserIdVof1;
        }
        
        public void setSysUserIdVof1(Long sysUserIdVof1) {
            this.sysUserIdVof1 = sysUserIdVof1;
        }
        
        public Long getSysUserIdVof2() {
            return sysUserIdVof2;
        }
        
        public void setSysUserIdVof2(Long sysUserIdVof2) {
            this.sysUserIdVof2 = sysUserIdVof2;
        }
        
        public String getIsSearchAll() {
            return isSearchAll;
        }
        
        public void setIsSearchAll(String isSearchAll) {
            this.isSearchAll = isSearchAll;
        }
        
        public EntityUserGroup getUserGroup() {
            return userGroup;
        }

        public void setUserGroup(EntityUserGroup userGroup) {
            this.userGroup = userGroup;
        }

        public TextDrafSignerThread(Long textId, Long sysUserIdVof1, Long sysUserIdVof2,
                String isSearchAll, EntityUserGroup userGroup) {
            this.textId = textId;
            this.sysUserIdVof1 = sysUserIdVof1;
            this.sysUserIdVof2 = sysUserIdVof2;
            this.isSearchAll = isSearchAll;
            this.userGroup = userGroup;
        }
        
        @Override
        public void run() {
            
            EntityText result = new EntityText();
            // Lay danh sach ca nhan chuyen tu dong
            TextDAO textDao = new TextDAO();
            result.setLstStaffSend(textDao.getListStaffSend(textId));
            // Lay thong tin nguoi chuyen ky nhay
            EntityStaff assigner = textDao.getTextSignInitialInfor(textId, sysUserIdVof1,
                    sysUserIdVof2, isSearchAll);
            if (assigner != null) {
                result.setAssignerName(assigner.getFullName());
                result.setAssignerEmail(assigner.getEmail());
                result.setAssignerMobile(assigner.getMobile());
                result.setAssignerComment(assigner.getCommentContent());
            }
            this.text = result;
            // Lay danh sach don vi, nhom ca nhan chuyen tu dong
            TextReceiverGroupDAO textReceiverGroupDAO = new TextReceiverGroupDAO();
            List<EntityTextReceiverGroup> listTextReceiverGroup = textReceiverGroupDAO
                    .getTextReceiverGroupList(sysUserIdVof2, textId);
            result.setListTextReceiverGroup(listTextReceiverGroup);
            // Lay danh sach lich hop
            MeetingWeekDAO meetingWeekDAO = new MeetingWeekDAO();
            // datdc them tham so
            // ko anh huong o ham nay
            List<MeetingApproveResult> listMeeting = meetingWeekDAO.getMeetingListByText(
                    userGroup, null, null, null, null, null, null, textId, null, null);
            
            // Datdc add them order cho list meeting original
            // De ghep list voi meeting other tren web
            if (!CommonUtils.isEmpty(listMeeting)) {
                int orderNumber = 0;
                for (MeetingApproveResult entity : listMeeting) {
                    orderNumber ++;
                    entity.setOrderMeetOther(Long.valueOf(orderNumber));
                }
            }
            if (null != textId) {
                listMeeting.addAll(meetingWeekDAO.getListMeetingOther(textId));
            }
            // Datdc add them list meeting orther
            result.setListMeeting(listMeeting);
        }
    }

    /**
     * datnv5: lay lich su tu choi ky
     */
    public static class TextGetHistoryRejectText extends Thread {
        
        private Long textId;
        private Long empLoyeeId;
        List<EntityTextRejectBefor> listReject;

        public Long getTextId() {
            return textId;
        }
        
        public void setTextId(Long textId) {
            this.textId = textId;
        }
        
        public TextGetHistoryRejectText(Long textId, Long employeeId) {
            this.textId = textId;
            this.empLoyeeId = employeeId;
        }
        
        public List<EntityTextRejectBefor> getListReject() {
            return listReject;
        }
        
        public void setListReject(List<EntityTextRejectBefor> listReject) {
            this.listReject = listReject;
        }
        
        @Override
        public void run() {
            TextDAO textDao = new TextDAO();
            this.listReject = new ArrayList<>();
            Long textIdGet = textId;
            while (true) {                
                EntityTextRejectBefor item = textDao.getGetHistoryRejectText(
                        textIdGet, this.empLoyeeId);
                if (item != null) {
                    if (item.getState().trim().length() > 0) {
                        listReject.add(item);
                    }
                    textIdGet = item.getTextId();
                } else {
                    textIdGet = null;
                }
                if (textIdGet == null) {
                    break;
                }
            }
        }
    }

    /**
     * datnv5: lay lich su tu choi ky
     */
    public static class UpdateTextListUserReject extends Thread {

        private final Long textId;

        public UpdateTextListUserReject(Long textId) {
            this.textId = textId;
        }
        
        @Override
        public void run() {
            TextDAO textDao = new TextDAO();
            List<Long> listUserReject = textDao.getListUserRejectText(textId);
            if (listUserReject != null && listUserReject.size() > 0) {
                textDao.updateListUserSignRejectBefor(textId, listUserReject);
            }
        }
    }
//    public static void main(String[] args) {
//         TextDAO textDao = new TextDAO();
//        List<Object> listReject = new ArrayList<>();
//            Long textIdGet = 94990L;
//         while (true) {                
//                EntityTextRejectBefor item = textDao.getGetHistoryRejectText(
//                        textIdGet,495224L);
//                if(item != null){
//                    listReject.add(item);
//                    textIdGet = item.getTextId();
//                }else{
//                    textIdGet = null;
//                }
//                if(textIdGet==null){
//                    break;
//                }
//            }
//    }
}
