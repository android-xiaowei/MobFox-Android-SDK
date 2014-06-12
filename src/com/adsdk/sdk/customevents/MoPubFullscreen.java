package com.adsdk.sdk.customevents;

import android.app.Activity;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class MoPubFullscreen extends CustomEventFullscreen {
	
	private MoPubInterstitial interstitial;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.mopub.mobileads.MoPubErrorCode");
			Class.forName("com.mopub.mobileads.MoPubInterstitial");
			Class.forName("com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		
		interstitial = new MoPubInterstitial(activity, adId);
		interstitial.setInterstitialAdListener(createListener());
		interstitial.load();
		
	}

	private InterstitialAdListener createListener() {
		return new InterstitialAdListener() {
			
			@Override
			public void onInterstitialShown(MoPubInterstitial arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}
			
			@Override
			public void onInterstitialLoaded(MoPubInterstitial arg0) {
				if(listener != null) {
					listener.onFullscreenLoaded(MoPubFullscreen.this);
				}
			}
			
			@Override
			public void onInterstitialFailed(MoPubInterstitial arg0, MoPubErrorCode arg1) {
				if(listener != null) {
					listener.onFullscreenFailed();
				}
			}
			
			@Override
			public void onInterstitialDismissed(MoPubInterstitial arg0) {
				if(listener != null) {
					listener.onFullscreenClosed();
				}
			}
			
			@Override
			public void onInterstitialClicked(MoPubInterstitial arg0) {
				if(listener != null) {
					listener.onFullscreenLeftApplication();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if(interstitial != null && interstitial.isReady()) {
			interstitial.show();
		}
	}

}
