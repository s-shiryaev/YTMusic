package ru.mrsmile2114.ytmusic;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import java.lang.ref.WeakReference;

public class HandlerOnDownloadCancelled extends Handler {
    private final WeakReference<MainActivity> mActivity;


    public HandlerOnDownloadCancelled(MainActivity activity) {
        mActivity = new WeakReference<MainActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity activity = mActivity.get();
        if (activity != null) {
            mActivity.get().RemoveItemByDownloadId((String) msg.obj);
        }
    }
}
