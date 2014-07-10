package com.adsdk.sdk.nativeads;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;

public class NativeAdManager {

	private NativeAd nativeAd;
	private String publisherId;
	private boolean includeLocation = false;

	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private NativeAdListener listener;

	private Context context;
	private NativeAdRequest request;

	private String requestUrl;
	private Handler handler;

	private List<String> adTypes;

	public NativeAdManager(Context context, String requestUrl, boolean includeLocation, String publisherId, NativeAdListener listener, List<String> adTypes) {
		if ((publisherId == null) || (publisherId.length() == 0)) {
			Log.e("Publisher Id cannot be null or empty");
			throw new IllegalArgumentException("User Id cannot be null or empty");
		}
		this.context = context;
		this.requestUrl = requestUrl;
		this.includeLocation = includeLocation;
		this.publisherId = publisherId;
		this.listener = listener;
		this.adTypes = adTypes;
		handler = new Handler();
		Util.prepareAndroidAdId(context);
	}

	public void requestAd() {
			Thread requestThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d(Const.TAG, "starting request thread");
					final RequestNativeAd requestAd;
					requestAd = new RequestNativeAd();

					try {
						nativeAd = requestAd.sendRequest(NativeAdManager.this.getRequest());
						if (nativeAd != null) {
							notifyAdLoaded(nativeAd);
						} else {
							notifyAdFailed();
						}
					} catch (final Throwable e) {
						notifyAdFailed();
					}
					Log.d(Const.TAG, "finishing request thread");
				}

			});
			requestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(final Thread thread, final Throwable ex) {
					Log.e(Const.TAG, "Exception in request thread", ex);
				}
			});
			requestThread.start();
	}

	private NativeAdRequest getRequest() {
		if (this.request == null) {
			this.request = new NativeAdRequest();
			this.request.setAndroidAdId(Util.getAndroidAdId());
			this.request.setPublisherId(this.publisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(context));
			this.request.setUserAgent2(Util.buildUserAgent());
			Log.d(Const.TAG, "WebKit UserAgent:" + this.request.getUserAgent());
		}
		request.setRequestUrl(requestUrl);
		request.setAdTypes(adTypes);
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setAdTypes(adTypes);
		request.setKeywords(keywords);
		Location location = null;
		if (this.includeLocation)
			location = Util.getLocation(context);
		if (location != null) {
			Log.d(Const.TAG, "location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			this.request.setLatitude(location.getLatitude());
			this.request.setLongitude(location.getLongitude());
		} else {
			this.request.setLatitude(0.0);
			this.request.setLongitude(0.0);
		}
		return this.request;
	}

	public NativeAdView getNativeAdView(NativeAd ad, NativeViewBinder binder) {
		NativeAdView view = new NativeAdView(context, ad, binder, listener);
		if (ad != null) {
			view.setOnClickListener(createOnNativeAdClickListener(ad.getClickUrl()));
		}
		return view;
	}

	private OnClickListener createOnNativeAdClickListener(final String clickUrl) {
		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyAdClicked();
				if (clickUrl != null && !clickUrl.equals("")) {
					new LoadUrlTask().execute(clickUrl);
				}

			}
		};
		return clickListener;
	}
	
	
	private class LoadUrlTask extends AsyncTask<String, Void, String> {

		String userAgent;

		public LoadUrlTask(){
			userAgent = Util.getDefaultUserAgentString(context);
		}

		@Override
		protected String doInBackground(String... urls) {
			String loadingUrl = urls[0];
			URL url = null;
			try {
				url = new URL(loadingUrl);
			} catch (MalformedURLException e) {
				return (loadingUrl != null) ? loadingUrl : "";
			}
			Log.d("Checking URL redirect:" + loadingUrl);

			int statusCode = -1;
			HttpURLConnection connection = null;
			String nextLocation = url.toString();

			Set<String> redirectLocations = new HashSet<String>();
			redirectLocations.add(nextLocation);

			try {
				do {
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("User-Agent",
							userAgent);
					connection.setInstanceFollowRedirects(false);

					statusCode = connection.getResponseCode();
					if (statusCode == HttpStatus.SC_OK) {
						connection.disconnect();
						break;
					} else {
						nextLocation = connection.getHeaderField("location");
						connection.disconnect();
						if (!redirectLocations.add(nextLocation)) {
							Log.d("URL redirect cycle detected");
							return "";
						}

						url = new URL(nextLocation);
					}
				} while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
						|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY
						|| statusCode == HttpStatus.SC_TEMPORARY_REDIRECT
						|| statusCode == HttpStatus.SC_SEE_OTHER);
			} catch (IOException e) {
				return (nextLocation != null) ? nextLocation : "";
			} finally {
				if (connection != null)
					connection.disconnect();
			}

			return nextLocation;
		}

		@Override
		protected void onPostExecute(String url) {
			if (url == null || url.equals("")) {
				url = "about:blank";
				return;
			}
			
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}


	private void notifyAdLoaded(final NativeAd ad) {
		if (listener != null) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adLoaded(ad);
				}
			});
		}
	}

	private void notifyAdFailed() {
		if (listener != null) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adFailedToLoad();
				}
			});
		}
	}

	private void notifyAdClicked() {
		if (listener != null) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adClicked();
				}
			});
		}
	}

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

}
