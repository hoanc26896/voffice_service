/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gentest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Dell
 */
public class GencodeTest4Junit {
    public static void main(String[] args) {
//        genClassTest("com.viettel.voffice.database.entity.EntityAttach");
    	
//        genClassTest("com.viettel.voffice.controler.signature.SignController");
//        genClassTest("com.viettel.voffice.database.dao.sign.CertManagementDAO");
    	
    	// com.viettel.voffice.controller
    	genClassTest("com.viettel.voffice.controler.AnswerDocumentController");
    }
    /**
     * thuc hien gen class test
     * @param classRootName: package class can gen code
     */
    private static void genClassTest(String classRootName) {
     if (classRootName == null) {
            return;
        }
        try {
            Class clazzRoot = Class.forName(classRootName);
            String pathClass = clazzRoot.getCanonicalName();
            //file source
            String pathFileSource = "src\\java\\" + pathClass.replace(".", "\\") + ".java";
            //folder file test gen
            String strPathFileTest = "test" + "/" + pathClass.replace(".", "/") + "Test.java";
            File fileClassTest = new File(strPathFileTest);
            
//            if (fileClassTest.exists()) {
//                //1. truong hop file da ton tai
//                
//            }else{
                //2. truong hop file chua ton tai
                fileClassTest.getParentFile().mkdirs();
                try (PrintWriter printWriteAction = new PrintWriter(fileClassTest)) {
                    StringBuilder strContentCodeAction = genarateClassTest(pathFileSource, clazzRoot);
                    printWriteAction.print(strContentCodeAction);
                }
//            }
        } catch (IOException | ClassNotFoundException  e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * thuc hien ghep chuoi noi dung ham test
     * @param pathFileSource
     * @param clazzRoot
     * @return 
     */
    private static StringBuilder genarateClassTest(String pathFileSource, Class clazzRoot) {
        String strPakageName = clazzRoot.getPackage().getName();
        String strClassName = clazzRoot.getSimpleName();
        //1.gen phan import class
        StringBuilder strContentCodeAction = new StringBuilder();
        strContentCodeAction.append("package ").append(strPakageName).append(";\r\n\r\n");
        strContentCodeAction.append("import java.util.HashMap;\r\n");
        strContentCodeAction.append("import java.util.*;\r\n");
        strContentCodeAction.append("import org.junit.*;\r\n");
        StringBuilder strImp = getGenImport(clazzRoot);
        if(strImp!=null && strImp.toString().trim().length()>0){
            strContentCodeAction.append(strImp);
        }
        strContentCodeAction.append("import org.mockito.*;\r\n");
        strContentCodeAction.append("import org.hamcrest.Matchers;\r\n");
        strContentCodeAction.append("import static org.hamcrest.Matchers.*;\r\n");
        strContentCodeAction.append("import org.hamcrest.core.AnyOf;\r\n");
        strContentCodeAction.append("import static org.hamcrest.MatcherAssert.assertThat;\r\n\r\n");
        
        //2.thuc hien gen comment class test
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class Test for class: ").append(strClassName).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGenTest").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");
        //3.thuc hien gen ten class
        strContentCodeAction.append("public class ").append(strClassName).append("Test {\n\n");
        //thuc hien gen khai bao cho class test
        strContentCodeAction.append(getDeclareClassTest(pathFileSource, clazzRoot));
        
        //thuc hien gen method test 1
        strContentCodeAction.append(getMethodUnitTest(pathFileSource,clazzRoot,0));
        
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }

    /**
     * thuc hien gen dinh nghia dau method
     * @param pathFileSource
     * @param clazzRoot
     * @return 
     */
    private static StringBuilder getDeclareClassTest(String pathFileSource, Class clazzRoot) {
        String strClassName = clazzRoot.getSimpleName();
        //check class la static thi ko can inject mock
        StringBuilder strContentCodeAction = new StringBuilder();
        List<Method> methods = FnUtil.getListMethodInClass(clazzRoot);
        boolean isStaticMethods = true;
        for (Method method : methods) {
            if(!Modifier.isStatic(method.getModifiers())){
                isStaticMethods = false;
                break;
            }
        }
        
        if(isStaticMethods){
            
        }else{
            //thuc hien insert moc
            strContentCodeAction.append("    @InjectMocks").append("\r\n");
            strContentCodeAction.append("    ").append(strClassName).append(" service;").append("\r\n\r\n");
            
            strContentCodeAction.append("    @Before").append("\r\n");
            strContentCodeAction.append("    public void setUp() {").append("\r\n");
            strContentCodeAction.append("       service = new ").append(strClassName).append("();").append("\r\n");
            strContentCodeAction.append("       MockitoAnnotations.initMocks(this);").append("\r\n");
            strContentCodeAction.append("    }").append("\r\n");
        }
        return strContentCodeAction;
    }

    /**
     * thuc hien gen method unit test
     * @param pathFileSource
     * @param clazzRoot
     * @param typeValue: -1 - gia tri khoi tao can duoi, 0 can giua, 1 can tren
     * @return 
     */
    private static StringBuilder getMethodUnitTest(String pathFileSource, Class clazzRoot, int typeValue) {
        List<Method> methods = FnUtil.getListMethodInClass(clazzRoot);
        StringBuilder strContentCodeAction = new StringBuilder();
        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            //1. gen method thuc hien test chuc nang
            //name method
            int indexMt = 0;
            String methodName = method.getName();
            for (int j = 0; j < i; j++) {
                String methodNameCk = methods.get(j).getName();
                if(methodName.equals(methodNameCk)){
                    ++indexMt;
                }
            }
            if(methodName.toLowerCase().equals("main")){
                continue;
            }
            strContentCodeAction.append("\r\n");
            strContentCodeAction.append("    @Test").append("\r\n");
            strContentCodeAction.append("    public void ").append(methodName).append(String.valueOf(indexMt)).append("() {").append("\r\n");
            //thuc hien chen content method
            strContentCodeAction.append(getDeclareVarialbeInMethod(clazzRoot,method,typeValue));
            strContentCodeAction.append("    }").append("\r\n");
        }
        return strContentCodeAction;
    }

