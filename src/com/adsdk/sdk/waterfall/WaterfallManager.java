package com.adsdk.sdk.waterfall;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.nativeformats.creative.Creative;
import com.adsdk.sdk.video.ResourceManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Created by nabriski on 4/28/15.
 */
public class WaterfallManager {

    private static final String BASE_URL = "http://static.starbolt.io/waterfalls.json";

    private static WaterfallManager instance = null;
    private Map<String,Waterfall> waterfalls = new HashMap<String,Waterfall>();

    protected WaterfallManager() {

        Waterfall fallbackBannerWaterfall = new Waterfall();
        fallbackBannerWaterfall.add("banner",1);
        fallbackBannerWaterfall.add("nativeFormat",1);
        waterfalls.put("banner",fallbackBannerWaterfall);

        Waterfall fallbackInterstitialWaterfall = new Waterfall();
        fallbackBannerWaterfall.add("video",1);
        fallbackBannerWaterfall.add("banner",1);
        fallbackBannerWaterfall.add("nativeFormat",1);
        waterfalls.put("interstitial",fallbackInterstitialWaterfall);

        // get remote waterfalls
        Thread requestThread = new Thread(new Runnable() {

            @Override
            public void run() {
                AndroidHttpClient client = null;
                try {
                    client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
                    HttpGet request = new HttpGet();
                    request.setHeader("User-Agent", System.getProperty("http.agent"));
                    request.setURI(new URI(BASE_URL));
                    HttpResponse response = client.execute(request);
                    StatusLine statusLine = response.getStatusLine();

                    int statusCode = statusLine.getStatusCode();
                    if(statusCode!=200){
                        Log.d("Failed to load creatives");
                    }

                    StringBuilder builder = new StringBuilder();
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    String responseString = builder.toString();
                    JSONObject responseJSON = new JSONObject(responseString);

                    JSONObject waterfallsObj = responseJSON.getJSONObject("waterfalls");
                    Iterator<String> iter = waterfallsObj.keys();
                    while(iter.hasNext()){
                        String k = iter.next();
                        JSONArray warr = waterfallsObj.getJSONArray(k);
                        Waterfall w = new Waterfall();

                        for (int i = 0; i < warr.length(); i++) {
                            JSONObject next = warr.getJSONObject(i);
                            w.add(next.getString("name"),next.getDouble("prob"));
                        }

                        waterfalls.put(k,w);
                    }


                } catch (Exception e) {
                    Log.d("Failed to load waterfalls with exception: ", e);
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }

            }
        });
        requestThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.d("Failed to load creatives with exception: ", ex);
            }
        });
        requestThread.start();

    }

    public static WaterfallManager getInstance() {
        if (instance == null) {
            instance = new WaterfallManager();
        }
        return instance;
    }

    public Waterfall getWaterfall(String displayType) {
        return waterfalls.get(displayType);
    }

    protected void addResourceCreative(String name , String template,int width, int height, double prob, Stack<Creative> stack) {
        stack.push(new Creative(name, template, width, height, prob));
    }
}
