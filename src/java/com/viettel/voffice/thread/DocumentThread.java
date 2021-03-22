/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.thread;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.database.dao.document.DocumentDAO;
import com.viettel.voffice.database.dao.document.DocumentInGroupDAO;
import com.viettel.voffice.database.dao.document.DocumentInStaffDAO;
import com.viettel.voffice.database.dao.file.FilesAttachmentDAO;
import com.viettel.voffice.database.dao.text.TextDAO;
import com.viettel.voffice.database.entity.EntityDocument;
import com.viettel.voffice.database.entity.EntityFilesAttachment;
import com.viettel.voffice.database.entity.User.EntityCvGroup;
import com.viettel.voffice.database.entity.document.EntityDocumentInGroup;
import com.viettel.voffice.database.entity.document.EntityDocumentInStaff;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.utils.CommonUtils;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Thread document
 *
 * @author thanght6
 * @since May 21, 2016
 */
public class DocumentThread {

    private static final Logger log = Logger.getLogger(DocumentThread.class);

    // Thread dem so luong van ban
    public static class DocumentCountThread extends Thread {

        // Id nguoi dung tren he thong 1
        private Long userId1;
        // Id nguoi dung tren he thong 2
        private Long userId2;
        // Loai danh sach
        private Integer type;
        // Trang thai
        private Integer status;
        // Doi tuong truy van bang document
        private DocumentDAO documentDAO;
        // So luong dem duoc
        private int count;
        // Thoi gian bat dau
        private Date startTime;
        // Thoi gian ket thuc
        private Date endTime;
        
        private List<Long> listSecretary;

        public DocumentCountThread(Long userId1, Long userId2,
                Integer type, Integer status, DocumentDAO documentDAO,
                List<Long> listSecretary) {
            this.userId1 = userId1;
            this.userId2 = userId2;
            this.type = type;
            this.status = status;
            this.documentDAO = documentDAO;
            this.listSecretary = listSecretary;
        }

        @Override
        public void run() {
            startTime = new Date();
            try {
                count = documentDAO.countDocument(userId1, userId2, type, status, listSecretary);
            } catch (Exception e) {
                log.error("Thread err", e);
                count = 0;
            }
            endTime = new Date();
        }

        public Integer getType() {
            return type;
        }

        public Integer getStatus() {
            return status;
        }

        public int getCount() {
            return count;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

		public List<Long> getListSecretary() {
			return listSecretary;
		}

		public void setListSecretary(List<Long> listSecretary) {
			this.listSecretary = listSecretary;
		}
    }

    // Thread lay chi tiet van ban
    public static class DocumentDetailThread extends Thread {

        // Id nguoi dung
        private Long userId1;
        // Id nguoi dung Vof2
        private Long userId2;
        // Id nguoi chuyen van ban tren he thong 1
        private Long senderId1;
        // Id nguoi chuyen van ban tren he thong 2
        private Long senderId2;
        // Id van ban
        private Long documentId;
        // Loai
        private int type;
        // Doi tuong truy van bang document
        private DocumentDAO documentDAO;
        // Doi tuong truy van bang files_attachment
        private FilesAttachmentDAO filesAttachmentDAO;
        // Doi tuong truy van bang document_in_staff
        private DocumentInStaffDAO documentInStaffDAO;
        // Doi tuong truy van bang document_in_group
        private DocumentInGroupDAO documentInGroupDAO;
        // Doi tuong truy van bang text
        private TextDAO textDAO;
        // Thong tin chi tiet cong van
        private EntityDocument document;
        // Danh sach file dinh kem
        private List<EntityFilesAttachment> listAttachment;
        // Danh sach y kien chi dao
        private List<EntityDocumentInStaff> listComment;
        // Danh sach nguoi cung nhan van ban do voi nguoi dung
        private List<EntityDocumentInStaff> listReceiverSameUser;
        // Danh sach nguoi nhan van ban do tu nguoi dung
        private List<EntityDocumentInStaff> listReceiver;
        // Danh sach don vi nhan van ban do tu nguoi dung
        private List<EntityDocumentInGroup> listGroup;
        // Danh sach nguoi ky chinh
        private List<EntityText> listMainSigner;
        // Danh sach nguoi ky ra soat
        private List<EntityText> listReviewSigner;
        // Danh sach nguoi ky nhay
        private List<EntityText> listFlashingSigner;
        private List<EntityCvGroup> lstCvGroup;
        // Thoi gian bat dau
        private Date startTime;
        // Thoi gian ket thuc
        private Date endTime;
        //hien thi thong tin nguoi nhan van ban danh sach trong y kien 
        private Boolean isReceiverShow;
        //Id chuyen van ban
        private Long documentInStaffId;

