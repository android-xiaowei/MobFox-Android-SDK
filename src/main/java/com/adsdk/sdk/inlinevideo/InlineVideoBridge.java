package com.adsdk.sdk.inlinevideo;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by nabriski on 7/28/15.
 */
public class InlineVideoBridge {


   public interface Listener {

       public void onAdClicked(String href);

       public void onAdClosed();

       public void onAdFinished();

       public void onAdError(Exception e);

       public void onAdLoaded();

   }

   Context ctx;
   Listener listener;


   public InlineVideoBridge(Context ctx,Listener listener){
        this.ctx = ctx;
        this.listener = listener;
   }

    public void send(String json) {

        JSONObject action = null;
        try {
            action = new JSONObject(json);
            JSONArray keys = action.names();
            String key = keys.get(0).toString();
            String value = action.get(key).toString();

            if (key.equals("adLoaded")) {
                listener.onAdLoaded();
            }
            else if (key.equals("clickURL")) {
               listener.onAdClicked(value);
            }

            else if (key.equals("close")) {
                listener.onAdClosed();
            }

            else if (key.equals("finished")) {
                listener.onAdFinished();
            }

            else if(key.equals("error")){
                listener.onAdError(new Exception(value));
            }
            else{ //unknown key treat as error
                listener.onAdError(new Exception(value));
            }

        } catch (JSONException e) {
            listener.onAdError(e);
        }

    }
}
