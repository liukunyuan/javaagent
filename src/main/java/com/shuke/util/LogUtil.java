package com.shuke.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogUtil {


    private static final Logger LOG = LoggerFactory.getLogger(LogUtil.class);
    public static void info(String str){
        LOG.info(str);
    }

    public static void error(String str){
        LOG.error(str);
    }

    public static void error(String str, Exception e){
        LOG.error(str);
    }
    public static String parse(Object object){
        if(null==object){
            return null;
        }
        String name = getClassName(object);

        if("sun".startsWith(name) || "java".startsWith(name)){
            return ""+object;
        }else{
            return com.alibaba.fastjson.JSONObject.toJSONString(object);
        }

    }

    public static String getClassName(Object object) {
        return object == null ? "" : object.getClass().getName();
    }



    public static String parse(boolean object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(byte object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(char object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(double object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(float object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(int object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(long object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
    public static String parse(short object){
        String name = getClassName(object);
        return String.valueOf(object);

    }
}
