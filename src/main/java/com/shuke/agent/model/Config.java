package com.shuke.agent.model;

public class Config {

    private String type;
    private String className;
    private String methodName;
    private int limitTimeMillis;
    private boolean printArgs;

    private int limitTimeMillisPrintArgs;

    private double limitSample;

    public Config(String type,
                  String className,
                  String methodName,
                  String limitTimeMillis,
                  String printArgs,
                  String limitSampleStr
    ) {
        this.type = type;
        this.className = className;
        this.methodName = methodName;

        this.limitTimeMillis = Integer.parseInt(limitTimeMillis);
        this.printArgs = printArgs.equals("true") ? true : false;
        this.limitSample=Double.parseDouble(limitSampleStr);
    }

    public Config(String type,
                  String className,
                  String methodName,
                  String limitTimeMillis,
                  String printArgs,
                  String limitSampleStr,
                  String limitTimeMillisPrintArgs
    ) {
        this.type = type;
        this.className = className;
        this.methodName = methodName;

        this.limitTimeMillis = Integer.parseInt(limitTimeMillis);
        this.printArgs = printArgs.equals("true") ? true : false;
        this.limitSample=Double.parseDouble(limitSampleStr);
        this.limitTimeMillisPrintArgs = Integer.parseInt(limitTimeMillisPrintArgs);
    }


    public int getLimitTimeMillisPrintArgs() {
        return limitTimeMillisPrintArgs;
    }

    public void setLimitTimeMillisPrintArgs(int limitTimeMillisPrintArgs) {
        this.limitTimeMillisPrintArgs = limitTimeMillisPrintArgs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLimitSample() {
        return limitSample;
    }

    public void setLimitSample(double limitSample) {
        this.limitSample = limitSample;
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
