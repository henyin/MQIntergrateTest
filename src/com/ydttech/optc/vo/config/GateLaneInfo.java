package com.ydttech.optc.vo.config;

/**
 * Created by Ean.Chung on 2017/5/16.
 */
public class GateLaneInfo {

    private String gateName;
    private String readerName;
    private String readerIp;
    private String entryAnt;
    private String exitAnt;
    private String iocName;
    private String iocIp;
    private String iocTimer;
    private String entryDo;
    private String exitDo;
    private String barrierName;
    private String barrierDoIndex;
    private String barrierIP;

    public String getGateName() {
        return gateName;
    }

    public void setGateName(String gateName) {
        this.gateName = gateName;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getReaderIp() {
        return readerIp;
    }

    public String getEntryAnt() {
        return entryAnt;
    }

    public void setEntryAnt(String entryAnt) {
        this.entryAnt = entryAnt;
    }

    public String getExitAnt() {
        return exitAnt;
    }

    public void setExitAnt(String exitAnt) {
        this.exitAnt = exitAnt;
    }

    public void setReaderIp(String readerIp) {
        this.readerIp = readerIp;
    }

    public String getEntryDo() {
        return entryDo;
    }

    public void setEntryDo(String entryDo) {
        this.entryDo = entryDo;
    }

    public String getExitDo() {
        return exitDo;
    }

    public void setExitDo(String exitDo) {
        this.exitDo = exitDo;
    }

    public String getIocName() {
        return iocName;
    }

    public void setIocName(String iocName) {
        this.iocName = iocName;
    }

    public String getIocIp() {
        return iocIp;
    }

    public void setIocIp(String iocIp) {
        this.iocIp = iocIp;
    }

    public String getIocTimer() {
        return iocTimer;
    }

    public void setIocTimer(String iocTimer) {
        this.iocTimer = iocTimer;
    }

    public String getBarrierName() {
        return barrierName;
    }

    public void setBarrierName(String barrierName) {
        this.barrierName = barrierName;
    }

    public String getBarrierDoIndex() {
        return barrierDoIndex;
    }

    public void setBarrierDoIndex(String barrierDoIndex) {
        this.barrierDoIndex = barrierDoIndex;
    }

    public String getBarrierIP() {
        return barrierIP;
    }

    public void setBarrierIP(String barrierIP) {
        this.barrierIP = barrierIP;
    }

    @Override
    public String toString() {
        return "GateLaneInfo{" +
                "gateName='" + gateName + '\'' +
                ", readerName='" + readerName + '\'' +
                ", readerIp='" + readerIp + '\'' +
                ", entryDo='" + entryDo + '\'' +
                ", exitDo='" + exitDo + '\'' +
                ", iocName='" + iocName + '\'' +
                ", iocIp='" + iocIp + '\'' +
                ", iocTimer='" + iocTimer + '\'' +
                ", barrierName='" + barrierName + '\'' +
                ", barrierDoIndex='" + barrierDoIndex + '\'' +
                ", barrierIP='" + barrierIP + '\'' +
                '}';
    }
}
