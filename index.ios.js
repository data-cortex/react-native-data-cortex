'use strict';

var React = require('react-native');
var {
  requireNativeComponent,
} = React;

var DataCortex = require('react-native').NativeModules.DataCortex;

exports.init = init;
exports.addUserTag = addUserTag;
exports.event = event;
exports.economyEvent = economyEvent;

function init(api_key, org)
{
	DataCortex.sharedInstance(api_key, org);
}

function addUserTag(userTag)
{
	DataCortex.addUserTag(userTag);
}

function event(properties)
{
    if( properties === null || typeof properties !== 'object' )
    {
        throw new Error('properties must be an object');
    }
    
	DataCortex.eventWithProperties(properties);
}

function economyEvent(properties)
{

    if( properties === null || typeof properties !== 'object' )
    {
        throw new Error('properties must be an object');
    }

    if( properties.spendCurrency && typeof properties.spendAmount === 'number' )
    {
		DataCortex.economyWithProperties(properties, properties.spendCurrency, properties.spendAmount);
    }
    else
    {
        throw new Error('You must pass spendCurrency and spendAmount to economyEvent');
    }

}