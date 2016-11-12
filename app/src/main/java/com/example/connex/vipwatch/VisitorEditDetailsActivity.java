package com.example.connex.vipwatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.HashMap;

public class VisitorEditDetailsActivity extends AppCompatActivity {

    private LinearLayout phone_layout;
    private LinearLayout email_layout;
    private HashMap<Integer, View> phoneViewList = new HashMap<>();
    private HashMap<Integer, View> emailViewList = new HashMap<>();
    private ArrayList<View> remove_item = new ArrayList<>();
    private View.OnClickListener listener;
    private EditText et_info_phone, et_info_email, et_notes, et_followup;
    private View loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_edit_details);

        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        /**phone_layout = (LinearLayout) findViewById(R.id.detail_phone);
        email_layout = (LinearLayout) findViewById(R.id.detail_email);*/

        try {
            getSupportActionBar().setTitle(AppInfo.visitorDetail.getString("name"));

            /**View phone_view = getLayoutInflater().inflate(R.layout.visitoreditdetail_extrainfo_phone, null);
            et_info_phone = (EditText) phone_view.findViewById(R.id.et_itemdetail);
            et_info_phone.setText(AppInfo.visitorDetail.getString("mobile"));

            phone_view.setId(phoneViewList.size());
            phoneViewList.put(phoneViewList.size(), phone_view);
            phone_layout.addView(phone_view);*/

            /** Set delete/add listener, not used.
             *  Will have to change the save detail API and add method to get multiple phone number.
             *  Also need to uncomment the corresponding view inside the layout file.*/
            /**deletePhoneOnclick();
            /*View add_phone = findViewById(R.id.add_phone);
            add_phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View view = getLayoutInflater().inflate(R.layout.visitoreditdetail_extra_info, null);
                    phone_layout.addView(view);
                    phoneViewList.put(phoneViewList.size(), view);
                    deletePhoneOnclick();

                }
            });*/

            /**View email_view = getLayoutInflater().inflate(R.layout.visitoreditdetail_extrainfo_email, null);
            et_info_email = (EditText) email_view.findViewById(R.id.et_itemdetail);

            et_info_email.setText(AppInfo.visitorDetail.getString("email"));

            email_view.setId(emailViewList.size());
            emailViewList.put(emailViewList.size(), email_view);
            email_layout.addView(email_view);*/

            /** Set delete/add listener, not used.
             *  Will have to change the save detail API and add method to get multiple email address.
             *  Also need to uncomment the corresponding view inside the layout file.*/
            /**deleteEmailOnclick();
            /*View add_email = findViewById(R.id.add_email);
            add_email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View view = getLayoutInflater().inflate(R.layout.visitoreditdetail_extra_info, null);
                    TextView textView = (TextView) view.findViewById(R.id.tv_itemindicator);
                    textView.setText("Email");
                    email_layout.addView(view);
                    emailViewList.put(emailViewList.size(), view);
                    deleteEmailOnclick();

                }
            });*/

            et_notes = (EditText) findViewById(R.id.et_notes);
            et_notes.setText(AppInfo.visitorDetail.getString("notes"));

            et_followup = (EditText) findViewById(R.id.et_followup);
            et_followup.setText(AppInfo.visitorDetail.getString("follow_up"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /** set delete phone number button onclicklistener */
    /**private void deletePhoneOnclick() {
        for (int i = 0; i < phoneViewList.size(); i++) {
            final int finalI = i;
            phoneViewList.get(i).findViewById(R.id.delete_item)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            phone_layout.removeView(phoneViewList.get(finalI));
                        }
                    });
            EditText et_newphone = (EditText) phoneViewList.get(i).findViewById(R.id.et_itemdetail);
            et_newphone.setInputType(InputType.TYPE_CLASS_PHONE);
        }
    }*/

    /** set delete email address button onclicklistener */
    /**private void deleteEmailOnclick() {
        for (int i = 0; i < emailViewList.size(); i++) {
            final int finalI = i;
            emailViewList.get(i).findViewById(R.id.delete_item)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            email_layout.removeView(emailViewList.get(finalI));
                        }
                    });
            EditText et_newemail = (EditText) emailViewList.get(i).findViewById(R.id.et_itemdetail);
            et_newemail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_visitor_edit_details_activity, menu);
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
            finish();
            return true;
        }

        if (id == R.id.action_save){
            loading.setVisibility(View.VISIBLE);
            loading.bringToFront();
            View view = this.getCurrentFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            final SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
            String url = "http://www.webvep.com/event/mobile/v2/events/" + preferences.getString("urlParams", null) + "/userInfo/save?"
                    + preferences.getString("urlGet", null);
            JSONObject params = new JSONObject();
            try {
                params.put("id", preferences.getString("visitorDetailId", null));
                params.put("mobile", et_info_phone.getText().toString().trim());
                params.put("email", et_info_email.getText().toString().trim());
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
                                    setGlobalUser();
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
                            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                Toast.makeText(VisitorEditDetailsActivity.this,
                                        "Cannot connect to server: " + error.networkResponse.statusCode,
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof AuthFailureError) {
                                Toast.makeText(VisitorEditDetailsActivity.this,
                                        "Cannot authenticate with server: " + error.networkResponse.statusCode,
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ServerError) {
                                Toast.makeText(VisitorEditDetailsActivity.this,
                                        "Server error: " + error.networkResponse.statusCode,
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof NetworkError) {
                                Toast.makeText(VisitorEditDetailsActivity.this,
                                        "Network error: " + error.networkResponse.statusCode,
                                        Toast.LENGTH_LONG).show();
                            } else if (error instanceof ParseError) {
                                Toast.makeText(VisitorEditDetailsActivity.this,
                                        "Parse error: " + error.networkResponse.statusCode,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            jsonObjectRequest.setShouldCache(false);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setGlobalUser(){
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
                                ((BitmapDrawable)VisitorEditDetailsActivity.this.getResources().getDrawable(R.drawable.head_image)).getBitmap();
                        loading.setVisibility(View.GONE);

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("edit_saved", "1");
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(VisitorEditDetailsActivity.this,
                                    "Cannot connect to server: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(VisitorEditDetailsActivity.this,
                                    "Cannot authenticate with server: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(VisitorEditDetailsActivity.this,
                                    "Server error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(VisitorEditDetailsActivity.this,
                                    "Network error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(VisitorEditDetailsActivity.this,
                                    "Parse error: " + error.networkResponse.statusCode,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

}
