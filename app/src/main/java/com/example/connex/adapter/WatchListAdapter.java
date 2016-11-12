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

//import com.example.hayton.vipwatch.R;

/**
 * Created by hayton on 17/12/2015.
 */
public class WatchListAdapter extends BaseAdapter{
    private ArrayList<JSONObject> mList;
    private LayoutInflater mInflater = null;
    private Activity mContext;
    private Drawable vipPhoto;
    private ImageLoader imageLoader;

    public WatchListAdapter(Activity context, Drawable photo, ArrayList<JSONObject> info){
        mList = info;
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
    public JSONObject getItem(int position) {
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
        if (convertView == null){
            mInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = mInflater.inflate(R.layout.item_watchlist, parent, false);
            holder = new ViewHolder();
            holder.vip_name = (TextView) v.findViewById(R.id.tv_vip_name);
            holder.vip_company = (TextView) v.findViewById(R.id.tv_vip_company);
            holder.vip_photo = (NetworkImageView) v.findViewById(R.id.vip_photo);
            holder.sec_attribute = (TextView) v.findViewById(R.id.sec_attribute);
            holder.vip_title = (TextView) v.findViewById(R.id.tv_title);
            holder.sec_attribute.setVisibility(View.GONE);
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder)v.getTag();
        }

        try {
            holder.vip_name.setText(getItem(position).getString("name"));
            holder.vip_company.setText(getItem(position).getString("company"));
            if (getItem(position).has("headImg")){
                imageLoader = VolleySingleton.getInstance(mContext).getImageLoader();

                holder.vip_photo.setImageUrl(getItem(position).getString("headImg")
                        , imageLoader);
                holder.vip_photo.setDefaultImageResId(R.drawable.head_image);
                holder.vip_photo.setErrorImageResId(R.drawable.head_image);
            }
            else holder.vip_photo.setDefaultImageResId(R.drawable.head_image);

            holder.vip_title.setText(getItem(position).getString("title"));

            return v;
        }catch (JSONException e){
            System.out.println(e);
        }
        return v;
    }

    /********* A holder Class to contain inflated xml file elements *********/
    public class ViewHolder{
        TextView vip_name;
        TextView vip_company;
        TextView sec_attribute;
        TextView vip_title;
        NetworkImageView vip_photo;
    }

}
