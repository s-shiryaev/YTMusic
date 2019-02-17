package ru.mrsmile2114.ytmusic.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import ru.mrsmile2114.ytmusic.MainActivity;

public class HandlerOnDownloadCancelled extends Handler {
    private final WeakReference<MainActivity> mActivity;


    public HandlerOnDownloadCancelled(MainActivity activity) {
        mActivity = new WeakReference<MainActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        MainActivity activity = mActivity.get();
        if (activity != null) {
            mActivity.get().RemoveItemByDownloadId((String) msg.obj,false);
        }
    }
}
