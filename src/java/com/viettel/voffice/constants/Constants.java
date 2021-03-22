/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

import com.viettel.voffice.utils.CommonUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author thanght6
 */
public class Constants {

    // ---------------------------- Hang so chung ------------------------------
    public static class Common {

        // Dau phay
        public static final String COMMA_CHAR = ",";
        public static final String SPACES_CHAR = "-";
        // khai bao dau hai cham
        public static final String COLON_CHAR = ":";
        // Khai bao dau thang
        public static final String SHARP_CHAR = "#";
        // Chu thuong
        public static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
        // Chu hoa
        public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // Chu so
        public static final String DIGITS = "0123456789";
        // Ky tu dac biet
        public static final String SPECIAL_CHARS = "!@#$%^&*()";
        
        /** Cu phap ghi log function - userId: - special_file: */
        public static final String LOG_SYNTAX = "%s - userId: %d - %s: %d - ";
    }

    // Cac hang so ngay gio    
    public static class DateTime {
        // Thoi gian bat dau ngay

        public static final String BEGIN_TIME_OF_DAY = "00:00:00";
        // Thoi gian ket thuc ngay
        public static final String END_TIME_OF_DAY = "23:59:59";
        // Thoi gian timeout cua thread trong don vi giay
        public static final int TIMEOUT_OF_THREAD_IN_SECOND_UNIT = 20;
    }
    // ---------------- Object type ----------------
    public static final Long OBJECT_TYPE_TASK = 1L;
    public static final Long OBJECT_TYPE_MISSION = 2L;
    public static final Long OBJECT_TYPE_MEETING_MINUTE = 3L;
    public static final Long OBJECT_TYPE_ORIENTATION = 4L;
    public static final Long OBJECT_TYPE_ORIENTATION_MISSION = 5L;
    // ---------------- Source type ----------------

    public static HashMap<Integer, String> getSourceType() {
        HashMap<Integer, String> map_ = new HashMap<>();
        map_.put(1, "Theo kế hoạch cá nhân");
//        SOURCE_TYPE.put(2, "Theo nhiệm vụ");
        map_.put(3, "Theo công việc phối hợp");
        map_.put(4, "Theo văn bản");
        map_.put(5, "Theo nguồn gốc khác");
//        SOURCE_TYPE.put(6, "Theo email");
        map_.put(7, "Theo kiến nghị đề xuất");
        map_.put(8, "Theo nhiệm vụ đơn vị");
        return map_;
    }
    // ---------------- Mission source type --------
    public static final Integer OTHER_MISSION_SOURCE_TYPE = 1;
    public static final Integer DOCUMENT_MISSION_SOURCE_TYPE = 2;
    public static final Integer MEETING_MINUTE_MISSION_SOURCE_TYPE = 3;
    public static final Integer REQUEST_PLANNING_DEPARTMENT_MISSION_SOURCE_TYPE = 4;
    public static final Integer ORIENTATION_MISSION_SOURCE_TYPE = 5;

    public static HashMap<Integer, String> getMissionSourceType() {
        HashMap<Integer, String> map_ = new HashMap<>();
        map_.put(OTHER_MISSION_SOURCE_TYPE, "Theo nguồn gốc khác");
        map_.put(DOCUMENT_MISSION_SOURCE_TYPE, "Theo văn bản");
        map_.put(MEETING_MINUTE_MISSION_SOURCE_TYPE, "Theo biên bản họp");
        map_.put(REQUEST_PLANNING_DEPARTMENT_MISSION_SOURCE_TYPE, "Theo đề xuất của phòng kế hoạch");
        map_.put(ORIENTATION_MISSION_SOURCE_TYPE, "Theo định hướng");
        return map_;
    }
    // ---------------- System role ----------------
    public static final Long SYS_ROLE_SUB_ADMIN = 337211L;
    public static final Long SYS_ROLE_TL = 336871L;
    public static final Long SYS_ROLE_ADMIN = 336815L;
    public static final Long SYS_ROLE_LDDV = 336952L;
    public static final Long SYS_ROLE_TTDV = 336953L;
    public static final Long SYS_ROLE_VT = 336954L;
    public static final Long SYS_ROLE_NV = 336955L;
    public static final Long SYS_ROLE_QLLH = 336991L;
    public static final Long SYS_ROLE_QTHTDV = 1L;
    public static final Long SYS_ROLE_TLCT = 337491L;
    public static final Long SYS_ROLE_BCQS = 41L;
    // Them quyen QLCTH
    public static final Long SYS_ROLE_QLCTH = 337431L;

    // ---------------- Field - Linh vuc -----------
    public static class FIELD {

        public static final Long CT = 1L;
        public static final Long CNTT = 2L;
        public static final Long DTO = 3L;
        public static final Long DT = 4L;
        public static final Long DTTC = 5L;
        public static final Long KTNB = 6L;
        public static final Long KDVT = 7L;
        public static final Long KH = 8L;
        public static final Long KTVT = 9L;
        public static final Long NCSX = 10L;
        public static final Long PC = 11L;
        public static final Long QL = 12L;
        public static final Long QCTT = 13L;
        public static final Long TCKT = 14L;
        public static final Long VP = 15L;
        public static final Long TT = 16L;
        public static final Long TH = 17L;
        public static final Long TCLD = 18L;
        public static final Long XD = 19L;

        public static HashMap<Long, String> getFieldMap() {
            HashMap<Long, String> map_ = new HashMap<>();
            map_.put(CT, "Chính trị");
            map_.put(CNTT, "Công nghệ thông tin");
            map_.put(DTO, "Đào tạo");
            map_.put(DT, "Đầu tư");
            map_.put(DTTC, "Đầu tư tài chính");
            map_.put(KTNB, "Kiểm toán nội bộ");
            map_.put(KDVT, "Kinh doanh viễn thông");
            map_.put(KH, "Kế hoạch");
            map_.put(KTVT, "Kỹ thuật viễn thông");
            map_.put(NCSX, "Nghiên cứu sản xuất");
            map_.put(PC, "Pháp chế");
            map_.put(QL, "Quản lý");
            map_.put(QCTT, "Quảng cáo truyền thông");
            map_.put(TCKT, "Tài chính kế toán");
            map_.put(VP, "Văn phòng");
            map_.put(TT, "Thanh tra");
            map_.put(TH, "Truyền hình");
            map_.put(TCLD, "Tổ chức lao động");
            map_.put(XD, "Xây dựng");
            return map_;
        }
    }

    // ---------------- Frequence update - Tan suat bao cao -----------
    public static HashMap<Integer, String> getFrequenceUpdate() {
        HashMap<Integer, String> map_ = new HashMap<>();
        map_.put(1, "voffice.appConstants.mission.frequencyUpdate.mission.frequencyUpdateMap.cap.nhat.theo.ngay");
        map_.put(2, "voffice.appConstants.mission.frequencyUpdate.mission.frequencyUpdateMap.cap.nhat.theo.tuan");
        map_.put(3, "voffice.appConstants.mission.frequencyUpdate.mission.frequencyUpdateMap.cap.nhat.theo.thang");
        map_.put(4, "voffice.appConstants.mission.frequencyUpdate.mission.frequencyUpdateMap.cap.nhat.theo.quy");
        return map_;
    }
    // ---------------- Mission status - Trang thai nhiem vu -----------
    public static final Integer MISSION_EXPIRED = 0;
    public static final Integer NOT_EXECUTE_MISSION_STATUS = 1;
    public static final Integer EXECUTING_MISSION_STATUS = 2;
    public static final Integer COMPLETED_MISSION_STATUS = 3;
    public static final Integer APPROVED_MISSION_STATUS = 4;
    public static final Integer REQUIRE_CLOSE_MISSION_STATUS = 5;
    public static final Integer CLOSED_MISSION_STATUS = 6;
    public static final Integer MISSION_REQUEST_TIME = 7;
    public static final Integer MISSION_NOT_YET_CLOSED = 8;
    public static final Integer MISSION_TRANSFERRED = 9;
    public static final Integer MISSION_WAIT_APPROVED = 10;
    public static final Integer MISSION_APPROVED = 11;
    public static final Integer MISSION_REJECTED = 12;

