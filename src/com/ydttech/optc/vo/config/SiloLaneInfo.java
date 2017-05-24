package com.ydttech.optc.vo.config;

/**
 * Created by Ean.Chung on 2017/5/16.
 */
public class SiloLaneInfo {

    private String siloNo;
    private String readerName;
    private String readerIp;

    public String getSiloNo() {
        return siloNo;
    }

    public void setSiloNo(String siloNo) {
        this.siloNo = siloNo;
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

    @Override
    public String toString() {
        return "SiloLaneInfo{" +
                "siloNo='" + siloNo + '\'' +
                ", readerName='" + readerName + '\'' +
                ", readerIp='" + readerIp + '\'' +
                '}';
    }
}
