/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.controler.signature;

import com.viettel.digital.sign.utils.ISequenceId;
import com.viettel.voffice.database.dao.common.CommonDataBaseDaoVO1;
import org.apache.log4j.Logger;

/**
 *
 * @author datnv5
 */
public class SequenceId implements ISequenceId {
    private static final Logger LOGGER = Logger.getLogger(SequenceId.class);
    @Override
    public int getNextMsspReqId() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select mssp20_req_seq.nextVal from dual");
        CommonDataBaseDaoVO1 cmd = new CommonDataBaseDaoVO1();
        Object intObject = cmd.excuteSqlGetValOnConditionListParams(strBuilder,
                null);
        Long value;
        try {
            value = Long.parseLong(intObject.toString());
        } catch (Exception e) {
            LOGGER.error("Err! getNextSequence mssp sim 2", e);
            value = 0L;
        }        
        return value.intValue();
    }
}
