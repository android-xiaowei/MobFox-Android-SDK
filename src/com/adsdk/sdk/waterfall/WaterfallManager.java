package com.adsdk.sdk.waterfall;

import android.util.Log;
import com.adsdk.sdk.networking.JSONRetriever;
import com.adsdk.sdk.networking.JSONRetrieverImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by nabriski on 4/28/15.
 */
public class WaterfallManager {

    private static final String URL = "http://static.starbolt.io/waterfalls2.json";

    private static WaterfallManager instance = null;
    private Map<String,Waterfall> waterfalls = new HashMap<String,Waterfall>();
    private static JSONRetriever retriever = new JSONRetrieverImpl();


    protected WaterfallManager() {

        Waterfall fallbackBannerWaterfall = new Waterfall();

        fallbackBannerWaterfall.add("banner",1);
        fallbackBannerWaterfall.add("nativeFormat",1);

        waterfalls.put("banner",fallbackBannerWaterfall);

        Waterfall fallbackInterstitialWaterfall = new Waterfall();
        fallbackBannerWaterfall.add("nativeFormat",1);
        fallbackBannerWaterfall.add("video",1);
        fallbackBannerWaterfall.add("banner",1);

        waterfalls.put("interstitial",fallbackInterstitialWaterfall);

        // get remote waterfalls
        retriever.retrieve(URL,new JSONRetriever.Listener(){
            @Override
            public void onFinish(Exception e, JSONObject obj) {

                Log.d("waterfall","json request returned.");

                if(e!=null) {
                    Log.e("waterfall","failed to retrieve waterfalls", e);
                    return;
                }

                JSONObject waterfallsObj = null;
                try {
                    waterfallsObj = obj.getJSONObject("waterfalls");
                    Iterator<String> iter = waterfallsObj.keys();
                    while(iter.hasNext()){
                        String k = iter.next();
                        JSONArray warr = waterfallsObj.getJSONArray(k);
                        Waterfall w = new Waterfall();

                        for (int i = 0; i < warr.length(); i++) {
                            JSONObject next = warr.getJSONObject(i);
                            w.add(next.getString("name"),next.getDouble("prob"));
                        }
                        Log.d("waterfall","putting: "+k+" , "+w.toString());
                        waterfalls.put(k,w);
                    }
                } catch (JSONException e1) {
                    Log.e("waterfall","error parsing waterfalls", e);
                }

            }
        });
    }

    public static WaterfallManager getInstance() {
        if (instance == null) {
            instance = new WaterfallManager();
        }
        return instance;
    }

    public Waterfall getWaterfall(String displayType) {
        return new Waterfall(waterfalls.get(displayType));
    }

    public static void setRetriever(JSONRetriever retriever) {
        WaterfallManager.retriever = retriever;
    }
}
