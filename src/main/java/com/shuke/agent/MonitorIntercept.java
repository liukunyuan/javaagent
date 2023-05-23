package com.shuke.agent;

import com.shuke.agent.track.TrackContext;
import com.shuke.agent.track.TrackManager;
import com.shuke.agent.model.Config;
import com.shuke.agent.model.Constant;
import com.shuke.agent.model.MeterMap;
import com.shuke.agent.util.LogUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class MonitorIntercept {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorIntercept.class);

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @AllArguments Object[] args,
                                   @SuperCall Callable<?> callable) throws Exception {

        String methodName = method.getDeclaringClass().getName()+ "_"+method.getName();
        Config exporterConfig = getExporterConfigByClassName(method.getDeclaringClass().getName());
        Config fileConfig = getFileConfigByClassName(method.getDeclaringClass().getName());

        long monitor_start = System.currentTimeMillis();
        String entrySpan = "";
        String linkId = null;
        try {
            // 获取link id，随机采样
            linkId = TrackManager.getCurrentSpan();
            if(null!=fileConfig && (getRandomNum() <= Constant.finalLimitSample || null!=linkId )){
                if (null == linkId ) {
                    linkId = UUID.randomUUID().toString();
                    TrackContext.setLinkId(linkId);
                }
                entrySpan = TrackManager.createEntrySpan();
            }


            // 业务逻辑
            Object call = callable.call();


            try {
                if(null==exporterConfig && null==fileConfig){
                    return call;
                }
                long monitor_time = System.currentTimeMillis() - monitor_start;

                if(null!=exporterConfig
                        && (!method.toString().contains(".call() ") || method.getName().startsWith("set") )){
//                    Counter counter = MeterMap.counterMap.get(methodName);
//                    if(null==counter){
//                        counter = MeterMap.composite.counter(methodName+"_counter");
//                        MeterMap.counterMap.put(methodName,counter);
//                    }
//                    counter.increment();

                    Counter counter = MeterMap.counterMap.get(methodName+"_counter");
                    if(null==counter && monitor_time >= fileConfig.getLimitTimeMillis()){
                        counter = MeterMap.composite.counter(methodName+"_counter");
                        MeterMap.counterMap.put(methodName+"_counter",counter);
                        counter.increment();
                    }else if(null!=counter){
                        counter.increment();
                    }

                    Timer timer = MeterMap.timerMap.get(methodName+"_timer");
                    if(null==timer && monitor_time >= fileConfig.getLimitTimeMillis()){
                        timer = MeterMap.composite.timer(methodName+"_timer");
                        MeterMap.timerMap.put(methodName+"_timer",timer);
                        timer.record(monitor_time , TimeUnit.MILLISECONDS);
                    }else if(null!=timer){
                        timer.record(monitor_time , TimeUnit.MILLISECONDS);
                    }


//                    AtomicLong myGauge = MeterMap.prometheusRegistry.gauge(methodName+"_gauge", new AtomicLong(0));
//                    myGauge.set(monitor_time);
                }

                if(null==fileConfig){
                    return call;
                }



                if (null == linkId  || monitor_time < fileConfig.getLimitTimeMillis() || method.toString().contains(".call() ")) {
//                if (null == linkId || monitor_time < Constant.finalLimitTimeMillis ) {
                    return call;
                }

                // 打印耗时
                StringBuilder stringBuilder = new StringBuilder();

                if (null!=fileConfig && fileConfig.isPrintArgs()) {
                    int parameterCount = method.getParameterCount();
                    for (int i = 0; i < parameterCount; i++) {
                        if (null == args[i] || StringUtils.isBlank(args[i].toString())) {
                            continue;
                        }
                        // 打印方法入参
                        stringBuilder.append(",[类型:" + method.getParameterTypes()[i].getTypeName() + ",内容:" + LogUtil.parse(args[i])+"]");
                    }
                }
                LOG.info("TRACEID:[" + entrySpan + "], 方法:[" + method + "],耗时:[" + monitor_time + "ms] "+stringBuilder.toString());



            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

            return call;
        } finally {
            if (null != linkId){
                TrackManager.getExitSpan();
            }
        }


    }

    private static Config getExporterConfigByClassName(String className){


        Iterator<Config> iterator = Constant.configList.iterator();
        while(iterator.hasNext()){
            Config next = iterator.next();
            if(Constant.exporter.equalsIgnoreCase(next.getType()) && className.startsWith(next.getClassName())){
                return next;
            }
        }
        return null;


    }

    private static Config getFileConfigByClassName(String className){


        Iterator<Config> iterator = Constant.configList.iterator();
        while(iterator.hasNext()){
            Config next = iterator.next();
            if(Constant.file.equalsIgnoreCase(next.getType()) && className.startsWith(next.getClassName())){
                return next;
            }
        }
        return null;


    }


    public static double getRandomNum() {
        double random = Math.random();
        return random;

    }


}


