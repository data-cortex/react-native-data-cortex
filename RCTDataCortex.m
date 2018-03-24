
#import "RCTDataCortex.h"
#import "DataCortex.h"

@implementation RCTDataCortex

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(sharedInstance:(NSString *)apiKey
  forOrg:(NSString *)org
  callback:(RCTResponseSenderBlock)callback) {
  [DataCortex sharedInstanceWithAPIKey:apiKey forOrg:org];
  callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(eventWithProperties:(NSDictionary *)properties) {
  [[DataCortex sharedInstance] eventWithProperties:properties];
}

RCT_EXPORT_METHOD(economyWithProperties:(NSDictionary *)properties
  spendCurrency:(NSString *)spendCurrency
  spendAmount:(float)spendAmount) {
  [[DataCortex sharedInstance] economyWithProperties:properties
    spendCurrency:spendCurrency
    spendAmount:[NSNumber numberWithFloat:spendAmount]];
}

RCT_EXPORT_METHOD(addUserTag:(NSString *) userTag) {
  [[DataCortex sharedInstance] setUserTag:userTag];
}

RCT_EXPORT_METHOD(appLogWithProperties:(NSDictionary *)properties) {
  [[DataCortex sharedInstance] appLogWithProperties:properties];
}

@end
