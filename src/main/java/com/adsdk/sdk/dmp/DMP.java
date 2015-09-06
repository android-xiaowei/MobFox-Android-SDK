package com.adsdk.sdk.dmp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.adsdk.sdk.networking.JSONRetriever;
import com.adsdk.sdk.networking.JSONRetrieverImpl;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import java.util.List;

/**
 * Created by nabriski on 8/9/15.
 */
public class DMP  {

    protected static String LAST_UPDATE_FILE = "mobfox-dmp-last-update";
    protected static long DAY = 1000*60*60*24;
    protected static DMP instance = null;
    Context context;

    protected DMP(Context context) {
        this.context = context;
    }

    public static DMP getInstance(Context context){
        if(instance  ==null){
            instance = new DMP(context);
        }
        return instance;
    }

    protected void post(String androidID) throws Exception{

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities( mainIntent, 0);

        JSONObject postData = new JSONObject();
        postData.put("id",androidID);

        JSONArray apps = new JSONArray();
        for(ResolveInfo inf : pkgAppsList){
            apps.put(inf.activityInfo.packageName);
        }
        postData.put("apps",apps);

        JSONRetriever ret = new JSONRetrieverImpl();
        ret.post("http://dmp.starbolt.io/logger.json",postData,new JSONRetriever.Listener(){

            @Override
            public void onFinish(Exception e, JSONObject o) {
                android.util.Log.d("dmp.update","finished updating");
            }
        });

    }

    protected Calendar readLastUpdate(Context c){


        FileInputStream fis = null;
        try {
            fis =c.openFileInput(LAST_UPDATE_FILE);
        } catch (FileNotFoundException e) {
            android.util.Log.d("dmp.update","last update not found");
            return null;
        }

        BufferedReader bufReader =new BufferedReader(new InputStreamReader(fis));
        String str = null;
        long millisecs;
        try {
            str = bufReader.readLine();
            android.util.Log.d("dmp.update","last update read: "+str);
            if(str==null) return null;
            millisecs = Long.parseLong(str);
        } catch (Exception e) {
            return null;
        }
        finally{
            try {
                bufReader.close();
            } catch (IOException e) {

            }
        }

        Calendar lastUpdate = Calendar.getInstance();
        lastUpdate.setTimeInMillis(millisecs);

        return lastUpdate;

    };

    protected void writeLastUpdate(Calendar lastUpdate,Context c){

        FileOutputStream fos = null;

        try {
            fos = c.openFileOutput(LAST_UPDATE_FILE,Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            android.util.Log.d("dmp.update","writer - last update not found");
           return;
        }

        BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(fos));
        try {
            android.util.Log.d("dmp.update","write update: "+String.valueOf(lastUpdate.getTimeInMillis()));
            bufWriter.write(String.valueOf(lastUpdate.getTimeInMillis()));
            bufWriter.flush();

        } catch (IOException e) {
            android.util.Log.d("dmp.update","unable to write");
            return;
        }
        finally{
            try {
                bufWriter.close();
            } catch (IOException e) {
            }
        }

    }

    public void update(final Context c){

        Calendar lastUpdate = readLastUpdate(c);
        final Calendar now        = Calendar.getInstance();


        if(lastUpdate!=null &&
           ((now.getTimeInMillis() - lastUpdate.getTimeInMillis()) / DAY) < 30){
            return;
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    String id = adInfo.getId();
                    //String id = "96bd03b6-defc-4203-83d3-dc1c730801f7";
                    post(id);
                    writeLastUpdate(now,c);
                } catch (Exception e) {
                    android.util.Log.d("dmp.update",e.toString(),e);
                }
                return null;
            }

        };
        task.execute();

    }

}