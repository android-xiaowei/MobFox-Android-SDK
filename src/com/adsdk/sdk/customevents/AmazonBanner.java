
package com.adsdk.sdk.customevents;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.AdSize;

public class AmazonBanner extends CustomEventBanner {

	private AdLayout banner;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.amazon.device.ads.Ad");
			Class.forName("com.amazon.device.ads.AdError");
			Class.forName("com.amazon.device.ads.AdLayout");
			Class.forName("com.amazon.device.ads.AdListener");
			Class.forName("com.amazon.device.ads.AdProperties");
			Class.forName("com.amazon.device.ads.AdRegistration");
			Class.forName("com.amazon.device.ads.AdSize");
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

		AdRegistration.setAppKey(adId);
		Activity activity = (Activity) context;
		AdSize size = new AdSize(width, height);
		banner = new AdLayout(activity, size);
		banner.setListener(createListener());
		banner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		banner.loadAd();
	}

	private AdListener createListener() {
		return new AdListener() {

			@Override
			public void onAdLoaded(Ad arg0, AdProperties arg1) {
				reportImpression();
				if (listener != null) {
					listener.onBannerLoaded(banner);
				}
			}

			@Override
			public void onAdFailedToLoad(Ad arg0, AdError arg1) {
				if (listener != null) {
					listener.onBannerFailed();
				}
			}

			@Override
			public void onAdExpanded(Ad arg0) {
				if (listener != null) {
					listener.onBannerExpanded();
				}
			}

			@Override
			public void onAdDismissed(Ad arg0) {
			}

			@Override
			public void onAdCollapsed(Ad arg0) {
				if (listener != null) {
					listener.onBannerClosed();
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
