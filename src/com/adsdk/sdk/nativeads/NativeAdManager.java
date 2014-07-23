package com.adsdk.sdk.nativeads;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;

public class NativeAdManager {

	private String publisherId;
	private boolean includeLocation = false;
	private String requestUrl;
	private NativeAdRequest request;
	
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private NativeAdListener listener;

	private Context context;
	
	private Handler handler;
	ExecutorService executor = Executors.newSingleThreadExecutor();

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
		request = getRequest();
			Thread requestThread = new RequestNativeAdTask(context, request, handler, listener);
			requestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(final Thread thread, final Throwable ex) {
					Log.e(Const.TAG, "Exception in request thread", ex);
				}
			});
			
			executor.submit(requestThread);
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
		return view;
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
