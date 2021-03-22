/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.other;

import com.viettel.voffice.controler.DatabaseController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author datnv5
 */
//@WebServlet("/managerServlet")
//@WebServlet(urlPatterns = {"/managerServlet"})
public class ManagerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        String strSession = session.getAttribute("permission").toString();
        req.setAttribute("start", 0);
        req.setAttribute("countrecord", 500);
        req.setAttribute("permission",strSession);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/pages/manager.jsp");
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String strSession = session.getAttribute("permission").toString();
        if (strSession.trim().equals("ADMINISTRATOR") ||
                strSession.trim().equals("SUPERADMINISTRATOR") || strSession.trim().equals("USERSELECT")) {
            String sqlQuery = req.getParameter("strQuery");
            byte[] bytes = sqlQuery.getBytes(StandardCharsets.ISO_8859_1);
            sqlQuery = new String(bytes, StandardCharsets.UTF_8);
            String startRecord = req.getParameter("start");
            String countRecord = req.getParameter("countrecord");
            String action = req.getParameter("action");
            Long longStart;
            try {
                longStart = Long.valueOf(startRecord);
            } catch (NumberFormatException e) {
//                System.out.println(e);
                longStart = 0L;
            }
            Long longCountRecord;
            try {
                longCountRecord = Long.valueOf(countRecord);
            } catch (NumberFormatException e) {
//                System.out.println(e);
                longCountRecord = 500L;
            }

            //thuc hien query database
            req.setAttribute("querySql", sqlQuery);
            req.setAttribute("start", longStart);
            req.setAttribute("countrecord", longCountRecord);
            req.setAttribute("permission",strSession);
            if ("First".equals(action)) {
                
                
                //thuc hien query database 1;
                DatabaseController databaseManagerController;
                databaseManagerController = new DatabaseController();
                List<List<Object>> listTablesVoffice2;
                if(sqlQuery.contains("SLDHQUERY")){
                    sqlQuery = sqlQuery.replace("SLDHQUERY", "");
                    listTablesVoffice2 = databaseManagerController.getDataQuerySLDH(sqlQuery, longStart, longCountRecord);
                }else{
                     listTablesVoffice2 = databaseManagerController.getDataQuery1(sqlQuery, longStart, longCountRecord);
                }
                       

                if (listTablesVoffice2 != null && listTablesVoffice2.size() > 0) {
                    req.setAttribute("headerquery", listTablesVoffice2.get(0));
                    listTablesVoffice2.remove(0);
                    req.setAttribute("dataquery", listTablesVoffice2);
                }
                if(listTablesVoffice2 ==null){
                    req.setAttribute("error", "Lỗi query");
                }
            } else if ("Second".equals(action)) {
                //thuc hien query database 2;
                String[] arrString = sqlQuery.split(";");
                if (arrString.length > 0) {
                    sqlQuery = arrString[0];
                }
                DatabaseController databaseManagerController;
                databaseManagerController = new DatabaseController();
                List<List<Object>> listTablesVoffice2
                        = databaseManagerController.getDataQuery(sqlQuery, longStart, longCountRecord);

                if (listTablesVoffice2 != null && listTablesVoffice2.size() > 0) {
                    req.setAttribute("headerquery", listTablesVoffice2.get(0));
                    listTablesVoffice2.remove(0);
                    req.setAttribute("dataquery", listTablesVoffice2);
                }
                
                if(listTablesVoffice2 ==null){
                    req.setAttribute("error", "Lỗi query");
                }
            } else if ("UpdateFirst".equals(action)  && strSession.contains("ADMINISTRATOR")) {
                //thuc hien cau len update du lieu tren 1.0
                DatabaseController databaseManagerController
                        = new DatabaseController();
                String[] arrString = sqlQuery.split(";");
                String strResult ="";
                if(sqlQuery.contains("ALL")){
                    sqlQuery = sqlQuery.replace("ALL", " ");
                    strResult += databaseManagerController.updateDataQuery1(sqlQuery);
                }else if(arrString.length>0){
                    for (int i = 0; i < arrString.length; i++) {
                        if(arrString[i].trim().length()>0){
                            strResult += databaseManagerController.updateDataQuery1(arrString[i]);
                        }
                    }
                }else{
                    strResult += databaseManagerController.updateDataQuery1(sqlQuery);
                }
                
                req.setAttribute("resutlUpdate", strResult);
            } else if ("UpdateSecond".equals(action) && strSession.contains("ADMINISTRATOR")) {
                //thuc hien cau len update du lieu tren 2.0
                DatabaseController databaseManagerController
                   = new DatabaseController();
                String[] arrString = sqlQuery.split(";");
                String strResult ="";
                if(sqlQuery.contains("ALL")){
                    sqlQuery = sqlQuery.replace("ALL", " ");
                    strResult += databaseManagerController.updateDataQuery(sqlQuery);
                }else if(arrString.length>0){
                    for (int i = 0; i < arrString.length; i++) {
                        if(arrString[i].trim().length()>0){
                            strResult += databaseManagerController.updateDataQuery(arrString[i]);
                        }
                    }
                }else{
                    strResult += databaseManagerController.updateDataQuery(sqlQuery);
                }
                req.setAttribute("resutlUpdate", strResult);
            }
            RequestDispatcher dispatcher = req.getRequestDispatcher("/pages/manager.jsp");
            dispatcher.forward(req, resp);
        } else {
            req.setAttribute("temp", "Đăng nhập lỗi");
            RequestDispatcher dispatcher = req.getRequestDispatcher("index.jsp");
            dispatcher.forward( req, resp);
        }
    }
}