    public static HashMap<Integer, String> getMissionStatus() {
        HashMap<Integer, String> map_ = new HashMap<>();
        map_.put(MISSION_EXPIRED, "Chậm tiến độ");
        map_.put(NOT_EXECUTE_MISSION_STATUS, "Chưa thực hiện");
        map_.put(EXECUTING_MISSION_STATUS, "Đang thực hiện");
        map_.put(COMPLETED_MISSION_STATUS, "Đã hoàn thành");
        map_.put(APPROVED_MISSION_STATUS, "Đã kết thúc");
        map_.put(REQUIRE_CLOSE_MISSION_STATUS, "Yêu cầu đóng");
        map_.put(CLOSED_MISSION_STATUS, "Đã đóng");
        map_.put(MISSION_REQUEST_TIME, "Đề xuất gia hạn");
        map_.put(CLOSED_MISSION_STATUS, "Chưa đóng");
        map_.put(CLOSED_MISSION_STATUS, "Đã chuyển");
        map_.put(MISSION_WAIT_APPROVED, "Chờ phê duyệt");
        map_.put(MISSION_APPROVED, "Đã phê duyệt");
        map_.put(MISSION_REJECTED, "Từ chối");
        return map_;
    }
    public static final int CALENDAR_TODAY_TYPE = 1; // Lich ngay hien tai
    public static final int CALENDAR_WEEK_NOW_TYPE = 2; // Lich tuan hien tai
    public static final int CALENDAR_DIRECTOR_TYPE = 3; // Lich hop ban giam doc
    public static final int CALENDAR_TGDTD_TYPE = 5; // Lich hop ban tong giam doc
    public static final int CALENDAR_NEXT_WEEK_TYPE = 4; // Lich hop tuan sau
    public static final Long TYPE_LDPB = 1L; // La lanh dao phong ban
    public static final Long TYPE_TTDV = 2L; // La ban giam doc

    public static class MISSION {

        public static class SOURCE_TYPE {

            public static final Integer OTHER = 1;
            public static final Integer DOCUMENT = 2;
            public static final Integer MEETING_MINUTE = 3;
            public static final Integer REQUEST_PLANNING_DEPARTMENT = 4;
            public static final Integer ORIENTATION_MISSION = 5;
            public static final Integer MISSION = 6;
            public static final Integer DIFFICULT_PROPOSE = 7;
            public static final Integer DEFENCE_DOCUMENT = 8;
            public static final Integer GOVERNMENT_DOCUMENT = 9;

            public static Map<Integer, String> setSourceTypeMap() {
                Map<Integer, String> sourceMapType = new LinkedHashMap<>();
                sourceMapType.put(OTHER, "Theo nguồn gốc khác");
                sourceMapType.put(DOCUMENT, "Theo văn bản");
                sourceMapType.put(MEETING_MINUTE, "Theo biên bản họp");
                sourceMapType.put(REQUEST_PLANNING_DEPARTMENT, "Theo đề xuất của phòng kế hoạch");
                sourceMapType.put(ORIENTATION_MISSION, "Theo định hướng");
                sourceMapType.put(MISSION, "Theo nhiệm vụ đơn vị");
                sourceMapType.put(DIFFICULT_PROPOSE, "Theo kiến nghị - đề xuất");
                sourceMapType.put(DEFENCE_DOCUMENT, "Theo văn bản Quốc phòng");
                sourceMapType.put(GOVERNMENT_DOCUMENT, "Theo văn bản Chính phủ");
                return sourceMapType;
            }
        }

        public static class STATUS {

            public static final Long NOT_EXECUTE = 1L;
            public static final Long EXECUTING = 2L;
            public static final Long COMPLETED = 3L;
            public static final Long APPROVED = 4L;
            public static final Long REQUEST_CLOSE = 5L;
            public static final Long CLOSED = 6L;
            public static final Integer INOT_EXECUTE = 1;
            public static final Integer IEXECUTING = 2;
        }

        public static class STATUS_APPROVED {

            public static final Integer INVISIBLE = 1;
            public static final Integer APPROVED = 2;
            public static final Integer REJECT = 3;
        }
    }

    public static class MISSION_PROCESS {

        public static class STATUS {

            public static final Long EXECUTING = 2L;
            public static final Long COMPLETED = 3L;
            public static final Long REQUEST_CLOSE = 5L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(EXECUTING, "Đang thực hiện");
                map_.put(COMPLETED, "Đã hoàn thành");
                map_.put(REQUEST_CLOSE, "Yêu cầu đóng");
                return map_;
            }
        }

        public static class APPROVE_STATUS {

