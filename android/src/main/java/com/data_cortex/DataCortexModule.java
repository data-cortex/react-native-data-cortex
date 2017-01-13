package com.data_cortex;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class DataCortexModule extends ReactContextBaseJavaModule {
  private static final String TAG = "DataCortexModule";

  private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  private static final String BASE_URL = "https://api.data-cortex.com/";
  private static final String URL_PATH = "/1/track";

  private static final String PREF_NAME = "data-cortex";
  private static final String PREF_EVENT_LIST = "event_list";
  private static final String PREF_USER_TAG = "user_tag";
  private static final String PREF_LAST_DAU_SEND = "last_dau_send";
  private static final String PREF_INSTALL_SENT = "install_sent";
  private static final String PREF_DEVICE_TAG = "device_tag";

  private final ReactApplicationContext context;
  private final RequestQueue mQueue;
  private final DateFormat mISOFormat;
  private final SharedPreferences mPrefs;

  private String mApiKey = null;
  private String mOrgName = null;
  private String mBaseUrl = null;
  private String mUserTag = null;
  private String mDeviceTag = null;
  private boolean mInstallSent = false;
  private long mLastDAUSend = 0;
  private ArrayList<JSONObject> mEventList = new ArrayList<JSONObject>();
  private boolean mRequestInFlight = false;
  private String mAppVer = "";
  private String mOSVer = "";
  private String mDeviceFamily = "";
  private String mDeviceType = "";
  private String mLanguage = "zz";
  private String mCountry = "";

  public DataCortexModule(ReactApplicationContext reactContext) {
    super(reactContext);

    context = reactContext;
    mQueue = Volley.newRequestQueue(context);
    mISOFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
    mISOFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    mPrefs = reactContext.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);

    mUserTag = mPrefs.getString(PREF_USER_TAG,null);
    mLastDAUSend = mPrefs.getLong(PREF_LAST_DAU_SEND,0);
    mInstallSent = mPrefs.getBoolean(PREF_INSTALL_SENT,false);

    final String json = mPrefs.getString(PREF_EVENT_LIST,null);
    if (json != null && json.length() > 0) {
      try {
        final JSONArray events = new JSONArray(json);
        for( int i = 0 ; i < events.length() ; ++i) {
          mEventList.add(events.getJSONObject(i));
        }
      } catch (JSONException e) {
        Log.e(TAG,"Failed to deserialize the event list",e);
      }
    }

    try {
      final PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
      mAppVer = pInfo.versionName;
    } catch (final PackageManager.NameNotFoundException e) {
      Log.e(TAG,"Failed to get app version",e);
    }

    mOSVer = Build.VERSION.RELEASE;
    mDeviceTag = getDeviceTag();
    mDeviceType = getDeviceType();
    mDeviceFamily = getDeviceFamily();
    mLanguage = Locale.getDefault().getLanguage().toLowerCase();
    mCountry = getCountry();
  }

  @Override
  public String getName() {
    return "DataCortex";
  }

  @ReactMethod
  public void sharedInstance(final String apiKey,final String orgName,final Callback callback) {
    mApiKey = apiKey;
    mOrgName = orgName;
    mBaseUrl = BASE_URL + mOrgName + URL_PATH;
    maybeAddInstall();
    maybeAddDAU();
    maybeSendEvents();
    callback.invoke((Object)null);
  }

  @ReactMethod
  public void addUserTag(final String userTag) {
    mUserTag = userTag;
    final SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString(PREF_USER_TAG,mUserTag);
    editor.apply();
  }

  @ReactMethod
  public void eventWithProperties(final ReadableMap props) {
    addEvent("event",mapToObject(props));
  }

  @ReactMethod
  public void economyWithProperties(final ReadableMap props,
    String spendCurrency,final Double spendAmount) {
    try {
      final JSONObject obj = mapToObject(props);
      obj.put("spend_amount",spendAmount);
      if (spendCurrency.length() > 16) {
        spendCurrency = spendCurrency.substring(0,16);
      }
      obj.put("spend_currency",spendCurrency);
      addEvent("economy",obj);
    } catch(final JSONException e) {
      Log.e(TAG,"economyWithProperties: json problem",e);
    }
  }

  private static String _getMapValueAsString(final ReadableMap map,final String key) {
    String ret = null;
    try {
      if (map.hasKey(key) && !map.isNull(key)) {
        switch (map.getType(key)) {
          case Boolean:
            ret = String.valueOf(map.getBoolean(key));
            break;
          case Number:
            final double d = map.getDouble(key);
            final long l = (long)d;
            if (d == l) {
              ret = String.valueOf(l);
            } else {
              ret = String.valueOf(d);
            }
            break;
          case String:
            ret = map.getString(key);
            break;
          default:
            break;
        }
      }
    } catch(final Exception e) {
      Log.e(TAG,"Convert failed to string for key: " + key + " on map:" + map,e);
    }
    if (ret != null && ret.length() == 0) {
      ret = null;
    }
    return ret;
  }
  private static Double _getMapValueAsDouble(final ReadableMap map,final String key) {
    Double ret = null;
    try {
      if (map.hasKey(key) && !map.isNull(key)) {
        switch (map.getType(key)) {
          case Boolean:
            if (map.getBoolean(key)) {
              ret = 1.0;
            } else {
              ret = 0.0;
            }
            break;
          case Number:
            ret = map.getDouble(key);
            break;
          case String:
            final String s = map.getString(key);
            if (s != null && s.length() > 0) {
              ret = Double.valueOf(s);
            }
            break;
          default:
            break;
        }
      }
    } catch(final Exception e) {
      Log.e(TAG,"Convert failed to double for key: " + key + " on map:" + map,e);
    }
    return ret;
  }
  private static Integer _getMapValueAsInt(final ReadableMap map,final String key) {
    Integer ret = null;
    try {
      if (map.hasKey(key) && !map.isNull(key)) {
        switch (map.getType(key)) {
          case Boolean:
            if (map.getBoolean(key)) {
              ret = 1;
            } else {
              ret = 0;
            }
            break;
          case Number:
            ret = (int)map.getDouble(key);
            break;
          case String:
            final String s = map.getString(key);
            if (s != null && s.length() > 0) {
              ret = Integer.valueOf(s);
            }
            break;
          default:
            break;
        }
      }
    } catch(final Exception e) {
      Log.e(TAG,"Convert failed to double for key: " + key + " on map:" + map,e);
    }
    return ret;
  }

  private static void _addRawString(final JSONObject obj,final ReadableMap props,final String key) {
    try {
      final String val = _getMapValueAsString(props,key);
      if (val != null) {
        obj.put(key,val);
      }
    } catch (final Exception e) {
      Log.e(TAG,"Failed to convert key: " + key + " props: " + props,e);
    }
  }
  private static void _addString(final JSONObject obj,final ReadableMap props,final String key,
                                 final int maxLen) {
    try {
      String val = _getMapValueAsString(props,key);
      if (val != null) {
        if (val.length() > maxLen) {
          val = val.substring(0,maxLen);
        }
        obj.put(key,val);
      }
    } catch (final Exception e) {
      Log.e(TAG,"Failed to convert key: " + key + " props: " + props,e);
    }
  }
  private static void _addNumber(final JSONObject obj,final ReadableMap props,final String key) {
    try {
      final Double val = _getMapValueAsDouble(props,key);
      if (val != null) {
        obj.put(key,val);
      }
    } catch (final Exception e) {
      Log.e(TAG,"Failed to convert key: " + key + " props: " + props,e);
    }
  }
  private static void _addInt(final JSONObject obj,final ReadableMap props,final String key) {
    try {
      final Integer val = _getMapValueAsInt(props,key);
      if (val != null) {
        obj.put(key,val);
      }
    } catch (final Exception e) {
      Log.e(TAG,"Failed to convert key: " + key + " props: " + props,e);
    }
  }

  private static JSONObject mapToObject(ReadableMap props) {
    final JSONObject obj = new JSONObject();
    _addRawString(obj,props,"event_datetime");

    _addString(obj,props,"kingdom",32);
    _addString(obj,props,"phylum",32);
    _addString(obj,props,"class",32);
    _addString(obj,props,"order",32);
    _addString(obj,props,"family",32);
    _addString(obj,props,"genus",32);
    _addString(obj,props,"species",32);

    _addString(obj,props,"online_status",16);

    _addNumber(obj,props,"float1");
    _addNumber(obj,props,"float2");
    _addNumber(obj,props,"float3");
    _addNumber(obj,props,"float4");

    _addNumber(obj,props,"spend_amount");
    _addString(obj,props,"spend_type",16);
    _addString(obj,props,"spend_currency",16);

    _addInt(obj,props,"event_index");

    return obj;
  }

  private void maybeAddInstall() {
    if (!mInstallSent) {
      try {
        final JSONObject props = new JSONObject();
        props.put("kingdom","organic");
        props.put("phylum","organic");
        props.put("class","organic");
        props.put("order","organic");
        props.put("family","organic");
        props.put("genus","organic");
        props.put("species","organic");
        addEvent("install",props);

        mInstallSent = true;
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_INSTALL_SENT,true);
        editor.commit();
      } catch(final JSONException e) {
        Log.e(TAG,"Error making install record",e);
      }
    }
  }
  private void maybeAddDAU() {
    final long now = System.currentTimeMillis();
    final long timeSinceDAU = now - mLastDAUSend;
    if (timeSinceDAU > 24*60*60*1000) {
      mLastDAUSend = now;

      addEvent("dau");
      final SharedPreferences.Editor editor = mPrefs.edit();
      editor.putLong(PREF_LAST_DAU_SEND,mLastDAUSend);
      editor.apply();
    }
  }

  private void addEvent(final String type) {
    addEvent(type,null);
  }
  private void addEvent(final String type,JSONObject props) {
    try {
      if (props == null) {
        props = new JSONObject();
      }
      props.put("type",type);
      if (!props.has("event_datetime")) {
        props.put("event_datetime",getISODateTime());
      }
      mEventList.add(props);

      saveEvents();
    } catch(final JSONException e) {
      Log.e(TAG,"addEvent: Failed to serialize object",e);
    }
    maybeAddDAU();
    maybeSendEvents();
  }
  private void saveEvents() {
    final JSONArray events = new JSONArray(mEventList);
    final String json = events.toString();
    final SharedPreferences.Editor editor = mPrefs.edit();
    editor.putString(PREF_EVENT_LIST,json);
    editor.apply();
  }

  private synchronized void maybeSendEvents() {
    if (!mRequestInFlight) {
      mRequestInFlight = true;
      sendEvents();
    }
  }

  private void sendEvents() {
    if (mEventList.size() > 0 && mApiKey != null && mOrgName != null && mBaseUrl != null) {
      final int sendCount = Math.min(10,mEventList.size());
      final JSONArray events = new JSONArray();
      for (int i = 0 ; i < sendCount ; ++i) {
        events.put(mEventList.get(i));
      }
      final String json = makeRequestJSON(events);

      startRequest(json, new RequestCallback() {
        @Override
        public void run(final int statusCode,final String response) {
          boolean clearItems = true;
          boolean delaySend = true;
          if (statusCode >= 200 && statusCode <= 299) {
            delaySend = false;
          } else if (statusCode == 400) {
            Log.e(TAG,"Bad request:" + response);
            delaySend = false;
          } else if (statusCode == 403) {
            // Bad request, clear items
            Log.e(TAG,"Authentication error, bad API key most likely:" + response);
          } else if (statusCode == 409) {
            // Conflict?!
            Log.i(TAG,"Conflict:" + response);
          } else if (statusCode >= 500) {
            clearItems = false;
          } else if (statusCode >= 300 && statusCode <= 399) {
            clearItems = false;
          } else {
            Log.i(TAG,"Unknown error:" + statusCode + " response: " + response);
            clearItems = false;
          }

          if (clearItems) {
            for (int i = 0 ; i < sendCount ; i++) {
              mEventList.remove(0);
            }
            saveEvents();
          }
          if (!delaySend) {
            mRequestInFlight = false;
            maybeSendEvents();
          } else {
            mRequestInFlight = false;
          }
        }
      });
    } else {
      mRequestInFlight = false;
    }
  }

  private String makeRequestJSON(final JSONArray events) {
    try {
      final JSONObject obj = new JSONObject();
      obj.put("api_key",mApiKey);
      obj.put("app_ver",mAppVer);
      obj.put("device_family",mDeviceFamily);
      obj.put("device_tag",mDeviceTag);
      obj.put("device_type",mDeviceType);
      obj.put("language",mLanguage);
      obj.put("country",mCountry);
      obj.put("os_ver",mOSVer);
      obj.put("os","android");
      obj.put("marketplace","google");
      if (mUserTag != null && mUserTag.length() > 0) {
        obj.put("user_tag",mUserTag);
      }

      obj.put("events",events);
      return obj.toString();
    } catch(final JSONException e) {
      Log.e(TAG,"makeRequestJSON: failed to make JSONObject",e);
    }
    return null;
  }

  private String getDeviceTag() {
    String deviceTag = mPrefs.getString(PREF_DEVICE_TAG,null);
    if (deviceTag == null) {
      final ContentResolver resolver = context.getContentResolver();
      final String androidID = Settings.Secure.getString(resolver,Settings.Secure.ANDROID_ID);
      if (androidID != null && androidID.length() >= 16) {
        deviceTag = androidID.toLowerCase();
      } else {
        deviceTag = UUID.randomUUID().toString().toLowerCase();
        Log.i(TAG,"Generated random UUID because this device has a bad ANDROID_ID: " + androidID);
      }
      final SharedPreferences.Editor editor = mPrefs.edit();
      editor.putString(PREF_DEVICE_TAG,deviceTag);
      editor.commit();
    }
    return deviceTag;
  }

  private String getDeviceType() {
    final String brand = Build.BRAND; //= "motorola"
    final String model = Build.MODEL; //= "XT1053"

    String deviceType = "";
    if (model.startsWith(brand)) {
      deviceType = model;
    } else {
      deviceType = brand + " " + model;
    }
    if (deviceType.length() > 16) {
      deviceType = deviceType.substring(0,16);
    }
    return deviceType;
  }
  private String getDeviceFamily() {
    String deviceFamily = "android";

    try {
      final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (tm != null && tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
        deviceFamily = "android_tablet";
      }
    } catch(final Exception e) {
      Log.e(TAG,"getDeviceFamily: get phone type failed",e);
    }
    return deviceFamily;
  }

  private String getCountry() {
    String country = Locale.getDefault().getCountry();

    try {
      final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
      final String simCountry = tm.getSimCountryIso();
      if (simCountry != null && simCountry.length() == 2) {
        country = simCountry;
      } else {
        final String networkCountry = tm.getNetworkCountryIso();
        if (networkCountry != null && networkCountry.length() == 2) {
          country = networkCountry;
        }
      }
    } catch(final Exception e) {
      Log.e(TAG,"getCountry: get network country failed",e);
    }

    return country.toUpperCase();
  }
  private String getISODateTime() {
    return mISOFormat.format(new Date());
  }

  private void startRequest(final String body,final RequestCallback callback) {
    final byte[] bodyBytes = body.getBytes(UTF8_CHARSET);

    final String url = mBaseUrl + "?current_time=" + getISODateTime();

    final StringRequest stringRequest = new StringRequest(Request.Method.POST,url,
            new Response.Listener<String>() {
              @Override
              public void onResponse(final String response) {
                callback.run(200,response);
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(final VolleyError error) {
                int statusCode = 0;
                String body = null;
                if (error != null && error.networkResponse != null) {
                  statusCode = error.networkResponse.statusCode;
                  if (error.networkResponse.data != null) {
                    body = new String(error.networkResponse.data,UTF8_CHARSET);
                  }
                }
                callback.run(statusCode,body);
              }
            })
    {
      @Override
      public String getBodyContentType() {
        return "application/json; charset=utf-8";
      }
      @Override
      public byte[] getBody() throws AuthFailureError {
        return bodyBytes;
      }
      /*
      @Override
      protected Response<String> parseNetworkResponse(final NetworkResponse response) {
        String responseString = "";
        if (response != null) {
          responseString = String.valueOf(response.statusCode);
          // can get more details such as response.headers
        }
        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
      }
      */
    };

    mQueue.add(stringRequest);
  }

  private static class RequestCallback {
    public void run(final int statusCode,final String response) {
    }
  }
}
