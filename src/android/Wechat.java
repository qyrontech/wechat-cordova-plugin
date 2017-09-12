package com.taiemao;

import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaArgs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import com.taiemao.wxapi.PayResult;

/**
 * This class echoes a string called from JavaScript.
 */
public class Wechat extends CordovaPlugin {

    public static final String TAG = "Cordova.Plugin.Wechat";

    public static final int ERROR_INVALID_PARAMETERS_CODE = -7;
    public static final int ERROR_SEND_REQUEST_FAILED_CODE = -8;
    public static final int ERROR_WECHAT_RESPONSE_UNKNOWN_CODE = -9;
    public static final String SUCESS_PAY = "支付成功";
    public static final String ERROR_INVALID_PARAMETERS = "参数格式错误";
    public static final String ERROR_SEND_REQUEST_FAILED = "发送请求失败";
    public static final String ERROR_WECHAT_RESPONSE_COMMON = "普通错误";
    public static final String ERROR_WECHAT_RESPONSE_USER_CANCEL = "用户点击取消并返回";
    public static final String ERROR_WECHAT_RESPONSE_SENT_FAILED = "发送失败";
    public static final String ERROR_WECHAT_RESPONSE_AUTH_DENIED = "授权失败";
    public static final String ERROR_WECHAT_RESPONSE_UNSUPPORT = "微信不支持";
    public static final String ERROR_WECHAT_RESPONSE_UNKNOWN = "未知错误";

    public static final String PREFS_NAME = "Cordova.Plugin.Wechat";
    public static final String WXAPPID_PROPERTY_KEY = "wechatappid";

    protected static CallbackContext currentCallbackContext;
    private static IWXAPI wxAPI;

    private static String appId;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        Log.i(TAG, ":<<<------------------------------------>>>");
        Log.i(TAG, ":<<<------------------------------------>>>");
        Log.i(TAG, ":<<<------------------------------------>>>");

        super.initialize(cordova, webView);

        appId = webView.getPreferences().getString("APPID", "");

        Log.i(TAG + "appId: ", appId);

        // save app id
        saveAppId(cordova.getActivity(), appId);

        // init api
        initWXAPI();

        Log.i(TAG, "plugin initialized.");
    }


    private void initWXAPI() {
        IWXAPI api = getWxAPI(cordova.getActivity());

        if (api != null) {
            api.registerApp(getAppId());
            Log.i(TAG, ":<<<--------api register------------>>>");
        } else {
            Log.i(TAG, ":<<<--------api not register------------>>>");
        }
    }

    /**
     * Get weixin api
     * @param ctx
     * @return
     */
    public static IWXAPI getWxAPI(Context ctx) {
        if (wxAPI == null) {
            String appId = getSavedAppId(ctx);

            if (!appId.isEmpty()) {
                wxAPI = WXAPIFactory.createWXAPI(ctx, appId);
            }
        }

        return wxAPI;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, String.format("%s is called. Callback ID: %s.", action, callbackContext.getCallbackId()));

        return pay(args, callbackContext);
    }

    protected boolean pay(JSONArray args, CallbackContext callbackContext) {

        final IWXAPI api = getWxAPI(cordova.getActivity());

        // check if # of arguments is correct
        final JSONObject params;
        try {
            params = args.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.error(new PayResult(ERROR_INVALID_PARAMETERS_CODE, ERROR_INVALID_PARAMETERS).toJson());
            return true;
        }

        PayReq req = new PayReq();

        try {
            req.appId = getAppId();
            req.partnerId = params.getString("partnerid");
            req.prepayId = params.getString("prepayid");
            req.nonceStr = params.getString("noncestr");
            req.timeStamp = params.getString("timestamp");
            req.sign = params.getString("sign");
            req.packageValue = "Sign=WXPay";

            Log.i(TAG + "partnerId", req.partnerId);
            Log.i(TAG + "prepayId", req.prepayId);
            Log.i(TAG + "nonceStr", req.nonceStr);
            Log.i(TAG + "timeStamp", req.timeStamp);
            Log.i(TAG + "partnerId", req.partnerId);
            Log.i(TAG + "sign", req.sign);
            Log.i(TAG + "packageValue", req.packageValue);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());

            callbackContext.error(new PayResult(ERROR_INVALID_PARAMETERS_CODE, ERROR_INVALID_PARAMETERS).toJson());
            return true;
        }

        if (api.sendReq(req)) {
            Log.i(TAG, "Payment request has been sent successfully.");

            // send no result
            sendNoResultPluginResult(callbackContext);
        } else {
            Log.i(TAG, "Payment request has been sent unsuccessfully.");

            // send error
            callbackContext.error(new PayResult(ERROR_SEND_REQUEST_FAILED_CODE, ERROR_SEND_REQUEST_FAILED).toJson());
        }

        return true;
    }

    public String getAppId() {
        if (appId == null) {
            appId = preferences.getString(WXAPPID_PROPERTY_KEY, "");
        }

        return appId;
    }

    /**
     * Get saved app id
     * @param ctx
     * @return
     */
    public static String getSavedAppId(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(WXAPPID_PROPERTY_KEY, "");
    }

    /**
     * Save app id into SharedPreferences
     * @param ctx
     * @param id
     */
    public static void saveAppId(Context ctx, String id) {
        if (id.isEmpty()) {
            return ;
        }

        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(WXAPPID_PROPERTY_KEY, id);
        editor.commit();
    }

    public static CallbackContext getCurrentCallbackContext() {
        return currentCallbackContext;
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        // save current callback context
        currentCallbackContext = callbackContext;

        // send no result and keep callback
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
}
