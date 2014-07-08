
package com.adsdk.sdk.customevents;

import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

import android.app.Activity;
import android.os.Handler;

//Uses Vungle SDK 3.0.7
public class VungleFullscreen extends CustomEventFullscreen {

	final VunglePub vunglePub = VunglePub.getInstance();
	private boolean alreadyReportedAdLoadStatus;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		alreadyReportedAdLoadStatus = false;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.vungle.publisher.EventListener");
			Class.forName("com.vungle.publisher.VunglePub");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		vunglePub.init(activity, adId);
		vunglePub.setEventListener(createListener());
		if (vunglePub.isCachedAdAvailable()) {
			if (listener != null) {
				listener.onFullscreenLoaded(this);
			}
		} else {
			Handler h = new Handler();
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (vunglePub.isCachedAdAvailable()) {
						if (listener != null && !alreadyReportedAdLoadStatus) {
							listener.onFullscreenLoaded(VungleFullscreen.this);
							alreadyReportedAdLoadStatus = true;
						}
					} else {
						if (listener != null && !alreadyReportedAdLoadStatus) {
							listener.onFullscreenFailed();
							alreadyReportedAdLoadStatus = true;
						}
					}

				}
			}, 5000);
		}

	}

	private EventListener createListener() {
		return new EventListener() {

			@Override
			public void onAdEnd() {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}

			@Override
			public void onAdStart() {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}

			@Override
			public void onAdUnavailable(String arg0) {
				if (listener != null && !alreadyReportedAdLoadStatus) {
					listener.onFullscreenFailed();
					alreadyReportedAdLoadStatus = true;
				}
			}

			@Override
			public void onCachedAdAvailable() {
				if (listener != null && !alreadyReportedAdLoadStatus) {
					listener.onFullscreenLoaded(VungleFullscreen.this);
					alreadyReportedAdLoadStatus = true;
				}
			}

			@Override
			public void onVideoView(boolean arg0, int arg1, int arg2) {
			}

		};
	}

	@Override
	public void finish() {
		super.finish();
		vunglePub.setEventListener(null);
	}

	@Override
	public void showFullscreen() {
		if (vunglePub != null && vunglePub.isCachedAdAvailable()) {
			vunglePub.playAd();
		}

	}

}
