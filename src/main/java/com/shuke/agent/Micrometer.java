package com.shuke.agent;

import com.sun.net.httpserver.HttpServer;
import com.test.model.Constant;
import com.test.model.MeterMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Micrometer {
    private static final Logger LOG = LoggerFactory.getLogger(Micrometer.class);




    public static void startMicrometer(){
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

        LOG.info("开始注册httpserver");
        composite.add(prometheusRegistry);
        //计数器
        MeterMap.composite= composite;
        MeterMap.prometheusRegistry= prometheusRegistry;
        try {
            //暴漏8080端口来对外提供指标数据
            HttpServer server = HttpServer.create(new InetSocketAddress(Constant.meterPort), 0);
            server.createContext("/metrics", httpExchange -> {
                //获取普罗米修斯指标数据文本内容
                String response = prometheusRegistry.scrape();
                //指标数据发送给客户端
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });


//            start(false);
            Thread thread = new Thread(server::start);
            thread.setDaemon(true);
            thread.start();
            LOG.info("完成注册httpserver");
        } catch (IOException e) {
            LOG.error(e.getMessage(),e);
        }



    }
}
