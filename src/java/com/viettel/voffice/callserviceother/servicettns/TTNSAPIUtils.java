/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.callserviceother.servicettns;

/**
 *
 * @author tuantm18
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.viettel.voffice.utils.CommonUtils;
//import com.viettel.voffice.utils.Encryption;

public class TTNSAPIUtils {

    /**
     * Singleton class.
     */
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String userName;
    private String password;
    private static final int RECALL_NUMBER = 5;
    private int callNumber = 0;
    private final HttpServletRequest req;

    private static final Logger LOGGER = Logger.getLogger(TTNSAPIUtils.class);

    public TTNSAPIUtils(HttpServletRequest req) {
        this.req = req;
        init();
    }

    public static TTNSAPIUtils getInstance(HttpServletRequest req) {
        return new TTNSAPIUtils(req);
    }

    private void init() {
        try {
            clientId = CommonUtils.getAppConfigValue("ttns.api.clientId");
            clientSecret = CommonUtils.getAppConfigValue("ttns.api.clientSecret");
            userName = CommonUtils.getAppConfigValue("ttns.api.userName");
            password = CommonUtils.getAppConfigValue("ttns.api.password");
            baseUrl = CommonUtils.getAppConfigValue("ttns.api.base.url");
//            clientId = "ttns-client-id";
//            clientSecret = "12345";
//            userName = "ttns";
//            password = "password";
//            baseUrl = "http://10.58.71.138:8765/TTNSWebService";
           
        } catch (Exception ex) {
            LOGGER.error("Init TTNS APIs ERROR: ", ex);
        }
    }

    /**
     * Get access token.
     *
     * @return
     */
    public String getAccessToken() {
        try {
            String refreshTokenKey = null;
            if (req != null) {
                refreshTokenKey = (String) req.getSession().getAttribute("TTNS_API_REFRESH_TOKEN");
            }
            if (CommonUtils.isEmpty(refreshTokenKey)) {
                refreshTokenKey = getRefreshToken();
            }
            callNumber++;
            Client client = Client.create();
            MultivaluedMap formData = new MultivaluedMapImpl();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("refresh_token", refreshTokenKey);
            WebResource webResource = client.resource(baseUrl + "/oauth/token");
            ClientResponse response = webResource.queryParams(formData).get(ClientResponse.class);
            String oauthClient = response.getEntity(String.class);
            Gson gson = new Gson();
            OauthModel oauth = gson.fromJson(oauthClient, OauthModel.class);
            if (!CommonUtils.isEmpty(oauth.getAccess_token())) {
                callNumber = 0;
                String accessTokenKey = oauth.getAccess_token();
                if (req != null) {
                    req.getSession().setAttribute("TTNS_API_ACCESS_TOKEN", accessTokenKey);
                }
                return accessTokenKey;
            } else {
                throw new Exception("GET access token ERROR");
            }
        } catch (Exception e) {
            if (callNumber <= RECALL_NUMBER) {
                getRefreshToken();
                return getAccessToken();
            }
            LOGGER.error("GET access token ERROR: ", e);
            return null;
        }
    }

    /**
     * Get refresh token.
     *
     * @return
     */
    private String getRefreshToken() {
        try {
            callNumber++;
            Client client = Client.create();
            WebResource webResource = client.resource(baseUrl + "/oauth/token");
            String payLoad
                    = "grant_type=password&client_id=" + clientId + "&client_secret=" + clientSecret
                    + "&username=" + userName + "&password=" + password;
            ClientResponse response
                    = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class,
                            payLoad);
            String oauthClient = response.getEntity(String.class);
            Gson gson = new Gson();
            OauthModel oauth = gson.fromJson(oauthClient, OauthModel.class);
            if (!CommonUtils.isEmpty(oauth.getRefresh_token())) {
                callNumber = 0;
                String refreshTokenKey = oauth.getRefresh_token();
                if (req != null) {
                    req.getSession().setAttribute("TTNS_API_REFRESH_TOKEN", refreshTokenKey);
                }
                return refreshTokenKey;
            } else {
                throw new Exception("GET refresh token ERROR");
            }
        } catch (Exception e) {
            if (callNumber <= RECALL_NUMBER) {
                return getRefreshToken();
            } else {
                LOGGER.error("GET refresh token ERROR: ", e);
                return null;
            }
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Call TTNS APIs.
     *
     * @param formData
     * @param serviceUrl
     * @param method
     * @return
     */
    public String callService(MultivaluedMap formData, String serviceUrl, String method) {
        try {
            callNumber++;
            String accessTokenKey = null;
            if (req != null) {
                accessTokenKey = (String) req.getSession().getAttribute("TTNS_API_ACCESS_TOKEN");
            }
            if (accessTokenKey == null || accessTokenKey.isEmpty()) {
                accessTokenKey = getAccessToken();
            }
            Client client = Client.create();
            WebResource webResource = client.resource(getBaseUrl());
            final ClientResponse response;
            if (formData != null) {
                if ("GET".equals(method)) {
                    response
                            = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).get(ClientResponse.class);
                } else if ("POST".equals(method)) {
                    response
                            = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).post(ClientResponse.class);
                } else if ("PUT".equals(method)) {
                    response
                            = webResource.path(serviceUrl).queryParams(formData).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).put(ClientResponse.class);
                } else {
                    response = null;
                }
            } else {
                if ("GET".equals(method)) {
                    response
                            = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).get(ClientResponse.class);
                } else if ("POST".equals(method)) {
                    response
                            = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).post(ClientResponse.class);
                } else if ("PUT".equals(method)) {
                    response
                            = webResource.path(serviceUrl).type(MediaType.APPLICATION_JSON)
                                    .header("Authorization", "Bearer " + accessTokenKey).put(ClientResponse.class);
                } else {
                    response = null;
                }
            }
            String result = response.getEntity(String.class);
            if ((CommonUtils.NVL(result).contains("invalid_grant") || CommonUtils.NVL(result).contains("invalid_token"))) {
                getAccessToken();
                throw new Exception(serviceUrl + " ERROR");
            } else {
                callNumber = 0;
                return result;
            }
        } catch (Exception ex) {
            if (callNumber <= RECALL_NUMBER) {
                return callService(formData, serviceUrl, method);
            } else {
                LOGGER.error(serviceUrl + " ERROR", ex);
                return null;
            }
        }
    }
}
