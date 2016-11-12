package com.example.connex.vipwatch;

import com.example.connex.vipwatch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by hayton on 17/6/2016.
 */
public class VipAlert extends Application {

    public static Activity runningActivity;

    public static void setRunningActivity(Activity activity) {runningActivity = activity;}

    public static Activity getRunningActivity() {return runningActivity;}

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;

    public static void volleyErrorHandle(final Context context, VolleyError error){
        if (error.networkResponse != null) {
            if (String.valueOf(error.networkResponse.statusCode).equals("401")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.alert_title)
                        .setMessage(R.string.alert_message)
                        .setPositiveButton(R.string.alert_save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .show();
            }

            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_no_connection),
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_no_authenticate) + error.networkResponse.statusCode,
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_error) + error.networkResponse.statusCode,
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_network_error) + error.networkResponse.statusCode,
                        Toast.LENGTH_LONG).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(context,
                        context.getResources().getString(R.string.server_parse_error) + error.networkResponse.statusCode,
                        Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(context,
                    context.getResources().getString(R.string.server_no_connection),
                    Toast.LENGTH_LONG).show();
        }

    }

    public static void logout(Context context){
        Intent intent = new Intent(context, activity_splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        SharedPreferences preferences = context.getSharedPreferences("users", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();

        for (Activity activity : AppInfo.activityList) {
            activity.finish();
        }

        if (editor.commit()) {
            System.out.println("logouted email:> "+preferences.getString("loginEmail", null));
            System.out.println("logouted password:> "+preferences.getString("loginPassword", null));
            System.out.println("login state after logout>: "+preferences.getBoolean("isLogin", false));
            System.out.println("logouted urlGet:> "+preferences.getString("urlGet", null));
            context.stopService(new Intent(context, NotificationService.class));
            context.startActivity(intent);
        }
    }

    public static void notification(final Context context, final String title, final String description,
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


