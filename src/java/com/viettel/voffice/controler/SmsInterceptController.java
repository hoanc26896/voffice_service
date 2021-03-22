/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.voffice.controler;

import com.viettel.voffice.database.dao.sms.SmsInterceptDAO;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.NumberConstants;
import com.viettel.voffice.database.entity.EntityMessIntercept;
import com.viettel.voffice.database.entity.EntityUserGroup;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author: DATNV5, mobile: 0986565786
 */
public class SmsInterceptController {

    /**
     * lay danh sach modul chan tin nhan
     * @param request
     * @param data
     * @return 
     */
    public String getListModulSms(HttpServletRequest request, String data) {
        String[] keys = new String[]{};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, 
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null); 
        }
        SmsInterceptDAO smsInterceptDAO = new SmsInterceptDAO();
        List<EntityMessIntercept>  ListMessIntercept 
                = smsInterceptDAO.getListModulSms(userGroup);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    ListMessIntercept, userGroup);
    }
    /**
     * lay danh sach modul duoc cau hinh theo ca nhan
     * @param request
     * @param data
     * @return 
     */
    public String getListModulInterceptSmsOfUserId(HttpServletRequest request,
            String data) {
        String[] keys = new String[]{"userId"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, 
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null); 
        }
        List<String> listParams = userGroup.getListParamsFromClient();
        Long userIdIntercept =FunctionCommon.isNumeric(listParams.get(0))?
                Long.valueOf(listParams.get(0)):null;
        SmsInterceptDAO smsInterceptDAO = new SmsInterceptDAO();
        //danh sach danh mục chan tin nhan theo user
        List<EntityMessIntercept>  ListMessIntercept 
                = smsInterceptDAO.getListModulInterceptSmsOfUserId(
                        userIdIntercept);
        //danh muc cau hinh chan
        List<EntityMessIntercept>  listAllMenuMess 
                = smsInterceptDAO.getListModulSms(userGroup);
        Boolean checkIntercept;
        List<EntityMessIntercept>  listResult = new ArrayList<>();
        for (EntityMessIntercept itemMess : listAllMenuMess) {
            checkIntercept = false;
            for (EntityMessIntercept itemIntercept : ListMessIntercept) {
                if(itemMess.getSmsId().equals(itemIntercept.getSmsId())){
                    checkIntercept = true;
                }
            }
            if(checkIntercept){
                itemMess.setIsActive(NumberConstants.NUMBER.FIRST);
            }else{
                itemMess.setIsActive(NumberConstants.NUMBER.ZERO);
            }
            listResult.add(itemMess);
        }
        
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    listResult, userGroup);
    }
    
    
    
    /**
     * them hoac xoa bo chan tin nhan
     * @param request
     * @param data
     * @return 
     */
    public String addOrRemoveInterceptByUser(HttpServletRequest request,
            String data) {
        String[] keys = new String[]{"userId","listIdIntercept"};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request, 
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null); 
        }
        List<String> listParams = userGroup.getListParamsFromClient();
        Long userIdIntercept =FunctionCommon.isNumeric(listParams.get(0))?
                Long.valueOf(listParams.get(0)):null;
        
        List<Long> listIdIntercept = FunctionCommon.getListIdFromString(
                listParams.get(1));
        
        SmsInterceptDAO smsInterceptDAO = new SmsInterceptDAO();
        Long valueResult 
                = smsInterceptDAO.addOrRemoveInterceptByUser(userGroup,
                        userIdIntercept, listIdIntercept);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                    valueResult, userGroup);
    }
}
