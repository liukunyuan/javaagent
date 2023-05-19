package org.itstack.demo.agent;

import com.shuke.model.Config;
import com.shuke.util.LogUtil;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.commons.lang3.StringUtils;
import org.itstack.demo.agent.track.TrackContext;
import org.itstack.demo.agent.track.TrackManager;
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
        try {
            String linkId = TrackManager.getCurrentSpan();
            if (null == linkId) {
                linkId = UUID.randomUUID().toString();
                TrackContext.setLinkId(linkId);
            }
            entrySpan = TrackManager.createEntrySpan();

            return callable.call();
        } finally {
            long monitor_time = System.currentTimeMillis() - monitor_start;
            /**
             * 每10条取一条，毫秒数大于500
             */
            if (getRandomNum() % 9 == 0 && monitor_time >= 500) {
                LOG.info("LinkId:" + entrySpan + " " + method + ":[" + monitor_time + "]毫秒");
                int parameterCount = method.getParameterCount();
                for (int i = 0; i < parameterCount; i++) {
                    if(null==args[i] || StringUtils.isBlank(args[i].toString())){
                        continue;
                    }
                    LOG.info("LinkId:" + entrySpan + " " + method + ":[" + monitor_time + "]毫秒" + ",入参类型:" + method.getParameterTypes()[i].getTypeName() + ",入参内容:" + LogUtil.parse(args[i]));
                }
            }
//            LOG.info("Config.finalLimitTimeMillis:" + Config.finalLimitTimeMillis);

            TrackManager.getExitSpan();
        }
    }


    public static int getRandomNum() {
        return (int) (Math.random() * 10 + 1);

    }


}


