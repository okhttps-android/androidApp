package com.uas.appworks.OA.platform.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;


/**
 * Created by Bitlike on 2017/12/12.
 */

public class BusinessTravel {
    public static final String EMPTY_STATUS = "未购票";
    public static final int TITLE = 1;
    public static final int UNKOWN = 0;
    public static final int AIR = 2;
    public static final int TRAIN = 3;
    public static final int HOTEL = 4;
    public static final int LEADER = 5;
    private boolean expand;
    private String fpId;//主表id
    private int id;//明细id
    private int type;
    private int number;
    private long startTime;
    private long endTime;
    private String allTime;
    private String code;//订单编号
    private String status;//状态
    private String orderType;
    private String seat;
    private String realFee;
    private String expecteFee;
    private String level;//级别
    private String payType;
    private String businessName;
    private String remark;
    private String title;
    private String cttpid;

    private String airStarting;
    private String airStartingCode;
    private String airDestination;
    private String airDestinationCode;

    private String trainStarting;
    private String trainStartingCode;
    private String trainDestination;
    private String trainDestinationCode;

    private String hotelCity;
    private String hotelCityCode;
    private String flightCode;
    private String hotelAddress;

    public BusinessTravel() {

    }

    public static BusinessTravel createTitle(JSONObject object) {
        BusinessTravel businessTravel = new BusinessTravel();
        businessTravel.type = TITLE;
        businessTravel.fpId = JSONUtil.getText(object, "FP_ID");
        businessTravel.code = JSONUtil.getText(object, "FP_CODE");
        businessTravel.startTime = JSONUtil.getLong(object, "FP_PRESTARTDATE");
        businessTravel.endTime = JSONUtil.getLong(object, "FP_PREENDDATE");
        return businessTravel;

    }


    public BusinessTravel(String cttpid, String fpId, long dfStartTime, long dfEndTime, JSONObject reimbursement) {
        try {
            title = JSONUtil.getText(reimbursement, "FPD_RES_TYPE");
            this.fpId = fpId;
            endTime = JSONUtil.getLong(reimbursement, "FPD_END_TIME");
            startTime = JSONUtil.getLong(reimbursement, "FPD_START_TIME");
            if (startTime <= 0 && dfStartTime > 0) {
                startTime = dfStartTime;
            }
            if (endTime <= 0 && dfEndTime > 0) {
                endTime = dfEndTime;
            }
            switch (title) {
                case "火车票":
                    this.type = TRAIN;
                    trainStarting = JSONUtil.getText(reimbursement, "FPD_TRAIN_STARTING");
                    trainDestination = JSONUtil.getText(reimbursement, "FPD_TRAIN_DESTINATION");
                    trainStartingCode = JSONUtil.getText(reimbursement, "FPD_CITYCODE3");
                    trainDestinationCode = JSONUtil.getText(reimbursement, "FPD_CITYCODE4");
                    break;
                case "飞机票":
                    this.type = AIR;
                    airStarting = JSONUtil.getText(reimbursement, "FPD_AIR_STARTING");
                    airDestination = JSONUtil.getText(reimbursement, "FPD_AIR_DESTINATION");
                    airStartingCode = JSONUtil.getText(reimbursement, "FPD_CITYCODE1");
                    airDestinationCode = JSONUtil.getText(reimbursement, "FPD_CITYCODE2");
                    break;
                case "住宿":
                    this.type = HOTEL;
                    hotelCity = JSONUtil.getText(reimbursement, "FPD_HOTEL_CITY");
                    hotelCityCode = JSONUtil.getText(reimbursement, "FPD_CITYCODE5");
                    hotelAddress = JSONUtil.getText(reimbursement, "FPD_HOTEL_ADDRESS");
                    if (endTime == startTime) {
                        endTime = startTime + 86400000;
                    } else if (DateFormatUtil.long2Str(endTime, DateFormatUtil.YMD).equals(DateFormatUtil.long2Str(startTime, DateFormatUtil.YMD))) {
                        endTime = startTime + 86400000;
                    }
                    break;
                default:
                    this.type = UNKOWN;
            }
            flightCode = JSONUtil.getText(reimbursement, "FPD_FLIGHT_CODE");
            status = JSONUtil.getText(reimbursement, "FPD_STATUS");
            orderType = JSONUtil.getText(reimbursement, "FPD_ORDER_TYPE");
            seat = JSONUtil.getText(reimbursement, "FPD_SEAT");
            realFee = JSONUtil.getText(reimbursement, "FPD_REAL_FEE");
            expecteFee = JSONUtil.getText(reimbursement, "FPD_EXPECTE_FEE");
            level = JSONUtil.getText(reimbursement, "FPD_LEVEL");
            number = JSONUtil.getInt(reimbursement, "FPD_NUMBER");
            payType = JSONUtil.getText(reimbursement, "FPD_PAY_TYPE");
            businessName = JSONUtil.getText(reimbursement, "FPD_BUSINESS_NAME");
            remark = JSONUtil.getText(reimbursement, "FPD_REMARK");
            allTime = JSONUtil.getText(reimbursement, "FPD_ALL_TIME");
            this.cttpid = cttpid;
            id = JSONUtil.getInt(reimbursement, "FPD_ID");
            code = JSONUtil.getText(reimbursement, "FPD_ORDER_CODE");
            if (TextUtils.isEmpty(allTime)) {
                allTime = longTime2Time(endTime - startTime);
            }
        } catch (Exception e) {
        }
    }

