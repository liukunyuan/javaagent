package com.test.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class AttachMain {
    public static void main(String[] args)
            throws IOException, AgentLoadException, AgentInitializationException, AttachNotSupportedException {
        // attach方法参数为目标应用程序的进程号
//        VirtualMachine vm = VirtualMachine.attach(args[0]);
        VirtualMachine vm = VirtualMachine.attach("31432");
        // 请用你自己的agent绝对地址，替换这个
//        vm.loadAgent("/home/j-liukunyuan-jk/myagent-jar-with-dependencies.jar");
        vm.loadAgent("D:\\code\\javaagent\\target\\myagent-jar-with-dependencies.jar");
    }
}
