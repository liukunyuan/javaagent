package com.shuke.agent.util;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
        try{
            if(null==object){
                return null;
            }
            String name = getClassName(object);

            if("sun".startsWith(name) || "java".startsWith(name)){
                return ""+object;
            }else{
                return com.alibaba.fastjson.JSONObject.toJSONString(object, SerializerFeature.IgnoreErrorGetter);
            }
        }catch (Exception e){
            LOG.error(e.getMessage(),e);
        }


        return  null;
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
