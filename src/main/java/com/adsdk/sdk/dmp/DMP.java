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

import java.util.Calendar;

import java.util.List;
import java.util.Random;

/**
 * Created by nabriski on 8/9/15.
 */
public class DMP  {

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

    public void update(){

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    //String id = "96bd03b6-defc-4203-83d3-dc1c730801f7";
                    byte[] by = adInfo.getId().getBytes();
                    long seed = 0;
                    for (int i = 0; i < 8; i++)
                    {
                        seed += ((long) by[i] & 0xffL) << (8 * i);
                    }
                    Random generator = new Random(seed);
                    int updateDayOfMonth = generator.nextInt(28)+1;
                    Calendar now  = Calendar.getInstance();
                    int currentDayOfMonth = now.get(Calendar.DAY_OF_MONTH);

                    android.util.Log.d("dmp.update","days: "+updateDayOfMonth+","+currentDayOfMonth);
                    if(updateDayOfMonth == currentDayOfMonth){
                    //if(updateDayOfMonth == updateDayOfMonth){
                        post(adInfo.getId());
                        //post(id);
                    }

                } catch (Exception e) {
                    android.util.Log.d("dmp.update",e.toString(),e);
                }

                return null;
            }

        };
        task.execute();

    }
}