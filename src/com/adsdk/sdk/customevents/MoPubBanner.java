
package com.adsdk.sdk.customevents;

import android.content.Context;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.BannerAdListener;

public class MoPubBanner extends CustomEventBanner {

	private MoPubView banner;
	private boolean reportedClick;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.mopub.mobileads.MoPubView");
			Class.forName("com.mopub.mobileads.MoPubErrorCode");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		banner = new MoPubView(context);
		banner.setAdUnitId(adId);
		banner.setAutorefreshEnabled(false);
		banner.setBannerAdListener(createListener());
		banner.loadAd();
	}

	private BannerAdListener createListener() {
		return new BannerAdListener() {

			@Override
			public void onBannerLoaded(MoPubView arg0) {
				reportImpression();
				if (listener != null) {
					listener.onBannerLoaded(banner);
				}
			}

			@Override
			public void onBannerFailed(MoPubView arg0, MoPubErrorCode arg1) {
				if (listener != null) {
					listener.onBannerFailed();
				}
			}

			@Override
			public void onBannerExpanded(MoPubView arg0) {
				if (listener != null && !reportedClick) {
					reportedClick = true;
					listener.onBannerExpanded();
				}
			}

			@Override
			public void onBannerCollapsed(MoPubView arg0) {
				if (listener != null) {
					reportedClick = false;
					listener.onBannerClosed();
				}
			}

			@Override
			public void onBannerClicked(MoPubView arg0) {
				if (listener != null && !reportedClick) {
					reportedClick = true;
					listener.onBannerExpanded();
				}
			}
		};
	}

	@Override
	public void destroy() {
		if (banner != null) {
			banner.destroy();
		}
		super.destroy();
	}
}
