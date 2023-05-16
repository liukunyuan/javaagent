package test;

public class MyAgentTest {
    public static void main(String[] args) throws InterruptedException {
        String className = Class.class.getName();
        String methodName = "main";
        String str = "LoggerFactory.getLogger("+className+".class).info(\"" + methodName + " cost(毫秒): \" + (System" +
                ".currentTimeMillis() - start));";

        System.out.println(str);


//        MyAgentTest mat = new MyAgentTest();
//        while(true){
//            mat.test();
//            Thread.sleep((long)(Math.random() * 10));//随机暂停0-10ms
//
//        }
    }

    public void test() throws InterruptedException {
        System.out.println("I'm TestAgent");
            Thread.sleep((long)(Math.random() * 100));//随机暂停0-100ms
    }
}
