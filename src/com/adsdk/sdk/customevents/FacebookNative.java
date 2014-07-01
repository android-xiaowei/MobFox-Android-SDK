package com.adsdk.sdk.customevents;

import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAd.Rating;

public class FacebookNative extends CustomEventNative {
	
	private NativeAd facebookNative;

	@Override
	public void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			Class.forName("com.facebook.ads.AdListener");
			Class.forName("com.facebook.ads.NativeAd");
			Class.forName("com.facebook.ads.NativeAd.Rating");
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
				listener.onCustomEventNativeFailed();
			}
			
			@Override
			public void onAdLoaded(Ad ad) {
				if (!facebookNative.equals(ad) || !facebookNative.isAdLoaded()) {
					listener.onCustomEventNativeFailed();
		            return;
		        }
				addTextAsset(HEADLINE_TEXT_ASSET, facebookNative.getAdTitle());
				addTextAsset(DESCRIPTION_TEXT_ASSET,facebookNative.getAdBody());
				addTextAsset(CALL_TO_ACTION_TEXT_ASSET, facebookNative.getAdCallToAction());
				addTextAsset(RATING_TEXT_ASSET,readRating(facebookNative.getAdStarRating()));
				addTextAsset("socialContextForAd",facebookNative.getAdSocialContext());
				
				addImageAsset(ICON_IMAGE_ASSET, facebookNative.getAdIcon().getUrl());
				addImageAsset(MAIN_IMAGE_ASSET, facebookNative.getAdCoverImage().getUrl());
				
				if (isNativeAdValid(FacebookNative.this)) {
					listener.onCustomEventNativeLoaded(FacebookNative.this);
				} else {
					listener.onCustomEventNativeFailed();
				}
			}
			

			@Override
			public void onAdClicked(Ad arg0) {
			}
		};
	}

	private String readRating(Rating rating) {
		if(rating != null) {
			int stars = (int) Math.round(5 * rating.getValue()/rating.getScale());
			return Integer.toString(stars);
		}
		return null;
	}
	
	public void handleClick() {
		facebookNative.handleClick();
	};
	
	@Override
	public void handleImpression() {
		facebookNative.logImpression();
	}
}
