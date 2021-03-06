/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.report.template;

import java.io.InputStream;

/**
 * Lop quan ly template
 *
 * @author thanght6
 * @since Sep 13, 2016
 */
public class ReportTemplateManager {
    
    /** Template giao viec cho nhan vien default */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE = "bao_cao_giao_nhiem_vu.jasper";
    
    /** Template giao viec cho nhan vien moi */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_1_NEW = "bao_cao_giao_nhiem_vu_1_new.jasper";
    
    /** Template giao viec cho nhan vien */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_1 = "bao_cao_giao_nhiem_vu_1.jasper";
    
     /** Template giao viec cho nhan vien */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_2 = "bao_cao_giao_nhiem_vu_2.jasper";
    
     /** Template giao viec cho nhan vien */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_3 = "bao_cao_giao_nhiem_vu_3.jasper";
    
     /** Template giao viec cho nhan vien */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_4 = "bao_cao_giao_nhiem_vu_4.jasper";
    
     /** Template giao viec cho nhan vien */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_5 = "bao_cao_giao_nhiem_vu_5.jasper";
    
    /** Template giao viec cho hop dong dich vu */
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_COLLABORATOR = "bao_cao_giao_nhiem_vu_HDDV.jasper";

    /** Template danh gia cong viec */
    public static final String TASK_ASSESSMENT_TEMPLATE = "bao_cao_danh_gia_cong_viec.jasper";

    /** Template giao nhiem vu tu bien ban hop */
    public static final String MISSION_ASSIGNMENT_TEMPLATE_FROM_MEETING_MINUTES = "bien_ban_hop_giao_nhiem_vu.jasper";
    
    /** Template phu luc chat */
    public static final String APPENDIX_CHAT_TEMPLATE = "phu_luc_chat.jasper";
    


    /** Phieu giao nhiem vu don vi */
    public static final String MISSION_ASSIGNMENT_TEMPLATE = "phieu_giao_nhiem_vu_don_vi.jasper";
    
    /** Phieu danh gia nhiem vu don vi */
    public static final String MISSION_ASSESSMENT_TEMPLATE = "phieu_danh_gia_nhiem_vu_don_vi.jasper";
    
    /** Cham diem don vi */
    public static final String ORG_RATING_TEMPLATE = "cham_diem_don_vi.jasper";
    /** Template bao cao ki */
    public static final String EMP_RATING_TEMPLATE_FROM_KI = "bang_tong_hop_danh_gia_ket_qua_hoan_thanh.jasper";
    
    /** Datdc Template danh gia cong viec*/
    public static final String TASK_ASSIGNMENT_TEMPLATE_FOR_EMPLOYEE_6 = "bao_cao_giao_nhiem_vu_6_nang_cap.jasper";
    
    public static InputStream getTemplate(String name) {
        InputStream is = ReportTemplateManager.class.getResourceAsStream(name);
        return is;
    }
}
