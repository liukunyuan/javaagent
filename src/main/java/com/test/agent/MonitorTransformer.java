//package com.test.agent;
//
//import javassist.*;
//import javassist.bytecode.CodeAttribute;
//import javassist.bytecode.LocalVariableAttribute;
//
//import java.io.ByteArrayInputStream;
//import java.lang.instrument.ClassFileTransformer;
//import java.security.ProtectionDomain;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 检测方法的执行时间
// */
//public class MonitorTransformer implements ClassFileTransformer {
//
//    // 超过500ms才打印
//    final static int limitTimeMillis = 500;
//
//    @Override
//    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
//                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
//        // 这里我们限制下，只针对目标包下进行耗时统计
//        className = className.replace("/", ".");
//        if (className.startsWith("java") || className.startsWith("sun")) {
//            return null;
//        }
//        if (!className.contains("org.apache.hadoop.hive.metastore.HiveAlterHandler")
//                && !className.toLowerCase().contains("listener")
//                && !className.contains("org.apache.hadoop.hive.metastore.api")
//                && !className.contains("com.shuke.agent")
//                && !className.contains("org.apache.hadoop.hive.metastore.HiveMetaStore")
//                && !(className.toLowerCase().contains("event") && className.contains("org.apache.hadoop.hive.metastore"))) {
//
//            return null;
//        }
//
//        CtClass cl = null;
//        try {
//            ClassPool classPool = ClassPool.getDefault();
//            cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
//            for (CtMethod method : cl.getDeclaredMethods()) {
//                // 所有方法，统计耗时；请注意，需要通过`addLocalVariable`来声明局部变量
//                try {
//                    /**
//                     * 过滤本地方法,否则会报
//                     */
//                    if (Modifier.isNative(method.getModifiers())) {
//                        continue;
//                    }
//
//
//
//                    method.addLocalVariable("monitorStart", CtClass.longType);
//                    method.insertBefore("monitorStart = System.currentTimeMillis();");
//                    String methodName = method.getLongName();
//
//
//                    String printMethodParams = "";
//
//                    int i=0;
//                    try{
//
//                        List<String> paramNames = getParamNames(method);
//                        if(null!=paramNames && paramNames.size()>0){
//                            printMethodParams=createJavaString(methodName,paramNames);
//                        }
//
//
//                    }catch (Exception e){
////                        com.shuke.util.LogUtil.error(e.getMessage(),e);
//                    }
//
//
//                    String body = "if(System.currentTimeMillis()-monitorStart> "+limitTimeMillis+"){" +
//                            "long monitorEnd = (System.currentTimeMillis() - monitorStart) ;"+
//                            "com.shuke.util.LogUtil.info(\"" + methodName + " cost(毫秒): \" +monitorEnd); " + printMethodParams+
//                            "}";
////                    com.shuke.util.LogUtil.info(body);
//                    method.insertAfter(body);
//                } catch (CannotCompileException e) {
////                    e.printStackTrace();
//                    continue;
//                }
//            }
//
//            byte[] transformed = cl.toBytecode();
//            return transformed;
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
//        return classfileBuffer;
//    }
//
//
//    private static List<String> getParamNames( CtMethod ctMethod) {
//        List<String> paramNames = new ArrayList<>();
//        try {
//            // 使用javassist的反射方法的参数名
//            javassist.bytecode.MethodInfo methodInfo = ctMethod.getMethodInfo();
//            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
//            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
//            if (attr != null) {
//                int len = ctMethod.getParameterTypes().length;
//                // 非静态的成员函数的第一个参数是this
//                int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
//                for (int i = 0; i < len; i++) {
//                    paramNames.add(attr.variableName(i +pos));
//                }
//
//            }
//            return paramNames;
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    //在javassist中$1代表方法的第一个参数,$2代表第二个参数以此类推可参考https://www.jianshu.com/p/b9b3ff0e1bf8
//    private String createJavaString(String methodName, List<String> params) {
//        if(null==params || params.size()==0){
//            return "";
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("try{");
//        for (String arg : params) {
//            stringBuilder.append("com.shuke.util.LogUtil.info(\"" + methodName + " cost(毫秒): \" +monitorEnd+\","+arg+":\"+com.alibaba.fastjson.JSONObject.toJSONString("+arg+"));");
//        }
//        stringBuilder.append(" }catch (Exception e){}");
//        return stringBuilder.toString();
//    }
//
//
//
//
//}
