package com.shuke.agent;

import com.sun.net.httpserver.HttpServer;
import com.test.model.Config;
import com.test.model.Constant;
import com.test.model.MeterMap;
import com.test.util.FileUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.ProtectionDomain;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MonitorAgent {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorAgent.class);

    //JVM 首先尝试在代理类上调用以下方法
    public static void premain(String args, Instrumentation inst) {
        String[] arr = args.split(":");

        Pattern pattern = Pattern.compile(
                "^(?:((?:[\\w.-]+)|(?:\\[.+])):)?" + // host name, or ipv4, or ipv6 address in brackets
                        "(\\d{1,5}):" +              // port
                        "(.+)");                     // config file

        Matcher matcher = pattern.matcher(args);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed arguments - " + args);
        }

        String givenHost = matcher.group(2);
        args = matcher.group(3);

        startMicrometer(givenHost);


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
        Constant.configList = configList;
        Constant.finalLimitSample=config.getLimitSample();
        LOG.info(configList.toString());
        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer(){

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {

                return builder.method(ElementMatchers.<MethodDescription> any()
                                .and(ElementMatchers.isMethod()))
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
                .ignore(ElementMatchers.<TypeDescription>nameStartsWith("java")
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("sun"))
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("jdk"))
                        .or(ElementMatchers.<TypeDescription>nameStartsWith("com.alibaba.ttl"))
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
                )
                .type(new ElementMatcher<TypeDescription>() {
                    @Override
                    public boolean matches(TypeDescription typeDefinitions) {
                        String className = typeDefinitions.getName();
                        Iterator<Config> iterator = configList.iterator();
                        boolean monitor = false;
                        while (iterator.hasNext()) {
                            Config next = iterator.next();
                            if (className.startsWith(next.getClassName())) {
                                monitor = true;
                                break;
                            }
                        }
                        if (!monitor) {
//                            LOG.info("拒绝:"+className);
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

    private static void startMicrometer(String port){
        //组合注册表
        CompositeMeterRegistry composite = new CompositeMeterRegistry();
        //内存注册表
        MeterRegistry registry = new SimpleMeterRegistry();
        composite.add(registry);
        //普罗米修斯注册表
        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        prometheusRegistry.config().meterFilter(
                new MeterFilter() {
                    @Override
                    public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                        config = DistributionStatisticConfig.builder()
                                .expiry(Duration.ofMinutes(1))
                                .build()
                                .merge(config);

                        if(id.getName().contains("_timer")) {
                            return DistributionStatisticConfig.builder()
                                    .percentiles(0.5)
                                    .build()
                                    .merge(config);
                        }
                        return config;
                    }
                });

        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);


        composite.add(prometheusRegistry);
        //计数器
        MeterMap.composite= composite;
        MeterMap.prometheusRegistry= prometheusRegistry;
        try {
            //暴漏8080端口来对外提供指标数据
            HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(port)), 0);
            server.createContext("/prometheus", httpExchange -> {
                //获取普罗米修斯指标数据文本内容
                String response = prometheusRegistry.scrape();
                //指标数据发送给客户端
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });


            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                String[] split = line.split(":");
                String[] arr = split[1].split("\\|");
                Config config = new Config(split[0],arr[0], arr[1], arr[2], arr[3],arr[4]);
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
