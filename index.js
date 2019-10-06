'use strict';

import ReactNative from 'react-native';

const { DataCortex } = ReactNative.NativeModules;

export default {
  init,
  addUserTag,
  event,
  economyEvent,
  log,
  logEvent,
  getDeviceTag,
};

let is_initialized = false;
let user_tag = false;
const event_list = [];
const economy_list = [];
const log_list = [];

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
    log_list.forEach(logEvent);

    event_list.splice();
    economy_list.splice();
    log_list.splice();

    done(err);
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
  if (!props || typeof props != 'object' ) {
    throw new Error('props must be an object');
  }
  if (!props.spendCurrency) {
    throw new Error('spendCurrency is required');
  }
  if (typeof props.spendAmount != 'number') {
    throw new Error('spendAmount is required');
  }

  if (is_initialized) {
    if (props.spendType && !props.spend_type) {
      props.spend_type = props.spendType;
    }

    DataCortex.economyWithProperties(props,props.spendCurrency,props.spendAmount);
  } else {
    economy_list.push(props);
  }
}

export function log() {
  if (!arguments || arguments.length == 0) {
    throw new Error('log must have arguments');
  }
  let log_line = "";
  for (let i = 0 ; i < arguments.length ; i++) {
    const arg = arguments[i];
    if (i > 0) {
      log_line += " ";
    }

    if (_isError(arg)) {
      log_line += arg.stack;
    } else if (typeof arg == 'object') {
      try {
        log_line += JSON.stringify(arg);
      } catch(e) {
        log_line += arg;
      }
    } else {
      log_line += arg;
    }
  }
  logEvent({ log_line });
}

export function logEvent(props) {
  if (!props || typeof props != 'object') {
    throw new Error('props must be an object.');
  }

  if (is_initialized) {
    DataCortex.appLogWithProperties(props);
  } else {
    log_list.push(props);
  }
}

function _isError(e) {
  return e && e.stack && e.message
    && typeof e.stack === 'string'
    && typeof e.message === 'string';
}

export function getDeviceTag(done) {
  if (is_initialized) {
    DataCortex.getDeviceTag(done);
  } else {
    done('not_initialized');
  }
}
