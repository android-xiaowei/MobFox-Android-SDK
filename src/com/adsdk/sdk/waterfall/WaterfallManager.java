package com.adsdk.sdk.waterfall;

import com.adsdk.sdk.Log;
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

    private static final String BASE_URL = "http://sdk.starbolt.io/waterfalls.json";

    private static WaterfallManager instance = null;
    private Map<String,Waterfall> waterfalls = new HashMap<String,Waterfall>();
    private static JSONRetriever retriever = new JSONRetrieverImpl();

    protected WaterfallManager(String publisherId) {

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

        retriever.retrieve(BASE_URL+"?p="+publisherId,new JSONRetriever.Listener(){
            @Override
            public void onFinish(Exception e, JSONObject obj) {


                if(e!=null) {
                    Log.e("waterfall failed to retrieve waterfalls", e);
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
                        Log.d("waterfall putting: "+k+" , "+w.toString());
                        waterfalls.put(k,w);
                    }
                } catch (JSONException e1) {
                    Log.e("waterfall error parsing waterfalls", e);
                }


            }
        });
    }

    public static WaterfallManager getInstance(String publisherID) {
        if (instance == null) {
            instance = new WaterfallManager(publisherID);
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
