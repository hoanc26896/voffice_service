/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.itextpdf.text.pdf.PdfReader;
import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.file.DownloadFileDocumentDAO;
import com.viettel.voffice.database.dao.staff.OrgDAO;
import com.viettel.voffice.database.dao.staff.StaffImageSignDAO;
import com.viettel.voffice.database.dao.staff.UserDAO;
import com.viettel.voffice.database.dao.task.TaskApprovalDAO;
import com.viettel.voffice.database.dao.task.TaskDAO;
import com.viettel.voffice.database.dao.text.TextProcessDAO;
import com.viettel.voffice.database.entity.EntityFileAttachment;
import com.viettel.voffice.database.entity.EntityFilesAttachment;
import com.viettel.voffice.database.entity.EntityImageOrg;
import com.viettel.voffice.database.entity.EntityRatioConfigDetail;
import com.viettel.voffice.database.entity.EntityStaff;
import com.viettel.voffice.database.entity.EntityStaffImageSign;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.EntityVhrEmployee;
import com.viettel.voffice.database.entity.EntityVhrOrg;
import com.viettel.voffice.database.entity.User.EntityOrgCriteriaRatingTotal;
import com.viettel.voffice.database.entity.User.EntityUser;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.meeting.EntityMeetingMinutes;
import com.viettel.voffice.database.entity.task.EntityEmpRating;
import com.viettel.voffice.database.entity.task.EntityMission;
import com.viettel.voffice.database.entity.task.EntityMissionChild;
import com.viettel.voffice.database.entity.task.EntityTask;
import com.viettel.voffice.database.entity.task.EntityTaskApproval;
import com.viettel.voffice.database.entity.text.EntityText;
import com.viettel.voffice.database.entity.text.EntityTextProcess;
import com.viettel.voffice.report.template.ReportTemplateManager;
import com.viettel.voffice.security.DES;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/**
 *
 * @author thanght6
 */
public class FileUtils {

