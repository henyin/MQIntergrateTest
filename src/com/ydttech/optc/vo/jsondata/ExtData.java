package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class ExtData {

    String readerActiveDT;
    String readerStandbyDT;
    String dataSendDT;

    public ExtData() {
    }

    public ExtData(String readerActiveDT, String readerStandbyDT, String dataSendDT) {
        this.readerActiveDT = readerActiveDT;
        this.readerStandbyDT = readerStandbyDT;
        this.dataSendDT = dataSendDT;
    }

    public String getReaderActiveDT() {
        return readerActiveDT;
    }

    public void setReaderActiveDT(String readerActiveDT) {
        this.readerActiveDT = readerActiveDT;
    }

    public String getReaderStandbyDT() {
        return readerStandbyDT;
    }

    public void setReaderStandbyDT(String readerStandbyDT) {
        this.readerStandbyDT = readerStandbyDT;
    }

    public String getDataSendDT() {
        return dataSendDT;
    }

    public void setDataSendDT(String dataSendDT) {
        this.dataSendDT = dataSendDT;
    }
}
