/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.viettel.voffice.constants.Constants;
import com.viettel.voffice.database.dao.task.TaskDAO;
import com.viettel.voffice.database.entity.task.EntityTask;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author thanght6
 */
public class TaskUtils {

    /**
     * Log loi
     */
    private static final Logger LOGGER = Logger.getLogger(TaskUtils.class);

    /**
     * Key id don vi phong ke hoach trong file cau hinh
     */
    public static final String PLAN_DEPARTMENT_ORG_ID_KEY = "id.org.plandepartment";

    /**
     * Id don vi phong ke hoach
     */
//    public static Long PLAN_DEPARTMENT_ORG_ID = 0L;
    // Chi so nhom cong viec chuc nang
    public static final String FUNCTIONAL_TASK_GROUP_INDEX = "I";

    // Ten nhom cong viec chuc nang
    public static final String FUNCTIONAL_TASK_GROUP_NAME = "Nhóm công việc: "
            + "Công việc định lượng";

    // Chi so nhom cong viec ne nep
    public static final String ROUTINE_TASK_GROUP_INDEX = "II";

    // Ten nhom cong viec ne nep
    public static final String ROUTINE_TASK_GROUP_NAME = "Nhóm công việc: "
            + "Công việc định tính";

    /**
     * <b>Phan biet cong viec chuc nang va cong viec ne nep</b><br>
     *
     * @author thanght6
     * @since Sep 12, 2016
     * @param listTask Danh sach cong viec ban dau
     * @param listFunctionalTask Danh sach cong viec chuc nang
     * @param listRoutineTask Danh sach cong viec ne nep
     */
    public static void distinguishTask(List<EntityTask> listTask,
            List<EntityTask> listFunctionalTask, List<EntityTask> listRoutineTask) {

        Integer taskType;
        for (EntityTask task : listTask) {
            taskType = task.getTaskGroup();
            if (taskType == null) {
                taskType = 2;
            }
            // Neu la cong viec dinh luong
            // -> Them vao danh sach cong viec chuc nang
            if (taskType == 2) {
                task.setTaskGroupIndex(FUNCTIONAL_TASK_GROUP_INDEX);
                task.setTaskGroupName(FUNCTIONAL_TASK_GROUP_NAME);
                listFunctionalTask.add(task);
            }
            // Neu la cong viec dinh tinh
            // -> Them vao danh sach cong viec ne nep
            if (taskType == 1) {
                task.setTaskGroupIndex(ROUTINE_TASK_GROUP_INDEX);
                task.setTaskGroupName(ROUTINE_TASK_GROUP_NAME);
                listRoutineTask.add(task);
            }           
        }
    }

    /**
     * <b>Lay id don vi phong ke hoach</b>
     *
     * @return
     */
//    public static Long getPlanDepartmentOrgId() {
//        // Neu id phong ke hoach co gia tri mac dinh (0)
//        // -> Lay gia tri id phong ke hoach trong file cau hinh
//        if (PLAN_DEPARTMENT_ORG_ID.equals(0L)) {
//            try {
//                PLAN_DEPARTMENT_ORG_ID = Long.parseLong(CommonUtils.getAppConfigValue(PLAN_DEPARTMENT_ORG_ID_KEY));
//            } catch (Exception ex) {
//                LOGGER.error("getPlanDepartmentOrgId - Exception:" + ex.getMessage());
//            }
//        }
//        return PLAN_DEPARTMENT_ORG_ID;
//    }
    /**
     * Mang ten cong viec ne nep phong ke hoach
     */
    protected static final String[] ARR_ROUTINE_TASK_NAME_OF_PLAN_DEPARTMENT = new String[]{
        "Đi làm muộn và nghỉ làm không báo cáo",
        "Nộp báo cáo muộn (tuần, tháng, cá nhân, tháng, báo cáo trên phần mềm...)",
        "Chất lượng báo cáo chưa đạt yêu cầu",
        "Vắng họp không lý do",
        "Ra ngoài không báo cáo",
        "Công tác phối hợp giữa các cá nhân trong phòng",
        "Muộn giờ họp"};

    /**
     * Mang noi dung cong viec ne nep phong ke hoach
     */
    protected static final String[] ARR_ROUTINE_TASK_CONTENT_OF_PLAN_DEPARTMENT = new String[]{
        "Đánh giá tác phong thực hiện nề nếp ra vào tập đoàn",
        "Đánh giá tiến độ thực hiện báo cáo của các cá nhân trong đơn vị",
        "Đánh giá chất lượng báo cáo của các cá nhân",
        "Đánh giá tác phong nề nếp hội họp",
        "Đánh giá tác phong thực hiện nề nếp ra vào tập đoàn",
        "Đánh giá việc thực hiện phối hợp công việc với các cá nhân trong đơn vị",
        "Đánh giá tác phong nề nếp hội họp"
    };

    /**
     * Ten cong viec ne nep mac dinh
     */
    public static final String DEFAULT_ROUTINE_TASK_NAME = "Công việc nề nếp";

    /**
     * Noi dung cong viec ne nep mac dinh
     */
    public static final String DEFAULT_ROUTINE_TASK_CONTENT = "Nội dung công việc nề nếp";

    /**
     * Dinh dang ky danh gia
     */
    public static final String PERIOD_FORMAT = "yyyyMM";

    /**
     * Dinh dang thoi gian bat dau/ket thuc cua nhiem vu
     */
    public static final String TASK_TIME_FORMAT = "dd/MM/yyyy HH:mm";

    /**
     * <b>Lay thoi gian bat dau/ket thuc theo ky danh gia</b>
     *
     * @param period Ky danh gia (yyyyMM)
     * @return
     */
    public static List<String> getTimeFromPeriod(String period) {

        List<String> listTime = new ArrayList<String>();
        // Lay doi tuong calendar ngay hien tai
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf;
        try {
            // Thiet lap calendar theo ngay danh gia
            sdf = new SimpleDateFormat(PERIOD_FORMAT);
            Date startDate = sdf.parse(period);
            calendar.setTime(startDate);
        } catch (ParseException ex) {
            LOGGER.error("getStartTimeFromPeriod - Exception:", ex);
        }
        // Dua calendar ve ngay dau thang
        calendar.set(Calendar.DATE, 1);
        // Thiet lap gio la 8
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        // Thiet lap phut la 0
        calendar.set(Calendar.MINUTE, 0);
        // Thoi gian bat dau
        Date startTime = calendar.getTime();

        // Dua calendar ve ngay dau thang sau
        calendar.add(Calendar.MONTH, 1);
        // Lui 1 ngay thanh ngay cuoi thang hien tai
        calendar.add(Calendar.DATE, -1);
        // Thiet lap gio la 17
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        // Thiet lap phut la 30
        calendar.set(Calendar.MINUTE, 30);
        // Thoi gian ket thuc
        Date endTime = calendar.getTime();

        // Chuyen thoi gian bat dau/ket thuc ve chuoi
        sdf = new SimpleDateFormat(TASK_TIME_FORMAT);
        String strStartTime = sdf.format(startTime);
        String strEndTime = sdf.format(endTime);
        listTime.add(strStartTime);
        listTime.add(strEndTime);

        return listTime;
    }
}
