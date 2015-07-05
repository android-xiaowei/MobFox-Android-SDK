package com.adsdk.sdk.networking;

import org.json.JSONObject;

/**
 * Created by nabriski on 4/29/15.
 */
public interface JSONRetriever {

    public interface Listener{
        public void onFinish(Exception e,JSONObject o);
    }

    public void retrieve(String url,Listener listener);
}
