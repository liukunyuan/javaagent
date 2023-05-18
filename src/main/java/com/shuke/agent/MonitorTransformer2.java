package com.shuke.agent;

import com.shuke.model.Config;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 检测方法的执行时间
 */
public class MonitorTransformer2 implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorTransformer2.class);
    private List<Config> configList;

    public MonitorTransformer2() {
    }

    public MonitorTransformer2(List<Config> configList) {
        this.configList = configList;
    }

    // 超过500ms才打印
    final static int limitTimeMillis = 500;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // 这里我们限制下，只针对目标包下进行耗时统计
        className = className.replace("/", ".");
        if (className.startsWith("java") || className.startsWith("sun")) {
            return null;
        }

        LOG.info(configList.toString());
        Iterator<Config> iterator = this.configList.iterator();
        boolean monitor = false;
        Config thisConfig=null;
        while (iterator.hasNext()) {
            Config next = iterator.next();
            if (className.contains(next.getClassName())) {
                monitor = true;
                thisConfig=next;
                break;
            }
        }
        if (!monitor) {
            return null;
        }
        CtClass cl = null;
        try {
            ClassPool classPool = ClassPool.getDefault();
            cl = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            for (CtMethod method : cl.getDeclaredMethods()) {
                // 所有方法，统计耗时；请注意，需要通过`addLocalVariable`来声明局部变量
                try {
                    /**
                     * 过滤本地方法,否则会报
                     */
                    if (Modifier.isNative(method.getModifiers())) {
                        continue;
                    }


                    method.addLocalVariable("monitor_Start", CtClass.longType);
                    method.insertBefore("monitor_Start = System.currentTimeMillis(); ");

                    String methodName = method.getLongName();


                    String printMethodParams = "";
                    if(thisConfig.isPrintArgs()){
                        try {
                            List<String> paramNames = getParamNames(method);
                            if (null != paramNames && paramNames.size() > 0) {
                                printMethodParams = createJavaString(methodName, paramNames);
                            }
                        } catch (Exception e) {
                        LOG.debug(e.getMessage(),e);
                        }
                    }



                    String body = "if(System.currentTimeMillis()-monitor_Start> " + thisConfig.getLimitTimeMillis() + "){" +
                            "long monitor_End = (System.currentTimeMillis() - monitor_Start) ;" +
                            "com.shuke.util.LogUtil.info(\"" + methodName + " cost(毫秒): \" +monitor_End); " + printMethodParams +
                            "}";
//                    com.shuke.util.LogUtil.info(body);
                    method.insertAfter(body);
                } catch (CannotCompileException e) {
//                    e.printStackTrace();
                    LOG.debug(e.getMessage(),e);
                    continue;
                }
            }

            byte[] transformed = cl.toBytecode();
            return transformed;
        } catch (Exception e) {
            LOG.debug(e.getMessage(),e);
        }
        return classfileBuffer;
    }


    private static List<String> getParamNames(CtMethod ctMethod) {
        List<String> paramNames = new ArrayList<>();
        try {
            // 使用javassist的反射方法的参数名
            javassist.bytecode.MethodInfo methodInfo = ctMethod.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            if (attr != null) {
                int len = ctMethod.getParameterTypes().length;
                // 非静态的成员函数的第一个参数是this
                int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
                for (int i = 0; i < len; i++) {
                    paramNames.add(attr.variableName(i + pos));
                }

            }
            return paramNames;
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    //在javassist中$1代表方法的第一个参数,$2代表第二个参数以此类推可参考https://www.jianshu.com/p/b9b3ff0e1bf8
    private String createJavaString(String methodName, List<String> params) {
        if (null == params || params.size() == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("try{");
        for (String arg : params) {
            stringBuilder.append("com.shuke.util.LogUtil.info(\"" + methodName + " cost(毫秒): \" +monitor_End+\"," + arg + ":\"+com.alibaba.fastjson.JSONObject.toJSONString(" + arg + "));");
        }
        stringBuilder.append(" }catch (Exception e){}");
        return stringBuilder.toString();
    }


}

