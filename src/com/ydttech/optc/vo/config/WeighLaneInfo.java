package com.ydttech.optc.vo.config;

/**
 * Created by Ean.Chung on 2017/5/16.
 */
public class WeighLaneInfo {

    private String weighName;
    private String readerName;
    private String readerIp;
    private String entryAnt;
    private String exitAnt;
    private String iocName;
    private String iocIp;
    private String iocTimer;
    private String entryDo;
    private String exitDo;

    public String getWeighName() {
        return weighName;
    }

    public void setWeighName(String weighName) {
        this.weighName = weighName;
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

    public void setReaderIp(String readerIp) {
        this.readerIp = readerIp;
    }

    public String getEntryDo() {
        return entryDo;
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

    @Override
    public String toString() {
        return "WeighLaneInfo{" +
                "weighName='" + weighName + '\'' +
                ", readerName='" + readerName + '\'' +
                ", readerIp='" + readerIp + '\'' +
                ", iocName='" + iocName + '\'' +
                ", iocIp='" + iocIp + '\'' +
                ", iocTimer='" + iocTimer + '\'' +
                ", entryDo='" + entryDo + '\'' +
                ", exitDo='" + exitDo + '\'' +
                '}';
    }
}
