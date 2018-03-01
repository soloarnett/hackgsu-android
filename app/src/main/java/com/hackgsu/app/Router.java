package com.hackgsu.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by soloarnett on 2/23/18.
 */

public class Router extends Application{
    // Called when the application is starting, before any other application objects have been created.
    // Overriding this method is totally optional!
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("initial", "initialization");


        mContext = getApplicationContext();
        preferences = mContext.getSharedPreferences("shared", MODE_PRIVATE);
        editor = preferences.edit();
//        preferences = this.getSharedPreferences("shared", MODE_PRIVATE);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenResult result) {
                        JSONObject additionalData;

                        try{
                            Log.d("notification opened", "" + result.notification.payload.toString());
                            additionalData = result.notification.payload.additionalData;
                        }catch(Exception e){
                            Log.d("additional data", "failed: " + e);
                        }
                    }
                })
                .setNotificationReceivedHandler(new OneSignal.NotificationReceivedHandler() {
                    @Override
                    public void notificationReceived(OSNotification notification) {
                        Log.d("notification received", "" + notification.toString());
                    }
                })
                .init();
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void setPushUID(Context context, final String pushUID){
        preferences = context.getSharedPreferences("shared", MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("pushUID", pushUID);
        editor.commit();
        Log.d("pushuid", "push: " + pushUID);
        String url = "https://aeb4oc6uwg.execute-api.us-east-1.amazonaws.com/prod/createuser";
        JSONObject params = new JSONObject();
        try{
            params.put("pushUID", pushUID);
            params.put("OS", "Android");
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("response", "reached: " + response.toString());
                    try{
                         createUserResponseHandler(pushUID, response.getInt("statusCode"));
                    }catch (Exception e){

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("err", "volley error: " + error.toString());
                }
            });
            NetworkSingleton.getInstance(this).addToRequestQueue(request);
        }catch(Exception e){
            Log.d("create err", "message: "+ e);
        }
    }

    public void createUserResponseHandler(String pushUID, int statusCode){
        Log.d("userresponse", ""+statusCode);
        preferences = mContext.getSharedPreferences("shared", MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString("status", "something");
        editor.commit();
        Log.d("pref", preferences.getString("status", "nothing"));
        String message;
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray player_ids = new JSONArray();
            player_ids.put(pushUID);
            jsonObject.put("include_player_ids", player_ids);
            jsonObject.put("android_channel_id", "8a44aae2-1056-46b6-b488-3d3b9006ccc1");
            jsonObject.put("priority", 10);
//            Log.d("status", statusCode + "");
            switch (statusCode){
                case 200:
                    message = "Welcome to HackGSU! Are you ready to get hacking!?";
                    jsonObject.put("contents", new JSONObject().put("en", message));
                    OneSignal.postNotification(jsonObject, null);
                    break;
                case 201:
                    break;
                case 204:
                    message = "Welcome back!";
                    jsonObject.put("contents", new JSONObject().put("en", message));
//                    OneSignal.postNotification(jsonObject, null);
                    break;
                default:
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getPushUID(Context context){
//        return preferences.getString("pushUID", "");
            preferences = context.getSharedPreferences("shared", MODE_PRIVATE);
//        Log.d("context", context.toString());
//        SharedPreferences preferences = getApplicationContext().getSharedPreferences("shared", MODE_PRIVATE);
//        prefs = this.getSharedPreferences("shared", MODE_PRIVATE);
//        editor = preferences.edit();
//        i\w
//        preferences.edit();
//        prefs.getString("pushUID", null);
//        preferences.getString("pushUID", "");
        return preferences.getString("pushUID", null);
    }


}
