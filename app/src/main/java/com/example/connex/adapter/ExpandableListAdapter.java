package com.example.connex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.connex.vipwatch.R;
import com.example.connex.vipwatch.VolleySingleton;

import java.util.HashMap;
import java.util.List;

/**
 * Created by hayton on 10/5/2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listParent;
    private HashMap<String, List<String[]>> listChild;
    ImageLoader imageLoader;

    public ExpandableListAdapter(Context context, List<String> listParent, HashMap<String, List<String[]>> listChild){
        this.context = context;
        this.listChild = listChild;
        this.listParent = listParent;
    }

    @Override
    public int getGroupCount() {
        System.out.println(" groupcount :> "+listParent.size());
        return this.listParent.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        System.out.println(" chldrencount :> "+this.listChild.get(this.listParent.get(groupPosition)).size());
        return this.listChild.get(this.listParent.get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return this.listParent.get(groupPosition);
    }

    @Override
    public String[] getChild(int groupPosition, int childPosition) {
        return this.listChild.get(this.listParent.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition);
        System.out.println(" title :>"+headerTitle);
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.locationlist_groupview, parent, false);

        TextView tv_boothname = (TextView) convertView.findViewById(R.id.tv_boothname);
        TextView tv_visitornum = (TextView) convertView.findViewById(R.id.tv_visitornum);
        tv_boothname.setText(headerTitle);
        tv_visitornum.setText(String.valueOf(getChildrenCount(groupPosition)));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String visitorname = getChild(groupPosition, childPosition)[0];
        final String watchlistname = getChild(groupPosition, childPosition)[1];
        final String title = getChild(groupPosition, childPosition)[3];
        final String img_url = getChild(groupPosition, childPosition)[4];
        //final ImageLoader imageLoader;
        System.out.println(" child :> "+visitorname);
        System.out.println(" user title:> "+title);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.locationlist_childview, parent, false);

        TextView tv_visitorname = (TextView) convertView.findViewById(R.id.tv_visitorname);
        TextView tv_watchlistdesc = (TextView) convertView.findViewById(R.id.tv_watchlisedesc);
        TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
        NetworkImageView vip_photo = (NetworkImageView) convertView.findViewById(R.id.vip_photo);

        tv_title.setText(title);
        tv_visitorname.setText(visitorname);
        tv_watchlistdesc.setText(watchlistname);

        //RequestQueue requestQueue = VolleySingleton.getInstance(context).getRequestQueue();

        imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        imageLoader.get(img_url, ImageLoader.getImageListener(vip_photo,
                R.drawable.head_image, R.drawable.head_image));
        //imageLoader = VolleySingleton.getInstance(context).getImageLoader();
        vip_photo.setImageUrl(img_url, imageLoader);
        //vip_photo.setDefaultImageResId(R.drawable.head_image);
        //vip_photo.setErrorImageResId(R.drawable.head_image);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
