/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.createchart.ContansChart;
import com.viettel.createchart.CreateChart;
import com.viettel.createchart.CreateChartColumnsAndLines;
import com.viettel.createchart.dto.DataChartDTO;
import com.viettel.createchart.dto.TitleChartDTO;
import com.viettel.voffice.constants.ConstantsFieldParams;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.report.TextReportDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.User.EntityUser;
import java.util.ArrayList;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.database.entity.EntityImageListStaffTopFlashsign;
import com.viettel.voffice.database.entity.EntityLog;
import com.viettel.voffice.database.entity.EntityTextRejectSign;
import com.viettel.voffice.database.entity.User.Vof2_EntityUser;
import com.viettel.voffice.database.entity.text.EntityTextProcessingTime;
import com.viettel.voffice.database.entity.text.EntityTextRejectionCount;
import com.viettel.voffice.database.entity.text.TextReportEntity;
import com.viettel.voffice.database.entity.text.TextReportResult;
import com.viettel.voffice.database.entity.text.TextReportSignerEntity;
import com.viettel.voffice.utils.LogUtils;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.joda.time.DateMidnight;

/**
 *
 * @author vinhnq13
 */
public class TextReportController {

    // Log file
    private static final Logger logger = Logger.getLogger(TextReportController.class);
    // Ten cua class bao gom ca ten package
    private static final String CLASS_NAME = TextReportController.class.getName();

