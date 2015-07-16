package com.adsdk.sdk.nativeformats.creative;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import com.adsdk.sdk.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.adsdk.sdk.networking.JSONRetriever;
import com.adsdk.sdk.networking.JSONRetrieverImpl;
import com.adsdk.sdk.utils.UserAgent;
import com.adsdk.sdk.video.ResourceManager;


/**
 * Created by itamar on 07/04/15.
 */

public class CreativesManager {


	private static final String BASE_URL = "http://static.starbolt.io/creatives.new2.json";

	private static CreativesManager instance = null;
	private Stack<Creative> creatives = new Stack<Creative>();
    private static JSONRetriever retriever = new JSONRetrieverImpl();

	protected CreativesManager(final Context ctx, final String publicationId) {

		// add fallback creatives
        if(ctx!=null) {
            addResourceCreative("fallback_block.mustache", false, "block", ResourceManager.getStringResource(ctx, "fallback_block.mustache"), 0, creatives);
            addResourceCreative("fallback_stripe.mustache", false, "stripe", ResourceManager.getStringResource(ctx, "fallback_stripe.mustache"), 0, creatives);
        }
        //get remote creatives
        retriever.retrieve(BASE_URL+"?p="+publicationId,new JSONRetriever.Listener(){

            @Override
            public void onFinish(Exception e, JSONObject o) {
                if(e!=null){
                    Log.d("Failed to load creatives",e);
                    return;
                }

                JSONArray creativeArr = null;
                try {
                    creativeArr = o.getJSONArray("creatives");
                    for (int i = 0; i < creativeArr.length(); i++) {
                        JSONObject c = creativeArr.getJSONObject(i);
                        Log.d(c.getString("name"));
                        creatives.push(new Creative(c.getString("name"), c.getBoolean("webgl"), c.getString("type"), c.getString("template"), c.getDouble("prob")));
                    }
                } catch (JSONException e1) {
                    Log.d("Failed to parse creatives",e1);
                }
            }
        });

	}

	public static CreativesManager getInstance(Context ctx,String publicationId) {
		if (instance == null) {
			instance = new CreativesManager(ctx,publicationId);
		}
		return instance;
	}

	public Creative getCreative( String type,String webviewUserAgent ) {

        UserAgent ua = new UserAgent(webviewUserAgent);

		List<Creative> filtered = new ArrayList<Creative>();

		for (Creative c : creatives) {

            if(!c.getType().equals(type)) continue;

            if(c.getWebgl() && (!ua.isChrome() || ua.getChromeVersion() < 36)) continue;

            filtered.add(c);
		}

		if (filtered.size() == 0)
			return null;

		double prob = Math.random(), agg = 0;

		Log.v("prob " + prob);
		for (Creative c : filtered) {
			Log.v("creative search: " + c.getName());
			Log.v("creative prob: " + c.getProb());
			if (c.getProb() == 0)
				continue;
			agg += c.getProb();
			Log.v("creative prob: " + c.getProb() + ", agg: " + agg);
			if (agg >= prob) {
				return c;
			}
		}

		// for fallback creatives
		return filtered.get(0);

	}

    protected void addResourceCreative(String name, boolean webgl, String type, String template, double prob, Stack<Creative> stack) {
        stack.push(new Creative(name, webgl, type, template, prob));
    }

    public static void setRetriever(JSONRetriever retriever) {
        CreativesManager.retriever = retriever;
    }
}
