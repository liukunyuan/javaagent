package com.shuke.agent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * 检测方法的执行时间
 */
public class MonitorTransformer implements ClassFileTransformer {

    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";
    // 超过100ms才打印
    final static int limitTimeMillis = 100;
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // 这里我们限制下，只针对目标包下进行耗时统计
        className = className.replace("/", ".");
        if(className.startsWith("java") || className.startsWith("sun")){
            return null;
        }
        if(!className.contains("org.apache.hadoop.hive.metastore.HiveAlterHandler")
                &&!className.toLowerCase().contains("listener")
                &&!className.contains("org.apache.hadoop.hive.metastore.api")
                &&!className.contains("org.apache.hadoop.hive.metastore.HiveMetaStore")
                &&!(className.toLowerCase().contains("event") && className.contains("org.apache.hadoop.hive.metastore"))){

            return null;
        }

        CtClass cl = null;
        try {
            ClassPool classPool = ClassPool.getDefault();
            cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            for (CtMethod method : cl.getDeclaredMethods()) {
                // 所有方法，统计耗时；请注意，需要通过`addLocalVariable`来声明局部变量
                try{
                    method.addLocalVariable("start", CtClass.longType);
                    method.insertBefore("start = System.currentTimeMillis();");
                    String methodName = method.getLongName();
//                    method.insertAfter("if(System.currentTimeMillis()-start> 100){System.out.println(\"" + methodName + " cost(毫秒): \" + (System" +
//                            ".currentTimeMillis() - start)); }");

                    method.insertAfter("if(System.currentTimeMillis()-start> "+limitTimeMillis+"){" +
                            "com.shuke.util.LogUtil.info(\"" + methodName + " cost(毫1秒): \" + (System" +
                            ".currentTimeMillis() - start)); " +
                            "}");
                }catch (CannotCompileException e){
                    continue;
                }
            }

            byte[] transformed = cl.toBytecode();
            return transformed;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classfileBuffer;
    }
}
