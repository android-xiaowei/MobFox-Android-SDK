
package com.adsdk.sdk.customevents;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMInterstitial;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.RequestListener;

import android.content.Context;

public class MillennialFullscreen extends CustomEventFullscreen {

	private MMInterstitial interstitial;

	@Override
	public void loadFullscreen(Context context, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.millennialmedia.android.MMAd");
			Class.forName("com.millennialmedia.android.MMException");
			Class.forName("com.millennialmedia.android.MMInterstitial");
			Class.forName("com.millennialmedia.android.MMRequest");
			Class.forName("com.millennialmedia.android.RequestListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		interstitial = new MMInterstitial(context);
		interstitial.setListener(createListener());
		interstitial.setApid(adId);
		MMRequest request = new MMRequest();

		interstitial.setMMRequest(request);

		if (interstitial.isAdAvailable() == false) {
			interstitial.fetch();
		} else {
			if (listener != null) {
				listener.onFullscreenLoaded(this);
			}
		}
	}

	private RequestListener createListener() {
		return new RequestListener() {

			@Override
			public void requestFailed(MMAd arg0, MMException arg1) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void requestCompleted(MMAd arg0) {
				if (listener != null) {
					listener.onFullscreenLoaded(MillennialFullscreen.this);
				}
			}

			@Override
			public void onSingleTap(MMAd arg0) {
			}

			@Override
			public void MMAdRequestIsCaching(MMAd arg0) {
			}

			@Override
			public void MMAdOverlayLaunched(MMAd arg0) {
				if (listener != null) {
					reportImpression();
					listener.onFullscreenOpened();
				}
			}

			@Override
			public void MMAdOverlayClosed(MMAd arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (interstitial != null && interstitial.isAdAvailable()) {
			interstitial.display();
		}
	}

}
