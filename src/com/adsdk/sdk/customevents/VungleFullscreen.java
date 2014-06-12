
package com.adsdk.sdk.customevents;

import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

import android.app.Activity;


//Uses Vungle SDK 3.0.7 RC
public class VungleFullscreen extends CustomEventFullscreen {
	
	final VunglePub vunglePub = VunglePub.getInstance();
	

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
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
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void onCachedAdAvailable() {
				if (listener != null) {
					listener.onFullscreenLoaded(VungleFullscreen.this);
				}
			}

			@Override
			public void onVideoView(boolean arg0, int arg1, int arg2) {
			}

		};
	}

	@Override
	public void showFullscreen() {
		if (vunglePub!=null && vunglePub.isCachedAdAvailable()) {
			vunglePub.playAd();
		}

	}

}
