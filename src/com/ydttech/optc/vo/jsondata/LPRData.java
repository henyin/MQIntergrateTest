package com.ydttech.optc.vo.jsondata;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class LPRData {

    String lpNo;
    String recogDT;
    String realLPNo;

    public LPRData() {
    }

    public LPRData(String lpNo, String recogDT, String realLPNo) {
        this.lpNo = lpNo;
        this.recogDT = recogDT;
        this.realLPNo = realLPNo;
    }

    public String getLpNo() {
        return lpNo;
    }

    public void setLpNo(String lpNo) {
        this.lpNo = lpNo;
    }

    public String getRecogDT() {
        return recogDT;
    }

    public void setRecogDT(String recogDT) {
        this.recogDT = recogDT;
    }

    public String getRealLPNo() {
        return realLPNo;
    }

    public void setRealLPNo(String realLPNo) {
        this.realLPNo = realLPNo;
    }
}
