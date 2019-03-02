package ru.mrsmile2114.ytmusic.utils;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class PageCheck extends AsyncTask<String,Void,Integer>  {
    private Consumer mConsumer;
    public  interface Consumer { void accept(Integer code); }

    public PageCheck(Consumer consumer){
        mConsumer=consumer;
    }

    @Override
    protected Integer doInBackground(String... params) {
        try {
            HttpsURLConnection connection =(HttpsURLConnection) new URL(params[0]).openConnection();
            connection.connect();
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer code) {
        mConsumer.accept(code);
    }
}
