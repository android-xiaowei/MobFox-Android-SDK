package com.adsdk.sdk.nativeads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.RequestException;
import com.adsdk.sdk.customevents.CustomEvent;
import com.adsdk.sdk.customevents.CustomEventNative;
import com.adsdk.sdk.customevents.CustomEventNative.CustomEventNativeListener;
import com.adsdk.sdk.customevents.CustomEventNativeFactory;
import com.adsdk.sdk.nativeads.NativeAd.ImageAsset;
import com.adsdk.sdk.nativeads.NativeAd.Tracker;

public class RequestNativeAd implements CustomEventNativeListener {
	private NativeAd nativeAd;
	private CustomEventNative customEventNative;
	private Context context;
	private boolean reportedAvailability;
	private Handler handler;
	private String requestResultJson;
	private NativeAdListener listener;

	public void sendRequest(NativeAdRequest request, Handler handler, NativeAdListener listener, Context context) throws RequestException {
		this.handler = handler;
		this.listener = listener;
		this.context = context;
		customEventNative = null;
		String url = request.toString();
		reportedAvailability = false;
		Log.d("Ad RequestPerform HTTP Get Url: " + url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setSoTimeout(client.getParams(), Const.SOCKET_TIMEOUT);
		HttpConnectionParams.setConnectionTimeout(client.getParams(), Const.CONNECTION_TIMEOUT);
		HttpProtocolParams.setUserAgent(client.getParams(), request.getUserAgent());
		HttpGet get = new HttpGet(url);
		get.setHeader("User-Agent", System.getProperty("http.agent"));
		HttpResponse response;
		try {
			response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				nativeAd = parse(response.getEntity().getContent(), response.getAllHeaders());
				if (!nativeAd.getCustomEvents().isEmpty()) {
					loadCustomEventNativeAd();
					if (customEventNative == null) { // failed to create custom event native ad
						loadOriginalNativeAd();
					}
				} else {
					loadOriginalNativeAd();
				}
			} else {
				notifyAdFailed();
				throw new RequestException("Server Error. Response code:" + responseCode);
			}
		} catch (Throwable t) {
			notifyAdFailed();
			throw new RequestException("Error in HTTP request", t);
		}
		
		while(!reportedAvailability) { //prevent deallocation of RequestNativeAd until loading failed or successful load was reported
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				notifyAdFailed();
			}
		}
		
		
	}

	private List<CustomEvent> getCustomEvents(Header[] headers) {
		List<CustomEvent> customEvents = new ArrayList<CustomEvent>();
		if (headers == null) {
			return customEvents;
		}

		for (int i = 0; i < headers.length; i++) {
			if (headers[i].getName().startsWith("X-CustomEvent")) {
				String json = headers[i].getValue();
				JSONObject customEventObject;
				try {
					customEventObject = new JSONObject(json);
					String className = customEventObject.getString("class");
					String parameter = customEventObject.getString("parameter");
					String pixel = customEventObject.getString("pixel");
					CustomEvent event = new CustomEvent(className, parameter, pixel);
					customEvents.add(event);
				} catch (JSONException e) {
					Log.e("Cannot parse json with custom event: " + headers[i].getName());
				}

			}
		}

		return customEvents;
	}

	protected NativeAd parse(final InputStream inputStream, Header[] headers) throws RequestException {

		final NativeAd response = new NativeAd();

		try {
			List<CustomEvent> customEvents = this.getCustomEvents(headers);
			response.setCustomEvents(customEvents);

			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			requestResultJson = sb.toString();

		} catch (UnsupportedEncodingException e) {
			throw new RequestException("Cannot parse Response", e);
		} catch (IOException e) {
			throw new RequestException("Cannot parse Response", e);
		}
		return response;
	}

	private void loadOriginalNativeAd() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				boolean valid = true;
				try {

					JSONObject mainObject = new JSONObject(requestResultJson);
					JSONObject imageAssetsObject = mainObject.optJSONObject("imageassets");
					if (imageAssetsObject != null) {
						@SuppressWarnings("unchecked")
						Iterator<String> keys = imageAssetsObject.keys();

						while (keys.hasNext()) {
							String type = keys.next();
							JSONObject assetObject = imageAssetsObject.getJSONObject(type);
							String url = assetObject.getString("url");
							int width = assetObject.getInt("width");
							int height = assetObject.getInt("height");
							ImageAsset asset = new ImageAsset(url, width, height);
							nativeAd.addImageAsset(type, asset);
						}
					}

					JSONObject textAssetsObject = mainObject.optJSONObject("textassets");
					if (textAssetsObject != null) {
						@SuppressWarnings("unchecked")
						Iterator<String> keys = textAssetsObject.keys();
						while (keys.hasNext()) {
							String type = keys.next();
							String text = textAssetsObject.getString(type);
							nativeAd.addTextAsset(type, text);
						}
					}

					nativeAd.setClickUrl(mainObject.optString("click_url", null));

					JSONArray trackersArray = mainObject.optJSONArray("trackers");
					if (trackersArray != null) {
						for (int i = 0; i < trackersArray.length(); i++) {
							JSONObject trackerObject = trackersArray.optJSONObject(i);
							if (trackerObject != null) {
								String type = trackerObject.getString("type");
								String url = trackerObject.getString("url");
								Tracker tracker = new Tracker(type, url);
								nativeAd.getTrackers().add(tracker);
							}
						}
					}
				} catch (JSONException e) {
					valid = false;
				}

				if (valid) {
					notifyAdLoaded(nativeAd);
				} else {
					notifyAdFailed();
				}
			}
		});
		t.start();
	}

	private void loadCustomEventNativeAd() {
		customEventNative = null;
		while (!nativeAd.getCustomEvents().isEmpty() && customEventNative == null) {

			try {
				final CustomEvent event = nativeAd.getCustomEvents().get(0);
				nativeAd.getCustomEvents().remove(event);
				customEventNative = CustomEventNativeFactory.create(event.getClassName());
				handler.post(new Runnable() {

					@Override
					public void run() {
						customEventNative.createNativeAd(context, RequestNativeAd.this, event.getOptionalParameter(), event.getPixelUrl());
					}
				});
			} catch (Exception e) {
				customEventNative = null;
				Log.d("Failed to create Custom Event Native Ad.");
			}
		}
	}

	private void notifyAdLoaded(final NativeAd ad) {
		if (listener != null && !reportedAvailability) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adLoaded(ad);
				}
			});
		}
		if (customEventNative != null) {
			customEventNative.unregisterListener();
		}
		reportedAvailability = true;
	}

	private void notifyAdFailed() {
		if (listener != null && !reportedAvailability) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adFailedToLoad();
				}
			});
		}
		if (customEventNative != null) {
			customEventNative.unregisterListener();
		}
		reportedAvailability = true;
	}

	@Override
	public void onCustomEventNativeFailed() {
		loadCustomEventNativeAd();
		if (customEventNative != null) {
			return;
		}
		loadOriginalNativeAd();
	}

	@Override
	public void onCustomEventNativeLoaded(NativeAd customNativeAd) {
		notifyAdLoaded(customNativeAd);
	}

}