        public DocumentDetailThread(Long userId1, Long userId2, Long senderId1,
                Long senderId2, Long documentId,Long documentInStaffId, int type, DocumentDAO documentDAO,
                FilesAttachmentDAO filesAttachmentDAO, DocumentInStaffDAO documentInStaffDAO,
                DocumentInGroupDAO documentInGroupDAO, TextDAO textDAO) {
            this.userId1 = userId1;
            this.userId2 = userId2;
            this.senderId1 = senderId1;
            this.senderId2 = senderId2;
            this.documentId = documentId;
            this.type = type;
            this.documentDAO = documentDAO;
            this.filesAttachmentDAO = filesAttachmentDAO;
            this.documentInStaffDAO = documentInStaffDAO;
            this.documentInGroupDAO = documentInGroupDAO;
            this.textDAO = textDAO;
            this.documentInStaffId=documentInStaffId;
        }

        public int getType() {
            return type;
        }

        public EntityDocument getDocument() {
            return document;
        }

        public List<EntityFilesAttachment> getListAttachment() {
            return listAttachment;
        }

        public List<EntityDocumentInStaff> getListComment() {
            return listComment;
        }

        public List<EntityDocumentInStaff> getListReceiverSameUser() {
            return listReceiverSameUser;
        }

        public List<EntityDocumentInStaff> getListReceiver() {
            return listReceiver;
        }

        public List<EntityDocumentInGroup> getListGroup() {
            return listGroup;
        }

        public List<EntityText> getListMainSigner() {
            return listMainSigner;
        }

        public List<EntityText> getListReviewSigner() {
            return listReviewSigner;
        }

        public List<EntityText> getListFlashingSigner() {
            return listFlashingSigner;
        }

        public List<EntityCvGroup> getLstCvGroup() {
            return lstCvGroup;
        }

        public void setLstCvGroup(List<EntityCvGroup> lstCvGroup) {
            this.lstCvGroup = lstCvGroup;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public Boolean isIsReceiverShow() {
            return isReceiverShow;
        }

        public void setIsReceiverShow(Boolean isReceiverShow) {
            this.isReceiverShow = isReceiverShow;
        }

        public Long getDocumentInStaffId() {
            return documentInStaffId;
        }

        public void setDocumentInStaffId(Long documentInStaffId) {
            this.documentInStaffId = documentInStaffId;
        }

        @Override
        public void run() {
            startTime = new Date();
            try {
                switch (type) {
                    // Thong tin chi tiet van ban
                    case Constants.Document.DetailType.DETAIL:
                        document = documentDAO.getDocumentDetail(userId1, userId2,
                                senderId1, senderId2, documentId,documentInStaffId);
                        break;
                    // Danh sach file dinh kem
                    case Constants.Document.DetailType.LIST_ATTACHMENT:
                        listAttachment = filesAttachmentDAO.getListAttachment(documentId,userId2);
                        break;
                    // Danh sach y kien chi dao
                    case Constants.Document.DetailType.LIST_COMMENT:
                        listComment = documentDAO.getListCommentFromDocument(
                                userId1, userId2, documentId, true);
                        break;
                    // Danh sach nguoi cung nhan van ban do voi nguoi dung
                    case Constants.Document.DetailType.LIST_RECEIVER_SAME_USER:
                        listReceiverSameUser = documentInStaffDAO.getListReceiver(
                                senderId1, senderId2, documentId, 0L, 5L, userId2, userId1, null);
                        break;
                    // Danh sach nguoi nhan van ban do tu nguoi dung
                    case Constants.Document.DetailType.LIST_RECEIVER:
                        listReceiver = documentInStaffDAO.getListReceiver(
                                userId1, userId2, documentId, 0L, 5L, userId2, userId1, null);
                        //Gan danh sach file co comment khi chuyen
                        listReceiver = documentInStaffDAO.addLstFileCommentToLstReceiver(listReceiver);
                        break;
                    // Danh sach don vi nhan van ban do tu nguoi dung
                    case Constants.Document.DetailType.LIST_GROUP:
                        listGroup = documentInGroupDAO.getListGroup(
                                userId1, userId2, documentId, 0L, 5L);
                        break;
                    // Danh sach nguoi ky
                    case Constants.Document.DetailType.LIST_SIGNER:
                        EntityText text = textDAO.getTextByDocumentId(documentId);
                        if (text == null || text.getTextId() == null) {
                            break;
                        }
                        List<EntityText> listSigner = textDAO.getTextListUserSign(text.getTextId(), userId2, null);
                        if (CommonUtils.isEmpty(listSigner)) {
                            break;
                        }
                        listMainSigner = textDAO.convertListSignTypeNew(listSigner, 1);
                        listReviewSigner = textDAO.convertListSignTypeNew(listSigner, 2);
                        listFlashingSigner = textDAO.convertListSignTypeNew(listSigner, 3);
                        break;
                    //Lay danh sach nhom van ban da chuyen
                    case Constants.Document.DetailType.LIST_CV_GROUP_RECEIVER:
                        lstCvGroup = documentDAO.getListReceivedCvGroup(userId1,
                                userId2, documentId);
                        break;
                }
            } catch (Exception e) {
                log.error("Thread err (DocumentDetailThread) ", e);
            }
            endTime = new Date();
        }
    }
}
