/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import com.google.gson.Gson;
import com.viettel.voffice.constants.ErrorCode;
import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.database.dao.staff.UserManuDAO;
import com.viettel.voffice.database.entity.EntitySyncMenu;
import com.viettel.voffice.database.entity.EntitySyncRequest;
import com.viettel.voffice.database.entity.EntitySyncRole;
import com.viettel.voffice.database.entity.EntitySyncRoleMenu;
import com.viettel.voffice.database.entity.EntitySyncUserRole;
import com.viettel.voffice.security.LoginSecurity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author ToND
 */
public class SyncMenuController {

    // Log file
    private static final Logger logger = Logger.getLogger(SolrSearchController.class);

    /**
     *
     * @param password
     * @return
     */
    private static String decodePassword(String password) {
        String value = null;
        if (password != null) {
            try {
                String parts[] = password.split("-");
                if (parts != null) {
                    List<Integer> partPool = new ArrayList<Integer>();
                    for (String part : parts) {
                        partPool.add(Integer.parseInt(part));
                    }
                    if (!partPool.isEmpty()) {
                        Integer raw[] = partPool.toArray(new Integer[partPool.size()]);
                        int length = raw.length;
                        byte real[] = new byte[length];
                        for (int idx = 0; idx < length; idx++) {
                            real[idx] = raw[idx].byteValue();
                        }
                        value = new String(real, "UTF-8");
                    }
                }
            } catch (Exception ex) {
                logger.error("decodePassword - Exception", ex);
            }
        }
        return value;
    }

    /**
     *
     * @param userName
     * @param password
     * @return
     */
    private static boolean login(String userName, String password) {
        boolean state = false;
        try {
            String accountName = FunctionCommon.getConfigFile("sync_account_name");
            String accountPass = FunctionCommon.getConfigFile("sync_account_pass");
            if ((userName != null) && (password != null) && (accountName.equals(userName))) {
                String decodePassword = SyncMenuController.decodePassword(password);
                if ((decodePassword != null) && (!decodePassword.isEmpty())) {
                    String realPassword = LoginSecurity.encodePassword(accountPass);
                    state = decodePassword.equals(realPassword);
                }
            }
        } catch (IOException ex) {
            logger.error("syncMenu - IO exception", ex);
        }
        return state;
    }

    /**
     *
     * @param data
     * @return
     */
    public String syncMenu(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    String defaultLink = FunctionCommon.getConfigFile("default_voffice_link");
                    List<EntitySyncMenu> menus = userMenuDAO.getSyncMenu(defaultLink, requestEntity.getSyncFrom(), requestEntity.getSyncTo());
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncMenu - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String syncRole(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    List<EntitySyncRole> menus = userMenuDAO.getSyncRole(requestEntity.getSyncFrom(), requestEntity.getSyncTo());
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncRole - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String syncRoleMenu(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    List<EntitySyncRoleMenu> menus = userMenuDAO.getSyncRoleMenu(requestEntity.getSyncFrom(), requestEntity.getSyncTo());
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncRoleMenu - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String getAllRoleMenu(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    List<EntitySyncRoleMenu> menus = userMenuDAO.getAllRoleMenu();
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncRoleMenu - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String syncUserRole(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    List<EntitySyncUserRole> menus = userMenuDAO.getSyncUserRole(requestEntity.getSyncFrom(), requestEntity.getSyncTo());
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncUserRole - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String getCountAllUserRole(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    Long count = userMenuDAO.getCountUserRole();
                    if (count != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, count, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncUserRole - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

    /**
     *
     * @param data
     * @return
     */
    public String getSomeUserRole(String data) {
        try {
            if (data != null) {
                Gson gson = new Gson();
                EntitySyncRequest requestEntity = gson.fromJson(data, EntitySyncRequest.class);
                if (login(requestEntity.getAccountName(), requestEntity.getAccountPass())) {
                    UserManuDAO userMenuDAO = new UserManuDAO();
                    Long offset = Long.parseLong(requestEntity.getSyncFrom());
                    Long size = Long.parseLong(requestEntity.getSyncTo());
                    List<EntitySyncUserRole> menus = userMenuDAO.getSomeUserRole(offset, size);
                    if (menus != null) {
                        return FunctionCommon.generateResponseJSON(ErrorCode.SUCCESS, menus, null);
                    }
                } else {
                    return FunctionCommon.generateResponseJSON(ErrorCode.WRONG_PASSWORD, null, null);
                }
            }
        } catch (Exception ex) {
            logger.error("syncUserRole - Exception", ex);
        }
        return FunctionCommon.generateResponseJSON(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
    }

}
