'use strict';

import React from 'react-native';

const { DataCortex } = React.NativeModules;

export function init(api_key,org) {
  DataCortex.sharedInstance(api_key,org);
}

export function addUserTag(userTag) {
  if (userTag && typeof userTag != 'string') {
    userTag = userTag.toString();
  }
  DataCortex.addUserTag(userTag);
}

export function event(props) {
  if (!props || typeof props !== 'object') {
    throw new Error('props must be an object');
  }

  DataCortex.eventWithProperties(props);
}

export function economyEvent(props) {
  if (!props || typeof props != 'object' )
  {
    throw new Error('props must be an object');
  }
  if (!props.spendCurrency) {
    throw new Error('spendCurrency is required');
  }
  if (typeof props.spendAmount != 'number') {
    throw new Error('spendAmount is required');
  }

  DataCortex.economyWithProperties(props,props.spendCurrency,props.spendAmount);
}

export default { init, addUserTag, event, economyEvent };
