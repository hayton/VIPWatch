package com.example.connex.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.connex.vipwatch.R;
import com.example.connex.vipwatch.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hayton on 6/6/2016.
 */
public class LocationList_visitor_adapter extends BaseAdapter {
    private ArrayList<String> mList;
    private LayoutInflater mInflater = null;
    private Activity mContext;
    private Drawable vipPhoto;
    private ArrayList<String> boothname;

    public LocationList_visitor_adapter(Activity context, Drawable photo, ArrayList<String> info, ArrayList<String> boothname) {
        mList = info;
        this.boothname = boothname;
        vipPhoto = photo;
        mContext = context;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (convertView == null) {
            mInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = mInflater.inflate(R.layout.item_watchlist, parent, false);
            holder = new ViewHolder();
            holder.vip_name = (TextView) v.findViewById(R.id.tv_vip_name);
            holder.vip_boothlocation = (TextView) v.findViewById(R.id.tv_vip_company);
            holder.vip_title = (TextView) v.findViewById(R.id.tv_title);
            holder.sec_attribute = (TextView) v.findViewById(R.id.sec_attribute);
            holder.vip_photo = (NetworkImageView) v.findViewById(R.id.vip_photo);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        try {
            JSONObject jsonObject = new JSONObject(getItem(position));
            holder.vip_name.setText(jsonObject.getString("name"));
            holder.sec_attribute.setText(R.string.tv_item_location);
            holder.vip_boothlocation.setText(boothname.get(position));
            if (jsonObject.has("headImg")) {
                ImageLoader imageLoader = VolleySingleton.getInstance(mContext).getImageLoader();
                holder.vip_photo.setImageUrl(jsonObject.getString("headImg"), imageLoader);
                holder.vip_photo.setDefaultImageResId(R.drawable.head_image);
                holder.vip_photo.setErrorImageResId(R.drawable.head_image);
            }
            else {
                holder.vip_photo.setDefaultImageResId(R.drawable.head_image);
            }
            holder.vip_title.setText(jsonObject.getString("title"));

            return v;
        } catch (JSONException e) {
            System.out.println(e);
        }
        return v;
    }

    /*********
     * A holder Class to contain inflated xml file elements
     *********/
    public class ViewHolder {
        TextView vip_name;
        TextView vip_boothlocation;
        TextView sec_attribute;
        TextView vip_title;
        NetworkImageView vip_photo;
    }
}
