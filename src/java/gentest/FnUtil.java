package gentest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class FnUtil {

    public static void search(final String pattern, final File folder, List<File> result) {
        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f, result);
            }

            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f);
                }
            }

        }
    }

    public static Method[] getAccessibleMethods(Class clazz) {
        List<Method> result = new ArrayList<>();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                    result.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return result.toArray(new Method[result.size()]);
    }
    
    /**
     * lay danh sach method in class
     * @param clazz
     * @return 
     */
    public static List<Method> getListMethodInClass(Class clazz) {
        List<Method> result = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                result.add(method);
            }
        }
        return result;
    }
    /**
     * Thuc hien doc file ton tai theo dong
     *
     * @param filePath
     * @return
     */
    public static String readLineByLine(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath, new String[0]), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            System.out.println(e);
        }
        return contentBuilder.toString();
    }
    
    /**
     * check type co phai la type base hay ko
     * @param strSimpleClassName
     * @return 
     */
    static Boolean checkClassIsBaseType(String strSimpleClassName) {
        switch (strSimpleClassName) {
            case "Integer":
            case "int":
            case "String":
            case "Long":
            case "long":
            case "Double":
            case "double":
            case "Byte":
            case "Boolean":
            case "boolean":
            case "Character":
            case "Short":
            case "Float":
            case "List":
            case "Date":
            case "Object":
            case "Integer[]":
            case "int[]":
            case "String[]":
            case "Long[]":
            case "long[]":
            case "Double[]":
            case "double[]":
            case "Byte[]":
            case "Boolean[]":
            case "boolean[]":
            case "Character[]":
            case "Short[]":
            case "Float[]":
            case "List[]":
            case "Date[]":
            case "Object[]":
            case "List<Integer>":
            case "List<int>":
            case "List<String>":
            case "List<Long>":
            case "List<long>":
            case "List<Double>":
            case "List<double>":
            case "List<Byte>":
            case "List<Boolean>":
            case "List<boolean>":
            case "List<Character>":
            case "List<Short>":
            case "List<Float>":
            case "List<List>":
            case "List<Date>":
            case "List<Object>":
                return true;
            default:
                return false;
        }
    }

    /**
     * tao du lieu co ban
     * @param strType
     * @param valueTest
     * @return 
     */
    public static String getValueTestByType(String strType, int valueTest) {
        switch (strType) {
            case "Integer":
            case "int":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Integer.MIN_VALUE);
                    case 0:
                        if (strType.equals("Integer")) {
                            return "0";
                        } else {
                            return "0";
                        }
                    case 1:
                        return String.valueOf(Integer.MAX_VALUE);
                    default:
                        return "0";
                }
            case "String":
                switch (valueTest) {
                    case -1:
                        return randomString(25);
                    case 0:
                        return "null";
                    case 1:
                        return randomString(5000);
                    default:
                        return "test";
                }
            case "Long":
            case "long":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Long.MIN_VALUE);
                    case 0:
                        if (strType.equals("Long")) {
                            return "null";
                        } else {
                            return "0L";
                        }
                    case 1:
                        return String.valueOf(Long.MAX_VALUE);
                }
                return "null";
            case "Double":
            case "double":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Double.MIN_VALUE);
                    case 0:
                        if (strType.equals("Long")) {
                            return "null";
                        } else {
                            return "0D";
                        }
                    case 1:
                        return String.valueOf(Double.MAX_VALUE);
                }
                return "null";
            case "Byte":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Byte.MIN_VALUE);
                    case 0:
                        return "null";
                    case 1:
                        return String.valueOf(Byte.MAX_VALUE);
                }
            case "Boolean":
            case "boolean":
                switch (valueTest) {
                    case -1:
                        return  "false";
                    case 0:
                        if (strType.equals("Double")) {
                            return "null";
                        } else {
                            return "true";
                        }
                    case 1:
                        return  "true";
                }
                return "null";
            case "Character":
                switch (valueTest) {
                    case -1:
                        return  "a";
                    case 0:
                         return "null";
                    case 1:
                        return  "z";
                }
            case "Short":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Short.MIN_VALUE);
                    case 0:
                        return "null";
                    case 1:
                        return String.valueOf(Short.MAX_VALUE);
                }
            case "Float":
                switch (valueTest) {
                    case -1:
                        return String.valueOf(Float.MIN_VALUE);
                    case 0:
                        return "null";
                    case 1:
                        return String.valueOf(Float.MAX_VALUE);
                }
            case "List":
                return "null";
            case "Date":
                switch (valueTest) {
                    case -1:
                        return "new Date(2014, 02, 11)";
                    case 0:
                        return "new Date()";
                    case 1:
                        return "new Date(2222, 02, 31)";
                }
            default:
                return "null";
        }
    }
    
    /**
     * gen random string
     * @param len
     * @return 
     */
    static String randomString(int len){
       String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
       SecureRandom rnd = new SecureRandom();
       StringBuilder sb = new StringBuilder(len);
       Random rand = new Random();
       int randomNum = rand.nextInt(7);
       int count = 0;
       for(int i = 0; i < len; i++){
          sb.append(AB.charAt(rnd.nextInt(AB.length())));
          ++count;
          if(count == randomNum){
              count = 0;
              randomNum = rand.nextInt(7);
              sb.append(" ");
          }
       }
       return sb.toString();
    }
}
