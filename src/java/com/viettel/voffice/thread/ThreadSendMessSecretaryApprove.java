/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.thread;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.controler.CommonControler;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO1;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO2;
import com.viettel.voffice.database.dao.document.AutoDigitalSignDAO;
import com.viettel.voffice.database.dao.document.DocumentSignDAO;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextProcess;
import com.viettel.voffice.utils.MessageUtils;
import java.util.ArrayList;
import java.util.List;
import vn.viettel.core.sign.utils.Constant;

/**
 *
 * @author datnv5
 */
public class ThreadSendMessSecretaryApprove implements Runnable {

    Boolean isSignParallel;
    Long textId;
    List<Long> lstGroupSecVof2;
    Long parallelSignLevel;
    List<EntityTextProcess> lstLeader;
    String comment;
    Long userIdVof2;
    Long userIdVof1;
    int sendSMS;
    public ThreadSendMessSecretaryApprove(Boolean isSignParallel,Long textId
        ,List<Long> lstGroupSecVof2, Long parallelSignLevel,List<EntityTextProcess> lstLeader
        , String comment, Long userIdVof2,  Long userIdVof1,int sendSMS) {
        this.isSignParallel = isSignParallel;
        this.textId = textId;
        this.lstGroupSecVof2 = lstGroupSecVof2;
        this.parallelSignLevel = parallelSignLevel;
        this.lstLeader = lstLeader;
        this.comment =  comment;
        this.userIdVof2 = userIdVof2;
        this.userIdVof1 = userIdVof1;
        this.sendSMS = sendSMS;
    }

    @Override
    public void run() {
        sendMessBeforeSecretaryApprove();
    }

