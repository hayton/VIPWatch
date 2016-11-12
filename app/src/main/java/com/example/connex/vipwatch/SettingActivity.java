package com.example.connex.vipwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private Spinner sp_stopalert;
    private Integer alertTimeInterval;
    SharedPreferences preferences;
    private View loading;
    TimerTask timerTask;
    Timer notificationTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        super.onCreateDrawer(AppInfo.drawer_list, SettingActivity.this, false);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.action_settings);

        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        final List<Integer> stopfor = new ArrayList<>();
        for (int i=0;i<20;i++){
            stopfor.add(i+1);
        }
        sp_stopalert = (Spinner) findViewById(R.id.sp_stopalert);
        ArrayAdapter adapter = new ArrayAdapter(SettingActivity.this,
                android.R.layout.simple_spinner_item, stopfor);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_stopalert.setAdapter(adapter);
        sp_stopalert.setSelection(AppInfo.alertInterval - 1);

        sp_stopalert.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppInfo.alertInterval = stopfor.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

    }

    private void startGetNotification(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getNotification();

            }
        };

        if (notificationTimer == null) {
            notificationTimer = new Timer();
        }
        notificationTimer.schedule(timerTask, 0, 2000);
    }

    private void getNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        String url = "http://www.webvep.com/event/mobile/v2/events/" + sharedPreferences.getString("urlParams", null) + "/alert/get?"
                + sharedPreferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println("app setting:> "+response);
                                try {
                                    if (response.getJSONArray("data").length() > 0){
                                        for (int i=0;i<response.getJSONArray("data").length();i++){
                                            JSONObject notificationJson = response.getJSONArray("data").getJSONObject(i);
                                            VipAlert.notification(getBaseContext(), notificationJson.getString("title"),
                                                    notificationJson.getString("content"), String.valueOf(notificationJson.getString("watchlistEndUserId")),
                                                    String.valueOf(notificationJson.getString("selectedVepId")));
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VipAlert.volleyErrorHandle(SettingActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(SettingActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onPause(){
        super.onPause();
        //notificationTimer.cancel();
        //notificationTimer = null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Check which request we're responding to
        if (requestCode == 999) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("currentRingtone", uri.toString());
                editor.commit();
                /**if (editor.commit()){
                    String ringTonePath = uri.toString();
                    RingtoneManager.setActualDefaultRingtoneUri(
                            SettingActivity.this,
                            RingtoneManager.TYPE_NOTIFICATION,
                            uri);
                    //loading.setVisibility(View.GONE);
                }*/
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_savestopalert:
                loading.setVisibility(View.VISIBLE);
                loading.bringToFront();
                preferences = getSharedPreferences("users", MODE_PRIVATE);
                String url = "http://www.webvep.com/event/mobile/v2/events/"+ preferences.getString("urlParams", null) +"/setting/update?"
                        +preferences.getString("urlGet", null);
                JSONObject params = new JSONObject();
                try {
                    params.put("alertTimeInterval", AppInfo.alertInterval);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                loading.setVisibility(View.GONE);
                                try {
                                    if (response.getString("result").equals("succeed")){
                                        Toast.makeText(SettingActivity.this,
                                                R.string.toast_setting_success, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                loading.setVisibility(View.GONE);
                                VipAlert.volleyErrorHandle(SettingActivity.this, error);
                            }
                        });
                jsonObjectRequest.setShouldCache(false);
                VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
                break;

            case R.id.btn_exportvisitor:
                loading.setVisibility(View.VISIBLE);
                loading.bringToFront();
                preferences = getSharedPreferences("users", MODE_PRIVATE);
                url = "http://www.webvep.com/event/mobile/v2/events/"+ preferences.getString("urlParams", null) +"/exportVIPInfo?"
                        +preferences.getString("urlGet", null);
                JsonObjectRequest jsonObjectRequestExport = new JsonObjectRequest(Request.Method.POST, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                loading.setVisibility(View.GONE);
                                try {
                                    System.out.println("app export reply;> "+response);
                                    if (response.getString("result").equals("succeed")){
                                        Toast.makeText(SettingActivity.this,
                                                R.string.toast_csv_exported, Toast.LENGTH_SHORT).show();
                                    }
                                    else if (response.getString("result").equals("failed")){
                                        Toast.makeText(SettingActivity.this,
                                                R.string.toast_csv_failed, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                loading.setVisibility(View.GONE);
                                VipAlert.volleyErrorHandle(SettingActivity.this, error);
                            }
                        });
                jsonObjectRequestExport.setShouldCache(false);
                VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequestExport);
                break;

            case R.id.btn_setringtone:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
                if (sharedPreferences.contains("currentRingtone")) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sharedPreferences.getString("currentRingtone", null)));
                }
                else{
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                }
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(intent, 999);
        }

    }

    /**@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