    /**
     * 2. Lấy danh sách văn bản bị từ chối
     *
     * @param startDate :ngày bắt đầu lấy
     * @param endDate :ngày cuối lấy
     * @param userRejectId : id cá nhân đăng nhập
     * @return: Danh sach van ban do user dang nhap tu choi
     */
    public String getLstDeatilDocRejectedByUserLogin(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getLstDeatilDocRejectedByUserLogin - (Lay danh sach van ban bi tu choi) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            logger.error("getLstDeatilDocRejectedByUserLogin - (Lay danh sach van ban bi tu choi) - Khong co"
                    + " thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        if (user != null && user.getUserId() != null) {
            userIdVof1 = user.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "getMonthYear"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay so luong
            String strDate = listValue.get(0);
            if (strDate != null) {
                int month = 1;
                int year = new Date().getYear();
                try {
                    month = Integer.valueOf(strDate.split("/")[0]);
                    year = Integer.valueOf(strDate.split("/")[1]);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    month = 1;
                    year = new Date().getYear();
                }
                DateMidnight now = new DateMidnight(year, month, 1);
                DateMidnight beginningOfLastMonth = now.minusMonths(1)
                        .withDayOfMonth(1);
//                DateMidnight endOfLastMonth = now.withDayOfMonth(1)
//                        .minusDays(1);
                Date startDate = DateUtils.addMonths(
                        beginningOfLastMonth.toDate(), 1);
//                Date endDate = DateUtils.addMonths(endOfLastMonth.toDate(),
//                        1);
                String dateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(startDate);
                TextReportDAO textReportDAO = new TextReportDAO();
                List<EntityTextRejectSign> listText = textReportDAO.getLstDeatilDocRejectedByUserLogin(dateReprot,
                        dateReprot, userIdVof1, userIdVof2);
                if (listText != null && listText.size() >= 0) {

                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listText, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);

                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }

        } // Loi Server
        catch (Exception ex) {
            logger.error("Thong ke van ban bi tu choi - Exception -"
                    + " \ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>1.Thong ke nguoi bi tu choi van ban</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     * @throws JSONException
     */
    public String getListStaffByRejectSign(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getListStaffByRejectSign - (Thong ke vanban bi tu choi) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            logger.error("getListStaffByRejectSign - (Thong ke vanban bi tu choi) - Khong co"
                    + " thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        if (user != null && user.getUserId() != null) {
            userIdVof1 = user.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "getMonthYear"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay so luong
            String strDate = listValue.get(0);
            if (strDate != null) {
                int month = 1;
                int year = new Date().getYear();
                try {
                    month = Integer.valueOf(strDate.split("/")[0]);
                    year = Integer.valueOf(strDate.split("/")[1]);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    month = 1;
                    year = new Date().getYear();
                }
                DateMidnight now = new DateMidnight(year, month, 1);
                DateMidnight beginningOfLastMonth = now.minusMonths(1)
                        .withDayOfMonth(1);
                DateMidnight endOfLastMonth = now.withDayOfMonth(1)
                        .minusDays(1);
                Date startDate = DateUtils.addMonths(
                        beginningOfLastMonth.toDate(), 1);
                Date endDate = DateUtils.addMonths(endOfLastMonth.toDate(),
                        1);
                String startDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(startDate);
                String endDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(endDate);
//                logger.error("Du lieu dau vao getListStaffByRejectSign: userIdVof1" + userIdVof1
//                        + " userIdVof2: " + userIdVof2 + " startDateReprot"
//                        + startDateReprot + "   " + endDateReprot);
                TextReportDAO textReportDAO = new TextReportDAO();
                List<EntityTextRejectSign> lstInforText = textReportDAO.getListStaffByRejectSign(startDateReprot,
                        endDateReprot, userIdVof1, userIdVof2);
                if (lstInforText != null && lstInforText.size() >= 0) {
//                    List<EntityTextRejectSign> lstImage =
//                            createChartColumn(lstInforText, userIdVof2, String.valueOf(month) + String.valueOf(year));
//                    EntityImageListStaffTopFlashsign result = new EntityImageListStaffTopFlashsign();
//                    result.setListImg(lstImage);
//                    result.setListStaffTopFlashsign(lstInforText);
                    // Ghi log ket thuc chuc nang
                    LogUtils.logFunctionalEnd(log);
                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, lstInforText, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);

                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }

        } // Loi Server
        catch (Exception ex) {
            logger.error("Thong ke van ban bi tu choi - Exception -"
                    + " \ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * 3. Biểu đồ đường Thống kê số lượng văn bản theo dạng đường theo ca nhan
     *
     * @param request
     * @return
     * @throws ProtocolException
     */
    public String getStatisticalLineReportReject(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getStatisticalLineReportReject - (Thong ke vanban theo dang duong) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        if (user != null && user.getUserId() != null) {
            userIdVof1 = user.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "getMonthYear", "staffIdVof1", "staffIdVof2"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay so luong
            String strDate = listValue.get(0);
            String strStaffIdVof1 = listValue.get(1);
            String strStaffIdVof2 = listValue.get(2);
            if (strDate != null) {
                int month = 1;
                int year = new Date().getYear();
                try {
                    month = Integer.valueOf(strDate.split("/")[0]);
                    year = Integer.valueOf(strDate.split("/")[1]);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    month = 1;
                    year = new Date().getYear();
                }
                DateMidnight now = new DateMidnight(year, month, 1);
//                DateMidnight beginningOfLastMonth = now.minusMonths(1)
//                        .withDayOfMonth(1);
                DateMidnight endOfLastMonth = now.withDayOfMonth(1)
                        .minusDays(1);

                Date startDate;

                Calendar c1 = GregorianCalendar.getInstance();
                c1.set(year, 0, 1, 0, 0); // January 30th 2000
                startDate = c1.getTime();

                int yearNow = Calendar.getInstance().get(Calendar.YEAR);
                int monthNow = Calendar.getInstance().get(Calendar.MONTH) + 1;

                Date endDate;
                if ((yearNow == year && monthNow == month)
                        || yearNow < year
                        || (yearNow == year && month > monthNow)) {
                    month = monthNow - 1;
                    if (yearNow < year) {
                        year = yearNow;
                    }
                    endDate = DateUtils.addMonths(endOfLastMonth.toDate(),
                            1);
                } else {
                    // lay ca thang hien tai neu khong phai thoi gian hien
                    // tai la trong thang
                    endDate = DateUtils.addMonths(endOfLastMonth.toDate(),
                            1);
                }
                String startDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(startDate);
                String endDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(endDate);
                String dateSelect = String.valueOf(month)
                        + String.valueOf(year);
                TextReportDAO textReportDAO = new TextReportDAO();
                List<EntityTextRejectSign> lstInforText;
                if ((strStaffIdVof1 == null || strStaffIdVof1.trim().length() <= 0)
                        && (strStaffIdVof2 == null || strStaffIdVof2.trim().length() <= 0)) {
                    // ve bieu do duong cho ca nhan
                    lstInforText = textReportDAO.getStatisticalDocRejectedByUserLogin(
                            startDateReprot,
                            endDateReprot,
                            userIdVof1, userIdVof2);
                    if (lstInforText != null && lstInforText.size() >= 0) {
                        List<EntityTextRejectSign> lstImage = createChartLineTextReject(lstInforText, String.valueOf(userIdVof1) + String.valueOf(userIdVof2), dateSelect);
                        EntityImageListStaffTopFlashsign result = new EntityImageListStaffTopFlashsign();
                        result.setListImg(lstImage);
                        result.setListStaffTopFlashsign(lstInforText);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                    } else {
                        logger.info("Khong co du lieu ve bieu do duong cho ca nhan");
                        return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                                null, null);

                    }
                } else {
                    // ve bieu do duong voi truong hop tong quat
                    Long staffIdVof1 = 0L;
                    Long staffIdVof2 = 0L;
                    try {
                        if (strStaffIdVof1 != null) {
                            staffIdVof1 = Long.valueOf(strStaffIdVof1.trim());
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        if (strStaffIdVof2 != null) {
                            staffIdVof2 = Long.valueOf(strStaffIdVof2.trim());
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    lstInforText = textReportDAO.getStatisticalDocRejectedOfStaff(
                            startDateReprot,
                            endDateReprot,
                            userIdVof1, userIdVof2, staffIdVof1, staffIdVof2);
                    if (lstInforText != null && lstInforText.size() >= 0) {
                        List<EntityTextRejectSign> lstImage = createChartLineTextReject(lstInforText,
                                staffIdVof1.toString() + staffIdVof2.toString(), String.valueOf(month) + String.valueOf(year));
                        EntityImageListStaffTopFlashsign result = new EntityImageListStaffTopFlashsign();
                        result.setListImg(lstImage);
                        result.setListStaffTopFlashsign(lstInforText);
                        // Ghi log ket thuc chuc nang
                        LogUtils.logFunctionalEnd(log);
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, result, aesKey);
                    } else {
                        logger.error("Khong co du lieu ve bieu do duong voi truong hop tong quat");
                        return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                                null, null);

                    }
                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }

        } // Loi Server
        catch (Exception ex) {
            logger.error("Thong ke vanban theo dang duong - Exception -"
                    + " \ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * 4. danh sach van ban user được chọn bị từ chối ký
     *
     * @param request
     * @return
     * @throws ProtocolException
     */
    public String getLstDetailDocRejectedOfStaff(HttpServletRequest request,
            String data, String isSecurity) throws JSONException {

        EntityLog log = new EntityLog(request, CLASS_NAME);
        EntityUserGroup userGroup = FunctionCommon.getStatusSession(request);
        // Khong co session
        if (!userGroup.getCheckSessionOk()) {
            logger.error("getLstDetailDocRejectedOfStaff - (danh sach van ban user duoc chon bi tu choi ky) - Session timeout!");
            return FunctionCommon.generateResponseJSON(userGroup.getEnumErrCode(), null, null);
        }
        //Lay userId tren he thong 2
        Vof2_EntityUser userVof2 = userGroup.getVof2_ItemEntityUser();
        if (userVof2 == null || userVof2.getUserId() == null) {
            logger.error("getLstDetailDocRejectedOfStaff - (danh sach van ban user duoc chon bi tu choi ky) - Khong co"
                    + " thong tin user tren he thong 2!");
            return FunctionCommon.generateResponseJSON(ErrorCode.NOT_ALLOW, null, null);
        }
        Long userIdVof2 = userVof2.getUserId();
        // Lay userId tren he thong 1
        EntityUser user = userGroup.getItemEntityUser();
        Long userIdVof1 = null;
        if (user != null && user.getUserId() != null) {
            userIdVof1 = user.getUserId();
        }
        // Giai ma du lieu neu ma hoa
        String aesKey = null;
        if (isSecurity != null && "1".equals(isSecurity)) {
            // Lay AES Key
            aesKey = userGroup.getStrAesKey();
            // Giai ma data client gui len
            data = SecurityControler.decodeDataByAes(aesKey, data);
        }
        // Lay ma nhan vien
        String cardId = userGroup.getCardId();
        log.setUserName(cardId);
        // Ghi log bat dau chuc nang
        log.setParamList(data);
        LogUtils.logFunctionalStart(log);
        try {
            JSONObject json = new JSONObject(data);
            String[] keys = new String[]{
                "getMonthYear", "staffIdVof1", "staffIdVof2"};
            List<String> listValue = FunctionCommon.getValuesFromJSON(json, keys);
            // Lay so luong
            String strDate = listValue.get(0);
            String strStaffIdVof1 = listValue.get(1);
            String strStaffIdVof2 = listValue.get(2);
            if (strDate != null) {
                int month = 1;
                int year = new Date().getYear();
                try {
                    month = Integer.valueOf(strDate.split("/")[0]);
                    year = Integer.valueOf(strDate.split("/")[1]);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    month = 1;
                    year = new Date().getYear();
                }
                DateMidnight now = new DateMidnight(year, month, 1);
                DateMidnight beginningOfLastMonth = now.minusMonths(1)
                        .withDayOfMonth(1);
                DateMidnight endOfLastMonth = now.withDayOfMonth(1)
                        .minusDays(1);
                Date startDate = DateUtils.addMonths(
                        beginningOfLastMonth.toDate(), 1);
                Date endDate = DateUtils.addMonths(endOfLastMonth.toDate(),
                        1);
                String startDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(startDate);
                String endDateReprot = com.viettel.voffice.utils.DateUtils.date2ddMMyyyyString(endDate);
                TextReportDAO textReportDAO = new TextReportDAO();
                Long staffIdVof1 = 0L;
                Long staffIdVof2 = 0L;
                try {
                    staffIdVof1 = Long.valueOf(strStaffIdVof1.trim());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    staffIdVof2 = Long.valueOf(strStaffIdVof2.trim());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                List<EntityTextRejectSign> listText = textReportDAO.getLstDetailDocRejectedOfStaff(startDateReprot,
                        endDateReprot, userIdVof1, userIdVof2, staffIdVof1, staffIdVof2);
                if (listText != null && listText.size() >= 0) {

                    return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, listText, aesKey);
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.ERR_NODATA,
                            null, null);

                }
            } else {
                return FunctionCommon.generateResponseJSON(ErrorCode.INPUT_INVALID,
                        null, null);
            }

        } // Loi Server
        catch (Exception ex) {
            logger.error("Danh sach van ban user duoc chon bi tu choi ky - Exception -"
                    + " \ndata: " + data, ex);
            return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * Ve bieu do cot thong ke so luong van ban tu choi ky
     *
     * @param listTextStaff
     * @param staffId
     * @return
     */
    private List<EntityTextRejectSign> createChartColumn(List<EntityTextRejectSign> listTextStaff,
            Long staffId, String strDateStart) {
        List<EntityTextRejectSign> lstImageText = new ArrayList<>();
        Long maxValue = 0L;

        // Chuẩn hóa dữ liệu create ảnh biểu đồ
        if (listTextStaff != null) {
            for (int i = 0; i < listTextStaff.size(); i++) {
                if (maxValue.longValue() <= listTextStaff.get(i).getCountText()
                        .longValue()) {
                    maxValue = listTextStaff.get(i).getCountText().longValue();
                }
            }

            int countNumber = listTextStaff.size() / 10;

            if (listTextStaff.size() % 10 > 0) {
                countNumber = countNumber + 1;
            }

            for (int j = 1; j <= countNumber; j++) {
                // danh sách các đường tiêu chí
                List<TitleChartDTO> listTileColumn_Store = new ArrayList<>();
                List<DataChartDTO> listDataColumnChart_Store = new ArrayList<>();
                String strNameChart = "";
                // phân trang tạo ảnh
                int maxNext;
                if (listTextStaff.size() >= (j - 1) * 10 + 10) {
                    maxNext = (j - 1) * 10 + 10;
                } else {
                    maxNext = listTextStaff.size();
                }
                int countColumn = 0;
                for (int i = (j - 1) * 10; i < maxNext; i++) {
                    DataChartDTO itemDataChart = new DataChartDTO();
                    itemDataChart.setCode(ContansChart.CODEONECOLUMN);
                    itemDataChart.setColumnCode(listTextStaff.get(i)
                            .getChiefId().toString());
                    String strName = String.valueOf(i + 1);
                    itemDataChart.setColumnTitle(strName);// listTextStaff.get(i).getFullName()
                    itemDataChart.setValue(Double.valueOf(listTextStaff.get(i)
                            .getCountText()));
                    listDataColumnChart_Store.add(itemDataChart);
                    countColumn++;
                }
                if (countColumn < 10 && listTextStaff.size() > 10) {
                    // add cot co gia tri bang 0 vao
                    for (int i = countColumn + 1; i <= 10; i++) {
                        DataChartDTO itemDataChart = new DataChartDTO();
                        itemDataChart.setCode(ContansChart.CODEONECOLUMN);
                        itemDataChart.setColumnCode(String.valueOf(158111 + i));
                        itemDataChart.setColumnTitle("");// listTextStaff.get(i).getFullName()
                        itemDataChart.setValue(0D);
                        listDataColumnChart_Store.add(itemDataChart);
                    }
                }

                TitleChartDTO itemTitle = new TitleChartDTO();
                itemTitle.setCode(ContansChart.CODEONECOLUMN);
                itemTitle.setTitle("");
                listTileColumn_Store.add(itemTitle);
                // tao duong dan anh
                String partFolder = CommonUtils.getAppConfigValue("pathfileimg_sltc");

                CreateChartColumnsAndLines.chartColumnsAndLinesDraw(
                        listTileColumn_Store, listDataColumnChart_Store, null,
                        null, strNameChart, "", "Số văn bản từ chối ký", "",
                        partFolder + "/", "img" + staffId + j + strDateStart
                        + ".jpg", maxValue);
                EntityTextRejectSign imageText = new EntityTextRejectSign();
                try {
                    imageText.setImgName("img" + staffId + j + strDateStart
                            + ".jpg");
                    imageText.setImgURL("img" + staffId + j + strDateStart
                            + ".jpg");
                    lstImageText.add(imageText);
                } catch (Exception e) {
                    logger.error("Loi ghi nhan duong dan anh", e);
                }

            }
        }
        return lstImageText;
    }

    /**
     * Kẻ biểu đồ cột thống kê văn bản bị từ chối trong vòng 6 tháng
     *
     * @param listTextStaff
     * @param staffId
     * @return
     */
    private List<EntityTextRejectSign> createChartLineTextReject(
            List<EntityTextRejectSign> listTextStaff, String staffId,
            String strDateStart) {
        List<EntityTextRejectSign> lstImageText = new ArrayList<>();

        // Chuẩn hóa dữ liệu create ảnh biểu đồ
        if (listTextStaff != null && listTextStaff.size() > 0) {
            // danh sách các đường tiêu chí
            List<TitleChartDTO> listTileLine_Store = new ArrayList<>();
            List<DataChartDTO> listDataLineChart_Store = new ArrayList<>();
            String strNameChart = "";
            String strMonth = strDateStart.substring(0, strDateStart.trim()
                    .length() - 4);
            String strYear = strDateStart.substring(strDateStart.trim()
                    .length() - 4, strDateStart.trim().length());
            int monthMax = Integer.valueOf(strMonth);
            for (int j = 1; j <= monthMax; j++) {
                // thực hiện add lần lượt các tháng
                Boolean isHaveMonth = false;
                for (int i = 0; i < listTextStaff.size(); i++) {
                    int month = 0;
                    try {
                        month = Integer.valueOf(listTextStaff.get(i)
                                .getDateReject().split("/")[0]);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        month = 0;
                    }
                    if (month == j) {
                        isHaveMonth = true;
                        DataChartDTO itemDataChart = new DataChartDTO();
                        itemDataChart.setCode(ContansChart.CODEONELINE);
                        // itemDataChart.setColumnCode(listTextStaff.get(i).getDateReject());
                        // itemDataChart.setColumnTitle(listTextStaff.get(i).getDateReject());
                        itemDataChart.setColumnCode("T" + j + "/" + strYear);
                        itemDataChart.setColumnTitle("T" + j + "/" + strYear);
                        itemDataChart.setValue(Double.valueOf(listTextStaff
                                .get(i).getCountText()));
                        listDataLineChart_Store.add(itemDataChart);
                    }
                }
                if (!isHaveMonth) {
                    // add tháng không có giá trị
                    DataChartDTO itemDataChart = new DataChartDTO();
                    itemDataChart.setCode(ContansChart.CODEONELINE);
                    itemDataChart.setColumnCode("T" + j + "/" + strYear);
                    itemDataChart.setColumnTitle("T" + j + "/" + strYear);
                    itemDataChart.setValue(0D);
                    listDataLineChart_Store.add(itemDataChart);
                }
            }
            TitleChartDTO itemTitle = new TitleChartDTO();
            itemTitle.setCode(ContansChart.CODEONELINE);
            itemTitle.setTitle("");
            listTileLine_Store.add(itemTitle);

            // tao duong dan anh
            String partFolder = CommonUtils.getAppConfigValue("pathfileimg_sltc");

            CreateChart.chartLineDraw(listTileLine_Store,
                    listDataLineChart_Store, "", "", strNameChart, partFolder
                    + "/", "imgLine" + staffId + strDateStart + ".jpg");
            EntityTextRejectSign imageText = new EntityTextRejectSign();
            try {
                imageText.setImgName("imgLine" + staffId + strDateStart
                        + ".jpg");
                imageText.setImgURL("imgLine" + staffId + strDateStart + ".jpg");
                lstImageText.add(imageText);
            } catch (Exception e) {
                logger.error("Loi ghi nhan duong dan anh", e);
            }

        }
        return lstImageText;
    }

    /**
     * <b>Bao cao chi tiet van ban bi tu choi</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String reportTextRejectedDetail(HttpServletRequest request,
            String data, String isSecurity) {
        
        String[] keys = new String[]{
            "officeSenderId",
            "lstSigner",
            "createDateFrom",
            "createDateTo",
            "groupRejectId",
            "userRejectId",
            "reSendStatus"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("reportTextRejectedDetail - Session time out - "
                    + userGroup.getEnumErrCode().toString());
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Khong ton tai tren he thong voffice 2
        if (!userGroup.checkUserId2()) {
            logger.error("reportTextRejectedDetail - user khong ton tai tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID don vi trinh ky
            String strOfficeSenderId = listValue.get(0);
            Long officeSenderId = null;
            if (!CommonUtils.isEmpty(strOfficeSenderId)) {
                officeSenderId = Long.valueOf(strOfficeSenderId.trim());
            }
            // Thong tin nguoi ky
            String strLstSigner = listValue.get(1);
            List<Vof2_EntityUser> lstSigner = null;
            if (!CommonUtils.isEmpty(strLstSigner)) {
                Type listEmployeeType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                }.getType();
                Gson gson = new Gson();
                lstSigner = gson.fromJson(strLstSigner.trim(), listEmployeeType);
            }
            // Ngay trinh ky
            String createDateFrom = listValue.get(2);
            String createDateTo = listValue.get(3);
            // ID don vi tu choi
            String strGroupRejectId = listValue.get(4);
            Long groupRejectId = null;
            if (!CommonUtils.isEmpty(strGroupRejectId)) {
                groupRejectId = Long.valueOf(strGroupRejectId.trim());
            }
            // ID nguoi tu choi
            String strUserRejectId = listValue.get(5);
            Long userRejectId = null;
            if (!CommonUtils.isEmpty(strUserRejectId)) {
                userRejectId = Long.valueOf(strUserRejectId.trim());
            }
            // Trang thai tim kiem
            String strReSendStatus = listValue.get(6);
            Long reSendStatus = null;
            if (!CommonUtils.isEmpty(strReSendStatus)) {
                reSendStatus = Long.valueOf(strReSendStatus.trim());
            }
            TextReportDAO textReportDAO = new TextReportDAO();
            if (reSendStatus != null && !CommonUtils.isEmpty(createDateFrom)
                    && !CommonUtils.isEmpty(createDateTo)
                    && (reSendStatus.equals(TextReportDAO.ALL_TEXT_REJECT)
                    || reSendStatus.equals(TextReportDAO.HAVE_NOT_RESEND_TEXT_REJECT)
                    || reSendStatus.equals(TextReportDAO.RESEND_TEXT_REJECT))) {

                TextReportResult textReport = textReportDAO.reportTextRejectedDetail(
                        officeSenderId, lstSigner, createDateFrom, createDateTo,
                        groupRejectId, userRejectId, reSendStatus);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, textReport, userGroup);
            } // Du lieu dau vao khong hop le
            else {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (Exception ex) {
            logger.error("reportTextRejectedDetail - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                    null, null);
        }
    }

    /**
     * <b>Bao cao tong hop van ban bi tu choi tong hop</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String reportTextRejectedSumary(HttpServletRequest request,
            String data, String isSecurity) {
        
        String[] keys = new String[]{
            "officeSenderId",
            "signer",
            "actionDateFrom",
            "actionDateTo",
            "reportType",
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("reportTextRejectedSumary - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        }
        // Khong ton tai tren he thong voffice 2
        if (!userGroup.checkUserId2()) {
            logger.error("reportTextRejectedSumary - User khong ton tai tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        }
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID don vi trinh ky
            String strOfficeSenderId = listValue.get(0);
            Long officeSenderId = null;
            if (!CommonUtils.isEmpty(strOfficeSenderId)) {
                officeSenderId = Long.valueOf(strOfficeSenderId.trim());
            }
            // Nguoi ky cuoi
            String strSigner = listValue.get(1);
            Gson gson = new Gson();
            Vof2_EntityUser lastSigner = gson.fromJson(strSigner, Vof2_EntityUser.class);
            
            // Ngay ky
            String actionDateFrom = listValue.get(2);
            String actionDateTo = listValue.get(3);
            // don vi tu choi
            String strReportType = listValue.get(4);
            Long reportType = 0L;
            if (!CommonUtils.isEmpty(strReportType)) {
                reportType = Long.valueOf(strReportType.trim());
            }
           
            if (reportType != null && !CommonUtils.isEmpty(actionDateFrom)
                    && !CommonUtils.isEmpty(actionDateTo) && lastSigner != null) {
                TextReportDAO textReportDAO = new TextReportDAO();
                TextReportResult textReport = textReportDAO.reportTextRejectedSumary(
                        officeSenderId, lastSigner.getUserId(), lastSigner.getLstVhrOrgAll(),
                        actionDateFrom, actionDateTo, reportType);
                return FunctionCommon.responseResult(ErrorCode.SUCCESS, textReport, userGroup);
            } // Du lieu dau vao khong hop le
            else {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (Exception ex) {
            logger.error("reportTextRejectedSumary - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    /**
     * <b>Bao cao chi tiet van ban ky 5 ngay</b><br>
     *
     * @param request
     * @param data
     * @param isSecurity
     * @return
     */
    public String reportTimeSignText(HttpServletRequest request,
            String data, String isSecurity) {

        String[] keys = new String[]{
            "officeSenderId",
            "lstSigner",
            "createDateFrom",
            "createDateTo",
            "numberLateDay",
            "signStatus",
            "typeReport"
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("reportTimeSignText - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        } 
        // Khong ton tai user tren he thong 2
        if (!userGroup.checkUserId2()) {
            logger.error("reportTimeSignText - user khong ton tai tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        } 
        try {
            List<String> listValue = userGroup.getListParamsFromClient();
            // ID don vi trinh ky
            String strOfficeSenderId = listValue.get(0);
            Long officeSenderId = null;
            if (!CommonUtils.isEmpty(strOfficeSenderId)) {
                officeSenderId = Long.valueOf(strOfficeSenderId.trim());
            }
            // Thong tin nguoi ky
            String strLstSigner = listValue.get(1);
            List<Vof2_EntityUser> lstSigner = null;
            if (!CommonUtils.isEmpty(strLstSigner)) {
                Type listEmployeeType = new TypeToken<ArrayList<Vof2_EntityUser>>() {
                }.getType();
                Gson gson = new Gson();
                lstSigner = gson.fromJson(strLstSigner.trim(), listEmployeeType);
            }
            // Ngay trinh ky
            String createDateFrom = listValue.get(2);
            String createDateTo = listValue.get(3);
            // So ngay ky cham 
            String strNumberLateDay = listValue.get(4);
            Integer numberLateDay = null;
            if (!CommonUtils.isEmpty(strNumberLateDay)) {
                numberLateDay = Integer.valueOf(strNumberLateDay.trim());
            }
            // Trang thai ky
            String strSignStatus = listValue.get(5);
            Integer signStatus = null;
            if (!CommonUtils.isEmpty(strSignStatus)) {
                signStatus = Integer.valueOf(strSignStatus.trim());
            }
            String strTypeReport = listValue.get(6);
            TextReportDAO textReportDAO = new TextReportDAO();
            if (!CommonUtils.isEmpty(createDateFrom)
                    && !CommonUtils.isEmpty(createDateTo)
                    && !CommonUtils.isEmpty(lstSigner)) {
                // Bao cao tong hop
                if (strTypeReport != null && "1".equals(strTypeReport)) {
                    List<TextReportSignerEntity> textReport = textReportDAO
                            .reportTimeSignTextSumary(officeSenderId, lstSigner,
                            createDateFrom, createDateTo, numberLateDay, signStatus);
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, textReport, userGroup);
                } // Bao cao chi tiet
                else {
                    List<TextReportEntity> textReport = textReportDAO.reportTimeSignText(
                            officeSenderId, lstSigner, createDateFrom, createDateTo, numberLateDay, signStatus);
                    return FunctionCommon.responseResult(ErrorCode.SUCCESS, textReport, userGroup);
                }
            } else {
                return FunctionCommon.responseResult(ErrorCode.INPUT_INVALID, null, null);
            }
        } catch (Exception ex) {
            logger.error("reportTimeSignText - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * <b>Bao cao thoi gian xu ly van ban</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String reportTextProcessingTime(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT,
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("reportTextProcessingTime - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        } 
        // Khong ton tai user tren he thong 2
        if (!userGroup.checkUserId2()) {
            logger.error("reportTextProcessingTime - user khong ton tai tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        } 
        try {
            // Parse du lieu client truyen len
            List<String> listValue = userGroup.getListParamsFromClient();
            Gson gson = new Gson();
            EntityTextProcessingTime text = gson.fromJson(listValue.get(0), EntityTextProcessingTime.class);
            // Lay bao cao
            TextReportDAO textReportDAO = new TextReportDAO();
            List<EntityTextProcessingTime> listTextProcessingTime = textReportDAO
                    .reportTextProcessingTime(userGroup.getUserId2(), text);
            // Kiem tra ket qua thuc hien
            if (listTextProcessingTime == null) {
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        listTextProcessingTime, userGroup);
            }
        } catch (Exception ex) {
            logger.error("reportTimeSignText - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
    
    /**
     * <b>Bao cao thoi gian xu ly van ban</b>
     * 
     * @param request
     * @param data
     * @return 
     */
    public String reportTextRejectionCount(HttpServletRequest request, String data) {

        String[] keys = new String[]{
            ConstantsFieldParams.TEXT,
        };
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, data, keys);
        // Session timeout
        if (!userGroup.getCheckSessionOk()) {
            logger.error("reportTextRejectionCount - Session timeout!");
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(), null, null);
        } 
        // Khong ton tai user tren he thong 2
        if (!userGroup.checkUserId2()) {
            logger.error("reportTextRejectionCount - user khong ton tai tren he thong 2!");
            return FunctionCommon.responseResult(ErrorCode.NOT_ALLOW, null, null);
        } 
        try {
            // Parse du lieu client truyen len
            List<String> listValue = userGroup.getListParamsFromClient();
            Gson gson = new Gson();
            EntityTextRejectionCount text = gson.fromJson(listValue.get(0),
                    EntityTextRejectionCount.class);
            // Lay bao cao
            TextReportDAO textReportDAO = new TextReportDAO();
            List<EntityTextRejectionCount> listTextRejectionCount = textReportDAO
                    .reportTextRejectionCount(userGroup.getUserId2(), text);
            // Kiem tra ket qua thuc hien
            if (listTextRejectionCount == null) {
                return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR,
                        null, null);
            } else {
                return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                        listTextRejectionCount, userGroup);
            }
        } catch (Exception ex) {
            logger.error("reportTextRejectionCount - Exception - username: "
                    + userGroup.getCardId(), ex);
            return FunctionCommon.responseResult(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
        }
    }
}
