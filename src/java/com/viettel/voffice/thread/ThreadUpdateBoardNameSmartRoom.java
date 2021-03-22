package com.viettel.voffice.thread;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.viettel.voffice.callserviceother.SmartRoomAPI;
import com.viettel.voffice.database.dao.logAction.LogActionDao;
import com.viettel.voffice.database.dao.meeting.MeetingWeekDAO;
import com.viettel.voffice.database.entity.calendar.MeetingApproveResult;
import com.viettel.voffice.database.entity.meeting.EntitySmartRoom;
import com.viettel.voffice.utils.CommonUtils;
import com.viettel.voffice.utils.DateUtils;

public class ThreadUpdateBoardNameSmartRoom implements Runnable {
    
    private MeetingApproveResult meeting;
    private String method;
    
    public ThreadUpdateBoardNameSmartRoom(MeetingApproveResult meeting, String method) {
        this.meeting = meeting;
        this.method = method;
    }
    
    @Override
    public void run() {
        excuteBusiness();
    }
    
    private void excuteBusiness() {
        if (meeting == null || meeting.getMeetingId() == null) {
            return;
        }
        try {
            SmartRoomAPI api = new SmartRoomAPI();
            if (meeting != null && !CommonUtils.isEmpty(method)) {
                if ("INSERT".equals(method)) {
                    JSONObject json = new JSONObject();
                    json.put("room", meeting.getRealRoomId());
                    json.put("time_begin", DateUtils.convertDateSmartRoom(DateUtils.strinToDateTime(meeting.getStartTime())));
                    json.put("time_end", DateUtils.convertDateSmartRoom(DateUtils.strinToDateTime(meeting.getEndTime())));
                    String president = meeting.getPresident();
                    if (!CommonUtils.isEmpty(president) && president.contains(";")) {
                        president = president.replaceAll(";", "");
                    }
                    json.put("host_name", president);
                    json.put("content", meeting.getTitle());
                    String result = api.sendRequest("POST", json.toString(), "calendars", null);
                    updateDatabase(result);
                } else if ("EDIT".equals(method)) {
                    if (meeting.getRealRoomId() != null) {
                        JSONObject json = new JSONObject();
                        json.put("room", meeting.getRealRoomId());
                        json.put("time_begin", DateUtils.convertDateSmartRoom(DateUtils.strinToDateTime(meeting.getStartTime())));
                        json.put("time_end", DateUtils.convertDateSmartRoom(DateUtils.strinToDateTime(meeting.getEndTime())));
                        String president = meeting.getPresident();
                        if (!CommonUtils.isEmpty(president) && president.contains(";")) {
                            president = president.replaceAll(";", "");
                        }
                        json.put("host_name", president);
                        json.put("content", meeting.getTitle());
                        String result = null;
                        if (meeting.getRealMeetingId() != null) {
                            result = api.sendRequest("PUT", json.toString(), "calendars/" + meeting.getRealMeetingId(), meeting.getEtag());
                        } else {
                            result = api.sendRequest("POST", json.toString(), "calendars",null);
                        }
                        updateDatabase(result);
                    } else {
                        if (meeting.getRealMeetingId() != null) {
                            String result = api.sendRequest("DELETE", null, "calendars/" + meeting.getRealMeetingId(), meeting.getEtag());
                            method = "DELETE";
                            updateDatabase(result);
                        }
                    }
                } else if ("DELETE".equals(method)) {
                    if (meeting.getRealMeetingId() != null) {
                        String result = api.sendRequest("DELETE", null, "calendars/" + meeting.getRealMeetingId(), meeting.getEtag());
                        updateDatabase(result);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(ThreadUpdateBoardNameSmartRoom.class).error(e);
        }
    }
    
    private void updateDatabase(String result) {
        if (meeting == null || meeting.getMeetingId() == null) {
            return;
        }
        Gson gson = new Gson();
        String message = "";
        if (!CommonUtils.isEmpty(result)) {
            EntitySmartRoom entity = gson.fromJson(result, EntitySmartRoom.class);
            if (entity != null) {
                if ("ERR".equals(entity.get_status())) {
                    message = entity.get_error() != null ? entity.get_error().toString() : null;
                } else if ("OK".equals(entity.get_status())) {
                    MeetingWeekDAO dao = new MeetingWeekDAO();
                    if (!CommonUtils.isEmpty(entity.getId()) && !CommonUtils.isEmpty(entity.get_etag())) {
                        dao.updateMeetingForSmartRoom(meeting.getMeetingId(), entity.getId(), entity.get_etag());
                    }
                    if ("DELETE".equals(method)) {
                        dao.updateMeetingForSmartRoom(meeting.getMeetingId(), null, null);
                    }
                } else {
                    message = "SmartRoomAPI.INSERT - Error: " + result;
                }
            } else {
                message = "SmartRoomAPI.INSERT - Error: " + result;
            }
        } else {
            message = "SmartRoomAPI.INSERT - Error";
        }
        if (!CommonUtils.isEmpty(message)) {
            message += " meetingId: " + meeting.getMeetingId();
            LogActionDao log = new LogActionDao();
            log.insertActionLog(10000L, "SmartRoomAPI", "SmartRoomAPI.INSERT", "", message,
                    new Date(), new Date(), "PC", "SmartRoomAPI", 0L);
        }
    }
}
