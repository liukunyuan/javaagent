package org.itstack.demo.agent.track;


import com.alibaba.ttl.TransmittableThreadLocal;

public class TrackContext {

//    private static final ThreadLocal<String> trackLocal = new ThreadLocal<String>();
    private static final TransmittableThreadLocal<String> trackLocal = new TransmittableThreadLocal<String>();

    public static void clear(){
        trackLocal.remove();
    }

    public static String getLinkId(){
        return trackLocal.get();
    }

    public static void setLinkId(String linkId){
        trackLocal.set(linkId);
    }

}