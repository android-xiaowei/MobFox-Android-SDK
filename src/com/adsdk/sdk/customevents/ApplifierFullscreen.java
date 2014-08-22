package com.adsdk.sdk.customevents;

import android.app.Activity;

import com.unity3d.ads.android.IUnityAdsListener;
import com.unity3d.ads.android.UnityAds;

public class ApplifierFullscreen extends CustomEventFullscreen {
	private static boolean initialized;
	private boolean shouldReportAvailability;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		shouldReportAvailability = true;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.unity3d.ads.android.IUnityAdsListener");
			Class.forName("com.unity3d.ads.android.UnityAds");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		if (!initialized) {
			UnityAds.init(activity, adId, createListener());
			initialized = true;
		} else if (UnityAds.canShowAds()){
			shouldReportAvailability = false;
			if (listener != null) {
				listener.onFullscreenLoaded(this);
			}
			UnityAds.setListener(createListener());
		} else {
			shouldReportAvailability = false;
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}

	}

	private IUnityAdsListener createListener() {
		return new IUnityAdsListener() {

			@Override
			public void onVideoStarted() {
			}

			@Override
			public void onVideoCompleted(String arg0, boolean arg1) {
			}

			@Override
			public void onShow() {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}

			@Override
			public void onHide() {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}

			@Override
			public void onFetchFailed() {
				if (listener != null && shouldReportAvailability) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void onFetchCompleted() {
				if (listener != null && shouldReportAvailability) {
					listener.onFullscreenLoaded(ApplifierFullscreen.this);
				}
			}
		};
	}

	@Override
	public void showFullscreen() {

		if (UnityAds.canShow() && UnityAds.canShowAds()) {
			UnityAds.show();
		}

	}

}