    // Logger de ghi lai loi
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class);
    // Id loai hop dong dich vu
    private static Long collaboratorContractTypeId1;

    /**
     * Lay id loai hop dong dich vu
     *
     * @return
     */
    private static synchronized Long getCollaboratorContractTypeId() {
        if (collaboratorContractTypeId1 == null) {
            try {
                collaboratorContractTypeId1 = Long.parseLong(CommonUtils
                        .getAppConfigValue("id.type.contract.collaborator"));
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return collaboratorContractTypeId1;
    }
    // Phan mo rong cua file pdf
    public static final String PDF_FILE_EXTENSION = ".pdf";
    // Phan mo rong cua file cer
    public static final String CER_FILE_EXTENSION = ".cer";
    // Phan mo rong file anh JPG
    public static final String JPG_IMAGE_FILE_EXTENSION = ".jpg";

    /**
     * Phan mo rong file anh PNG
     */
    public static final String PNG_IMAGE_FILE_EXTENSION = ".png";

    // Ten file giao viec mac dinh cho cong tac vien (Hop dong dich vu)
    public static final String DEFAULT_COLLABORATOR_TASK_ASSIGNMENT_FILE_NAME = "Bao_cao_giao_nhiem_vu_HDDV.pdf";
    // Ten file giao viec mac dinh cho nhan vien chinh thuc (Hop dong lao dong)
    public static final String DEFAULT_EMPLOYEE_TASK_ASSIGNMENT_FILE_NAME = "Phieu_giao_nhiem_vu.pdf";
    // Ten file danh gia cong viec mac dinh
    public static final String DEFAULT_TASK_ASSESSMENT_FILE_NAME = "Danh_gia_cong_viec_nhan_vien.pdf";
    // Thu muc xuat file chung
    private static String exportFolder1 = "";
    private static String exportFolder2 = "";
    // Ten storage luu file tam
    private static String tmpStorageName = "";
    // Thu muc storage luu file tam
    private static String tmpStorageFolder = "";
    // Thu muc anh panner
    private static String pannerImageFolder = "";
    // Thu muc anh bao cao bieu do
    private static String imageReportFolder = "";
    
    //pm1_os20 dinh nghia file separator
//    private static String _separator = File.separator;
    private static final String _separator = "/";

    /**
     * Lay thuc muc xuat file chung
     *
     * @return Duong dan tuyet doi thu muc export
     */
    private static synchronized String getExportFolder() {
        if (CommonUtils.isEmpty(exportFolder1)) {
            exportFolder1 = CommonUtils.getAppConfigValue("folder.export");
        }
        return exportFolder1;
    }
    // Thu muc upload chung
    private static String uploadFolder1 = "";

    /**
     * Lay duong dan thu muc upload
     *
     * @return duong dan tuyen doi thu muc upload
     */
    private static synchronized String getUploadFolder() {
        if (CommonUtils.isEmpty(uploadFolder1)) {
            uploadFolder1 = CommonUtils.getAppConfigValue("folder.upload");
        }
        return uploadFolder1;
    }

    /**
     * Lay duong dan thu muc upload
     *
     * @return duong dan tuyen doi thu muc upload
     */
    private static String getRequestUploadFolder(int type) throws IOException {

        try {
            //requestFolder = CommonUtils.getAppConfigValue("folder.request.upload");
            String keySaveFile = "";
            if (type == TASK_REQUEST_FILE_TYPE) {
                keySaveFile = FunctionCommon.getPropertiesValue("storage_file_attachment");
            } else if (type == DOC_TEXT_FILE_COMMENT_SIGN) {
                keySaveFile = FunctionCommon.getPropertiesValue("storageName");
            } else if (type == TASK_ATTACHMENT_FILE_TYPE) {
                keySaveFile = "folder.upload";
            } else if (type == DOC_TEXT_FILE_MEETING_MINUTES
                    || type == DOC_TEXT_FILE_MISSION) {
                keySaveFile = "storage_misson";
            }

            return FunctionCommon.getPropertiesValue(keySaveFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }

    }
    // Thu muc xuat file cong viec ca nhan
    private static String taskExportFolder = "";

    /**
     * Lay thu muc xuat file giao viec
     *
     * @return Duong dan tuong doi thu muc xuat file cong viec ca nhan
     */
    private static synchronized String getTaskExportFolder() {
        if (CommonUtils.isEmpty(taskExportFolder)) {
            taskExportFolder = CommonUtils.getAppConfigValue("folder.export.task");
        }
        return taskExportFolder;
    }
    // Thu muc xuat file danh gia cong viec
    private static String taskRatingExportFolder = "";

    /**
     * Lay thu muc xuat file danh gia cong viec
     *
     * @return
     */
    private static synchronized String getTaskRatingExportFolder() {
        if (CommonUtils.isEmpty(taskRatingExportFolder)) {
            taskRatingExportFolder = CommonUtils.getAppConfigValue("folder.export.taskrating");
        }
        return taskRatingExportFolder;
    }

    /**
     * <b>Lay ten storage tam de luu file</b><br>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @return
     */
    public static synchronized String getTmpStorageName() {
        if (CommonUtils.isEmpty(tmpStorageName)) {
            try {
                tmpStorageName = FunctionCommon.getStorageConfigFile("storageName_saveFileTmp");
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return tmpStorageName;
    }

    /**
     * <b>Lay thu muc storage tam de luu file</b><br>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @return
     */
    private static synchronized String getTmpStorageFolder() {
        if (CommonUtils.isEmpty(tmpStorageFolder)) {
            try {
                tmpStorageFolder = FunctionCommon.getStorageConfigFile(getTmpStorageName());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return tmpStorageFolder;
    }
    // Thu muc anh nhan vien
    private static String staffImageFolder = "";

    /**
     * <b>Lay thu muc anh nhan vien</b><br>
     *
     * @return
     */
    private static synchronized String getStaffImageFolder() {
        if (CommonUtils.isEmpty(staffImageFolder)) {
            staffImageFolder = CommonUtils.getAppConfigValue("folder.image.staff");
        }
        return staffImageFolder;
    }
    // Loai phan ten
    private static final int NAME_PART_TYPE = 1;
    // Loai phan mo rong
    private static final int EXTENSION_PART_TYPE = 2;

    /**
     * Lay ra 1 phan cua ten file (ten hoac phan mo rong)
     *
     * @param type Loai 1: Lay ra ten file 2: Lay ra phan mo rong cua file
     * @param fileName Ten file
     * @return
     */
    public static String getPartOfFileName(int type, String fileName) {

        String result = null;
        // Neu ten file la null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(fileName)) {
            return result;
        }
        // Lay vi tri cuoi cung cua dau .
        // Neu khong co dau . trong ten file (Vi tri = -1)
        // -> Tra ve null
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot <= 0) {
            return result;
        }
        // Lay ra theo loai
        switch (type) {
            // Lay ra phan ten
            case NAME_PART_TYPE:
                result = fileName.substring(0, lastIndexOfDot);
                break;
            // Lay ra phan mo rong
            case EXTENSION_PART_TYPE:
                result = fileName.substring(lastIndexOfDot + 1);
                break;
            default:
                return result;
        }
        return result;
    }

    /**
     * Sinh ten file ngau nhien
     *
     * @param extension Phan mo rong cua ten file
     * @return Tra ve ten file + phan mo rong
     */
    public static String generateRandomFileName(String extension) {

        // Sinh ten ngau nhien
        UUID uuid = UUID.randomUUID();
        String name = uuid.toString();

        // Cong ten vua sinh voi phan mo rong
        name += extension;

        return name;
    }

    /**
     * Lay duong dan an toan<br>
     * + Thay the \\ bang /<br>
     * + Thay the // bang /<br>
     *
     * @param path
     * @return
     */
    public static String getSafePath(String path) {
        if (path == null) {
            return null;
        }
        return path.replace("\\", "/").replace("//", "/");
    }

    /**
     * Lay duong dan tuong doi cua thu muc xuat file
     *
     * @param type Loai thuc muc luu file 1: Lay thu muc luu file giao viec ca
     * nhan dau thang 2: Lay thu muc luu file danh gia cong viec ca nhan cuoi
     * thang
     * @param employeeCode Ma nhan vien
     * @return
     */
    public static String getExportPath(int type, String employeeCode) {

        // Kiem tra ma nhan vien
        if (CommonUtils.isEmpty(employeeCode)) {
            LOGGER.error("getExportPath - Loi employeeCode empty");
            return null;
        }
        String path = null;
        // Lay duong dan export chung theo tung loai
        switch (type) {
            // Lay thu muc luu file giao cong viec ca nhan dau thang
            case TASK_ASSIGNMENT_FILE_TYPE:
                path = getTaskExportFolder();
                break;
            // Lay thu muc luu file danh gia cong viec ca nhan cuoi thang
            case TASK_ASSESSMENT_FILE_TYPE:
                path = getTaskRatingExportFolder();
                break;
            default:
                LOGGER.error("getExportPath - Loi type khong hop le - type = " + type
                        + " not in (1, 2)");
                return path;
        }

        // Lay ky danh gia hien tai
        String currentPerriod = CommonUtils.getCurrentPeriod();

        // Noi ma nhan vien va ky danh gia vao duong dan
        path += _separator + currentPerriod + _separator + employeeCode;
        return path;
    }

    /**
     * <b>Tao duong danh file bao cao</b><br>
     *
     * @author thanght6
     * @since Sep 14, 2016
     * @param type Loai
     * @param employeeCode Ma nhan vien
     * @return
     */
    public static List<String> generateRecordFilePath(int type, String employeeCode) {

        // Lay duong dan thu muc xuat file
        String rootFolder = getStorageExport();
        if (CommonUtils.isEmpty(rootFolder)) {
            LOGGER.error("generateRecordFilePath - Loi lay duong dan thu muc xuat file");
            return null;
        }
        rootFolder = getSafePath(rootFolder);
        // Lay duong dan thu muc luu file tuong ung
        String absolutePath = getExportPath(type, employeeCode);
        if (CommonUtils.isEmpty(absolutePath)) {
            LOGGER.error("generateRecordFilePath - Loi lay thu muc luu file tuong ung");
            return null;
        }
        absolutePath = _separator + absolutePath + _separator;
        absolutePath = getSafePath(absolutePath);
        // Tao thu muc luu file neu chua ton tai
        File folder = new File(rootFolder + absolutePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        // Sinh ten ngau nhien
        String randomFileName = generateRandomFileName(PDF_FILE_EXTENSION);
        if (CommonUtils.isEmpty(randomFileName)) {
            LOGGER.error("generateRecordFilePath - Loi sinh ten ngau nhien");
            return null;
        }
        // Kiem tra ten file moi sinh co ton tai khong
        File file = new File(rootFolder + absolutePath + randomFileName);
        while (file.exists()) {
            randomFileName = "_" + randomFileName;
            file = new File(rootFolder + absolutePath + randomFileName);
        }
        absolutePath += randomFileName;
        List<String> listPath = new ArrayList<>();
        listPath.add(rootFolder);
        listPath.add(absolutePath);
        return listPath;
    }
    // Nhan nguoi giao nhiem vu
    private static final String ASSIGNER_LABEL = "NGƯỜI QUẢN LÝ TRỰC TIẾP";
    // Nhan nguoi danh gia
    private static final String ASSESSOR_LABEL = "NGƯỜI ĐÁNH GIÁ";

    /**
     * <b>Sinh danh sach file cong viec ca nhan theo danh sach nhan vien</b><br>
     *
     * @param type
     * @param assignmentOrg         don vi giao viec/danh gia
     * @param assigner              nguoi giao viec/ danh gia
     * @param listEmployee          danh sach nhan vien nhan viec/duoc danh gia
     * @param period                ky danh gia (yyyyMM)
     * @return Danh sach nhan vien voi file giao viec
     */
    public static List<EntityVhrEmployee> exportListTaskFile(int type,
            EntityVhrOrg assignmentOrg, EntityVhrEmployee assigner,
            List<EntityVhrEmployee> listEmployee, String period) {
        List<EntityVhrEmployee> result = new ArrayList<>();
        // Kiem tra danh sach nhan vien
        //minhnq xoa period != 6
        if (assignmentOrg == null || assigner == null || CommonUtils.isEmpty(listEmployee)
                || CommonUtils.isEmpty(period)) {
            String errorMessage = "";
            if (assignmentOrg == null) {
                errorMessage += "Khong co don vi giao viec/danh gia, ";
            }
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec/danh gia, ";
            }
            if (CommonUtils.isEmpty(listEmployee)) {
                errorMessage += "Khong co danh sach nhan viec duoc nhan viec/duoc danh gia, ";
            }
            //minhnq xoa period != 6
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky danh gia hoac ky danh gia sai dinh dang, ";
            }
            LOGGER.error("exportListTaskFile - Loi du lieu dau vao - " + errorMessage);
            return result;
        }
        EntityVhrEmployee employee;
        String fileName;
        String filePath;
        Long contractTypeId;
        Long collaboratorContractTypeId = getCollaboratorContractTypeId();
        UserDAO userDAO = new UserDAO();
        // HaNH Bo sung them storage
        String storage;
        // End HaNH
        switch (type) {
            // Xuat danh sach file giao cong viec ca nhan dau thang
            case TASK_ASSIGNMENT_FILE_TYPE:
                storage = "task_export";
                for (EntityVhrEmployee enforcer : listEmployee) {
                    contractTypeId = enforcer.getLabourContractTypeId();
                    // Neu khong co chuc danh cua nguoi nhan nhiem vu
                    // -> Lay chuc danh theo id nguoi nhan nhiem vu
                    if (CommonUtils.isEmpty(enforcer.getPosition())) {
                        enforcer.setPosition(userDAO.getPositionById(enforcer.getEmployeeId()));
                    }
                    // Xuat file cho cong tac vien (Hop dong dich vu)
                    if (contractTypeId != null
                            && collaboratorContractTypeId != null
                            && contractTypeId.equals(collaboratorContractTypeId)) {
                        // Gan ten mac dinh cho file
                        fileName = DEFAULT_COLLABORATOR_TASK_ASSIGNMENT_FILE_NAME;
                        filePath = exportTaskAssignmentFile(ReportTemplateManager.TASK_ASSIGNMENT_TEMPLATE_FOR_COLLABORATOR,
                                assignmentOrg, assigner, enforcer, period);
                    } // Xua file cho nhan vien chinh thuc (Hop dong lao dong)
                    else {
                        // Gan ten mac dinh cho file
                        fileName = DEFAULT_EMPLOYEE_TASK_ASSIGNMENT_FILE_NAME;
                        filePath = exportTaskAssignmentFile(ReportTemplateManager
                                .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_1_NEW,
                                assignmentOrg, assigner, enforcer, period);
                    }
                    if (!CommonUtils.isEmpty(filePath)) {
                        employee = new EntityVhrEmployee();
                        employee.setEmployeeId(enforcer.getEmployeeId());
                        employee.setFullName(enforcer.getFullName());
                        employee.setTaskFileName(fileName);
                        employee.setTaskFilePath(filePath);
                        employee.setTaskStorage(storage);
                        result.add(employee);
                    }
                }
                break;
            // Xuat danh sach file danh gia cong viec ca nhan cuoi thang
            case TASK_ASSESSMENT_FILE_TYPE:
                storage = "task_export";
                for (EntityVhrEmployee enforcer : listEmployee) {
                    // Neu khong co chuc danh cua nguoi nhan nhiem vu
                    // -> Lay chuc danh theo id nguoi nhan nhiem vu
                    if(enforcer == null){
                        continue;
                    }
                    if (CommonUtils.isEmpty(enforcer.getPosition())) {
                        enforcer.setPosition(userDAO.getPositionById(enforcer.getEmployeeId()));
                    }
                    if (CommonUtils.isEmpty(enforcer.getEmployeeCode())) {
                        employee = userDAO.getEmployeeById(enforcer.getEmployeeId());
                        if (employee != null) {
                            enforcer.setEmployeeCode(employee.getEmployeeCode());
                        }
                    }
                    fileName = DEFAULT_TASK_ASSESSMENT_FILE_NAME;
                    int length = 0;
                    if(listEmployee.get(0).getListPerformTask()!=null){
                        if(listEmployee.get(0).getListPerformTask().get(0)!=null 
                                && listEmployee.get(0).getListPerformTask().get(0).getLength()!= null){
                            length = listEmployee.get(0).getListPerformTask().get(0).getLength();
                        }else{
                            length = 0;
                        }
                    }
                    switch (length) {
                        case 1:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_1,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        case 2:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_2,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        case 3:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_3,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        case 4:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_4,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        case 5:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_5,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        // Datdc Nang cap 
                        case 6:
                            filePath = exportTaskAssignmentFileUpdate(ReportTemplateManager
                                    .TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_6,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                        default:
                            filePath = exportTaskAssignmentFile(ReportTemplateManager
                                    .TASK_ASSESSMENT_TEMPLATE,
                                    assignmentOrg, assigner, enforcer, period);
                            break;
                    }
                    
                    if (!CommonUtils.isEmpty(filePath)) {
                        employee = new EntityVhrEmployee();
                        employee.setEmployeeId(enforcer.getEmployeeId());
                        employee.setFullName(enforcer.getFullName());
                        employee.setTaskFileName(fileName);
                        employee.setTaskFilePath(filePath);
                        employee.setTaskStorage(storage);
                        result.add(employee);
                    }
                }
                break;
        }

        return result;
    }

    /**
     * <b>Xuat file giao viec</b>
     *
     * @author thanght6
     * @since Sep 12, 2016
     * @param templateName Ten template
     * @param assignmentOrg Don vi giao viec
     * @param assigner Nguoi ky
     * @param enforcer Nguoi thuc hien
     * @param period Ky danh gia (MM/yyyy)
     * @return Duong dan tuong doi cua file vua sinh
     */
    public static String exportTaskAssignmentFile(String templateName,
            EntityVhrOrg assignmentOrg, EntityVhrEmployee assigner,
            EntityVhrEmployee enforcer, String period) {

        // Kiem tra du lieu dau vao
        if (CommonUtils.isEmpty(templateName) || assignmentOrg == null
                || assigner == null || enforcer == null
                || CommonUtils.isEmpty(enforcer.getListPerformTask())
                || CommonUtils.isEmpty(period)) {
            String errorMessage = "";
            if (CommonUtils.isEmpty(templateName)) {
                errorMessage += "Khong co ten file mau, ";
            }
            if (assignmentOrg == null) {
                errorMessage += "Khong co don vi giao viec, ";
            }
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec, ";
            }
            if (enforcer == null) {
                errorMessage += "Khong co nguoi thuc hien, ";
            }
            if (enforcer != null && CommonUtils.isEmpty(enforcer.getListPerformTask())) {
                errorMessage += "Khong co danh sach cong viec, ";
            }
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky danh gia, ";
            }
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi du lieu dau "
                    + "vao khong hop le - " + errorMessage);
            return null;
        }
        List<EntityTask> listTask = enforcer.getListPerformTask();
        TaskDAO taskDAO = new TaskDAO();
        taskDAO.getTaskResult(listTask);
        // Lay danh sach cong viec phe duyet
        Long taskId;
        Long approvalState;
        List<Long> listApprovedTaskId = new ArrayList<>();
        List<EntityTask> listApprovedTask = new ArrayList<>();
        for (EntityTask task : listTask) {
            taskId = task.getTaskId();
            approvalState = task.getApprovalState();
            if (taskId != null && !taskId.equals(0L)
                    && (Constants.TASK_APPROVAL.STATE.APPROVED.equals(approvalState) || approvalState==null)) {
                listApprovedTaskId.add(taskId);
                listApprovedTask.add(task);
            }
        }
        // Lay nguon goc cong viec
        Map<Long, String> hmSourceDescription = taskDAO.getSourceDescriptionForListTask(listApprovedTaskId);
        // Gan mo ta nguon goc cho tung cong viec
        String sourceDescription;
        for (EntityTask task : listApprovedTask) {
            sourceDescription = hmSourceDescription.get(task.getTaskId());
            if (sourceDescription == null) {
                sourceDescription = "";
            }
            task.setSourceDescription(sourceDescription);
        }
        // Datdc bo check cong viec theo luong cu
        // Phan biet cong viec chuc nang vs cong viec ne nep
        //List<EntityTask> listFunctionalTask = new ArrayList<>();
        //List<EntityTask> listRoutineTask = new ArrayList<>();
        //TaskUtils.distinguishTask(listApprovedTask, listFunctionalTask, listRoutineTask);
        // Neu chua co cong viec ne nep -> Sinh cong viec ne nep
        //if (CommonUtils.isEmpty(listRoutineTask)) {
        //    listRoutineTask.addAll(TaskUtils.generateRoutineTasks(assignmentOrg
        //            .getSysOrganizationId(), period, null, null));
        //}
        // Tao danh sach cong viec giao cho nhan vien co thu tu cong viec chuc nang truoc
        //List<EntityTask> listAssignedTask = new ArrayList<>();
        //listAssignedTask.addAll(listFunctionalTask);
        //listAssignedTask.addAll(listRoutineTask);
        if (CommonUtils.isEmpty(listApprovedTask)) {
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi khong co cong"
                    + " viec giao di");
            return null;
        }
        // Tao duong dan luu file
        List<String> listPath = generateRecordFilePath(TASK_ASSIGNMENT_FILE_TYPE,
                enforcer.getEmployeeCode());
        if (CommonUtils.isEmpty(listPath)) {
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi tao duong "
                    + "dan luu file bao cao");
            return null;
        }
        String reportFilePath = listPath.get(0) + listPath.get(1);
        // Lay file template bao cao
        // Datdc thay listFunctionalTask bang listApprovedTask
        InputStream template = ReportTemplateManager.getTemplate(templateName);
        if (exportTaskReportToPdfFile(template, assignmentOrg, period, assigner,
                enforcer, listApprovedTask, 0, null, reportFilePath, ASSIGNER_LABEL)) {
            return listPath.get(1);
        }
        return null;
    }

    /**
     * <b>Xuat file danh gia cong viec ca nhan cuoi thang</b><br>
     *
     * @param assessmentOrg Don vi danh gia
     * @param assigner Nguoi danh gia
     * @param enforcer Nguoi duoc danh gia
     * @param period Ky danh gia (MM/yyyy)
     * @return
     */
    public static String exportTaskAssessmentFile(EntityVhrOrg assessmentOrg,
            EntityVhrEmployee assigner, EntityVhrEmployee enforcer, String period) {

        // Kiem tra du lieu dau vao
        if (assigner == null || enforcer == null
                || CommonUtils.isEmpty(enforcer.getListPerformTask())
                || CommonUtils.isEmpty(period)) {
            String errorMessage = "";
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec, ";
            }
            if (enforcer == null) {
                errorMessage += "Khong co nguoi thuc hien, ";
            }
            if (enforcer != null && CommonUtils.isEmpty(enforcer.getListPerformTask())) {
                errorMessage += "Khong co danh sach cong viec, ";
            }
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky danh gia, ";
            }
            LOGGER.error("exportTaskAssessmentPDFFile - Loi du lieu dau "
                    + "vao khong hop le - " + errorMessage);
            return null;
        }
        List<EntityTask> listTask = enforcer.getListPerformTask();
        TaskDAO taskDAO = new TaskDAO();
        taskDAO.getTaskResult(listTask);
        // Lay noi dung cong viec do Ipad khong day noi dung cong viec len
        List<Long> listTaskId = new ArrayList<>();
        for (EntityTask task : listTask) {
            if (CommonUtils.isEmpty(task.getContent())) {
                listTaskId.add(task.getTaskId());
            }
        }
        if (!CommonUtils.isEmpty(listTaskId)) {
            List<EntityTask> listTaskContent = taskDAO.getTaskById(listTaskId);
            if (!CommonUtils.isEmpty(listTaskContent)) {
                for (EntityTask task : listTask) {
                    for (EntityTask taskContent : listTaskContent) {
                        if (task.getTaskId() != null
                                && task.getTaskId().equals(taskContent.getTaskId())) {
                            task.setContent(taskContent.getContent());
                            break;
                        }
                    }
                }
            }
        }
        // Phan biet cong viec chuc nang vs cong viec ne nep
        List<EntityTask> listFunctionalTask = new ArrayList<>();
        List<EntityTask> listRoutineTask = new ArrayList<>();
        TaskUtils.distinguishTask(listTask, listFunctionalTask, listRoutineTask);
        // Tao danh sach cong viec da danh gia co thu tu cong viec chung nang truoc
        List<EntityTask> listAssessedTask = new ArrayList<>();
        listAssessedTask.addAll(listFunctionalTask);
        listAssessedTask.addAll(listRoutineTask);
        if (CommonUtils.isEmpty(listAssessedTask)) {
            LOGGER.error("exportTaskAssessmentPDFFile - Loi khong co cong viec "
                    + "duoc danh gia");
            return null;
        }
        double sum = 0;
        double mediumPoint = 0;
        Double ratingPoint;
        // Neu khong co cong viec chuc nang hoac khong co cong viec ne nep
        // -> Khong tinh diem trung binh theo ty le
        if (CommonUtils.isEmpty(listFunctionalTask)
                || CommonUtils.isEmpty(listRoutineTask)) {
            for (EntityTask task : listAssessedTask) {
                // Lay diem danh gia cua tung cong viec
                ratingPoint = task.getRatingPoint();
                // Neu diem danh gia > 0 -> Cong vao tong diem cong viec
                if (ratingPoint != null && ratingPoint > 0) {
                    sum += ratingPoint;
                }
            }
            // Diem trung binh bang tong chia cho so cong viec
            mediumPoint += sum / listAssessedTask.size();
        } // Neu co ca cong viec chuc nang va cong viec ne nep 
        // -> Tinh diem trung binh theo ty le
        else {
            // Lay ty le diem cong viec ne nep theo don vi
            // VD: ratio = 10
            // 10% diem cong viec ne nep
            // 90% diem cong viec chuc nang
            int ratio = taskDAO.getRoutinePointRatioConfig(assessmentOrg.getSysOrganizationId());
            // Duyet tung cong viec chuc nang
            for (EntityTask task : listFunctionalTask) {
                // Lay diem danh gia cua tung cong viec chuc nang
                ratingPoint = task.getRatingPoint();
                // Neu diem danh gia > 0 -> Cong vao tong diem cong viec chuc nang
                if (ratingPoint != null && ratingPoint > 0) {
                    sum += ratingPoint;
                }
            }
            // Tinh diem trung binh cong viec chuc nang theo ty le
            mediumPoint += (sum * (100 - ratio)) / (100 * listFunctionalTask.size());
            // Gan lai tong diem de tinh lai diem trung binh cho cong viec ne nep
            sum = 0;
            // Duyet tung cong viec ne nep
            for (EntityTask task : listRoutineTask) {
                // Lay diem danh gia cua tung cong viec ne nep
                ratingPoint = task.getRatingPoint();
                // Neu diem danh gia > 0 -> Cong vao tong diem cong viec ne nep
                if (ratingPoint != null && ratingPoint > 0) {
                    sum += ratingPoint;
                }
            }
            // Tinh diem trung binh cong viec ne nep theo ty le
            mediumPoint += (sum * ratio) / (100 * listRoutineTask.size());
        }
        //Begin HaNH: Thay doi theo giai phap, hien nay tren giao dien WEB dang lam tron 1 chu so,
        //vi vay tren phieu danh gia cung phai lam tron 1 chu so sau dau phay (.) 
        // Lam tron 1 chu so sau dau cham (.)
        mediumPoint = (double) Math.round(mediumPoint * 10) / 10;
        //End HaNH
        // Xep loai
        String classification = taskDAO.getClassificationByPoint(assessmentOrg
                .getSysOrganizationId(), (long) mediumPoint);
        // Tao duong dan luu file
        List<String> listPath = generateRecordFilePath(TASK_ASSESSMENT_FILE_TYPE,
                enforcer.getEmployeeCode());
        if (CommonUtils.isEmpty(listPath)) {
            LOGGER.error("exportTaskAssessmentPDFFile - Loi tao duong dan luu "
                    + "file bao cao");
            return null;
        }
        String reportFilePath = listPath.get(0) + listPath.get(1);
        // Lay file template bao cao
        InputStream template = ReportTemplateManager.getTemplate(
                ReportTemplateManager.TASK_ASSESSMENT_TEMPLATE);
        if (exportTaskReportToPdfFile(template, assessmentOrg, period, assigner, enforcer, listAssessedTask,
                mediumPoint, classification, reportFilePath, ASSESSOR_LABEL)) {
            return listPath.get(1);
        }
        return null;
    }
    // Cau truc dia diem, thoi gian
    private static final String LOCATION_AND_TIME = "..., ngày %d tháng %d năm %d";

    /**
     * <b>Xuat file bao cao cong viec ra file Pdf</b><br>
     *
     * @author thanght6
     * @since Sep 14, 2016
     * @param template          file mau
     * @param assignmentOrg     don vi giao viec/danh gia
     * @param period            ky giao/danh gia
     * @param assigner          nguoi giao viec
     * @param enforcer          nguoi nhan
     * @param listTask          danh sach cong viec
     * @param mediumPoint       diem trung binh khi xuat file danh gia cong viec
     * @param classification    xep loai khi xuat file danh gia cong viec
     * @param filePath          duong dan luu file bao cao
     * @param searchText
     * @return
     */
    public static boolean exportTaskReportToPdfFile(InputStream template,
            EntityVhrOrg assignmentOrg, String period,
            EntityVhrEmployee assigner, EntityVhrEmployee enforcer, List<EntityTask> listTask,
            double mediumPoint, String classification, String filePath, String searchText) {

        // Kiem tra du lieu dau vao        
        if (template == null || assignmentOrg == null
                || assignmentOrg.getSysOrganizationId() == null
                || CommonUtils.isEmpty(assignmentOrg.getOrgParentName())
                || CommonUtils.isEmpty(assignmentOrg.getName())
                //minhnq bo period != 6
                || CommonUtils.isEmpty(period) 
                || assigner == null || enforcer == null
                || CommonUtils.isEmpty(listTask) || CommonUtils.isEmpty(filePath)) {
            String errorMessage = "";
            if (template == null) {
                errorMessage += "Khong co template, ";
            }
            if (assignmentOrg == null || assignmentOrg.getSysOrganizationId() == null
                    || CommonUtils.isEmpty(assignmentOrg.getOrgParentName())
                    || CommonUtils.isEmpty(assignmentOrg.getName())) {
                errorMessage += "Thieu thong tin don vi danh gia, ";
            }
            //minhnq bo period != 6
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky giao/danh gia, ";
            }
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec, ";
            }
            if (enforcer == null) {
                errorMessage += "Khong co nguoi nhan viec, ";
            }
            if (CommonUtils.isEmpty(listTask)) {
                errorMessage += "Khong co cong viec giao di, ";
            }
            if (CommonUtils.isEmpty(filePath)) {
                errorMessage += "Khong co duong dan file bao cao, ";
            }
            LOGGER.error("exportTaskReportToPdfFile - Loi du lieu dau vao - " + errorMessage);
            return false;
        }
        String parentOrgName = assignmentOrg.getOrgParentName();
        String assignmentOrgName = assignmentOrg.getName();
        // Lay thoi gian hien tai
        Calendar calendar = Calendar.getInstance();
        int currentDate = calendar.get(Calendar.DATE);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        String locationAndTime = String.format(LOCATION_AND_TIME, currentDate,
                currentMonth, currentYear);
        String year = period.substring(0, 4);
        String month = period.substring(4);
        period = month + "/" + year;
        if(period.length()==5){
            period = year;
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("parentOrgName", parentOrgName);
        parameter.put("assignmentOrgName", assignmentOrgName);
        parameter.put("locationAndTime", locationAndTime);
        parameter.put("period", period);
        if (!CommonUtils.isEmpty(period)) {
            if (year.equalsIgnoreCase(period)) {
                parameter.put("period", "Năm " + period);
            } else {
                String headerDate = "Quý " + period;
                parameter.put("period", headerDate);
            }
        } else {
            parameter.put("period", "");
        }
        parameter.put("assignerPosition", assigner.getPosition());
        parameter.put("year", year);
        parameter.put("assignerName", assigner.getFullName());
        parameter.put("receiverName", enforcer.getFullName());
        //minhnq them code
        parameter.put("receiverCode", enforcer.getEmployeeCode());
        if(enforcer.getListPerformTask().get(0).getOrg_name() == null){
            parameter.put("org_name", "");
        } else {
            parameter.put("org_name", enforcer.getListPerformTask().get(0).getOrg_name());
        }
        if(enforcer.getListPerformTask().get(0).getPositionName() == null){
            parameter.put("positionName", "");
        } else {
            parameter.put("positionName", enforcer.getListPerformTask().get(0).getPositionName());
        }
        parameter.put("TC1", enforcer.getListPerformTask().get(0).getTC1());
        parameter.put("TC2", enforcer.getListPerformTask().get(0).getTC2());
        parameter.put("TC3", enforcer.getListPerformTask().get(0).getTC3());
        parameter.put("TC4", enforcer.getListPerformTask().get(0).getTC4());
        parameter.put("TC5", enforcer.getListPerformTask().get(0).getTC5());
        parameter.put("receiverPosition", enforcer.getPosition());
        parameter.put("mediumPoint", mediumPoint);
        DecimalFormat f = new DecimalFormat("0.00");
        if (enforcer.getSumPoint_NV() == null || enforcer.getSumPoint_NV() == 0L) {
            parameter.put("sumPointNV",  "");
        } else {
            parameter.put("sumPointNV",  f.format(enforcer.getSumPoint_NV()));
            
        }
        if (enforcer.getSumPoint_LD() == null || enforcer.getSumPoint_LD() == 0L) {
            parameter.put("sumPointLD",  "");
        } else {
            parameter.put("sumPointLD",  f.format(enforcer.getSumPoint_LD()));
            
        }
        classification = "";
        if (enforcer.getAverageTask() != null) {
            if(enforcer.getAverageTask().getAttitudePoint() == null) {
                enforcer.getAverageTask().setAttitudePoint(0L);
            }
            Double avergeTask;
            String bonus = "";
            if (enforcer.getAverageTask().getAddOrMinus() == 1) {
                avergeTask = enforcer.getSumPoint_LD() + enforcer.getAverageTask().getAttitudePoint();                
            } else {
                avergeTask = enforcer.getSumPoint_LD() - enforcer.getAverageTask().getAttitudePoint();
                if (!enforcer.getAverageTask().getAttitudePoint().equals(0L)) {
                    bonus = String.format(" (Điểm trừ: %d)",
                            enforcer.getAverageTask().getAttitudePoint());
                }
            }
            parameter.put("avergeTask", f.format(avergeTask));
            if (enforcer.getAverageTask().getAttitudeRating()== 1) {
                parameter.put("attitudeRating", "Không đạt" + bonus);
            } else if(enforcer.getAverageTask().getAttitudeRating()== 2) {
                parameter.put("attitudeRating", "Đạt");
            } else if(enforcer.getAverageTask().getAttitudeRating()== 3) {
                parameter.put("attitudeRating", "Tốt");
            }
            parameter.put("commentAverage", enforcer.getAverageTask().getCommentAverage() == null ?
                    "" : enforcer.getAverageTask().getCommentAverage());
            // Lay danh sach ty le
            TaskDAO taskDAO = new TaskDAO();
            List<EntityRatioConfigDetail> listRatio = taskDAO.getListRatioConfigDetail(
                    assignmentOrg.getSysOrganizationId(), 7, "code.ratio.rating.task", null);
            if (!CommonUtils.isEmpty(listRatio)) {
                for (EntityRatioConfigDetail ratioConfig : listRatio) {
                    if (avergeTask >= ratioConfig.getRatioMinPoint()
                            && avergeTask <= ratioConfig.getRatioMaxPoint()) {
                        classification = ratioConfig.getLogicalName();
                    }
                }
            }
        } else {
            parameter.put("avergeTask", "");
            parameter.put("attitudeRating", "");
            parameter.put("commentAverage", "");
        }
        parameter.put("classification", classification);
        
        String workLocation = "";
        Map<String, String> mapLocation = new LinkedHashMap<String, String>();
        // Cap nhat du lieu cong viec
        // Neu du lieu null thi gan bang rong de tranh in ra chu null
        String startTime;
        String endTime;
        for (EntityTask task : listTask) {
//            System.out.print(task.getTaskId() + ",");
            startTime = task.getStartTime();
            endTime = task.getEndTime();
            if (task.getWeight() == null) {
                task.setWeight(1L);
            }
            if (task.getSourceDescription() == null) {
                task.setSourceDescription("");
            }
            if (task.getContent() == null) {
                task.setContent("");
            }
            if (task.getTaskResult() == null) {
                task.setTaskResult("");
            }
            if (task.getApprovalComment() == null) {
                task.setApprovalComment("");
            }
            if (task.getRatingPoint() == null) {
                task.setRatingPoint(0D);
            }
            // Bo gio, phut khoi thoi gian bat dau, thoi gian ket thuc
            if (startTime != null && startTime.trim().length() > 10) {
                startTime = startTime.trim().substring(0, 10);
                task.setStartTime(startTime);
            }
            if (endTime != null && endTime.trim().length() > 10) {
                endTime = endTime.trim().substring(0, 10);
                task.setEndTime(endTime);
            }
            
            if (task.getNV_tc1() == null || task.getNV_tc1().equals("0.0")) {
                task.setNV_tc1("");
            } else {
                task.setNV_tc1(task.getNV_tc1().replace(".0", ""));
            }
            if (task.getNV_tc2() == null || task.getNV_tc2().equals("0.0")) {
                task.setNV_tc2("");
            } else {
                task.setNV_tc2(task.getNV_tc2().replace(".0", ""));
            }
            if (task.getNV_tc3() == null || task.getNV_tc3().equals("0.0")) {
                task.setNV_tc3("");
            } else {
                task.setNV_tc3(task.getNV_tc3().replace(".0", ""));
            }
            if (task.getNV_tc4() == null || task.getNV_tc4().equals("0.0")) {
                task.setNV_tc4("");
            } else {
                task.setNV_tc4(task.getNV_tc4().replace(".0", ""));
            }
            if (task.getNV_tc5() == null || task.getNV_tc5().equals("0.0")) {
                task.setNV_tc5("");
            } else {
                task.setNV_tc5(task.getNV_tc5().replace(".0", ""));
            }
            if (task.getLD_tc1() == null || task.getLD_tc1().equals("0.0")) {
                task.setLD_tc1("");
            } else {
                task.setLD_tc1(task.getLD_tc1().replace(".0", ""));
            }
            if (task.getLD_tc2() == null || task.getLD_tc2().equals("0.0")) {
                task.setLD_tc2("");
            } else {
                task.setLD_tc2(task.getLD_tc2().replace(".0", ""));
            }
            if (task.getLD_tc3() == null || task.getLD_tc3().equals("0.0")) {
                task.setLD_tc3("");
            } else {
                task.setLD_tc3(task.getLD_tc3().replace(".0", ""));
            }
            if (task.getLD_tc4() == null || task.getLD_tc4().equals("0.0")) {
                task.setLD_tc4("");
            } else {
                task.setLD_tc4(task.getLD_tc4().replace(".0", ""));
            }
            if (task.getLD_tc5() == null || task.getLD_tc5().equals("0.0")) {
                task.setLD_tc5("");
            } else {
                task.setLD_tc5(task.getLD_tc5().replace(".0", ""));
            }
            if (task.getNV_td() == null || task.getNV_td().equals("0.0")) {
                task.setNV_td("");
            }
            if (task.getLD_td() == null || task.getLD_td().equals("0.0")) {
                task.setLD_td("");
            }
            
            if (!CommonUtils.isEmpty(task.getWorkLocation())) {
                String location = task.getWorkLocation().trim().replace(" ", "");
                location = FunctionCommon.removeUnsign(location).toLowerCase();
                if (mapLocation.get(location) == null) {
                    mapLocation.put(location, task.getWorkLocation());
                }
            }
        }
        if (mapLocation.size() > 0) {
            for (String str : mapLocation.keySet()) {
                workLocation += mapLocation.get(str) + "; ";
            }
            workLocation = workLocation.substring(0, workLocation.length() - 2);
        }
        parameter.put("workLocation", workLocation);
        
        //minhnq tinh tong percent
        Double sumPercent = 0D;
        for (EntityTask task : listTask) {
         // Datdc Set cho phan tram
            // Do khac kieu du lieu
            if (task.getPercent() == null || task.getPercent() == 0D) {
                task.setPercentExport("0");
            } else {
                task.setPercentExport(String.valueOf(task.getPercent().intValue()));
            }
            sumPercent = sumPercent+ task.getPercent();
        }
        int sum = sumPercent.intValue();
        parameter.put("sumPercent", sum);
        //minhnq end
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listTask);
            JasperPrint print = JasperFillManager.fillReport(template, parameter, dataSource);
            JasperExportManager.exportReportToPdfFile(print, filePath);

            // Them comment vao file de dinh anh chu ky ve sau
            PdfUtils.addSignPlaceHolder(filePath, searchText);
