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
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;

import com.adsdk.sdk.video.ResourceManager;


/**
 * Created by itamar on 07/04/15.
 */

public class CreativesManager {

	private static final String BASE_URL = "http://static.starbolt.io/creatives20.json";

	private static CreativesManager instance = null;
	private Stack<Creative> creatives = new Stack<Creative>();

	protected CreativesManager(final Context ctx, final String publicationId) {

		// add fallback creatives
        String libs = ResourceManager.getStringResource(ctx, "libs.js");

        addResourceCreative("fallback_320x480.mustache",ResourceManager.getStringResource(ctx, "fallback_320x480.mustache"), 320, 480, 0, creatives);
		addResourceCreative("fallback_320x50.mustache",ResourceManager.getStringResource(ctx, "fallback_320x50.mustache"), 320, 50, 0, creatives);

		// get remote creatives
		Thread requestThread = new Thread(new Runnable() {

			@Override
			public void run() {
				AndroidHttpClient client = null;
				try {
					client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
					HttpGet request = new HttpGet();
					request.setHeader("User-Agent", System.getProperty("http.agent"));

					request.setURI(new URI(BASE_URL+"?p"+publicationId));

					HttpResponse response = client.execute(request);
					StatusLine statusLine = response.getStatusLine();

					int statusCode = statusLine.getStatusCode();
					if (statusCode == 200) {
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
						Log.d("creatives loaded: ");
						JSONArray creativeArr = responseJSON.getJSONArray("creatives");
						for (int i = 0; i < creativeArr.length(); i++) {
							JSONObject c = creativeArr.getJSONObject(i);
							Log.d(c.getString("name"));
							creatives.push(new Creative(c.getString("name"), c.getString("template"), c.getInt("width"), c.getInt("height"), c.getDouble("prob")));
						}
						Log.d("creatives ready");

					} else {
						Log.d("Failed to load creatives");
					}

				} catch (Exception e) {
					Log.d("Failed to load creatives with exception: ", e);
				} finally {
					if (client != null) {
						client.close();
					}
				}

			}
		});
		requestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.d("Failed to load creatives with exception: ", ex);
			}
		});
		requestThread.start();

	}

	public static CreativesManager getInstance(Context ctx,String publicationId) {
		if (instance == null) {
			instance = new CreativesManager(ctx,publicationId);
		}
		return instance;
	}

	public Creative getCreative(int width, int height) {

		Log.v("width: " + width + ", height: " + height);
		Log.v("num creatives: " + creatives.size());

		List<Creative> filtered = new ArrayList<Creative>();

		for (Creative c : creatives) {
			Log.v("creative: " + c.getName());
			if (c.width == width && c.height == height)
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

	protected void addResourceCreative(String name , String template,int width, int height, double prob, Stack<Creative> stack) {
		stack.push(new Creative(name, template, width, height, prob));
	}

}
