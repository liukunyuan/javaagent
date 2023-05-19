package com.shuke.agent;

import com.test.model.Config;
import com.test.model.Constant;
import com.test.util.FileUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MonitorAgent {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorAgent.class);

    //JVM 首先尝试在代理类上调用以下方法
    public static void premain(String args, Instrumentation inst) {

        System.out.println("args:" + args);
        if (StringUtils.isBlank(args)) {
            LOG.error("配置文件为空，跳过监控");
            return;
        }
        ArrayList<String> configStrList = FileUtils.file2list(args, Charset.defaultCharset().toString());
        List<Config> configList = MonitorAgent.getConfigList(configStrList);
        if (null == configList || configList.size() == 0) {
            LOG.error("配置文件为空，跳过监控");
            return;
        }
        Config config = configList.get(0);
        Constant.finalLimitTimeMillis = config.getLimitTimeMillis();
        Constant.printArgs = config.isPrintArgs();
        Constant.finalLimitSample=config.getLimitSample();
        LOG.info(configList.toString());

        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader,
                                                    JavaModule javaModule) {
                return builder.method(ElementMatchers.<MethodDescription>any().and(ElementMatchers.isMethod()))
                        .intercept(MethodDelegation.to(MonitorIntercept.class));
            }
        };
        /**
         * 1.type指定了agent拦截的包名，以[com.agent]作为前缀
         * 2.指定了转换器transformer
         * 3.将配置安装到Instrumentation
         * 4.disableClassFormatChanges 禁止修改类名
         */
        new AgentBuilder.Default()
//                .disableClassFormatChanges()
//                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(ElementMatchers.<TypeDescription>nameStartsWith("java")
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("sun"))
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("org.slf4j."))
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("org.groovy."))
                        .or(ElementMatchers.<TypeDescription>nameContains("javassist"))
                        .or(ElementMatchers.<TypeDescription>nameContains("asm"))
                        .or(ElementMatchers.<TypeDescription>nameContains("com.intellij"))
                        .or(ElementMatchers.<TypeDescription>nameContains("com.sun"))
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("com.alibaba.fastjson"))
                        .or(ElementMatchers.<TypeDescription>nameContains("net.bytebuddy"))
                        .or(ElementMatchers.<TypeDescription>nameContains("org.itstack.demo.agent.track"))
                        .or(ElementMatchers.<TypeDescription>nameContains("reflectasm"))
                ).type(new ElementMatcher<TypeDescription>() {
                    @Override
                    public boolean matches(TypeDescription typeDefinitions) {
                        String className = typeDefinitions.getName();
                        Iterator<Config> iterator = configList.iterator();
                        boolean monitor = false;
                        while (iterator.hasNext()) {
                            Config next = iterator.next();
                            if (className.contains(next.getClassName())) {
                                monitor = true;
                                break;
                            }
                        }
                        if (!monitor) {
                            return false;
                        }

                        return true;
                    }
                })
                .transform(transformer)
                .installOn(inst);

    }

    //如果代理类没有实现上面的方法，那么 JVM 将尝试调用该方法
    public static void premain(String agentArgs) {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {


        premain(agentArgs, inst);
    }

    public static List<Config> getConfigList(ArrayList<String> configStrList) {
        try {
            if (null == configStrList || configStrList.size() == 0) {
                return null;
            }
            ArrayList<Config> configs = new ArrayList<>();
            for (String line : configStrList) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String[] arr = line.split("\\|");
                Config config = new Config(arr[0], arr[1], arr[2], arr[3],arr[4]);
                configs.add(config);
            }

            return configs;
        } catch (Exception e) {
            LOG.error("读取配置文件失败");
            LOG.error(e.getMessage(), e);
        }
        return null;

    }

}
