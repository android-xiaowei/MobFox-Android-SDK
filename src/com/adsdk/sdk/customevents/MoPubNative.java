
package com.adsdk.sdk.customevents;

import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.View;

import com.adsdk.sdk.nativeads.NativeAd;
import com.mopub.nativeads.MoPubNative.MoPubNativeListener;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeResponse;

public class MoPubNative extends CustomEventNative {

	@Override
	public void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			Class.forName("com.mopub.nativeads.MoPubNative.MoPubNativeListener");
			Class.forName("com.mopub.nativeads.NativeErrorCode");
			Class.forName("com.mopub.nativeads.NativeResponse");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		MoPubNativeListener moPubListener = createMoPubNativeListener();
		com.mopub.nativeads.MoPubNative moPubNative = new com.mopub.nativeads.MoPubNative(context, optionalParameters, moPubListener);
		moPubNative.makeRequest();
	}

	private MoPubNativeListener createMoPubNativeListener() {
		return new MoPubNativeListener() {

			@Override
			public void onNativeLoad(final NativeResponse response) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						setClickUrl(response.getClickDestinationUrl());
						if (response.getImpressionTrackers() != null) {
							for (String impressionTrackerUrl : response.getImpressionTrackers()) {
								addImpressionTracker(impressionTrackerUrl);
							}
						}

						addImageAsset(NativeAd.MAIN_IMAGE_ASSET, response.getMainImageUrl());
						addImageAsset(NativeAd.ICON_IMAGE_ASSET, response.getIconImageUrl());
						addTextAsset(NativeAd.CALL_TO_ACTION_TEXT_ASSET, response.getCallToAction());
						addTextAsset(NativeAd.DESCRIPTION_TEXT_ASSET, response.getSubtitle());
						addTextAsset(NativeAd.HEADLINE_TEXT_ASSET, response.getTitle());

						Map<String, Object> extras = response.getExtras();
						for (Entry<String, Object> entry : extras.entrySet()) {
							if (entry.getValue() != null && entry.getValue() instanceof String) {
								addExtraAsset(entry.getKey(), (String) entry.getValue());
							}
						}

						if (isNativeAdValid(MoPubNative.this)) {
							listener.onCustomEventNativeLoaded(MoPubNative.this);
						} else {
							listener.onCustomEventNativeFailed();
						}
					}
				});
				t.start();
			}

			@Override
			public void onNativeImpression(View arg0) {
			}

			@Override
			public void onNativeFail(NativeErrorCode arg0) {
				listener.onCustomEventNativeFailed();
			}

			@Override
			public void onNativeClick(View arg0) {
			}
		};
	}

}
