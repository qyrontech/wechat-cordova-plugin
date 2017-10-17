package com.taiemao.wxapi;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class PayResult {
    private int resultStatus;
    private String result;

    public PayResult(int resultStatus, String result) {
        this.resultStatus = resultStatus;
        this.result = result;
    }

    @Override
    public String toString() {
        return "resultStatus={" + resultStatus + "};result={" + result + "}";
    }

    public JSONObject toJson(){
        Map<String, String> payResultsMap = new HashMap<String, String>() {{
            put("resultStatus", String.valueOf(resultStatus));
            put("result", result);
        }};
        return new JSONObject(payResultsMap);
    }

    /**
     * @return the resultStatus
     */
    public int getResultStatus() {
        return resultStatus;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }
}
