package com.example.connex.vipwatch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by hayton on 9/5/2016.
 */
public class BaseActivity extends AppCompatActivity {

    private ArrayList<JSONObject> viplist = new ArrayList<>();
    private ArrayList<HashMap> drawer_list = new ArrayList<>();

    private ListView lv;
    private ListView drawerList;
    //private WatchListAdapter watchListAdapter;
    private Drawable vip_photo;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Context mContext;
    private CharSequence mTitle;
    private RelativeLayout drawer_parent_layout;
    //public static ViewPager pager;
    //WatchListPagerAdapter pagerAdapter;

    protected void onCreateDrawer(final ArrayList<HashMap> drawer_list, final Context context, final boolean isError){
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        AppInfo.activityList.add(this);
        this.drawer_list = drawer_list;
        mContext = context;

        //setup toolbar and actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.hamburger_icon);
        getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        /** navigation drawer starts */
        NavigationView view = (NavigationView) findViewById(R.id.nav_view);
        TextView tv_userEmail = (TextView)view.getHeaderView(0).findViewById(R.id.tv_useremail);
        tv_userEmail.setText(getSharedPreferences("users", MODE_PRIVATE).getString("username", null));
        System.out.println("app userName:> "+getSharedPreferences("users", MODE_PRIVATE).getString("username", null));

        Menu drawer_menu = view.getMenu();
        for (int i=0;i<AppInfo.filter.size();i++) {
            drawer_menu.add(drawer_menu.getItem(0).getGroupId(), i, 0, "    "+AppInfo.filter.get(i).toString());

        }

        if (!isError) {
            view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    Intent intent;

                    if (menuItem.getItemId() >= 0 && menuItem.getItemId() < AppInfo.filter.size()) {
                        if (BaseActivity.this instanceof WatchListActivity) {
                            drawerLayout.closeDrawers();
                            WatchListActivity.pager.setCurrentItem(menuItem.getItemId());
                        } else {
                            drawerLayout.closeDrawers();
                            goToWatchListActivity(BaseActivity.this, menuItem.getItemId());
                        }
                    }
                    switch (menuItem.getItemId()) {
                        case R.id.watchlist:
                            if (BaseActivity.this instanceof WatchListActivity) {
                                drawerLayout.closeDrawers();
                            } else {
                                drawerLayout.closeDrawers();
                                goToWatchListActivity(BaseActivity.this, 0);
                            }
                            break;
                        case R.id.visitorlocation:
                            if (BaseActivity.this instanceof VisitorLocationActivity) {
                                drawerLayout.closeDrawers();
                            } else {
                                drawerLayout.closeDrawers();
                                intent = new Intent(BaseActivity.this, VisitorLocationActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                            break;
                        case R.id.alertcenter:
                            if (BaseActivity.this instanceof AlertCenterActivity) {
                                drawerLayout.closeDrawers();
                            } else {
                                drawerLayout.closeDrawers();
                                intent = new Intent(BaseActivity.this, AlertCenterActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                            break;
                        case R.id.setting:
                            if (BaseActivity.this instanceof SettingActivity) {
                                drawerLayout.closeDrawers();
                            } else {
                                drawerLayout.closeDrawers();
                                intent = new Intent(BaseActivity.this, SettingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intent);
                            }
                            break;
                        case R.id.logout:
                            VipAlert.logout(BaseActivity.this);
                    }
                    return true;
                }
            });
        }
        view.requestLayout();
        view.bringToFront();
        /** navigation drawer ends */

    }

    @Override
    public void onBackPressed(){
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)){
            this.drawerLayout.closeDrawers();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }
        if(id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void goToWatchListActivity(Context context, int fragmentNum){
        Intent intent = new Intent(context, WatchListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("fragmentNum", fragmentNum);
        startActivity(intent);
    }
}
