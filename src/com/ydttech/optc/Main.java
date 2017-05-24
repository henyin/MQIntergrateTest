package com.ydttech.optc;

import com.ydttech.optc.vo.config.GateLaneInfo;
import com.ydttech.optc.vo.config.IBGSConfig;
import com.ydttech.optc.vo.config.SiloLaneInfo;
import com.ydttech.optc.vo.config.WeighLaneInfo;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main {

    private static Logger logger = LoggerFactory.getLogger("Main");
    private static Map<String, Thread> gateProcessMap = new HashMap<String, Thread>();

    private static String initLogDirectory = "../conf/";
    private static String initLogFilename = "log4j2.xml";
    private static String initLog = initLogDirectory+initLogFilename;

    public static void main(String[] args) {

        if (!read_env_cfg()) {
            logger.info("read configure file is failure!");
            System.exit(1);
        }

        logger.debug("IBGSConfig.getGateLaneInfoHashMap().size():{}", IBGSConfig.getGateLaneInfoHashMap().size());
        logger.debug("IBGSConfig.getSiloLaneInfoHashMap().size():{}", IBGSConfig.getSiloLaneInfoHashMap().size());
        logger.debug("IBGSConfig.getWeighLaneInfoHashMap().size():{}", IBGSConfig.getWeighLaneInfoHashMap().size());

        for (GateLaneInfo gateLaneInfo : IBGSConfig.getGateLaneInfoHashMap().values()) {
            new Thread(new GateProcess(gateLaneInfo)).start();
        }

        for (WeighLaneInfo weighLaneInfo : IBGSConfig.getWeighLaneInfoHashMap().values()) {
            new Thread(new WeighProcess(weighLaneInfo)).start();
        }

        for (SiloLaneInfo siloLaneInfo : IBGSConfig.getSiloLaneInfoHashMap().values()) {
            new Thread(new SiloProcess(siloLaneInfo)).start();
        }

    }

    static boolean read_env_cfg() {

        boolean resultCode = true;

        IBGSConfig.init();

        return resultCode;
    }
}
