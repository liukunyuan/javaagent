package com.shuke.agent;


public class MyAgentTest {
    public static void main(String[] args) throws InterruptedException {
        String className = Class.class.getName();
        String methodName = "main";
        String name = "arg0";
//        System.out.println("com.shuke.util.LogUtil.info("+name+");");
//        System.out.println("try {com.shuke.util.LogUtil.info(new ObjectMapper().writeValueAsString(\""+name+"\"));} catch (JsonProcessingException e) {};");

        MyAgentTest mat = new MyAgentTest();
        while (true) {
            mat.testRun("hello world", "中国人:" + Math.random());
            Thread.sleep((long) (Math.random() * 10));//随机暂停0-10ms

        }
    }

    public void testRun(String helloname, String aa) throws InterruptedException {
        System.out.println("I'm TestAgent");
        Thread.sleep((long) (Math.random() * 2000));//随机暂停0-100ms
//        long monitorStart = System.currentTimeMillis();



    }
}
