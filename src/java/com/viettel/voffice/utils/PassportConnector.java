/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import viettel.passport.client.ServiceTicketValidator;

/**
 * Ket noi voi trang passport
 *
 * @author thanght6
 * @since Jan 04, 2017
 */
public class PassportConnector {

    // Log file
    private static final Logger LOGGER = Logger.getLogger(PassportConnector.class);
    // Duong dan file cau hinh passport
    private static final String PASSPORT_CONFIG_PATH = "passport";
    // Tai nguyen cau hinh
    public static final ResourceBundle resourceBundle = ResourceBundle.getBundle(PASSPORT_CONFIG_PATH);
    // URL validate ticket
    private static String validateURL;
    // URL server voffice
    private static String serviceURL;
    // Domain
    private static String domainCode;

    public static synchronized String getValidateURL() {
        if (CommonUtils.isEmpty(validateURL)) {
            validateURL = resourceBundle.getString("url.validate");
        }
        return validateURL;
    }

    public static synchronized String getServiceURL() {
        if (CommonUtils.isEmpty(serviceURL)) {
            serviceURL = resourceBundle.getString("url.service");
        }
        return serviceURL;
    }

    public static synchronized String getDomainCode() {
        if (CommonUtils.isEmpty(domainCode)) {
            domainCode = resourceBundle.getString("code.domain");
        }
        return domainCode;
    }

    /**
     * <b>Lay thong tin xac thuc qua Passport</b><br>
     *
     * @param ticket
     * @param webUrl
     * @return
     */
    public static String authenticate(String ticket, String webUrl) {
        try {
            // Kiem tra du lieu dau vao
            if (CommonUtils.isEmpty(ticket) || CommonUtils.isEmpty(webUrl)) {
                LOGGER.error("authenticate - Loi du lieu dau vao - ticket: " + ticket
                        + " | webUrl: " + webUrl);
                return null;
            }
            ServiceTicketValidator validator = new ServiceTicketValidator();
            validator.setCasValidateUrl(getValidateURL());
            validator.setServiceTicket(ticket);
            validator.setService(webUrl);
            //validator.setDomainCode(getDomainCode());
            validator.validate();
            if (validator.getUserToken() != null) {
                return validator.getUserToken().getUserName();
            }
        } catch (Exception ex) {
            LOGGER.error("authenticate - Exception - ticket: " + ticket, ex);
        }
        return null;
    }
}
