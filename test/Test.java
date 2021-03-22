/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author datnv5
 */
public class Test {
    private static  String testStr;

    public Test() {
        if(testStr==null){
            testStr = "Tesssssssssssss";
        }else{
            testStr = "Tesssssssssssss111111111111111111";
        }
    }

    public static void setTestStr(String testStr) {
        Test.testStr = testStr;
    }

    public static String getTestStr() {
        return testStr;
    }


}
