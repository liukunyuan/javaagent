package com.test.agent;


import org.apache.commons.lang3.ClassUtils;

public class MyAgentTest {
    /**
     * java -javaagent:monitoragent-jar-with-dependencies.jar=test.config  -cp monitoragent-jar-with-dependencies.jar com.shuke.agent.MyAgentTest
     * mvn  assembly:assembly
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        String className = Class.class.getName();
        String methodName = "main";
        String name = "arg0|sfs|fsf";
//        System.out.println("com.shuke.util.LogUtil.info("+name+");");
//        System.out.println("try {com.shuke.util.LogUtil.info(new ObjectMapper().writeValueAsString(\""+name+"\"));} catch (JsonProcessingException e) {};");
        System.out.println( ClassUtils.getName(true));



        MyAgentTest mat = new MyAgentTest();
        while (true) {
            mat.testRun("hello world",  1000L);
            Thread.sleep((long) (Math.random() * 10));//随机暂停0-10ms

        }
    }

    public void testRun(String helloname, Long aa) throws InterruptedException {
        System.out.println("I'm TestAgent");
        Thread.sleep((long) (Math.random() * 2000));//随机暂停0-100ms
//        long monitorStart = System.currentTimeMillis();


    }
}
