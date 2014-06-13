
package com.adsdk.sdk.customevents;

import com.unity3d.ads.android.IUnityAdsListener;
import com.unity3d.ads.android.UnityAds;

import android.app.Activity;

public class ApplifierFullscreen extends CustomEventFullscreen {

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
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

		UnityAds.init(activity, adId, createListener());

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
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void onFetchCompleted() {
				if (listener != null) {
					listener.onFullscreenLoaded(ApplifierFullscreen.this);
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (UnityAds.canShowAds()) {
			UnityAds.show();
		}

	}

}
