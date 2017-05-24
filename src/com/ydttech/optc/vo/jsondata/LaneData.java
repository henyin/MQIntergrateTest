package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class LaneData {

    String laneName;
    String readerName;
    String readerIP;

    public LaneData() {
    }

    public LaneData(String laneName, String readerName, String readerIP) {
        this.laneName = laneName;
        this.readerName = readerName;
        this.readerIP = readerIP;
    }

    public String getName() {
        return laneName;
    }

    public void setName(String laneName) {
        this.laneName = laneName;
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
