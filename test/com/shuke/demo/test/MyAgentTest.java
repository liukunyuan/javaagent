package com.shuke.demo.test;


import com.shuke.agent.MonitorAgent;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAgentTest {
    private static final Logger LOG = LoggerFactory.getLogger(MyAgentTest.class);

    /**
     *  java -javaagent:monitoragent-jar-with-dependencies.jar=test.config  -cp monitoragent-jar-with-dependencies.jar com.test.agent.MyAgentTest
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
//            Thread.sleep((long) (1000));//随机暂停0-10ms

        }
    }

    public void testRun(String helloname, Long aa) throws InterruptedException {
        System.out.println("I'm TestAgent");
        double time = Math.random() * 2000;
        LOG.info("睡眠:{}",time);
        Thread.sleep((long)time);//随机暂停0-100ms
//        long monitorStart = System.currentTimeMillis();



    }
}
