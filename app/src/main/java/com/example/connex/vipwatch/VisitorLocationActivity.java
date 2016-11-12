package com.example.connex.vipwatch;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
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
import com.example.connex.adapter.ExpandableListAdapter;
import com.example.connex.adapter.LocationList_visitor_adapter;
import com.example.connex.adapter.LocationPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hayton on 5/5/2016.
 */
public class VisitorLocationActivity extends BaseActivity implements ActionBar.TabListener{

    ViewPager locationPager;
    LocationPagerAdapter pagerAdapter;
    //private String[] header = {getResources().getString(R.string.locationpager_area),
           // getResources().getString(R.string.locationpager_visitor)};
    private TabLayout tabLayout;
    private ArrayList<String> listParent = new ArrayList<>();
    private HashMap<String, List<String[]>> listChild = new HashMap<>();
    private ArrayList<String> userArray = new ArrayList<>();
    private ArrayList<String> boothname = new ArrayList<>();
    private View loading;
    Timer notificationTimer = new Timer();
    Timer locationTimer = new Timer();
    TimerTask locationTimerTask;
    TimerTask notificationTimertask;
    ArrayList<Timer> timerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitorlocation);
        super.onCreateDrawer(AppInfo.drawer_list, VisitorLocationActivity.this, false);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_visitorlocation);

        locationPager = (ViewPager) findViewById(R.id.pager_location);
        loading = findViewById(R.id.loading);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.locationpager_area));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.locationpager_visitor));

        startGetLocation();
        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

        getData(false);

        /*locationPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //System.out.println("changed");
                getData(true);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

    }

    private void getData(final Boolean isRefresh){
        SharedPreferences preferences = getSharedPreferences("users", Context.MODE_PRIVATE);
        final String url = "http://www.webvep.com/event/mobile/v2/events/"+ preferences.getString("urlParams", null) +"/boothTrackingList?"
                +preferences.getString("urlGet", null);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loading.setVisibility(View.GONE);
                        listChild.clear();
                        listParent.clear();
                        userArray.clear();
                        boothname.clear();
                        System.out.println("app response:> " + response.toString());
                        try {
                            for (int i=0;i<response.getJSONArray("data").length();i++){
                                listParent.add(response.getJSONArray("data").getJSONObject(i).getString("name"));
                                List<String[]> temp_child = new ArrayList<>();
                                for (int j=0;j<response.getJSONArray("data").getJSONObject(i).
                                        getJSONArray("users").length();j++){
                                    //new string[]{name, watchlistname, visitorid, title, headimg link}
                                    temp_child.add(new String[]{
                                            response.getJSONArray("data").getJSONObject(i).
                                                    getJSONArray("users").getJSONObject(j).getString("name"),
                                            response.getJSONArray("data").getJSONObject(i).getJSONArray("users").
                                                    getJSONObject(j).getString("watchListName"),
                                            response.getJSONArray("data").getJSONObject(i).getJSONArray("users").
                                                    getJSONObject(j).getString("id"),
                                            response.getJSONArray("data").getJSONObject(i).getJSONArray("users").
                                                    getJSONObject(j).getString("title"),
                                            response.getJSONArray("data").getJSONObject(i).getJSONArray("users").
                                                    getJSONObject(j).getString("headImg")});
                                    userArray.add(response.getJSONArray("data").getJSONObject(i).
                                            getJSONArray("users").getJSONObject(j).toString());
                                    boothname.add(response.getJSONArray("data").getJSONObject(i).getString("name"));

                                    System.out.println("userarray :>" + userArray);
                                }
                                listChild.put(listParent.get(i), temp_child);
                                System.out.println("listchild :> "+listChild);
                            }
                            if (!isRefresh) {
                                //initView();
                                String[] header = {getResources().getString(R.string.locationpager_area),
                                        getResources().getString(R.string.locationpager_visitor)};
                                pagerAdapter = new LocationPagerAdapter(VisitorLocationActivity.this, getSupportFragmentManager(),
                                        header, listParent, listChild, userArray, boothname);
                                locationPager.setAdapter(pagerAdapter);
                                tabLayout.setupWithViewPager(locationPager);
                            }
                            else{
                                //pagerAdapter.notifyDataSetChanged();
                                LocationBaseFragment.updateLocation1ListView();
                                VisitorBaseFragment.updateVisitor1ListView();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        VipAlert.volleyErrorHandle(VisitorLocationActivity.this, error);

                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(VisitorLocationActivity.this).addToRequestQueue(jsonObjectRequest);
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
                                System.out.println("visitorlocation:> "+response);
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
                                VipAlert.volleyErrorHandle(VisitorLocationActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(VisitorLocationActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void startGetNotification(){
        notificationTimertask = new TimerTask() {
            @Override
            public void run() {
                getNotification();

            }
        };

        if (notificationTimer == null) {
            notificationTimer = new Timer();
        }
        timerList.add(notificationTimer);
        notificationTimer.schedule(notificationTimertask, 0, 2000);
    }

    private void startGetLocation(){
        locationTimerTask = new TimerTask() {
            public void run() {
                getData(true);
            }
        };

        if (locationTimer == null) {
            locationTimer = new Timer();
        }
        timerList.add(locationTimer);
        locationTimer.schedule(locationTimerTask, 0, 3000);
    }

    @Override
    protected void onPause(){
        super.onPause();
        //notificationTimer.cancel();
        //locationTimer.cancel();

        //notificationTimer = null;

        for (int i=0;i<timerList.size();i++){
            timerList.get(i).cancel();
        }
        locationTimer = null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        //startGetLocation();
        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_visitorlocation, menu);
    return true;
    }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     // Handle action bar item clicks here. The action bar will
     // automatically handle clicks on the Home/Up button, so long
     // as you specify a parent activity in AndroidManifest.xml.
     int id = item.getItemId();

     //noinspection SimplifiableIfStatement
     if (id == R.id.action_refresh) {
         loading.setVisibility(View.VISIBLE);
         loading.bringToFront();
         getData(true);
         return true;
     }

     return super.onOptionsItemSelected(item);
     }*/

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        locationPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}
