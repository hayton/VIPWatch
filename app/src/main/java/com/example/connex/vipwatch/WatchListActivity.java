package com.example.connex.vipwatch;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.connex.adapter.WatchListAdapter;
import com.example.connex.adapter.WatchListPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hayton on 17/12/2015.
 */
public class WatchListActivity extends BaseActivity {

    private ArrayList<JSONObject> viplist = new ArrayList<>();
    private ArrayList<HashMap> drawer_list = new ArrayList<>();

    private WatchListAdapter watchListAdapter;
    private Drawable vip_photo;
    public static ViewPager pager;
    private ArrayList filter = new ArrayList();
    private TabLayout tabLayout;
    WatchListPagerAdapter pagerAdapter;
    private View loading;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Timer timer = new Timer();
    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


            Intent serviceintent = new Intent(WatchListActivity.this, NotificationService.class);
            //startService(serviceintent);

        SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")){
            startTimer();
        }

        //if user login because of receiving push message, go to VisitorDetailActivity directly
        if (getIntent().hasExtra("fromPush")){
            System.out.println("app from push");

            final Intent intent = new Intent(WatchListActivity.this, VisitorDetailActivity.class);
            //startActivity(intent);

            intent.putExtra("fromPush", "1");
            System.out.println("app at watchlist visitor id oncreate:> "+getIntent().getStringExtra("visitorDetailId"));
            String visitorDetailId = getIntent().getStringExtra("visitorDetailId");

            //set global user
            SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
            final String url = "http://www.webvep.com/event/mobile/v2/events/"+preferences.getString("urlParams", null)+"/userInfo/"
                    +visitorDetailId+"?"+preferences.getString("urlGet", null);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("app detail url:> " + url);
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

                            startActivity(intent);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VipAlert.volleyErrorHandle(WatchListActivity.this, error);
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }

        setContentView(R.layout.activity_watchlist);

        loading = findViewById(R.id.loading);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_watchlist);

        preferences = getSharedPreferences("users", MODE_PRIVATE);
        editor = preferences.edit();
        //if (!VipWatch.isActivityVisible()) {
            final String url = "http://www.webvep.com/event/mobile/v2/events/" + preferences.getString("urlParams", null) + "/watchlist/get?"
                    + preferences.getString("urlGet", null);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("app url:> " + url);
                            System.out.println("app watchlist:> " + response.toString());
                            loading.setVisibility(View.GONE);
                            try {
                                filter.clear();
                                drawer_list.clear();
                                AppInfo.watchlistUser = response.getJSONArray("data");
                                for (int i = 0; i < response.getJSONArray("data").length(); i++) {
                                    filter.add(response.getJSONArray("data").getJSONObject(i).getString("name"));
                                    for (int j = 0; j < response.getJSONArray("data").getJSONObject(i).getJSONArray("users").length(); j++) {
                                        AppInfo.visitorNameMapJson.put(response.getJSONArray("data").getJSONObject(i).getJSONArray("users").getJSONObject(j).getString("name"),
                                                response.getJSONArray("data").getJSONObject(i).getJSONArray("users").getJSONObject(j));

                                        //save the json into the watchlistUser sharedpreferences ("vipname", vipJson)
                                        editor.putString(response.getJSONArray("data").getJSONObject(i).getJSONArray("users").getJSONObject(j).getString("name"),
                                                response.getJSONArray("data").getJSONObject(i).getJSONArray("users").getJSONObject(j).toString());
                                        editor.commit();
                                    }
                                }
                                filter.add(0, "All");
                                AppInfo.filter = filter;
                                String[] head = {"Watch List", "Visitor Location", "Alert Center", "Setting", "Logout"};
                                drawer_list.addAll(genHead(head));
                                drawer_list.addAll(1, genFilter(filter));
                                AppInfo.drawer_list = drawer_list;

                                initView(false, getIntent());//false => app has no problem connecting the server

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            drawer_list.clear();
                            String[] head = {"Watch List", "Visitor Location", "Alert Center", "Setting", "Logout"};
                            drawer_list.addAll(genHead(head));
                            AppInfo.drawer_list = drawer_list;
                            initView(true, getIntent());//true => app has some problem connecting the server

                            VipAlert.volleyErrorHandle(WatchListActivity.this, error);
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        //}


        if (getIntent().hasExtra("fromLogin")) {
            Toast.makeText(WatchListActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
            getIntent().removeExtra("fromLogin");
        }

        if (getIntent().hasExtra("fragmentNum")){
            Integer fragmentNum = getIntent().getExtras().getInt("fragmentNum");
            System.out.println("app fragmentNum oncreate:> " + fragmentNum);
            pager.setCurrentItem(fragmentNum);
        }


    }

    @Override
    protected void onResume(){
        super.onResume();
        VipAlert.activityResumed();
       /* SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startTimer();
        }*/
        System.out.println("watchlist visitor id onresume:> " + getIntent().getStringExtra("visitorDetailId"));
    }

    @Override
    protected void onPause(){
        super.onPause();
        //timer.cancel();
        //timer.purge();
        //timer = null;
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);

    }

    private void startTimer(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getNotification();

            }
        };
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(timerTask, 0, 2000);
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
                            System.out.println("app notification watchlist:> "+response);
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
                                VipAlert.volleyErrorHandle(WatchListActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(WatchListActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    //if app cannot connect to server => isError=true, else => isError=false
    private void initView(boolean isError, Intent intent){
        super.onCreateDrawer(AppInfo.drawer_list, WatchListActivity.this, isError);
        //swipe view (ViewPager) starts
        if (!isError) {
            pagerAdapter = new WatchListPagerAdapter(WatchListActivity.this, getSupportFragmentManager()
                    , watchListAdapter, getResources().getDrawable(R.drawable.head_image), AppInfo.watchlistUser, AppInfo.filter);

            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(pagerAdapter);
            //add tab to the tablayout
            for (int i = 0; i < AppInfo.filter.size(); i++) {
                tabLayout.addTab(
                        tabLayout.newTab()
                                .setText(AppInfo.filter.get(i).toString())
                );
            }
            tabLayout.setupWithViewPager(pager);
            //swipe view (ViewPager) ends
            //loading.setVisibility(View.GONE);
        }

        if (intent.hasExtra("fragmentNum")){
            System.out.println("app fragmentNum iniview:> " + intent.getExtras().getInt("fragmentNum"));
            pager.setCurrentItem(intent.getExtras().getInt("fragmentNum"));
        }
    }
    
    private ArrayList<HashMap> genHead (String[] item){
        ArrayList<HashMap> head = new ArrayList<>();
        for (String name:item) {
            HashMap<String, String> data = new HashMap<>();
            data.put("name", name);
            data.put("type", "head");
            head.add(data);
        }
        return head;
    }

    private ArrayList<HashMap> genFilter (ArrayList item){
        ArrayList<HashMap> filter = new ArrayList<>();
        for (int i=0;i<item.size();i++){
            HashMap<String, String> data = new HashMap<>();
            data.put("name", item.get(i).toString());
            data.put("type", "filter");
            filter.add(data);
        }
        return filter;
    }

}
