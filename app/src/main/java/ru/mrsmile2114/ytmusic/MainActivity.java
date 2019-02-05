package ru.mrsmile2114.ytmusic;

import android.app.ProgressDialog;
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

import ru.mrsmile2114.ytmusic.dummy.DownloadsItems;
import ru.mrsmile2114.ytmusic.dummy.PlaylistItems.PlaylistItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PlaylistItemsFragment.OnListFragmentInteractionListener,
        DownloadStartFragment.OnFragmentInteractionListener,
        DownloadsFragment.OnListFragmentInteractionListener {

    private FloatingActionButton fab;
    private NavigationView navigationView;
    private Menu mymenu;
    private ProgressDialog mProgressDialog;

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
        mProgressDialog= new ProgressDialog(this);
        mProgressDialog.setTitle("Please Wait...");
        fab.setImageResource(R.drawable.ic_menu_search);
        GoToFragment(DownloadStartFragment.class);//go to start fragment
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
        mymenu.findItem(R.id.action_check_box).setVisible(false);
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
            //System.out.println("OPT SELECTED");
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
            GoToFragment(DownloadStartFragment.class);
        } else if (id == R.id.nav_manage) {
            GoToFragment(DownloadsFragment.class);
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

    public void GoToFragment(Class fragmentclass){//method of transition to the desired fragment
        Fragment fragment;
        if (fragmentclass==DownloadStartFragment.class){//for DownloadStartFragment
            fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOAD_START");
            if (fragment==null) {
                fragment = new DownloadStartFragment();
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOAD_START");
            transaction.addToBackStack("FRAGMENT_DOWNLOAD_START");
            transaction.commit();
            getSupportFragmentManager().executePendingTransactions();
            navigationView.setCheckedItem(R.id.nav_download);
        } else if (fragmentclass==DownloadsFragment.class){//for DownloadsFragment
            fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOADS_MANAGE");
            if (fragment==null){
                fragment = new DownloadsFragment();
            }
            if (fragment.isVisible()){
                ((DownloadsFragment)fragment).RefreshRecyclerView();
            }else{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment, "FRAGMENT_DOWNLOADS_MANAGE");
                transaction.addToBackStack("FRAGMENT_DOWNLOADS_MANAGE");
                transaction.commit();
                getSupportFragmentManager().executePendingTransactions();
            }
            navigationView.setCheckedItem(R.id.nav_manage);
        } else if (fragmentclass==PlaylistItemsFragment.class){//for PlaylistItemsFragment
            fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_PLAYLIST_ITEMS");
            if (fragment==null){
                fragment = new PlaylistItemsFragment();
            }
            if (fragment.isVisible()){
                ((PlaylistItemsFragment)fragment).RefreshRecyclerView();
            }else{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment, "FRAGMENT_PLAYLIST_ITEMS");
                transaction.addToBackStack("FRAGMENT_PLAYLIST_ITEMS");
                transaction.commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        }
    }

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

    public void setMainProgressDialogVisible(boolean visible){
        if (visible){
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onListFragmentInteraction(DownloadsItems.DownloadsItem item) {

    }
}