    private void sendMessBeforeSecretaryApprove() {
        CommonControler smsDAO = new CommonControler();
        int resultP = 0;
        if (isSignParallel) {
            //neu la xet duyet ky song song
            //cap nhat bang next_sign_parallel van thu
            DocumentSignDAO documentSignDAO = new DocumentSignDAO();
            resultP = documentSignDAO.updateSignParallelNext(textId, null,
                    (long) Constant.TEXT_PROCESS_DEFINE.TEXT_ACTION_STATE_LD_SIGNER, lstGroupSecVof2);
        }
        if (!isSignParallel || (isSignParallel && resultP == 1)) {
            //neu khong phai la ky song song
            //hoac la ky song song va cap nhat bang text_sign_next thanh cong
            //thuc hien gui tin nhan
            if (isSignParallel && resultP == 1) {
                //cap nhat va gui tin nhan cho nguoi ky tiep theo text_sign_next
                StringBuilder querySms = new StringBuilder();
                querySms.append("select t.title title, t.creator_id_vof2 creatorId, ");
                querySms.append(" tp.emp_vhr_id signerIdVO1, tp.org_vhr_id receivedId, ");
                querySms.append(" 2 isVtReviewNew, ");
                querySms.append(" t.SIGN_LEVEL_PARALLEL signParallel, ");
                querySms.append(" tp.SIGN_LEVEL_PARALLEL parallelSignLevel, tp.state state ");
                querySms.append(" from text t left join text_process tp on ");
                querySms.append(" t.text_id = tp.text_id where t.text_id = ? and tp.SIGN_LEVEL_PARALLEL = ? ");
                querySms.append(" and (tp.state = 0 or tp.state = 3) and tp.signature_type = 3 ");
                List<Object> paramSms = new ArrayList<>();
                paramSms.add(textId);
                paramSms.add(parallelSignLevel);
                CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
                List<EntityText> listResult = (List<EntityText>) cmd.excuteSqlGetListObjOnCondition(querySms, paramSms, null, null, EntityText.class);
                CommonDataBaseDaoVO2 cmdVof2 = new CommonDataBaseDaoVO2();
                DocumentSignDAO documentSignDAO = new DocumentSignDAO();
                documentSignDAO.updateNextSignParallelAndSendSms(listResult, textId, cmdVof2, true);
            }

            // <editor-fold defaultstate="collapsed" desc="1. Gui tin nhan cho nguoi trinh ky van ban.">
            if (lstLeader.get(0).getCreatorIdVof2() != null) {
//                        System.out.println("Thuc hien gui tin nhan xet duyet tren 2.0 "
//                                + " lstLeader.get(0).getCreatorIdVof2(): " + lstLeader.get(0).getCreatorIdVof2()
//                                + " getUserId2:" + userVof2.getUserId());
                smsDAO.sentMessToTextSignVof2(lstLeader.get(0).getTitle(),
                        userIdVof2, lstLeader.get(0).getCreatorIdVof2(),
                        Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT,
                        comment, MessageUtils.TEXT_LINK_TYPE, textId,
                        Constants.SMS_TEXT_INTERCEPT.SIGNMAIN_SMSSIGNER_REJECT);
            } else {
//                        System.out.println("Thuc hien gui tin nhan xet duyet tren 1.0 "
//                                + " lstLeader.get(0).getCreatorId(): " + lstLeader.get(0).getCreatorId()
//                                + " userIdVof1:" + userIdVof1);
                smsDAO.sentMessToTextSign(lstLeader.get(0).getTitle(),
                        userIdVof1, lstLeader.get(0).getCreatorId(),
                        Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT,
                        comment, MessageUtils.TEXT_LINK_TYPE, textId);
            }

            //</editor-fold>
            // <editor-fold defaultstate="collapsed" desc="2. Gui tin nhan trinh ky cho lanh dao cua van than thu.">
            if (!isSignParallel && sendSMS == 1) {
                //neu khong phai la ky song song thi gui tin nhan theo luong
                for (EntityTextProcess tp : lstLeader) {
                    if (tp.getEmpVhrId() != null) {
                        smsDAO.sentMessToTextSignVof2(tp.getTitle(),
                                tp.getCreatorIdVof2(), tp.getEmpVhrId(),
                                Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                "", MessageUtils.TEXT_LINK_TYPE, textId,
                                Constants.SMS_TEXT_INTERCEPT.TOSUBMIT);
                        break;
                    } else {
                        smsDAO.sentMessToTextSign(tp.getTitle(),
                                tp.getCreatorId(), tp.getChiefId(),
                                Constants.SMS_TEXT_CONFIG.CREATE_NEW_TEXT,
                                "", MessageUtils.TEXT_LINK_TYPE, textId);
                        break;
                    }
                }
            }
            //</editor-fold>
            // <editor-fold defaultstate="collapsed" desc="3. Gui tin nhan da xet duyet cho nguoi ky truoc do.">
            AutoDigitalSignDAO autoDigitalSignDAO = new AutoDigitalSignDAO();
            List<EntityTextProcess> lstTextProcess = autoDigitalSignDAO.
                    getListChiefSignedText(textId, userIdVof1, userIdVof2);
            if (lstTextProcess != null && lstTextProcess.size() > 0) {
                int sizeLstProcess = lstTextProcess.size();
                Long chiefId;
                for (int i = 0; i < sizeLstProcess; i++) {
                    chiefId = lstTextProcess.get(i).getChiefId();
                    if (lstLeader.get(0).getCreatorIdVof2() != null
                            && lstLeader.get(0).getCreatorIdVof2() > 0) {
                        smsDAO.sentMessToTextSignVof2(
                                lstLeader.get(0).getTitle(),
                                userIdVof2,
                                lstTextProcess.get(i).getEmpVhrId(),
                                Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT,
                                comment, MessageUtils.TEXT_LINK_TYPE,
                                textId, Constants.SMS_TEXT_INTERCEPT.SIGNER_SMSBEFORSIGN);
                    } else if (lstLeader.get(0).getCreatorId() != null) {
                        smsDAO.sentMessToTextSign(
                                lstLeader.get(0).getTitle(),
                                userIdVof1, chiefId,
                                Constants.SMS_TEXT_CONFIG.SECRETARY_SIGN_TEXT,
                                comment, MessageUtils.TEXT_LINK_TYPE,
                                textId);
                    }
                }
            }
            //</editor-fold>
        }

    }

}
