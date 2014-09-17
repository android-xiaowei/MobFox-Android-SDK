
package com.adsdk.sdk.customevents;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import android.app.Activity;

public class FacebookFullscreen extends CustomEventFullscreen {

	private InterstitialAd interstitial;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			Class.forName("com.facebook.ads.InterstitialAd");
			Class.forName("com.facebook.ads.InterstitialAdListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		interstitial = new InterstitialAd(activity, adId);
		interstitial.setAdListener(createListener());
		interstitial.loadAd();

	}

	private InterstitialAdListener createListener() {
		return new InterstitialAdListener() {

			@Override
			public void onError(Ad arg0, AdError arg1) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void onAdLoaded(Ad arg0) {
				if (listener != null) {
					listener.onFullscreenLoaded(FacebookFullscreen.this);
				}
			}

			@Override
			public void onAdClicked(Ad arg0) {
				if (listener != null) { // TODO: Check listener methods
					listener.onFullscreenLeftApplication();
				}
			}

			@Override
			public void onInterstitialDisplayed(Ad arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}

			@Override
			public void onInterstitialDismissed(Ad arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (interstitial != null && interstitial.isAdLoaded()) {
			interstitial.show();
		}

	}

}