    /**
     * lay khai bao bien
     * @param clazzRoot
     * @param method
     * @return 
     */
    private static StringBuilder getDeclareVarialbeInMethod(Class clazzRoot, Method method, int typeValue) {
        StringBuilder strContentCodeAction = new StringBuilder();
        //thuc hien get params dau vao method
        Parameter[] params = method.getParameters();
        String strMethodName = method.getName();
        System.out.println("\n\rmethodName= " + strMethodName);
        for (Parameter param : params) {
            String strType = param.getType().getSimpleName();
            String strName = param.getName();
            Boolean isCheck = FnUtil.checkClassIsBaseType(strType);
            if(!isCheck){
                Boolean isCheckInstance;
                try {
                    //check xem class co phuong thuc khoi tao ko
                    Class clazzVar = Class.forName(param.getType().getName());
                    clazzVar.newInstance();
                    isCheckInstance = true;
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                    isCheckInstance = false;
                }
                if(isCheckInstance){
                    strContentCodeAction.append("        ").append(strType).append(" ").append(strName).append(" = new ").append(strType).append("();\r\n");
                }else{
                    strContentCodeAction.append("        ").append(strType).append(" ").append(strName).append(" = null").append(";\r\n");
                }
            }else{
                //cac kieu du lieu co ban
                String valueTest = FnUtil.getValueTestByType(strType, typeValue);
                strContentCodeAction.append("        ").append(strType).append(" ").append(strName).append(" = ").append(valueTest).append(";\r\n");
            }
        }
        //gen call method test
        String strType = method.getReturnType().getSimpleName();
        if(Modifier.isStatic(method.getModifiers())) {
            //neu la class static thi lay luong class.method
            if (!"void".equals(strType)) {
                strContentCodeAction.append("        ").append(strType).append(" actualResult = ").append(clazzRoot.getSimpleName()).append(".").append(strMethodName).append("(");
            } else {
                strContentCodeAction.append("        ").append(clazzRoot.getSimpleName()).append(".").append(strMethodName).append("(");
            }
            Boolean fisrtParams = true;
            for (Parameter param : params) {
                if(!fisrtParams){
                    strContentCodeAction.append(", ");
                }
                strContentCodeAction.append(param.getName());
                fisrtParams = false;
            }
            strContentCodeAction.append(");\r\n");
        }else{
            if(!"void".equals(strType)){
                strContentCodeAction.append("        ").append(strType).append(" actualResult = service.").append(strMethodName).append("(");
            }else{
                strContentCodeAction.append("        ").append("service.").append(strMethodName).append("(");
            }
            Boolean fisrtParams = true;
            for (Parameter param : params) {
                if(!fisrtParams){
                    strContentCodeAction.append(", ");
                }
                strContentCodeAction.append(param.getName());
                fisrtParams = false;
            }
            strContentCodeAction.append(");\r\n");
        }
        if(!"void".equals(strType)){
            if(typeValue == 0){
                if("String".equals(strType)){
                    strContentCodeAction.append("        ").append("assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is(\"\")))").append(";\r\n");
                }else{
                    strContentCodeAction.append("        ").append("assertThat(actualResult, Matchers.nullValue())").append(";\r\n");
                }
            }else{
                strContentCodeAction.append("        ").append("assertThat(actualResult, Matchers.notNullValue())").append(";\r\n");
            }
        }
        return strContentCodeAction;
    }
    
    /**
     * gen phan import theo cac params trong cac method
     * @param clazzRoot
     * @return 
     */
    private static StringBuilder getGenImport(Class clazzRoot) {
        StringBuilder strContentCodeAction = new StringBuilder(); 
        //check params co cac doi tuong ko co ban
        List<String> listTypeMyDefi = new ArrayList<>();
        List<Method> methods = FnUtil.getListMethodInClass(clazzRoot);
        for (Method method : methods) {
            Parameter[] params = method.getParameters();
            for (Parameter param : params) {
                String strSimpleClassName = param.getType().getSimpleName();
                String pathItem = param.getType().getName();
                Boolean isCheck = FnUtil.checkClassIsBaseType(strSimpleClassName);
                if(!isCheck){
                    listTypeMyDefi.add(pathItem);
                }
            }
        }
        //thuc hien remove item trung nhau
        Object[] st = listTypeMyDefi.toArray();
        for (Object s : st) {
          if (listTypeMyDefi.indexOf(s) != listTypeMyDefi.lastIndexOf(s)) {
              listTypeMyDefi.remove(listTypeMyDefi.lastIndexOf(s));
           }
        }
        
        //gen code import
        if(listTypeMyDefi.size()>0){
            for (String s : listTypeMyDefi) {
                strContentCodeAction.append("import ").append(s).append(";\r\n");
            }
        }
        return strContentCodeAction;
    }

}
