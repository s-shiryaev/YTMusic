package ru.mrsmile2114.ytmusic;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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

    private final Handler handlerDownload = new HandlerOnDownloadCancelled(this);
    private DownloadFinishedReceiver onDownloadComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        StartContentObserver();

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
        mProgressDialog= new ProgressDialog(    this);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("Please Wait...");

        onDownloadComplete = new DownloadFinishedReceiver(){//create a descendant of a class DownloadFinishedReceiver
            @Override
            public void onReceive(final Context context, Intent intent) {
                super.onReceive(context,intent);
                RemoveItemByDownloadId(String.valueOf(intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID)));
            }

            @Override
            protected void removeTempOnFailure(Context con, long downloadId) {
                super.removeTempOnFailure(con, downloadId);
                Snackbar.make(findViewById(R.id.sample_content_fragment),
                        "Could not get link. Please try again.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

        };
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        fab.setImageResource(R.drawable.ic_menu_search);
        GoToFragment(DownloadStartFragment.class);//go to start fragment
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(onDownloadComplete);
        super.onDestroy();
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

    @Override
    public void RemoveItemByDownloadId(String downloadId) {
        DownloadsItems.DownloadsItem item = DownloadsItems.getITEMbyDownloadId(downloadId);
        if (item!=null){
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            manager.remove(Long.parseLong(downloadId));
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("FRAGMENT_DOWNLOADS_MANAGE");
            if (fragment == null) {
                fragment = new DownloadsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("FRAGMENT_DOWNLOADS_MANAGE");
                transaction.commit();
            }
            ((DownloadsFragment) fragment).RemoveItemByDownloadId(downloadId);
        }
    }

    public void SetMainFabImage(int imageResource){ fab.setImageResource(imageResource); }
    public void SetMainFabVisible(boolean visible){
        if (visible){
            fab.show();
        } else {
            fab.hide();
        }
    }

    public void SetMainCheckBoxVisible(boolean vis){mymenu.findItem(R.id.action_check_box).setVisible(vis); }
    public void SetMainCheckBoxListener(CheckBox.OnCheckedChangeListener listener){
        ((CheckBox)mymenu.findItem(R.id.action_check_box).getActionView()).setOnCheckedChangeListener(listener);
    }

    public void SetMainProgressDialogVisible(boolean visible){
        if (visible){
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onListFragmentInteraction(DownloadsItems.DownloadsItem item) {

    }

    public long downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName, boolean hide) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        if (hide) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);
        } else
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS+"/YTMusic", fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        return manager.enqueue(request);
    }

    public void StartAsyncYtExtraction(String url){
        new YTExtractor(this).extract(url, true, true);
    }

    @Override
    public void SetTitle(String title) { setTitle(title); }

    public void StartContentObserver(){
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"),
                true, new ContentObserver(null) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        if (uri.toString().matches(".*\\d+$")) {
                            String changedId;
                            changedId=uri.getLastPathSegment();
                            DownloadsItems.DownloadsItem item = DownloadsItems.getITEMbyDownloadId(changedId);
                            if (item!=null){
                                Log.d("DEBUG", "onChange: " + uri.toString() + " " + changedId );
                                try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                                    if (cursor != null && cursor.moveToFirst()) {
                                        Log.d("DEBUG", "onChange: running");
                                    } else {
                                        Log.w("DEBUG", "onChange: cancel");
                                        Message m = Message.obtain();
                                        m.obj = changedId;
                                        handlerDownload.sendMessage(m);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public boolean HaveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {
                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }
}
