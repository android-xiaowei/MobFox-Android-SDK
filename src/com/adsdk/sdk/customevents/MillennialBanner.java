
package com.adsdk.sdk.customevents;

import com.millennialmedia.android.MMAd;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMException;
import com.millennialmedia.android.MMRequest;
import com.millennialmedia.android.MMSDK;
import com.millennialmedia.android.RequestListener;

import android.content.Context;

public class MillennialBanner extends CustomEventBanner {

	private MMAdView millenialAdView;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.millennialmedia.android.MMAd");
			Class.forName("com.millennialmedia.android.MMAdView");
			Class.forName("com.millennialmedia.android.MMException");
			Class.forName("com.millennialmedia.android.MMRequest");
			Class.forName("com.millennialmedia.android.MMSDK");
			Class.forName("com.millennialmedia.android.RequestListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		millenialAdView = new MMAdView(context);
		millenialAdView.setId(MMSDK.getDefaultAdId());
		millenialAdView.setWidth(width);
		millenialAdView.setHeight(height);
		millenialAdView.setApid(adId);

		MMRequest request = new MMRequest();

		millenialAdView.setMMRequest(request);
		millenialAdView.setListener(createAdListener());

		millenialAdView.getAd();

	}

	private RequestListener createAdListener() {
		return new RequestListener() {

			@Override
			public void requestFailed(MMAd arg0, MMException arg1) {
				if (listener != null) {
					listener.onBannerFailed();
				}
			}

			@Override
			public void requestCompleted(MMAd arg0) {
				reportImpression();
				if (listener != null) {
					listener.onBannerLoaded(millenialAdView);
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
					listener.onBannerExpanded();
				}
			}

			@Override
			public void MMAdOverlayClosed(MMAd arg0) {
				if (listener != null) {
					listener.onBannerClosed();
				}
			}
		};
	}

}
