package com.test.agent;


import com.test.model.Config;
import com.test.util.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MonitorAgent {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorAgent.class);
    public static void premain(String args, Instrumentation inst){
        System.out.println("args:"+args);
        if(StringUtils.isBlank(args)){
            LOG.error("配置文件为空，跳过监控");
            return ;
        }
        ArrayList<String> configStrList = FileUtils.file2list(args, Charset.defaultCharset().toString());
        List<Config> configList = getConfigList(configStrList);
        if(null==configList || configList.size()==0){
            LOG.error("配置文件为空，跳过监控");
            return ;
        }
        LOG.info(configList.toString());
        inst.addTransformer(new MonitorTransformer2(configList));
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs,inst);
    }

    public static List<Config> getConfigList(ArrayList<String> configStrList){
        try{
            if(null==configStrList || configStrList.size()==0){
                return null;
            }
            ArrayList<Config> configs = new ArrayList<>();
            for (String line : configStrList) {
                if(StringUtils.isBlank(line)){
                    continue;
                }
                String[] arr = line.split("\\|");
                Config config = new Config(arr[0], arr[1], arr[2], arr[3],arr[4]);
                configs.add(config);
            }

            return configs;
        }catch (Exception e){
            LOG.error("读取配置文件失败");
            LOG.error(e.getMessage(),e);
        }
        return null;

    }


}