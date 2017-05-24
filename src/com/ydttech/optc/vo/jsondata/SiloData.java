package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class SiloData {

    String siloNo;
    String readerName;
    String readerIP;

    public SiloData() {
    }

    public SiloData(String siloNo, String readerName, String readerIP) {
        this.siloNo = siloNo;
        this.readerName = readerName;
        this.readerIP = readerIP;
    }

    public String getNo() {
        return siloNo;
    }

    public void setNo(String siloNo) {
        this.siloNo = siloNo;
    }

    public String getReader() {
        return readerName;
    }

    public void setReader(String readerName) {
        this.readerName = readerName;
    }

    public String getReaderIP() {
        return readerIP;
    }

    public void setReaderIP(String readerIP) {
        this.readerIP = readerIP;
    }
}
