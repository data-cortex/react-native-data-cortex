'use strict';

var React = require('react-native');
var {
  requireNativeComponent,
} = React;

var DataCortex = require('react-native').NativeModules.DataCortex;

exports.init = init;
exports.addUserTag = addUserTag;
exports.eventWithProperties = eventWithProperties;
exports.economyWithProperties = economyWithProperties;

function init(api_key, org)
{
	DataCortex.sharedInstance(api_key, org);
}

function addUserTag(userTag)
{
	DataCortex.addUserTag(userTag);
}

function eventWithProperties(properties)
{
	DataCortex.eventWithProperties(properties);
}

function economyWithProperties(properties, spendCurrency, spendAmount)
{
	DataCortex.economyWithProperties(properties, spendCurrency, spendAmount);
}