package com.shuke.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Element;

public class LogUtil {

    public  static ThreadLocal<String> LOCAL_VAR = new ThreadLocal<String>();

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
}
