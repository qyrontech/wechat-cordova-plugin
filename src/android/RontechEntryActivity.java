package com.taiemao.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.apache.cordova.CallbackContext;

import com.taiemao.Wechat;

public class RontechEntryActivity extends Activity implements IWXAPIEventHandler {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IWXAPI api = Wechat.getWxAPI(this);

        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        IWXAPI api = Wechat.getWxAPI(this);
        if (api == null) {
            startMainActivity();
        } else {
            api.handleIntent(intent, this);
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        CallbackContext ctx = Wechat.getCurrentCallbackContext();

        if (ctx == null) {
            startMainActivity();
            return ;
        }

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                ctx.success(new PayResult(BaseResp.ErrCode.ERR_OK, Wechat.SUCESS_PAY).toJson());
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ctx.error(new PayResult(BaseResp.ErrCode.ERR_USER_CANCEL, Wechat.ERROR_WECHAT_RESPONSE_USER_CANCEL).toJson());
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                ctx.error(new PayResult(BaseResp.ErrCode.ERR_AUTH_DENIED, Wechat.ERROR_WECHAT_RESPONSE_AUTH_DENIED).toJson());
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                ctx.error(new PayResult(BaseResp.ErrCode.ERR_SENT_FAILED, Wechat.ERROR_WECHAT_RESPONSE_SENT_FAILED).toJson());
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                ctx.error(new PayResult(BaseResp.ErrCode.ERR_UNSUPPORT, Wechat.ERROR_WECHAT_RESPONSE_UNSUPPORT).toJson());
                break;
            case BaseResp.ErrCode.ERR_COMM:
                ctx.error(new PayResult(BaseResp.ErrCode.ERR_COMM, Wechat.ERROR_WECHAT_RESPONSE_COMMON).toJson());
                break;
            default:
                ctx.error(new PayResult(Wechat.ERROR_WECHAT_RESPONSE_UNKNOWN_CODE, Wechat.ERROR_WECHAT_RESPONSE_UNKNOWN).toJson());
                break;
        }

        finish();
    }

    @Override
    public void onReq(BaseReq req) {
        finish();
    }

    protected void startMainActivity() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(intent);
    }
}
