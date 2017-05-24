package com.ydttech.optc;

import com.mmm.driver.IEventListener;
import com.mmm.mapping.EventInfo;
import com.ydttech.optc.core.ReaderClient;
import com.ydttech.optc.util.*;
import com.ydttech.optc.vo.config.GateLaneInfo;
import com.ydttech.optc.vo.config.IBGSConfig;
import com.ydttech.optc.vo.jsondata.*;
import com.ydttech.optc.vo.packdata.GatePacketData;
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
public class GateProcess implements Runnable {

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

    GatePacketData entryPacketData;

    private SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    private GateLaneInfo gateLaneInfo;

    public GateProcess(GateLaneInfo gateLaneInfo) {
        this.gateLaneInfo = gateLaneInfo;
    }

    IEventListener iEventListener = new IEventListener() {
        public void EventFound(Object o, EventInfo eventInfo) {
            logger.info("gateLane:{} event found, eventInfo data:{}", gateLaneInfo.getGateName(), eventInfo.getEventData());

            if (eventInfo.getEventType() == EventInfo.EVENT_TYPES.TAG_ARRIVE) {

//                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TID);
                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String epc = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String ant = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.ANTENNA);
                String rssi = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.RSSI);

                entryPacketData.setETagData(new ETagData(tid, epc, dstSdf.format(new Date()), ant, rssi));

                if (entryPacketData.getLprDataList().size() == 0) {
                    ResultSet resultSet = dbUtil.executeQuery("call sp_getLPR('" + epc.trim() + "')");
                    String lpNo = null;
                    try {
                        if (resultSet.next()) {
                            lpNo = resultSet.getString("LPR");
                            logger.info("gateLane:{} LPNo:{} EPC:{} isEntry:{} isExit:{}",
                                    gateLaneInfo.getGateName(), lpNo, epc.trim(), isEntry, isExit);
                            entryPacketData.setLPRData(new LPRData(lpNo, "", ""));
                            barrierModbus.setDOValue(Integer.parseInt(gateLaneInfo.getBarrierDoIndex()), true);
                        }
                    } catch (Exception e) {
                        logger.error("db error:{}", e.getMessage());
                    }
                }

            }
        }
    };

    @Override
    public void run() {
        logger.info("Gate Lane:{} process start initial ......", gateLaneInfo.getGateName());

        HttpReqInit();
        DatabaseInit();
        ReaderInit();
        ModbusInit();
        MQInit();
//        IOMQInit();

        logger.info("Gate Lane:{} process start successfully ......", gateLaneInfo.getGateName());

        while (running) {
            try {
                Thread.yield();
                if (!modbusUtil.isAlive()) {
                    if (modbusUtil.open()) {
                        modbusUtil.activeEvent();
                        logger.info("Modbus Slave:{} connection is re-opened!", modbusUtil.getIoCtrlId());
                    } else
                        logger.info("Modbus Slave:{} is not alive!", modbusUtil.getIoCtrlId());
                }

                if (!barrierModbus.isAlive()) {
                    if (barrierModbus.open()) {
                        barrierModbus.activeEvent();
                        logger.info("barrierModbus Slave:{} connection is re-opened!", barrierModbus.getIoCtrlId());
                    } else
                        logger.info("barrierModbus Slave:{} is not alive!", barrierModbus.getIoCtrlId());
                }

                if (!readerClient.isAlive()) {
                    readerCon = false;
                    logger.info("reader:{} is disconnected!", gateLaneInfo.getReaderName());
                    logger.info("Trying to re-connect to reader:{} ...", gateLaneInfo.getReaderName());
                    readerClient.close();
                    if (readerClient.open()) {
                        readerClient.registerEvent(iEventListener, "event.tag.arrive");
                        readerCon = true;
                        logger.info("re-connect to reader:{} is successfully", gateLaneInfo.getReaderName());
                    }
                }
                Thread.sleep(5000);
                logger.debug("gateLane:{} check process is done.", gateLaneInfo.getGateName());
            } catch (Exception e) {
                logger.error("gateLane:{} sensor main loop exception:{}", gateLaneInfo.getGateName(), e.getMessage());
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
            logger.error("gateLane:{} db open connection to {} is not open successful!", gateLaneInfo.getGateName(), dbURL);
            System.exit(1);
        } else {
            logger.debug("gateLane:{} db open connection to {} is successfully!", gateLaneInfo.getGateName(), dbURL);
        }
    }

    private void HttpReqInit() {
//        httpReqUtil = new HttpReqUtil(IBGSConfig.HTTPCFG_GATEURI);
        entryHttpReqUtil = new HttpReqUtil(IBGSConfig.HTTPCFG_ENTRYURI);
        exitHttpReqUtil = new HttpReqUtil(IBGSConfig.HTTPCFG_EXITURI);
    }

    private void MQInit() {

        String connURI = IBGSConfig.MQCFG_MQURL;
        String username = IBGSConfig.MQCFG_USERNAME;
        String password = IBGSConfig.MQCFG_PASSWORD;
        String topic = gateLaneInfo.getReaderName();
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                        if (((TextMessage) message).getText().equalsIgnoreCase("active")) {
                            if (readerClient.getOpMode() == 0) {
                                if (!readerClient.activeEvent()) {
                                    logger.error("gateLane reader:{} active event mode is failed!", gateLaneInfo.getReaderName());
                                }
                                else {
                                    logger.info("gateLane reader:{} is in active event!", gateLaneInfo.getReaderName());
                                    entryPacketData = new GatePacketData();
                                }
                            }
                        } else if (((TextMessage) message).getText().equalsIgnoreCase("standby")) {
                            if (readerClient.getOpMode() == 1)
                                if (!readerClient.standbyEvent())
                                    logger.error("gateLane reader:{} standby event mode is failed!", gateLaneInfo.getReaderName());
                                else {
                                    logger.info("gateLane reader:{} is changed into standby event mode!", gateLaneInfo.getReaderName());
                                    if (entryPacketData != null &&  entryPacketData.geteTagDataHashMap().size() > 0) {
                                        entryPacketData.setLaneData(new LaneData(gateLaneInfo.getGateName(), gateLaneInfo.getReaderName(), gateLaneInfo.getReaderIp()));
                                        entryPacketData.setBarrierData(new BarrierData("1", dstSdf.format(new Date())));
                                        entryPacketData.setExtData(new ExtData(dstSdf.format(new Date()), dstSdf.format(new Date()), dstSdf.format(new Date())));

                                        httpReqUtil = isEntry ? entryHttpReqUtil : exitHttpReqUtil;
                                        httpReqUtil.send(entryPacketData.getJsonContent());
                                        logger.info("gateLane:{} url:{}  data:{}",
                                                gateLaneInfo.getGateName(),  httpReqUtil.getPostURL(), entryPacketData.getJsonContent());
//                                        modbusUtil.setDOValue(Integer.parseInt(gateLaneInfo.getBarrierDoIndex()), true);
                                        entryPacketData = null;
                                        isEntry = false; isExit = false;
                                    }
                                    barrierModbus.setDOValue(Integer.parseInt(gateLaneInfo.getBarrierDoIndex()), false);
                                }
                        } else
                            logger.info("invalid message:{}", ((TextMessage) message).getText());

                } catch (JMSException e) {
                    logger.error("on message error:{}", e.getMessage());
                }

            }
        };

        mqUtil = new MQUtil(connURI, username, password);

        if (!mqUtil.open()) {
            logger.info("MQ server:{} connection is failure!", connURI);
            System.exit(1);
        } else {
            logger.debug("MQ server:{} connection is successful!", connURI);
        }

        if (!mqUtil.createTopic(topic)) {
            logger.info("MQ server:{} create topic:{} is failure!", connURI, topic);
            System.exit(1);
        } else {
            logger.debug("MQ server:{} create topic:{} is is successful!", connURI, topic);
        }

        if (!mqUtil.registerEvent(messageListener)) {
            logger.info("MQ server:{} register event is failure!", connURI);
        } else {
            logger.debug("MQ server:{} register event is successful!", connURI);
        }
    }

    private void ModbusInit() {

        String ioCtrlId = gateLaneInfo.getIocName();
        String ioCtrlIP = gateLaneInfo.getIocIp();

        String barrierCtrlId = gateLaneInfo.getBarrierName();
        String barrierIP = gateLaneInfo.getBarrierIP();

        final int diAmount = 8;
        int doAmount = 8;

        CoilsEventListener barrierEventListener = new CoilsEventListener() {
            @Override
            public void DIChangeEvent(DICoilsMessage diCoilsMessage) {
                logger.info("{}", diCoilsMessage.toString());
            }

            @Override
            public void DOChangeEvent(DOCoilsMessage doCoilsMessage) {
                logger.info("open barrier:{}", doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getBarrierDoIndex())));
            }
        };


        CoilsEventListener coilsEventListener = new CoilsEventListener() {
            @Override
            public void DIChangeEvent(DICoilsMessage diCoilsMessage) {

            }

            @Override
            public void DOChangeEvent(DOCoilsMessage doCoilsMessage) {

                if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getEntryDo())) == true) {
                    isEntry = true;
                    readerClient.setAntennaMux(gateLaneInfo.getEntryAnt());
                    mqUtil.send("test", "test", "entry", "active");
                } else if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getExitDo())) == true) {
                    isExit = true;
                    readerClient.setAntennaMux(gateLaneInfo.getExitAnt());
                    mqUtil.send("test", "test", "exit", "active");
                } else if (isEntry && doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getEntryDo())) == false) {
                    mqUtil.send("test", "test", "entry", "standby");
                } else if (isExit && doCoilsMessage.getBitVector().getBit(Integer.parseInt(gateLaneInfo.getExitDo())) == false) {
                    mqUtil.send("test", "test", "exit", "standby");
                }

