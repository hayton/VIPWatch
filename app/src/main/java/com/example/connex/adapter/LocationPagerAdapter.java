package com.example.connex.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.connex.vipwatch.LocationBaseFragment;
import com.example.connex.vipwatch.VisitorBaseFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hayton on 9/5/2016.
 */
public class LocationPagerAdapter extends FragmentStatePagerAdapter {

    private static Context context;
    private String[] header;
    private static ArrayList<String> listParent;
    private static HashMap<String, List<String[]>> listChild;
    private static ArrayList<String> userArray;
    private static ArrayList<String> boothname;
    private static ExpandableListAdapter expadapter;
    private static LocationList_visitor_adapter adapter;
    private static ArrayList<JSONObject> userJsonArray = new ArrayList<>();

    LocationBaseFragment frag1;  VisitorBaseFragment frag2;

    public LocationPagerAdapter(Activity context, FragmentManager fm, String[] header,
                                ArrayList<String> listParent, HashMap<String, List<String[]>> listChild,
                                ArrayList<String> userArray, ArrayList<String> boothname) {
        super(fm);
        this.context = context;
        this.header = header;
        this.listParent = listParent;
        this.listChild = listChild;
        this.userArray = userArray;
        this.boothname = boothname;
        //this.expadapter = expadapter;
        //this.adapter = adapter;


    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    @Override
    public Fragment getItem(int position) {
        //Fragment fragment = new LocationBaseFragment();
        Bundle bundle = new Bundle();
        //bundle.putInt("section_number", position);
        bundle.putStringArrayList("listParent", listParent);
        bundle.putSerializable("listChild", listChild);
        //fragment.setArguments(bundle);

        Bundle bundle2 = new Bundle();
        bundle2.putStringArrayList("userArray", userArray);
        bundle2.putStringArrayList("boothname", boothname);

        switch (position)
        {
            case 0:
                frag1 = new LocationBaseFragment();
                frag1.setArguments(bundle);
                return frag1;

            case 1:
                frag2 = new VisitorBaseFragment();
                frag2.setArguments(bundle2);
                return frag2;
        }

        return null;

    }

    @Override
    public int getCount() {
        return header.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return header[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){

    }


}
