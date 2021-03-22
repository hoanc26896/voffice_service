/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

/**
 * Liet ke ma loi tra ve
 * 
 * @author thanght6
 */
public enum ErrorCode {
    
    // Ma HTTP
    SUCCESS(200, "Thành công"),
    //loi dang nhap tai khoan sso
    ACCOUNT_SSO_INACTIVATED_OR_LOCKED(204, "Tài khoản chưa kích hoạt hoặc đang bị khóa"),
    ACCOUNT_SSO_LOCKED_INFOR_INCORECT(205, "Tài khoản bị khóa do nhập thông tin không chính xác nhiều lần"),
    ACCOUNT_SSO_LOCKED_BY_ADMIN(207, "Tài khoản chưa kích hoạt hoặc đang bị khóa"),
    ACCOUNT_SSO_LOCKED_PASSWORD_EXPIRED(208, "Tài khoản bị khóa do mật khẩu hết hiệu lực"),
    ACCOUNT_CLIENTREMOTE_PREVENT(209, "Khóa hệ thống do sử dụng mất an toàn, Liên hệ: ducnp or datnv5"),
    SERVICE_UNAVAILABLE(503, "Service không hợp lệ"),
    
    WRONG_PASSWORD(800, "Mật khẩu không đúng"),
    NO_SESSION(801, "Không có session"),
    SESSION_TIME_OUT(802, "Hết phiên đăng nhập"),
    INPUT_INVALID(803, "Dữ liệu đầu vào không hợp lệ"),
    CONVERT_JSON_ERROR(804, "Lỗi convert json"),
    INTERNAL_SERVER_ERROR(805, "Lỗi Server"),
    NOT_ALLOW(806, "Đồng chí chưa được phân quyền để xem chức năng này"),
    NOT_RELATED_MISSION(807, "Đơn vị đồng chí không liên quan đến nhiệm vụ này"),
    ERR_NOSESSION(801, "Session Không tồn tại"),
    ERR_SESSION_TIME_OUT(802, "Hết phiên đăng nhập"),
    ERR_NODATA(15811140, "Lỗi! Không có dữ liệu"),
    INPUT_NOT_EMPTY(812, "Dữ liệu đầu vào không được trống"),
    DUPLICATE_ASSIGN_ERROR(808, "Đơn vị giao việc không được trùng với đơn vị thực hiện"),
    DUPLICATE_COMBINATION_ERROR(809, "Đơn vị phối hợp không được trùng với đơn vị giao việc"),
    DUPLICATE_PERFORM_ERROR(810, "Đơn vị chuyển nhiệm vụ không được trùng với đơn vị thực hiện"),
    DUPLICATE_PERFORM_COMBINATION_ERROR(811, "Đơn vị thực hiện không được trùng với đơn vị phối hợp"),
    ERR_NOT_FORWARD_REQUEST(813, "Không thể chuyển kiến nghị đã được gửi"),
    EXIST_CA_SIM_SIGN_TRANSACTION(814, "Đang trong quá trình xử lý văn bản khác"),
    ASSIGNED_TASK_FOR_USER(815, "Đã giao việc cho nhân viên trong tháng"),
    ASSESSED_TASK_FOR_USER(816, "Đã đánh giá công việc cho nhân viên trong tháng"),
    ERR_NOT_SYNC(814, "Tài khoản chưa được đồng bộ từ hệ thống Voffice 1.0"),
    ERR_DATE_VAILD(817, "Ngày hết hạn không hợp lệ"),
    
    // Ma loi phan van ban (900-999)
    DOCUMENT_NOT_EXIST(901, "Văn bản không tồn tại"),
    DOCUMENT_WAS_PUBLISHED(902, "Văn bản đã được công bố"),
    
    // Ma loi phan van ban trinh ky (1000-1099)
    TEXT_NOT_EXIST_SIGNER(1000, "Người ký không tồn tại"),
    TEXT_EXIST_NUMBER_MANUAL(1001, "Đã tồn tại số đăng ký"),
    TEXT_LOCK_SIGN_STATE(1002, "Không thể ký do văn bản bị khóa"),
    TEXT_SIGN_NOT_PERMISS(1003, "Không thể ký do văn bản bị hủy"),
    
    // Ma loi phan chung thu (1100-1199)
    CERTIFICATE_NOT_EXIST(1100, "Chứng thư không tồn tại"),
    CERTIFICATE_OTP_INCORRECT(1101, "Mã OTP không chính xác"),
    CERTIFICATE_OTP_EXCEED_INCORRECT_LIMIT(1102, "Đồng chí đã nhập sai mã OTP 5 lần"),
    CERTIFICATE_OTP_NOT_EXIST(1103, "Đồng chí chưa lấy mã OTP để thiết lập lại mật khẩu chứng thư"),
    
    //Ma loi trung ten template dải lỗi 700 - 750
    IS_EXISTS_TEMPLATE_NAME(700, "Template đã tồn tại trên hệ thống"),
    ERROR_TEMPLATE_IS_MAX(701, "Không thể thêm mới mẫu, danh sách đã đủ 20 mẫu"),
    ERR_STATUS(452, "Văn bản đã bị thu hồi"),
    
    //Mã lỗi duyệt lịch
    IS_CONFLICT_TIME_USE_MEETING_ROOM(600, "Phòng họp đã được sử dụng trong khoảng thời gian này"),
    CHANGE_LOCATION_ERROR(601, "Đổi phòng họp không thành công");
    
    // Ma loi 
    private int errorCode;
    
    // Mo ta loi
    private String message;

    private ErrorCode(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public static ErrorCode getErrorCode(int code) {
        for (ErrorCode error : ErrorCode.values()) {
            if (error.getErrorCode() == code) {
                return error;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.errorCode + " - " + this.message;
    }
}
