package com.ydttech.optc.vo.config;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by Ean.Chung on 2017/5/16.
 */
public class IBGSConfig {

    private static Logger logger = LoggerFactory.getLogger("IBGSConfig");

    public static String initCfgDirectory = "";
    public static String initCfgFilename = "../conf/envSIT.xml";
    public static String initCfg = initCfgDirectory+initCfgFilename;

    public static String DBCFG_DBURL;
    public static String DBCFG_DRIVERNAME;
    public static String DBCFG_USERNAME;
    public static String DBCFG_PASSWORD;

    public static String MQCFG_MQURL;
    public static String MQCFG_USERNAME;
    public static String MQCFG_PASSWORD;

    public static String HTTPCFG_SILOURI;
    public static String HTTPCFG_ENTRYURI;
    public static String HTTPCFG_EXITURI;

    private static HashMap<String, GateLaneInfo> gateLaneInfoHashMap = new HashMap<String, GateLaneInfo>();
    private static HashMap<String, SiloLaneInfo> siloLaneInfoHashMap = new HashMap<String, SiloLaneInfo>();
    private static HashMap<String, WeighLaneInfo> weighLaneInfoHashMap = new HashMap<String, WeighLaneInfo>();


    public static void init() {
        loadIBGSConfig();
    }

    private static void loadIBGSConfig() {
        Document docCfgXml = null;
        SAXReader reader = new SAXReader();
        Properties props = System.getProperties();
        Node node;
        List<Node> nodeList;

        try {
            docCfgXml = reader.read(new File(initCfg));

            node = docCfgXml.selectSingleNode("//dbCfg");
            if (node.hasContent()) {
                DBCFG_DBURL = node.valueOf("dbURL");
                DBCFG_DRIVERNAME = node.valueOf("driverName");
                DBCFG_USERNAME = node.valueOf("username");
                DBCFG_PASSWORD = node.valueOf("password");
            } else {
                logger.error("loadIBGSConfig DBCFG no content!");
                System.exit(1);
            }

            node = docCfgXml.selectSingleNode("//mqCfg");
            if (node.hasContent()) {
                MQCFG_MQURL = node.valueOf("mqURL");
                MQCFG_USERNAME = node.valueOf("username");
                MQCFG_PASSWORD = node.valueOf("password");
            } else {
                logger.error("loadIBGSConfig MQCFG no content!");
                System.exit(1);
            }

            node = docCfgXml.selectSingleNode("//httpCfg");
            if (node.hasContent()) {
                HTTPCFG_SILOURI = node.valueOf("siloURI");
                HTTPCFG_ENTRYURI = node.valueOf("entryURI");
                HTTPCFG_EXITURI = node.valueOf("exitURI");
            } else {
                logger.error("loadIBGSConfig HTTPCFG no content!");
                System.exit(1);
            }

            nodeList = docCfgXml.selectNodes("//gateLane/gate");
            if (nodeList.size() == 0) {
                logger.warn("loadIBGSConfig gateLane no content!");
            }

            for (Node nodeTmp : nodeList) {

                GateLaneInfo gateLaneInfo = new GateLaneInfo();
                gateLaneInfo.setGateName(nodeTmp.valueOf("@name"));
                gateLaneInfo.setReaderName(nodeTmp.selectSingleNode("reader").valueOf("@name"));
                gateLaneInfo.setReaderIp(nodeTmp.selectSingleNode("reader").valueOf("@ip"));
                gateLaneInfo.setEntryAnt(nodeTmp.selectSingleNode("reader").valueOf("@entryAnt"));
                gateLaneInfo.setExitAnt(nodeTmp.selectSingleNode("reader").valueOf("@exitAnt"));


                gateLaneInfo.setIocName(nodeTmp.selectSingleNode("ioc").valueOf("@name"));
                gateLaneInfo.setIocIp(nodeTmp.selectSingleNode("ioc").valueOf("@ip"));
                gateLaneInfo.setIocTimer(nodeTmp.selectSingleNode("ioc").valueOf("@timer"));
                gateLaneInfo.setEntryDo(nodeTmp.selectSingleNode("ioc").valueOf("@entryDo"));
                gateLaneInfo.setExitDo(nodeTmp.selectSingleNode("ioc").valueOf("@exitDo"));

                gateLaneInfo.setBarrierName(nodeTmp.selectSingleNode("barrier").valueOf("@name"));
                gateLaneInfo.setBarrierDoIndex(nodeTmp.selectSingleNode("barrier").valueOf("@openDo"));
                gateLaneInfo.setBarrierIP(nodeTmp.selectSingleNode("barrier").valueOf("@ip"));

                gateLaneInfoHashMap.put(gateLaneInfo.getGateName(), gateLaneInfo);
                logger.debug("gate lane info:{}", gateLaneInfo.toString());
            }

            nodeList = docCfgXml.selectNodes("//siloLane/silo");
            if (nodeList.size() == 0) {
                logger.warn("loadIBGSConfig siloLane no content!");
            }

            for (Node nodeTmp : nodeList) {

                SiloLaneInfo siloLaneInfo = new SiloLaneInfo();
                siloLaneInfo.setSiloNo(nodeTmp.valueOf("@no"));
                siloLaneInfo.setReaderName(nodeTmp.selectSingleNode("reader").valueOf("@name"));
                siloLaneInfo.setReaderIp(nodeTmp.selectSingleNode("reader").valueOf("@ip"));

                siloLaneInfoHashMap.put(siloLaneInfo.getSiloNo(), siloLaneInfo);
                logger.debug("silo lane info:{}", siloLaneInfo.toString());
            }

            nodeList = docCfgXml.selectNodes("//weighLane/weigh");
            if (nodeList.size() == 0) {
                logger.warn("loadIBGSConfig weighLane no content!");
            }

            for (Node nodeTmp : nodeList) {

                WeighLaneInfo weighLaneInfo = new WeighLaneInfo();
                weighLaneInfo.setWeighName(nodeTmp.valueOf("@name"));
                weighLaneInfo.setReaderName(nodeTmp.selectSingleNode("reader").valueOf("@name"));
                weighLaneInfo.setReaderIp(nodeTmp.selectSingleNode("reader").valueOf("@ip"));
                weighLaneInfo.setEntryAnt(nodeTmp.selectSingleNode("reader").valueOf("@entryAnt"));
                weighLaneInfo.setExitAnt(nodeTmp.selectSingleNode("reader").valueOf("@exitAnt"));

                weighLaneInfo.setIocName(nodeTmp.selectSingleNode("ioc").valueOf("@name"));
                weighLaneInfo.setIocIp(nodeTmp.selectSingleNode("ioc").valueOf("@ip"));
                weighLaneInfo.setIocTimer(nodeTmp.selectSingleNode("ioc").valueOf("@timer"));
                weighLaneInfo.setEntryDo(nodeTmp.selectSingleNode("ioc").valueOf("@entryDo"));
                weighLaneInfo.setExitDo(nodeTmp.selectSingleNode("ioc").valueOf("@exitDo"));

                weighLaneInfoHashMap.put(weighLaneInfo.getWeighName(), weighLaneInfo);
                logger.debug("weigh lane info:{}", weighLaneInfo.toString());
            }

        } catch (Exception e) {
            logger.error("read env. config file:{} error:{}", initCfg, e.getMessage());
        }
    }

    public static HashMap<String, GateLaneInfo> getGateLaneInfoHashMap() {
        return gateLaneInfoHashMap;
    }

    public static void setGateLaneInfoHashMap(HashMap<String, GateLaneInfo> gateLaneInfoHashMap) {
        IBGSConfig.gateLaneInfoHashMap = gateLaneInfoHashMap;
    }

    public static HashMap<String, SiloLaneInfo> getSiloLaneInfoHashMap() {
        return siloLaneInfoHashMap;
    }

    public static void setSiloLaneInfoHashMap(HashMap<String, SiloLaneInfo> siloLaneInfoHashMap) {
        IBGSConfig.siloLaneInfoHashMap = siloLaneInfoHashMap;
    }

    public static HashMap<String, WeighLaneInfo> getWeighLaneInfoHashMap() {
        return weighLaneInfoHashMap;
    }

    public static void setWeighLaneInfoHashMap(HashMap<String, WeighLaneInfo> weighLaneInfoHashMap) {
        IBGSConfig.weighLaneInfoHashMap = weighLaneInfoHashMap;
    }
}
