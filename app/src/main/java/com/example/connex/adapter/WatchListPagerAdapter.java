package com.example.connex.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.connex.vipwatch.AppInfo;
import com.example.connex.vipwatch.R;
import com.example.connex.vipwatch.VipAlert;
import com.example.connex.vipwatch.VisitorDetailActivity;
import com.example.connex.vipwatch.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by hayton on 1/2/2016.
 */
public class WatchListPagerAdapter extends FragmentPagerAdapter {

    private static WatchListAdapter mAdapter;
    private static JSONArray mList;
    private static Drawable mPhoto;
    private static ArrayList mFilter;
    private static Activity mContext;

    public WatchListPagerAdapter(Activity context, FragmentManager fm, WatchListAdapter adapter, Drawable photo,
                                 JSONArray list, ArrayList filter) {
        super(fm);
        mAdapter = adapter;
        mPhoto = photo;
        mList = list;
        mFilter = filter;
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        /*switch (position) {
            case 0:
                // The first section of the app is the most interesting -- it offers
                // a launchpad into the other demonstrations in this example application.
                return new LaunchpadSectionFragment();

            default:
                // The other sections of the app are dummy placeholders.
                Fragment fragment = new DummySectionFragment();
                Bundle args = new Bundle();
                args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
                fragment.setArguments(args);
                return fragment;
        }*/
        Fragment fragment = new SectionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("section_number", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mFilter.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFilter.get(position).toString();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        //container.removeViewAt(position);
    }

     public static class SectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // initiate the root view of the viewPager
            View rootView = inflater.inflate(R.layout.app_listview_norefresh, container, false);
            final View loading = rootView.findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            final Bundle bundle = getArguments();
            final ListView listView = (ListView) rootView.findViewById(R.id.app_listview_norefresh);
            //final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);

            final ArrayList<ArrayList<JSONObject>> sortedVIP = new ArrayList<>(); //store jsonobject according to description
            final ArrayList<JSONObject> users = new ArrayList<>(); //store jsonobject for all users

            // put jsonobject for all users
            for (int i=0;i<mList.length();i++){
                try {
                    for (int j=0;j<mList.getJSONObject(i).getJSONArray("users").length();j++){
                        users.add(mList.getJSONObject(i).getJSONArray("users").getJSONObject(j));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //combine every users json to form "ALL"
            for (int i=0;i<mList.length();i++) {
                ArrayList<JSONObject> temp = new ArrayList<>();
                sortedVIP.add(temp);
                try {
                    for (int j=0;j<mList.getJSONObject(i).getJSONArray("users").length();j++){
                        sortedVIP.get(i).add(0, mList.getJSONObject(i).getJSONArray("users").getJSONObject(j));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (bundle.getInt(ARG_SECTION_NUMBER)==0) {
                mAdapter = new WatchListAdapter(getActivity(), mPhoto, users);
                listView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                listView.setTag(bundle.getInt(ARG_SECTION_NUMBER));
            }
            else {
                mAdapter = new WatchListAdapter(getActivity(), mPhoto, sortedVIP.get(bundle.getInt(ARG_SECTION_NUMBER)-1));
                listView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                listView.setTag(bundle.getInt(ARG_SECTION_NUMBER));
            }

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, VisitorDetailActivity.class);
                    loading.setVisibility(View.VISIBLE);
                    loading.bringToFront();
                    try {
                        if (bundle.getInt(ARG_SECTION_NUMBER)==0){
                            System.out.println(" clicked at :> " + users.get(position).getString("name"));
                            AppInfo.visitorDetailName = users.get(position).getString("name");
                            SharedPreferences preferences = mContext.getSharedPreferences("users", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("visitorDetailId", users.get(position).getString("id"));
                            editor.commit();

                            //(Context, Sharedpreference, Intent, View)
                            setGlobalUser(mContext, preferences, intent, loading);

                        }
                        else {
                            System.out.println(" clicked at :> " + sortedVIP.get(bundle.getInt(ARG_SECTION_NUMBER)-1).get(position).getString("name"));
                            AppInfo.visitorDetailName = sortedVIP.get(bundle.getInt(ARG_SECTION_NUMBER)-1).get(position).getString("name");
                            SharedPreferences preferences = mContext.getSharedPreferences("users", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("visitorDetailId", sortedVIP.get(bundle.getInt(ARG_SECTION_NUMBER) - 1).get(position).getString("id"));
                            editor.commit();

                            setGlobalUser(mContext, preferences, intent, loading);

                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return rootView;
        }

         private void setGlobalUser(final Context mContext, SharedPreferences preferences, final Intent intent, final View loading){
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
                                     ((BitmapDrawable)mContext.getResources().getDrawable(R.drawable.head_image)).getBitmap();
                             loading.setVisibility(View.GONE);

                             startActivity(intent);
                         }
                     },
                     new Response.ErrorListener() {
                         @Override
                         public void onErrorResponse(VolleyError error) {
                             loading.setVisibility(View.GONE);
                             VipAlert.volleyErrorHandle(mContext, error);
                         }
                     });
             jsonObjectRequest.setShouldCache(false);
             VolleySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
         }

     }



}
