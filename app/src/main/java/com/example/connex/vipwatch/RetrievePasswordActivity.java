package com.example.connex.vipwatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hayton on 5/5/2016.
 */
public class RetrievePasswordActivity extends AppCompatActivity implements View.OnClickListener{

    TimerTask timerTask;
    Timer notificationTimer = new Timer();

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_retrivepassword);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        getSupportActionBar().setTitle("Forget password");

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
                                System.out.println("retrievepassword:> "+response);
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
                                VipAlert.volleyErrorHandle(RetrievePasswordActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(RetrievePasswordActivity.this).addToRequestQueue(jsonObjectRequest);
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
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.homeAsUp:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_retrieve:
                //TODO: send retrieve to server
        }
    }
}
