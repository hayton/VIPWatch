package com.example.connex.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.connex.vipwatch.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hayton on 4/1/2016.
 */
public class DrawerAdapter extends BaseAdapter {

    private ArrayList<HashMap> mdrawerItem;
    private Activity mcontext;
    private LayoutInflater mlayoutInflater = null;
    public int mFilterLength;

    public DrawerAdapter(Activity context, ArrayList<HashMap> drawerItem){
        mcontext = context;
        mdrawerItem = drawerItem;
        //mFilterLength = filterLength;
        mlayoutInflater = (LayoutInflater) mcontext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mdrawerItem.size();
    }

    @Override
    public HashMap getItem(int position) {
        return mdrawerItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = new ViewHolder();
        if (convertView == null){
            mlayoutInflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            /*if ((position>=1)&&(position<=mFilterLength)){
                v = mlayoutInflater.inflate(R.layout.drawer_filter, parent, false);
                holder.item = (TextView) v.findViewById(R.id.watchlist_filter);
                //v.setTag(holder);
            }
            else{
                v = mlayoutInflater.inflate(R.layout.drawer_list_header, parent, false);
                holder.item = (TextView) v.findViewById(R.id.drawer_list_header);
                //v.setTag(holder);
            }*/

            if (getItem(position).get("type").equals("filter")){
                v = mlayoutInflater.inflate(R.layout.item_drawer_list_filter, parent, false);
                holder.item = (TextView) v.findViewById(R.id.watchlist_filter);
            }
            else {
                v = mlayoutInflater.inflate(R.layout.item_drawer_list_header, parent, false);
                holder.item = (TextView) v.findViewById(R.id.drawer_list_header);
            }
            v.setTag(holder);
        }
        else {
            holder = (ViewHolder)v.getTag();
        }

        holder.item.setText(getItem(position).get("name").toString());
        return v;
    }

    public class ViewHolder{
        TextView item;
    }
}
