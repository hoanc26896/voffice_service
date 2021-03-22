/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.http.HttpServletRequest;
import org.openide.util.Exceptions;

/**
 *
 * @author datnv5
 */
public class ViettelPayController {

    /**
     * Kiem tra trang thai thanh toan chung thu
     *
     * @param request
     * @param billcode
     * @param merchant_code
     * @param order_id
     * @param check_sum
     * @return
     */
    public String VerifyDataTrans(HttpServletRequest request, String billcode,
            String merchant_code, String order_id, String check_sum) {
        //kiem tra du lieu trong database xem chung thu gia han da duoc thanh toan chua
        
        return "";
    }
}
