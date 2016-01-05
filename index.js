'use strict';

import React from 'react-native';

const { DataCortex } = React.NativeModules;

let is_initialized = false;
let user_tag = false;
const event_list = [];
const economy_list = [];

export function init(api_key,org,done) {
  if (!done) {
    done = function() {};
  }
  DataCortex.sharedInstance(api_key,org,(err) => {
    is_initialized = true;
    if (user_tag !== false) {
      addUserTag(user_tag);
    }
    event_list.forEach(event);
    economy_list.forEach(economyEvent);
  });
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
  if (is_initialized) {
    DataCortex.eventWithProperties(props);
  } else {
    event_list.push(props);
  }
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

  if (is_initialized) {
    DataCortex.economyWithProperties(props,props.spendCurrency,props.spendAmount);
  } else {
    economy_list.push(props);
  }
}

export default { init, addUserTag, event, economyEvent };