//            System.out.println("Export - filePath: " + filePath);
            return true;
        } catch (JRException ex) {
            LOGGER.error("exportTaskReportToPdfFile - Exception: ", ex);
            return false;
        }
    }
    
    /** File van ban trinh ky */
    public static final int DOC_TEXT_FILE_TYPE = 0;

    /** File giao cong viec ca nhan dau thang */
    public static final int TASK_ASSIGNMENT_FILE_TYPE = 1;

    /** File danh gia cong viec ca nhan cuoi thang */
    public static final int TASK_ASSESSMENT_FILE_TYPE = 2;

    /** File dinh kem cong viec ca nhan */
    public static final int TASK_ATTACHMENT_FILE_TYPE = 3;

    /** File cer */
    public static final int CER_FILE_TYPE = 4;

    /** File van ban trinh ky */
    public static final int TEXT_FILE_TYPE = 5;

    /** File cong van */
    public static final int DOCUMENT_FILE_TYPE = 6;

    /** File dinh kem cong viec ca nhan */
    public static final int TASK_REQUEST_FILE_TYPE = 7;

    /** File dinh kem comment ky */
    public static final int DOC_TEXT_FILE_COMMENT_SIGN = 8;

    /** File anh nhan vien */
    public static final int STAFF_IMAGE_FILE_TYPE = 9;

    /** File dinh kem tao nhiem vu tu cong viec */
    public static final int DOC_TEXT_FILE_MISSION = 10;

    /** File dinh kem tao bien ban hop */
    public static final int DOC_TEXT_FILE_MEETING_MINUTES = 11;

    /** Lay anh theo man hinh Ipad */
    public static final int VIEW_IMAGE_ICON_APP_MOBILE = 12;

    /** Lay anh bao cao van ban ky */
    public static final int VIEW_IMAGE_REPORT_MOBILE = 13;

    /** File bao cao danh gia ki */
    public static final int DOC_TEXT_FILE_KI = 14;

    /** File anh chu ky */
    public static final int SIGNATURE_IMAGE_FILE_TYPE = 15;
    
    /** File van ban doi tac */
    public static final int TEXT_PARTNER_FILE_TYPE = 16;
    
    /**
     * pm1_os20 add File brief document
     */
    public static final int BRIEF_FILE_TYPE = 16;
    
    /** File nhiem vu */
    public static final int MISSION_FILE_TYPE = 18;

    /**
     * <b>Lay file theo loai truyen vao</b>
     *
     * @param type Loai<br>
     * 1: File giao cong viec ca nhan dau thang<br>
     * 2: File danh gia cong viec ca nhan cuoi thang<br>
     * 3: File dinh kem cong viec ca nhan<br>
     * @param filePath
     * @return
     */
    public static File getFileByType(int type, String filePath) {
        File file = null;
        // Kiem tra duog dan file truyen vao (duong dan tuong doi)
        // Neu duong dan file null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(filePath)) {
            LOGGER.error("getFileByType - Duong dan file null hoac rong");
            return file;
        }

        // Duong dan tuyet doi thu muc goc de luu file
        String rootFolder;
        switch (type) {
            // Lay file giao cong viec ca nhan dau thang
            case TASK_ASSIGNMENT_FILE_TYPE:
            // Lay file danh gia cong viec ca nhan cuoi thang
            case TASK_ASSESSMENT_FILE_TYPE:
                // Thu muc goc la thu muc xuat file
                rootFolder = getExportFolder();
                break;
            // Lay file dinh kem cong viec ca nhan
            case TASK_ATTACHMENT_FILE_TYPE:
                // Thu muc goc la thu muc upload
                try {
                    rootFolder = getRequestUploadFolder(type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;
            case TASK_REQUEST_FILE_TYPE:
                // Thu muc goc la thu muc upload
                try {
                    rootFolder = getRequestUploadFolder(type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }

                break;
            case DOC_TEXT_FILE_COMMENT_SIGN:
                // Thu muc goc la thu muc upload
                try {
                    rootFolder = getRequestUploadFolder(type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }

                break;
            //Lay anh the nhan vien
            case STAFF_IMAGE_FILE_TYPE:
                rootFolder = getStaffImageFolder();
                break;
            // Neu loai truyen vao khong thuoc 1 trong cac loai dinh nghia
            // -> Tra ve null
            case DOC_TEXT_FILE_MISSION:
                // Thu muc goc la thu muc upload
                try {
                    rootFolder = getRequestUploadFolder(type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;
            case DOC_TEXT_FILE_MEETING_MINUTES:
                // Thu muc goc la thu muc upload
                try {
                    rootFolder = getRequestUploadFolder(type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;
            case VIEW_IMAGE_ICON_APP_MOBILE:
                // Thu muc goc anh icon app ngay le tet
                try {
                    rootFolder = getPannerImageFolder();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;

            case VIEW_IMAGE_REPORT_MOBILE:
                // Thu muc goc anh icon app ngay le tet
                try {
                    rootFolder = getImageReportFolder();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;
            default:
//                logger.error("getFileByType - type khong hop le | type = " + type);
                return file;
        }

        // Neu thu muc goc null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(rootFolder)) {
            LOGGER.error("getFileByType - rootFolder null hoac rong");
            return file;
        }

        // Neu duong dan tuyet doi thu muc goc voi duong dan tuong doi cua file
        // -> Duong dan tuyet doi cua file
        filePath = rootFolder + _separator + filePath;
        filePath = getSafePath(filePath);

        // Tao doi tuong file theo duong dan
        file = new File(filePath);
        return file;
    }

    /**
     * <b></b> @param filePath
     *
     * @param filePath          duong dan file 
     * @param storage           thu muc luu tru
     * @return
     */
    public static File getFileByType(String filePath, String storage) {
        
        File file = null;
        if (CommonUtils.isEmpty(filePath)) {
            LOGGER.error("getFileByType - Duong dan file null hoac rong");
            return file;
        }
        // Duong dan tuyet doi thu muc goc de luu file
        String rootFolder = null;
        try {
            if (CommonUtils.isEmpty(storage)) {
                storage = "storage_4";
            }
            rootFolder = FunctionCommon.getPropertiesValue(storage);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        // Neu thu muc goc null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(rootFolder)) {
            LOGGER.error("getFileByType - rootFolder null hoac rong");
            return file;
        }
        // Neu duong dan tuyet doi thu muc goc voi duong dan tuong doi cua file
        // -> Duong dan tuyet doi cua file
        filePath = rootFolder + _separator + filePath;
        FunctionCommon.writeLogsNewfile(filePath);
        filePath = getSafePath(filePath);
        // Tao doi tuong file theo duong dan
        file = new File(filePath);
        return file;
    }

    /**
     * <b>Lay dung luong file</b>
     *
     * @author thanght6
     * @since 2015-12-27
     * @param type Loai file<br>
     * 1: File giao cong viec ca nhan dau thang<br>
     * 2: File danh gia cong viec ca nhan cuoi thang thang<br>
     * 3: File dinh kem cong viec ca nhan<br>
     * @param filePath Duong dan file
     * @return
     */
    public static Long getFileSize(int type, String filePath, String storage) {
        Long fileSize = 0L;
        // Neu duong dan file null hoac rong
        // -> Tra ve 0
        if (CommonUtils.isEmpty(filePath)) {
            LOGGER.error("getFileSize - filePath null hoac rong");
            return fileSize;
        }
        // Lay doi tuong file
        // Neu doi tuong file null hoac file khong ton tai
        // -> Tra ve 0
        //HaNH: Neu la phieu giao viec/phieu danh gia thi lay thu muc chua theo storage, khong lay theo type
        File file;
        if (type == FileUtils.TASK_ASSIGNMENT_FILE_TYPE || type == FileUtils.TASK_ASSESSMENT_FILE_TYPE) {

            if (CommonUtils.isEmpty(storage)) {
                storage = getStorageExport();
            } else {
                storage = FunctionCommon.getPropertiesValue(storage);
            }
            String originFilePath = storage + _separator + filePath;
            originFilePath = FileUtils.getSafePath(originFilePath);
            file = new File(originFilePath);
        } else {
            if (CommonUtils.isEmpty(storage)) {
                file = getFileByType(type, filePath);
            } else {
                file = getFileByType(type, filePath, storage);
            }

        }
        if (file == null || !file.exists()) {
            LOGGER.error("getFileSize - file null hoac khong ton tai");
            return fileSize;
        }
        fileSize = file.length();
        return fileSize;
    }

    /**
     * Tao duong dan de luu file da ky Noi them dau _ vao sau ten file goc
     *
     * @param originFilePath Duong dan file goc
     * @return
     */
    public static String generatePathForSignedFile(String originFilePath, String userId) {
        String path = null;
        // Neu duong dan file goc null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(originFilePath)) {
            LOGGER.error("generatePathForSignedFile - originFilePath null hoac rong");
            return path;
        }

        // Dua duong dan ve dinh dang chuan
        originFilePath = getSafePath(originFilePath);

        // Lay ra vi tri dau phan cach cuoi cung
        // Neu khong co dau phan cach trong duong dan (Vi tri = -1)
        // Hoac chi co 1 dau phan cach o dau (Vi tri = 0)
        // -> Tra ve null
        int lastIndexOfSeparator = originFilePath.lastIndexOf(_separator);
        if (lastIndexOfSeparator <= 0) {
            return path;
        }

        // Lay duong dan thu muc luu file goc
        // Neu duong dan thu muc luu file goc la null hoac rong
        // -> Tra ve null
        String relativePath = originFilePath.substring(0, lastIndexOfSeparator);
        if (CommonUtils.isEmpty(relativePath)) {
            return path;
        }

        // Lay ten file goc
        // Neu ten file goc la null hoac rong
        // -> Tra ve null
        String fileName = originFilePath.substring(lastIndexOfSeparator + 1);
        if (CommonUtils.isEmpty(fileName)) {
            return path;
        }

        // Lay phan ten trong ten file goc (Khong co phan mo rong)
        // Neu phan ten la null hoac rong
        // -> Tra ve null
        String namePart = getPartOfFileName(NAME_PART_TYPE, fileName);
        if (CommonUtils.isEmpty(namePart)) {
            return path;
        }

        // Lay phan mo rong trong ten file goc
        // Neu phan mo rong la null hoac rong
        // -> Tra ve null
        String extensionPart = getPartOfFileName(EXTENSION_PART_TYPE, fileName);
        if (CommonUtils.isEmpty(extensionPart)) {
            return path;
        }

        // Noi them dau _ de phan biet file goc voi file da ky
        namePart += "_" + userId;
        path = relativePath + _separator + namePart + "." + extensionPart;
        path = getSafePath(path);
        return path;
    }

    public static String extractFileNameNotExt(String fileName) {
        String[] lstSep = fileName.split(_separator);
        String fileNameNotExt = lstSep[lstSep.length - 1];

        return fileNameNotExt;
    }

    public static boolean checkSafeFileName(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == 0) {
                return false;
            } else if (c == '.') {
                char c2 = input.charAt(i + 1);
                if (c2 == '.') {
                    char c3 = input.charAt(i + 2);
                    if (c3 == '\\' || c3 == '/') {
                        return false;
                    }
                } else if (c2 == '\\' || c2 == '/') {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isAllowedType(String fileName) {
        if (fileName != null && !"".equals(fileName.trim())) {
            // Datdc them dinh dang file eml
            String[] allowedType = {".jpg", ".jpeg", ".png", ".bmp", ".doc",
                ".docx", ".xls", ".xlsx", ".pdf", ".rar", ".zip", ".txt",
                ".pptx", ".ppt", ".gif", ".tif", ".cer", ".crt", ".msg", ".mpp", ".eml"};
            String ext = extractFileExt(fileName).toLowerCase();
            for (String allowedType1 : allowedType) {
                if (allowedType1.equals(ext)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static String extractFileExt(String fileName) {
        int dotPos = fileName.lastIndexOf(".");
        String extension = fileName.substring(dotPos);
        return extension;
    }
    // Tu khoa storage mac dinh
    public static final String DEFAULT_STORAGE_KEY = "storage.default";

    /**
     * <b>Lay storage mac dinh</b><br/>
     *
     * @author thanght6
     * @since May 30, 2016
     * @return
     */
    public static String getDefaultStorage() {
        return CommonUtils.getAppConfigValue(DEFAULT_STORAGE_KEY);
    }
    // Tu khoa folder upload van ban trinh ky tam
    public static final String TMP_TEXT_UPLOAD_FOLDER_KEY = "folder.upload.text.tmp";
    // Thu muc upload van ban trinh ky tam
//    public static String tmpTextUploadFolder;

    /**
     * Lay thu muc upload
     *
     * @return
     */
//    public static String getTmpTextUploadFolder() {
//        if (CommonUtils.isEmpty(tmpTextUploadFolder)) {
//            tmpTextUploadFolder = CommonUtils.getAppConfigValue(TMP_TEXT_UPLOAD_FOLDER_KEY);
//        }
//        return tmpTextUploadFolder;
//    }
    /**
     * Folder upload van ban
     */
    //public static String textUploadFolder;
    /**
     * Lay thu muc upload
     *
     * @return
     */
//    public static String getTextUploadFolder() {
//        if (CommonUtils.isEmpty(textUploadFolder)) {
//            textUploadFolder = CommonUtils.getAppConfigValue("folder.upload.text");
//        }
//        return textUploadFolder;
//    }
    /**
     * Thu muc tai len van ban
     */
//    public static String documentUploadFolder;
    /**
     * Lay thu muc upload
     *
     * @return
     */
//    public static String getDocumentUploadFolder() {
//        if (CommonUtils.isEmpty(documentUploadFolder)) {
//            documentUploadFolder = CommonUtils.getAppConfigValue("folder.upload.document");
//        }
//        return documentUploadFolder;
//    }
    /**
     * <b>Lay duong dan thu muc storage</b></br>
     *
     * @author thanght6
     * @since May 30, 2016
     * @param storageName Ten storage
     * @return
     */
    public static String getStorageFolder(String storageName) {

        // Neu ten storage null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(storageName)) {
            return null;
        }
        return CommonUtils.getAppConfigValue(storageName);
    }

    /**
     * <b>Tao duong dan de upload file van ban theo id van ban</b><br/>
     *
     * @author thanght6
     * @since May 30, 2016
     * @param objectId          id van ban trinh ky/id cong van/ma nhan vien
     * @param type              loai file
     * @return
     */
    public static String generatePathToUploadById(Object objectId, int type) {

        String path = null;
        // Kiem tra dau vao
        if (objectId == null || objectId.toString().trim().length() == 0) {
            LOGGER.error("generatePathToUploadById"
                    + " - Loi du lieu dau vao - objectId null");
            return path;
        }
        // Lay thu muc de upload file van ban
        String uploadFolder = null;
        switch (type) {
            // File van ban trinh ky
            case TEXT_FILE_TYPE:
                uploadFolder = CommonUtils.getAppConfigValue("folder.upload.text");
                break;
            // File cong van
            case DOCUMENT_FILE_TYPE:
                uploadFolder = CommonUtils.getAppConfigValue("folder.upload.document");
                break;
            // File van ban doi tac
            case TEXT_PARTNER_FILE_TYPE:
                uploadFolder = CommonUtils.getAppConfigValue("folder.upload.text_partner");
                break;
            // File nhiem vu
            case MISSION_FILE_TYPE:
                uploadFolder = CommonUtils.getAppConfigValue("folder.upload.mission");
                break;
        }
        if (CommonUtils.isEmpty(uploadFolder)) {
            LOGGER.error("generatePathToUploadById - Loi thu muc upload van ban null hoac rong!");
            return path;
        }

        // Lay doi tuong calendar cua ngay hien tai
        Calendar calendar = Calendar.getInstance();

        // Tao duong dan upload van ban theo cau truc
        // /[FOLDER]/[YEAR]/[MONTH]/[DATE]/[OBJECT_ID]
        path = _separator + uploadFolder
                + _separator + calendar.get(Calendar.YEAR)
                + _separator + (calendar.get(Calendar.MONTH) + 1)
                + _separator + calendar.get(Calendar.DATE)
                + _separator + objectId;
        // Neu la file nhiem vu thi duong dan them [HOUR][MINUTE][SECOND]
        if (type == MISSION_FILE_TYPE) {
            path += _separator + String.format("%02d%02d%02d",
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
        }
        return path;
    }
    
    /**
     * <b>Lay file van ban theo storage va duong dan</b><br>
     *
     * @author thanght6
     * @since Jun 20, 2016
     * @param storage
     * @param filePath
     * @return
     */
    public static File getTextFile(String storage, String filePath) {

        File file = null;
        // Kiem tra dau vao
        try {
            if (CommonUtils.isEmpty(storage) || CommonUtils.isEmpty(filePath)) {
                return file;
            }

            // Lay thu muc storage
            String storageFolder = getStorageFolder(storage);
            if (CommonUtils.isEmpty(storageFolder)) {
                return file;
            }

            // Cong thu muc storage voi duong dan tuong doi cua file
            // -> Duong dan tuyet doi cua file
            String absolutePath = storageFolder + _separator + filePath;
            absolutePath = getSafePath(absolutePath);

            // Khoi tao doi tuong file theo duong dan tuyet doi
            file = new File(absolutePath);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return file;
    }

    /**
     * <b>Copy file</b><br>
     *
     * @author thanght6
     * @since Aug 31, 2016
     * @param is Luong byte vao
     * @param os Luong byte ra
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        while ((read = is.read(bytes)) != -1) {
            os.write(bytes, 0, read);
        }
        os.flush();
        os.close();
    }

    /**
     * <b>Luu file tam</b>
     *
     * @author thanght6
     * @since Aug 29, 2016
     * @param userId Id user
     * @param type Loai file
     * @param fileName Ten file
     * @param is Luong byte cua file
     * @return
     */
    public static EntityFileAttachment saveTmpFile(Long userId, int type,
            String fileName, InputStream is) {

        if (userId == null || CommonUtils.isEmpty(fileName) || is == null) {
            LOGGER.error("saveTmpFile - userId: " + userId + " - Loi du lieu dau vao");
            return null;
        }
        // Lay thu muc tam
        String storageName = getTmpStorageName();
        String storageFolder = getTmpStorageFolder();
        if (CommonUtils.isEmpty(storageFolder)) {
            LOGGER.error("saveTmpFile - userId: " + userId + " - Loi khong lay "
                    + "duoc thu muc tam");
            return null;
        }
        // Lay phan mo rong cua file
        String extension = extractFileExt(fileName);
        if (CommonUtils.isEmpty(extension)) {
            LOGGER.error("saveTmpFile - userId: " + userId + " - Loi ten file "
                    + "khong co phan mo rong");
            return null;
        }
        // Duong dan thu muc luu file
        String parentFolderPath = storageFolder + _separator + userId;
        // Neu thu muc luu file chua ton tai -> tao thu muc
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        // Sinh ten ngau nhien cho file
        String randomFileName = generateRandomFileName(extension);
        // Them thoi gian hien tai vao dau ten file
        Date now = new Date();
        randomFileName = now.getTime() + "_" + randomFileName;
        // Neu file da ton tai thi them dau _ vao dau ten
        File tmpFile = new File(parentFolderPath + _separator + randomFileName);
        while (tmpFile.exists()) {
            randomFileName = "_" + randomFileName;
            tmpFile = new File(parentFolderPath + _separator + randomFileName);
        }
        // Sinh duong dan tuong doi
        String path = userId + _separator + randomFileName;
        OutputStream os;
        try {
            os = new FileOutputStream(tmpFile);
            if (type == TEXT_FILE_TYPE) {
                DES.encrypt(is, os);
            } else {
                copy(is, os);
            }
            EntityFileAttachment attachment = new EntityFileAttachment();
            attachment.setAttachment(path);
            attachment.setStorage(storageName);
            return attachment;
        } catch (IOException ex) {
            LOGGER.error("saveTmpFile - Loi copy file: ", ex);
            return null;
        } catch (Throwable ex) {
            LOGGER.error("saveTmpFile - Loi ma hoa: ", ex);
            return null;
        }
    }

    /**
     * <b>Chen them thong tin vao file cong viec da duoc ky</b><br>
     * 1. Trang phu luc ky<br>
     * 2. Anh chu ky<br>
     * 3. Watermark nguoi doc<br>
     *
     * @param username Ten nguoi dung
     * @param fileId Id file
     * @param file
     * @param tmpPath Duong dan luu file tam
     * @return
     */
    public static boolean insertInfoIntoSignedTaskFile(String username,
            Long fileId, File file, String tmpPath) {

        // Kiem tra dau vao
        if (fileId == null || file == null || !file.exists() || CommonUtils.isEmpty(tmpPath)) {
            LOGGER.error("insertInfoIntoSignedTaskFile - Loi du lieu dau vao");
            return false;
        }
        try {
            // Lay mang byte cua file
            byte[] fileBytes = DownloadFileDocumentDAO.loadFile(file.getPath());
            // Them trang phu luc nguoi ky
            PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
            fileBytes = pdfSignatureUtil.addSignPageToData(fileBytes, tmpPath, null);
            // Lay thong tin nguoi ky
            TaskApprovalDAO taskApprovalDAO = new TaskApprovalDAO();
            EntityTaskApproval taskApproval = taskApprovalDAO.getApproverByFileId(fileId);
            if (taskApproval == null) {
                LOGGER.error("insertInfoIntoSignedTaskFile - Loi lay thong tin nguoi ky");
                return false;
            }
            // Lay thong tin anh chu ky
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            EntityStaffImageSign staffImageSign = staffImageSignDAO.getStaffImageSignByCardId(
                    taskApproval.getApproverCode(), taskApproval.getApproverId(),
                    taskApproval.getCreatedDate());
            // Tao danh sach nguoi ky
            List<EntityTextProcess> listSigner = new ArrayList<>();
            if (staffImageSign == null) {
                LOGGER.info("insertInfoIntoSignedTaskFile - Khong co anh chu ky");
            } else {
                EntityTextProcess signer = new EntityTextProcess();
                signer.setState((long) Constants.Text.State.APPROVED);
                signer.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                signer.setSignImageIndex(1L);
                signer.setNameImageSign(staffImageSign.getName());
                listSigner.add(signer);
            }
            // TungHD
            // DatDC add param for Mark
            pdfSignatureUtil.writeWaterMarkPdf(fileBytes, username, null, listSigner,
                    tmpPath, null, null, null, null, null, null, null, "vi", null, null,
                    null, null, false, null, false);
            return true;
        } catch (Exception ex) {
            LOGGER.error("insertInfoIntoSignedTaskFile - Exception: ", ex);
            return false;
        }
    }

    /**
     * <b>Lay duong dan tuong doi cua file da ky</b>
     *
     * @param originFilePath Duong dan tuong doi cua file goc
     * @param signedFilePath Duong dan tuyet doi cua file ky duoc sinh tu file
     * goc
     * @return
     */
    public static String getRelativePath(String originFilePath, String signedFilePath) {

        // Kiem tra dau vao
        if (CommonUtils.isEmpty(originFilePath) || CommonUtils.isEmpty(signedFilePath)) {
            LOGGER.error("getRelativePath - Loi du lieu dau vao - originFilePath: "
                    + originFilePath + " | signedFilePath: " + signedFilePath);
            return null;
        }
        // Lay thu muc
        int index = originFilePath.lastIndexOf("/");
        if (index == -1) {
            LOGGER.error("getRelativePath - Khong lay duoc vi tri ket thuc thu muc"
                    + " trong duong dan file goc");
            return null;
        }
        String folder = originFilePath.substring(0, index + 1);
        // Lay vi tri thu muc trong duong dan file ky
        index = signedFilePath.lastIndexOf(folder);
        if (index == -1) {
            LOGGER.error("getRelativePath - Khong lay duoc vi tri bat dau thu muc"
                    + " trong duong dan file da ky");
            return null;
        }
        return signedFilePath.substring(index);
    }

    /**
     * <b>Doc cac byte cua file</b>
     *
     * @param filePath Duong dan file
     * @return
     * @throws java.io.IOException
     */
    public static byte[] loadFile(String filePath) throws IOException {
        FileInputStream fis = null;
        byte[] result = null;
        try {
            fis = new FileInputStream(filePath);
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            result = baos.toByteArray();
        } catch (IOException ex) {
            LOGGER.error("loadFile: ", ex);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return result;
    }

    /**
     * Lay thuc muc xuat file Phieu giao viec/Phieu danh gia
     *
     * @author HaNH
     * @return Duong dan tuyet doi thu muc export
     */
    private static synchronized String getStorageExport() {
        if (CommonUtils.isEmpty(exportFolder2)) {
            try {
                exportFolder2 = FunctionCommon.getConfigFile("task_export");
            } catch (Exception e) {
                LOGGER.error("Error getStorageExport:", e);
            }
        }
        return exportFolder2;
    }

    /**
     * <b>Chen them thong tin vao file danh gia ki</b><br>
     * 1. Trang phu luc ky<br>
     * 2. Anh chu ky<br>
     * 3. Watermark nguoi doc<br>
     *
     * @author cuongnv
     * @param username Ten nguoi dung
     * @param fileId Id file
     * @param file
     * @param tmpPath Duong dan luu file tam
     * @return
     */
    public static boolean insertInfoIntoSignedKI(String username,
            Long fileId, File file, String tmpPath) {

        // Kiem tra dau vao
        if (fileId == null || file == null || !file.exists() || CommonUtils.isEmpty(tmpPath)) {
            LOGGER.error("insertInfoIntoSignedKI - Loi du lieu dau vao");
            return false;
        }
        try {
            // Lay mang byte cua file
            byte[] fileBytes = DownloadFileDocumentDAO.loadFile(file.getPath());
            // Them trang phu luc nguoi ky
            PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
            fileBytes = pdfSignatureUtil.addSignPageToData(fileBytes, tmpPath, null);
            // Lay thong tin nguoi ky
            TaskDAO taskDAO = new TaskDAO();
            EntityVhrEmployee vhrEmp = taskDAO.getEmployeeSignKi(fileId);
            if (vhrEmp == null) {
                LOGGER.error("insertInfoIntoSignedKI - Loi lay thong tin nguoi ky");
                return false;
            }
            // Lay thong tin anh chu ky
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            EntityStaffImageSign staffImageSign = staffImageSignDAO.getStaffImageSignByCardId(
                    vhrEmp.getEmployeeCode(), vhrEmp.getEmployeeId(), vhrEmp.getCreatedDate());
            // Tao danh sach nguoi ky
            List<EntityTextProcess> listSigner = new ArrayList<>();
            if (staffImageSign == null) {
                LOGGER.info("insertInfoIntoSignedKI - Khong co anh chu ky");
            } else {
                EntityTextProcess signer = new EntityTextProcess();
                signer.setState((long) Constants.Text.State.APPROVED);
                signer.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                signer.setSignImageIndex(1L);
                signer.setNameImageSign(staffImageSign.getName());
                listSigner.add(signer);
            }
            // TungHD
            // DatDC add param for Mark
            pdfSignatureUtil.writeWaterMarkPdf(fileBytes, username, null, listSigner,
                    tmpPath, null, null, null, null, null, null, null, "vi", null,
                    null, null, null, false, null, false);
            return true;
        } catch (Exception ex) {
            LOGGER.error("insertInfoIntoSignedKI - Exception: ", ex);
            return false;
        }
    }

    /**
     * <b>Doc file theo storage</b>
     *
     * @author cuongnv
     * @since 04/03/2017
     * @param type Loai vb
     * @param filePath Loai vb
     * @param storage Kho luu tru
     * @return
     */
    public static File getFileByType(int type, String filePath, String storage) {

        File file = null;
        // Kiem tra duog dan file truyen vao (duong dan tuong doi)
        // Neu duong dan file null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(filePath)) {
            LOGGER.error("getFileByType - Duong dan file null hoac rong");
            return file;
        }

        // Duong dan tuyet doi thu muc goc de luu file
        String rootFolder;
        switch (type) {
            // Lay file giao cong viec ca nhan dau thang
            case TASK_ASSIGNMENT_FILE_TYPE:
            // Lay file danh gia cong viec ca nhan cuoi thang
            case TASK_ASSESSMENT_FILE_TYPE:
                // Thu muc goc la thu muc xuat file
                if (CommonUtils.isEmpty(storage)) {
                    //Kho luu tru mac dinh neu khong trien khai luu tru storage
                    rootFolder = getExportFolder();

                } else {
                    rootFolder = FunctionCommon.getPropertiesValue(storage);
                }
                break;

            // Lay file dinh kem cong viec ca nhan
            case TASK_ATTACHMENT_FILE_TYPE:
            // Thu muc goc la thu muc upload
            case TASK_REQUEST_FILE_TYPE:
            case DOC_TEXT_FILE_MISSION:
            case DOC_TEXT_FILE_MEETING_MINUTES:
                // Thu muc goc la thu muc upload
                if (CommonUtils.isEmpty(storage)) {
                    //Kho luu tru mac dinh neu khong trien khai luu tru storage
                    try {
                        rootFolder = getRequestUploadFolder(type);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        rootFolder = "";
                    }

                } else {
                    rootFolder = FunctionCommon.getPropertiesValue(storage);
                }

                break;

            //Lay anh the nhan vien
            case STAFF_IMAGE_FILE_TYPE:
                rootFolder = getStaffImageFolder();
                break;
            // Neu loai truyen vao khong thuoc 1 trong cac loai dinh nghia

            case VIEW_IMAGE_ICON_APP_MOBILE:
                // Thu muc goc anh icon app ngay le tet
                try {
                    rootFolder = getPannerImageFolder();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;

            case VIEW_IMAGE_REPORT_MOBILE:
                // Thu muc goc anh icon app ngay le tet
                try {
                    rootFolder = getImageReportFolder();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    rootFolder = "";
                }
                break;
            case DOC_TEXT_FILE_KI:
                if (CommonUtils.isEmpty(storage)) {
                    //Kho luu tru mac dinh neu khong trien khai luu tru storage
                    rootFolder = FunctionCommon.getPropertiesValue("task_export");
                } else {    
                    rootFolder = FunctionCommon.getPropertiesValue(storage);
                }
                break;
            // File anh chu ky
            case SIGNATURE_IMAGE_FILE_TYPE:
                // Neu khong co storage thi lay thu muc luu tru mac dinh
                if (CommonUtils.isEmpty(storage)) {
                    storage = "storage_null";
                }
                rootFolder = getStorageFolder(storage);
                
                break;
            case DOC_TEXT_FILE_COMMENT_SIGN:
                // Thu muc goc la thu muc upload
                if (CommonUtils.isEmpty(storage)) {
                    storage = CommonUtils.getAppConfigValue("storageName");
                }
                rootFolder = getStorageFolder(storage);
                break;
            default:
                LOGGER.error("getFileByType - type khong hop le - type = " + type);
                return file;
        }

        // Neu thu muc goc null hoac rong
        // -> Tra ve null
        if (CommonUtils.isEmpty(rootFolder)) {
            LOGGER.error("getFileByType - Loi rootFolder null hoac rong!");
            return file;
        }
        // Neu duong dan tuyet doi thu muc goc voi duong dan tuong doi cua file
        // -> Duong dan tuyet doi cua file
        filePath = rootFolder + _separator + filePath;
        filePath = getSafePath(filePath);
        // Tao doi tuong file theo duong dan
        file = new File(filePath);
        return file;
    }

    /**
     * <b>Lay thu muc anh panner tet</b><br>
     *
     * @return
     */
    private static synchronized String getPannerImageFolder() {
        if (CommonUtils.isEmpty(pannerImageFolder)) {
            pannerImageFolder = CommonUtils.getAppConfigValue("folder.image.app.icon");
        }
        return pannerImageFolder;
    }

    /**
     * <b>Lay thu muc anh bao cao</b><br>
     *
     * @return
     */
    private static synchronized String getImageReportFolder() {
        if (CommonUtils.isEmpty(imageReportFolder)) {
            imageReportFolder = CommonUtils.getAppConfigValue("pathfileimg_sltc");
        }
        return imageReportFolder;
    }

    /**
     * lay thong tin so trang va kich thuoc file cong van
     *
     * @param storage
     * @param filePath
     * @param userIdVof2
     * @return
     */
    public static String[] getFileAttachmentInfor(String storage, String filePath, Long userIdVof2) {
        String[] infoFile = new String[2];
        Long filesize;
        int numPage;
        String absPath;
        PdfReader pdfReader = null;
        try {
            if (storage != null) {
                absPath = FunctionCommon.getStorageConfigFile(storage) + filePath;
            } else {
                absPath = CommonUtils.getAppConfigValue("document-root-path") + filePath;
            }
            File newFile = new File(absPath);
            if (newFile.exists()) {
                EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                FileInputStream fis = null;
                FileOutputStream fos = null;
                File fileTmp = null;
                FileInputStream fisPdf = null;
                try {
                    String temPath = absPath.substring(0, absPath.length() - 4)
                            + userIdVof2.toString() + "_" + System.currentTimeMillis() + "_tmp_.pdf";
                    fileTmp = new File(temPath);
                    //giai ma ra file tam
                    SecretKey key = ef.getKey();
                    fis = new FileInputStream(absPath);
                    fos = new FileOutputStream(fileTmp);
                    ef.decrypt(key, fis, fos);
                    if (fileTmp.exists()) {
                        filesize = fileTmp.length();
                        fisPdf = new FileInputStream(fileTmp);
                        pdfReader = new PdfReader(fisPdf);
                        numPage = pdfReader.getNumberOfPages();
                        infoFile[1] = String.valueOf(filesize);
                        infoFile[0] = String.valueOf(numPage);
                    }
                } catch (Throwable ex1) {
                    LOGGER.error(ex1.getMessage(), ex1);
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (pdfReader != null) {
                        pdfReader.close();
                    }
                    if (fisPdf != null) {
                        fisPdf.close();
                    }
                    if (fileTmp != null && fileTmp.exists()) {
                        // xóa file temp
                        fileTmp.delete();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return infoFile;
    }

    /**
     * lay thong tin so trang va kich thuoc file van ban trinh ky
     *
     * @param filePath
     * @param textId
     * @param userIdVof2
     * @return
     */
    public static String[] getAttachInfor(String filePath, Long textId, Long userIdVof2) {
        String[] infoFile = new String[2];
        Long filesize;
        int numPage;
        String absPath;
        PdfReader pdfReader = null;
        try {
            String keySaveFileTmp = FunctionCommon.getStorageConfigFile("storageName_saveFileTmp");
            String uploadPathTmp = FunctionCommon.getStorageConfigFile(keySaveFileTmp);
            absPath = uploadPathTmp + _separator + filePath;
            File newFile = new File(absPath);
            if (newFile.exists()) {
                //neu    file tmm tom tai thuc hien  giai  ma file tam
                EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                FileInputStream fis = null;
                FileOutputStream fos = null;
                File fileTmp = null;
                FileInputStream fisPdf = null;
                try {
                    Calendar calendar = Calendar.getInstance();
                    String realUploadPath = _separator + "Text" + _separator + calendar.get(Calendar.YEAR)
                            + _separator
                            + (calendar.get(Calendar.MONTH) + 1)
                            + _separator
                            + calendar.get(Calendar.DATE) + _separator + textId;

                    String uploadPath = uploadPathTmp + realUploadPath;
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }
                    String[] lstSep = filePath.split(_separator);
                    String fileNameNotExt = lstSep[lstSep.length - 1];
                    String temPath = uploadPath + _separator
                            + fileNameNotExt.substring(0, fileNameNotExt.length() - 4)
                            + userIdVof2.toString() + "_" + System.currentTimeMillis() + "_tmp_.pdf";
                    fileTmp = new File(temPath);
                    //giai ma ra file tam
                    SecretKey key = ef.getKey();
                    fis = new FileInputStream(absPath);
                    fos = new FileOutputStream(fileTmp);
                    ef.decrypt(key, fis, fos);
                    if (fileTmp.exists()) {
                        filesize = fileTmp.length();
                        fisPdf = new FileInputStream(fileTmp);
                        pdfReader = new PdfReader(fisPdf);
                        numPage = pdfReader.getNumberOfPages();
                        infoFile[1] = String.valueOf(filesize);
                        infoFile[0] = String.valueOf(numPage);
                    }
                } catch (Throwable ex1) {
                    LOGGER.error(ex1.getMessage(), ex1);
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (pdfReader != null) {
                        pdfReader.close();
                    }
                    if (fisPdf != null) {
                        fisPdf.close();
                    }
                    if (fileTmp != null && fileTmp.exists()) {
                        // xóa file temp
                        fileTmp.delete();
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return infoFile;
    }

    /**
     * lay so luong trang va kich thuoc file cong viec
     *
     * @param filePath
     * @return
     */
    public static String[] getTaskFileAttachInfor(String filePath) {
        String[] infoFile = new String[2];
        Long filesize;
        int numPage;
        PdfReader pdfReader = null;
        FileInputStream fis = null;
        try {
            String uploadPathTmp = FunctionCommon.getConfigFile("folder.export");
            File tmpFile = new File(uploadPathTmp + filePath);
            if (tmpFile.exists()) {
                filesize = tmpFile.length();
                fis = new FileInputStream(tmpFile);
                pdfReader = new PdfReader(fis);
                numPage = pdfReader.getNumberOfPages();
                infoFile[1] = String.valueOf(filesize);
                infoFile[0] = String.valueOf(numPage);
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return infoFile;
    }

    /**
     * <b>Xuat bien ban hop ra file PDF</b><br>
     *
     * @param userGroup user
     * @param meetingMinutes bien ban hop
     * @param isPreview true - xem truoc file
     * @return
     */
    public static EntityFileAttachment exportMeetingMinutesToPdfFile(EntityUserGroup userGroup,
            EntityMeetingMinutes meetingMinutes, boolean isPreview) {

        StringBuilder errorMessage = new StringBuilder("exportMeetingMinutesToPdfFile");
        if (userGroup == null || userGroup.getUserId2() == 0L
                || meetingMinutes == null
                || CommonUtils.isEmpty(meetingMinutes.getListMission())
                || CommonUtils.isEmpty(meetingMinutes.getListStaff())) {
            errorMessage.append(" - Loi du lieu dau vao!");
            LOGGER.error(errorMessage);
            return null;
        }
        Long userId = userGroup.getUserId2();
        errorMessage.append(" - userId: ").append(userId);
        // Tao duong dan luu file tam
        // Lay thu muc tam
        String storageName = getTmpStorageName();
        String storageFolder = getTmpStorageFolder();
        if (CommonUtils.isEmpty(storageFolder)) {
            errorMessage.append(" - Loi lay thu muc tam!");
            LOGGER.error(errorMessage);
            return null;
        }
        Long officePublishedId = null;
        Map<Long, EntityStaff> mapSignerShowSignatureImage = new TreeMap<>();
        List<EntityStaff> listSigner = meetingMinutes.getListStaff();
        for (EntityStaff signer : listSigner) {
            // Lay ra danh sach nguoi ky co hien thi chu ky
            if (signer.getSignImage() != null && signer.getSignImage() > 0L) {
                mapSignerShowSignatureImage.put(signer.getSignLevel(), signer);
            }
            // Lay ra don vi ban hanh
            if (signer.getIsPublic() != null && signer.getIsPublic() == 1) {
                officePublishedId = signer.getGroupId();
            }
        }

        // Lay thong tin don vi ban hanh va don vi cha
        OrgDAO orgDAO = new OrgDAO();
        List<EntityVhrOrg> listOrg = orgDAO.getChildAndParentOrg(userId, officePublishedId);
        if (CommonUtils.isEmpty(listOrg)) {
            errorMessage.append(" - Loi lay thong tin don vi ban hanh va don vi cha!");
            LOGGER.error(errorMessage);
            return null;
        }
        meetingMinutes.setPublishOrg(listOrg.get(0));
        // Ten don vi ban hanh
        String promulgationOrgName = listOrg.get(0).getName();
        String promulgationOrgCode = listOrg.get(0).getCode();
        String parentOrgName = null;
        // Don vi ban hanh co don vi cha
        if (listOrg.size() >= 2) {
            parentOrgName = listOrg.get(1).getName();
        }
        String promulgationSignerPosition = null;
        String beforeSignerPosition = null;
        // Neu chi co 1 nguoi ky hien thi anh chu ky thi chi hien thi anh chu ky ben duoi
        List<Long> listSignLevelShowSignatureImage = new ArrayList<>(mapSignerShowSignatureImage.keySet());
        EntityStaff signer1, signer2;
        List<Long> listStaffIdShowSignatureImage = new ArrayList<>();

        // Neu so nguoi ky hien thi anh chu ky lon hon 1
        if (mapSignerShowSignatureImage.size() > 0) {
            signer1 = mapSignerShowSignatureImage.get(listSignLevelShowSignatureImage.get(0));
            signer1.setSignImage(1L);
            if (!CommonUtils.isEmpty(signer1.getJobTitle())) {
                beforeSignerPosition = signer1.getJobTitle().toUpperCase();
            } else {
                beforeSignerPosition = "PHÊ DUYỆT";
            }
            listStaffIdShowSignatureImage.add(signer1.getStaffId());
            if (mapSignerShowSignatureImage.size() > 1) {
                signer2 = mapSignerShowSignatureImage.get(listSignLevelShowSignatureImage.get(1));
                signer2.setSignImage(2L);
                if (!CommonUtils.isEmpty(signer2.getJobTitle())) {
                    promulgationSignerPosition = signer2.getJobTitle().toUpperCase();
                }
                listStaffIdShowSignatureImage.add(signer2.getStaffId());
            }
        }

        // Sinh duong dan thu muc luu file (VD: /voffice3/storage/Upload/Text/tmp/6485)
        String parentFolderPath = storageFolder + _separator + userId;
        // Neu thu muc luu file chua ton tai -> tao thu muc
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        // Sinh ten ngau nhien cho file
        Date now = new Date();
        String randomFileName = "BBH_" + now.getTime() + PDF_FILE_EXTENSION;
        File desFile = new File(parentFolderPath + _separator + randomFileName);
        // Neu file da ton tai thi them dau gach chan vao dau ten
        while (desFile.exists()) {
            randomFileName = "_" + randomFileName;
            desFile = new File(parentFolderPath + _separator + randomFileName);
        }
        // Sinh duong dan tuyet doi cho file tam chua ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/BBH_1489138337192.pdf)
        String tmpFilePath = parentFolderPath + _separator + randomFileName;
        randomFileName = "encrypted_" + randomFileName;
        // Sinh duong dan tuong doi file ma hoa
        // (VD: 6485/encrypted_BBH_1489138337192.pdf)
        String relativePath = userId + _separator + randomFileName;
        // Sinh duong dan tuyet doi file ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/encrypted_BBH_1489138337192.pdf)
        String encryptedFilePath = parentFolderPath + _separator + randomFileName;

        InputStream template = ReportTemplateManager.getTemplate(
                ReportTemplateManager.MISSION_ASSIGNMENT_TEMPLATE_FROM_MEETING_MINUTES);
        Map<String, Object> params = new HashMap<>();
        params.put("parentOrgName", parentOrgName);
        params.put("promulgationOrgName", promulgationOrgName);
        params.put("promulgationOrgCode", promulgationOrgCode);
        params.put("numberOfSignatureImage", mapSignerShowSignatureImage.size());
        params.put("promulgationSignerPosition", promulgationSignerPosition);
        params.put("conclusionTitle", meetingMinutes.getTitle());
        params.put("conclusionTime", meetingMinutes.getConclusionDate()
                .replace(":", "h").replace(" ", " ngày "));
        params.put("conclusionStaffName", meetingMinutes.getConclusionStaffName());
        params.put("conclusionStaffPosition", meetingMinutes.getConclusionStaffPosition());
        params.put("conclusionOrgName", meetingMinutes.getConclusionOrgName());
        params.put("conclusionTypeName", meetingMinutes.getConclusionTypeName());
        params.put("conclusionTarget", meetingMinutes.getTarget());
        params.put("beforeSignerPosition", beforeSignerPosition);
        params.put("textReceiverPlace", meetingMinutes.getTextReceiverPlace());
        params.put("nameConclude", meetingMinutes.getNameConclude());
        // Gop cac don vi cung thuc hien vao 1 ban ghi
        List<EntityMission> listMission = meetingMinutes.getListMission();
        List<EntityMission> listMissionClone = new ArrayList<>();
        EntityMission missionBefore = null;
        boolean isFirst = true;
        for (EntityMission mission : listMission) {
            // Nhiem vu hien tai trung ten va nguoi giao voi nhiem vu truoc do
            if (missionBefore != null
                    && mission.getMissionName().equals(missionBefore.getMissionName())
                    && mission.getAssignId().equals(missionBefore.getAssignId())) {
                if (isFirst) {
                    missionBefore.setOrgPerformName("- " + missionBefore.getOrgPerformName());
                    isFirst = false;
                }
                missionBefore.setOrgPerformName(missionBefore.getOrgPerformName()
                        + "\n\n- " + mission.getOrgPerformName());
            } else {
                if (missionBefore != null) {
                    listMissionClone.add(missionBefore);
                }
                isFirst = true;
                missionBefore = mission;
            }
        }
        listMissionClone.add(missionBefore);
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    listMissionClone);
            JasperPrint print = JasperFillManager.fillReport(template, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, tmpFilePath);

            // Them note
            if (mapSignerShowSignatureImage.size() > 0) {
                List<String> listKey = new ArrayList<>();
                if (mapSignerShowSignatureImage.size() > 1) {
                    listKey.add("PHÊ DUYỆT");
                }
                listKey.add(beforeSignerPosition);
                PdfUtils.addSignPlaceHolder(tmpFilePath, listKey);
            }

            // Neu la xem truoc file
            // -> Them anh chu ky, watermark
            if (isPreview) {
                // Tao danh sach nguoi ky
                List<EntityTextProcess> listTextProcess = new ArrayList<>();
                if (!CommonUtils.isEmpty(listStaffIdShowSignatureImage)) {
                    // Lay thong tin anh chu ky
                    StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
                    List<EntityStaffImageSign> listStaffImageSign = staffImageSignDAO
                            .getStaffImageSignById(listStaffIdShowSignatureImage);
                    if (!CommonUtils.isEmpty(listStaffImageSign)) {
                        EntityTextProcess textProcess;
                        Long index = 1L;
                        for (Long staffId : listStaffIdShowSignatureImage) {
                            textProcess = new EntityTextProcess();
                            textProcess.setState((long) Constants.Text.State.APPROVED);
                            textProcess.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                            textProcess.setSignImageIndex(index);
                            textProcess.setNameImageSign("default_sign.png");
                            listTextProcess.add(textProcess);
                            index++;
                            for (EntityStaffImageSign staffImageSign : listStaffImageSign) {
                                if (staffId.equals(staffImageSign.getStaffIdVof2())) {
                                    textProcess.setNameImageSign(staffImageSign.getName());
                                    break;
                                }
                            }
                        }
                    }
                }
                // Lay mang byte cua file
                byte[] fileBytes = DownloadFileDocumentDAO.loadFile(tmpFilePath);
                PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
                // TungHD
                // DatDC add param for Mark
                pdfSignatureUtil.writeWaterMarkPdf(fileBytes, userGroup.getCardId()
                        + "_" + userGroup.getName2(), null, listTextProcess,
                        tmpFilePath, null, null, null, null, null, null, null, "vi", 0L,
                        null, null, null, false, null, false);
            } else {

                // Ma hoa file
                FileInputStream is = null;
                FileOutputStream os = null;
                try {
                    is = new FileInputStream(tmpFilePath);
                    os = new FileOutputStream(encryptedFilePath);
                    DES.encrypt(is, os);
                } catch (Exception ex) {
                    errorMessage.append(" - Ma hoa file - Exception!");
                    LOGGER.error(errorMessage, ex);
                    return null;
                } catch (Throwable ex) {
                    errorMessage.append(" - Ma hoa file - Exception!");
                    LOGGER.error(errorMessage, ex);
                    return null;
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException ex) {
                        errorMessage.append(" - Dong luong - Exception!");
                        LOGGER.error(errorMessage, ex);
                    }
                }
                // Xoa file tam
                File tmpFile = new File(tmpFilePath);
                tmpFile.delete();
            }

            EntityFileAttachment fileAttachment = new EntityFileAttachment();
            String attachmentName = FunctionCommon.removeUnsign(meetingMinutes.getTitle());
            // Ten file toi da 100 ky tu
            if (attachmentName.length() > 96) {
                attachmentName = attachmentName.substring(0, 96);
            }
            attachmentName += PDF_FILE_EXTENSION;
            fileAttachment.setName(attachmentName);
            fileAttachment.setFilePath(relativePath);
            fileAttachment.setStorage(storageName);
            fileAttachment.setAttachment(tmpFilePath);
            return fileAttachment;
        } catch (Exception ex) {
            errorMessage.append(" - Exception!");
            LOGGER.error(errorMessage, ex);
            return null;
        }
    }
    
    /**
     * <b>Xuat danh gia ki ra file PDF</b><br>
     *
     * @author pm1_os20
     * @param userGroup user
     * @param listSigner
     * @param titleName
     * @param orgId
     * @param period
     * @param lstKi
     * @param lstTypeTask
     * @param listEmpRating
     * @param isPreview true - xem truoc file
     * @return
     * @since 28/10/2017
     */
    public static EntityFileAttachment exportKiToPdfFile(EntityUserGroup userGroup,
             List<EntityStaff> listSigner, String titleName, Long orgId, String period, List<String> lstKi, List<String> lstTypeTask, List<EntityEmpRating> listEmpRating, boolean isPreview) {

        StringBuilder errorMessage = new StringBuilder("exportKiToPdfFile");
        if (userGroup == null || userGroup.getUserId2() == 0L                
                || CommonUtils.isEmpty(listSigner)) {
            errorMessage.append(" - Loi du lieu dau vao!");
            LOGGER.error(errorMessage);
            return null;
        }
        Long userId = userGroup.getUserId2();
        errorMessage.append(" - userId: ").append(userId);
        // Tao duong dan luu file tam
        // Lay thu muc tam
        String storageName = getTmpStorageName();
        String storageFolder = getTmpStorageFolder();
        if (CommonUtils.isEmpty(storageFolder)) {
            errorMessage.append(" - Loi lay thu muc tam!");
            LOGGER.error(errorMessage);
            return null;
        }
        
        Map<Long, EntityStaff> mapSignerShowSignatureImage = new TreeMap<>();       
        for (EntityStaff signer : listSigner) {
            // Lay ra danh sach nguoi ky co hien thi chu ky
            if (signer.getSignImage() != null && signer.getSignImage() > 0L) {
                mapSignerShowSignatureImage.put(signer.getSignLevel(), signer);
            }
        }

        // Lay thong tin don vi ban hanh va don vi cha
        OrgDAO orgDAO = new OrgDAO();
        List<EntityVhrOrg> listOrg = orgDAO.getChildAndParentOrg(userId, orgId);
        if (CommonUtils.isEmpty(listOrg)) {
            errorMessage.append(" - Loi lay thong tin don vi va don vi cha!");
            LOGGER.error(errorMessage);
            return null;
        }
        //meetingMinutes.setPublishOrg(listOrg.get(0));
        // Ten don vi ban hanh
        String promulgationOrgName = listOrg.get(0).getName();
        //String promulgationOrgCode = listOrg.get(0).getCode();
        String parentOrgName = null;
        // Don vi ban hanh co don vi cha
        if (listOrg.size() >= 2) {
            parentOrgName = listOrg.get(1).getName();
        }
        //String promulgationSignerPosition = null;
        String beforeSignerPosition = "Thủ trưởng đơn vị";
        // Neu chi co 1 nguoi ky hien thi anh chu ky thi chi hien thi anh chu ky ben duoi
//        List<Long> listSignLevelShowSignatureImage = new ArrayList<>(mapSignerShowSignatureImage.keySet());
//        EntityStaff signer1, signer2;
//        List<Long> listStaffIdShowSignatureImage = new ArrayList<>();

        // Neu so nguoi ky hien thi anh chu ky lon hon 1
//        if (mapSignerShowSignatureImage.size() > 0) {
//            signer1 = mapSignerShowSignatureImage.get(listSignLevelShowSignatureImage.get(0));
//            signer1.setSignImage(1L);
//            if (!CommonUtils.isEmpty(signer1.getJobTitle())) {
//                beforeSignerPosition = signer1.getJobTitle().toUpperCase();
//            } else {
//                beforeSignerPosition = "Thủ trưởng đơn vị";
//            }
//            listStaffIdShowSignatureImage.add(signer1.getStaffId());
//            if (mapSignerShowSignatureImage.size() > 1) {
//                signer2 = mapSignerShowSignatureImage.get(listSignLevelShowSignatureImage.get(1));
//                signer2.setSignImage(2L);
//                /*if (!CommonUtils.isEmpty(signer2.getJobTitle())) {
//                    promulgationSignerPosition = signer2.getJobTitle().toUpperCase();
//                }*/
//                listStaffIdShowSignatureImage.add(signer2.getStaffId());
//            }
//        }

        // Sinh duong dan thu muc luu file (VD: /voffice3/storage/Upload/Text/tmp/6485)
        String parentFolderPath = storageFolder + _separator + userId;
        // Neu thu muc luu file chua ton tai -> tao thu muc
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        // Sinh ten ngau nhien cho file
        Date now = new Date();
        String randomFileName = "KI_" + now.getTime() + PDF_FILE_EXTENSION;
        File desFile = new File(parentFolderPath + _separator + randomFileName);
        // Neu file da ton tai thi them dau gach chan vao dau ten
        while (desFile.exists()) {
            randomFileName = "_" + randomFileName;
            desFile = new File(parentFolderPath + _separator + randomFileName);
        }
        // Sinh duong dan tuyet doi cho file tam chua ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/BBH_1489138337192.pdf)
        String tmpFilePath = parentFolderPath + _separator + randomFileName;
        randomFileName = "encrypted_" + randomFileName;
        // Sinh duong dan tuong doi file ma hoa
        // (VD: 6485/encrypted_BBH_1489138337192.pdf)
        String relativePath = userId + _separator + randomFileName;
        // Sinh duong dan tuyet doi file ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/encrypted_BBH_1489138337192.pdf)
        String encryptedFilePath = parentFolderPath + _separator + randomFileName;

        InputStream template = ReportTemplateManager.getTemplate(ReportTemplateManager.EMP_RATING_TEMPLATE_FROM_KI);
        Map<String, Object> params = new HashMap<>();
        try {
            params.put("parentOrgName", parentOrgName);            
            params.put("assignmentOrgName", promulgationOrgName);
            params.put("period", period);
            params.put("receiverName", "");
            params.put("receiverPosition", "");
            params.put("rankPercen1",lstKi.get(0));
            params.put("rank1", Integer.valueOf(lstKi.get(1)));
            params.put("rankPercen2", lstKi.get(2));
            params.put("rank2", Integer.valueOf(lstKi.get(3)));
            params.put("rankPercen3", lstKi.get(4));
            params.put("rank3", Integer.valueOf(lstKi.get(5)));
            params.put("rankPercen4", lstKi.get(6));
            params.put("rank4", Integer.valueOf(lstKi.get(7)));
            params.put("rankPercen5", lstKi.get(8));
            params.put("rank5", Integer.valueOf(lstKi.get(9)));
            params.put("rankPercen6", lstKi.get(10));
            params.put("rank6", Integer.valueOf(lstKi.get(11)));
            //params.put("sumRankPercen", lstKi.get(12));
            params.put("sumRank", Integer.valueOf(lstKi.get(12)));
            params.put("rankKIPercen1", lstTypeTask.get(0));
            params.put("rankKI1", Integer.valueOf(lstTypeTask.get(1)));
            params.put("rankKIPercen2", lstTypeTask.get(2));
            params.put("rankKI2", Integer.valueOf(lstTypeTask.get(3)));
            params.put("rankKIPercen3", lstTypeTask.get(4));
            params.put("rankKI3", Integer.valueOf(lstTypeTask.get(5)));
            params.put("rankKIPercen4", lstTypeTask.get(6));
            params.put("rankKI4", Integer.valueOf(lstTypeTask.get(7)));
            params.put("rankKIPercen5", lstTypeTask.get(8));
            params.put("rankKI5", Integer.valueOf(lstTypeTask.get(9)));
            params.put("rankKIPercen6", lstTypeTask.get(10));
            params.put("rankKI6", Integer.valueOf(lstTypeTask.get(11)));
            params.put("sumRankKIPercen", lstTypeTask.get(12));
            params.put("sumRankKI", Integer.valueOf(lstTypeTask.get(13)));
            params.put("month", period.substring(4,6));
            params.put("year", period.substring(0,4));
         } catch (Exception ex) {
            errorMessage.append(" - Du lieu input loi Exception!");
            LOGGER.error(errorMessage, ex);
            return null;
         }
                
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listEmpRating);
            JasperPrint print = JasperFillManager.fillReport(template, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, tmpFilePath);
            PdfUtils.addSignPlaceHolder(tmpFilePath, beforeSignerPosition);
//            // Them note
//            if (mapSignerShowSignatureImage.size() > 0) {
//                List<String> listKey = new ArrayList<>();
//                if (mapSignerShowSignatureImage.size() > 1) {
//                    listKey.add("Thủ trưởng đơn vị");
//                }
//                listKey.add(beforeSignerPosition);
//                PdfUtils.addSignPlaceHolder(tmpFilePath, listKey);
//            }

            // Neu la xem truoc file
            // -> Them anh chu ky, watermark
            if (isPreview) {
                // Tao danh sach nguoi ky
                List<EntityTextProcess> listTextProcess = new ArrayList<>();
//                    // Lay thong tin anh chu ky
                StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
                EntityTextProcess textProcess;
//                        Long index = 1L;
                for (EntityStaff staff : listSigner) {
                    textProcess = new EntityTextProcess();
                    textProcess.setState((long) Constants.Text.State.APPROVED);
                    textProcess.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                    textProcess.setSignImageIndex(staff.getSignImage());
                    textProcess.setSignImageId(staff.getSignImageId());
                    EntityStaffImageSign entityImage = staffImageSignDAO.getSignatureImageById(staff.getSignImageId());
                    if (entityImage != null) {
                        textProcess.setNameImageSign(entityImage.getName());
                    } else {
                        textProcess.setNameImageSign("default_sign.png");
                    }
                    listTextProcess.add(textProcess);
                }
                // Lay mang byte cua file
                byte[] fileBytes = DownloadFileDocumentDAO.loadFile(tmpFilePath);
                PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
                // TungHD
                // DatDC add param for Mark
                pdfSignatureUtil.writeWaterMarkPdf(fileBytes, userGroup.getCardId()
                        + "_" + userGroup.getName2(), null, listTextProcess,
                        tmpFilePath, null, null, null, null, null, null, null, "vi", 0L,
                        null, null, null, false, null, false);
            } else {

                // Ma hoa file
                FileInputStream is = null;
                FileOutputStream os = null;
                try {
                    is = new FileInputStream(tmpFilePath);
                    os = new FileOutputStream(encryptedFilePath);
                    DES.encrypt(is, os);
                } catch (Exception ex) {
                    errorMessage.append(" - Ma hoa file - Exception!");
                    LOGGER.error(errorMessage, ex);
                    return null;
                } catch (Throwable ex) {
                    errorMessage.append(" - Ma hoa file - Exception!");
                    LOGGER.error(errorMessage, ex);
                    return null;
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException ex) {
                        errorMessage.append(" - Dong luong - Exception!");
                        LOGGER.error(errorMessage, ex);
                    }
                }
                // Xoa file tam
                File tmpFile = new File(tmpFilePath);
                tmpFile.delete();
            }

            EntityFileAttachment fileAttachment = new EntityFileAttachment();
            String attachmentName = FunctionCommon.removeUnsign(titleName);
            // Ten file toi da 100 ky tu
            if (attachmentName.length() > 96) {
                attachmentName = attachmentName.substring(0, 96);
            }
            attachmentName += PDF_FILE_EXTENSION;
            fileAttachment.setName(attachmentName);
            fileAttachment.setFilePath(relativePath);
            fileAttachment.setStorage(storageName);
            fileAttachment.setAttachment(tmpFilePath);
            return fileAttachment;
        } catch (Exception ex) {
            errorMessage.append(" - Exception!");
            LOGGER.error(errorMessage, ex);
            return null;
        }
    }

    /**
     * <b>Storage luu anh chu ky</b>
     */
    //public static String signatureImageStorage;
    /**
     * <b>Lay storage luu anh chu ky</b>
     *
     * @return
     */
//    public static String getSignatureImageStorage() {
//        
//        if (CommonUtils.isEmpty(signatureImageStorage)) {
//            signatureImageStorage = CommonUtils.getAppConfigValue("storage.image.signature");
//        }
//        return signatureImageStorage;
//    }
    /**
     * Thu muc luu anh chu ky
     */
//    public static String signatureImageFolder;
    /**
     * <b>Lay thu muc luu anh chu ky</b>
     *
     * @return
     */
//    public static String getSignatureImageFolder() {
//        
//        if (CommonUtils.isEmpty(signatureImageFolder)) {
//            signatureImageFolder = CommonUtils.getAppConfigValue("pathUploadImageSinger");
//        }
//        return signatureImageFolder;
//    }
    /**
     * <b>Chuyen anh chu ky tu thu muc tam vao thu muc that</b><br>
     *
     * @param userId id user
     * @param listStaffImageSign danh sach anh chu ky
     * @return
     */
    public static boolean moveSignatureImage(Long userId,
            List<EntityStaffImageSign> listStaffImageSign) {

        // Kiem tra dau vao
        if (userId == null || CommonUtils.isEmpty(listStaffImageSign)) {
            LOGGER.error("moveSignatureImage - Loi du lieu dau vao!");
            return false;
        }
        String storage = CommonUtils.getAppConfigValue("storage.image.signature");
        // Lay thu muc storage luu anh chu ky
        String storageFolder = getStorageFolder(storage);
        if (CommonUtils.isEmpty(storageFolder)) {
            LOGGER.error("moveSignatureImage - Loi lay thu muc luu anh chu ky!");
            return false;
        }
        // Thu muc storage tai len anh chu ky tam
        String tmpUploadStorage, tmpUploadStorageFolder;
        // Lay thu muc upload tam
        String tmpUploadFolder = CommonUtils.getAppConfigValue(TMP_TEXT_UPLOAD_FOLDER_KEY);
        String realPath;
        File srcFile, destFile;
        String signatureImageName;
        EntityStaffImageSign staffImageSign;
        // Duyet danh sach anh chu ky
        for (Iterator<EntityStaffImageSign> iterator = listStaffImageSign.iterator(); iterator.hasNext();) {
            staffImageSign = iterator.next();
            try {
                tmpUploadStorage = staffImageSign.getStorage();
                tmpUploadStorageFolder = getStorageFolder(tmpUploadStorage);
                srcFile = new File(tmpUploadStorageFolder + _separator + tmpUploadFolder
                        + _separator + staffImageSign.getPath());
                // Tao ten file luu vao thu muc
                signatureImageName = staffImageSign.generateName();
                realPath = _separator + CommonUtils.getAppConfigValue("pathUploadImageSinger")
                        + _separator + signatureImageName;
                destFile = new File(storageFolder + realPath);
                // Neu file da ton tai
                if (destFile.exists()) {
                    destFile.delete();
                }
                org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
                staffImageSign.setName(signatureImageName);
                staffImageSign.setPath(realPath);
                staffImageSign.setStorage(storage);
            } catch (Exception ex) {
                LOGGER.error("moveSignatureImage - userId: " + userId + " - Exception!", ex);
                iterator.remove();
            }
        }
        return !CommonUtils.isEmpty(listStaffImageSign);
    }
    
    /**
     * <b>Chuyen anh chu ky tu thu muc tam vao thu muc that</b><br>
     *
     * @param userId id user
     * @param listStaffImageSign danh sach anh chu ky
     * @return
     */
	public static boolean addImageOrg(Long userId, EntityImageOrg listStaffImageSign) {

		// Kiem tra dau vao
		if (userId == null || listStaffImageSign == null) {
			LOGGER.error("moveSignatureImage - Loi du lieu dau vao!");
			return false;
		}
		String storage = CommonUtils.getAppConfigValue("storage.image.signature");
		// Lay thu muc storage luu anh chu ky
		String storageFolder = getStorageFolder(storage);
		if (CommonUtils.isEmpty(storageFolder)) {
			LOGGER.error("moveSignatureImage - Loi lay thu muc luu anh chu ky!");
			return false;
		}
		// Thu muc storage tai len anh chu ky tam
		String tmpUploadStorage, tmpUploadStorageFolder;
		// Lay thu muc upload tam
		String tmpUploadFolder = CommonUtils.getAppConfigValue(TMP_TEXT_UPLOAD_FOLDER_KEY);
		String realPath;
		File srcFile, destFile;
		String signatureImageName;
		// Duyet danh sach anh chu ky
		try {
			tmpUploadStorage = listStaffImageSign.getStorage();
			tmpUploadStorageFolder = getStorageFolder(tmpUploadStorage);
			srcFile = new File(
					tmpUploadStorageFolder + _separator + tmpUploadFolder + _separator + listStaffImageSign.getPath());
			// Tao ten file luu vao thu muc
			signatureImageName = listStaffImageSign.getName();
			realPath = _separator + CommonUtils.getAppConfigValue("pathUploadImageSinger") + _separator
					+ System.currentTimeMillis() + "_" + signatureImageName;
            realPath = ConverterUtil.toUnsignedChar(realPath).replaceAll(" ", "_");
			destFile = new File(storageFolder + realPath);
			// Neu file da ton tai
			if (destFile.exists()) {
				destFile.delete();
			}
			org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
			listStaffImageSign.setName(signatureImageName);
			listStaffImageSign.setPath(realPath);
			listStaffImageSign.setStorage(storage);
		} catch (Exception ex) {
			LOGGER.error("moveSignatureImage - userId: " + userId + " - Exception!", ex);
		}
		return listStaffImageSign != null;
	}

    /**
     * <b>Thay doi ten anh chu ky neu ngay bat dau hieu luc thay doi</b>
     *
     * @param newStaffImageSign doi tuong anh chu ky moi
     * @return
     */
    public static boolean renameSignatureImage(EntityStaffImageSign newStaffImageSign) {

        // Kiem tra thong tin anh chu ky
        if (newStaffImageSign == null || newStaffImageSign.getStaffImageSignId() == null) {
            LOGGER.error("renameSignatureImage - Loi du lieu dau vao!");
            return false;
        }
        // Neu ngay bat dau co hieu luc bi thay thoi thi thay doi ten anh + duong dan
        StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
        EntityStaffImageSign oldStaffImageSign = staffImageSignDAO.getSignatureImageById(
                newStaffImageSign.getStaffImageSignId());
        if (oldStaffImageSign == null) {
            LOGGER.error("renameSignatureImage - Loi lay thong tin anh chu ky cu!");
            return false;
        }
        // Kiem tra ngay hieu luc cu voi ngay hieu luc moi
        if (!CommonUtils.isEmpty(oldStaffImageSign.getFromDateActive())
                && !CommonUtils.isEmpty(newStaffImageSign.getFromDateActive())
                && !oldStaffImageSign.getFromDateActive().contains(newStaffImageSign.getFromDateActive())) {
            try {
                // Lay thu muc storage
                String storage = oldStaffImageSign.getStorage();
                if (CommonUtils.isEmpty(storage)) {
                    storage = CommonUtils.getAppConfigValue("storage.image.signature");
                }
                String storageFolder = getStorageFolder(storage);
                String newName = newStaffImageSign.generateName();
                String newPath = _separator + CommonUtils.getAppConfigValue("pathUploadImageSinger")
                        + _separator + newName;
                File oldFile = new File(storageFolder + oldStaffImageSign.getPath());
                File newFile = new File(storageFolder + newPath);
                org.apache.commons.io.FileUtils.moveFile(oldFile, newFile);
                newStaffImageSign.setPath(newPath);
                newStaffImageSign.setName(newName);
                return true;
            } catch (Exception ex) {
                LOGGER.error("renameSignatureImage - Loi doi ten file!", ex);
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * <b>Xem truoc file phu luc chat</b><br>
     *
     * @param userGroup doi tuong user
     * @param text doi tuong van ban
     * @return
     */
    public static EntityFileAttachment exportAppendixChatToPdfFile(
            EntityUserGroup userGroup, EntityText text) {

        StringBuilder errorMessage = new StringBuilder("exportAppendixChatToPdfFile");
        if (userGroup == null || text == null || CommonUtils.isEmpty(text.getListMessage())) {
            errorMessage.append(" - Loi du lieu dau vao!");
            LOGGER.error(errorMessage);
            return null;
        }
        errorMessage.append(" - userId: ").append(userGroup.getUserId2());
        // Tao duong dan luu file tam
        // Lay thu muc tam
        String storageName = getTmpStorageName();
        String storageFolder = getTmpStorageFolder();
        if (CommonUtils.isEmpty(storageFolder)) {
            errorMessage.append(" - Loi lay thu muc tam!");
            LOGGER.error(errorMessage);
            return null;
        }
        // Sinh duong dan thu muc luu file (VD: /voffice3/storage/Upload/Text/tmp/6485)
        String parentFolderPath = storageFolder + _separator + userGroup.getUserId2();
        // Neu thu muc luu file chua ton tai -> tao thu muc
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        // Sinh ten ngau nhien cho file
        Date now = new Date();
        String randomFileName = "PLC_" + now.getTime() + PDF_FILE_EXTENSION;
        File desFile = new File(parentFolderPath + _separator + randomFileName);
        // Neu file da ton tai thi them dau gach chan vao dau ten
        while (desFile.exists()) {
            randomFileName = "_" + randomFileName;
            desFile = new File(parentFolderPath + _separator + randomFileName);
        }
        // Sinh duong dan tuyet doi cho file tam
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/PLC_1489138337192.pdf)
        String tmpFilePath = parentFolderPath + _separator + randomFileName;
        randomFileName = "encrypted_" + randomFileName;
        // Sinh duong dan tuong doi file
        // (VD: 6485/encrypted_PLC_1489138337192.pdf)
        String relativePath = userGroup.getUserId2() + _separator + randomFileName;
        // Sinh duong dan file ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/encrypted_PLC_1489138337192.pdf)
        String encryptedFilePath = parentFolderPath + _separator + randomFileName;
        TextProcessDAO textProcessDAO = new TextProcessDAO();
        File signatureImage = null;
        if (textProcessDAO.isMainSigner(userGroup.getUserId1(), userGroup.getUserId2(),
                text.getTextId())) {
            // Lay thong tin anh chu ky
            StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
            List<Long> listUserId = new ArrayList<>();
            listUserId.add(userGroup.getUserId2());
            List<EntityStaffImageSign> listStaffImageSign = staffImageSignDAO.getStaffImageSignById(listUserId);
            if (!CommonUtils.isEmpty(listStaffImageSign)) {
                EntityStaffImageSign staffImageSign = listStaffImageSign.get(0);
                signatureImage = getFileByType(SIGNATURE_IMAGE_FILE_TYPE,
                        staffImageSign.getPath(), staffImageSign.getStorage());
            }
        } else {
            LOGGER.error("exportAppendixChatToPdfFile - userId: " + userGroup.getUserId2()
                    + " - User khong phai nguoi ky chinh!");
        }
        InputStream isSignatureImage = null;
        try {
            if (signatureImage != null && signatureImage.exists()) {
                isSignatureImage = new FileInputStream(signatureImage);
            } else {
                LOGGER.error("exportAppendixChatToPdfFile - userId: "
                        + userGroup.getUserId2() + " - Anh chu ky khong ton tai!");
            }
            InputStream template = ReportTemplateManager.getTemplate(
                    ReportTemplateManager.APPENDIX_CHAT_TEMPLATE);
            Map<String, Object> params = new HashMap<>();
            params.put("textTitle", text.getTitle());
            params.put("signerName", userGroup.getName2());
            params.put("isSignatureImage", isSignatureImage);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    text.getListMessage());
            JasperPrint print = JasperFillManager.fillReport(template, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, tmpFilePath);
        } catch (Exception ex) {
            errorMessage.append(" - Loi xuat file!");
            LOGGER.error(errorMessage, ex);
            return null;
        } finally {
            try {
                if (isSignatureImage != null) {
                    isSignatureImage.close();
                }
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        // Ma hoa file
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(tmpFilePath);
            os = new FileOutputStream(encryptedFilePath);
            DES.encrypt(is, os);
        } catch (Exception ex) {
            errorMessage.append(" - Loi ma hoa file!");
            LOGGER.error(errorMessage, ex);
            return null;
        } catch (Throwable ex) {
            errorMessage.append(" - Loi ma hoa file!");
            LOGGER.error(errorMessage, ex);
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException ex) {
                errorMessage.append(" - Dong luong - Exception!");
                LOGGER.error(errorMessage, ex);
            }
        }
        // Tao doi duong tra ve
        EntityFileAttachment fileAttachment = new EntityFileAttachment();
        String email = userGroup.getVof2_ItemEntityUser() == null
                ? null : userGroup.getVof2_ItemEntityUser().getStrEmail();
        if (email != null && email.trim().length() > 0) {
            email = email.substring(0, email.lastIndexOf("@"));
        }
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        fileAttachment.setFileName(String.format("Phu luc chat - %s - %s.pdf",
                email, sdf.format(date)));
        fileAttachment.setFilePath(relativePath);
        fileAttachment.setStorage(storageName);
        fileAttachment.setAttachment(tmpFilePath);
        return fileAttachment;
    }

    /**
     * <b>Sinh ten file ngau nhien</b><br>
     * Ten moi sinh ra co cung dinh dang voi ten file cu truyen vao
     *
     * @param oldFileName
     * @return
     */
    public static String randomFileName(String oldFileName) {

        // Lay phan mo rong trong ten file
        String extension = getPartOfFileName(EXTENSION_PART_TYPE, oldFileName);
        // Sinh chuoi ngau nhien
        UUID uuid = UUID.randomUUID();
        return uuid.toString() + "." + extension;
    }

    /**
     * <b>Di chuyen file tu thu muc tam</b><br>
     *
     * @param objectId              id van ban trinh ky/ban hanh
     * @param listAttachment        danh sach file dinh kem
     * @param type                  loai van ban trinh ky/ban hanh
     * @return
     */
    public static boolean moveTmpFile(Object objectId,
            List<EntityFilesAttachment> listAttachment, int type) {

        // Kiem tra dau vao
        if (objectId == null || CommonUtils.isEmpty(listAttachment)) {
            LOGGER.error("moveTmpFile - Loi du lieu dau vao!");
            return false;
        }
        // Tao duong dan thu muc dich
        String destPath = generatePathToUploadById(objectId, type);
        if (CommonUtils.isEmpty(destPath)) {
            LOGGER.error("moveTmpFile - Loi tao duong dan thu muc dich!");
            return false;
        }
        // Lay thu muc luu file tam
        String tmpStorage = getTmpStorageName();
        for (EntityFilesAttachment attachment : listAttachment) {
            // fix bug copy file ql nhiem vu start
            if (!attachment.isIsCopy()) {
                attachment.setStorage(tmpStorage);
            }
            // fix bug copy file ql nhiem vu end
        }
        return moveFile(listAttachment, destPath, null);
    }
    
    /**
     * <b>Di chuyen file tu thu muc tam</b><br>
     *
     * @param listAttachment danh sach file dinh kem
     * @param destPath duong dan thu muc dich
     * @param destStorage storage dich
     * @return
     */
    public static boolean moveTmpFile(List<EntityFilesAttachment> listAttachment,
            String destPath, String destStorage) {

        // Kiem tra dau vao
        if (CommonUtils.isEmpty(listAttachment)) {
            LOGGER.error("moveTmpFile - Loi du lieu dau vao!");
            return false;
        }
        // Lay thu muc luu file tam
        String tmpStorage = getTmpStorageName();
        for (EntityFilesAttachment attachment : listAttachment) {
            attachment.setStorage(tmpStorage);
        }
        return moveFile(listAttachment, destPath, destStorage);
    }

    /**
     * <b>Di chuyen file</b><br>
     *
     * @param objectId id van ban trinh ky/ban hanh
     * @param listAttachment danh sach file dinh kem
     * @param type loai van ban trinh ky/ban hanh
     * @return
     */
    public static boolean moveFile(Long objectId,
            List<EntityFilesAttachment> listAttachment, int type) {

        // Kiem tra dau vao
        if (objectId == null) {
            LOGGER.error("moveFile - Loi du lieu dau vao!");
            return false;
        }
        // Tao duong dan thu muc dich
        String destPath = generatePathToUploadById(objectId, type);
        if (CommonUtils.isEmpty(destPath)) {
            LOGGER.error("moveFile - Loi tao duong dan thu muc dich!");
            return false;
        }
        return moveFile(listAttachment, destPath, null);
    }

    /**
     * <b>Di chuyen file</b><br>
     *
     * @param listAttachment danh sach file
     * @param destPath duong dan thu dich
     * @param destStorage storage dich
     * @return
     */
    public static boolean moveFile(List<EntityFilesAttachment> listAttachment,
            String destPath, String destStorage) {

        // Kiem tra dau vao
        if (CommonUtils.isEmpty(listAttachment) || CommonUtils.isEmpty(destPath)) {
            LOGGER.error("moveFileToSpecificPath - Du lieu dau vao khong hop le!");
            return false;
        }
        // Kiem tra ten va phan mo rong
        String fileName;
        for (EntityFilesAttachment attachment : listAttachment) {
            fileName = attachment.getName();
            if (!checkSafeFileName(fileName) || !isAllowedType(fileName)) {
                LOGGER.error("moveFileToSpecificPath - Ten hoac phan mo rong khong hop le!");
                return false;
            }
        }
        // Storage mac dinh
        String defaultStorage = getDefaultStorage();
        String defaultStorageFolder = getStorageFolder(defaultStorage);
        // Kiem tra cau hinh storage mac dinh        
        if (CommonUtils.isEmpty(defaultStorage)
                || CommonUtils.isEmpty(defaultStorageFolder)) {
            LOGGER.error("moveFileToSpecificPath - Loi khong co cau hinh storage mac dinh!");
            return false;
        }
        // Neu khong co storage dich thi lay storage mac dinh
        if (CommonUtils.isEmpty(destStorage)) {
            destStorage = defaultStorage;
        }
        // Thuc muc storage dich
        String desStorageFolder = getStorageFolder(destStorage);
        // Storage nguon, thuc muc storage nguon
        String srcStorage, srcStorageFolder;
        // File nguon, thu muc file dich, file dich
        // Neu thu muc file dich chua ton tai thi tao moi
        File destDir = new File(desStorageFolder + destPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File srcFile, destFile;
        String randomFileName;
        InputStream is = null;
        OutputStream os = null;
        for (EntityFilesAttachment attachment : listAttachment) {
            // Lay storage cua file
            srcStorage = attachment.getStorage();
            srcStorageFolder = getStorageFolder(srcStorage);
            // Neu srcStorageFolder null hoac rong
            // -> Lay gia tri storageFolder mac dinh
            if (CommonUtils.isEmpty(srcStorageFolder)) {
                srcStorageFolder = defaultStorageFolder;
            }
            // Kiem tra xem file nguon co ton tai khong
            srcFile = new File(srcStorageFolder + _separator
                    + attachment.getAttachment());
            if (!srcFile.exists()) {
                LOGGER.error("moveFile - Loi file nguon khong ton tai"
                        + " - srcStorage: " + srcStorage
                        + " - srcPath: " + srcStorageFolder
                        + _separator + attachment.getAttachment());
                return false;
            }
            // Sinh ten file ngau nhien
            randomFileName = randomFileName(attachment.getName());
            destFile = new File(desStorageFolder + destPath + _separator + randomFileName);
            while (destFile.exists()) {
                randomFileName = "_" + randomFileName;
                destFile = new File(desStorageFolder + destPath + _separator + randomFileName);
            }
            try {
                // Copy file
                if (attachment.isIsCopy()) {
                    if (attachment.isDecrypt()) {
                        is = new FileInputStream(srcFile);
                        os = new FileOutputStream(destFile);
                        DES.encrypt(is, os);
                    } else {
                        org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
                    }
                } // Move file
                else {
                    // Giai ma file neu file tam da ma hoa va file luu khong muon ma hoa
                    if (attachment.isDecrypt()) {
                        is = new FileInputStream(srcFile);
                        os = new FileOutputStream(destFile);
                        DES.decrypt(is, os);
                        srcFile.delete();
                    } else {
                        org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
                    }                    
                }
                // Gan storage
                attachment.setStorage(destStorage);
                // Gan attachment
                attachment.setAttachment(destPath + _separator + randomFileName);
                // Gan kich thuoc file
                attachment.setFileSize(destFile.length());
                FunctionCommon.writeLogsNewfile(desStorageFolder + destPath + _separator + randomFileName);
            } catch (IOException ex) {
                LOGGER.error("moveFile - IOException!", ex);
                return false;
            } catch (Throwable ex) {
                LOGGER.error("moveFile - Throwable!", ex);
                return false;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                } catch (IOException ex) {
                    LOGGER.error("moveFile - IOException!", ex);
                }
            }
        }
        return true;
    }
    
    /**
     * @author cuongnv
     * @param lstTmpPath danh sach file tam
     * @param path path file luu tren storage
     * @return
     * @since 30/11/2016
     */
    public static boolean moveFilePDF(List<String> lstTmpPath, String path, String store) {
        try {
            for (String fileName : lstTmpPath) {
                // Check file name and MIME type
                // Chuyen cac ham xu ly ve file sang FileUtils
                if (!checkSafeFileName(fileName)
                        || !isAllowedType(fileName)) {
                    return false;
                }

                String rootStoragePath = FunctionCommon.getPropertiesValue(store);
                String uploadPath = rootStoragePath + path;
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String keySaveFileTmp = FunctionCommon.getPropertiesValue("storageName_saveFileTmp");
                String uploadPathTmp = FunctionCommon.getPropertiesValue(keySaveFileTmp);
                File tmpFile = new File(uploadPathTmp + _separator + fileName);

                if (!tmpFile.exists()) {
                    LOGGER.error("Error moveFilePDF: File temp not found");
                    return false;
                }

                String encodeFileName = extractFileNameNotExt(fileName);

                File file = new File(uploadPath + encodeFileName);
                FunctionCommon.writeLogsNewfile(uploadPath+encodeFileName);
                // Move tmp file to upload directory
                FileInputStream fis = new FileInputStream(tmpFile);
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    //Giai ma file ma hoa va copy file
                    DES.decrypt(fis, fos);
                    fis.close();
                    fos.close();
                    org.apache.commons.io.FileUtils.deleteQuietly(tmpFile);
                } catch (Throwable ex) {
                    fis.close();
                    fos.close();
                    LOGGER.error("Loi giai ma file", ex);
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            LOGGER.error("moveFilePDF - Exception!", ex);
            return false;
        }
    }

    public static EntityFileAttachment generateTmpFileInfo(Long userId) {

        String storageName = getTmpStorageName();
        String storageFolder = getTmpStorageFolder();
        if (CommonUtils.isEmpty(storageFolder)) {
            LOGGER.error("generateTmpFileInfo - userId: " + userId
                    + " - Loi lay thu muc luu file tam!");
            return null;
        }
        // Sinh duong dan thu muc luu file (VD: /voffice3/storage/Upload/Text/tmp/6485)
        String parentFolderPath = storageFolder + _separator + userId;
        // Neu thu muc luu file chua ton tai -> tao thu muc
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        // Sinh ten ngau nhien cho file
        String randomFileName = generateRandomFileName(PDF_FILE_EXTENSION);
        // Duong dan tuyet doi cho file tam chua ma hoa
        // (VD: /voffice3/storage/Upload/Text/tmp/6485/BBH_1489138337192.pdf)
        String tmpFilePath = parentFolderPath + _separator + randomFileName;
        File tmpFile = new File(tmpFilePath);
        // Neu file da ton tai thi them dau gach chan vao dau ten
        while (tmpFile.exists()) {
            randomFileName = "_" + randomFileName;
            tmpFilePath = parentFolderPath + _separator + randomFileName;
            tmpFile = new File(tmpFilePath);
        }
        String encryptedFilePath = _separator + userId
                + _separator + "encrypted_" + randomFileName;
        EntityFileAttachment tmpFileInfo = new EntityFileAttachment();
        tmpFileInfo.setStorage(storageName);
        tmpFileInfo.setAttachment(tmpFilePath);
        tmpFileInfo.setFilePath(encryptedFilePath);
        tmpFileInfo.setFileName(storageFolder + encryptedFilePath);
        return tmpFileInfo;
    }
    
    /**
     * <b>Tao file giao nhiem vu/bao cao danh gia nhiem vu</b>
     * 
     * @param userGroup         thong tin user
     * @param isAssignment      true - tao file giao nhiem vu
     * @param listOrg           danh sach don vi
     * @param listMission       danh sach nhiem vu
     * @param listSigner        danh sach nguoi ky
     * @param period            ky giao nhiem vu/bao cao danh gia
     * @param isPreview         true - xem truoc file trinh ky
     * @param totalPoint        tong diem
     * @param mediumPoint       diem trung binh
     * @param classification    xep loai
     * @param documentCode      ma ban hanh van ban + ngay ban hanh
     * @return 
     */
    public static EntityFileAttachment exportMissionFile(EntityUserGroup userGroup,
            boolean isAssignment, List<EntityVhrOrg> listOrg,
            List<EntityMissionChild> listMission, List<EntityUserGroup> listSigner,
            String period, boolean isPreview, Integer totalPoint, Float mediumPoint,
            String classification, String documentCode) {

        EntityFileAttachment tmpFileInfo = null;
        Long userId = userGroup == null ? null : userGroup.getUserId2();
        String errorDesc = String.format(Constants.Common.LOG_SYNTAX, "exportMissionFile",
                userId, "cardId", 0);
        // Kiem tra du lieu dau vao
        // + Ky danh gia phai co dinh dang yyyyMM
        if (userGroup ==  null || CommonUtils.isEmpty(listSigner)
                || CommonUtils.isEmpty(period) || period.length() != 6) {
            LOGGER.error(errorDesc + "Loi du lieu dau vao!");
            return tmpFileInfo;
        }
        EntityUser user1;
        List<EntityUser> listSignerWhichShowImage = new ArrayList<>();
        Vof2_EntityUser user2;
        List<Vof2_EntityUser> listSignerWhichShowImage2 = new ArrayList<>();
        for (int i = 0; i < listSigner.size(); i++) {
            // User co hien thi anh chu ky
            user1 = listSigner.get(i).getItemEntityUser();
            user2 = listSigner.get(i).getVof2_ItemEntityUser();
            if (user1.getSignImage() != null) {
                listSignerWhichShowImage.add(user1);
                listSignerWhichShowImage2.add(user2);
            }
        }
        // So nguoi ky co anh chu ky khac 2
        if (listSignerWhichShowImage.size() != 2) {
            LOGGER.error(errorDesc + "So nguoi ky co anh chu ky # 2!");
            return tmpFileInfo;
        }
        // Tao thong tin file tam
        tmpFileInfo = generateTmpFileInfo(userGroup.getUserId2());
        if (tmpFileInfo == null || CommonUtils.isEmpty(tmpFileInfo.getAttachment())) {
            LOGGER.error(errorDesc + "Loi tao thong tin file tam!");
            tmpFileInfo = null;
            return tmpFileInfo;
        }
        try {
            // Lay thong tin don vi
            String performingOrgName = listOrg.get(0).getName();
            if (!CommonUtils.isEmpty(performingOrgName)) {
                performingOrgName = performingOrgName.toUpperCase();
            }
            String parentOrgName = null;
            if (listOrg.size() > 1) {
                parentOrgName = listOrg.get(1).getName();
            }
            InputStream template;
            List<String> listKey = new ArrayList<>();
            int underlineWidth;
            // Phieu giao nhiem vu
            if (isAssignment) {
                template = ReportTemplateManager.getTemplate(ReportTemplateManager
                        .MISSION_ASSIGNMENT_TEMPLATE);
                listKey.add("NGƯỜI NHẬN NHIỆM VỤ");
                listKey.add("NGƯỜI GIAO NHIỆM VỤ");
                tmpFileInfo.setName(String.format("GIAO_NHIEM_VU_THANG_%s_%s.pdf",
                        period.substring(4), period.substring(0, 4)));
                underlineWidth = underlineWidth(performingOrgName, 287, 2);
            } // Phieu danh gia nhiem vu
            else {
                template = ReportTemplateManager.getTemplate(ReportTemplateManager
                        .MISSION_ASSESSMENT_TEMPLATE);
                listKey.add("NGƯỜI BÁO CÁO");
                listKey.add("NGƯỜI ĐÁNH GIÁ");
                tmpFileInfo.setName(String.format("BAO_CAO_THUC_HIEN_NHIEM_VU_THANG_%s_%s.pdf",
                        period.substring(4), period.substring(0, 4)));
                underlineWidth = underlineWidth(performingOrgName, 350, 1);
            }
            
            Map<String, Object> params = new HashMap<>();
            // Nguoi ky thu 1 la nguoi nhan viec
            // Nguoi ky thu 2 la nguoi giao viec
            Vof2_EntityUser receiver = listSignerWhichShowImage2.get(0);
            Vof2_EntityUser assigner = listSignerWhichShowImage2.get(1);
            params.put("ASSIGNER_NAME", assigner.getFullName());
            params.put("ASSIGNER_POSITION", assigner.getJobTile());
            params.put("ASSIGNER_ORG", assigner.getAdOrgName());
            params.put("RECEIVER_NAME", receiver.getFullName());
            params.put("RECEIVER_POSITION", receiver.getJobTile());
            params.put("RECEIVER_ORG", receiver.getAdOrgName());
            params.put("PARENT_ORG", parentOrgName);
            params.put("YEAR_OF_PERIOD", period.substring(0, 4));
            params.put("MONTH_OF_PERIOD", period.substring(4));
            params.put("TOTAL_POINT", totalPoint);
            params.put("MEDIUM_POINT", mediumPoint);
            params.put("CLASSIFICATION", classification);
            params.put("PERFORMING_ORG", performingOrgName);
            params.put("DOCUMENT_CODE", documentCode);
            params.put("UNDERLINE_WIDTH", underlineWidth);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    listMission);
            JasperPrint print = JasperFillManager.fillReport(template, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, tmpFileInfo.getAttachment());
            // Them note vao van ban            
            PdfUtils.addSignPlaceHolder(tmpFileInfo.getAttachment(), listKey);
            // Xem truoc file
            if (isPreview) {
                // Them chan chu ky, watermark
                List<EntityTextProcess> listTextProcess = new ArrayList<>();
                if (!CommonUtils.isEmpty(listSignerWhichShowImage)) {
                    EntityTextProcess textProcess;
                    int index = 1;
                    StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
                    EntityStaffImageSign staffImageSign;
                    for (EntityUser signer : listSignerWhichShowImage) {
                        textProcess = new EntityTextProcess();
                        textProcess.setState((long) Constants.Text.State.APPROVED);
                        textProcess.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                        textProcess.setSignImageIndex((long) index++);
                        textProcess.setNameImageSign("default_sign.png");
                        // Co id anh chu ky
                        if (signer.getSignImageId() != null) {
                            // Lay thong tin anh chu ky theo id
                            staffImageSign = staffImageSignDAO.getSignatureImageById(
                                    signer.getSignImageId());
                            if (staffImageSign != null
                                    && !CommonUtils.isEmpty(staffImageSign.getName())) {
                                textProcess.setNameImageSign(staffImageSign.getName());
                            }
                        }                        
                        listTextProcess.add(textProcess);
                    }
                }
                // Lay mang byte cua file
                byte[] fileBytes = DownloadFileDocumentDAO.loadFile(tmpFileInfo.getAttachment());
                PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
                // TungHD
                // DatDC add param for Mark
                pdfSignatureUtil.writeWaterMarkPdf(fileBytes, userGroup.getCardId()
                        + "_" + userGroup.getName2(), null, listTextProcess,
                        tmpFileInfo.getAttachment(), null, null, null, null, null,
                        null, null, "vi", 0L, null, null, null, false, null, false);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception!", ex);
            tmpFileInfo = null;
        }
        return tmpFileInfo;
    }
    
    /**
     * 
     * @param userGroup
     * @param listOrg
     * @param listOrgRating
     * @param listSigner
     * @param period
     * @param isPreview
     * @return 
     */
    public static EntityFileAttachment exportOrgRatingFile(EntityUserGroup userGroup,
            List<EntityVhrOrg> listOrg, List<EntityOrgCriteriaRatingTotal> listOrgRating,
            List<EntityUserGroup> listSigner, String period, boolean isPreview, Integer type) {
        
        EntityFileAttachment tmpFileInfo = null;
        String errorDesc = "exportOrgRatingFile - userId: "
                + (userGroup == null ? null : userGroup.getUserId2()) + " - ";
        // Kiem tra du lieu dau vao
        // + Danh sach nguoi ky
        // + Ky danh gia phai co dinh dang yyyyMM
        if (userGroup ==  null || CommonUtils.isEmpty(listSigner)
                || CommonUtils.isEmpty(period)) {
            LOGGER.error(errorDesc + "Loi du lieu dau vao!");
            return tmpFileInfo;
        }
        // Tao thong tin file tam
        tmpFileInfo = generateTmpFileInfo(userGroup.getUserId2());
        if (tmpFileInfo == null || CommonUtils.isEmpty(tmpFileInfo.getAttachment())) {
            LOGGER.error(errorDesc + "Loi tao thong tin file tam!");
            tmpFileInfo = null;
            return tmpFileInfo;
        }
        
        try {
        	String title = "";
        	if (type != null) {
        		if (type.intValue() == 1) {
        			title = "TỔNG HỢP THI ĐUA THÁNG " + period.substring(4) + " NĂM " + period.substring(0, 4);
        			tmpFileInfo.setName(String.format("Tong hop cham diem thi dua thang %s/%s cua cac CQDV.pdf",
        	                period.substring(4), period.substring(0, 4)));
        		} else if (type.intValue() == 2) {
        			Integer quarter = Integer.valueOf(period.substring(4));
        			String quarterStr = "";
        			if (quarter > 9) {
        				quarterStr = "4";
        			} else if (quarter > 6) {
        				quarterStr = "3";
        			} else if (quarter > 3) {
        				quarterStr = "2";
        			} else if (quarter > 0) {
        				quarterStr = "1";
        			}
        			title = "TỔNG HỢP THI ĐUA QUÝ " + quarterStr + " NĂM " + period.substring(0, 4);
        			tmpFileInfo.setName(String.format("Tong hop cham diem thi dua quy %s nam %s cua cac CQDV.pdf",
        					quarterStr, period.substring(0, 4)));
        		} else if (type.intValue() == 3) {
        			title = "TỔNG HỢP THI ĐUA NĂM " + period;
        			tmpFileInfo.setName(String.format("Tong hop cham diem thi dua nam %s cua cac CQDV.pdf", period));
        		}
            } 
        	
            // Lay thong tin don vi
//            String orgName = listOrg.get(0).getName();
//            String parentOrgName = null;
//            if (listOrg.size() > 1) {
//                parentOrgName = listOrg.get(1).getName();
//            }
            String parentOrgName = listOrg.get(0).getName();
            // Dia diem va thoi gian hien tai
            String locationAndTime = "Hà Nội, ";
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy");
            locationAndTime += sdf.format(now);
            // Don vi tao danh gia
            String orgName = "Văn phòng";
            // Chuc vu nguoi danh gia
            String assignerPosition = "Chỉ huy đơn vị";
            InputStream template = ReportTemplateManager.getTemplate(ReportTemplateManager
                        .ORG_RATING_TEMPLATE);
            Map<String, Object> params = new HashMap<>();
            params.put("PARENT_ORG_NAME", parentOrgName);
            params.put("ORG_NAME", orgName.toUpperCase());
            params.put("LOCATION_AND_TIME", locationAndTime);
            params.put("TITLE_TEMPLATE", title.toUpperCase());
//            params.put("MONTH_OF_PERIOD", period.substring(4));
//            params.put("YEAR_OF_PERIOD", period.substring(0, 4));
            params.put("ASSIGNER_POSITION", assignerPosition.toUpperCase());
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    listOrgRating);
            JasperPrint print = JasperFillManager.fillReport(template, params, dataSource);
            JasperExportManager.exportReportToPdfFile(print, tmpFileInfo.getAttachment());
            // Them note vao van ban            
            EntityUser user1;
            List<EntityUser> listSignerWhichShowImage = new ArrayList<>();
            List<String> listKey = new ArrayList<>();
            listKey.add(orgName.toUpperCase());
            listKey.add(assignerPosition.toUpperCase());
            for (int i = 0; i < listSigner.size(); i++) {
                // User co hien thi anh chu ky
                user1 = listSigner.get(i).getItemEntityUser();                
                if (user1.getSignImage() != null) {
                    listSignerWhichShowImage.add(user1);
                }
            }
            if (!CommonUtils.isEmpty(listKey)) {
                PdfUtils.addSignPlaceHolder(tmpFileInfo.getAttachment(), listKey, true);
            }
            // Xem truoc file
            if (isPreview) {
                // Them chan chu ky, watermark
                List<EntityTextProcess> listTextProcess = new ArrayList<>();
                if (!CommonUtils.isEmpty(listSignerWhichShowImage)) {
                    EntityTextProcess textProcess;
                    int index = 1;
                    StaffImageSignDAO staffImageSignDAO = new StaffImageSignDAO();
                    EntityStaffImageSign staffImageSign;
                    for (EntityUser signer : listSignerWhichShowImage) {
                        textProcess = new EntityTextProcess();
                        textProcess.setState((long) Constants.Text.State.APPROVED);
                        textProcess.setStatusSign((long) Constants.TextProcess.State.LEADER_SIGNED);
                        textProcess.setSignImageIndex((long) index++);
                        textProcess.setNameImageSign("default_sign.png");
                        // Co id anh chu ky
                        if (signer.getSignImageId() != null) {
                            // Lay thong tin anh chu ky theo id
                            staffImageSign = staffImageSignDAO.getSignatureImageById(
                                    signer.getSignImageId());
                            if (staffImageSign != null
                                    && !CommonUtils.isEmpty(staffImageSign.getName())) {
                                textProcess.setNameImageSign(staffImageSign.getName());
                            }
                        }                        
                        listTextProcess.add(textProcess);
                    }
                }
                // Lay mang byte cua file
                byte[] fileBytes = DownloadFileDocumentDAO.loadFile(tmpFileInfo.getAttachment());
                PDFSignatureUtil pdfSignatureUtil = new PDFSignatureUtil();
                // TungHD
                // DatDC add param for Mark
                pdfSignatureUtil.writeWaterMarkPdf(fileBytes, userGroup.getCardId()
                        + "_" + userGroup.getName2(), null, listTextProcess,
                        tmpFileInfo.getAttachment(), null, null, null, null, null,
                        null, null, "vi", 0L, null, null, null, false, null, false);
            }
        } catch (Exception ex) {
            LOGGER.error(errorDesc + "Exception!", ex);
            tmpFileInfo = null;
        }
        return tmpFileInfo;
    }

    /**
     * Tra ve do rong cua ky tu Times New Roman Bold size 13
     * 
     * @param c
     * @return 
     */
    public static float timesNewRoman13BoldToPixel(char c) {
        
        if (c == '%') {
            return 13.2f;
        }
        if (c == 'W') {
            return 13f;
        }
        if (c == 'M') {
            return 12.3f;
        }
        if (c == '@') {
            return 12.1f;
        }
        if (c == 'm' || c == '&') {
            return 10.9f;
        }
        if (c == 'Q') {
            return 10.3f;
        }
        if (c == 'G' || c == 'H' || c == 'K' || c == 'O') {
            return 10.2f;
        }
        if (c == 'w') {
            return 9.6f;
        }
        if (c == 'A' || c == 'C' || c == 'D' || c == 'N' || c == 'R'
                || c == 'U' || c == 'V' || c == 'X' || c == 'Y') {
            return 9.4f;
        }
        if (c == 'B') {
            return 8.6f;
        }
        if (c == 'E' || c == 'L' || c == 'T' || c == 'Z') {
            return 8.7f;
        }
        if (c == 'F' || c == 'P') {
            return 8f;
        }
        if (c == '^') {
            return 7.6f;
        }
        if (c == '+' || c == '=' || c == '<' || c == '>') {
            return 7.5f;
        }
        if (c == 'S' || c == 'b' || c == 'd' || c == 'h' || c == 'k' || c == 'n'
                || c == 'p' || c == 'q' || c == 'u' || c == '"') {
            return 7.3f;
        }
        if (c == '~') {
            return 6.8f;
        }
        if (c == 'x' || c == 'y') {
            return 6.7f;
        }
        if (c == 'J' || c == 'a' || c == 'g' || c == 'o' || c == 'v'
                || (c >= '0' && c <= '9') || c == '_' || c == '#'
                || c == '$' || c == '*' || c == '?') {
            return 6.5f;
        }
        if (c == 'c' || c == 'e' || c == 'r' || c == 'z') {
            return 5.8f;
        }
        if (c == '}' || c == '{') {
            return 5.2f;
        }
        if (c == 'I' || c == 's') {
            return 5.1f;
        }
        if (c == 'f') {
            return 4.5f;
        }
        if (c == 't' || c == '!' || c == ':' || c == ';' || c == '('
                || c == ')' || c == '-' || c == '`') {
            return 4.4f;
        }
        if (c == 'l' || c == '\'' || c == '\\' || c == '/' || c == 'i') {
            return 3.7f;
        }
        if (c == ' ' || c == '.' || c == ',') {
            return 3.3f;
        }
        if (c == '|') {
            return 2.9f;
        }
        
        return 0;
    }
    
    /**
     * Tra ve do rong cua ky tu Times New Roman Bold size 11
     * 
     * @param c
     * @return 
     */
    public static float timesNewRoman11BoldToPixel(char c) {
        
        if (c == '%') {
            return 11f;
        }
        if (c == 'W') {
            return 11f;
        }
        if (c == 'M') {
            return 10.7f;
        }
        if (c == '@') {
            return 10.3f;
        }
        if (c == 'm' || c == '&') {
            return 9.2f;
        }
        if (c == 'Q' || c == 'G' || c == 'K' ) {
            return 8.7f;
        }
        if (c == 'H' || c == 'O') {
            return 8.6f;
        }
        if (c == 'w') {
            return 8.1f;
        }
        if (c == 'A' || c == 'C' || c == 'D' || c == 'N' || c == 'R'
                || c == 'U' || c == 'V' || c == 'X' || c == 'Y') {
            return 8f;
        }
        if (c == 'B') {
            return 7.6f;
        }
        if (c == 'E' || c == 'L' || c == 'T' || c == 'Z') {
            return 7.4f;
        }
        if (c == 'F' || c == 'P') {
            return 6.9f;
        }
        if (c == '^') {
            return 6.4f;
        }
        if (c == '+' || c == '=' || c == '<' || c == '>') {
            return 6.3f;
        }
        if (c == 'S' || c == 'b' || c == 'd' || c == 'h' || c == 'k' || c == 'n'
                || c == 'p' || c == 'q' || c == 'u' || c == '"') {
            return 6.2f;
        }
        if (c == '~') {
            return 5.8f;
        }
        if (c == 'y') {
            return 5.6f;
        }
        if (c == 'x' || c == 'o' || c == 'v') {
            return 5.5f;
        }
        if (c == 'J' || c == 'a' || c == 'g'
                || (c >= '0' && c <= '9') || c == '_' || c == '#'
                || c == '$' || c == '*' || c == '?') {
            return 5.6f;
        }
        if (c == 'z') {
            return 5.1f;
        }
        if (c == 'c' || c == 'e' || c == 'r') {
            return 4.9f;
        }
        if (c == '}' || c == '{' || c == 'I' || c == 's') {
            return 4.4f;
        }
        if (c == '!') {
            return 3.8f;
        }
        
        if (c == 'f' || c == 't' || c == ':' || c == ';'  || c == '(' || c == ')'
                || c == '-' || c == '`') {
            return 3.7f;
        }       
        if (c == 'i' || c == 'l' || c == '\'' || c == '\\' || c == '/') {
            return 3.1f;
        }
        if (c == '.' || c == ',') {
            return 2.8f;
        }
        if (c == ' ') {
            return 2.7f;
        }
        if (c == '|') {
            return 2.6f;
        }        
        return 0;
    }
    
    /**
     * Duong gach chan bang 1/2 chuoi, neu dong rong chuoi lon hon do rong toi da
     * thi lay do rong toi da / 2
     * 
     * @param str           chuoi
     * @param maxWidth      do rong toi da
     * @param type          loai
     * @return 
     */
    public static int underlineWidth(String str, float maxWidth, int type) {
        
        if (CommonUtils.isEmpty(str)) {
            return 0;
        }
        str = FunctionCommon.removeUnsign(str);
        char[] chars = str.toCharArray();
        float width = 0;
        for (char c : chars) {
            switch (type) {
                // Times New Roman 13 Bold
                case 1:
                    width += timesNewRoman13BoldToPixel(c);
                    break;
                case 2:
                    width += timesNewRoman11BoldToPixel(c);
                    break;
            }
        }
        if (width > maxWidth) {
            width = maxWidth;
        }
        return width == 0 ? 0 : (int) width / 2;
    }
    
    /**
     * Lay thong tin file word va excel apache poi
     * 
     * @param storage
     * @param filePath
     * @param userId
     * @return
     */
    public static String[] getInforFileDocExcel(String storage, String filePath, Long userId) {
        String[] infoFile = new String[2];
        String absPath;
        try {
            if (storage != null) {
                absPath = FunctionCommon.getStorageConfigFile(storage) + filePath;
            } else {
                absPath = CommonUtils.getAppConfigValue("document-root-path") + filePath;
            }
            File newFile = new File(absPath);
            if (newFile.exists()) {
                String extension = extractFileExt(filePath);
                if (!CommonUtils.isEmpty(extension)) {
                    EncryptDecryptSignDocument ef = new EncryptDecryptSignDocument();
                    FileInputStream fis = null;
                    FileOutputStream fos = null;
                    File fileTmp = null;
                    FileInputStream fisPdf = null;
                    try {
                        String temPath = absPath.substring(0, absPath.length() - 4)
                                + userId.toString() + "_" + System.currentTimeMillis() + "_tmp_" + extension;
                        fileTmp = new File(temPath);
                        //giai ma ra file tam
                        SecretKey key = ef.getKey();
                        fis = new FileInputStream(absPath);
                        fos = new FileOutputStream(fileTmp);
                        ef.decrypt(key, fis, fos);
                        if (fileTmp.exists()) {
                            fisPdf = new FileInputStream(fileTmp);
                            if (extension.equalsIgnoreCase(".xls")) {
                                HSSFWorkbook workbook = new HSSFWorkbook(fisPdf);
                                infoFile[0] = String.valueOf(workbook.getNumberOfSheets());
                            }
                            if (extension.equalsIgnoreCase(".xlsx")) {
                                XSSFWorkbook workbook = new XSSFWorkbook(fisPdf);
                                infoFile[0] = String.valueOf(workbook.getNumberOfSheets());
                            }
                            if (extension.equalsIgnoreCase(".doc")) {
                                HWPFDocument doc = new HWPFDocument(fisPdf);
                                int pageCount = doc.getSummaryInformation().getPageCount();
                                infoFile[0] = String.valueOf(pageCount);
                                
                            }
                            if (extension.equalsIgnoreCase(".docx")) {
                                XWPFDocument xdoc = new XWPFDocument(fisPdf);
                                int pageCount = xdoc.getProperties().getExtendedProperties().getUnderlyingProperties().getPages();
                                infoFile[0] = String.valueOf(pageCount);
                            }
                            
                            infoFile[1] = String.valueOf(fileTmp.length());
                        }
                    } catch (Throwable ex1) {
                        LOGGER.error(ex1.getMessage(), ex1);
                    } finally {
                        if (fos != null) {
                            fos.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                        if (fisPdf != null) {
                            fisPdf.close();
                        }
                        if (fileTmp != null && fileTmp.exists()) {
                            fileTmp.delete();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return infoFile;
    }
    
    
    /**
     * <b>Xuat file giao viec</b>
     * Datdc tach ra danh rieng cho nang cap bao cao
     * ko anh huong cac luong khac
     * @author thanght6
     * @since Sep 12, 2016
     * @param templateName Ten template
     * @param assignmentOrg Don vi giao viec
     * @param assigner Nguoi ky
     * @param enforcer Nguoi thuc hien
     * @param period Ky danh gia (MM/yyyy)
     * @return Duong dan tuong doi cua file vua sinh
     */
    public static String exportTaskAssignmentFileUpdate(String templateName,
            EntityVhrOrg assignmentOrg, EntityVhrEmployee assigner,
            EntityVhrEmployee enforcer, String period) {

        // Kiem tra du lieu dau vao
        if (CommonUtils.isEmpty(templateName) || assignmentOrg == null
                || assigner == null || enforcer == null
                || CommonUtils.isEmpty(enforcer.getListPerformTask())
                || CommonUtils.isEmpty(period)) {
            String errorMessage = "";
            if (CommonUtils.isEmpty(templateName)) {
                errorMessage += "Khong co ten file mau, ";
            }
            if (assignmentOrg == null) {
                errorMessage += "Khong co don vi giao viec, ";
            }
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec, ";
            }
            if (enforcer == null) {
                errorMessage += "Khong co nguoi thuc hien, ";
            }
            if (enforcer != null && CommonUtils.isEmpty(enforcer.getListPerformTask())) {
                errorMessage += "Khong co danh sach cong viec, ";
            }
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky danh gia, ";
            }
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi du lieu dau "
                    + "vao khong hop le - " + errorMessage);
            return null;
        }
        List<EntityTask> listTask = enforcer.getListPerformTask();
        TaskDAO taskDAO = new TaskDAO();
        taskDAO.getTaskResult(listTask);
        // Lay danh sach cong viec phe duyet
        Long taskId;
        Long approvalState;
        List<Long> listApprovedTaskId = new ArrayList<>();
        List<EntityTask> listApprovedTask = new ArrayList<>();
        for (EntityTask task : listTask) {
            taskId = task.getTaskId();
            approvalState = task.getApprovalState();
            if (taskId != null && !taskId.equals(0L)
                    && (Constants.TASK_APPROVAL.STATE.APPROVED.equals(approvalState) || approvalState==null)) {
                listApprovedTaskId.add(taskId);
                listApprovedTask.add(task);
            }
        }
        // Lay nguon goc cong viec
        Map<Long, String> hmSourceDescription = taskDAO.getSourceDescriptionForListTask(listApprovedTaskId);
        // Gan mo ta nguon goc cho tung cong viec
        String sourceDescription;
        for (EntityTask task : listApprovedTask) {
            sourceDescription = hmSourceDescription.get(task.getTaskId());
            if (sourceDescription == null) {
                sourceDescription = "";
            }
            task.setSourceDescription(sourceDescription);
        }
        // Datdc bo check cong viec
        // Phan biet cong viec chuc nang vs cong viec ne nep
        /*List<EntityTask> listFunctionalTask = new ArrayList<>();
        List<EntityTask> listRoutineTask = new ArrayList<>();
        TaskUtils.distinguishTask(listApprovedTask, listFunctionalTask, listRoutineTask);*/
        // Neu chua co cong viec ne nep -> Sinh cong viec ne nep
        //if (CommonUtils.isEmpty(listRoutineTask)) {
        //    listRoutineTask.addAll(TaskUtils.generateRoutineTasks(assignmentOrg
        //            .getSysOrganizationId(), period, null, null));
        //}
        // Tao danh sach cong viec giao cho nhan vien co thu tu cong viec chuc nang truoc
        /*List<EntityTask> listAssignedTask = new ArrayList<>();
        listAssignedTask.addAll(listFunctionalTask);
        listAssignedTask.addAll(listRoutineTask);*/
        if (CommonUtils.isEmpty(listApprovedTask)) {
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi khong co cong"
                    + " viec giao di");
            return null;
        }
        // Tao duong dan luu file
        List<String> listPath = generateRecordFilePath(TASK_ASSIGNMENT_FILE_TYPE,
                enforcer.getEmployeeCode());
        if (CommonUtils.isEmpty(listPath)) {
            LOGGER.error("exportTaskAssignmentFileForEmployee - Loi tao duong "
                    + "dan luu file bao cao");
            return null;
        }
        String reportFilePath = listPath.get(0) + listPath.get(1);
        // Lay file template bao cao
        // Datdc thay listAssignedTask bang listApprovedTask
        InputStream template = ReportTemplateManager.getTemplate(templateName);
        if (exportTaskReportToPdfFileUpdate(template, assignmentOrg, period, assigner,
                enforcer, listApprovedTask, 0, null, reportFilePath, ASSIGNER_LABEL)) {
            return listPath.get(1);
        }
        return null;
    }
    
    /**
     * <b>Xuat file bao cao cong viec ra file Pdf</b><br>
     * Datdc tach ra danh rieng cho nang cap bao cao
     * ko anh huong cac luong khac
     * @author thanght6
     * @since Sep 14, 2016
     * @param template          file mau
     * @param assignmentOrg     don vi giao viec/danh gia
     * @param period            ky giao/danh gia
     * @param assigner          nguoi giao viec
     * @param enforcer          nguoi nhan
     * @param listTask          danh sach cong viec
     * @param mediumPoint       diem trung binh khi xuat file danh gia cong viec
     * @param classification    xep loai khi xuat file danh gia cong viec
     * @param filePath          duong dan luu file bao cao
     * @param searchText
     * @return
     */
    public static boolean exportTaskReportToPdfFileUpdate(InputStream template,
            EntityVhrOrg assignmentOrg, String period,
            EntityVhrEmployee assigner, EntityVhrEmployee enforcer, List<EntityTask> listTask,
            double mediumPoint, String classification, String filePath, String searchText) {

        // Kiem tra du lieu dau vao
        // Datdc bo check length period khac 6, no co the la moi nam 
        if (template == null || assignmentOrg == null
                || assignmentOrg.getSysOrganizationId() == null
                || CommonUtils.isEmpty(assignmentOrg.getOrgParentName())
                || CommonUtils.isEmpty(assignmentOrg.getName())
                || CommonUtils.isEmpty(period) || assigner == null
                || enforcer == null || CommonUtils.isEmpty(listTask)
                || CommonUtils.isEmpty(filePath)) {
            String errorMessage = "";
            if (template == null) {
                errorMessage += "Khong co template, ";
            }
            if (assignmentOrg == null || assignmentOrg.getSysOrganizationId() == null
                    || CommonUtils.isEmpty(assignmentOrg.getOrgParentName())
                    || CommonUtils.isEmpty(assignmentOrg.getName())) {
                errorMessage += "Thieu thong tin don vi danh gia, ";
            }
            if (CommonUtils.isEmpty(period)) {
                errorMessage += "Khong co ky giao/danh gia, ";
            }
            if (assigner == null) {
                errorMessage += "Khong co nguoi giao viec, ";
            }
            if (enforcer == null) {
                errorMessage += "Khong co nguoi nhan viec, ";
            }
            if (CommonUtils.isEmpty(listTask)) {
                errorMessage += "Khong co cong viec giao di, ";
            }
            if (CommonUtils.isEmpty(filePath)) {
                errorMessage += "Khong co duong dan file bao cao, ";
            }
            LOGGER.error("exportTaskReportToPdfFile - Loi du lieu dau vao - " + errorMessage);
            return false;
        }
        String parentOrgName = assignmentOrg.getOrgParentName();
        String assignmentOrgName = assignmentOrg.getName();
        // Lay thoi gian hien tai
        Calendar calendar = Calendar.getInstance();
        int currentDate = calendar.get(Calendar.DATE);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        String locationAndTime = String.format(LOCATION_AND_TIME, currentDate,
                currentMonth, currentYear);
        // Datdc check nam hay quy
        String year = "";
        String month = "";
        if (!CommonUtils.isEmpty(period)) {
            year = period.substring(0, 4);
            if (period.length() == 6) {
                month = period.substring(4);
                period = month + "/" + year;
            }
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("parentOrgName", parentOrgName);
        parameter.put("assignmentOrgName", assignmentOrgName);
        parameter.put("locationAndTime", locationAndTime);
        // Datdc sua lai tieu de start
        if (!CommonUtils.isEmpty(period)) {
            if (year.equalsIgnoreCase(period)) {
                parameter.put("period", "Năm " + period);
            } else {
                String headerDate = "Quý " + period;
                parameter.put("period", headerDate);
            }
        } else {
            parameter.put("period", "");
        }
        // Datdc sua lai tieu de end
        parameter.put("assignerPosition", assigner.getPosition());
        parameter.put("year", year);
        parameter.put("assignerName", assigner.getFullName());
        parameter.put("receiverName", enforcer.getFullName());
        // Datdc add param code nguoi nhan
        parameter.put("receiverCode", enforcer.getEmployeeCode());
        if(enforcer.getListPerformTask().get(0).getOrg_name() == null){
            parameter.put("org_name", "");
        } else {
            parameter.put("org_name", enforcer.getListPerformTask().get(0).getOrg_name());
        }
        if(enforcer.getListPerformTask().get(0).getPositionName() == null){
            parameter.put("positionName", "");
        } else {
            parameter.put("positionName", enforcer.getListPerformTask().get(0).getPositionName());
        }
        parameter.put("TC1", enforcer.getListPerformTask().get(0).getTC1());
        parameter.put("TC2", enforcer.getListPerformTask().get(0).getTC2());
        parameter.put("TC3", enforcer.getListPerformTask().get(0).getTC3());
        parameter.put("TC4", enforcer.getListPerformTask().get(0).getTC4());
        parameter.put("TC5", enforcer.getListPerformTask().get(0).getTC5());
        parameter.put("receiverPosition", enforcer.getPosition());
        parameter.put("mediumPoint", mediumPoint);
        DecimalFormat f = new DecimalFormat("0.00");
        if (enforcer.getSumPoint_NV() == null || enforcer.getSumPoint_NV() == 0L) {
            parameter.put("sumPointNV",  "");
        } else {
            parameter.put("sumPointNV",  f.format(enforcer.getSumPoint_NV()));
            
        }
        if (enforcer.getSumPoint_LD() == null || enforcer.getSumPoint_LD() == 0L) {
            parameter.put("sumPointLD",  "");
        } else {
            parameter.put("sumPointLD",  f.format(enforcer.getSumPoint_LD()));
            
        }
        if (enforcer.getSumPoint_LD() == null || enforcer.getSumPoint_LD() == 0L) {
            parameter.put("sumPointLD",  "");
        } else {
            parameter.put("sumPointLD",  f.format(enforcer.getSumPoint_LD()));
            
        }
        if (enforcer.getSumPointPercent() == null
                || enforcer.getSumPointPercent() == 0L) {
            parameter.put("sumPointPercent", "");
        } else {
            parameter.put("sumPointPercent", enforcer.getSumPointPercent()
                    .intValue());

        }
        classification = "";
        if (enforcer.getAverageTask() != null) {
            // Datdc sua lay them tham so
            // thay doi cach tinh
            Double avergeTask;
            /*if (enforcer.getAverageTask().getAddOrMinus() == 1) {
                avergeTask = enforcer.getSumPoint_LD() + enforcer.getAverageTask().getAttitudePoint();                
            } else {
                avergeTask = enforcer.getSumPoint_LD() - enforcer.getAverageTask().getAttitudePoint();
                if (!enforcer.getAverageTask().getAttitudePoint().equals(0L)) {
                    bonus = String.format(" (Điểm trừ: %d)",
                            enforcer.getAverageTask().getAttitudePoint());
                }
            }*/
            avergeTask = enforcer.getSumPoint_LD();
            parameter.put("avergeTask", f.format(enforcer.getSumPoint_LD()));
            // Lay gia tri cua dia diem lam viec duoc giao
            // neu la null set lai
            if (null == enforcer.getAverageTask().getComplyLocation()) {
                enforcer.getAverageTask().setComplyLocation(1L);
            }
            // Noi quy lao dong
            if (null == enforcer.getAverageTask().getComplyRule()) {
                enforcer.getAverageTask().setComplyRule(1L);
            }
            // Set gia tri noi quy lao dong attitudeRule
            if (enforcer.getAverageTask().getComplyRule().equals(1L)) {
                parameter.put("attitudeRule", "Tuân thủ");
            } else if (enforcer.getAverageTask().getComplyRule().equals(2L)) {
                parameter.put("attitudeRule", "Vi phạm chưa đến mức xử lý");
            } else if (enforcer.getAverageTask().getComplyRule().equals(3L)) {
                parameter.put("attitudeRule", "Xử lý kỷ luật lao động");
            }
            // Set gia tri text dia diem lam viec
            if (enforcer.getAverageTask().getComplyLocation().equals(1L)) {
                parameter.put("attitudeRating", "Tuân thủ");
            } else {
                parameter.put("attitudeRating", "Không tuân thủ");
            }
            // Set ghi chu dia diem va noi quy
            if (CommonUtils.isEmpty(enforcer.getAverageTask()
                    .getComplyRuleNote())) {
                parameter.put("complyRuleNote", "");
            } else {
                parameter.put("complyRuleNote", enforcer.getAverageTask()
                        .getComplyRuleNote());
            }
            if (CommonUtils.isEmpty(enforcer.getAverageTask()
                    .getComplyLocationNote())) {
                parameter.put("complyLocationNote", "");
            } else {
                parameter.put("complyLocationNote", enforcer.getAverageTask()
                        .getComplyLocationNote());
            }
            parameter.put("commentAverage", enforcer.getAverageTask().getCommentAverage() == null ?
                    "" : enforcer.getAverageTask().getCommentAverage());
            // Lay danh sach ty le
            TaskDAO taskDAO = new TaskDAO();
            List<EntityRatioConfigDetail> listRatio = taskDAO.getListRatioConfigDetail(
                    assignmentOrg.getSysOrganizationId(), 7, "code.ratio.rating.task", null);
            if (!CommonUtils.isEmpty(listRatio)) {
                for (EntityRatioConfigDetail ratioConfig : listRatio) {
                    if (avergeTask >= ratioConfig.getRatioMinPoint()
                            && avergeTask <= ratioConfig.getRatioMaxPoint()) {
                        classification = ratioConfig.getLogicalName();
                    }
                }
            }
        } else {
            parameter.put("avergeTask", "");
            parameter.put("attitudeRating", "");
            parameter.put("commentAverage", "");
        }
        parameter.put("classification", classification);
        
        String workLocation = "";
        Map<String, String> mapLocation = new LinkedHashMap<String, String>();
        // Cap nhat du lieu cong viec
        // Neu du lieu null thi gan bang rong de tranh in ra chu null
        String startTime;
        String endTime;
        for (EntityTask task : listTask) {
//            System.out.print(task.getTaskId() + ",");
            startTime = task.getStartTime();
            endTime = task.getEndTime();
            if (task.getWeight() == null) {
                task.setWeight(1L);
            }
            if (task.getSourceDescription() == null) {
                task.setSourceDescription("");
            }
            if (task.getContent() == null) {
                task.setContent("");
            }
            if (task.getTaskResult() == null) {
                task.setTaskResult("");
            }
            if (task.getApprovalComment() == null) {
                task.setApprovalComment("");
            }
            if (task.getRatingPoint() == null) {
                task.setRatingPoint(0D);
            }
            // Bo gio, phut khoi thoi gian bat dau, thoi gian ket thuc
            if (startTime != null && startTime.trim().length() > 10) {
                startTime = startTime.trim().substring(0, 10);
                task.setStartTime(startTime);
            }
            if (endTime != null && endTime.trim().length() > 10) {
                endTime = endTime.trim().substring(0, 10);
                task.setEndTime(endTime);
            }
            
            if (task.getNV_tc1() == null || task.getNV_tc1().equals("0.0")) {
                task.setNV_tc1("");
            } else {
                task.setNV_tc1(task.getNV_tc1().replace(".0", ""));
            }
            if (task.getNV_tc2() == null || task.getNV_tc2().equals("0.0")) {
                task.setNV_tc2("");
            } else {
                task.setNV_tc2(task.getNV_tc2().replace(".0", ""));
            }
            if (task.getNV_tc3() == null || task.getNV_tc3().equals("0.0")) {
                task.setNV_tc3("");
            } else {
                task.setNV_tc3(task.getNV_tc3().replace(".0", ""));
            }
            if (task.getNV_tc4() == null || task.getNV_tc4().equals("0.0")) {
                task.setNV_tc4("");
            } else {
                task.setNV_tc4(task.getNV_tc4().replace(".0", ""));
            }
            if (task.getNV_tc5() == null || task.getNV_tc5().equals("0.0")) {
                task.setNV_tc5("");
            } else {
                task.setNV_tc5(task.getNV_tc5().replace(".0", ""));
            }
            if (task.getLD_tc1() == null || task.getLD_tc1().equals("0.0")) {
                task.setLD_tc1("");
            } else {
                task.setLD_tc1(task.getLD_tc1().replace(".0", ""));
            }
            if (task.getLD_tc2() == null || task.getLD_tc2().equals("0.0")) {
                task.setLD_tc2("");
            } else {
                task.setLD_tc2(task.getLD_tc2().replace(".0", ""));
            }
            if (task.getLD_tc3() == null || task.getLD_tc3().equals("0.0")) {
                task.setLD_tc3("");
            } else {
                task.setLD_tc3(task.getLD_tc3().replace(".0", ""));
            }
            if (task.getLD_tc4() == null || task.getLD_tc4().equals("0.0")) {
                task.setLD_tc4("");
            } else {
                task.setLD_tc4(task.getLD_tc4().replace(".0", ""));
            }
            if (task.getLD_tc5() == null || task.getLD_tc5().equals("0.0")) {
                task.setLD_tc5("");
            } else {
                task.setLD_tc5(task.getLD_tc5().replace(".0", ""));
            }
            if (task.getNV_td() == null || task.getNV_td().equals("0.0")) {
                task.setNV_td("");
            }
            if (task.getLD_td() == null || task.getLD_td().equals("0.0")) {
                task.setLD_td("");
            }
            
            // Datdc Set cho phan tram
            // Do khac kieu du lieu
            if (task.getPercent() == null || task.getPercent() == 0D) {
                task.setPercentExport("0");
            } else {
                task.setPercentExport(String.valueOf(task.getPercent().intValue()));
            }
            
            if (!CommonUtils.isEmpty(task.getWorkLocation())) {
                String location = task.getWorkLocation().trim().replace(" ", "");
                location = FunctionCommon.removeUnsign(location).toLowerCase();
                if (mapLocation.get(location) == null) {
                    mapLocation.put(location, task.getWorkLocation());
                }
            }
        }

        if (mapLocation.size() > 0) {
            for (String str : mapLocation.keySet()) {
                workLocation += mapLocation.get(str) + "; ";
            }
            workLocation = workLocation.substring(0, workLocation.length() - 2);
        }
        parameter.put("workLocation", workLocation);
        
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listTask);
            JasperPrint print = JasperFillManager.fillReport(template, parameter, dataSource);
            JasperExportManager.exportReportToPdfFile(print, filePath);

            // Them comment vao file de dinh anh chu ky ve sau
            PdfUtils.addSignPlaceHolder(filePath, searchText);
//            System.out.println("Export - filePath: " + filePath);
            return true;
        } catch (JRException ex) {
            LOGGER.error("exportTaskReportToPdfFile - Exception: ", ex);
            return false;
        }
    }

    /**
     * <b>Chuyen anh chu ky tu thu muc tam vao thu muc that</b><br>
     *
     * @param userId id user
     * @param listStaffImageSign danh sach anh chu ky
     * @return
     */
	public static boolean addLstImageOrg(Long userId, List<EntityImageOrg> listStaffImageSign) {

		// Kiem tra dau vao
		if (userId == null || CommonUtils.isEmpty(listStaffImageSign)) {
			LOGGER.error("moveSignatureImage - Loi du lieu dau vao!");
			return false;
		}
		String storage = CommonUtils.getAppConfigValue("storage.image.signature");
		// Lay thu muc storage luu anh chu ky
		String storageFolder = getStorageFolder(storage);
		if (CommonUtils.isEmpty(storageFolder)) {
			LOGGER.error("moveSignatureImage - Loi lay thu muc luu anh chu ky!");
			return false;
		}
		// Thu muc storage tai len anh chu ky tam
		String tmpUploadStorage, tmpUploadStorageFolder;
		// Lay thu muc upload tam
		String tmpUploadFolder = CommonUtils.getAppConfigValue(TMP_TEXT_UPLOAD_FOLDER_KEY);
		String realPath;
		File srcFile, destFile;
		String signatureImageName;
		EntityImageOrg staffImageSign;
		// Duyet danh sach anh chu ky
		for (Iterator<EntityImageOrg> iterator = listStaffImageSign.iterator(); iterator.hasNext();) {
			staffImageSign = iterator.next();
			try {
				tmpUploadStorage = staffImageSign.getStorage();
				tmpUploadStorageFolder = getStorageFolder(tmpUploadStorage);
				srcFile = new File(
						tmpUploadStorageFolder + _separator + tmpUploadFolder + _separator + staffImageSign.getPath());
				// Tao ten file luu vao thu muc
				signatureImageName = staffImageSign.getName();
				realPath = _separator + CommonUtils.getAppConfigValue("pathUploadImageSinger") + _separator
						+ System.currentTimeMillis() + "_" + signatureImageName;
	            realPath = ConverterUtil.toUnsignedChar(realPath).replaceAll(" ", "_");
				destFile = new File(storageFolder + realPath);
				// Neu file da ton tai
				if (destFile.exists()) {
					destFile.delete();
				}
				org.apache.commons.io.FileUtils.moveFile(srcFile, destFile);
				staffImageSign.setName(signatureImageName);
				staffImageSign.setPath(realPath);
				staffImageSign.setStorage(storage);
			} catch (Exception ex) {
				LOGGER.error("moveSignatureImage - userId: " + userId + " - Exception!", ex);
				iterator.remove();
			}
		}
		
		return !CommonUtils.isEmpty(listStaffImageSign);
	}
	
	public static boolean copyFiles(Long textId, List<String> lstPath, List<String> lstStorateCopy, boolean encrypt) {
	    InputStream is = null;
        OutputStream os = null;
        try {
            if (lstPath != null && lstPath.size() > 0) {
                for (int i = 0; i < lstPath.size(); i++) {
                    // Check file name and MIME type
                    if (!FileUtils.checkSafeFileName(lstPath.get(i)) || !FileUtils.isAllowedType(lstPath.get(i))) {
                        return false;
                    }

                    String keySaveFile = FunctionCommon.getStorageConfigFile("storage_saveFile");
                    String copyStoragePath = FunctionCommon.getPropertiesValue(lstStorateCopy.get(i));

                    Calendar calendar = Calendar.getInstance();

                    String realUploadPath = _separator + "Text" + _separator + calendar.get(Calendar.YEAR)
                            + _separator
                            + (calendar.get(Calendar.MONTH) + 1)
                            + _separator
                            + calendar.get(Calendar.DATE) + _separator + textId;

                    String uploadPath = FunctionCommon.getPropertiesValue(keySaveFile) + realUploadPath;
                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }

                    File tmpFile = new File(copyStoragePath + lstPath.get(i));
                    if (!tmpFile.exists()) {
                        LOGGER.error("FILE NOT EXISTS PATH:  " + copyStoragePath + lstPath.get(i));
                        return false;
                    }

                    String encodeFileName;
                    encodeFileName = extractFileNameNotExt(lstPath.get(i));

                    // Check encode file name
                    File file = new File(uploadPath + _separator + encodeFileName);

                    if (file.exists()) {
                        encodeFileName = "_" + encodeFileName;
                        file = new File(uploadPath + _separator + encodeFileName);
                    }
                    
                    if (encrypt) {
                        is = new FileInputStream(tmpFile);
                        os = new FileOutputStream(file);
                        DES.encrypt(is, os);
                    } else {
                        org.apache.commons.io.FileUtils.copyFile(tmpFile, file);
                    }
                    FunctionCommon.writeLogsNewfile(uploadPath + _separator + encodeFileName);
                }
            } else {
                return false;
            }

            return true;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
                if (os != null) {
                    os.close();
                    os = null;
                }
            } catch (IOException ex) {
                LOGGER.error("moveFile - IOException!", ex);
            }
        }
    }
}
