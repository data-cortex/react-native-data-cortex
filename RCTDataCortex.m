//
//  RCTDataCortex.m
//  AwesomeProject
//
//  Created by Yanko Bolanos on 10/17/15.
//  Copyright © 2015 Facebook. All rights reserved.
//

#import "RCTDataCortex.h"
#import "DataCortex.h"

@implementation RCTDataCortex

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(sharedInstance: (NSString*) apiKey forOrg: (NSString*) org)
{
    NSLog(@"initializing...%@:%@", apiKey, org);
    [DataCortex sharedInstanceWithAPIKey:apiKey forOrg:org];
}


RCT_EXPORT_METHOD(eventWithProperties: (NSDictionary*) properties)
{
    NSLog(@"Event! %@", properties);
    [[DataCortex sharedInstance] eventWithProperties:properties];
}

RCT_EXPORT_METHOD(economyWithProperties: (NSDictionary*)
                  properties spendCurrency:(NSString*) spendCurrency
                  spendAmount: (float) spendAmount)
{
    NSLog(@"Economy! %@ %@ %f", properties, spendCurrency, spendAmount);
    
    [[DataCortex sharedInstance] economyWithProperties:properties
                                         spendCurrency: spendCurrency
                                           spendAmount: [NSNumber numberWithFloat:spendAmount]];
}

RCT_EXPORT_METHOD(addUserTag: (NSString*) userTag)
{
    NSLog(@"userTag: %@", userTag);
    [[DataCortex sharedInstance] addUserTag:userTag];
}

@end
