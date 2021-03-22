/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.constants;

/**
 *
 * @author datnv5
 */
public class NumberConstants {
    //thoi gian chung đợi thuc thi thread

    public static final Long L_TIMESECON_WAITINGTHREAD = 20L;
    public static final int I_SUM_THREAD = 120;

    /**
     * dinh nghia cac ma loi va text trả về client khi ky file mem datnv5
     */
    public interface DIGITAL_SIGN_RESULT_CODE {
        //===Trang thai khoi tao===========

        public static final Long ERR_CODE_INIT_SUCSESS = 1L;
        public static final String ERR_MESS_INIT_SUCSESS = "Khởi tạo thành công";
        //===Thuc hien hash thanh cong===========
        public static final Long ERR_CODE_HASH_SUCSESS = 1L;
        public static final String ERR_MESS_HASH_SUCSESS = "Thực hiện hash thành công";
        //===Thuc hien ky thanh cong===========
        public static final Long ERR_CODE_SIGN_SUCSESS = 1L;
        public static final String ERR_MESS_SIGN_SUCSESS = "Thực hiện ký thành công";
        //===Thông báo lỗi chưa cấp chứng thư======================
        public static final Long ERR_CODE_SEVITICATE_NOTFOUND = 1581111L;
        public static final String ERR_MESS_SEVITICATE_NOTFOUND = "Lỗi! chưa cấp chứng thư";
        //===thong tin serial====================
        public static final Long ERR_CODE_SERIAL_NOTFOUND = 1581112L;
        public static final String ERR_MESS_SERIAL_NOTFOUND = "Lỗi! không có thông tin serial chứng thư";
        //===van ban da duoc xu ly tu truoc======
        public static final Long ERR_CODE_DOC_PROCESSED = 31L;
        public static final String ERR_MESS_DOC_PROCESSED = "Lỗi! văn bản đã được xử lý từ trước";
        //===File trinh ky khong ton tai======
        public static final Long ERR_CODE_FILE_NOT_EXIST = 1581114L;
        public static final String ERR_MESS_FILE_NOT_EXIST = "Lỗi! file kí chính không tồn tại";
        //===File chung thu khong ton tai======
        public static final Long ERR_CODE_FILE_SERVITICATE_NOT_EXIST = 1581115L;
        public static final String ERR_MESS_FILE_SERVITICATE_NOT_EXIST = "Lỗi! file chứng thư không tồn tại";
        //===Thuc hien ky loi===========
        public static final Long ERR_CODE_HASH_NOTSUCSESS = 1581117L;
        public static final String ERR_MESS_HASH_NOTSUCSESS = "Lỗi! Thực hiện hash file";
        //===Chu ky thực hiện phía client gửi lên gặp lỗi, hoặc mất trạng thái lưu trữ ký trước đó
        public static final Long ERR_CODE_SIGNHASH_NOTSUCSESS = 1581118L;
        public static final String ERR_MESS_SIGNHASH_NOTSUCSESS = "Lỗi! Thực hiện ký file từ client gửi lên";
        //===Mat khau chung thu luu tru  tren server bị thay doi
        public static final Long ERR_CODE_SIGNHASH_SEVITICATEFAILPASS = 1581119L;
        public static final String ERR_MESS_SIGNHASH_SEVITICATEFAILPASS = "Lỗi! Chứng thư lưu trữ trên server bị lỗi";
        //===Loi chu ky khong dung dinh dang=======
        public static final Long ERR_CODE_SIGNHASH_LENGFAIL = 15811110L;
        public static final String ERR_MESS_SIGNHASH_LENGFAIL = "Lỗi! Độ dài chữ ký không đúng";
        //===Loi thong tin luu tru file serviticate truoc khi ky====
        public static final Long ERR_CODE_SEVITICATENOTFOUND = 15811111L;
        public static final String ERR_MESS_SEVITICATENOTFOUND = "Lỗi! Không lấy được thông tin chứng thư trước khi ký";
        //===Loi update duong dan file ky sau khi ky xong====
        public static final Long ERR_CODE_UPDATEFILESIGNURL_ERRDATA = 15811112L;
        public static final String ERR_MESS_UPDATEFILESIGNURL_ERRDATA = "Lỗi! Lưu trữ file chữ ký không thành công";
        //===Loi trong qua trinh thuc hien dinh file chu ky
        public static final Long ERR_CODE_SIGNHASHATTACHFILEERR = 15811113L;
        public static final String ERR_MESS_SIGNHASHATTACHFILEERR = "Lỗi! Trong quá trình thực hiện đính file chữ ký";
        //===Loi trong qua trinh thuc hien dinh file chu ky
        public static final Long ERR_CODE_FILECERTNOTACTIVE = 15811114L;
        public static final String ERR_MESS_FILECERTNOTACTIVE = "Lỗi! Chưa kích hoạt chứng thư";
        //===loi dang co chung thu khac cho cap hoac chua nhap OTP
        public static final int ERR_CODE_NEWSCREATEFILECER = 15811115;
        public static final String ERR_MESS_NEWSCREATEFILECER = "Lỗi! Đang tồn tại một yêu cầu đang chờ cấp cer hoặc chưa nhập mã otp";
        //===Loi chung thu dang trong qua trinh tam ngung
        public static final Long ERR_CODE_CERPENDING = 15811116L;
        public static final String ERR_MESS_CERPENDING = "Lỗi! Chứng thư đang bị tạm ngưng sử dụng";
        //===Loi chung thu bi thu hoi
        public static final Long ERR_CODE_CERREMOVE = 15811117L;
        public static final String ERR_MESS_CERREMOVE = "Lỗi! Chứng thư bị thu hồi";
        //===Loi chung thu het han
        public static final Long ERR_CODE_CEREXPIRE = 15811118L;
        public static final Long ERR_CODE_CEREXPIRE_1MONTH = 158111181L;
        public static final String ERR_MESS_CEREXPIRE = "Lỗi! Chứng thư hết hạn";
    }

