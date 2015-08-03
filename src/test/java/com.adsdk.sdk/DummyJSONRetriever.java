package com.adsdk.sdk;

import com.adsdk.sdk.networking.JSONRetriever;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nabriski on 4/29/15.
 */
public class DummyJSONRetriever implements JSONRetriever{

    String lastURL;
    String json;

    public DummyJSONRetriever(String json){
        this.json = json;
    }

    @Override
    public void retrieve(String url, Listener listener) {

        this.lastURL = url;

        try {
            JSONObject obj = new JSONObject(json);
            listener.onFinish(null,obj);
        } catch (JSONException e) {
            listener.onFinish(e,null);
        }
    }

    public String getLastURL(){
        return this.lastURL;
    }

}
