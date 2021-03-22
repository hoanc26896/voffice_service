/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.thread;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import vn.viettel.core.sign.utils.Constant;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.database.dao.SystemParameterDAO;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO1;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.document.AutoDigitalSignDAO;
import com.viettel.voffice.database.dao.document.DocOrgRepublishDAO;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.dao.file.AttachDAO;
import com.viettel.voffice.database.dao.sms.SmsDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.text.TextCommonDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.dao.text.TextSignDAO;
import com.viettel.voffice.database.entity.EntityDocOrgRepublish;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntitySystemParameter;
import com.viettel.voffice.database.entity.EntityTextMark;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.GroupMapping;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.text.EntityMutiSms;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextProcess;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;
import com.viettel.voffice.utils.MessageUtils;

/**
 *
 * @author datnv5
 */
public class ThreadExcuteAfterSigned implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ThreadExcuteAfterSigned.class);
    String signType;
    Boolean isSignParallel;
    Long textId;
    Long userVofice2;
    Long userIdVof1;
    EntityText textCommon;
    String comment;
    String currentDate;
    Vof2_EntityUser userVof2;
    List<EntityText> listStaff;
    List<EntityMutiSms> lstMutiSms;
    Boolean isLastSigner;
    EntityText itemText;

    public ThreadExcuteAfterSigned(String signType, Boolean isSignParallel,
            Long textId, Long userVofice2, Long userIdVof1, EntityText text, String comment,
            String currentDate, Vof2_EntityUser userVof2, List<EntityText> listStaff,
            List<EntityMutiSms> lstMutiSms, Boolean isLastSigner, EntityText itemText) {
        this.signType = signType;
        this.isSignParallel = isSignParallel;
        this.textId = textId;
        this.userVofice2 = userVofice2;
        this.userIdVof1 = userIdVof1;
        this.textCommon = text;
        this.comment = comment;
        this.currentDate = currentDate;
        this.userVof2 = userVof2;
        this.listStaff = listStaff;
        this.lstMutiSms = lstMutiSms;
        this.isLastSigner = isLastSigner;
        this.itemText = itemText;
    }

    @Override
    public void run() {
        ExcuteFollowAfterSigned();
    }

    /**
     * thuc hien cong bo ban hanh tu dong sau khi ky xong
     */
    private void ExcuteFollowAfterSigned() {
        CommonControler smsDAO = new CommonControler();
        CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
        // tim xem vai tro cua userId doi voi van ban
        // neu la nguoi tham gia thi chi can cap nhat lai state cua nguoi do
        int isLevelEnd = 2;
        switch (signType) {
            case "2":
                //neu la ky nhay
                if (isSignParallel) {
                    //neu la ky song song thi gui tin nhan luon
                    StringBuilder querySms = new StringBuilder();
                    List<Object> paramSms = new ArrayList<>();
                    querySms.append("select tp.sender_id senderId,tp.assigner_id_vof2 assignerIdVof2 from text_process tp ");
                    querySms.append("where tp.text_id = ? and tp.signature_type = 2");
                    querySms.append(" and (tp.emp_vhr_id = ? ");
                    paramSms.add(textId);
                    paramSms.add(userVofice2);
                    if (userIdVof1 > 0) {
                        querySms.append("or tp.chief_id = ? ");
                        paramSms.add(userIdVof1);

                    }
                    querySms.append(")");
                    List<EntityText> listtextSms = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(querySms,
                            paramSms, null, null, EntityText.class);
                    if (listtextSms != null && listtextSms.size() > 0) {
                        EntityText itemUser = listtextSms.get(0);
                        if (itemUser.getAssignerIdVof2() != null) {
                            //Thuc hien gui tin nhan tren he thong 2
                            smsDAO.sentMessToTextSignVof2(textCommon.getTitle(),
                                    userVofice2, itemUser.getAssignerIdVof2(),
                                    Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT,
                                    comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                    Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                        }
                    }
                }
                break;
            case "1":
            case "3":
                if (isSignParallel) {
                    //neu la ky song song
                    //cap nhat state va parallelLevet bang text va gui tin nha
                    isLevelEnd = updateTextSignParallelLevel(textId, userIdVof1,
                            userVofice2, currentDate, comment, textCommon);
                } else {
                    //neu la ky luong cu
                    //cap nhat state va level bang text
                    isLevelEnd = updateTextSignLevel(textId, userIdVof1,
                            userVofice2, currentDate, comment);

                }
                //<editor-fold defaultstate="collapsed" desc="datnv5:bo sung check neu la ky cuoi thi thuc hien ban hanh tu dong neu co;date:19/01/2016">
                if (isLevelEnd == 2) {
                    //090416 tra ve ket qua khi ky duyet thanh cong
                    AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                    autoDigitalSignDAO.sendResultMutiSignText(textId, AutoDigitalSignDAO.AUTO_SIGN_SUCCESS,
                            AutoDigitalSignDAO.SIGN_ONE_VO20, userVof2.getStrEmail());

                    //Hiendv bo sung lay dung user va don vi ban hanh
                    Long userPublishIdVof2 = null;
                    Long orgPublishIdVof2 = null;
                    Long userPublishIdVof1 = null;
                    Long orgPublishIdVof1 = null;
                    //Bo sung them cho Outsource  Id nguoi trinh
                    Long createdIdVof2 = textCommon.getCreatorIdVof2();
                    StringBuilder query = new StringBuilder();
                    query.append("select tp.receiver_id receivedId, tp.chief_id signerId, t.SIGN_WITH_COMPANY signWithCompany,")
                            .append("tp.emp_vhr_id empVhrId, t.OFFICE_PUBLISHED_ID_VOF2 orgVhrId, t.title ")
                            .append(" from text t ")
                            .append("left join text_process tp ")
                            .append(" on t.text_id=tp.text_id  ")
                            .append(" and t.OFFICE_PUBLISHED_ID_VOF2=tp.ORG_VHR_ID")
                            .append(" where  t.text_id = ?  ")
                            .append(" order by tp.SIGN_LEVEL desc,tp.signature_type desc");
                    List<Object> param = new ArrayList<>();
                    param.add(textId);
                    listStaff = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(query, param, null, null, EntityText.class);
                    if (listStaff != null && listStaff.size() > 0) {
                        if (listStaff.get(0).getEmpVhrId() != null
                                && listStaff.get(0).getEmpVhrId() > 0) {
                            //Neu ton tai Id nguoi ky cua don vi banh hanh
                            orgPublishIdVof2 = listStaff.get(0).getOrgVhrId();
                            userPublishIdVof2 = listStaff.get(0).getEmpVhrId();
                        } else {
                            //Gan Id don vi ban hanh
                            orgPublishIdVof2 = listStaff.get(0).getOrgVhrId();
                        }
                        if (listStaff.get(0).getReceivedId() != null) {
                            orgPublishIdVof1 = Long.parseLong(listStaff.get(0).getReceivedId());
                            userPublishIdVof1 = Long.parseLong(listStaff.get(0).getSignerId());
                        }
                        // 201812-Pitagon: add
                        itemText.setSignWithCompany(listStaff.get(0).getSignWithCompany());
                        itemText.setTitle(listStaff.get(0).getTitle());
                    }
                    //ban hanh tu dong
                    Long documentId = promulgateTextAuto(textId, userPublishIdVof1,
                            orgPublishIdVof1, userPublishIdVof2, orgPublishIdVof2);
                    // update du lieu bang doc_org_republish sau khi ky:: OUtsourceTeam/SOnnd
                    if (documentId != null && documentId > 0L) {
                        LOGGER.error("Thuc hien update van ban lien ke documentId:=" + documentId);
                        if (updateDatabaseRepublishAfterSign(documentId, textCommon)) {
                            //gui van ban den cac don vi lien quan sau khi thuc hien ban hanh tu dong
                            //author OutSourceTeam/sonnd
                            // 8-7-2016
                            // lay uservof1 neu suervof1 khac 0 hoac null
                            // lay danh sach va chuyen van ban den cac don vi
                            List<EntityDocOrgRepublish> dor = getListGroupToSend(documentId);
                            if (dor != null && dor.size() > 0 && createdIdVof2 != null) {
                                DocumentDAO docDao = new DocumentDAO();
                                List<GroupMapping> lst = new ArrayList<>();
                                int sizeOfDor = dor.size();
                                for (int i = 0; i < sizeOfDor; i++) {
                                    GroupMapping mapping = new GroupMapping();
                                    mapping.setGroupVof2(dor.get(i).getSysOrganizationId());
                                    lst.add(mapping);
                                }
                                //Lay thong tin nguoi trinh
                                UserDAO userDao = new UserDAO();
                                Vof2_EntityUser userCreatedVof2 = userDao.vof2_getUserInforVHRById(createdIdVof2);
                                if (userCreatedVof2 != null) {
                                    // Gui van ban cho tat ca cac don vi
                                    docDao.sendDocumentToGroup(documentId,
                                            lst, "", null, null, userCreatedVof2, null, null, null);
                                    // Gui sms cho thu truong, lanh dao, van thu sau khi thuc hien 
                                    DocOrgRepublishDAO docOrgDao = new DocOrgRepublishDAO();
                                    List<Long> lstOrganization = new ArrayList<>();
                                    for (EntityDocOrgRepublish doc : dor) {
                                        lstOrganization.add(doc.getSysOrganizationId());
                                    }
                                    docOrgDao.sendSMSAfterSign(lstOrganization, userCreatedVof2.getUserId());
                                }

                            }
                            // ket thuc chinh sua cong bo tu dong
                        }
                    }
                    // 201812-Pitagon: add nghiep vu xin dau
                    if (itemText.getSignWithCompany() != null && itemText.getSignWithCompany().intValue() == 1) {
                    	TextDAO textDao = new TextDAO();
                    	if (textDao.requireMark(textId)) {
                    	    List<EntityTextMark> orgMarkedList = textDao.getOrgMarkedList(textId, 1, true);
                        	if (!CommonUtils.isEmpty(orgMarkedList)) {
                        	    List<Long> orgIds = new ArrayList<>();
                        	    for (EntityTextMark entity : orgMarkedList) {
                        	        orgIds.add(entity.getOrgId());
                        	    }
                        	    
                        	    UserDAO userDao = new UserDAO();
                        	    List<EntityVhrEmployee> users = userDao.getUserDocumentManagerOrg(orgIds);
                        	    if (!CommonUtils.isEmpty(users)) {
                        	        for (EntityVhrEmployee entity : users) {
                        	            smsDAO.sentSMS(itemText.getTitle(), createdIdVof2, entity.getEmployeeId(),
                        	                    null, Constants.SMS_TEXT_CONFIG.ASK_FOR_REAL, "", 101L);
                        	        }
                        	    }
                        	}
                    	}
                    }
                }
                //</editor-fold>
                break;
        }
        if (!isSignParallel) {
            //neu la luong ky cu
            //vinhnq13_sms gui tin nhan khi ky thanh cong
            processSendSmsSignText(textId, signType, userIdVof1, userVof2,
                    lstMutiSms, comment, isLastSigner, itemText, isLevelEnd);
        }

    }

    /**
     * cap nhat level //, state bang text
     *
     * @param textId
     * @param userIdVof1
     * @param userIdVof2
     * @param currDate
     * @param comment
     * @param text
     * @return
     */
    private int updateTextSignParallelLevel(Long textId, Long userIdVof1,
            Long userIdVof2, String currDate, String comment, EntityText text) {
        CommonControler smsDAO = new CommonControler();
        int res = 0;
        if (textId == null || textId <= 0) {
            return res;
        }
        try {
            Long textLevelCur = text.getSignParallel();
            String title = text.getTitle();
            Long creatorIdVof2 = text.getCreatorIdVof2();
            CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
            CommonDataBaseDaoVO2 cmdVof2 = new CommonDataBaseDaoVO2();
            //lay level ky tiep theo
            Long textLevelUpdate = getTextSignParallelLevelInProcess(textId, cmd, textLevelCur);
            Boolean isUpdateSignNextOK = false;//trang thai cap nhat level nguoi ky tiep theo
            if (textLevelUpdate != null && textLevelUpdate == Constants.TextProcess.State.TEXT_SIGN_ALL) {
                // Neu textLevelUpdate = -1 thi co nghia tat ca deu da ki
                //Cap nhat level ky cua bang Text: level =leve +1;
                if (textLevelCur != null) {
                    //Cap nhat level cua bang text
                    if (updateParallelLevelText(textId, textLevelCur + 1,
                            (long) Constant.TEXT_PROCESS_DEFINE.TEXT_STATE_APPROVED, cmd)) {
                        //Gui tin nhan khi nguoi cuoi cung thuc hien ky
                        //kiem tra co phai la phieu xuat nhap kho khong
                        AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                        if (!autoDigitalSignDAO.checkTextTypeXNK(textId)) {
                            //neu khong phai la phieu xuat nhap kho thi moi gui tin nhan
                            sendSmsToSignerAll(textId, userIdVof1, userIdVof2,
                                    title, comment, cmd);
                        }
                        res = 2;
                        isUpdateSignNextOK = true;
                    }
                }
            } else {
                //neu khong phai la ky xong tat ca
                if (textLevelCur != null && textLevelUpdate != null
                        && textLevelUpdate > -1L) {
                    if (!textLevelCur.equals(textLevelUpdate)) {
                        //neu level la level moi
                        //update level cua bang text
                        if (updateParallelLevelText(textId, textLevelUpdate, null, cmd)) {
                            //Thuc hien cap nhat actionDate cua nguoi ky tiep theo
                            HashMap<String, Object> hmParam = new HashMap<>();
                            StringBuilder sqlUpdateActionDate = new StringBuilder();
                            sqlUpdateActionDate.append(" update text_process set action_date = to_date(:currDate,'dd/MM/yyyy hh24:mi:ss')")
                                    .append(" where text_id =:textId and SIGN_LEVEL_PARALLEL =:signLevel and state!=:state ");
                            hmParam.put("signLevel", textLevelUpdate);
                            hmParam.put("textId", textId);
                            hmParam.put("currDate", currDate);
                            hmParam.put("state", (long) Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER);
                            if (cmd.insertOrUpdateDataBase(sqlUpdateActionDate, hmParam)) {
                                //cap nhat nguoi ky song song tiep theo va gui tin nhan
                                StringBuilder querySms = new StringBuilder();
                                querySms.append("select t.title title, t.creator_id_vof2 creatorId, ");
                                querySms.append(" tp.emp_vhr_id signerIdVO1, tp.org_vhr_id receivedId, ");
                                querySms.append(" tp.review_new_level isVtReviewNew, ");
                                querySms.append(" t.SIGN_LEVEL_PARALLEL signParallel, ");
                                querySms.append(" tp.SIGN_LEVEL_PARALLEL parallelSignLevel, tp.state state ");
                                querySms.append(" from text t left join text_process tp on ");
                                querySms.append(" t.text_id = tp.text_id where t.text_id = ? and tp.SIGN_LEVEL_PARALLEL = ? ");
                                querySms.append(" and (tp.state = 0 or tp.state = 3) and tp.signature_type <> 2");
                                List<Object> paramSms = new ArrayList<>();
                                paramSms.add(textId);
                                paramSms.add(textLevelUpdate);
                                List<EntityText> listResult = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(querySms, paramSms, null, null, EntityText.class);
                                DocumentSignDAO documentSignDAO = new DocumentSignDAO();
                                documentSignDAO.updateNextSignParallelAndSendSms(listResult, textId, cmdVof2, true);
                                isUpdateSignNextOK = true;
                            }
                        }
                    } else {
                        //neu khong thay doi level
                        isUpdateSignNextOK = true;
                    }
                }
            }
            if (isUpdateSignNextOK) {
                //neu cap nhat nguoi tiep theo thanh cong
                //cap nhat trang thai bang next_sign_parallel
                DocumentSignDAO documentSignDAO = new DocumentSignDAO();
                int result = documentSignDAO.updateSignParallelNext(textId, userIdVof2,
                        (long) Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER, null);
                //gui tin nhan
                if (result == 1) {
                    //gui tin nhan cho nguoi tao
                    if (creatorIdVof2 != null && creatorIdVof2 > 0) {
                        smsDAO.sentMessToTextSignVof2(title, userIdVof2, creatorIdVof2,
                                Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                    }
                    if (res == 2) {
                        //neu la ky cuoi gui tin nhan cho van thu bao co cong van ban hanh
                        AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                        if (userIdVof2 != null && !autoDigitalSignDAO.checkTextTypeXNK(textId)) {
                            //neu la nguoi ky cuoi ky xong
                            //khong phai la phieu xnk
                            //gui tin nhan cho van thu ban hanh thu cong
                            autoDigitalSignDAO.sendSmsToSecretorPublishOrg(textId, userIdVof2);
                        }
                    } else {
                        List<Long> userIds = new ArrayList<>();
                        //gui tin nhan cho nguoi ky truoc do
                        AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                        List<EntityTextProcess> lstTextProcess = autoDigitalSignDAO.getListChiefSignedText(textId, userIdVof1, userIdVof2);
                        if (lstTextProcess != null && lstTextProcess.size() > 0) {
                            int sizeLstProcess = lstTextProcess.size();
//                            Long chiefId;
                            for (int i = 0; i < sizeLstProcess; i++) {
                                //neu co cau hinh nhan tin nhan
                                smsDAO.sentMessToTextSignVof2(title, userIdVof2,
                                        lstTextProcess.get(i).getEmpVhrId(),
                                        Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE,
                                        textId,
                                        Constants.SMS_TEXT_INTERCEPT.SIGNER_SMSBEFORSIGN);
                                userIds.add(lstTextProcess.get(i).getEmpVhrId());
//                                chiefId = lstTextProcess.get(i).getChiefId();
//                                if (chiefId != null && chiefId > 0) {
//                                    if (autoDigitalSignDAO.isReceiveSmsAfterSignNotFinal(chiefId)) {
//                                       
//                                    }
//                                }
                            }
                        }
                        // Lay danh sach nguoi ky song song gui tin nhan
                        List<EntityTextProcess> listUser = autoDigitalSignDAO.getListParallel(textId, userIdVof2);
                        if (!CommonUtils.isEmpty(listUser)) {
                            for (int i = 0; i < listUser.size(); i++) {
                                if (userIds.contains(listUser.get(i).getEmpVhrId())) {
                                    continue;
                                }
                                //neu co cau hinh nhan tin nhan
                                smsDAO.sentMessToTextSignVof2(title, userIdVof2,
                                        listUser.get(i).getEmpVhrId(),
                                        Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE,
                                        textId,
                                        Constants.SMS_TEXT_INTERCEPT.SIGNER_SMSBEFORSIGN);
                            }
                        }
                        
                        if (res == 2) {
                        } else {
                            //datnv5: bo sung chuyen trang thai nguoi ky nhay sang trang thai nguoi ky chinh da ky
                            TextSignDAO textSignDAO = new TextSignDAO();
                            EntityUserGroup userGroup = new EntityUserGroup();
                            userGroup.setVof2_ItemEntityUser(userVof2);
                            List<Long> listIdTextSign = new ArrayList<>();
                            listIdTextSign.add(textId);
                            Boolean resultCheckDeny = textSignDAO.updateInitialsignToDenyWhenMainsign(userGroup, listIdTextSign);
                            if (resultCheckDeny) {
                                //gui tin nhan cho nguoi ky nhay biet ky chinh da ky
                                addSendMessageInitialSignWait(userGroup, listIdTextSign);
                            }
                        }
                    }
                }
            }
        } catch (Exception en) {
            LOGGER.error(en);
            res = 0;
        }
        return res;
    }

    /**
     * lay thong tin levlel ky song next
     *
     * @param textId
     * @param cmd
     * @param signLevelCur
     * @return
     * @throws Exception
     */
    private Long getTextSignParallelLevelInProcess(
            Long textId, CommonDataBaseDaoVO1 cmd, Long signLevelCur) throws Exception {
        Long level = -2L;
        if (textId == null || textId <= 0) {
            return level;
        }
        try {
            List<Object> params = new ArrayList<>();
            StringBuilder sqlTextProcess = new StringBuilder();
            sqlTextProcess.append(" select a.SIGN_LEVEL_PARALLEL parallelSignLevel, a.state state ");
            sqlTextProcess.append(" from text_process a where a.text_id = ? ");
            sqlTextProcess.append(" and a.signature_type = 3 ");
            sqlTextProcess.append(" order by a.SIGN_LEVEL_PARALLEL desc, a.signature_type desc ");
            params.add(textId);
            List<EntityTextProcess> lstTextProcess = (List<EntityTextProcess>) cmd.excuteSqlGetListObjOnCondition(sqlTextProcess, params,
                    null, null, EntityTextProcess.class);
            if (lstTextProcess == null || lstTextProcess.isEmpty()) {
                return 0L;
            }
            final int size = lstTextProcess.size();
            EntityTextProcess temp;
            Long state = lstTextProcess.get(0).getState();
            if (state == Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER) {
                //neu nguoi cuoi cung ky duyet xong
                level = (long) Constants.TextProcess.State.TEXT_SIGN_ALL;
            } else {
                Boolean isLevelSignAll = false;//trang thai ky cua level
                if (signLevelCur != null) {
                    for (int i = 0; i < size; i++) {
                        temp = lstTextProcess.get(i);
                        if (temp.getParallelSignLevel() != null && signLevelCur.equals(temp.getParallelSignLevel())) {
                            //kt nguoi ky level hien tai da ky xong
                            if (temp.getState() == Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER) {
                                //da ky xong
                                isLevelSignAll = true;
                            } else {
                                //chua ky xong
                                isLevelSignAll = false;
                                break;
                            }
                        }
                    }
                    level = signLevelCur;
                    if (isLevelSignAll) {
                        //neu tat ca nguoi cung level ky xong
                        level = signLevelCur + 1;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return level;
    }

    /**
     * <b>Ham cap nhat thu tu nguoi ky trong bang textProcess</b><br>
     *
     * @param textId Id van ban trinh ky
     * @param userIdVof1
     * @param userIdVof2
     * @param currDate
     * @param comment
     * @return Trang thai cap nhat: 1 - Thanh cong, 0 - That bai
     */
    private int updateTextSignLevel(Long textId, Long userIdVof1,
            Long userIdVof2, String currDate, String comment) {

        int res = 0;
        if (textId == null || textId <= 0) {
            res = 0;
        }

        try {
            CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();

            EntityText text = getTextById(textId, cmd);
            Long textLevel = getTextSignLevelInProcess(textId, cmd);
            Long state = 1L;
            if (text.getSignLevel() != null && textLevel != null && textLevel > -1L) {
                if (!text.getSignLevel().equals(String.valueOf(textLevel))) {
                    //vinhnq13 update level cua bang text
                    updateLevelText(textId, textLevel, state, cmd);
//                    System.out.println("Thuc hien cap nhat level ky cua nguoi tiep theo text_id, textLevel  :"
//                            + textId.toString() + "---" + textLevel);
                    //Thuc hien cap nhat actionDate cua nguoi ky thiep theo
                    HashMap<String, Object> hmParam = new HashMap<>();
                    StringBuilder sqlUpdateActionDate = new StringBuilder();
                    sqlUpdateActionDate.append(" update text_process set action_date = to_date(:currDate,'dd/MM/yyyy hh24:mi:ss')")
                            .append(" where text_id =:textId and sign_level =:signLevel and state!=:state ");
                    hmParam.put("signLevel", textLevel);
                    hmParam.put("textId", textId);
                    hmParam.put("currDate", currDate);
                    hmParam.put("state", (long) Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER);
                    cmd.insertOrUpdateDataBase(sqlUpdateActionDate, hmParam);
                }

            } else {

                // Neu textLevel = -1 thi co nghia tat ca deu da ki
                if (textLevel == -1L) {
                    state = 3L;
                    //Cap nhat level ky cua bang Text: level =leve +1;
                    if (text.getSignLevel() != null) {
                        textLevel = Long.parseLong(text.getSignLevel()) + 1;
                    } else {
                        textLevel = 1L;
                    }
                    //Cap nhat level cua bang text
                    updateLevelText(textId, textLevel, state, cmd);
                    //Gui tin nhan khi nguoi cuoi cung thuc hien ky
//                    System.out.println("Thuc hien gui tin nhan khi nguoi cuoi cung ky");
                    //kiem tra co phai la phieu xuat nhap kho khong
                    AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                    if (!autoDigitalSignDAO.checkTextTypeXNK(textId)) {
                        //neu khong phai la phieu xuat nhap kho thi moi gui tin nha
                        sendSmsToSignerAll(textId, userIdVof1, userIdVof2,
                                text.getTitle(), comment, cmd);
                    }
                    res = 2;
                } else {
                    //Cap nhat level cua bang text
                    updateLevelText(textId, textLevel, state, cmd);
                }
            }
            //

        } catch (Exception en) {
            LOGGER.error("updateTextSignLevel", en);
            res = 0;
        }

        return res;
    }

    //<editor-fold defaultstate="collapsed" desc="coder:datnv5, cac ham thuc hien quy trinh ban hanh, chuyen tu dong">
    /**
     * <b>Ban hanh van ban tu dong</b><br>
     *
     * @author DatNV5
     * @param textId
     * @param v1UserId Id nguoi ban hanh tren 1.0
     * @param v1OrgId Id don vi cua nguoi baxn hanh tren 1.0
     * @param v2UserId Id nguoi ban hanh tren 2.0
     * @param v2OrgId Id don vi cua nguoi ban hanh tren 2.0
     * @return Tra ve trang thai ban hanh thanh cong hay khong
     */
    private Long promulgateTextAuto(Long textId,
            Long v1UserId, Long v1OrgId,
            Long v2UserId, Long v2OrgId) {
        Long result = 0L;
        if (v2OrgId == null || v2OrgId <= 0L) {
            //Neu khong ton tai don vi ban hanh thi khong thuc hien ban hanh tu dong
            return result;
        }
        TextDAO textDAO = new TextDAO();
        AttachDAO attachDAO = new AttachDAO();
        CommonControler smsDAO = new CommonControler();
        //<editor-fold defaultstate="collapsed" desc="lay chi tiet van ban trinh ky">
        EntityDocument entityDocument = new EntityDocument();
        EntityText entityText = textDAO.getTextViewDetail(v2UserId,textId);
        //</editor-fold>
        if (entityText != null && entityText.getAUTO_PROMULGATE_TEXT() != null
                && entityText.getAUTO_PROMULGATE_TEXT().equals(1L)) {
            //<editor-fold defaultstate="collapsed" desc="lay file dinh kem">
//            List<Long> lstAttachOrther = new ArrayList<>();
//            List<EntityFileAttachment> listFileMain = attachDAO.getListMainSigningFile(textId);
//            if (listFileMain != null && listFileMain.size() > 0) {
//                lstAttachOrther.add(listFileMain.get(0).getFileAttachmentId());
//            }
//            List<EntityFileAttachment> listFileOther = textDAO.getTextAttachSignOrther(
//                    null, textId, v2UserId);
//            if (listFileOther != null && listFileOther.size() > 0) {
//                for (int i = 0; i < listFileOther.size(); i++) {
//                    lstAttachOrther.add(listFileOther.get(i).getFileAttachmentId());
//                }
//            }
            //</editor-fold>
            //sua goi lay so dang ky
            TextCommonDAO textCommonDAO = new TextCommonDAO();
            Long type = (entityText.getTypeId() != null) ? Long.valueOf(entityText.getTypeId()) : -1L;
            //bo sung them lay theo do mat
            Long stypeId = (entityText.getSecurityCode() != null) ? Long.valueOf(entityText.getSecurityCode()) : null;
            String typeConfig = null;
            SystemParameterDAO systemParameterDAO = new SystemParameterDAO();
            EntitySystemParameter systemParameter = systemParameterDAO.getConfigValueByCode("TYPE_ID_TO_AUTOPUBLISH");
            if (systemParameter != null
                    && !CommonUtils.isEmpty(systemParameter.getValue())) {
                typeConfig = systemParameter.getValue();

            }
            Long registerNumber = textCommonDAO.getNumberPromulgateNew(textId,
                    entityText.getOfficePublishedId(), type, stypeId, null, typeConfig);

//            System.out.println("Get number Auto register of OfficePublishedId "
//                    + entityText.getOfficePublishedId() + " --- " + registerNumber.toString());
            entityDocument.setDocumentId(entityText.getTextId());
            entityDocument.setOfficeSender(entityText.getDepartSentSignFullPathVof2());
            entityDocument.setOfficeSenderId(entityText.getOfficeSenderId());
            entityDocument.setRegisterNumber(registerNumber.toString());
            if (typeConfig != null && typeConfig.contains(Constants.Common.COMMA_CHAR
                    + type.toString() + Constants.Common.COMMA_CHAR)) {
                entityDocument.setCode(entityText.getCode() + "." + registerNumber.toString());
            } else {
                entityDocument.setCode(registerNumber.toString() + "/" + entityText.getCode());
            }

            entityDocument.setTitle(entityText.getTitle());
            entityDocument.setPromulgateDate(DateUtils.date2ddMMyyyyString(
                    DateUtils.sysDate()));
            entityDocument.setReceiveDate(DateUtils.date2ddMMyyyyString(
                    DateUtils.sysDate()));
            //thuc hien lay nguoi ky cuoi theo van ban
            TextCommonDAO textCommonDAO1 = new TextCommonDAO();

            List<EntityTextProcess> lstUser = textCommonDAO1.getListSignerFinalByListTextId(textId);
            if (lstUser != null && lstUser.size() > 0) {
                entityDocument.setSigner(lstUser.get(0).getChiefName());
            }
            Long stype;
            try {
                stype = Long.parseLong(entityText.getSecurityCode());
            } catch (NumberFormatException e) {
                stype = 0L;
                LOGGER.error(e.getMessage(), e);
            }
            entityDocument.setStypeId(stype);
            Date tomorrow = new Date(DateUtils.sysDate().getTime() + (1000 * 60 * 60 * 24 * 3));
            entityDocument.setDeadlineDate(DateUtils.date2ddMMyyyyString(tomorrow));
            //Fix loi khi ban hanh tu dong khong cap nhat ngay chuyen tien voi van ban tai chinh
//            entityDocument.setTransferDate(DateUtils.date2ddMMyyyyString(
//                    DateUtils.sysDate()));
            entityDocument.setSignedOnPaperDate(DateUtils.date2ddMMyyyyString(
                    DateUtils.sysDate()));

            // Luu thong tin nguoi tao va don vi nguoi tao theo nguoi ban hanh           
            // Neu don vi ban hanh khong co trong luong ky
            if (v2UserId == null || v2UserId <= 0L) {
                //Thuc hien lay van thu don vi de ban hanh
                List<EntityUser> listSerectary = textDAO.getListSecretaryByGroupId(null, v2OrgId);
                if (listSerectary != null && listSerectary.size() > 0) {
                    //Gan van thu mac dinh la nguoi ban hanh
                    EntityUser textProcess = listSerectary.get(0);
//                    entityDocument.setCreatorId(textProcess.getUserId());
//                    entityDocument.setCreatorGroupId(v1OrgId);
                    entityDocument.setCreatorId2(textProcess.getUserId());
                    entityDocument.setCreatorGroupId2(v2OrgId);
                } else {
                    //Ko co van thu thi Check lay lanh dao don vi de ban hanh
                    List<EntityUser> lstLeader = textDAO.getListLeaderByGroupId(v2OrgId);
                    if (lstLeader != null && lstLeader.size() > 0) {
                        EntityUser textProcess = lstLeader.get(0);
//                        entityDocument.setCreatorId(textProcess.getUserId());
//                        entityDocument.setCreatorGroupId(v1OrgId);
                        entityDocument.setCreatorId2(textProcess.getUserId());
                        entityDocument.setCreatorGroupId2(v2OrgId);
                    } else {
                        //Neu ko ton tai ca van thu va thu truong/lanh dao thi ko thuc hien ban hanh tu dong
                        return result;
                    }
                }
            } else {
                //Neu ton tai don vi banh hanh trong luong ky thuc hien lay van thu hoac lanh dao ban hanh
                List<EntityTextProcess> lstUserSecrectary = getListUserRoleSecrectary(v1OrgId, textId, v2OrgId);
                if (lstUserSecrectary != null && lstUserSecrectary.size() > 0) {
                    //Gan van thu mac dinh la nguoi ban hanh
                    EntityTextProcess textProcess = lstUserSecrectary.get(0);
                    entityDocument.setCreatorId(textProcess.getChiefId());
                    entityDocument.setCreatorGroupId(textProcess.getReceivedId());
                    entityDocument.setCreatorId2(textProcess.getEmpVhrId());
                    entityDocument.setCreatorGroupId2(textProcess.getOrgVhrId());
                } else {
                    //Gan mac dinh user chon ban hanh la nguoi ban hanh
                    entityDocument.setCreatorId(v1UserId);
                    entityDocument.setCreatorGroupId(v1OrgId);
                    entityDocument.setCreatorId2(v2UserId);
                    entityDocument.setCreatorGroupId2(v2OrgId);
                }
            }

            entityDocument.setProcessType(0L);
            entityDocument.setTypeId(Long.parseLong(entityText.getTypeId()));
            entityDocument.setIsArrive(0); //Mac dinh luon la van ban di
            entityDocument.setPriorityId(Long.parseLong(entityText.getUrgencyCode()));
            entityDocument.setContent(entityText.getContent());
            entityDocument.setAreaId(entityText.getAreaId());
            entityDocument.setIsForwardAgent(0);
            entityDocument.setIsFromCoporation(1);
            try {
                //Hiendv bo sung tham so thiet lap tu dong cong bo =false , do khong phai la ban hanh thu cong thiet lap tu dong cong bo
                result = textDAO.updateDocumentPromulgate(entityDocument.getCreatorId(), v1OrgId,
                        null, textId, entityDocument, true,
                        entityDocument.getCreatorId2(), entityDocument.getCreatorGroupId2());

                //vinhnq13_sms gui sms khi ban hanh tu dong thanh cong
                if (result != null && result > 0L) {
                    List<EntityText> listtextSms = getInfoText(textId);
                    if (listtextSms != null && listtextSms.size() > 0) {
                        EntityText itemUser = listtextSms.get(0);
                        Long creatorId;
                        String textTitle = itemUser.getTitle();
                        if (itemUser.getCreatorIdVof2() != null && itemUser.getCreatorIdVof2() > 0) {
//                            System.out.println("Gui tin nhan thong bao ban hanh tu dong cho nguoi trinh tren 2.0");
                            creatorId = itemUser.getCreatorIdVof2();
                            smsDAO.sentMessToTextSignVof2(textTitle,
                                    entityDocument.getCreatorId2(), creatorId,
                                    Constants.SMS_TEXT_CONFIG.AUTO_PUBLIC_DOC,
                                    entityText.getPromulgatingDepart(),
                                    MessageUtils.TEXT_LINK_TYPE, textId,
                                    Constants.SMS_TEXT_INTERCEPT.CREATOR_PROMULGATE);
                        } else {
//                            System.out.println("Gui tin nhan thong bao ban hanh tu dong cho nguoi trinh tren 1.0");
                            creatorId = Long.parseLong(itemUser.getCreator());
                            smsDAO.sentMessToTextSign(textTitle, entityDocument.getCreatorId(),
                                    creatorId, Constants.SMS_TEXT_CONFIG.AUTO_PUBLIC_DOC,
                                    "", MessageUtils.TEXT_LINK_TYPE, textId);
                        }

                        // 10/05/2016 Gui tin nhan cho van thu cua don vi ban hanh van ban tu dong
                        if ((v1OrgId != null)
                                && (v1UserId != null || v2UserId != null)
                                && !CommonUtils.isEmpty(textTitle)) {
                            textDAO.sendSmsForVtAutoPubText(v1UserId, v2UserId,
                                    v1OrgId, v2OrgId, textTitle,
                                    entityText.getPromulgatingDepart(), textId);
                        }
                    }
                }

                //tu dong chuyen van ban
//                if (entityText.getAUTO_SEND_TEXT() == 1L && strResult.equals("SUCCESS")) {
//                    sendTextAuto(textId, sysUserId);
//                }
            } catch (Exception e) {
                FunctionCommon.writeLog(1, e);
            }
            return result;
        }

        return result;
    }

    /**
     * <b>Updated truong documentid cho cac don vi can ban hanh lai van ban lien
     * ke</b><br>
     *
     * @author OutsourceTeam/SonND
     * @param documentId ID cong van duoc lay tu bang text sau khi cong bo tu
     * dong
     * @param textId Id van ban trinh ky
     * @return
     */
    private boolean updateDatabaseRepublishAfterSign(Long documentId, EntityText text) {

        try {
            // kiem tra xem van ban dang ky co phai la van ban lien ke khong
//            TextDAO textDAO = new TextDAO();
//            boolean isNearBy = textDAO.checkTextNearby(text.getTextId());

            // kiem tra xem no co la van ban goc hay khong
            DocOrgRepublishDAO docDao = new DocOrgRepublishDAO();
            List<EntityVhrOrg> rangeAdd = docDao.getOganizationAdded(text.getTextId(), null);

            CommonDataBaseDaoVO1 cmdVo1 = new CommonDataBaseDaoVO1();
            StringBuilder query = new StringBuilder();
            List<Object> params = new ArrayList<>();
            if (text.getIsLienKe() != null && text.getIsLienKe() > 0) {
                // thuc hien them republish_document_ID,republish_status,republish_date
                // vao truong co republish_text_ID = text_Id
                query.append("UPDATE DOC_ORG_REPUBLISH ");
                query.append("SET REPUBLISHED_DOCUMENT_ID = ?");
                query.append(", REPUBLISHED_STATUS = ?");
                query.append(", REPUBLISHED_DATE = TO_DATE(?, 'dd/MM/yyyy HH24:MI:ss')");
                query.append(" where REPUBLISH_TEXT_ID = ?");
                params.add(documentId);
                Date dateSign = new Date();
                SimpleDateFormat formatte = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                params.add(1);
                params.add(formatte.format(dateSign));
                params.add(text.getTextId());
                cmdVo1.insertOrUpdateDataBase(query, params);
            }
            if (!CommonUtils.isEmpty(rangeAdd)) {
                LOGGER.error("thuc hien them document_id cho ban ghi co text_id :=" + text.getTextId());
                // thuc hien them document_id cho ban ghi co text_id = text_id
                ArrayList<HashMap<String, Object>> para2 = new ArrayList<>();
                query = new StringBuilder();
                query.append("UPDATE DOC_ORG_REPUBLISH SET ");
                query.append("DOCUMENT_ID = :documentId ");
                query.append("WHERE TEXT_ID = :textId");
                query.append("and SYS_ORGANIZATION_ID = :sysId");
                params.clear();
                int numberElement = rangeAdd.size();
                for (int i = 0; i < numberElement; i++) {
                    HashMap<String, Object> temp = new HashMap<>();
                    temp.put("documentId", documentId);
                    temp.put("textId", text.getTextId());
                    temp.put("sysId", rangeAdd.get(i).getSysOrganizationId());
                    para2.add(temp);
                }
                cmdVo1.insertDataBaseBloc(query, para2);
                LOGGER.error("query :=" + query + " param: " + para2);
            }
            return true;

        } catch (Exception ex) {
            LOGGER.error("updateDatabaseRepublishAfterSign (Updated truong "
                    + "documentid cho cac don vi can ban hanh lai van ban lien ke)"
                    + " - Exception!", ex);
        }
        return false;
    }

    /**
     * <b>Lay danh sach cac don vi de chuyen van ban khi ban hanh tu dong sau
     * khi ky cuoi</b><br>
     *
     * @author OutSourceTeam/SonND
     * @since Jul 8, 2016
     * @param documentId
     * @param listOrganization
     * @return
     */
    private List<EntityDocOrgRepublish> getListGroupToSend(Long documentId) {
        List<EntityDocOrgRepublish> listOrganization = null;
        try {
            CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
            StringBuilder query = new StringBuilder();
            query.append("select SYS_ORGANIZATION_ID sysOrganizationId from DOC_ORG_REPUBLISH where DOCUMENT_ID = ?");
            List params = new ArrayList();
            params.add(documentId);
            listOrganization = (List<EntityDocOrgRepublish>) cmd.excuteSqlGetListObjOnCondition(query, params, null, null, EntityDocOrgRepublish.class);

        } catch (Exception ex) {
            LOGGER.error("getListGroupToSend (Lay danh sach cac don vi de chuyen"
                    + " van ban khi ban hanh tu dong sau khi ky cuoi) - Exception!", ex);
        }
        return listOrganization;
    }

    /**
     * <b>Gui tin nhan sau khi ky duyet van ban</b><br>
     *
     * @param textId
     * @param signType
     * @param userId
     * @param lstMutiSms
     * @param comment
     * @param isLastSigner
     * @param itemText
     * @param isLevelEnd
     */
    private void processSendSmsSignText(Long textId, String signType,
            Long userIdVof1, Vof2_EntityUser userVof2,
            List<EntityMutiSms> lstMutiSms, String comment, Boolean isLastSigner,
            EntityText itemText, int isLevelEnd) {
        CommonControler smsDAO = new CommonControler();
        try {
            //vinhnq13_sms gui tin nhan khi ky thanh cong
            //Map Id nguoi ky qua db VO2
            Long userIdVof2 = userVof2.getUserId();
            List<EntityText> entityText = getInfoText(textId);
            String textTitle = "";//Tieu de van ban
            Long creatorId = null; //Id nguoi trinh ky
            if (entityText != null && entityText.size() > 0) {
                textTitle = entityText.get(0).getTitle();
                if (entityText.get(0).getCreatorIdVof2() != null) {
                    creatorId = entityText.get(0).getCreatorIdVof2();
                } else {
                    creatorId = Long.parseLong(entityText.get(0).getCreator());
                }

            }
            AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
            //kiem tra co phai la phieu xuat nhap kho khong
            boolean isEiText = autoDigitalSignDAO.checkTextTypeXNK(textId);
            if ("2".equals(signType)) {
                //1.Truong hop ky nhay
                StringBuilder querySms = new StringBuilder();
                List<Object> paramSms = new ArrayList<>();
                querySms.append("select tp.sender_id senderId,tp.assigner_id_vof2 assignerIdVof2 from text_process tp ");
                querySms.append("where tp.text_id = ? and tp.signature_type = 2");
                querySms.append(" and (tp.emp_vhr_id = ? ");
                paramSms.add(textId);
                paramSms.add(userVof2.getUserId());
                if (userIdVof1 != null && userIdVof1 > 0) {
                    querySms.append("or tp.chief_id = ? ");
                    paramSms.add(userIdVof1);

                }
                querySms.append(")");

                CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
                List<EntityText> listtextSms = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(querySms, paramSms, null, null, EntityText.class);
                if (listtextSms != null && listtextSms.size() > 0) {
                    EntityText itemUser = listtextSms.get(0);
                    if (itemUser.getAssignerIdVof2() != null) {
                        //Thuc hien gui tin nhan tren he thong 2
                        //kiem tra chan tin nhan tu nguoi ky nhay cho nguoi ky chinh
                        smsDAO.sentMessToTextSignVof2(textTitle, userIdVof2,
                                itemUser.getAssignerIdVof2(),
                                Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT,
                                comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                    } else {
                        //Thuc hien gui tin nhan tren he thong 1
                        smsDAO.sentMessToTextSign(textTitle, userIdVof1,
                                itemUser.getSenderId(),
                                Constants.SMS_TEXT_CONFIG.LEADER_FLASH_SIGN_TEXT,
                                comment, MessageUtils.TEXT_LINK_TYPE, textId);
                    }
                }
            } else {
                int lstMutiSmsSize = lstMutiSms.size();
                List<Long> lstUserReceivedSms = new ArrayList<>(); //danh sach nguoi da nhan tin nhan da ky duyet
                //TH ky chinh
                //1. Gui sms cho nguoi trinh ky
                if (creatorId != null) {
                    if (lstMutiSmsSize > 0) {
//                        System.out.println("Van ban ky nhieu lstMutiSmsSize: " + lstMutiSmsSize);
                        //neu la ky nhieu
                        boolean isSendMutiSms = false;
                        EntityMutiSms mutiSmsTmp;
                        EntityMutiSms mutiSmsTmpOK = null;
                        for (int k = 0; k < lstMutiSmsSize; k++) {
                            mutiSmsTmp = lstMutiSms.get(k);
                            if (((mutiSmsTmp.getCreatorId() != null && mutiSmsTmp.getCreatorId().equals(creatorId))
                                    || (mutiSmsTmp.getCreatorIdVof2() != null && mutiSmsTmp.getCreatorIdVof2().equals(creatorId)))
                                    && (mutiSmsTmp.getCountCreator() > 1L)) {
                                isSendMutiSms = true;
                                mutiSmsTmpOK = mutiSmsTmp;
                            }
                        }
                        if (isSendMutiSms) {
                            if (mutiSmsTmpOK != null && mutiSmsTmpOK.getIsSend() == null) {
//                                System.out.println("Thuc hien gui tin nhan da ky nhieu");
                                //gui tin nhan gop
                                if (entityText.get(0).getCreatorIdVof2() != null
                                        && entityText.get(0).getCreatorIdVof2() > 0) {
                                    smsDAO.sentMessToTextSignVof2(
                                            mutiSmsTmpOK.getCountCreator().toString(),
                                            userIdVof2, creatorId,
                                            Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_SMS,
                                            comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                            Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                                } else if (entityText.get(0).getCreatorId() != null) {
                                    smsDAO.sentMessToTextSign(mutiSmsTmpOK.getCountCreator().toString(),
                                            userIdVof1, creatorId,
                                            Constants.SMS_TEXT_CONFIG.MESSAGE_LANGUAGE_MUTI_SMS,
                                            comment, MessageUtils.TEXT_LINK_TYPE, textId);
                                }
                                //Danh dau lai la da gui de cac lan sau ko thuc hien gui nua
                                mutiSmsTmpOK.setIsSend(Integer.valueOf(1));
                            }
                        } else {
//                            System.out.println("Khong thao man dieu kien ky nhieu,Thuc hien gui tin nhan ky don le");
                            //gui tin nhan 1 van ban toi nguoi trinh ky
                            if (entityText.get(0).getCreatorIdVof2() != null
                                    && entityText.get(0).getCreatorIdVof2() > 0) {
                                smsDAO.sentMessToTextSignVof2(textTitle, userIdVof2,
                                        creatorId, Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                        Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                            } else if (entityText.get(0).getCreatorId() != null) {
                                smsDAO.sentMessToTextSign(textTitle, userIdVof1,
                                        creatorId, Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE, textId);
                            }

                        }
                    } // Ky 1 van ban
                    else {
//                        System.out.println("Gui tin nhan khong phai la ky nhieu");
                        //neu la ky don --> Thuc hien gui tin nhan da ky cho nguoi trinh ky
                        if (entityText.get(0).getCreatorIdVof2() != null
                                && entityText.get(0).getCreatorIdVof2() > 0) {
//                            System.out.println("Thuc hien gui tin nhan da ky don le tren 2.0");
                            smsDAO.sentMessToTextSignVof2(textTitle, userIdVof2,
                                    creatorId, Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                    comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                    Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                            lstUserReceivedSms.add(creatorId);
                        } else if (entityText.get(0).getCreatorId() != null) {
//                            System.out.println("Thuc hien gui tin nhan da ky don le tren 1.0");
                            smsDAO.sentMessToTextSign(textTitle, userIdVof1,
                                    creatorId, Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                    comment, MessageUtils.TEXT_LINK_TYPE, textId);
                        }
                    }
                }

                //Neu ko phai la nguoi ky cuoi va trang thai nguoi tiep theo la chua ky =0
//                System.out.println("Kiem tra trang thai da la nguoi ky cuoi chua: " + isLastSigner);
                if (!isLastSigner) {
                    if (itemText != null
                            && itemText.getState().equals(String.valueOf(Constant.TEXT_PROCESS_DEFINE.TEXT_PROCESS_STATE_NEW))) {
                        //2. Gui sms cho van thu nguoi ky tiep theo (neu co)
//                        StringBuilder querySms = new StringBuilder();
//                        querySms.append("select tp.review_new_level isVtReviewNew ");
//                        querySms.append(" from text_process tp where tp.text_id = ? ");
//                        querySms.append("and tp.state = 0 and tp.signature_type =3 order by tp.sign_level asc");
//                        List<Object> paramSms = new ArrayList<>();
//                        paramSms.add(textId);
//                        CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
//                        List<EntityText> listResult = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(querySms, paramSms, null, null, EntityText.class);
//                        boolean isSendVT = false;
//                        if (listResult != null && listResult.size() > 0) {
//                            EntityText entity = listResult.get(0);
//                            //Neu review_new_level bang 1 hoac 0 thi gui sms cho van thu, neu bang 2 thi gui sms truc tiep cho ng ky.
//                            if (itemText.getIsVtReviewNew() != null && !itemText.getIsVtReviewNew().equals(2L)) {
//                                isSendVT = true;
//                            }
//                        }
                        //Neu review_new_level bang 1 hoac 0 thi gui sms cho van thu, neu bang 2 thi gui sms truc tiep cho ng ky.
                        boolean isSendVT = false;
                        if (itemText.getIsVtReviewNew() != null && !itemText.getIsVtReviewNew().equals(2L)) {
                            isSendVT = true;
                        }
                        if (isSendVT) {
//                            System.out.println("Nguoi ky tiep theo co van thu");
                            //gui tin nhan cho van thu

                            TextDAO textDao = new TextDAO();
                            Long receiverId = null;
                            if (itemText.getReceivedId() != null) {
                                receiverId = Long.parseLong(itemText.getReceivedId());
                            }
//                          Lay danh sach van thu
                            List<EntityUser> listVT = textDao.getListSecretaryByGroupId(receiverId, itemText.getOrgVhrId());

                            if (listVT != null && listVT.size() > 0) {
                                for (int i = 0; i < listVT.size(); i++) {
                                    Long strVTId = listVT.get(i).getUserId();
                                    if (isEiText && itemText.getlTextProcessId() != null) {
//                                        System.out.println("Gui tin nhan trinh phieu xuat nhap kho cho van thu: "
//                                                + strVTId);
                                        //neu la phieu xnk thi gui tin nhan bang tien trinh
                                        //rao lai do dang lay sai van thu tren con 2
//                                        autoDigitalSignDAO.sendSignTextByProcessVof1(itemText.getlTextProcessId(), strVTId,
//                                                AutoDigitalSignDAO.CLECK_SIGNATURE_TYPE);
                                    } else {
                                        //neu khong thi gui tin nhan binh thuong
                                        if (entityText.get(0).getCreatorIdVof2() != null
                                                && entityText.get(0).getCreatorIdVof2() > 0) {
//                                            System.out.println("Gui tin nhan trinh ky cho van thu tren 2.0 " + strVTId);
                                            smsDAO.sentMessToTextSignVof2(textTitle,
                                                    creatorId, strVTId,
                                                    Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                                    "", MessageUtils.TEXT_LINK_TYPE, textId,
                                                    Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                                        } else if (entityText.get(0).getCreatorId() != null) {
//                                            System.out.println("Gui tin nhan trinh ky cho van thu tren 1.0 " + strVTId);
                                            smsDAO.sentMessToTextSign(textTitle,
                                                    creatorId, strVTId,
                                                    Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                                    "", MessageUtils.TEXT_LINK_TYPE, textId);
                                        }
                                    }
                                }
                            }
                        } // Gui tin nhan cho lanh dao
                        else {
                            Long nextSignerId = 0L;
                            if (itemText.getSignerId() != null && !"0".equals(itemText.getSignerId())) {
                                nextSignerId = Long.parseLong(itemText.getSignerId());
                            }
                            if (isEiText) {
                                //neu la phieu xnk thi gui tin nhan bang tien trinh
//                                System.out.println("Gui tin nhan trinh phieu xuat nhap kho cho lanh dao: " + nextSignerId);
                                autoDigitalSignDAO.sendSignTextByProcessVof1(itemText.getlTextProcessId(), nextSignerId,
                                        AutoDigitalSignDAO.LEADER_SIGNATURE_TYPE);
                            } else {
                                //neu khong thi gui tin nhan binh thuong
                                if (entityText.get(0).getCreatorIdVof2() != null
                                        && entityText.get(0).getCreatorIdVof2() > 0) {
//                                    System.out.println("Gui tin nhan trinh ky cho lanh dao tren 2.0 " + itemText.getEmpVhrId());
                                    smsDAO.sentMessToTextSignVof2(textTitle, creatorId,
                                            itemText.getEmpVhrId(),
                                            Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                            "", MessageUtils.TEXT_LINK_TYPE, textId,
                                            Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                                } else if (entityText.get(0).getCreatorId() != null) {
//                                    System.out.println("Gui tin nhan trinh ky cho lanh dao tren 1.0 " + nextSignerId);
                                    smsDAO.sentMessToTextSign(textTitle, creatorId,
                                            nextSignerId, Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                            "", MessageUtils.TEXT_LINK_TYPE, textId);
                                }
                            }
                        }
                    } else if (itemText != null) {
                        //gui tin nhan cho lanh dao

                        Long nextSignerId = 0L;
                        if (itemText.getSignerId() != null && !"0".equals(itemText.getSignerId())) {
                            nextSignerId = Long.parseLong(itemText.getSignerId());
                        }
                        if (isEiText) {
                            //neu la phieu xnk thi gui tin nhan bang tien trinh
//                            System.out.println("Thuc hien gui tin nhan trinh ky XNK tiep theo cho lanh dao: " + nextSignerId);
                            autoDigitalSignDAO.sendSignTextByProcessVof1(itemText.getlTextProcessId(), nextSignerId,
                                    AutoDigitalSignDAO.LEADER_SIGNATURE_TYPE);
                        } else {
                            //neu khong thi gui tin nhan binh thuong
                            if (entityText.get(0).getCreatorIdVof2() != null
                                    && entityText.get(0).getCreatorIdVof2() > 0) {
//                                System.out.println("Thuc hien gui tin nhan trinh ky tiep theo cho lanh dao: " + itemText.getEmpVhrId());
                                smsDAO.sentMessToTextSignVof2(textTitle, creatorId,
                                        itemText.getEmpVhrId(),
                                        Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                        "", MessageUtils.TEXT_LINK_TYPE, textId,
                                        Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                            } else if (entityText.get(0).getCreatorId() != null) {
//                                System.out.println("Thuc hien gui tin nhan trinh ky tiep theo cho lanh dao: " + nextSignerId);
                                smsDAO.sentMessToTextSign(textTitle, creatorId,
                                        nextSignerId, Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                        "", MessageUtils.TEXT_LINK_TYPE, textId);
                            }
                        }
                    }
                }

                if (isLevelEnd < 2) {
                    //neu khong phai la ky cuoi
                    //130416 gui tin nhan cho nguoi ky truoc
                    if (!isEiText) {
                        //neu khong phai la chung tu XNK gui tin nhan

                        List<EntityTextProcess> lstTextProcess = autoDigitalSignDAO.getListChiefSignedText(textId, userIdVof1, userIdVof2);
                        if (lstTextProcess != null && lstTextProcess.size() > 0) {
                            int sizeLstProcess = lstTextProcess.size();
                            Long chiefId;
                            for (int i = 0; i < sizeLstProcess; i++) {
                                chiefId = lstTextProcess.get(i).getChiefId();
//                                System.out.println("Thuc hien gui tin nhan cho nhung nguoi ky truoc do trong luong ky: " + chiefId);
//                                if (chiefId != null && chiefId > 0) {
//                                    if (autoDigitalSignDAO.isReceiveSmsAfterSignNotFinal(chiefId)) {
//                                        
//                                    }
//                                }
                                if (entityText.get(0).getCreatorIdVof2() != null
                                        && entityText.get(0).getCreatorIdVof2() > 0) {
                                    if (lstTextProcess.get(i).getEmpVhrId() != null
                                            && !lstUserReceivedSms.contains(lstTextProcess.get(i).getEmpVhrId())) {
                                        //sua gui lap tin nhan
                                        smsDAO.sentMessToTextSignVof2(textTitle,
                                                userIdVof2, lstTextProcess.get(i).getEmpVhrId(),
                                                Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                                comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                                Constants.SMS_TEXT_INTERCEPT.SIGNER_SMSBEFORSIGN);
                                        lstUserReceivedSms.add(lstTextProcess.get(i).getEmpVhrId());
                                    }
                                } else if (entityText.get(0).getCreatorId() != null) {
//                               //neu duoc cau hinh thi moi gui tin nhan
                                    smsDAO.sentMessToTextSign(textTitle,
                                            userIdVof1, chiefId,
                                            Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                            comment, MessageUtils.TEXT_LINK_TYPE,
                                            textId);
                                }
                            }
                        }
                        // Lay danh sach nguoi ky song song gui tin nhan
                        List<EntityTextProcess> listUser = autoDigitalSignDAO.getListParallel(textId, userIdVof2);
                        if (!CommonUtils.isEmpty(listUser)) {
                            for (int i = 0; i < listUser.size(); i++) {
                                //neu co cau hinh nhan tin nhan
                                smsDAO.sentMessToTextSignVof2(textTitle, userIdVof2,
                                        listUser.get(i).getEmpVhrId(),
                                        Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE,
                                        textId,
                                        Constants.SMS_TEXT_INTERCEPT.SIGNER_SMSBEFORSIGN);
                            }
                        }
                    }
                }
                //28032017 gui tin nhan cho van thu neu la Ban TGD ky
                //sendSmsPreSignForSecretaryGroup(textId, textTitle, userIdVof2,
                //        (long) TextState.TEXT_ACTION_STATE_LD_SIGNER, comment, lstUserReceivedSms);

                //Neu ko phai la nguoi cuoi cung ky
                //Thuc hien gui tin nhan ky chinh cho ky nhay truong truong hop co chuyen ky nhay
                if (isLevelEnd == 2 && userIdVof2 != null && !autoDigitalSignDAO.checkTextTypeXNK(textId)) {
                    //neu la nguoi ky cuoi ky xong
                    //khong phai la phieu xnk
                    //gui tin nhan cho van thu ban hanh thu cong
                    autoDigitalSignDAO.sendSmsToSecretorPublishOrg(textId, userIdVof2);
                } else if (isLevelEnd != 2 && userIdVof2 != null) {
                    //datnv5: bo sung chuyen trang thai nguoi ky nhay sang trang thai nguoi ky chinh da ky
                    TextSignDAO textSignDAO = new TextSignDAO();
                    EntityUserGroup userGroup = new EntityUserGroup();
                    userGroup.setVof2_ItemEntityUser(userVof2);
                    List<Long> listIdTextSign = new ArrayList<>();
                    listIdTextSign.add(textId);
                    Boolean resultCheckDeny = textSignDAO.updateInitialsignToDenyWhenMainsign(userGroup, listIdTextSign);
                    if (resultCheckDeny) {
                        //gui tin nhan cho nguoi ky nhay biet ky chinh da ky
                        addSendMessageInitialSignWait(userGroup, listIdTextSign);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("processSendSmsSignText (Gui tin nhan sau khi ky duyet"
                    + " van ban) - Exception - textId: " + textId
                    + " | userId1: " + userIdVof1, ex);
        }
    }

    /**
     * cap nhat level // bang text
     *
     * @param textId
     * @param level
     * @param state
     * @param cmd
     * @return
     */
    public boolean updateParallelLevelText(Long textId, Long level, Long state,
            CommonDataBaseDaoVO1 cmd) {

        boolean res = false;
        try {
            StringBuilder sqlUpdateLevelText = new StringBuilder();
            HashMap<String, Object> hmParam = new HashMap<String, Object>();
            sqlUpdateLevelText.append(" update text t set t.SIGN_LEVEL_PARALLEL =:signLevel ");
            hmParam.put("signLevel", level);
            if (state != null) {
                //neu co cap nhat trang thai
                sqlUpdateLevelText.append(" , t.state = :state ");
                hmParam.put("state", state);
            }
            sqlUpdateLevelText.append(" where t.text_id =:textId ");
            hmParam.put("textId", textId);
            res = cmd.insertOrUpdateDataBase(sqlUpdateLevelText, hmParam);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return res;
    }

    //Ham gui sms toi tat ca nguoi da ky trong luong ky
    public int sendSmsToSignerAll(Long textId, Long userIdVof1, Long userIdVof2,
            String titleOfText, String comment, CommonDataBaseDaoVO1 cmd) {
        int res = 0;
        try {
            CommonControler smsDAO = new CommonControler();
            List<Object> param = new ArrayList<Object>();
            StringBuilder sqlTextProcess = new StringBuilder();
            sqlTextProcess.append("select a.chief_Id chiefId,a.emp_vhr_id empVhrId");
            sqlTextProcess.append(" from Text_Process a where a.text_Id= ?");
            param.add(textId);
            List<EntityTextProcess> lstTextProcess = (List<EntityTextProcess>) cmd.excuteSqlGetListObjOnCondition(sqlTextProcess, param, null, null, EntityTextProcess.class);
            if (lstTextProcess != null && lstTextProcess.size() > 0) {
                int sizeList = lstTextProcess.size();
                //lay don vi ban hanh
                AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
                List<Vof2_EntityUser> lstSecetary = autoDigitalSignDAO.getListSecretaryPublishOrg(textId);
                Boolean isSend;
                for (int i = 0; i < sizeList; i++) {
                    if (lstTextProcess.get(i).getEmpVhrId() != null && lstTextProcess.get(i).getEmpVhrId() > 0) {
                        if (!lstTextProcess.get(i).getEmpVhrId().equals(userIdVof2)) {
                            //Neu ton tai nguoi ky tren 2.0 thuc hien gui tin nhan tren 2.0
//                            System.out.println("Thuc hien gui tin nhan da ky van ban "
//                                    + titleOfText + " tren 2.0 cho nguoi ky: " + lstTextProcess.get(i).getEmpVhrId());
                            isSend = true;
                            if (lstSecetary != null && !lstSecetary.isEmpty()) {
                                for (Vof2_EntityUser userTmp : lstSecetary) {
                                    if (userTmp.getUserId().equals(lstTextProcess.get(i).getEmpVhrId())) {
                                        //neu la van thu cua don vi ban hanh thi khong gui nua
                                        isSend = false;
                                        break;
                                    }
                                }
                            }
                            if (isSend) {
                                //kiem tra ko gui tin nhan cua nguoi ky truoc neu cau hinh
                                smsDAO.sentMessToTextSignVof2(titleOfText,
                                        userIdVof2, lstTextProcess.get(i).getEmpVhrId(),
                                        Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                        comment, MessageUtils.TEXT_LINK_TYPE, textId,
                                        Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
                                res++;
                            }
                        }
                    } else if (userIdVof1 != null && userIdVof1 > 0L
                            && lstTextProcess.get(i).getChiefId() != null
                            && lstTextProcess.get(i).getChiefId() > 0) {
                        if (!lstTextProcess.get(i).getChiefId().equals(userIdVof1)) {
                            //Neu ton tai nguoi ky tren 2.0 thuc hien gui tin nhan tren 2.0
//                            System.out.println("Thuc hien gui tin nhan da ky van ban "
//                                    + titleOfText + " tren 1.0 cho nguoi ky: " + lstTextProcess.get(i).getEmpVhrId());
                            smsDAO.sentMessToTextSign(titleOfText,
                                    userIdVof1, lstTextProcess.get(i).getChiefId(),
                                    Constants.SMS_TEXT_CONFIG.LEADER_MAIN_SIGN_TEXT,
                                    comment, MessageUtils.TEXT_LINK_TYPE, textId);
                            res++;
                        }
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("sendSmsToSignerAll", e);
        }
        return res;
    }

    /*
     * Ham lay level cua bang Text
     */
    public EntityText getTextById(Long textId, CommonDataBaseDaoVO1 cmd) {
        EntityText text = null;
        try {
            List<Object> params = new ArrayList<Object>();
            StringBuilder sql = new StringBuilder();
            sql.append("select text_id textId,sign_level signLevel,title,");
            sql.append("SIGN_LEVEL_PARALLEL signParallel, ");
            sql.append(" creator_id_vof2 creatorIdVof2,IS_LIENKE isLienKe ");
            sql.append(" from Text where text_id=? ");
            params.add(textId);
            List<EntityText> lstText = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(sql, params, null, null, EntityText.class);
            if (lstText != null && lstText.size() > 0) {
                text = lstText.get(0);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return text;
    }

    /**
     * <b> Ham lay level hien tai cua danh sach ky</b><br>
     *
     * @param textId
     * @param cmd
     * @return
     * @throws Exception
     */
    private Long getTextSignLevelInProcess(
            Long textId, CommonDataBaseDaoVO1 cmd) throws Exception {
        Long level = -1L;
        if (textId == null || textId <= 0) {
            return -1L;
        }
        try {
            //15/04 Hiendv sua noi dung lay danh sach theo thu tu signature_type
            List<Object> params = new ArrayList<Object>();
            StringBuilder sqlTextProcess = new StringBuilder();
            sqlTextProcess.append(" select a.sign_level signLevel, a.state state");
            sqlTextProcess.append(" from text_process a where a.text_id = ? ");
            sqlTextProcess.append(" order by a.sign_level desc, a.signature_type desc ");
            params.add(textId);
            List<EntityTextProcess> lstTextProcess = (List<EntityTextProcess>) cmd.excuteSqlGetListObjOnCondition(sqlTextProcess, params, null, null, EntityTextProcess.class);

            if (lstTextProcess == null || lstTextProcess.isEmpty()) {
                return 0L;
            }
            final int size = lstTextProcess.size();

            EntityTextProcess temp;
            int count = 0;
            for (int i = 0; i < size; i++) {
                temp = lstTextProcess.get(i);
                if (temp.getState() == Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER) {
                    if (i == 0) {
                        return -1L;
                    } else {
                        return lstTextProcess.get(i - 1).getSignLevel();
                    }
                } else if (temp.getState() == Constant.TEXT_PROCESS_DEFINE.TEXT_STATE_NEW_CREATE) {
                    count++;
                }
            }
            /*
             * TruongPV added: 17-04-2011 Xu ly tinh signLevel cho truong hop add
             * nguoi ky duyet, sau do add nguoi ky nhay ==> update signLevel cua
             * text la 0L
             */
            if (count == size) {
                return 0L;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return level;
        }

        return level;

    }

    /**
     * <b>Ham update level cua bang Text sau khi ky</b><br>
     *
     * @param textId Id van ban cap nhat
     * @param level Thu tu ky tiep theo cua bang text
     * @param state Trang thai<br>
     * 1: Dang xu ly<br>
     * 3: Da ket thuc luong ky<br>
     * @param cmd
     * @return
     */
    public boolean updateLevelText(Long textId, Long level, Long state, CommonDataBaseDaoVO1 cmd) {
        boolean res = false;
        try {
            StringBuilder sqlUpdateLevelText = new StringBuilder();
            HashMap<String, Object> hmParam = new HashMap<String, Object>();
            sqlUpdateLevelText.append(" update text t set ");
            sqlUpdateLevelText.append(" t.sign_level =:signLevel, ");
            sqlUpdateLevelText.append("t.state = :state where t.text_id =:textId ");
            hmParam.put("signLevel", level);
            hmParam.put("textId", textId);
            hmParam.put("state", state);
            res = cmd.insertOrUpdateDataBase(sqlUpdateLevelText, hmParam);
//            System.out.println("================Update level text: " + res);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return res;
    }

    /**
     * lay danh sach ca nhan vai tro la van thu
     *
     * @param groupId
     * @param textId
     * @param v2GroupId
     * @return
     */
    public List<EntityTextProcess> getListUserRoleSecrectary(Long groupId, Long textId,
            Long v2GroupId) {

        List<EntityTextProcess> listVT;
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        sql.append("select CHIEF_ID chiefId, RECEIVER_ID receivedId, EMP_VHR_ID empVhrId, ");
        sql.append(" ORG_VHR_ID orgVhrId from TEXT_PROCESS ");
        sql.append(" where TEXT_ID = ? and SIGNATURE_TYPE = 1 and (ORG_VHR_ID = ?");

        params.add(textId);
        params.add(v2GroupId);
        if (groupId != null && groupId > 0) {
            sql.append(" OR RECEIVER_ID = ?");
            params.add(groupId);
        }
        sql.append(")");

        CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
        listVT = (List<EntityTextProcess>) cmd.excuteSqlGetListObjOnCondition(sql, params,
                null, null, EntityTextProcess.class);
        return listVT;
    }

    /*
     *
     * Ham lay thong tin van ban
     */
    public List<EntityText> getInfoText(Long textId) {
        List<EntityText> listResult = null;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("Select t.TITLE title, t.CREATOR_ID creator, t.CREATOR_ID_VOF2 creatorIdVof2 , t.CREATOR_ID creatorId, ");
            sql.append(" t.is_encrypt as isEncrypt,t.state as status from text t where t.text_id = ?");
            List<Object> param = new ArrayList<>();
            param.add(textId);
            CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
            listResult = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(sql, param, null, null, EntityText.class);
        } catch (Exception ex) {

            LOGGER.error("getInfoText", ex);
        }
        return listResult;

    }

    /**
     * thuc hien gui tin nhan cho nguoi ky nhay chu ky ma ky chinh da ky
     *
     * @param userGroup
     * @param listIdTextSign
     */
    private void addSendMessageInitialSignWait(EntityUserGroup userGroup, List<Long> listIdTextSign) {
        if (listIdTextSign != null && listIdTextSign.size() > 0) {
            List<EntityVhrEmployee> lstVhrE = getListUserReceive(userGroup, listIdTextSign);
            if (lstVhrE == null) {
                return;
            }
            if (listIdTextSign.size() == 1) {
                //gui 1 van ban
                List<EntityText> itemText = getInfoText(listIdTextSign.get(0));
                String strMess = "D/c " + FunctionCommon.removeUnsign(userGroup.getName2())
                        + " DA KY DUYET van ban co tieu de: " + FunctionCommon.removeUnsign(
                                itemText.get(0).getTitle().trim())
                        + ". VAN BAN ky nhay cua dong chi da chuyen vao muc tat ca";
                strMess = MessageUtils.addLinkIntoMessage(strMess, MessageUtils.TEXT_LINK_TYPE, listIdTextSign.get(0));
                for (EntityVhrEmployee userVhr : lstVhrE) {
                    SmsDAO sendOtpToUser = new SmsDAO();
                    sendOtpToUser.addMessToTableMessVof2(userGroup.getUserId2(), userVhr.getEmployeeId(),
                            CommonControler.getSMSMobile(userVhr.getMobilePhone()),
                            strMess, 1, Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                }
            } else {
                //gui nhieu van ban
                String strMess = "D/c :" + FunctionCommon.removeUnsign(userGroup.getName2())
                        + "  DA KY DUYET " + listIdTextSign.size()
                        + " van ban"
                        + ". Cac VAN BAN nay duoc chuyen vao muc tat ca van ban";
                for (EntityVhrEmployee userVhr : lstVhrE) {
                    SmsDAO sendOtpToUser = new SmsDAO();
                    sendOtpToUser.addMessToTableMessVof2(userGroup.getUserId2(),
                            userVhr.getEmployeeId(),
                            CommonControler.getSMSMobile(userVhr.getMobilePhone()),
                            strMess, 1, Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                }
            }
        }
    }

    /**
     * danh sach nguoi nhan tin nhan nguoi ky chinh da ky nhung chua thuc hien
     * ky nhay
     *
     * @param userGroup
     * @param listIdTextSign
     * @return
     */
    private List<EntityVhrEmployee> getListUserReceive(EntityUserGroup userGroup,
            List<Long> listIdTextSign) {
        //danh sach nguoi ky nhay
        StringBuilder sqlGetListUser = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sqlGetListUser.append(" select EMP_VHR_ID userId from TEXT_PROCESS ");
        sqlGetListUser.append(" where TEXT_ID in( ");
        sqlGetListUser.append(FunctionCommon.generateQuestionMark(listIdTextSign.size()));
        sqlGetListUser.append(" ) and SIGNATURE_TYPE = 2 and state = 6 ");
        sqlGetListUser.append(" and ASSIGNER_ID_VOF2 = ? and EMP_VHR_ID is not null");
        params.addAll(listIdTextSign);
        params.add(userGroup.getUserId2());

        CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
        List<EntityUser> listIdUserReceive = (List<EntityUser>) cmd.excuteSqlGetListObjOnCondition(sqlGetListUser, params,
                null, null, EntityUser.class);
        //lay thong tin nguoi nhan tu danh sach id
        if (listIdUserReceive == null || listIdUserReceive.size() <= 0) {
            return null;
        }
        List<Long> listUs = new ArrayList<>();
        for (EntityUser listIdUserReceive1 : listIdUserReceive) {
            listUs.add(listIdUserReceive1.getUserId());
        }
        StringBuilder sqlUserV2 = new StringBuilder();
        List<Object> params2 = new ArrayList<>();
        CommonDataBaseDaoVO2 cmdV2 = new CommonDataBaseDaoVO2();
        sqlUserV2.append(" select EMPLOYEE_ID employeeId,MOBILE_PHONE mobilePhone");
        sqlUserV2.append(" from VHR_EMPLOYEE where EMPLOYEE_ID in(");
        sqlUserV2.append(FunctionCommon.generateQuestionMark(listUs.size()));
        sqlUserV2.append(")");
        params2.addAll(listUs);
        List<EntityVhrEmployee> listUserReceiveV2 = (List<EntityVhrEmployee>) cmdV2.excuteSqlGetListObjOnCondition(sqlUserV2, params2,
                null, null, EntityVhrEmployee.class);
        if (listUserReceiveV2 == null || listUserReceiveV2.size() <= 0) {
            return null;
        } else {
            return listUserReceiveV2;
        }
    }
}
