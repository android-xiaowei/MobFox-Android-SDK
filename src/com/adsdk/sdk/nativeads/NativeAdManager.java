package com.adsdk.sdk.nativeads;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;
import com.adsdk.sdk.customevents.CustomEvent;
import com.adsdk.sdk.customevents.CustomEventNative;
import com.adsdk.sdk.customevents.CustomEventNative.CustomEventNativeListener;
import com.adsdk.sdk.customevents.CustomEventNativeFactory;

public class NativeAdManager implements CustomEventNativeListener {

	private NativeAd nativeAd;
	private CustomEventNative customEventNative;
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
						customEventNative = null;
						nativeAd = requestAd.sendRequest(NativeAdManager.this.getRequest());
						if (nativeAd != null) {
							if(!nativeAd.getCustomEvents().isEmpty()) {
								loadCustomEventNativeAd();
								if(customEventNative == null) { //failed to create custom event native ad
									if(nativeAd.isNativeAdValid()) {
										notifyAdLoaded(nativeAd);	
									} else {
										notifyAdFailed(); //both custom event and normal native ad failed
									}
								}
							} else {
								if(nativeAd.isNativeAdValid()) {
									notifyAdLoaded(nativeAd);	
								} else {
									notifyAdFailed();
								}
							}
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
	
	private void loadCustomEventNativeAd() {
		customEventNative = null;
		while (!nativeAd.getCustomEvents().isEmpty() && customEventNative == null) {
			try {
				CustomEvent event = nativeAd.getCustomEvents().get(0);
				nativeAd.getCustomEvents().remove(event);
				customEventNative = CustomEventNativeFactory.create(event.getClassName());
				customEventNative.createNativeAd(context, this, event.getOptionalParameter(), event.getPixelUrl());
			} catch (Exception e) {
				customEventNative = null;
				Log.d("Failed to create Custom Event Native Ad.");
			}
		}
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
			ad.prepareImpression(view);
			view.setOnClickListener(createOnNativeAdClickListener(ad));
		}
		return view;
	}

	private OnClickListener createOnNativeAdClickListener(final NativeAd ad) {
		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyAdClicked();
				ad.handleClick();
				if (ad.getClickUrl() != null && !ad.getClickUrl().equals("")) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ad.getClickUrl()));
					context.startActivity(intent);
				}

			}
		};
		return clickListener;
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

	@Override
	public void onCustomEventNativeFailed() {
		loadCustomEventNativeAd();
		if(customEventNative != null) {
			return;
		} else if (nativeAd.isNativeAdValid()) {
			notifyAdLoaded(nativeAd);
		} else {
			notifyAdFailed();
		}
	}

	@Override
	public void onCustomEventNativeLoaded(NativeAd customNativeAd) {
		notifyAdLoaded(customNativeAd);
	}

}
