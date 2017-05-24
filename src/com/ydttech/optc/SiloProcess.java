package com.ydttech.optc;

import com.mmm.driver.IEventListener;
import com.mmm.mapping.EventInfo;
import com.ydttech.optc.core.ReaderClient;
import com.ydttech.optc.util.*;
import com.ydttech.optc.vo.config.GateLaneInfo;
import com.ydttech.optc.vo.config.IBGSConfig;
import com.ydttech.optc.vo.config.SiloLaneInfo;
import com.ydttech.optc.vo.jsondata.*;
import com.ydttech.optc.vo.packdata.GatePacketData;
import com.ydttech.optc.vo.packdata.SiloPacketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ean.Chung on 2017/5/5.
 */
public class SiloProcess implements Runnable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean running = true;
    private Boolean readerEvent = new Boolean(false);
    private boolean isEntry = false, isExit = false;
    private boolean readerCon = false;

    private ReaderClient readerClient;
    private MQUtil mqUtil, ioMQUtil;
    private ModbusUtil modbusUtil, barrierModbus;
    private HttpReqUtil httpReqUtil;
    private HttpReqUtil entryHttpReqUtil, exitHttpReqUtil;
    private DBUtil dbUtil;

    SiloPacketData entryPacketData;

    private SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    private SiloLaneInfo siloLaneInfo;

    public SiloProcess(SiloLaneInfo siloLaneInfo) {
        this.siloLaneInfo = siloLaneInfo;
    }

    IEventListener iEventListener = new IEventListener() {
        public void EventFound(Object o, EventInfo eventInfo) {
            logger.info("silo:{} event found, eventInfo data:{}", siloLaneInfo.getSiloNo(), eventInfo.getEventData());

            if (eventInfo.getEventType() == EventInfo.EVENT_TYPES.TAG_ARRIVE) {
                entryPacketData = new SiloPacketData();

//                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TID);
                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String epc = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String ant = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.ANTENNA);
                String rssi = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.RSSI);

                entryPacketData.setETagData(new ETagData(tid, epc, dstSdf.format(new Date()), ant, rssi));
                entryPacketData.setSiloData(new SiloData(siloLaneInfo.getSiloNo(), siloLaneInfo.getReaderName(), siloLaneInfo.getReaderIp()));
                entryPacketData.setExtData(new ExtData(dstSdf.format(new Date()), dstSdf.format(new Date()), dstSdf.format(new Date())));

                logger.info("url:{}  data:{}", httpReqUtil.getPostURL(), entryPacketData.getJsonContent());
                httpReqUtil.send(entryPacketData.getJsonContent());
                entryPacketData = null;

//                ResultSet resultSet = dbUtil.executeQuery("call sp_getLPR('" + epc.trim() + "')");
//                String lpNo = null;
//                try {
//                    if (resultSet.next()) {
//                        lpNo = resultSet.getString("LPR");
//                        entryPacketData.setLPRData(new LPRData(lpNo, "", ""));
//                    }
//                } catch (Exception e) {
//                    logger.error("db error:{}", e.getMessage());
//                }

            }
        }
    };

    @Override
    public void run() {
        logger.info("Silo Lane:{} process start ......", siloLaneInfo.getSiloNo());

        HttpReqInit();
        DatabaseInit();
        ReaderInit();
//        ModbusInit();
        MQInit();
//        IOMQInit();


        while (running) {
            try {
                Thread.yield();
//                if (!modbusUtil.isAlive()) {
//                    if (modbusUtil.open()) {
//                        modbusUtil.activeEvent();
//                        logger.info("Modbus Slave connection is re-opened!");
//                    } else
//                        logger.info("Modbus Slave is not alive!");
//                }

//                if (!barrierModbus.isAlive()) {
//                    if (barrierModbus.open()) {
//                        logger.info("barrierModbus Slave connection is re-opened!");
//                    } else
//                        logger.info("barrierModbus Slave is not alive!");
//                }

                if (!readerClient.isAlive()) {
                    readerCon = false;
                    logger.info("reader:{} is disconnected!", siloLaneInfo.getReaderName());
                    logger.info("Trying to re-connect to reader:{} ...", siloLaneInfo.getReaderName());
                    readerClient.close();
                    if (readerClient.open()) {
                        readerClient.registerEvent(iEventListener, "event.tag.arrive");
                        readerCon = true;
                        logger.info("re-connect to reader:{} is successfully", siloLaneInfo.getReaderName());
                    }
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                logger.error("sensor main loop exception:{}", e.getMessage());
            }

        }

    }

    private void DatabaseInit() {

        String dbURL = IBGSConfig.DBCFG_DBURL;
        String driverName = IBGSConfig.DBCFG_DRIVERNAME;
        String username = IBGSConfig.DBCFG_USERNAME;
        String password = IBGSConfig.DBCFG_PASSWORD;

        dbUtil = new DBUtil(driverName);

        if (!dbUtil.open(dbURL, username, password)) {
            logger.error("siloLane:{} db open connection to {} is not open successful!", siloLaneInfo.getSiloNo(), dbURL);
            System.exit(1);
        } else {
            logger.debug("siloLane:{} db open connection to {} is successfully!", siloLaneInfo.getSiloNo(), dbURL);
        }
    }

    private void HttpReqInit() {
        httpReqUtil = new HttpReqUtil(IBGSConfig.HTTPCFG_SILOURI);
    }

    private void MQInit() {

        String connURI = IBGSConfig.MQCFG_MQURL;
        String username = IBGSConfig.MQCFG_USERNAME;
        String password = IBGSConfig.MQCFG_PASSWORD;
        String topic = siloLaneInfo.getReaderName();
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                        if (((TextMessage) message).getText().equalsIgnoreCase("active")) {
                            if (readerClient.getOpMode() == 0) {
                                if (!readerClient.activeEvent()) {
                                    logger.info("silo reader:{} active event mode is failed!", siloLaneInfo.getReaderName());
                                }
                                else {
                                    logger.info("silo reader:{} is in active event!", siloLaneInfo.getReaderName());
                                    entryPacketData = new SiloPacketData();
                                }
                            }
                        } else if (((TextMessage) message).getText().equalsIgnoreCase("standby")) {
                            if (readerClient.getOpMode() == 1)
                                if (!readerClient.standbyEvent())
                                    logger.info("silo reader:{} standby event mode is failed!", siloLaneInfo.getReaderName());
                                else {
                                    logger.info("silo reader:{} is changed into standby event mode!", siloLaneInfo.getReaderName());
                                    if (entryPacketData != null &&  entryPacketData.geteTagDataHashMap().size() > 0) {
                                        entryPacketData.setSiloData(new SiloData(siloLaneInfo.getSiloNo(), siloLaneInfo.getReaderName(), siloLaneInfo.getReaderIp()));
                                        entryPacketData.setExtData(new ExtData(dstSdf.format(new Date()), dstSdf.format(new Date()), dstSdf.format(new Date())));

//                                        httpReqUtil = isEntry ? entryHttpReqUtil : exitHttpReqUtil;
                                        httpReqUtil.send(entryPacketData.getJsonContent());
                                        logger.info("siloLane:{} url:{}  data:{}",
                                                siloLaneInfo.getSiloNo(),  httpReqUtil.getPostURL(), entryPacketData.getJsonContent());
//                                        modbusUtil.setDOValue(Integer.parseInt(gateLaneInfo.getBarrierDoIndex()), true);
                                        entryPacketData = null;
                                        isEntry = false; isExit = false;
                                    }
//                                    modbusUtil.setDOValue(Integer.parseInt(gateLaneInfo.getBarrierDoIndex()), false);
                                }
                        } else
                            logger.info("invalid message:{}", ((TextMessage) message).getText());

                } catch (JMSException e) {
                    logger.info("on message error:{}", e.getMessage());
                }

            }
        };

        mqUtil = new MQUtil(connURI, username, password);

        if (!mqUtil.open()) {
            logger.error("MQ server:{} connection is failure!", connURI);
            System.exit(1);
        } else {
            logger.debug("MQ server:{} connection is successful!", connURI);
        }

        if (!mqUtil.createTopic(topic)) {
            logger.error("MQ server:{} create topic:{} is failure!", connURI, topic);
            System.exit(1);
        } else {
            logger.debug("MQ server:{} create topic:{} is is successful!", connURI, topic);
        }

        if (!mqUtil.registerEvent(messageListener)) {
            logger.error("MQ server:{} register event is failure!", connURI);
        } else {
            logger.debug("MQ server:{} register event is successful!", connURI);
        }

        mqUtil.send("test", "test", "entry", "active");
    }