    public String getFpId() {
        return fpId;
    }

    public String getProduct() {
        switch (type) {
            case AIR:
                return "air";
            case TRAIN:
                return "train";
            case HOTEL:
                return "hotel";
            default:
                return "center";
        }
    }

    public String getStarting() {
        return type == AIR ? StringUtil.getMessage(airStarting) : StringUtil.getMessage(trainStarting);
    }


    public String getDestination() {
        return type == AIR ? StringUtil.getMessage(airDestination) : StringUtil.getMessage(trainDestination);
    }


    public String getTitleAndCode() {
        return title + "   " + (StringUtil.isEmpty(code) ? "" : code);
    }

    private String longTime2Time(long time) {
        long second = time / 1000;
        long hh = second / 3600;
        long mm = (second % 3600) / 60;
        return hh + "小时" + mm + "分钟";
    }

    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    public boolean isExpand() {
        return expand;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public long getStartTime() {
        return startTime <= 0 ? System.currentTimeMillis() : startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getAllTime() {
        return allTime;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return TextUtils.isEmpty(status) ? EMPTY_STATUS : status;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getSeat() {
        return seat;
    }

    public String getRealFee() {
        return TextUtils.isEmpty(realFee) ? "" : realFee;
    }

    public String getExpecteFee() {
        return expecteFee;
    }

    public String getLevel() {
        return level;
    }

    public String getPayType() {
        return payType;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getRemark() {
        return remark;
    }

    public String getTitle() {
        return title;
    }

    public String getCttpid() {
        return cttpid;
    }

    public String getAirStarting() {
        return  airStarting;
    }

    public String getAirStartingCode() {
        return StringUtil.isEmpty(airStartingCode) ? "" : airStartingCode;
    }


    public String getAirDestination() {
        return   airDestination;
    }

    public String getAirDestinationCode() {
        return StringUtil.isEmpty(airDestinationCode) ? "" : airDestinationCode;
    }

    public String getTrainStarting() {
        return   trainStarting;
    }

    public String getTrainStartingCode() {
        return StringUtil.isEmpty(trainStartingCode) ? "" : trainStartingCode;
    }

    public String getTrainDestination() {
        return   trainDestination;
    }

    public String getTrainDestinationCode() {
        return StringUtil.isEmpty(trainDestinationCode) ? "" : trainDestinationCode;
    }


    public String getHotelCity() {
        return   hotelCity;
    }

    public String getHotelCityCode() {
        return hotelCityCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }
}
