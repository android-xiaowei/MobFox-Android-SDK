package com.adsdk.sdk.customevents;

import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMBanner;
import com.inmobi.monetization.IMBannerListener;
import com.inmobi.monetization.IMErrorCode;

public class InMobiBanner extends CustomEventBanner {

	private IMBanner banner;
	private static boolean isInitialized;
	private boolean reportedClick;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.inmobi.commons.InMobi");
			Class.forName("com.inmobi.monetization.IMBanner");
			Class.forName("com.inmobi.monetization.IMBannerListener");
			Class.forName("com.inmobi.monetization.IMErrorCode");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		if (!(context instanceof Activity)) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		if (!isInitialized) {
			InMobi.initialize(context, optionalParameters);
			isInitialized = true;
		}
		if (width >= 728 && height >= 90) {
			banner = new IMBanner((Activity) context, optionalParameters, IMBanner.INMOBI_AD_UNIT_728X90);
		} else if (width >= 300 && height >= 250) {
			banner = new IMBanner((Activity) context, optionalParameters, IMBanner.INMOBI_AD_UNIT_300X250);
		} else if (width >= 468 && height >= 60) {
			banner = new IMBanner((Activity) context, optionalParameters, IMBanner.INMOBI_AD_UNIT_468X60);
		} else {
			banner = new IMBanner((Activity) context, optionalParameters, IMBanner.INMOBI_AD_UNIT_320X50);
		}
		banner.setIMBannerListener(createListener());
		banner.setRefreshInterval(IMBanner.REFRESH_INTERVAL_OFF);
		banner.loadBanner();

	}

	private IMBannerListener createListener() {
		return new IMBannerListener() {

			@Override
			public void onShowBannerScreen(IMBanner arg0) {
				if (listener != null && !reportedClick) {
					reportedClick = true;
					listener.onBannerExpanded();
				}
			}

			@Override
			public void onLeaveApplication(IMBanner arg0) {
				if (listener != null && !reportedClick) {
					reportedClick = true;
					listener.onBannerExpanded();
				}
			}

			@Override
			public void onDismissBannerScreen(IMBanner arg0) {
				reportedClick = false;
				if (listener != null) {
					listener.onBannerClosed();
				}
			}

			@Override
			public void onBannerRequestSucceeded(IMBanner arg0) {
				reportImpression();
				if (listener != null) {
					listener.onBannerLoaded(banner);
				}
			}

			@Override
			public void onBannerRequestFailed(IMBanner arg0, IMErrorCode arg1) {
				if (listener != null) {
					listener.onBannerFailed();
				}
			}

			@Override
			public void onBannerInteraction(IMBanner arg0, Map<String, String> arg1) {
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
