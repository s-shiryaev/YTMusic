package ru.mrsmile2114.ytmusic;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import ru.mrsmile2114.ytmusic.PlaylistItems.PlaylistItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PlaylistItemsFragment.OnListFragmentInteractionListener, DownloadStart.OnFragmentInteractionListener {

    private FloatingActionButton fab;
    private NavigationView navigationView;
    private Menu mymenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(FabListMain);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_download);
        fab.setImageResource(R.drawable.ic_menu_search);
        Fragment fragment;
        if (getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_START")==null) { //no memory leak :D
            fragment = new DownloadStart();
        } else {
            fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_START");
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOAD_START");
        transaction.addToBackStack("FRAGMENT_DOWNLOAD_START");
        transaction.commit();
        fab.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mymenu=menu;
        //selectAllCheckBox.setVisibility(View.INVISIBLE);
        //selectAllCheckBox.setEnabled(false);
        mymenu.findItem(R.id.action_check_box).setVisible(false);
        //((CheckBox)mymenu.findItem(R.id.action_check_box).getActionView()).setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
        //    @Override
        //    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //        if (isChecked) {
        //           System.out.println("CLICK CHECK *->0");
         //           //System.out.println(position+" CHANGED ON TRUE");//TODO:DELETE ON RELEASE
         //       } else {
         //           System.out.println("CLICK CHECK 0->*");
         //           //System.out.println(position+"CHANGED ON False");//TODO:DELETE ON RELEASE
        //        }
        //    }
        //});
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_check_box){
            System.out.println("OPT SELECTED");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Class fragmentClass = null;
        if (id == R.id.nav_download) {
            Fragment fragment;
            if (getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_START")==null) { //no memory leak :D
                fragment = new DownloadStart();
                //System.out.println("NEW FRAGMENT START");

            } else {
                fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_START");
                //System.out.println("FRAGMENT FIND START");
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOAD_START");
            transaction.addToBackStack("FRAGMENT_DOWNLOAD_START");
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        } else if (id == R.id.nav_manage) {
            Fragment fragment;
            if (getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_MANAGE")==null) { //no memory leak :D
                fragment = new PlaylistItemsFragment();
                //System.out.println("NEW FRAGMENT DOWN");

            } else {
                //System.out.println("FRAGMENT FIND DOWN");
                fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_MANAGE");
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOAD_MANAGE");
            transaction.addToBackStack("FRAGMENT_DOWNLOAD_MANAGE");
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onListFragmentInteraction(PlaylistItem item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public View.OnClickListener FabListMain = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            System.out.println("TEST");
        }
    };

    public void SetMainFabListener(View.OnClickListener listener){ fab.setOnClickListener(listener); }
    public void SetMainFabImage(int imageResourse){ fab.setImageResource(imageResourse); }
    public void SetMainFabVisible(boolean vis){
        if (vis){
            fab.show();
        } else {
            fab.hide();
        }
    }

    public void SetMainCheckBoxVisible(boolean vis){mymenu.findItem(R.id.action_check_box).setVisible(vis); }
    public void SetMainCheckBoxListener(CheckBox.OnCheckedChangeListener listener){
        ((CheckBox)mymenu.findItem(R.id.action_check_box).getActionView()).setOnCheckedChangeListener(listener);
    }

    public void SetCheckedItem(int id){
        navigationView.setCheckedItem(id);
    }
}
