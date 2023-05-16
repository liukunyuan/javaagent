package test;


import java.lang.instrument.Instrumentation;
public class MyAgent {
    public static void premain(String args, Instrumentation inst){
        System.out.println("Hi, I'm agent!");

//        inst.addTransformer(new MyTransformer());
        inst.addTransformer(new MyTransformer2());
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain");
        inst.addTransformer(new MyTransformer2());
    }
}