    //hang so tu nhien
    public interface NUMBER {

        public static final Integer ZERO = 0;
        public static final Integer FIRST = 1;
        public static final Long FIVE = 5L;
    }

    public interface SETTIMEOUT {

        public static final int TIMEOUT_QUERYSQLSEQUENCE_SECOND = 3600;
    }
    //ma xac nhan dang ky chung thu mem

    public interface REGISTERCERT {
        public static final Long SUCSESS = 1L;
        //khong co du lieu doi cap phat chung thu
        public static final Long DATAWAIT_NOTFOUND = 1585L;
        //Loi params day len tu client
        public static final Long WRONG = -9999L;
        // Chua co ban ghi gia han chung thu nao, client co the xin gia han chung thu
        public static final Long DATA_NOTFOUND = -9998L;
        public static final Long NEWCSR_NOTFOUND = -1581L;
        public static final Long PUBLICKEY_NOTFOUND = -1582L;
        public static final Long WRONG_OTP = -1583L;
        public static final Long OVERLOAD_OTP = -1584L;
        //co du lieu cap phat
        public static final Long DATAWAIT_FULL = -1585L;
        //Khong ton tai file cert
        public static final Long CERTFILE_NOTFOUD = -1586L;
        //nhap sai ma activeCert
        public static final Long WRONG_ACTIVATE = -1587L;
        public static final Long OVERLOAD_ACTIVATE = -1588L;
        public static final Long UPDATEFAILSTATE_CERT = -1589L;
        //nhap sai pass de huy chung thu
        public static final Long PASSLOGIN_ERR = -15810L;
        //Loi huy chung thu
        public static final Long CANCELCERT_ERR = -15811L;
        
        //Loi vuot qua thoi gian nhap
        public static final Long OVERTIME = -15812L;
    }
    //Hang so log thoi gian xu ly sql bi cham

    public interface TIME_PROCESS_SQL {

        public static final Long LOG_TIME_PROCESS_SQL = 8000L;
    }
}
