/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler;


import com.viettel.voffice.database.dao.DatabaseManagerDAO;
import com.viettel.voffice.database.entity.DatabaseManagerEntity;
import java.util.List;

/**
 *
 * @author datnv5
 */
public class DatabaseController {

    public List<DatabaseManagerEntity> getListTableViewVoffice2() {
        DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        List<DatabaseManagerEntity> listTablesVoffice2 
                =  databaseManagerDAO.getListTablesVof2View();
        return listTablesVoffice2;
    }

    public List<List<Object>> getDataQuery(String sqlQuery, Long start, Long size) {
        
        DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        List<List<Object>> listTablesVoffice2 
                =  databaseManagerDAO.getDataQuery(sqlQuery,start,size);
        return listTablesVoffice2;
    }
    
    public List<List<Object>> getDataQuery1(String sqlQuery, Long start, Long size) {
        
        DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        List<List<Object>> listTablesVoffice2 
                =  databaseManagerDAO.getDataQuery1(sqlQuery,start,size);
        return listTablesVoffice2;
    }
    
    public List<List<Object>> getDataQuerySLDH(String sqlQuery, Long start, Long size) {
        DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        List<List<Object>> listTablesVoffice2 
                =  databaseManagerDAO.getDataQuerySLDH(sqlQuery,start,size);
        return listTablesVoffice2;
    }
    
    public String updateDataQuery(String sqlQuery) {
        DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        String result =  databaseManagerDAO.updateDataQuery(sqlQuery);
        return result;
    }
    
    public String updateDataQuery1(String sqlQuery) {
         DatabaseManagerDAO databaseManagerDAO = new DatabaseManagerDAO();
        String result =  databaseManagerDAO.updateDataQuery1(sqlQuery);
        return result;
    }
}
