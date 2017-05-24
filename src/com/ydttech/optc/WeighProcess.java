package com.ydttech.optc;

import com.mmm.driver.IEventListener;
import com.mmm.mapping.EventInfo;
import com.ydttech.optc.core.ReaderClient;
import com.ydttech.optc.util.*;
import com.ydttech.optc.vo.config.GateLaneInfo;
import com.ydttech.optc.vo.config.IBGSConfig;
import com.ydttech.optc.vo.config.WeighLaneInfo;
import com.ydttech.optc.vo.jsondata.*;
import com.ydttech.optc.vo.packdata.GatePacketData;
import com.ydttech.optc.vo.packdata.WeighPacketData;
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
public class WeighProcess implements Runnable {

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

    WeighPacketData entryPacketData;

    private SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    private WeighLaneInfo weighLaneInfo;

    public WeighProcess(WeighLaneInfo weighLaneInfo) {
        this.weighLaneInfo = weighLaneInfo;
    }

    IEventListener iEventListener = new IEventListener() {
        public void EventFound(Object o, EventInfo eventInfo) {
            logger.debug("weighLane:{} event found, eventInfo data:{}", weighLaneInfo.getWeighName(), eventInfo.getEventData());

            if (eventInfo.getEventType() == EventInfo.EVENT_TYPES.TAG_ARRIVE) {

//                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TID);
                String tid = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String epc = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.TAG_ID);
                String ant = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.ANTENNA);
                String rssi = eventInfo.getParameter(EventInfo.EVENT_TAG_ARRIVE_PARAMS.RSSI);

                entryPacketData.setETagData(new ETagData(tid, epc, dstSdf.format(new Date()), ant, rssi));

                ResultSet resultSet = dbUtil.executeQuery("call sp_getLPR('" + epc.trim() + "')");
                String lpNo = null;
                try {
                    if (resultSet.next()) {
                        lpNo = resultSet.getString("LPR");
                        entryPacketData.setLPRData(new LPRData(lpNo, "", ""));
                    }
                } catch (Exception e) {
                    logger.error("db error:{}", e.getMessage());
                }

            }
        }
    };

    @Override
    public void run() {
        logger.info("Weigh Lane:{} process start ......", weighLaneInfo.getWeighName());

        HttpReqInit();
        DatabaseInit();
        ReaderInit();
        ModbusInit();
        MQInit();
//        IOMQInit();


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

//                if (!barrierModbus.isAlive()) {
//                    if (barrierModbus.open()) {
//                        logger.info("barrierModbus Slave connection is re-opened!");
//                    } else
//                        logger.info("barrierModbus Slave is not alive!");
//                }

                if (!readerClient.isAlive()) {
                    readerCon = false;
                    logger.info("reader:{} is disconnected!", weighLaneInfo.getReaderName());
                    logger.info("Trying to re-connect to reader:{} ...", weighLaneInfo.getReaderName());
                    readerClient.close();
                    if (readerClient.open()) {
                        readerClient.registerEvent(iEventListener, "event.tag.arrive");
                        readerCon = true;
                        logger.info("re-connect to reader:{} is successfully", weighLaneInfo.getReaderName());
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
            logger.error("weighLane:{} db open connection to {} is not open successful!", weighLaneInfo.getWeighName(), dbURL);
            System.exit(1);
        } else {
            logger.debug("weighLane:{} db open connection to {} is successfully!", weighLaneInfo.getWeighName(), dbURL);
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
        String topic = weighLaneInfo.getReaderName();
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                        if (((TextMessage) message).getText().equalsIgnoreCase("active")) {
                            if (readerClient.getOpMode() == 0) {
                                if (!readerClient.activeEvent()) {
                                    logger.error("gateLane reader:{} active event mode is failed!", weighLaneInfo.getReaderName());
                                }
                                else {
                                    logger.info("gateLane reader:{} is in active event!", weighLaneInfo.getReaderName());
                                    entryPacketData = new WeighPacketData();
                                }
                            }
                        } else if (((TextMessage) message).getText().equalsIgnoreCase("standby")) {
                            if (readerClient.getOpMode() == 1)
                                if (!readerClient.standbyEvent())
                                    logger.error("weighLane reader:{} standby event mode is failed!", weighLaneInfo.getReaderName());
                                else {
                                    logger.info("weighLaneLane reader:{} is changed into standby event mode!", weighLaneInfo.getReaderName());
                                    if (entryPacketData != null &&  entryPacketData.geteTagDataHashMap().size() > 0) {
                                        entryPacketData.setLaneData(new LaneData(weighLaneInfo.getWeighName(), weighLaneInfo.getReaderName(), weighLaneInfo.getReaderIp()));
                                        entryPacketData.setBarrierData(new BarrierData("0", dstSdf.format(new Date())));
                                        entryPacketData.setExtData(new ExtData(dstSdf.format(new Date()), dstSdf.format(new Date()), dstSdf.format(new Date())));

                                        httpReqUtil = isEntry ? entryHttpReqUtil : exitHttpReqUtil;

                                        httpReqUtil.send(entryPacketData.getJsonContent());
                                        logger.info("weighLane:{} url:{}  data:{}",
                                                weighLaneInfo.getWeighName(),  httpReqUtil.getPostURL(), entryPacketData.getJsonContent());

                                        entryPacketData = null;
                                        isEntry = false; isExit = false;
                                    }
//                                    modbusUtil.setDOValue(Integer.parseInt(weighLaneInfo.getBarrierDoIndex()), false);
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
    }

    private void ModbusInit() {

        String ioCtrlId = weighLaneInfo.getIocName();
        String ioCtrlIP = weighLaneInfo.getIocIp();

//        String barrierCtrlId = weighLaneInfo.getBarrierName();


        int diAmount = 8;
        int doAmount = 8;
        CoilsEventListener coilsEventListener = new CoilsEventListener() {
            @Override
            public void DIChangeEvent(DICoilsMessage diCoilsMessage) {

            }

            @Override
            public void DOChangeEvent(DOCoilsMessage doCoilsMessage) {

                if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(weighLaneInfo.getEntryDo())) == true) {
                    isEntry = true;
                    readerClient.setAntennaMux("1");
                    mqUtil.send("test", "test", "entry", "active");
                } else if (doCoilsMessage.getBitVector().getBit(Integer.parseInt(weighLaneInfo.getExitDo())) == true) {
                    isExit = true;
                    readerClient.setAntennaMux("2");
                    mqUtil.send("test", "test", "exit", "active");
                } else if (isEntry && doCoilsMessage.getBitVector().getBit(Integer.parseInt(weighLaneInfo.getEntryDo())) == false) {
                    mqUtil.send("test", "test", "entry", "standby");
                } else if (isExit && doCoilsMessage.getBitVector().getBit(Integer.parseInt(weighLaneInfo.getExitDo())) == false) {
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
//        barrierModbus = new ModbusUtil(barrierCtrlId, ioCtrlIP, diAmount, doAmount);

        modbusUtil.setEventTimer(Long.parseLong(weighLaneInfo.getIocTimer()));

        if (!modbusUtil.open()) {
            logger.error("Modbus Slave:{} connection to {} is failure!", ioCtrlId, ioCtrlIP);
            System.exit(1);
        } else {
            logger.debug("Modbus Slave:{} connection to {} is successful!", ioCtrlId, ioCtrlIP);
        }

        if (modbusUtil.registerEvent(coilsEventListener)) {
            logger.debug("Modbus Slave:{} register event timer:{} is successful!", ioCtrlId, weighLaneInfo.getIocTimer());

            modbusUtil.activeEvent();
        } else {
            logger.error("Modbus Slave:{} register event timer:{} is failure!", ioCtrlId, weighLaneInfo.getIocTimer());
        }

//        if (!barrierModbus.open()) {
//            logger.info("Modbus Slave:{} connection to {} is failure!", weighLaneInfo, ioCtrlIP);
//            System.exit(1);
//        } else {
//            logger.info("Modbus Slave:{} connection to {} is successful!", weighLaneInfo, ioCtrlIP);
//        }

    }

    private void ReaderInit() {

        String ipAddress = weighLaneInfo.getReaderIp();
        String readerId = weighLaneInfo.getReaderName();

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
