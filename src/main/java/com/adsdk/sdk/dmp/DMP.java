package com.adsdk.sdk.dmp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;

import com.adsdk.sdk.Util;
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
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;
import java.util.Random;

/**
 * Created by nabriski on 8/9/15.
 */
public class DMP  {


    public static String NEXT_BUNDLE_FILE = "mobfox-dmp-next-bundle";
    public static String BUNDLE_FILE = "mobfox-dmp-bundle";
    protected static long DAY = 1000*60*60*24;
    protected static DMP instance = null;
    List<int[]> data = new ArrayList<int[]>();

    Context context;

    protected DMP(Context context) {

        this.context = context;

        int [] arr1 = {46,41,69,98,40,47,90,83,46,41,124,83,39,45,86,87,59};
        data.add(arr1);

        int [] arr2 = {56,57,84,64,48,5,95,70,44,34,69,115,42,56,88,68,32,56,88,87,58};
        data.add(arr2);

        int[] arr3 = {40,47,69,91,63,37,69,75,0,34,87,93};
        data.add(arr3);

        int[] arr4 = {57,45,82,89,40,43,84,124,40,33,84};
        data.add(arr4);
    }

    public static DMP getInstance(Context context){
        if(instance  == null){
            instance = new DMP(context);
        }
        return instance;
    }



    public void update(final Context c){

        String next = Util.read(c, DMP.NEXT_BUNDLE_FILE);
        Calendar bundleDate = null;
        if(next==null){
            bundleDate = Calendar.getInstance();
            Random generator = new Random();
            int i = generator.nextInt(7) + 1;
            bundleDate.add(Calendar.DAY_OF_YEAR,i);
            Util.write(c, DMP.NEXT_BUNDLE_FILE, String.valueOf(bundleDate.getTimeInMillis()));
            android.util.Log.d("dmp.bundle","created next bundle");
            return;
        }

        bundleDate = Calendar.getInstance();
        bundleDate.setTimeInMillis(Long.parseLong(next.trim()));

        if(Calendar.getInstance().before(bundleDate)) return;
        android.util.Log.d("dmp.bundle","bundle will update");

        bundleDate = Calendar.getInstance();
        Random generator = new Random();
        int i = generator.nextInt(30) + 1;
        bundleDate.add(Calendar.DAY_OF_YEAR,i);
        Util.write(c, DMP.NEXT_BUNDLE_FILE, String.valueOf(bundleDate.getTimeInMillis()));
        android.util.Log.d("dmp.bundle","reset next bundle");

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                AdvertisingIdClient.Info adInfo = null;
                try {
                    adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    String id = adInfo.getId();
                    //String id = "96bd03b6-defc-4203-83d3-dc1c730801f7";
                    Bundle b = new Bundle(id);
                    b.addData(c, data);
                    String s = b.toString();
                    Util.write(c,DMP.BUNDLE_FILE,s);
                    android.util.Log.d("dmp.bundle","bundle updated");
                } catch (Exception e) {
                    android.util.Log.d("dmp.update", e.toString(), e);
                }

                return null;

            };

        };
        task.execute();

    }

}