package com.example.connex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.connex.vipwatch.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hayton on 26/5/2016.
 */
public class AlertListAdapter extends BaseAdapter {

    private ArrayList<JSONObject> alertHistory;
    private Context mContext;

    public AlertListAdapter (ArrayList<JSONObject> alertHistory, Context context) {
        this.alertHistory = alertHistory;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return this.alertHistory.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return this.alertHistory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_alertlist, parent, false);
            holder = new ViewHolder();
            holder.vip_name = (TextView) convertView.findViewById(R.id.tv_vip_name);
            holder.alert_booth = (TextView) convertView.findViewById(R.id.alert_booth);
            holder.alert_listdesc = (TextView) convertView.findViewById(R.id.alert_listdesc);
            holder.alert_time = (TextView) convertView.findViewById(R.id.alert_time);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        try {
            holder.vip_name.setText(getItem(position).getString("userName"));
            holder.alert_booth.setText(getItem(position).getString("boothName"));
            holder.alert_time.setText(getItem(position).getString("sendTime"));
            holder.alert_listdesc.setText(getItem(position).getString("watchListName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public class ViewHolder{
        TextView vip_name;
        TextView alert_time;
        TextView alert_booth;
        TextView alert_listdesc;
    }
}
