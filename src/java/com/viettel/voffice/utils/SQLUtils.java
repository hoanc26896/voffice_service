/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.utils;

import com.viettel.voffice.constants.FunctionCommon;
import com.viettel.voffice.constants.StringConstants;
import java.util.List;

/**
 * Lop chung de xu ly cac van de SQL
 *
 * @author thanght6
 * @since May 19, 2016
 */
public class SQLUtils {

    /**
     * <b>Sinh dieu kien khi tim kiem 1 truong theo text</b><br/>
     *
     * @author thanght6
     * @since May 19, 2016
     * @param columnName Ten cot
     * @return
     */
    public static String generateConditionForSearchText(String columnName) {

        StringBuilder condition = new StringBuilder();
        // Kiem tra dau vao
        if (CommonUtils.isEmpty(columnName)) {
            return condition.toString();
        }
        condition.append(String.format(" (lower(%s) ", columnName));
        condition.append(" like ? escape '/' ");
        condition.append(" or ");
        condition.append(String.format(" translate(lower(%s),'", columnName));
        condition.append(StringConstants.strSpec);
        condition.append("', '");
        condition.append(StringConstants.strRepl);
        condition.append("') like ? escape '/') ");
        return condition.toString();
    }

    /**
     * <b>Chuyen doi gia tri de tim kiem</b><br/>
     *
     * @author thanght6
     * @since May 19, 2016
     * @param value
     * @return
     */
    public static String convertParameterValueForSearchText(String value) {
        // Kiem tra dau vao
        if (CommonUtils.isEmpty(value)) {
            return value;
        }
        value = FunctionCommon.removeUnsign(value.trim()).toLowerCase();
        value = "%" + FunctionCommon.escapeSql(value) + "%";
        return value;
    }

    /**
     * <b>Sinh cac dau hoi cham (?) de noi vao cau SQL</b><br/>
     *
     * @author thanght6
     * @since May 20, 2016
     * @param size So luong dau hoi
     * @return
     */
    public static String generateQuestionMark(int size) {
        if (size == 0) {
            return "";
        }
        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < size - 1; i++) {
            questionMarks.append(" ?, ");
        }
        questionMarks.append(" ? ");
        return questionMarks.toString();
    }
    /** Cu phap dieu kien IN */
    public static final String IN_CONDITION_SYNTAX = " %s in (%s) ";
    
    /** Cu phap dieu kien NOT IN */
    public static final String NOT_IN_CONDITION_SYNTAX = " %s not in (%s) ";
    
    /** Gioi han so luong param trong cau lenh in */
    public static final int IN_CONDITION_NUM = 900;

    /**
     * <b>Sinh dieu kien IN cho cau query</b><br>
     *
     * @author thanght6
     * @since Aug 22, 2016
     * @param columnName        ten cot
     * @param size              Kich thuoc danh sach tham so
     * @return
     */
    public static String generateINCondition(String columnName, int size) {
        
        if (CommonUtils.isEmpty(columnName) || size == 0) {
            return "";
        }
        StringBuilder condition = new StringBuilder(" ( ");
        int limit = IN_CONDITION_NUM;
        if (size <= limit) {
            condition.append(String.format(IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(size)));
        } else {
            condition.append(String.format(IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(limit)));
            int i;
            for (i = limit; i < size - limit; i += limit) {
                condition.append(" or ");
                condition.append(String.format(IN_CONDITION_SYNTAX, columnName,
                        generateQuestionMark(limit)));
            }
            condition.append(" or ");
            condition.append(String.format(IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(size - i)));
        }
        condition.append(" ) ");
        return condition.toString();
    }
    
    /**
     * <b>Sinh dieu kien NOT IN cho cau query</b><br>
     *
     * @author thanght6
     * @since Mar 15, 2018
     * @param columnName        ten cot
     * @param size              kich thuoc danh sach tham so
     * @return
     */
    public static String generateNOTINCondition(String columnName, int size) {
        
        if (CommonUtils.isEmpty(columnName) || size == 0) {
            return "";
        }
        StringBuilder condition = new StringBuilder(" ( ");
        int limit = IN_CONDITION_NUM;
        if (size <= limit) {
            condition.append(String.format(NOT_IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(size)));
        } else {
            condition.append(String.format(NOT_IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(limit)));
            int i;
            for (i = limit; i < size - limit; i += limit) {
                condition.append(" and ");
                condition.append(String.format(NOT_IN_CONDITION_SYNTAX, columnName,
                        generateQuestionMark(limit)));
            }
            condition.append(" and ");
            condition.append(String.format(NOT_IN_CONDITION_SYNTAX, columnName,
                    generateQuestionMark(size - i)));
        }
        condition.append(" ) ");
        return condition.toString();
    }

    public static void addFilter(String conjunction, String field, String operator,
            String paramName, Object value, boolean isNullable, boolean ignoreNull,
            StringBuilder queryString, List<Object> paramMap) {
        
        if (!ignoreNull || (value != null && !((value instanceof String)
                && ((String) value).trim().isEmpty()))) {
            queryString.append(" ").append(conjunction).append(" ( ");
            if (isNullable) {
                queryString.append(field).append(" IS NULL OR ");
            }
            if ("LIKE".equals(operator) || "NOT LIKE".equals(operator)) {
                String s = trimBlankSpaces((String) value).trim().toLowerCase();
                queryString.append(" LOWER(").append(field).append(") ").append(operator).append(" ?").append(" ESCAPE '/' ");
                s = "%" + s.replace("/", "//").replace("_", "/_").replace("%", "/%") + "%";
                paramMap.add(s);
                // }
            } else if ("LIKE_HEAD".equals(operator) || "LIKE_TAIL".equals(operator)) {
                queryString.append(" LOWER(").append(field).append(") ").append("LIKE").append(" ?").append(" ESCAPE '/' ");
                value = ("LIKE_HEAD".equals(operator) ? "" : "%")
                        + trimBlankSpaces((String) value).trim().toLowerCase().replace("/", "//").replace("_", "/_").replace("%", "/%")
                        + ("LIKE_TAIL".equals(operator) ? "" : "%");
                paramMap.add(value);
            } else if ("=".equals(operator) || "<>".equals(operator) || ">".equals(operator)
                    || ">=".equals(operator) || "<".equals(operator) || "<=".equals(operator)) {
                queryString.append(field).append(" ").append(operator).append(" ?");
                paramMap.add(value);
            } else if ("LIKE_UNSIGN".equals(operator)) {
                String s = trimBlankSpaces((String) value).trim().toLowerCase();
                s = "%" + FunctionCommon.escapeSql(s) + "%";
                queryString.append(FunctionCommon.sql_SelectLikeAsk(field));
                paramMap.add(s);
            }
            queryString.append(" ) ");
        }
    }

    public static String trimBlankSpaces(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }
}
