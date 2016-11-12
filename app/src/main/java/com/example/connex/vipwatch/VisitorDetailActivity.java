package com.example.connex.vipwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by hayton on 6/5/2016.
 */
public class VisitorDetailActivity extends AppCompatActivity {

    private LinearLayout email_layout;
    private LinearLayout phone_layout;
    private ImageView iv_headimg;
    private TabLayout tabLayout;
    private EditText et_notes, et_followup;
    private TextView tv_notes, tv_followup;
    private Boolean isEditing, isEdited;
    private Toolbar toolbar;
    private View loading;
    private String temp_notes, temp_followup;

    TimerTask timerTask;
    Timer notificationTimer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceStates){
        super.onCreate(savedInstanceStates);
        setContentView(R.layout.activity_visitordetail);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setVisibility(View.GONE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);

        temp_notes = temp_followup = "";

        //put info in extra_info layout
        //phone_layout = (LinearLayout) findViewById(R.id.phone);
        //email_layout = (LinearLayout) findViewById(R.id.email);
        iv_headimg = (ImageView) findViewById(R.id.iv_headimg);
        et_followup = (EditText) findViewById(R.id.et_followup);
        et_notes = (EditText) findViewById(R.id.et_notes);
        loading = findViewById(R.id.loading);

        loading.setVisibility(View.GONE);
        et_followup.setVisibility(View.GONE);
        et_notes.setVisibility(View.GONE);

        isEditing = isEdited = false;

        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/

        getDetails();

    }

    private void getDetails(){
        //phone_layout.removeAllViews();
        //email_layout.removeAllViews();
        getHeadImg(iv_headimg);

        try {
            getSupportActionBar().setTitle(AppInfo.visitorDetail.getString("name"));

            iv_headimg.setImageBitmap(AppInfo.visitorDetailHeadBitmap);

            TextView tv_name = (TextView) findViewById(R.id.tv_name);
            tv_name.setText(AppInfo.visitorDetail.getString("name"));

            TextView tv_title = (TextView) findViewById(R.id.tv_title);
            tv_title.setText(AppInfo.visitorDetail.getString("title"));

            TextView tv_company = (TextView) findViewById(R.id.tv_company);
            tv_company.setText(AppInfo.visitorDetail.getString("company"));

            //View phone_view = getLayoutInflater().inflate(R.layout.visitordetail_extra_info, null);
            LinearLayout layout_phone = (LinearLayout) findViewById(R.id.layout_phone);
            if (AppInfo.visitorDetail.getString("mobile").equals("")){
                layout_phone.setVisibility(View.GONE);
            }
            else {
                TextView tv_info_phone = (TextView) findViewById(R.id.tv_info_phone);
                tv_info_phone.setText(AppInfo.visitorDetail.getString("mobile"));
            }
            //phone_layout.addView(phone_view);

            //View email_view = getLayoutInflater().inflate(R.layout.visitordetail_extra_info, null);
            TextView tv_info_email = (TextView) findViewById(R.id.tv_info_email);
            tv_info_email.setText(AppInfo.visitorDetail.getString("email"));
            //email_layout.addView(email_view);

            TextView tv_preference = (TextView) findViewById(R.id.tv_preference);
            tv_preference.setText(AppInfo.visitorDetail.getString("preference"));

            tv_notes = (TextView) findViewById(R.id.tv_notes);
            tv_notes.setText(AppInfo.visitorDetail.getString("notes"));

            tv_followup = (TextView) findViewById(R.id.tv_followup);
            tv_followup.setText(AppInfo.visitorDetail.getString("follow_up"));

            /**TextView tv_lastlocation = (TextView) findViewById(R.id.tv_lastlocation);
            tv_lastlocation.setText(AppInfo.visitorDetail.getString("lastLocation"));

            TextView tv_lasttime = (TextView) findViewById(R.id.tv_lasttime);
            tv_lasttime.setText(AppInfo.visitorDetail.getString("lastLocationTime"));*/

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getHeadImg(final ImageView iv_headimg){
        //String url_img = AppInfo.visitorDetail.getString("headImg");
        ImageRequest imageRequest = new ImageRequest(AppInfo.visitorDetailHeadimgUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                iv_headimg.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                iv_headimg.setImageDrawable(getResources().getDrawable(R.drawable.head_image));
            }
        });
        VolleySingleton.getInstance(this).addToRequestQueue(imageRequest);
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
                                System.out.println("app visitordetail:> "+response);
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
                                VipAlert.volleyErrorHandle(VisitorDetailActivity.this, error);
                            }
                        });

        VolleySingleton.getInstance(VisitorDetailActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onPause(){
        super.onPause();
        //notificationTimer.cancel();
        //notificationTimer = null;
    }

    @Override
    public void onBackPressed(){
        if (isEditing){
            temp_notes = et_notes.getText().toString().trim();
            temp_followup = et_followup.getText().toString().trim();
            tv_notes.setVisibility(View.VISIBLE);
            tv_followup.setVisibility(View.VISIBLE);

            tv_notes.setText(temp_notes);
            tv_followup.setText(temp_followup);

            et_notes.setVisibility(View.GONE);
            et_followup.setVisibility(View.GONE);

            try {
                if (!et_notes.getText().toString().trim().equals(AppInfo.visitorDetail.getString("notes")) ||
                        !et_followup.getText().toString().trim().equals(AppInfo.visitorDetail.getString("follow_up"))) {
                    isEdited = true;

                    AlertDialog.Builder builder = new AlertDialog.Builder(VisitorDetailActivity.this);
                    builder.setTitle(R.string.alert_title)
                            .setMessage(R.string.alert_message)
                            .setPositiveButton(R.string.alert_save, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    save(toolbar.getMenu().findItem(R.id.action_editdetails));
                                }
                            })
                            .setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isEdited = false;
                                    Menu menu = toolbar.getMenu();
                                    menu.findItem(R.id.action_editdetails).setTitle(R.string.menu_edit);
                                }
                            })
                            .show();

                }
                else {
                    isEdited = false;
                    Menu menu = toolbar.getMenu();
                    menu.findItem(R.id.action_editdetails).setTitle(R.string.menu_edit);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            isEditing = false;
        }
        else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Toast.makeText(VisitorDetailActivity.this, R.string.toast_visitordetail_saved,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        /*SharedPreferences pref = getSharedPreferences("users", MODE_PRIVATE);
        if (pref.getString("poll", null).equals("1")) {
            startGetNotification();
        }*/
        getDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_visitordetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_editdetails:
                item.setTitle("Save");
                if (isEditing || isEdited){
                    save(item);
                }
                else {
                    tv_notes.setVisibility(View.GONE);
                    tv_followup.setVisibility(View.GONE);

                    et_notes.setVisibility(View.VISIBLE);
                    et_followup.setVisibility(View.VISIBLE);

                    try {
                        if (temp_notes.equals("")){
                            et_notes.setText(AppInfo.visitorDetail.getString("notes"));

                        }
                        if (temp_followup.equals("")){
                            et_followup.setText(AppInfo.visitorDetail.getString("follow_up"));
                        }
                        else {
                            et_notes.setText(temp_notes);
                            et_followup.setText(temp_followup);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    et_notes.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(et_notes, InputMethodManager.SHOW_IMPLICIT);
                    isEditing = true;
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void save(MenuItem item){
        loading.setVisibility(View.VISIBLE);
        loading.bringToFront();
        item.setTitle("Edit");
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        et_notes.setVisibility(View.GONE);
        et_followup.setVisibility(View.GONE);

        tv_notes.setVisibility(View.VISIBLE);
        tv_followup.setVisibility(View.VISIBLE);

        final SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
        String url = "http://www.webvep.com/event/mobile/v2/events/" + preferences.getString("urlParams", null) + "/userInfo/save?"
                + preferences.getString("urlGet", null);
        JSONObject params = new JSONObject();
        try {
            params.put("id", preferences.getString("visitorDetailId", null));
            params.put("notes", et_notes.getText().toString().trim());
            params.put("follow_up", et_followup.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("app save params:> " + params);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("app result:> "+response);
                        try {
                            if (response.getString("result").equals("succeed")){
                                reloadUser();
                                Toast.makeText(VisitorDetailActivity.this, R.string.toast_visitordetail_saved,
                                        Toast.LENGTH_SHORT)
                                        .show();
                                temp_notes = temp_followup = "";
                                isEdited = isEditing = false;
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
                        VipAlert.volleyErrorHandle(VisitorDetailActivity.this, error);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);

        isEditing = false;

    }

    private void reloadUser(){
        SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
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
                                ((BitmapDrawable)VisitorDetailActivity.this.getResources().getDrawable(R.drawable.head_image)).getBitmap();
                        loading.setVisibility(View.GONE);
                        getDetails();
                        /**Intent returnIntent = new Intent();
                        returnIntent.putExtra("edit_saved", "1");
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();*/
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        VipAlert.volleyErrorHandle(VisitorDetailActivity.this, error);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}
