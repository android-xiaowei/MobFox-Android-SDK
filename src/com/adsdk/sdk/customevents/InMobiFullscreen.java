package com.adsdk.sdk.customevents;

import java.util.Map;

import android.app.Activity;

import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMInterstitial;
import com.inmobi.monetization.IMInterstitialListener;

public class InMobiFullscreen extends CustomEventFullscreen {
	
	private IMInterstitial interstitial;
	private static boolean isInitialized;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.inmobi.commons.InMobi");
			Class.forName("com.inmobi.monetization.IMErrorCode");
			Class.forName("com.inmobi.monetization.IMInterstitial");
			Class.forName("com.inmobi.monetization.IMInterstitialListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		if(!isInitialized) {
			InMobi.initialize(activity, optionalParameters);
			isInitialized = true;
		}
		interstitial = new IMInterstitial(activity, optionalParameters);
		interstitial.setIMInterstitialListener(createListener());
		interstitial.loadInterstitial();
	}

	private IMInterstitialListener createListener() {
		return new IMInterstitialListener() {
			
			@Override
			public void onShowInterstitialScreen(IMInterstitial arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}
			
			@Override
			public void onLeaveApplication(IMInterstitial arg0) {
			}
			
			@Override
			public void onInterstitialLoaded(IMInterstitial arg0) {
				if (listener != null) {
					listener.onFullscreenLoaded(InMobiFullscreen.this);
				}
			}
			
			@Override
			public void onInterstitialInteraction(IMInterstitial arg0, Map<String, String> arg1) {
				if(listener != null) {
					listener.onFullscreenLeftApplication();
				}
			}
			
			@Override
			public void onInterstitialFailed(IMInterstitial arg0, IMErrorCode arg1) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}
			
			@Override
			public void onDismissInterstitialScreen(IMInterstitial arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (interstitial.getState() ==IMInterstitial.State.READY) {			
			interstitial.show();
		}
	}

}
