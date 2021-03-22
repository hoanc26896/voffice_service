/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.action;

import com.viettel.voffice.controler.VHROrgController;
import com.viettel.voffice.controler.ViettelPayController;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author datnv5
 */
@Path("ViettelPay")
public class ViettelPayAction {

    /**
     * xac thuc thanh toan cho user
     * @param request
     * @param billcode
     * @param merchant_code
     * @param order_id
     * @param check_sum
     * @return 
     */
    @POST
    @Path("VerifyDataTrans")
    @Consumes("application/x-www-form-urlencoded")
    public String VerifyDataTrans(@Context HttpServletRequest request,
            @FormParam("billcode") String billcode,
            @FormParam("merchant_code") String merchant_code,
            @FormParam("order_id") String order_id,
            @FormParam("check_sum") String check_sum) {
        //Thuc hien xac nhan lai thanh toan tien mua chung thu theo order_id
        ViettelPayController viettelPayController = new ViettelPayController();
        
        return  viettelPayController.VerifyDataTrans(request,billcode,
                merchant_code,order_id,check_sum);
    }
}
