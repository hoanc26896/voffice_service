/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.SystemManagerDAO;
import com.viettel.voffice.database.entity.EntityUserGroup;
import com.viettel.voffice.database.entity.sysmanager.SysMenuEntity;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Dieu huong lay du lieu phan quan tri
 * @author datnv5, SystemManagerController
 */
public class SystemManagerController {

    /**
     * Lay danh sach menu chinh
     * @param request
     * @param data
     * @serialData getMenuMain
     * @return 
     */
    public String getMenuMain(HttpServletRequest request, String data) {
        //Lay thong tin user theo sessionId cua request
        //publicKey: neu la chung thu mem, versionCert: 1 check cua sim
        String[] keys = new String[]{};
        EntityUserGroup userGroup = FunctionCommon.getDataFromClient(request,
                data, keys);
        if (!userGroup.getCheckSessionOk()) {
            //truong hop bị timeout
            return FunctionCommon.responseResult(userGroup.getEnumErrCode(),
                    null, null);
        }
        //danh sach cac key de lay du lieu tu client
        List<SysMenuEntity> listSysMenuEntity ;
        SystemManagerDAO systemManagerDAO = new SystemManagerDAO();
        listSysMenuEntity = systemManagerDAO.getMenuMain(userGroup);
        return FunctionCommon.responseResult(ErrorCode.SUCCESS,
                listSysMenuEntity, userGroup);
    }
    
}
