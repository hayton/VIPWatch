package com.example.connex.vipwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.connex.adapter.LocationList_visitor_adapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hayton on 8/7/2016.
 */
public class VisitorBaseFragment extends Fragment {

    private ArrayList<String> userArray = new ArrayList<>();
    private ArrayList<String> boothname = new ArrayList<>();
    private static LocationList_visitor_adapter adapter;

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View pagerView = inflater.inflate(R.layout.location_pagerview_inflate, container, false);
        RelativeLayout relativeLayout = (RelativeLayout) pagerView.findViewById(R.id.lv_app_norefresh);

        final ListView visitorListView = (ListView) relativeLayout.findViewById(R.id.app_listview_norefresh);
        final View loading = relativeLayout.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        userArray = getArguments().getStringArrayList("userArray");
        System.out.println(" fragment userarray:> "+userArray);

        boothname = getArguments().getStringArrayList("boothname");
        System.out.println(" fragment boothname:> "+boothname);

        //getData(expandableListView, visitorListView, loading);

        //switch (getArguments().getInt(ARG_SECTION_NUMBER)) {

        //case 1:
        adapter = new LocationList_visitor_adapter(getActivity()
                , getResources().getDrawable(R.drawable.head_image), userArray, boothname);
        visitorListView.bringToFront();
        visitorListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        visitorListView.setTag("visitor");
        //break;
        //}

        visitorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    JSONObject jsonObject = new JSONObject(userArray.get(position));
                    System.out.println(" visitor name :> " + jsonObject.getString("name"));

                    Intent intent = new Intent(getContext(), VisitorDetailActivity.class);
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("users", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("visitorDetailId", jsonObject.getString("id"));
                    if (editor.commit()) {
                        setGlobalUser(sharedPreferences, intent, loading);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return pagerView;
    }

    public static void updateVisitor1ListView(){
        if(adapter != null){
            adapter.notifyDataSetChanged();
            System.out.println(" called visitor");
        }
    }

    private void setGlobalUser(SharedPreferences preferences, final Intent intent, final View loading){
        final String url = "http://www.webvep.com/event/mobile/v2/events/"+preferences.getString("urlParams", null)+"/userInfo/"
                +preferences.getString("visitorDetailId", null)+"?"+preferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" detail url:> "+url);
                        AppInfo.visitorDetail = response;
                        System.out.println(" visitor detail:> " + response);
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
                        VipAlert.volleyErrorHandle(getContext(), error);
                    }
                });
        jsonObjectRequest.setShouldCache(false);
        VolleySingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
}
