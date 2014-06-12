package com.adsdk.sdk.customevents;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import android.content.Context;

public class FacebookBanner extends CustomEventBanner {
	
	private AdView banner;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			Class.forName("com.facebook.ads.AdListener");
			Class.forName("com.facebook.ads.AdSize");
			Class.forName("com.facebook.ads.AdView");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		
		banner = new AdView(context, adId, AdSize.BANNER_320_50); //there is only one size for banner
		banner.setAdListener(createListener());
		banner.loadAd();
		
	}

	private AdListener createListener() {
		return new AdListener() {
			
			@Override
			public void onError(Ad arg0, AdError arg1) {
				if (listener != null) {
					listener.onBannerFailed();
				}
			}
			
			@Override
			public void onAdLoaded(Ad arg0) {
				reportImpression();
				if (listener != null) {
					listener.onBannerLoaded(banner);
				}
			}
			
			@Override
			public void onAdClicked(Ad arg0) {
				if (listener != null) {
					listener.onBannerExpanded();
				}
			}
		};
	}

}
