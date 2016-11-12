package com.example.connex.vipwatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity implements View.OnClickListener {

    private JSONObject loginParams = new JSONObject();
    private EditText et_email, et_password;
    public static View after_login;
    private Button btn_login;
    private String visitorDetailId, selected_VepId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        after_login = findViewById(R.id.loading);
        after_login.setVisibility(View.GONE);

        btn_login = (Button) findViewById(R.id.login);
        Button forget = (Button) findViewById(R.id.forget);
        final Spinner sp_eventlist = (Spinner) findViewById(R.id.sp_eventlist);
        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);

        ArrayAdapter adapter = new ArrayAdapter(LoginActivity.this,
                android.R.layout.simple_spinner_item, AppInfo.eventlist);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_eventlist.setAdapter(adapter);

        if (getIntent().hasExtra("visitorDetailId") || getIntent().hasExtra("selected_VepId")) {
            visitorDetailId = getIntent().getStringExtra("visitorDetailId");
            selected_VepId = getIntent().getStringExtra("selected_VepId");
            sp_eventlist.setSelection(AppInfo.vepIdMapJsonPosition.get(selected_VepId));
            Toast.makeText(LoginActivity.this, R.string.toast_choose_activity, Toast.LENGTH_LONG).show();
        }
        /*else if (getIntent().hasExtra("from401")){
            Toast.makeText(LoginActivity.this, R.string.from401)
        }*/
        //sp_eventlist.setSelection(1);
        sp_eventlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("app clicked at:> " + position);
                try {
                    loginParams = AppInfo.loginParams.getJSONObject(position);
                    SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("urlParams", loginParams.getString("clientDomain").toLowerCase() +
                            "/" + loginParams.getString("eventUrl") + "/" + loginParams.getString("language").toLowerCase());
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                after_login.setVisibility(View.VISIBLE);
                after_login.bringToFront();
                SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putString("loginEmail", et_email.getText().toString().trim());
                editor.putString("loginPassword", et_password.getText().toString().trim());

                if (editor.commit()) {
                    System.out.println(" committed");
                    System.out.println("loginemail:> " + preferences.getString("loginEmail", null));
                    System.out.println("loginpassword:> " + preferences.getString("loginPassword", null));

                    if (getIntent().hasExtra("fromPush")){
                        loginFromPush();
                    }
                    else {
                        /** login function can be found in NotificationReceiver.java */
                        PushManager.startWork(LoginActivity.this, PushConstants.LOGIN_TYPE_API_KEY, AppInfo.api_key);
                    }
                }

                break;
            case R.id.forget:
                Intent intent = new Intent(getApplicationContext(), RetrievePasswordActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void loginFromPush(){
        try {
            System.out.println("login from push");
            SharedPreferences preferences = getSharedPreferences("users", MODE_PRIVATE);
            final SharedPreferences.Editor editor = preferences.edit();
            final String url = "http://www.webvep.com/event/mobile/v2/events/"
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
                            AppInfo.alertInterval = response.getInt("alert_interval");
                            editor.putString("urlGet", "sessionId=" + response.getString("sessionId") + "&userId="
                                    + response.getString("userId") + "&userType=" + response.getString("userType") +
                                    "&selectedVepId=" + response.getString("selectedVepId"));
                            editor.putString("selected_VepId", response.getString("selectedVepId"));
                            editor.putString("username", response.getString("name"));
                            editor.putBoolean("isLogin", true);
                            if (editor.commit()) {
                                Intent intent = new Intent(LoginActivity.this, WatchListActivity.class);
                                intent.putExtra("fromPush", "1");
                                intent.putExtra("visitorDetailId", visitorDetailId);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                        else if (response.getString("result").equals("failed")){
                            Toast.makeText(LoginActivity.this, R.string.login_failed,
                                    Toast.LENGTH_SHORT).show();
                            after_login.setVisibility(View.GONE);
                        }
                        else {
                            Toast.makeText(LoginActivity.this, R.string.login_unauthorized,
                                    Toast.LENGTH_SHORT).show();
                            after_login.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                    after_login.setVisibility(View.GONE);
                VipAlert.volleyErrorHandle(LoginActivity.this, error);
                }
        });
            loginRequest.setShouldCache(false);
            VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
