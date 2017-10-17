/********* wechat-cordova-plugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "WXApi.h"
#import "WXApiObject.h"

@interface Wechat : CDVPlugin {
  // Member variables go here.
}

@property (nonatomic, strong) NSString *currentCallbackId;
@property (nonatomic, strong) NSString *wechatAppId;

- (void)pay:(CDVInvokedUrlCommand *)command;
@end

@implementation Wechat

#pragma mark "API"
- (void)pluginInitialize {

  NSString* appId = [[self.commandDelegate settings] objectForKey:@"appid"];
  if (appId){
    self.wechatAppId = appId;
    [WXApi registerApp: appId];
  }
}


- (void)pay:(CDVInvokedUrlCommand*)command {
  // check arguments
  NSDictionary *params = [command.arguments objectAtIndex:0];
  if (!params) {
    NSDictionary *resultDic = @{@"resultStatus":[NSString stringWithFormat:@"%d",-7], @"result":@"参数格式错误"};
    [self failWithCallbackID:command.callbackId messageAsDictionary:resultDic];
    return ;
  }
  // check required parameters
  NSArray *requiredParams;
  if ([params objectForKey:@"partnerid"]) {
    requiredParams = @[@"partnerid", @"prepayid", @"timestamp", @"noncestr", @"sign"];
  }

  for (NSString *key in requiredParams) {
    if (![params objectForKey:key]) {
      NSDictionary *resultDic = @{@"resultStatus":[NSString stringWithFormat:@"%d",-7], @"result":@"参数格式错误"};
      [self failWithCallbackID:command.callbackId messageAsDictionary:resultDic];
      return ;
    }
  }

  PayReq *req = [[PayReq alloc] init];
  req.partnerId = [params objectForKey:requiredParams[0]];
  req.prepayId = [params objectForKey:requiredParams[1]];
  req.timeStamp = [[params objectForKey:requiredParams[2]] intValue];
  req.nonceStr = [params objectForKey:requiredParams[3]];
  req.package = @"Sign=WXPay";
  req.sign = [params objectForKey:requiredParams[4]];

  if ([WXApi sendReq:req]) {
    // save the callback id
    self.currentCallbackId = command.callbackId;
  }
  else {
    NSDictionary *resultDic = @{@"resultStatus":[NSString stringWithFormat:@"%d",-8], @"result":@"发送请求失败"};
    [self failWithCallbackID:command.callbackId messageAsDictionary:resultDic];
  }
}


#pragma mark "WXApiDelegate"

- (void)onResp:(BaseResp *)resp {

  BOOL success = NO;
  NSString *message = @"Unknown";
  NSDictionary *response = nil;

  switch (resp.errCode) {
    case WXSuccess:
        success = YES;
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXSuccess], @"result":@"支付成功"};
    break;

    case WXErrCodeCommon:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXErrCodeCommon], @"result":@"普通错误"};
    break;

    case WXErrCodeUserCancel:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXErrCodeUserCancel], @"result":@"用户点击取消并返回"};
    break;

    case WXErrCodeSentFail:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXErrCodeSentFail], @"result":@"发送失败"};
    break;

    case WXErrCodeAuthDeny:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXErrCodeAuthDeny], @"result":@"授权失败"};
    break;

    case WXErrCodeUnsupport:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",WXErrCodeUnsupport], @"result":@"微信不支持"};
    break;

    default:
        response = @{@"resultStatus":[NSString stringWithFormat:@"%d",-9], @"result":@"未知错误"};
  }

  if (success) {
    [self successWithCallbackID:self.currentCallbackId messageAsDictionary:response];
  }
  else {
    [self failWithCallbackID:self.currentCallbackId messageAsDictionary:response];
  }

  self.currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"

- (void)handleOpenURL:(NSNotification *)notification {
  NSURL* url = [notification object];

  if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:self.wechatAppId]) {
    [WXApi handleOpenURL:url delegate:self];
  }
}

- (void)successWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message {
  CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
  [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)successWithCallbackID:(NSString *)callbackID messageAsDictionary:(NSDictionary *)message {
  CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:message];
  [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message {
  CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
  [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID messageAsDictionary:(NSDictionary *)message {
  CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:message];
  [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

@end
