package com.shuke.model;

public class Config {
    public static int finalLimitTimeMillis;
    private String className;
    private String methodName;
    private int limitTimeMillis;
    private boolean printArgs;

    public Config(String className, String methodName, String limitTimeMillis, String printArgs) {
        this.className = className;
        this.methodName = methodName;

        this.limitTimeMillis = Integer.parseInt(limitTimeMillis);
        Config.finalLimitTimeMillis = this.limitTimeMillis;
        this.printArgs = printArgs.equals("true")?true:false;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLimitTimeMillis() {
        return limitTimeMillis;
    }

    public void setLimitTimeMillis(int limitTimeMillis) {
        this.limitTimeMillis = limitTimeMillis;
    }

    public boolean isPrintArgs() {
        return printArgs;
    }

    public void setPrintArgs(boolean printArgs) {
        this.printArgs = printArgs;
    }
}
