package com.example.connex.vipwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hayton on 6/6/2016.
 */
public class activity_splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();

        System.out.println("login state:> " + preferences.getBoolean("isLogin", false));

        if (getIntent().hasExtra("fromPush") || preferences.getBoolean("isLogin", false)) {
            gotoWatchlistActivity(preferences, editor, getIntent());
        }
        else if (!preferences.getBoolean("isLogin", false)) {
            //get event list from server and put the list into AppInfo.eventlist
            getEventList(getIntent());
        }

    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        //setIntent(intent);
        if (intent.hasExtra("fromPush")){
            SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            gotoWatchlistActivity(preferences, editor, intent);
        }
    }

    private void getEventList(final Intent receivedIntent){
        final SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
        String url = "http://www.webvep.com/event/report/eventList";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("app response:> " + response.toString());
                        try {
                            AppInfo.loginParams = response.getJSONArray("data");
                            AppInfo.eventlist.clear();
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("eventlistdata", response.getJSONArray("data").toString());
                            if (editor.commit()) {
                                for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                    AppInfo.eventlist.add(response.getJSONArray("data").getJSONObject(i).getString("name"));
                                    AppInfo.vepIdMapJsonPosition
                                            .put(String.valueOf(response.getJSONArray("data").getJSONObject(i).getInt("id")), i);
                                }
                                Intent intent = new Intent(activity_splash.this, LoginActivity.class);
                                if (receivedIntent.hasExtra("fromPush")) {
                                    intent.putExtra("fromPush", "1");
                                    intent.putExtra("visitorDetailId", receivedIntent.getStringExtra("visitorDetailId"));
                                    intent.putExtra("selected_VepId", receivedIntent.getStringExtra("selected_VepId"));
                                }
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VipAlert.volleyErrorHandle(activity_splash.this, error);

                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void gotoWatchlistActivity(final SharedPreferences preferences, final SharedPreferences.Editor editor, final Intent receivedIntent){
        if (receivedIntent.hasExtra("fromPush")){
            if (!receivedIntent.getStringExtra("selected_VepId").equals(preferences.getString("selected_VepId", null))){
                getEventList(receivedIntent);
            }
            else {
                try {
                    System.out.println(" passed id:> " + receivedIntent.getStringExtra("visitorDetailId"));
                    String url = "http://www.webvep.com/event/mobile/v2/events/" + preferences.getString("urlParams", null) + "/vipAuthenticate";
                    JSONObject login_json = new JSONObject();
                    login_json.put("email", preferences.getString("loginEmail", null));
                    login_json.put("password", preferences.getString("loginPassword", null));
                    login_json.put("deviceType", 3);
                    login_json.put("channelId", preferences.getString("channelid", null));
                    System.out.println("app url:> " + url);
                    System.out.println("app login_json:> " + login_json);
                    JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url,
                            login_json, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("app response:> " + response.toString());
                            try {
                                if (response.getString("result").equals("authenticated")) {
                                    AppInfo.alertInterval = response.getInt("alert_interval");
                                    editor.putString("urlGet", "sessionId=" + response.getString("sessionId") + "&userId="
                                            + response.getString("userId") + "&userType=" + response.getString("userType") +
                                            "&selectedVepId=" + response.getString("selectedVepId"));
                                    if (editor.commit()) {
                                        Intent intent = new Intent(activity_splash.this, WatchListActivity.class);

                                        System.out.println("isLogin from splash:> " + preferences.getBoolean("isLogin", false));
                                        if (receivedIntent.hasExtra("fromPush")) {
                                            System.out.println("fromPush from splash");
                                            intent.putExtra("fromPush", "1");
                                            intent.putExtra("visitorDetailId", receivedIntent.getStringExtra("visitorDetailId"));
                                        }
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        activity_splash.this.startActivity(intent);

                                        finish();
                                    }
                                } else if (response.getString("result").equals("failed")) { //means session expires
                                    PushManager.startWork(activity_splash.this, PushConstants.LOGIN_TYPE_API_KEY, AppInfo.api_key);
                                } else {
                                    Toast.makeText(activity_splash.this, R.string.login_unauthorized,
                                            Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //volleyErrorHandle(activity_splash.this, error);
                            VipAlert.volleyErrorHandle(activity_splash.this, error);

                        }
                    });
                    loginRequest.setShouldCache(false);
                    VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            PushManager.startWork(activity_splash.this, PushConstants.LOGIN_TYPE_API_KEY, AppInfo.api_key);
        }
    }

    private void volleyErrorHandle(Context context, VolleyError error){
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            Toast.makeText(context,
                    R.string.server_no_connection,
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof AuthFailureError) {
            Toast.makeText(context,
                    R.string.server_no_authenticate + error.networkResponse.statusCode,
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof ServerError) {
            Toast.makeText(context,
                    R.string.server_error + error.networkResponse.statusCode,
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof NetworkError) {
            Toast.makeText(context,
                    R.string.server_network_error + error.networkResponse.statusCode,
                    Toast.LENGTH_LONG).show();
        } else if (error instanceof ParseError) {
            Toast.makeText(context,
                    R.string.server_parse_error + error.networkResponse.statusCode,
                    Toast.LENGTH_LONG).show();
        }
    }
}
