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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.connex.adapter.ExpandableListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hayton on 8/7/2016.
 */
public class LocationBaseFragment extends Fragment {


    private static ArrayList<String> listParent = new ArrayList<>();
    private static HashMap<String, List<String[]>> listChild = new HashMap<>();
    private static ExpandableListAdapter expadapter;
    //private static Context context;


    public static final String ARG_SECTION_NUMBER = "section_number";
    //private List<String> listParent = new ArrayList<>();
    //private HashMap<String, List<String[]>> listChild = new HashMap<>();
    //private ArrayList<JSONObject> userArray = new ArrayList<>();
    //private ArrayList<String> boothname = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View pagerView = inflater.inflate(R.layout.location_pagerview_inflate, container, false);
        RelativeLayout relativeLayout = (RelativeLayout) pagerView.findViewById(R.id.lv_app_norefresh);

        final ExpandableListView expandableListView =
                (ExpandableListView) pagerView.findViewById(R.id.lv_visitorlocation);

        final View loading = relativeLayout.findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        System.out.println(" getarguments:> "+getArguments());

        //if(getArguments().getSerializable("hashmap") != null) {
            listChild = (HashMap<String, List<String[]>>) getArguments().getSerializable("listChild");
            System.out.println(" hashmap:> "+listChild);
        //}
        listParent = getArguments().getStringArrayList("listParent");


        expadapter = new ExpandableListAdapter(getActivity(), listParent, listChild);
        expandableListView.bringToFront();
        expandableListView.setAdapter(expadapter);
        expadapter.notifyDataSetChanged();
        expandableListView.setTag("location");
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                Intent intent = new Intent(getContext(), VisitorDetailActivity.class);
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("users", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("visitorDetailId", listChild.get(listParent.get(groupPosition)).get(childPosition)[2]);
                if (editor.commit()) {
                    setGlobalUser(sharedPreferences, intent, loading);
                }

                return false;
            }
        });

        return pagerView;
    }

    public static void updateLocation1ListView() {
        if (expadapter != null) {
            expadapter.notifyDataSetChanged();
            System.out.println("location refresh");
        }
    }


    private void setGlobalUser(SharedPreferences preferences, final Intent intent, final View loading) {
        final String url = "http://www.webvep.com/event/mobile/v2/events/" + preferences.getString("urlParams", null) + "/userInfo/"
                + preferences.getString("visitorDetailId", null) + "?" + preferences.getString("urlGet", null);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(" detail url:> " + url);
                        AppInfo.visitorDetail = response;
                        System.out.println(" visitor detail:> " + response);
                        if (response.has("headImg")) {
                            try {
                                AppInfo.visitorDetailHeadimgUrl = response.getString("headImg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else AppInfo.visitorDetailHeadBitmap =
                                ((BitmapDrawable) getResources().getDrawable(R.drawable.head_image)).getBitmap();
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