//    private void ModbusInit() {
//
//        String ioCtrlId = gateLaneInfo.getIocName();
//        String ioCtrlIP = gateLaneInfo.getIocIp();
//
//        String barrierCtrlId = gateLaneInfo.getBarrierName();
//
//
//        int diAmount = 8;
//        int doAmount = 8;
//        CoilsEventListener coilsEventListener = new CoilsEventListener() {
//            @Override
//            public void DIChangeEvent(DICoilsMessage diCoilsMessage) {
//
//            }
//
//            @Override
//            public void DOChangeEvent(DOCoilsMessage doCoilsMessage) {
//
//                if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getEntryDo())) == true) {
//                    isEntry = true;
//                    mqUtil.send("test", "test", "entry", "active");
//                } else if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getExitDo())) == true) {
//                    isExit = true;
//                    mqUtil.send("test", "test", "exit", "active");
//                } else if (isEntry && doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getEntryDo())) == false) {
//                    mqUtil.send("test", "test", "entry", "standby");
//                } else if (isExit && doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getExitDo())) == false) {
//                    mqUtil.send("test", "test", "exit", "standby");
//                }
//
////                int readerDO = Integer.parseInt(gateLaneInfo.getActiveDo());
////
////                if (doCoilsMessage.getBitVector().getBit(readerDO) == true) {
////                    mqUtil.send("test", "test", "test", "active");
////                } else if (doCoilsMessage.getBitVector().getBit(readerDO) == false) {
////                    mqUtil.send("test", "test", "test", "standby");
////                }
//
//            }
//        };
//
//        modbusUtil = new ModbusUtil(ioCtrlId, ioCtrlIP, diAmount, doAmount);
//        barrierModbus = new ModbusUtil(barrierCtrlId, ioCtrlIP, diAmount, doAmount);
//
//        modbusUtil.setEventTimer(Long.parseLong(gateLaneInfo.getIocTimer()));
//
//        if (!modbusUtil.open()) {
//            logger.info("Modbus Slave:{} connection to {} is failure!", ioCtrlId, ioCtrlIP);
//            System.exit(1);
//        } else {
//            logger.info("Modbus Slave:{} connection to {} is successful!", ioCtrlId, ioCtrlIP);
//        }
//
//        if (modbusUtil.registerEvent(coilsEventListener)) {
//            logger.info("Modbus Slave:{} register event timer:{} is successful!", ioCtrlId, gateLaneInfo.getIocTimer());
//
//            modbusUtil.activeEvent();
//        } else {
//            logger.info("Modbus Slave:{} register event timer:{} is failure!", ioCtrlId, gateLaneInfo.getIocTimer());
//        }
//
//        if (!barrierModbus.open()) {
//            logger.info("Modbus Slave:{} connection to {} is failure!", barrierCtrlId, ioCtrlIP);
//            System.exit(1);
//        } else {
//            logger.info("Modbus Slave:{} connection to {} is successful!", barrierCtrlId, ioCtrlIP);
//        }
//
//    }

    private void ReaderInit() {

        String ipAddress = siloLaneInfo.getReaderIp();
        String readerId = siloLaneInfo.getReaderName();

//        IEventListener tagEventHandler = new IEventListener() {
//            @Override
//            public void EventFound(Object o, EventInfo eventInfo) {
//                logger.info("event found, eventInfo data:{}", eventInfo.getEventData());
//
//                if (eventInfo.getEventType() == EventInfo.EVENT_TYPES.TAG_ARRIVE) {
//                    String epc = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
//
//                    PackedData packedData = new PackedData();
//                    packedData.creatJSON(1);
//
//                    httpReqUtil.send(packedData.getJSON());
//                    barrierModbus.setDOValue(1, true);
//                }
//            }
//        };


        readerClient = new ReaderClient(readerId, ipAddress);

        if (!readerClient.open()) {
            logger.error("reader:{} connection to {} is not open successful!", readerId, ipAddress);
            System.exit(1);
        } else {
            logger.debug("reader:{} connection to {} is opened successfully!", readerId, ipAddress);
            readerCon = true;
        }

        if (!readerClient.setDataField()) {
            logger.error("reader:{} set report field is failure!", readerId);
            System.exit(1);
        } else {
            logger.debug("reader:{} set report field is successful!", readerId);
        }

        if (!readerClient.standbyEvent())
            logger.error("reader:{} standby event mode is failed!", readerId);
        else
            logger.debug("reader:{} standby event mode is successful!", readerId);

        if (readerClient.registerEvent(iEventListener, "event.tag.arrive") != null) {
            logger.debug("reader:{} register event is successful!", readerId);
        } else
            logger.error("reader:{} register event is failed!", readerId);
    }


    private void IOMQInit() {

        String connURI = "amqp://192.168.1.155:5672";
        String username = "admin";
        String password = "password";
        String topic = "ioLogik";
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                        String command = ((TextMessage) message).getText();
                        if (command.startsWith("DO")) {
                            int doIndex = 0;
                            boolean value = false;
                            String[] tokens = command.split(":");
                            doIndex = Integer.parseInt(tokens[1]);
                            value = Boolean.parseBoolean(tokens[2]);

                            logger.info("ioLogik cmmand str:[{}], doIndex:{} value:{}", command, doIndex, value);

                            modbusUtil.setDOValue(doIndex, value);
                        } else
                            logger.info("invalid message:{}", ((TextMessage) message).getText());
                } catch (JMSException e) {
                    logger.info("on message error:{}", e.getMessage());
                }

            }
        };

        mqUtil = new MQUtil(connURI, username, password);

        if (!mqUtil.open()) {
            logger.info("MQ server:{} connection is failure!", connURI);
            System.exit(1);
        } else {
            logger.info("MQ server:{} connection is successful!", connURI);
        }

        if (!mqUtil.createTopic(topic)) {
            logger.info("MQ server:{} create topic:{} is failure!", connURI, topic);
            System.exit(1);
        } else {
            logger.info("MQ server:{} create topic:{} is is successful!", connURI, topic);
        }

        if (!mqUtil.registerEvent(messageListener)) {
            logger.info("MQ server:{} register event is failure!", connURI);
        } else {
            logger.info("MQ server:{} register event is successful!", connURI);
        }
    }
}
