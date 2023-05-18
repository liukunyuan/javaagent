package com.shuke.agent;


import com.shuke.util.LogUtil;

import java.util.Arrays;

public class MyAgentTest {
    public static void main(String[] args) throws InterruptedException {
        String className = Class.class.getName();
        String methodName = "main";
        String name = "arg0|sfs|fsf";
//        System.out.println("com.shuke.util.LogUtil.info("+name+");");
//        System.out.println("try {com.shuke.util.LogUtil.info(new ObjectMapper().writeValueAsString(\""+name+"\"));} catch (JsonProcessingException e) {};");

        String[] arr = name.split("\\|");
        System.out.println(Arrays.asList(arr));




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
