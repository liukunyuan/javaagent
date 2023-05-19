package org.itstack.demo.test;

/**
 * 线程内方法追踪
 * 博客：http://itstack.org
 * 论坛：http://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛获取学习源码｝
 * create by fuzhengwei on 2019
 * VM options：
 * -javaagent:E:\itstack\GIT\itstack.org\itstack-demo-agent\itstack-demo-agent-05\target\itstack-demo-agent-05-1.0.0-SNAPSHOT.jar=testargs
 * -javaagent:D:\code\javaagent\target\monitoragent-jar-with-dependencies.jar=testargs
 */
public class ApiTest {

    public static void main(String[] args) {


        //线程一
        new Thread(() -> new ApiTest().http_lt1("hello",333L)).start();

        //线程二
        new Thread(() -> {
            new ApiTest().http_lt1("hello",343L);
        }).start();

        new Thread(() -> {
            new ApiTest().http_lt1("hello3",3433L);
        }).start();

    }


    public void http_lt1(String aa,Long bb) {
        System.out.println("测试结果：hi1");
        http_lt2(aa,98.0d);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void http_lt2(String hello,double cc) {
        System.out.println("测试结果：hi2");
        http_lt3();
    }

    public void http_lt3() {
        System.out.println("测试结果：hi3");
    }


}