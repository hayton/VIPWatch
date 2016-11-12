package com.example.connex.vipwatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.example.connex.adapter.AlertListAdapter;

/**
 * Created by hayton on 22/1/2016.
 */
public class AlertCenterActivity extends BaseActivity {

    private SwipeRefreshLayout refreshLayout;
    private ListView lv_alertHistory;
    private com.example.connex.adapter.AlertListAdapter alertListAdapter;
    private TabLayout tabLayout;
    private ArrayList<JSONObject> alertHistory = new ArrayList<>();
    private ArrayList<String> time_comapre = new ArrayList<>();
    private View loading;
    TimerTask timerTask;
    Timer notificationTimer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_center);
        super.onCreateDrawer(AppInfo.drawer_list, AlertCenterActivity.this, false);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alert Center");

        loading = findViewById(R.id.loading);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        lv_alertHistory = (ListView) findViewById(R.id.app_listview_norefresh);

        final View instructions = findViewById(R.id.instructions);
        instructions.bringToFront();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //final SharedPreferences.Editor editor = preferences.edit();
        Boolean isFirstTime = preferences.getBoolean("isFirstTime", true);
        System.out.println("app isfirsttime:> "+isFirstTime);
        if (isFirstTime) {
            instructions.setVisibility(View.VISIBLE);
            instructions.bringToFront();
            instructions.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    System.out.println("app touched");
                    instructions.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isFirstTime", false);
                    editor.commit();
                    return false;
                }
            });
        }
        else{
            instructions.setVisibility(View.GONE);
        }

        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

        getAlertList(loading);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAlertList(loading);
            }
        });

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
                                System.out.println("app alertcenter:> "+response);
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
                                VipAlert.volleyErrorHandle(AlertCenterActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(AlertCenterActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void deleteAlertList(final View loading){
        loading.setVisibility(View.VISIBLE);
        loading.bringToFront();
        final SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        String url = "http://www.webvep.com/event/mobile/v2/events/" + sharedPreferences.getString("urlParams", null) + "/alertlist/delete?"
                + sharedPreferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("result").equals("succeed")){
                                //loading.setVisibility(View.GONE);
                                getAlertList(loading);
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
                        VipAlert.volleyErrorHandle(AlertCenterActivity.this, error);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getAlertList(final View loading) {
        loading.setVisibility(View.VISIBLE);
        loading.bringToFront();
        final SharedPreferences sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);
        String url = "http://www.webvep.com/event/mobile/v2/events/" + sharedPreferences.getString("urlParams", null) + "/alertlist/get?"
                + sharedPreferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            System.out.println("app alertlist:> " + response.getString("data"));
                            alertHistory.clear();
                            //time_comapre.clear();
                            for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                alertHistory.add(0, response.getJSONArray("data").getJSONObject(i));
                                time_comapre.add(response.getJSONArray("data").getJSONObject(i).getString("sendTime"));
                            }
                            //Collections.sort(time_comapre);
                            //System.out.println("app sorted time:> " + time_comapre);
                            alertListAdapter = new AlertListAdapter(alertHistory, AlertCenterActivity.this);
                            lv_alertHistory.setAdapter(alertListAdapter);
                            alertListAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                            loading.setVisibility(View.GONE);

                            lv_alertHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    loading.setVisibility(View.VISIBLE);
                                    loading.bringToFront();
                                    Intent intent = new Intent(AlertCenterActivity.this, VisitorDetailActivity.class);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    try {
                                        editor.putString("visitorDetailId", alertHistory.get(position).getString("id"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if (editor.commit()) {
                                        setGlobalUser(sharedPreferences, intent, loading);
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        VipAlert.volleyErrorHandle(AlertCenterActivity.this, error);
                        loading.setVisibility(View.GONE);
                    }
                });

        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void setGlobalUser(SharedPreferences preferences, final Intent intent, final View loading){
        final String url = "http://www.webvep.com/event/mobile/v2/events/"+preferences.getString("urlParams", null)+"/userInfo/"
                +preferences.getString("visitorDetailId", null)+"?"+preferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("app detail url:> "+url);
                        AppInfo.visitorDetail = response;
                        System.out.println("app visitor detail:> " + response);
                        if (response.has("headImg")) {
                            try {
                                AppInfo.visitorDetailHeadimgUrl = response.getString("headImg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else AppInfo.visitorDetailHeadBitmap =
                                ((BitmapDrawable)getResources().getDrawable(R.drawable.head_image)).getBitmap();
                        loading.setVisibility(View.GONE);

                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(AlertCenterActivity.this,
                                    "Cannot connect to server: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(AlertCenterActivity.this,
                                    "Cannot authenticate with server: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(AlertCenterActivity.this,
                                    "Server error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(AlertCenterActivity.this,
                                    "Network error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(AlertCenterActivity.this,
                                    "Parse error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alertcenter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.homeAsUp:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.clear:
                deleteAlertList(loading);
        }
        return super.onOptionsItemSelected(item);
    }
}
