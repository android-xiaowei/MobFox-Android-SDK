package com.adsdk.sdk.customevents;

import android.content.Context;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Rating;

public class FacebookNative extends CustomEventNative {

	private NativeAd facebookNative;

	@Override
	public void createNativeAd(final Context context, CustomEventNativeListener listener, final String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			Class.forName("com.facebook.ads.AdListener");
			Class.forName("com.facebook.ads.NativeAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		facebookNative = new NativeAd(context, optionalParameters);
		facebookNative.setAdListener(createListener());
		facebookNative.loadAd();
	}

	private AdListener createListener() {
		return new AdListener() {

			@Override
			public void onError(Ad arg0, AdError arg1) {
				if (listener != null) {
					listener.onCustomEventNativeFailed();
				}
			}

			@Override
			public void onAdLoaded(final Ad ad) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						if (!facebookNative.equals(ad) || !facebookNative.isAdLoaded()) {
							if (listener != null) {
								listener.onCustomEventNativeFailed();
							}
							return;
						}
						addTextAsset(HEADLINE_TEXT_ASSET, facebookNative.getAdTitle());
						addTextAsset(DESCRIPTION_TEXT_ASSET, facebookNative.getAdBody());
						addTextAsset(CALL_TO_ACTION_TEXT_ASSET, facebookNative.getAdCallToAction());
						addTextAsset(RATING_TEXT_ASSET, readRating(facebookNative.getAdStarRating()));
						addTextAsset("socialContextForAd", facebookNative.getAdSocialContext());

						addImageAsset(ICON_IMAGE_ASSET, facebookNative.getAdIcon().getUrl());
						addImageAsset(MAIN_IMAGE_ASSET, facebookNative.getAdCoverImage().getUrl());

						if (isNativeAdValid(FacebookNative.this)) {
							if (listener != null) {
								listener.onCustomEventNativeLoaded(FacebookNative.this);
							}
						} else {
							{
								listener.onCustomEventNativeFailed();
							}
						}

					}
				});
				t.start();
			}

			@Override
			public void onAdClicked(Ad arg0) {
			}
		};
	}

	private String readRating(Rating rating) {
		if (rating != null) {
			int stars = (int) Math.round(5 * rating.getValue() / rating.getScale());
			return Integer.toString(stars);
		}
		return null;
	}

	@Override
	public void prepareImpression(View view) {
		facebookNative.registerViewForInteraction(view);
	}

}