//                int readerDO = Integer.parseInt(gateLaneInfo.getActiveDo());
//
//                if (doCoilsMessage.getBitVector().getBit(readerDO) == true) {
//                    mqUtil.send("test", "test", "test", "active");
//                } else if (doCoilsMessage.getBitVector().getBit(readerDO) == false) {
//                    mqUtil.send("test", "test", "test", "standby");
//                }

            }
        };

        modbusUtil = new ModbusUtil(ioCtrlId, ioCtrlIP, diAmount, doAmount);
        barrierModbus = new ModbusUtil(barrierCtrlId, barrierIP, diAmount, doAmount);

        modbusUtil.setEventTimer(Long.parseLong(gateLaneInfo.getIocTimer()));
        barrierModbus.setEventTimer(100);

        if (!modbusUtil.open()) {
            logger.info("Modbus Slave:{} connection to {} is failure!", ioCtrlId, ioCtrlIP);
            System.exit(1);
        } else {
            logger.debug("Modbus Slave:{} connection to {} is successful!", ioCtrlId, ioCtrlIP);
        }

        if (modbusUtil.registerEvent(coilsEventListener)) {
            logger.debug("Modbus Slave:{} register event timer:{} is successful!", ioCtrlId, gateLaneInfo.getIocTimer());
            modbusUtil.activeEvent();
        } else {
            logger.error("Modbus Slave:{} register event timer:{} is failure!", ioCtrlId, gateLaneInfo.getIocTimer());
        }

        if (!barrierModbus.open()) {
            logger.error("Modbus Slave:{} connection to {} is failure!", barrierCtrlId, barrierIP);
            System.exit(1);
        } else {
            logger.debug("Modbus Slave:{} connection to {} is successful!", barrierCtrlId, barrierIP);
        }

        if (barrierModbus.registerEvent(barrierEventListener)) {
            logger.debug("Modbus Slave:{} register event timer:{} is successful!", barrierCtrlId, 1000);
            barrierModbus.activeEvent();
        } else {
            logger.error("Modbus Slave:{} register event timer:{} is failure!", barrierCtrlId, 1000);
        }

    }

    private void ReaderInit() {

        String ipAddress = gateLaneInfo.getReaderIp();
        String readerId = gateLaneInfo.getReaderName();

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
            logger.info("reader:{} standby event mode is failed!", readerId);
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

                            logger.debug("ioLogik cmmand str:[{}], doIndex:{} value:{}", command, doIndex, value);

                            modbusUtil.setDOValue(doIndex, value);
                        } else
                            logger.info("invalid message:{}", ((TextMessage) message).getText());
                } catch (JMSException e) {
                    logger.error("on message error:{}", e.getMessage());
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
    }
}
