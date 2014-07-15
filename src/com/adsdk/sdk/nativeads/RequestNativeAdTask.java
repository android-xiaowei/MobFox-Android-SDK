
package com.adsdk.sdk.nativeads;

import android.content.Context;
import android.os.Handler;

import com.adsdk.sdk.Const;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.customevents.CustomEvent;
import com.adsdk.sdk.customevents.CustomEventNative;
import com.adsdk.sdk.customevents.CustomEventNative.CustomEventNativeListener;
import com.adsdk.sdk.customevents.CustomEventNativeFactory;

public class RequestNativeAdTask extends Thread implements CustomEventNativeListener {
	private NativeAd nativeAd;
	private CustomEventNative customEventNative;
	private NativeAdListener listener;
	private boolean reportedAvailability;

	private Context context;
	private NativeAdRequest request;

	private Handler handler;

	public RequestNativeAdTask(Context context, NativeAdRequest request, Handler handler, NativeAdListener listener) {
		this.context = context;
		this.request = request;
		this.handler = handler;
		this.listener = listener;
	}

	@Override
	public void run() {
		Log.d(Const.TAG, "starting request thread");
		final RequestNativeAd requestAd;
		requestAd = new RequestNativeAd();
		reportedAvailability = false;

		try {
			customEventNative = null;
			nativeAd = requestAd.sendRequest(request);
			if (nativeAd != null) {
				if (!nativeAd.getCustomEvents().isEmpty()) {
					loadCustomEventNativeAd();
					if (customEventNative == null) { // failed to create custom event native ad
						if (nativeAd.isNativeAdValid()) {
							notifyAdLoaded(nativeAd);
						} else {
							notifyAdFailed(); // both custom event and normal native ad failed
						}
					}
				} else {
					if (nativeAd.isNativeAdValid()) {
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

	private void notifyAdLoaded(final NativeAd ad) {
		if (listener != null && !reportedAvailability) {
			reportedAvailability = true;
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adLoaded(ad);
				}
			});
		}
	}

	private void notifyAdFailed() {
		if (listener != null && !reportedAvailability) {
			reportedAvailability = true;
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adFailedToLoad();
				}
			});
		}
	}

	@Override
	public void onCustomEventNativeFailed() {
		loadCustomEventNativeAd();
		if (customEventNative != null) {
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
