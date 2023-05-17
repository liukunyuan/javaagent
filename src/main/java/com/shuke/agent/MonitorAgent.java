package com.shuke.agent;


import java.lang.instrument.Instrumentation;
public class MonitorAgent {
    public static void premain(String args, Instrumentation inst){
        System.out.println("Hi, I'm agent!");

//        inst.addTransformer(new MyTransformer());
        inst.addTransformer(new MonitorTransformer2());
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain");
        inst.addTransformer(new MonitorTransformer2());
    }
}