package com.example.connex.vipwatch;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.android.pushservice.PushMessageReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by hayton on 25/11/2015.
 */
public class NotificationReceiver extends PushMessageReceiver {

    @Override
    public void onBind(final Context context, int error, String appid, String userid, final String channelid, String requestid) {
        String responseString = "onBind errorCode= "+error+" appid= "+appid+" userid= "
                +userid+" channelid= "+channelid+" requestid= "+requestid;

        System.out.println("onbinded: "+channelid);

        //channelid refers to the channel which is used to send push to the specific mobile device
        SharedPreferences sharedPreferences = context.getSharedPreferences("users", Activity.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("channelid", channelid);
        if (editor.commit()){
            login(context);
        }
    }

    private void login(final Context context){
        System.out.println("start from zero");
        try {
            final SharedPreferences preferences = context.getSharedPreferences("users", Context.MODE_PRIVATE);
            System.out.println(" urlParams:> "+preferences.getString("urlParams", null));
            String url = "http://www.webvep.com/event/mobile/v2/events/"
                    + preferences.getString("urlParams", null) + "/vipAuthenticate";
            JSONObject login_json = new JSONObject();
            login_json.put("email", preferences.getString("loginEmail", null));
            login_json.put("password", preferences.getString("loginPassword", null));
            login_json.put("deviceType", 3);
            login_json.put("channelId", preferences.getString("channelid", null));
            System.out.println("app url:> "+url);
            System.out.println("app login_json:> "+login_json);
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url,
                    login_json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    System.out.println("app login response:> " + response.toString());
                    try {
                        if (response.getString("result").equals("authenticated")){
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("isLogin", true);
                            AppInfo.alertInterval = response.getInt("alert_interval");
                            editor.putString("urlGet", "sessionId="+response.getString("sessionId")+"&userId="
                                    +response.getString("userId")+"&userType="+response.getString("userType")+
                                    "&selectedVepId="+response.getString("selectedVepId"));
                            editor.putString("selected_VepId", response.getString("selectedVepId"));
                            editor.putString("username", response.getString("name"));
                            editor.putString("poll", response.getString("poll"));

                            if (editor.commit()) {
                                System.out.println("login email:> "+preferences.getString("loginEmail", null));
                                System.out.println("login password:> " + preferences.getString("loginPassword", null));
                                System.out.println("login state>: "+preferences.getBoolean("isLogin", true));
                                System.out.println("login urlGet:> "+preferences.getString("urlGet", null));
                                Intent intent = new Intent(context, WatchListActivity.class);
                                intent.putExtra("fromLogin", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                            }
                        }
                        else if (response.getString("result").equals("failed")){
                            LoginActivity.after_login.setVisibility(View.GONE);
                            Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (LoginActivity.after_login.getVisibility() == View.VISIBLE) {
                                LoginActivity.after_login.setVisibility(View.GONE);
                            }
                            Toast.makeText(context, R.string.login_unauthorized , Toast.LENGTH_SHORT).show();
                            if (preferences.getBoolean("isLogin", false)) {
                                VipAlert.logout(context);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    //LoginActivity.after_login.setVisibility(View.GONE);
                    VipAlert.volleyErrorHandle(context, error);

                }
            });
            //loginRequest.setShouldCache(false);
            VolleySingleton.getInstance(context).addToRequestQueue(loginRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onUnbind(Context context, int i, String s) {

    }

    @Override
    public void onSetTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onDelTags(Context context, int i, List<String> list, List<String> list1, String s) {

    }

    @Override
    public void onListTags(Context context, int i, List<String> list, String s) {

    }

    @Override
    public void onMessage(Context context, String message, String customContent) {
        SharedPreferences preferences = context.getSharedPreferences("users", Context.MODE_PRIVATE);

        String messageString = "received message= " + message + " customContent= "
                + customContent;
        System.out.println("app received msg:> " + messageString);
        System.out.println("app isLogin:> " + preferences.getBoolean("isLogin", false));

        if (preferences.getBoolean("isLogin", false)) {

            if (isJSONValid(message)) {
                try {
                    JSONObject receivedMsg = new JSONObject(message);
                    String title = (String) receivedMsg.get("title");
                    String description = (String) receivedMsg.get("description");
                    String visitorDetailId = String.valueOf(receivedMsg.getJSONObject("custom_content").getInt("enduser_id"));
                    String selected_VepId = String.valueOf(receivedMsg.getJSONObject("custom_content").getInt("selected_vep_id"));
                    SharedPreferences user = context.getSharedPreferences("users", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("visitorDetailId", visitorDetailId);
                    if (editor.commit()) {
                        System.out.println("app showed msg:> " + message);

                        VipAlert.notification(context, title, description, visitorDetailId, selected_VepId);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                VipAlert.notification(context, "VIP arrived!", message, message, "35");

            }
        }
    }

    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContent) {
        /** This callback function is not used as the Intent attachment remains unresolved.
         *  Notification feature is achieved in "onMessage".*/


    }

    @Override
    public void onNotificationArrived(Context context, String s, String s1, String s2) {

    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        }
        catch (JSONException ex) {
            try {
                new JSONArray(test);
            }
            catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private void notification(final Context context, final String title, final String description,
                              String visitorDetailId, String selected_VepId){
        //if (editor.commit()) {
            System.out.println("app visitordetailid received:> "+visitorDetailId);
            final long[] pattern = {0, 300, 150, 300}; // {wait, 1st vibrate, wait, 2nd vibrate}
            Intent intent = new Intent(context, activity_splash.class);
            intent.putExtra("fromPush", "1");
            intent.putExtra("visitorDetailId", visitorDetailId);
            intent.putExtra("selected_VepId", selected_VepId);
            intent.setAction(Long.toString(System.currentTimeMillis())); //must set this in order to make intent stay updated
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //}

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent,
                    PendingIntent.FLAG_ONE_SHOT);  //->changed from update_current to one_shot
            Uri defaultSoundUri;
            SharedPreferences sharedPreferences = context.getSharedPreferences("users", Context.MODE_PRIVATE);
            if (sharedPreferences.contains("currentRingtone")){
                defaultSoundUri = Uri.parse(sharedPreferences.getString("currentRingtone", null));
            }
            else{
                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                //.setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(description)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(pattern)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        //set global user
        SharedPreferences preferences = context.getSharedPreferences("users", Context.MODE_PRIVATE);
        final String url = "http://www.webvep.com/event/mobile/v2/events/"+preferences.getString("urlParams", null)+"/userInfo/"
                +visitorDetailId+"?"+preferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.has("headImg")) {
                            try {
                                AppInfo.visitorDetailHeadimgUrl = response.getString("headImg");
                                ImageRequest imageRequest = new ImageRequest(response.getString("headImg"), new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap response) {
                                        notificationBuilder.setLargeIcon(response);
                                        NotificationManager notificationManager =
                                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify((int) new Date().getTime(), notificationBuilder.build());
                                    }
                                }, 0, 0, null, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                });
                                VolleySingleton.getInstance(context).addToRequestQueue(imageRequest);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else AppInfo.visitorDetailHeadBitmap =
                                ((BitmapDrawable)context.getResources().getDrawable(R.drawable.head_image)).getBitmap();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VipAlert.volleyErrorHandle(context, error);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

        //}
    }

}
