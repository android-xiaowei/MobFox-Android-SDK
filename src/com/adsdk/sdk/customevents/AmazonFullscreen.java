
package com.adsdk.sdk.customevents;

import android.app.Activity;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.InterstitialAd;

public class AmazonFullscreen extends CustomEventFullscreen {

	private InterstitialAd interstitial;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.amazon.device.ads.Ad");
			Class.forName("com.amazon.device.ads.AdError");
			Class.forName("com.amazon.device.ads.AdListener");
			Class.forName("com.amazon.device.ads.AdProperties");
			Class.forName("com.amazon.device.ads.AdRegistration");
			Class.forName("com.amazon.device.ads.InterstitialAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		AdRegistration.setAppKey(adId);
		interstitial = new InterstitialAd(activity);
		interstitial.setListener(createListener());
		interstitial.loadAd();

	}

	private AdListener createListener() {
		return new AdListener() {

			@Override
			public void onAdLoaded(Ad arg0, AdProperties arg1) {
				if (listener != null) {
					listener.onFullscreenLoaded(AmazonFullscreen.this);
				}
			}

			@Override
			public void onAdFailedToLoad(Ad arg0, AdError arg1) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void onAdExpanded(Ad arg0) {
				if (listener != null) { // TODO: Check listener methods
					listener.onFullscreenLeftApplication();
				}
			}

			@Override
			public void onAdDismissed(Ad arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}

			@Override
			public void onAdCollapsed(Ad arg0) {
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (interstitial != null) {
			if (interstitial.showAd()) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}
		}

	}

}
