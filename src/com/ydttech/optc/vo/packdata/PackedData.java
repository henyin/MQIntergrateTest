package com.ydttech.optc.vo.packdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydttech.optc.vo.*;
import com.ydttech.optc.vo.jsondata.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Ean.Chung on 2017/3/23.
 */
public class PackedData {

    List<LaneData> laneDataList = new ArrayList<LaneData>();
    List<SiloData> siloDataList = new ArrayList<SiloData>();
    List<ETagData> eTagDataList = new ArrayList<ETagData>();
    List<LPRData> lprDataList = new ArrayList<LPRData>();
    List<BarrierData> barrierDataList = new ArrayList<BarrierData>();
    ExtData extData = new ExtData();

    ObjectMapper mapper = new ObjectMapper();
    String jsonString;
    Map<String, Object> dataMap = new HashMap<String, Object>();

    private static SimpleDateFormat dstSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public PackedData() {
    }

    public void creatJSON(int packType) {

        LaneData laneData = new LaneData();
        if (packType == 1 || packType == 2) {
            if (packType == 1) {
                laneData.setName("T1-1");
                laneData.setReader("一號門入口");
            }
            else {
                laneData.setName("T1-2");
                laneData.setReader("一號門出口");
            }
            laneDataList.add(laneData);
            dataMap.put("lane", laneDataList);

            lprDataList.add(new LPRData());

            BarrierData barrierData = new BarrierData();
            barrierData.setOpenDT(dstSdf.format(new Date()));
            barrierDataList.add(barrierData);

            dataMap.put("lpr", lprDataList);
            dataMap.put("barrier", barrierDataList);
        }





        if (packType == 3) {
            SiloData siloData = new SiloData();
            siloData.setReader("SILO 區");
            dataMap.put("silo", siloDataList);
            siloDataList.add(new SiloData());
        }

        {
            ETagData eTagData = new ETagData();

            eTagData.setTid("OPTCH0008");
            eTagData.setAnt("1");
            eTagData.setEpc("OPTCH0008");
            eTagData.setRssi("-33");
            eTagData.setArriveDT(dstSdf.format(new Date()));
            eTagDataList.add(eTagData);
        }

        if (packType == 1) {
            ETagData personData = new ETagData();
            personData.setTid("OPTCH0061");
            personData.setAnt("2");
            personData.setEpc("OPTCH0061");
            personData.setRssi("-50");
            personData.setArriveDT(dstSdf.format(new Date()));

            eTagDataList.add(personData);

            ETagData tailData = new ETagData();
            tailData.setTid("OPTCH0170");
            tailData.setAnt("2");
            tailData.setEpc("OPTCH0170");
            tailData.setRssi("-50");
            tailData.setArriveDT(dstSdf.format(new Date()));
            eTagDataList.add(tailData);


        }

        if (packType == 2) {
            ETagData eTagData = new ETagData();
            eTagData.setTid("OPTCH0119");
            eTagData.setAnt("2");
            eTagData.setEpc("OPTCH0119");
            eTagData.setRssi("-50");
            eTagData.setArriveDT(dstSdf.format(new Date()));
            eTagDataList.add(eTagData);
        }

        if (packType == 3) {
            ETagData personData = new ETagData();
            personData.setTid("OPTCH0061");
            personData.setAnt("2");
            personData.setEpc("OPTCH0061");
            personData.setRssi("-50");
            personData.setArriveDT(dstSdf.format(new Date()));

            eTagDataList.add(personData);
            ETagData tailData = new ETagData();
            tailData.setTid("OPTCH0170");
            tailData.setAnt("2");
            tailData.setEpc("OPTCH0170");
            tailData.setRssi("-50");
            tailData.setArriveDT(dstSdf.format(new Date()));
            eTagDataList.add(tailData);
        }


        dataMap.put("tag", eTagDataList);

        try {
            jsonString = mapper.writeValueAsString(dataMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public String getJSON() {
        return jsonString;
    }

    public List<ETagData> geteTagDataList() {
        return eTagDataList;
    }
}
