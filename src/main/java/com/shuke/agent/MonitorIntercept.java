package com.shuke.agent;

import com.shuke.agent.track.TrackContext;
import com.shuke.agent.track.TrackManager;
import com.test.model.Constant;
import com.test.util.LogUtil;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Callable;

public class MonitorIntercept {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorIntercept.class);

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @AllArguments Object[] args,
                                   @SuperCall Callable<?> callable) throws Exception {
        long monitor_start = System.currentTimeMillis();
        String entrySpan = "";
        String linkId = null;
        try {
            // 获取link id，随机采样
            if(getRandomNum() <= Constant.finalLimitSample ){
                linkId = TrackManager.getCurrentSpan();
                if (null == linkId ) {
                    linkId = UUID.randomUUID().toString();
                    TrackContext.setLinkId(linkId);
                }
                entrySpan = TrackManager.createEntrySpan();
            }


            // 业务逻辑
            Object call = callable.call();


            try {
                long monitor_time = System.currentTimeMillis() - monitor_start;

                if (null == linkId || monitor_time < Constant.finalLimitTimeMillis || method.toString().contains(".call() ")) {
//                if (null == linkId || monitor_time < Constant.finalLimitTimeMillis ) {
                    return call;
                }

                // 打印耗时
                StringBuilder stringBuilder = new StringBuilder();

                if (Constant.printArgs) {
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


    public static double getRandomNum() {
        double random = Math.random();
        return random;

    }


}


