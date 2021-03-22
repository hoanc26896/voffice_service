/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.callserviceother;

import com.viettel.servicesso.PassportWS;
import com.viettel.servicesso.PassportWSService;
import com.viettel.servicesso.Response;
import com.viettel.voffice.constants.FunctionCommon;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
//import javax.xml.ws.WebServiceRef;
import org.apache.log4j.Logger;

/**
 *
 * @author datnv5
 */
public class SSORunnable {
    private static final Logger LOGGER = Logger.getLogger(SSORunnable.class);
//    @WebServiceRef(wsdlLocation = "http://10.58.71.44:8180/passportv3/passportWS?wsdl")
   // private PassportWSService passPorsSSO;
    Response responseRequest;
    String strUserName;
    String strPass;
    static PassportWSService passPorsSSO = null;
    public SSORunnable(String strUserName1,String strPass1) {
        strUserName = strUserName1;
        strPass = strPass1;
    }
    
    private  static synchronized PassportWS getPassPort(){
        if(passPorsSSO == null) {
            try {
//                String strUrl =  FunctionCommon.getPropertiesValue(
//                        "url.service.sso");
//                passPorsSSO = new PassportWSService(
//                        new URL(strUrl),
//                        new QName("http://passport.viettel.com/",
//                                "passportWSService"));   
            } catch (Exception ex) {
                LOGGER.error("Err! getPassPort sso service",ex);
            }
        }
        PassportWS result = null;
        if(passPorsSSO!=null && passPorsSSO.getPassportWSPort()!=null){
            result = passPorsSSO.getPassportWSPort();
        }
        return result;
    }
    
    public  Response getLoginServiceSso() {
        Response  resultResponse = SSORunnable.getPassPort().authen(
                strUserName.trim(), strPass.trim(), "VOFFICEMOBILESYSTEM");
        return resultResponse;
    }

    
    public Response getResult() {
        return responseRequest;
    }
}