            public static final Long WAITING = 1L;
            public static final Long APPROVED = 2L;
            public static final Long REJECTED = 3L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(WAITING, "Chờ phê duyệt");
                map_.put(APPROVED, "Đã phê duyệt");
                map_.put(REJECTED, "Từ chối");
                return map_;
            }
        }
    }

    // ============================ Cong viec ca nhan ==========================
    public static class TASK {

        public static class COMMAND_TYPE {

            public static final Long PERFORM = 1L;
            public static final Long PERSONAL = 2L;
            public static final Long TRANSFERRED = 3L;
            public static final Long COMBINATION = 4L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(PERFORM, "Công việc được giao");
                map_.put(PERSONAL, "Cá nhân đề xuất");
                map_.put(TRANSFERRED, "Công việc đã bàn giao");
                map_.put(COMBINATION, "Công việc phối hợp");
                return map_;
            }
        }

        public static class TASK_TYPE {

            public static final Long FUNCTIONAL = 1L;
            public static final Long ORDERLY = 2L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(FUNCTIONAL, "Công việc chức năng");
                map_.put(ORDERLY, "Công việc nề nếp");
                return map_;
            }
        }

        public static class TASK_TYPE_2 {

            public static final Long FREQUENTLY = 1L;
            public static final Long UNEXPECTED = 2L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(FREQUENTLY, "Công việc thường xuyên");
                map_.put(UNEXPECTED, "Công việc đột xuất");
                return map_;
            }
        }

        public static class STATUS {

            public static final Long NOT_EXECUTE = 1L;
            public static final Long EXECUTING = 2L;
            public static final Long COMPLETED = 3L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(NOT_EXECUTE, "Chưa thực hiện");
                map_.put(EXECUTING, "Đang thực hiện");
                map_.put(COMPLETED, "Đã hoàn thành");
                return map_;
            }
        }

        public static class RECEIVER {

            public static final Long NOT_RECEIVE = 1L;
            public static final Long RECEIVED = 2L;
            public static final Long REJECTED = 3L;

            public static Map<Long, String> getMap() {
                Map<Long, String> map_ = new HashMap<>();
                map_.put(NOT_RECEIVE, "Chưa tiếp nhận");
                map_.put(RECEIVED, "Đã tiếp nhận");
                map_.put(REJECTED, "Từ chối");
                return map_;
            }
        }

        public static class SourceType {

            public static final Integer PLAN_PERSON = 1;
//            public static final Integer FLOW_MISSION = 2;
            public static final Integer TASK = 3;
            public static final Integer DOCUMENT = 4;
            public static final Integer OTHER = 5;
//            public static final Integer EMAIL = 6;
            public static final Integer REQUEST = 7;
            public static final Integer MISSION = 8;

            public static Map<Integer, String> getMap() {
                Map<Integer, String> map_ = new HashMap<>();
                map_.put(PLAN_PERSON, "Theo kế hoạch cá nhân");
                // MAP.put(FLOW_MISSON, "Theo nhiệm vụ");
                map_.put(TASK, "Theo công việc phối hợp");
                map_.put(DOCUMENT, "Theo văn bản");
                map_.put(OTHER, "Theo nguồn gốc khác");
                // MAP.put(EMAIL, "Theo email");
                map_.put(REQUEST, "Theo kiến nghị đề xuất");
                map_.put(MISSION, "Theo nhiệm vụ đơn vị");
                return map_;
            }
        }
    }

    public static class TASK_APPROVAL {

        public static class STATE {

            /** Phe duyet giao viec */
            public static final Long APPROVED = 1L;
            
            /** Tu choi giao viec */
            public static final Long REJECTED = 2L;
            
            /** Phe duyet danh gia viec */
            public static final Long ASSESSED = 3L;
            
            /** Tu choi danh gia viec */
            public static final Long REJECTED_ASSESS = 4L;

            public static HashMap<Long, String> getMap() {
                HashMap<Long, String> map_ = new HashMap<>();
                map_.put(APPROVED, "Phê duyệt");
                map_.put(REJECTED, "Từ chối");
                map_.put(ASSESSED, "Phê duyệt đánh giá");
                map_.put(REJECTED_ASSESS, "Từ chối đánh giá");
                return map_;
            }
        }
    }

    public static class TaskRating {

        // Trang thai
        public static class Status {

            // Nhan vien tu danh gia
            public static final int EMPLOYEE_SELF_ASSESSED = 1;
            // Lanh dao danh gia
            public static final int LEADER_ASSESSED = 2;
            // Lanh dao ky danh gia
            public static final int LEADER_SIGNED_ASSESSMENT = 3;
        }
    }

    /**
     * Ma loi tra ve cua ki tren di dong
     */
    public interface SIGN_RESULT_CODE {
        // quy trinh thanh cong

        public static final Long SUCCESS = 1L;
        // file van ban khong ton tai
        public static final Long FILE_NOT_EXIST = 2L;
        public static final String FILE_NOT_EXIST_TEXT = "Lỗi! File không tồn tại";
        // ki MSSP bi loi (ket noi, loi he thong...)
        public static final Long SIGN_MSSP_ERROR = 3L;
        // xac thuc chu ki khong dung
        public static final Long CA_VERIFY_NOT_CORRECT = 4L;
        // loi cap nhat db
        public static final Long UPDATE_DB_ERROR = 5L;
        // nguoi dung khong co CA SIM
        public static final Long NO_SIM_CA = 6L;
        public static final String NO_SIM_CA_TEXT = "Lỗi! Người dùng không có CA SIM";
        // nguoi dung khong co so CMND
        public static final Long NO_CMND_USER = 7L;
        // nguoi dung khong co CA SIM
        public static final Long NO_SIM_SERIAL = 8L;
        public static final String NO_SIM_SERIAL_TEXT = "Lỗi! Không có serial Sim";
        //File da duoc ky
        public static final Long FILE_SIGNED = 9L;
        //Loi khong lay dc chung thu
        public static final Long CERTEIFICATE_CREATE = 10L;
        public static final String CERTEIFICATE_CREATE_TEXT = "Lỗi! không lấy được chứng thư";
        //Ky khong thanh cong do ko nhan dc ket qua xac nhan tu tin nhan
        public static final Long NO_SUCCESS = 11L;
        //Dang xu ly ky
        public static final Long STATUS_DELAY_SIGN = 417L;
        // chung thu het han
        public static final Long ERROR_EXPIRE_CER = 12L;
        // chung thu bi thu hoi
        public static final Long ERROR_CER_BITHUHOI = 13L;
        // timeouw ket noi MSSP
        public static final Long MSSP_NO_CONNECTION = 14L;
        // timout thoi gian xu li nguoi dung
        public static final Long MSSP_TIMEOUT = 15L;
        // khong co quyen ki nhieu van ban
        public static final Long NO_PERMISS_SIGN_MULTI = 16L;
        // ki khong thanh cong het
        public static final Long NO_SUCCESS_FULL = 17L;
        // chua co thong tin SIM CA tren he thong MSSP
        public static final Long UNKNOWN_CLIENT = 18L;
        // khong du space
        public static final Long NOT_ENOUGH_SPACE = 19L;
        // nguoi dung khong ki
        public static final Long USER_NOT_SIGN = 20L;
        // dinh dang file khong dung
        public static final Long FILE_FORMAT_ERROR = 21L;
        // dinh dang chu ki bi loi
        public static final Long SIGNATURE_FORMAT_ERROR = 22L;
        // file chua duoc ki
        public static final Long FILE_IS_NOT_SIGNED = 23L;
        // chung thu het han
        public static final Long EXPIRE_CERTIFICATE = 24L;
        // chung thu bi thu hoi
        public static final Long REVOKED_CERTIFICATE = 25L;
        // chung thu khong duoc ho tro kiem tra online
        public static final Long CERTIFICATE_IS_NOT_SUPPORT_CHECK_ONLINE = 26L;
        // chung thu khong duoc ho tro boi VIETTEL CA
        public static final Long UNKNOWN_ONLINE_STATUS = 27L;
        // khong ket noi duoc den he thong VIETTEL CA
        public static final Long CANNOT_CONNECT_VIETTELCA = 28L;
        // chu ki tra ve khong du
        public static final Long WRONG_SIGNATURE_LENGTH = 29L;
        // dinh dang chung thu khong dung
        public static final Long WRONG_CER_FORMAT = 30L;
        // Van ban da da duoc xy ly tu truoc
        public static final Long DOC_IS_PROCESS = 31L;
        // Sai mat khau chung thu
        public static final Long WRONG_CRT_PASSWORD = 57005L;
        /*
         * MA LOI KY DIEN TU SIMCA 2.0
         *
         * lay tu ma chinh cua MSSPv2.0 tra ve + 2000 (20 => version2.0)
         *
         * convert mot so ma chung sang voi MSSPv1.2
         */
        //MSSPV2.0: ORGERRCODE_COMPLETE = "00"; //---------------------------Giao dịch thành công
        public static final Long MSSPV20_COMPLETE = SUCCESS;
        //MSSPV2.0: ORGERRCODE_REQUESTOK = "01"; //--------------------------Yêu cầu đã được chấp nhận trên hệ thống
        public static final Long MSSPV20_REQUESTOK = 2001L;
        //MSSPV2.0: ORGERRCODE_INPUT_INVALID = "12"; //----------------------Lỗi tham số đầu vào, thiếu thông tin dữ liệu bắt buộc hoặc dữ liệu không đúng định dạng
        public static final Long MSSPV20_INPUT_INVALID = SIGN_MSSP_ERROR; //lam ro lai
        //MSSPV2.0: ORGERRCODE_MSISDN_INVALID = "13"; //---------------------Số thuê bao không đúng định dạng hoặc không tồn tại
        public static final Long MSSPV20_MSISDN_INVALID = NO_SIM_CA;
        //MSSPV2.0: ORGERRCODE_MSISDN_WRONG = "14"; //-----------------------Số điện thoại không giống với giao dịch ký đã yêu cầu
        public static final Long MSSPV20_MSISDN_WRONG = NO_SIM_CA;
        //MSSPV2.0: ORGERRCODE_PROCESSCODE_INVALID = "15"; //----------------Mã dịch vụ không hợp lệ (PROCESS_CODE)
        public static final Long MSSPV20_PROCESSCODE_INVALID = NO_SUCCESS; //lam ro lai
        //MSSPV2.0: ORGERRCODE_MSG_DUPLICATED = "16"; //---------------------Bản tin trùng lặp
        public static final Long MSSPV20_MSG_DUPLICATED = NO_SUCCESS;
        //MSSPV2.0: ORGERRCODE_MSSFORMAT_NOTSUPPORTED = "17"; //-------------Không hỗ trợ định dạng dữ liệu (MSSFormat không đúng)
        public static final Long MSSPV20_MSSFORMAT_NOTSUPPORTED = NO_SUCCESS; //lam ro lai
        //MSSPV2.0: ORGERRCODE_MSGMODE_NOTSUPPORTED = "18"; //---------------Cơ chế gửi nhận bản tin không được hỗ trợ
        public static final Long MSSPV20_MSGMODE_NOTSUPPORTED = NO_SUCCESS; //lam ro lai
        //MSSPV2.0: ORGERRCODE_CERT_INVALID = "21";
        public static final Long MSSPV20_CERT_INVALID = UNKNOWN_CLIENT; //loi cu: ERROR_EXPIRE_CER
        //MSSPV2.0: ORGERRCODE_CERT_SERIAL_INVALID = "22"; //----------------Serial của chứng thư số không đúng
        public static final Long MSSPV20_CERT_SERIAL_INVALID = ERROR_EXPIRE_CER; //lam ro lai
        //MSSPV2.0: ORGERRCODE_HASH_INVALID = "27"; //-----------------------Chuỗi hash văn bản không hợp lệ
        public static final Long MSSPV20_HASH_INVALID = SIGNATURE_FORMAT_ERROR; //lam ro lai
        //MSSPV2.0: ORGERRCODE_CLIENT_FORBIDDEN = "29"; //-------------------Client không được phép thực hiện giao dịch này
        public static final Long MSSPV20_CLIENT_FORBIDDEN = UNKNOWN_CLIENT;
        //MSSPV2.0: ORGERRCODE_HINHTHUCKY_INVALID = "30"; //-----------------Hình thức giao dịch (ký văn bản, ký giao dịch) của client không đúng
        public static final Long MSSPV20_HINHTHUCKY_INVALID = NO_SUCCESS; //lam ro lai
        //MSSPV2.0: ORGERRCODE_CLIENT_UNAUTHORIZED = "31"; //----------------Client xác thực không hợp lệ
        public static final Long MSSPV20_CLIENT_UNAUTHORIZED = UNKNOWN_CLIENT;
        //MSSPV2.0: ORGERRCODE_USER_CANCELED = "40"; //----------------------Người sử dụng từ chối ký điện tử
        public static final Long MSSPV20_USER_CANCELED = USER_NOT_SIGN;
        //MSSPV2.0: ORGERRCODE_TRANS_CANCELED = "50"; //---------------------Giao dịch ký đã bị hủy
        public static final Long MSSPV20_TRANS_CANCELED = USER_NOT_SIGN; //lam ro lai
        //MSSPV2.0: ORGERRCODE_TRANS_ALREADY_CANCELED = "52"; //-------------Giao dịch ký đã bị hủy thành công trước đó
        public static final Long MSSPV20_TRANS_ALREADY_CANCELED = USER_NOT_SIGN;
        //MSSPV2.0: ORGERRCODE_TRANS_NOTFOUND = "55"; //---------------------Giao dịch trước đó không tồn tại
        public static final Long MSSPV20_TRANS_NOTFOUND = NO_SUCCESS;
        //MSSPV2.0: ORGERRCODE_SENTTOSIM_FAILED = "70"; //-------------------Không gửi được yêu cầu xuống SIM
        public static final Long MSSPV20_SENTTOSIM_FAILED = SIGN_MSSP_ERROR;
        //MSSPV2.0: ORGERRCODE_SENTTOSIM_FAILED_FULLMEM = "71"; //------------Không gửi được yêu cầu xuống Sim xử lý do bộ nhớ của Sim đã đầy
        public static final Long MSSPV20_SENTTOSIM_FAILED_FULLMEM = SIGN_MSSP_ERROR;
        //MSSPV2.0: ORGERRCODE_MSISDN_UNREGISTERED = "72"; //----------------Sim chưa thực hiện đăng ký với MSSP
        public static final Long MSSPV20_MSISDN_UNREGISTERED = NO_SIM_CA;
        //MSSPV2.0: ORGERRCODE_CANNOT_SENTRESULT_TO_CLIENT = "73"; //--------Không thể gửi kết quả về cho client
        public static final Long MSSPV20_CANNOT_SENTRESULT_TO_CLIENT = NO_SUCCESS_FULL; //lam ro lai
        //MSSPV2.0: ORGERRCODE_SERVICE_NOTAVAIABLE = "74"; //----------------Dịch vụ đã bị khóa hoặc bị hủy bỏ
        public static final Long MSSPV20_SERVICE_NOTAVAIABLE = MSSP_NO_CONNECTION; //lam ro lai
        //MSSPV2.0: ORGERRCODE_SYSTEM_UPGRADING = "80"; //-------------------Hệ thống đang nâng cấp
        public static final Long MSSPV20_SYSTEM_UPGRADING = MSSP_NO_CONNECTION; //lam ro lai
        //MSSPV2.0: ORGERRCODE_OTHER_UNKNOWN = "99"; //----------------------Lỗi ngoài danh mục mô tả, UNKNOWN
        public static final Long MSSPV20_OTHER_UNKNOWN = SIGN_MSSP_ERROR;
        //MSSPV2.0: ORGERRCODE_OTHER_TIMEOUT = "98"; //----------------------Lỗi ngoài danh mục mô tả, TIMEOUT
        public static final Long MSSPV20_OTHER_TIMEOUT = MSSP_TIMEOUT;
        //MSSPV2.0: ORGERRCODE_OTHER_NORESP = "97"; //-----------------------Lỗi ngoài danh mục mô tả, NO RESPONSE
        public static final Long MSSPV20_OTHER_NORESP = SIGN_MSSP_ERROR;
        //MSSPV2.0: ORGERRCODE_OTHER_NOSIGNATURE = "96"; //------------------Lỗi ngoài danh mục mô tả, NO SIGNATURE
        public static final Long MSSPV20_OTHER_NOSIGNATURE = SIGNATURE_FORMAT_ERROR;
        //MSSPV2.0: ORGERRCODE_OTHER_INVALIDSIGNATURE = "95"; //-------------Lỗi ngoài danh mục mô tả, NO SIGNATURE
        public static final Long MSSPV20_OTHER_INVALIDSIGNATURE = SIGNATURE_FORMAT_ERROR;
        /* KET THUC - MA LOI KY DIEN TU SIMCA 2.0 */
        /**
         * 2015-07-31 chucvq ma loi verify chu ki khong ok
         */
        public static final Long VERIFY_SIGNATURE_FALSE = 99L;
    }

    /**
     * Ma loi tra ve cua tu choi ky
     */
    public interface REJECT_SIGN_RESULT_CODE {
        //Tu choi ky thanh cong

        public static final String REJECT_SUCCESS = "1";
        //Tu choi ky khong thanh cong
        public static final String NO_REJECT_SUCCESS = "-1";
    }

    // ============================= Van ban ===================================
    /**
     * Hang so trong phan van ban
     */
    public static class Text {

        /**
         * Cac loai tim kiem
         */
        public static class SearchType {

            /**
             * Tat ca
             */
            public static final int ALL = 0;
            /**
             * Tim kiem van ban xet duyet
             */
            public static final int SECRETARY_SIGN = 2;
            /**
             * Tim kiem van ban ky duyet
             */
            public static final int SIGNED = 3;
            /**
             * TIM KIEM VAN BAN BAN HAN
             */
            public static final int PUBLISHED_SIGN = 4;
            /* Van ban da ky nhay
             */
            public static final int INITIAL_SIGNED = 4;
            /**
             * Van ban da tu choi ky duyet
             */
            public static final int REJECTED_SIGN = 5;
            /**
             * Van ban da tu choi ky nhay
             */
            public static final int REJECTED_INITIAL_SIGN = 6;
            /**
             * Van ban trinh ky
             */
            public static final int TEXT_ASSIGN = 7;
            /**
             * Van ban khong phai tai chinh
             */
            public static final int NON_FINANCIAL = 0;
            /**
             * Van ban tai chinh
             */
            public static final int FINANCIAL = 1;
            // 201812-Pitagon: add
            public static final int MARK = 8;
        }

        /**
         * Trang thai
         */
        public static class State {

            /**
             * Tao moi
             */
            public static final int NEW = 0;

            /**
             * Dang xu ly
             */
            public static final int PROCESSING = 1;

            /**
             * Bi tu choi
             */
            public static final int REJECTED = 2;

            /**
             * Da phe duyet (Tat ca nguoi tham gia da ky)
             */
            public static final int APPROVED = 3;

            /**
             * Da ban hanh
             */
            public static final int PUBLISHED = 4;

            /**
             * Cho ky nhay
             */
            public static final int WAITING_FLASH_SIGN = 5;

            /**
             * Da huy luong cong van
             */
            public static final int CANCELED = 6;
            /**
             * Tu choi ky nhung trinh lai
             */
            public static final int REJECTBUTRESIGN = 7;
            /**
             * Da huy ban hanh
             */
            public static final int CANCEL_PUBLISHED = 27;
        }
        /**
         * Tu khoa id loai van ban tai chinh trong file cau hinh
         */
        public static final String FINANCIAL_DOCUMENT_TYPE_ID_KEY = "id.type.document.financial";
        public static final String NORMAL_DOCUMENT_STYPE_ID_KEY = "id.stype.document.normal";
        public static final String SECRET_DOCUMENT_STYPE_ID_KEY = "id.stype.document.secret";
    }

    // =========================== Xu ly van ban ===============================
    /**
     * Xu ly van ban
     */
    public static class TextProcess {

        /**
         * Trang thai xu ly van ban
         */
        public static class State {

            /**
             * Chua xu ly
             */
            public static final int NOT_HANDLE = 0;
            /**
             * Van thu tu choi
             */
            public static final int SECRETARY_REJECTED = 1;
            /**
             * Van thu da xet duyet chuyen lanh dao nhung lanh dao tu choi
             */
            public static final int LEADER_REJECTED = 2;
            /**
             * Lanh dao co van thu da xet duyet nhung chua ky
             */
            public static final int SECRETARY_SIGNED = 3;
            /**
             * Lanh dao da ky
             */
            public static final int LEADER_SIGNED = 4;
            /**
             * Cho ky nhay (Cua nguoi ky chinh)
             */
            public static final int INITIAL_SIGNING = 5;
            //vo hieu hoa ky nhay
            public static final int DENY_INITIAL_SIGNING = 6;
            /**
             * Da ky nhay (Cua nguoi ky chinh)
             */
            public static final int INITIAL_SIGNED = 45;
            /**
             * Tu choi ky nhay
             */
            public static final int INITIAL_REJECTED = 25;
            /**
             * Tim kiem tat cat ky chinh tren web
             */
            public static final int TEXT_SIGN_ALL = -1;
            /**
             * Tim kiem tat ca ky nhay
             */
            public static final int TEXT_INITIAL_ALL = -2;
            // tim kiem van ban da ky nhung lanh dao cap tren tu choi ky
            public static final int TEXT_SEARCH_TYPE_LEADER_DENNY_SIGNED = 7;
            /**
             * Tim kiem tat cat ky chinh tren mobile
             */
            public static final int TEXT_SIGN_ALL_FOR_MOBILE = -3;
            /**
             * Hiendv2: Bo sung trang thai la van ban ky song song
             */
            public static final int STATE_SIGN_PRALLEL = -1;
        }

        /**
         * Loai ky
         */
        public static class SignatureType {

            /**
             * Xet duyet
             */
            public static final int APPROVE = 1;
            /**
             * Ky nhay
             */
            public static final int INITIAL_SIGN = 2;
            /**
             * Ky chinh
             */
            public static final int MAIN_SIGN = 3;
        }
    }

    // =============================== Vai tro =================================
    public static class Role {

        /**
         * Tu khoa lay ra danh sach id vai tro lanh dao cong ty he thong 1 trong
         * file cau hinh
         */
        public static final String COMPANY_LEADER_ID_KEY = "id.role.leader.company";
        /**
         * Tu khoa lay ra danh sach id vai tro lanh dao phong ban he thong 1
         * trong file cau hinh
         */
        public static final String DEPARTMENT_LEADER_ID_KEY = "id.role.leader.department";
        // Tu khoa lay id vai tro thu truong don vi trong file cau hinh
        public static final String CEO_ROLE_ID_KEY = "id.role.ceo";
        // Tu khoa lay id vai tro van thu trong file cau hinh
        public static final String SECRETARY_ROLE_ID_KEY = "id.role.secretary";
    }

    // ========================== Cau hinh thoi gian ===========================
    /**
     * Cau hinh thoi gian
     */
    public static class TimeConfig {

        /**
         * Loai
         */
        public static class Type {

            /**
             * Thang truoc
             */
            public static final int LAST_MONTH = 0;
            /**
             * Thang hien tai
             */
            public static final int CURRENT_MONTH = 1;
            /**
             * Thang sau
             */
            public static final int NEXT_MONTH = 2;
        }
    }

    public interface TEXT_PROCESS_DEFINE {

        public static final int TEXT_ACTION_STATE_LD_SIGNER = 4;
        // ki tu SIM
        public static final long SIGN_FROM_SIM = 2;
        public static final int TEXT_STATE_NEW_CREATE = 0; // moi tao
        public static final int TEXT_STATE_PROCESSING = 1; // Dang xy ly
        public static final int TEXT_STATE_REJECTED = 2; // Dang xy ly
        public static final int TEXT_STATE_APPROVED = 3; // Dc phe duyet ( tat ca moi nguoi tham gia deu ky)
        public static final int TEXT_STATE_PUBLISHED = 4; // Da dc ban hanh
        public static final int TEXT_STATE_SIGNDRAFF = 5; // Cho ky nhay
        public static final int TEXT_STATE_IS_CANCEL = 6; // Cong van bi huy
        public static final int TEXT_STATE_SIGNDRAFF_COMPLETE = 7; // Cho ky nhay
        public static final int TEXT_ACTION_STATE_NOT_RESPOND = 0; // Van thu chua xu ly
        public static final int TEXT_ACTION_STATE_VT_REJECTED = 1; // Van thu tu choi
        public static final int TEXT_ACTION_STATE_LD_REJECTED = 2; // Da tu choi
        public static final int TEXT_ACTION_STATE_VT_SIGNER = 3; // Van thu da ky
        public static final int TEXT_STATE_SEND_TO_DEPARTMENT = 1; // gui len lanh dao phong
        public static final int TEXT_STATE_SEND_DEPARTMENT_SIGNED = 2; // lanh dao phong ky
        public static final int TEXT_STATE_SEND_DEPARTMENT_REJECTED = 3; // lanh dao phong tu choi
        public static final int TEXT_STATE_SEND_TO_CENTER = 8; // gui toi trung tam
        public static final int TEXT_STATE_SEND_TO_CENTER_SIGNED = 9; // trung tam da ly
        public static final int TEXT_STATE_SEND_TO_CENTER_REJECTED = 10; // trung tam tu choi
        public static final int TEXT_STATE_SEND_TO_COMPANY = 16; // gui toi cong ty
        public static final int TEXT_STATE_SEND_TO_COMPANY_SIGNED = 17; // cong ty da ky
        public static final int TEXT_STATE_SEND_TO_COMPANY_REJECTED = 18; // cong ty tu choi
        public static final int TEXT_STATE_SEND_TO_CORPORATION = 24; // gui toi tap doan
        public static final int TEXT_STATE_SEND_TO_CORPORATION_SIGNED = 25; // tap doan da ky
        public static final int TEXT_STATE_SEND_TO_CORPORATION_REJECTED = 26; // tap doan tu choi
        public static final int TEXT_PROCESS_STATE_NEW = 0; // moi tao
        public static final int TEXT_PROCESS_STATE_REJECT = 2; // tu choi
        public static final int TEXT_PROCESS_STATE_APPROVE = 4; // da ky
        public static final int TEXT_STATE_APPROVED_CANCEL = 27; // Huy ban hanh
        public static final long SIGN_FROM_APPROVED_DOC = 0;
        public static final long SIGN_FROM_SIM_PKCS2 = 4;//Ky bang file mem
        public static final long SIGN_FROM_SIM_1 = 2;//Ky bang file mem
        public static final long SIGN_FROM_SIM_2 = 3;//Ky bang file mem
    }

    /**
     * Cau hinh cho bang source_map
     */
    public static class SourceMap {

        /**
         * Loai doi tuong
         */
        public static class ObjectType {

            public static final Integer TASK = 1;
            public static final Integer MISSION = 2;
            public static final Integer MEETING_MINUTE = 3;
            public static final Integer ORIENTATION = 4;
            public static final Integer ORIENTATION_MISSION = 5;
            //cuongnv:: 15/12/2016 ::Lay vb tham chieu
            public static final Integer MISSION_DOCUMENT_REF = 6;
            // Tien do cong viec
            public static final Integer TASK_PROCESS = 8;
            
            private static HashMap<Integer, String> getMap() {
                HashMap<Integer, String> map_ = new HashMap<>();
                map_.put(TASK, "Công việc cá nhân");
                map_.put(MISSION, "Nhiệm vụ đơn vị");
                map_.put(MEETING_MINUTE, "Biên bản họp");
                map_.put(ORIENTATION, "Định hướng");
                map_.put(ORIENTATION_MISSION, "Nhiệm vụ định hướng");
                return map_;
            }
        }
    }

    //vinhnq13 catalory gui tin nhan ky dien tu
    public interface SMS_TEXT_CONFIG {

        public static final Long CREATE_NEW_TEXT = 1L; // trinh ky van ban
        public static final Long LEADER_MAIN_SIGN_TEXT = 2L; //ky duyet van ban
        public static final Long LEADER_REJECT_TEXT = 3L; // tu choi ky duyet van ban
        public static final Long LEADER_FLASH_SIGN_TEXT = 4L;// Ky nhay
        public static final Long LEADER_REJECT_FLASH_SIGN_TEXT = 5L; // tu choi ky nhay
        public static final Long SECRETARY_SIGN_TEXT = 6L; // Van thu xet duyet
        public static final Long SECRETARY_REJECT_TEXT = 7L; // Van thu tu choi xet duyet
        public static final Long TRANSFER_SIGN_TEXT = 8L; // chuyen ky nhay
        public static final Long PUBLISH_DOC = 9L; // Ban hanh van ban
        public static final Long CANCEL_THREAD_DOC = 10L; // Huy luong cong van
        public static final Long CANCEL_PUBLIC_DOC = 11L; // Huy ban hanh
        public static final Long AUTO_PUBLIC_DOC = 12L; // Ban hanh tu dong
        public static final Long AUTO_SEND_DOC = 23L; // Tu dong chuyen van ban
        public static final Long CREATE_NEW_REQUEST = 14L; // Gui kien nghi de xuat
        public static final Long REQUEST_RESOLVE = 15L; // Dong kien nghi de xuat
        public static final Long DISAGREE_RESOLVE = 16L; // Tu choi kien nghi de xuat
        public static final Long DISAGREE_RESOLVE_AND_FORWARD = 17L; // Tu choi kien nghi de xuat va gui len cap tren
        public static final Long SEND_DOC_TO_STAFF = 21l; // Tu choi kien nghi de xuat va gui len cap tren
        public static final Long MESSAGE_LANGUAGE_MUTI_SMS = 18L; //loai tin nhan gop
        public static final Long MESSAGE_LANGUAGE_MUTI_ADD_COMMENT_SMS = 19L; //loai tin nhan gop bo sung y kien chi dao
        public static final Long LEADER_MAIN_SIGN_TEXT_ADD_COMMENT = 20L;
        public static final Long LEADER_FLASH_SIGN_TEXT_ADD_COMMENT = 22L;
        public static final Long SECRETARY_SIGN_TEXT_ADD_COMMENT = 26L; // Van thu xet duyet
        // Thay nguoi ky
        public static final Long REPLACE_SIGNER = 27L;
        // Lay ma OTP de thay mat khau chung thu
        public static final Long OTP_CODE_FOR_RESET_CERTIFICATE_PASSWORD = 28L;
        // Mat khau chung thu moi
        public static final Long NEW_CERTIFICATE_PASSWORD = 29L;
        // Thay doi trang trai cap trinh ky
        public static final Long UPDATE_STATUS_SIGNING_BRIEFCASE = 30L;
        //080317 gui tin nhan canh bao trinh ky lai cho nguoi trinh 
        public static final Long LEADER_REJECT_TEXT_WARNING_TO_CREATOR = 33L; //lanh dao tu choi
        public static final Long SECRETARY_REJECT_TEXT_WARNING_TO_CREATOR = 34L; //van thu tu choi
        //Tin nhan cong viec ca nhan
        public static final Long PERSON_TASK_ASSGIN = 1L; //Giao viec
        public static final Long PERSON_TASK_SIGN_FIST_MONTH = 2L; //Phe duyet dau thang
        public static final Long PERSON_TASK_ASSESSMENT_LAST_MONTH = 3L; //Ky danh gia cuoi thang
        // Nguoi giao chinh sua cong viec
        public static final Long ASSIGNER_UPDATE_TASK = 4L;
        
        // Nguoi giao phe duyet cong viec
        public static final Long ASSIGNER_APPROVE_TASK = 5L;
        
        // Nguoi giao tu choi cong viec
        public static final Long ASSIGNER_REJECT_TASK = 6L;

        //Bo sung loai tin nhan tu choi khi co comment file
        public static final Long LEADER_REJECT_TEXT_COMMENT_FILE = 38L;
        public static final Long LEADER_REJECT_TEXT_COMMENT_FILE_1 = 39L;
        public static final Long SECRETARY_REJECT_COMMENT_FILE = 40L;
        public static final Long SECRETARY_REJECT_COMMENT_FILE_1 = 41L;
        public static final Long LEADER_REJECT_WARNING_TO_CREATOR_COMMENT_FILE = 42L;
        public static final Long LEADER_REJECT_WARNING_TO_CREATOR_COMMENT_FILE_1 = 43L;
        public static final Long SECRETARY_REJECT_WARNING_TO_CREATOR_COMMENT_FILE = 44L;
        public static final Long SECRETARY_REJECT_WARNING_TO_CREATOR_COMMENT_FILE_1 = 45L;
        // Tin nhan de nghi ky thay
        public static final Long OFFER_TO_SIGN_INSTEAD = 46L;
        //pm1_os20 add: Tin nhan ban giao ho so
        public static final Long BRIEF_PROCESS_ASSGIN = 48L;
        public static final Long MOIT_REQUEST_SIGN = 49L;
        public static final Long MOIT_SEND_DOCUMENT = 50L;
        // 201812-Pitagon: add
        public static final Long ASK_FOR_REAL = 51L; //Yeu cau dong dau
        public static final Long MARK_SUCCESS = 52L; //Da dong dau
        public static final Long REJECT_MARK = 53L; //Tu choi dong dau
        public static final Long ASK_FOR_REPLY = 54L; //Yeu cau tra loi van ban
        public static final Long WRITTEN_RESPONSE = 55L; //Tra loi bang van ban thanh cong
        public static final Long CANCEL_ASK_FOR_REPLY = 56L; //Thu hoi yeu cau tra loi bang van ban
        public static final Long ROLLBACK_MARK_DOC = 57L; //Rollback dong dau van ban
    }

    //datnv5: ma chan tin nhan
    public interface SMS_TEXT_INTERCEPT {

        //================tin nhan trinh ky================
        //nhan tin nhan cua nguoi trinh hoac nguoi ky truoc
        public static final Long TOSUBMIT = 101L;
        //nguoi trinh nhan tin nhan cua nguoi ky hoac reject
        // public static final Long CREATOR_SIGNANDREJECT = 102L;
        //gui tin nhan cho nguoi ky truoc
        public static final Long SIGNER_SMSBEFORSIGN = 103L;
        //gui tin nhan cho nguoi ky nhay
        public static final Long INITIAL_SMSSIGNMAIN = 104L;
        //tin nhan KY DUYET/TU CHOI
        public static final Long SIGNMAIN_SMSSIGNER_REJECT = 105L;
        //van ban cho ban hanh
        public static final Long TOWAITPROMULGATE = 106L;
        //tin nhan ban hanh cho nguoi trinh ky
        public static final Long CREATOR_PROMULGATE = 107L;
        //tin nhan ban hanh van ban cho van thu
        public static final Long SECRETARY_PROMULGATE = 107L;
        //================tin nhan Van ban================        
        //canh bao ban hanh
        public static final Long WANINGPROMULGATE = 108L;
        //cap trinh ky
        public static final Long BRIEFCASE = 110L;

        /** Tin nhan thay nguoi ky */
        public static final Long REPLACE_SIGNER = 111L;
        
        /** Huy ban hanh */
        public static final Long CANCEL_PUBLISHING = 112L;

        //tin nhan nhan van ban sau khi ban hanh
        public static final Long RECEIVE_PROMULGATE = 201L;

        //gui tin nhan cho van thu khi lanh dao xu ly van ban
        public static final Long SECRETARY_LEADERHANDLEDOC = 202L;

        // Tin nhan bo sung van ban
        public static final Long DOCUMENT_EXTEND = 203L;

        //============tin nhan nhiem vu===================
        // tin nhan giao nhiem vu cho thu truong, tro ly
        public static final Long LEADER_MESSGIVEMISSION = 401L;
        //tin nhan cho nguoi nhan khi thay doi tien do cong viec === hien tai bo
        public static final Long RECEIVED_UPDATEMISSION = 402L;
        //thay doi giao viec cho don vi
        public static final Long CHANGEMISS_TODEPART = 403L;
        //============tin nhan cong viec=================
        //tin nhan cho nguoi nhan cong viec
        public static final Long RECEIVEDTASK_DOTASK = 501L;
        //ky cong viec dau thang
        public static final Long SIGNTASK_STARTMONTH = 502L;
        //ky danh gia cong viec cuoi thang
        public static final Long SIGNTASK_ENDMONTH = 503L;

        //gui tin nhan kien nghi de xuat
        public static final Long PROPOSE_PETITION = 600L;

        //tin nhan chung thu mem
        public static final Long CERTDEVICE_SMS = 700L;
        public static final Long DATTEST11 = 7200L;
        //pm1_os20 add: tin nhan ban giao ho so
        public static final Long BRIEF_PROCESS = 801L;
        public static final Long BRIEF_PROCESS_RECEIVE = 802L;
        public static final Long BRIEF_PROCESS_ACCEPT = 803L;
        public static final Long BRIEF_PROCESS_REJECT = 804L;
        public static final Long BRIEF_PROCESS_NEED_RETURN = 805L;
        public static final Long BRIEF_PROCESS_RETURNED = 806L;
        public static final Long BRIEF_PROCESS_EXPIRING = 807L;
    }
    //vinhnq13 ghi log theo chuan Tap doan
    public static final int ACT_LOGIN = 0;
    public static final int ACT_ERROR = 1;
    public static final int ACT_START = 2;
    public static final int ACT_STOP = 3;
    //Config don vi 260855 148863
    public static final Long ORG_PKH = 260855L;//

    // Van ban cong van
    public static class Document {

        // Cach dem
        public static class CountType {

            /*
             * Tat ca van ban bao gom:
             * + Nguoi dung tao
             * + Nguoi dung chuyen di
             * + Nguoi dung nhan duoc
             */
            public static final int ALL = 0;
            // Tat ca cac trang thai van ban nguoi dung nhan duoc
            public static final int ALL_STATUS_OF_RECEIVED_DOCUMENT = 1;
            // Dem theo 1 loai nhat dinh
            public static final int ONE_TYPE = 2;
        }

        // Key tra ve cho tung loai cong van dem duoc
        public static class CountKey {
            // Van ban nguoi dung tao

            public static final String CREATED = "created";
            // Van ban nguoi dung chuyen di
            public static final String SENT = "sent";
            // Van ban nguoi dung nhan duoc
            public static final String RECEIVED = "received";
            public static final String RECEIVED_ALL = "all";
            // Van ban nguoi dung nhan duoc va chua xu ly
            public static final String RECEIVED_NEW = "new";
            // Van ban nguoi dung nhan duoc va chua doc
            public static final String RECEIVED_UNREAD = "unread";
            // Van ban nguoi dung nhan duoc va da doc
            public static final String RECEIVED_READ = "read";
            // Van ban nguoi dung nhan duoc va dang xu ly
            public static final String RECEIVED_PROCESSING = "processing";
            // Van ban nguoi dung nhan duoc va quan trong
            public static final String RECEIVED_IMPORTANT = "important";
            // Van ban nguoi dung nhan duoc va da luu
            public static final String RECEIVED_SAVED = "saved";
        }

        // Loai
        public static class Type {

            // Nguoi nhan
            public static final int RECEIVER = 1;
            // Nguoi tao
            public static final int CREATOR = 2;
            // Nguoi chuyen
            public static final int SENDER = 3;
            public static final int RECEIVER_IN_GROUP = 4;// La nguoi nhan duy nhat trong don vi
            public static final int ANSWER_DOCUMENT = 5;
        }

        /**
         * Trang thai
         */
        public static class Status {

            /**
             * Tat ca
             */
            public static final int ALL = 0;

            /**
             * Moi
             */
            public static final int NEW = 1;

            /**
             * Chua doc
             */
            public static final int UNREAD = 2;

            /**
             * Da doc
             */
            public static final int READ = 3;

            /**
             * Dang xu ly
             */
            public static final int PROCESSING = 4;

            /**
             * Quan trong
             */
            public static final int IMPORTANT = 5;

            /**
             * Da luu
             */
            public static final int SAVED = 6;
        }

        // Loai chi tiet van ban
        public static class DetailType {

            // Thong tin chi tiet van ban
            public static final int DETAIL = 1;
            // Danh sach file dinh kem
            public static final int LIST_ATTACHMENT = 2;
            // Danh sach y kien chi dao cho van ban
            public static final int LIST_COMMENT = 3;
            // Danh sach nguoi cung nhan van ban voi nguoi dung
            public static final int LIST_RECEIVER_SAME_USER = 4;
            // Danh sach nguoi nhan van ban do tu nguoi dung
            public static final int LIST_RECEIVER = 5;
            // Danh sach don vi nhan van ban do tu nguoi dung
            public static final int LIST_GROUP = 6;
            // Danh sach nguoi ky
            public static final int LIST_SIGNER = 7;
            // Danh sach nhom van ban da nhan
            public static final int LIST_CV_GROUP_RECEIVER = 8;
        }

        /**
         * Loai van ban lien ke
         */
        public static class DocumentAdjacent {

            /**
             * Tat ca
             */
            public static final int ALL = 0;

            /**
             * Van ban yeu cau cap duoi ban hanh lai
             */
            public static final int ORIGINAL = 1;

            /**
             * Van ban phai ban hanh lai
             */
            public static final int ADJACENT = 2;

            /**
             * Chua ban hanh
             */
            public static final int NotPromulgated = 1;

            /**
             * Da ban hanh
             */
            public static final int Promulgated = 2;
        }
    }

    // Log
    public static class Log {

        // Trang thai giao dich
        public static class TransactionStatus {

            // Thanh cong
            public static final int SUCCESS = 0;
            // That bai
            public static final int FAIL = 1;
        }
    }

    // Chung thu
    public static class P12Cert {

        // Trang thai
        public static class Status {

            // Tao moi file CSR
            public static final int NEW_CSR_FILE = 0;
            // Da xac nhan ma OTP
            public static final int CONFIRMED_OTP = 1;
            // Da kich hoat
            public static final int ACTIVATED = 2;
            // Bi thu hoi
            public static final int REVOKED = 3;
            // Khong hop le
            public static final int INVALID = 4;
            // Dang tam ngung
            public static final int SUSPENDING = 5;
        }
    }

    // Kich thuoc anh chuc ky
    public static class SignatureImageSize {

        // Chieu rong
        public static final int WIDTH = 136;
        // Chieu cao
        public static final int HEIGHT = 86;
    }

    //ky song song
    public static class TextSignParallel {

        public static final long LOCK_SIGN_STATE = 1L;
    }

    // Danh sach yeu thich
    public static class Favourite {

        // Loai chuc nang
        public static class FunctionType {

            // Chuyen cong van
            public static final int SEND_DOCUMENT = 1;
            // Chuyen ky nhay
            public static final int FORWARD_INITIAL_SIGN = 2;
            // Loc nguoi gui
            public static final int FILTERING_SENDER = 3;
        }
    }

    public static class FileAttachmentMapper {

        /**
         * Loai doi tuong
         */
        public static class ObjectType {

            /**
             * Bien ban hop
             */
            public static final int MEETING_MINUTES = 1;

            /**
             * Nhiem vu
             */
            public static final int MISSION = 2;

            /**
             * Tien do cong viec
             */
            public static final int TASK_PROCESS = 3;

            /**
             * Tien do nhiem vu
             */
            public static final int MISSION_PROCESS = 4;

            /**
             * Dinh huong
             */
            public static final int ORIENTATION = 5;

            /**
             * Kien nghi de xuat
             */
            public static final int REQUEST = 6;
        }
    }

    public static class MenuIndex {

        /**
         * Van thu xet duyet
         */
        public static final int SECRETARY = 1;

        /**
         * Ky duyet
         */
        public static final int TEXT = 2;

        /**
         * Ky chuyen tien
         */
        public static final int FINANCIAL_TEXT = 3;

    }
    
    /**
     * Van ban doi tac
     */
    public static class TextPartner {
        
        /**
         * Loai van ban
         */
        public static class Type {
            
            /** Van ban trinh sang doi tac ky */
            public static final int REQUEST_FOR_SIGN = 1;
            
            /** Van ban nhan duoc tu doi tac can Voffice ky */
            public static final int RECEIVE_TO_SIGN = 2;
            
            /** Van ban duoc chuyen den Voffice */
            public static final int RECEIVE_TO_VIEW = 3;
        }

        /**
         * Trang thai
         */
        public static class State {

            /** Hoan thanh */
            public static final int COMPLETE = 0;
            
            /** Tao moi */
            public static final int NEW = 1;
            
            /** Cho ky */
            public static final int WAIT_SIGN = 2;
            
            /** Da ky */
            public static final int SIGNED = 3;
            
            /** Da ban hanh */
            public static final int PUBLISHED = 4;
            
            /** Cho chung thuc */
            public static final int VERIFY_WAIT = 5;
            
            /** Tu choi chung thuc */
            public static final int VERIFY_REJECT = 6;
            
            /** Da chung thuc */
            public static final int VERIFY_TRUST = 7;
            
            /** Da chuyen */
            public static final int SENT = 8;
            
            /** Tu choi ky */
            public static final int REJECTED_SIGN = 9;
            
        }
    }

    /**
     * Tu dong chuyen van ban don vi, nhom ca nhan
     */
    public static class TextReceiverGroup {
        
        /**
         * Loai don vi hay nhom ca nhan
         */
        public static class Type {
            
            /** Don vi */
            public static final int DEPARTMENT = 1;
            
            /** Nhom ca nhan */
            public static final int GROUP = 2;
        }
    }
    
    /** 
     * Tieu chi cham diem don vi
     */
    public static class OrgCriteria {
        
        /**
         * Loai tieu chi
         */
        public static class Type {
            
            /** Doanh thu */
            public static final int REVENUE = 1;
            
            /** Thue bao */
            public static final int SUBSCRIBERS = 2;
            
            /** Chi phi */
            public static final int COST = 3;
            
            /** Nhiem vu */
            public static final int MISSION = 4;
            
            /** Thuong cho kinh doanh */
            public static final int REWARD_FOR_BUSINESS = 5;
            
            /** Thuong cho nhiem vu */
            public static final int REWARD_FOR_MISSION = 6;
            
            /** GSM */
            public static final int GSM = 7;
        }
    }

    /**
     * Cau hinh cho tro ly - lanh dao
     */
    public static class MeetingAssistant {
        
        /** 
         * Loai tro ly
         */
        public static class AssisType {
            
            /** Tro ly lich hop */
            public static final int MEETING_SCHEDULE = 1;
            
            /** Tro ly van ban */
            public static final int DOCUMENT = 2;
            
            /** Tro ly cap trinh ky */
            public static final int BRIEFCASE = 3;
            
            /** Tro ly kien nghi, de xuat */
            public static final int REQUEST = 4;
            
            
            // 201901-Pitagon: bo xung loai tro ly
            /** Tro ly sua lich hop */
            public static final int EDIT = 5;
            
            /** Tro ly sua duyet hop */
            public static final int APPROVE = 6;

        }
    }

    /**
     * Cau hinh da ngon ngu tin nhan
     */
    public static class SysMessMutilanguage {
        
        public static class Type {
            
            /** Cong viec */
            public static final int TASK = 3;
        }        
    }
    
    public static class MEETING {
        public static class GROUP_ROLE {
            public static final String IS_GROUP = "VIG";
            // KHOI CO QUAN TAP DOAN
            public static final String IS_VGO = "VGO";
        }
        
        public static class MESSAGE {
        	public static final Integer CONFLICT_ROOM = 600;
        	public static final Integer CONFLICT_PARTICANT = 601;
        	public static final Integer CHANGE_ORG_APPROVAL = 602;
        	public static final Integer REASON_IS_NULL = 603;
        	public static final Integer CONFLICT_ROOM_VIDEO_CONFERENCE = 604;
        	public static final Integer CONFLICT_VIDEO_CONFERENCE = 605;
        	public static final Integer CONFLICT_VIDEO_CONFERENCE_ROOM = 606;
        	public static final Integer ROOM_HAS_NOT_VIDEO_CONFERENCE = 607;
        }
        
        public static class STATE {
            public static final Long WAIT_APPROVE = 1l;
            public static final Long ADMIN_APPROVED = 2l;
            public static final Long REJECT = 3l;
            public static final Long CANCEL = 4l;
            public static final Long DELETE = 99l;
        }
        
        public static class MEETING_ROLE {
            public static final Long PARTICIPANT = 0l;
            public static final Long PREPARE = 1l;
            public static final Long PRESIDENT = 2l;
            public static final Long PREPARE_MAIN = 11l;
            public static final Long PREPARE_COORDINATE = 12l;
        }
    }
    public static class VHR_ORG {
        public static class ID {
            public static final Long GDTD = Long.valueOf(CommonUtils.getAppConfigValue("sysOrganization.id.pgdtd"));
            public static final String DIRECTOR_GROUP_ID = CommonUtils.getAppConfigValue("sysOrganization.id.orgIdsDirectVig");
            public static final Long TVT = Long.valueOf(CommonUtils.getAppConfigValue("sysOrganization.id.tvt"));
            public static final Long TVT_BRANCH = Long.valueOf(CommonUtils.getAppConfigValue("sysOrganization.id.tvtBranch"));
        }
    }

    // ---------------- AREA - Nganh -----------
    public static class AREA {
        public static final Long KD = 3L; // dbtest la 922
    }

    /**
     * 
     * @author DatDC
     * Constants an hien cac config con dau
     * MARK
     */
    public static class MARK {
        public static final Long IS_SHOW = 1L;
        public static final Long UN_SHOW = 0L;
    }

    public static class CONNECT_DOCUMENT {
        public static final Long DOC_IN = 1L;
        public static final Long DOC_OUT = 2L;
        
        public static class DOC_TYPE {
            public static final Long NEW = 0L;
            public static final Long REVOKE = 1L;
            public static final Long UPDATE = 2L;
            public static final Long REPLACE = 3L;
            public static final Long RECOVER = 4L;
        }
        
        public static class PROCESS_TYPE {
            public static final Long RECEIVED = 1L;
            public static final Long REJECT = 2L;
            public static final Long ACCEPTED = 3L;
            public static final Long ASSIGNMENT = 4L;
            public static final Long PROCESSING = 5L;
            public static final Long FINISH = 6L;
            public static final Long RECOVER_REJECT = 16L;
            public static final Long RECOVER_ACCEPTED = 15L;
        }
        
        public static class PRIORITY {
            public static final Long NORMAL = 0L;
            public static final Long URGENT = 1L;
            public static final Long VERY_URGENT = 2L;
            public static final Long EXPRESS = 3L;
            public static final Long TIMER_EXPRESS = 4L;
        }
    }
}
