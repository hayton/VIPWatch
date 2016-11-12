package com.example.connex.vipwatch;

import android.app.Activity;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hayton on 9/5/2016.
 */
public class AppInfo {
    public static ArrayList<Activity> activityList = new ArrayList<>();

    public static HashMap<String, Integer> vepIdMapJsonPosition = new HashMap<>();

    public static ArrayList<HashMap> drawer_list = new ArrayList<>(); //items in the navigation drawer
    public static ArrayList filter = new ArrayList();

    public static String api_key = "NULQVtq3mjXFCttOy4FmzM95";

    public static JSONArray watchlistUser = new JSONArray();
    public static HashMap<String, JSONObject> visitorNameMapJson = new HashMap<>();

    public static String visitorDetailName;
    public static JSONObject visitorDetail;
    public static String visitorDetailHeadimgUrl;
    public static Bitmap visitorDetailHeadBitmap;

    public static Integer alertInterval;

    public static List<String> eventlist = new ArrayList<>();
    public static JSONArray loginParams;

}