package com.data_cortex;

import android.content.SharedPreferences;
import android.util.Log;
import android.content.Context;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;


public class DataCortexModule extends ReactContextBaseJavaModule {

  private static final String TAG = "DataCortexModule";

  private final ReactApplicationContext mReactContext;
  private final SharedPreferences mPrefs;
  private String mApiKey;
  private String mOrgName;
  private String mUserTag = null;


  public DataCortexModule(ReactApplicationContext reactContext) {

    super(reactContext);

    mReactContext = reactContext;
    mPrefs = reactContext.getSharedPreferences("data-cortex",Context.MODE_PRIVATE);
    mUserTag = mPrefs.getString("user_tag",null);
  }

  @Override
  public String getName() {
    return "DataCortex";
  }

  @ReactMethod
  public void sharedInstance(final String apiKey,final String orgName, final Callback callback) {
    Log.i(TAG,"sharedInstance: apiKey: " + apiKey + " orgName: " + orgName);
    Log.i(TAG,"userTag: " + mUserTag);
    callback.invoke((Object)null);
  }

  @ReactMethod
  public void addUserTag(final String userTag) {
    Log.i(TAG,"addUserTag: " + userTag);
    mUserTag = userTag;
    final SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString("user_tag", mUserTag);
    editor.commit();
  }

  @ReactMethod
  public void eventWithProperties(final ReadableMap props) {
    Log.i(TAG,"eventWithProperties: " + props);
    Log.i(TAG,"userTag: " + mUserTag);
  }

  @ReactMethod
  public void economyWithProperties(final ReadableMap props,
    final String spendCurrency, final Double spendAmount) {
    Log.i(TAG,"economyWithProperties: " + props);
  }

}
