package com.shuke.agent.model;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.util.HashMap;

public class MeterMap {
    public static HashMap<String, Counter> counterMap = new HashMap<String, Counter>();
    public static HashMap<String, Timer> timerMap = new HashMap<String, Timer>();
    public static PrometheusMeterRegistry prometheusRegistry ;
    public static HashMap<String, Gauge> gaugeMap = new HashMap<String, Gauge>();
    public static CompositeMeterRegistry composite ;
}
