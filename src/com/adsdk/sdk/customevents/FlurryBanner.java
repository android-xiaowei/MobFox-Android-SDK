
package com.adsdk.sdk.customevents;

import android.content.Context;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;

public class FlurryBanner extends CustomEventBanner implements FlurryAdListener {

	private static boolean isInitialized;
	private String adSpace;
	private Context context;
	private FrameLayout bannerLayout;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String[] adIdParts = optionalParameters.split(";");
		if (adIdParts.length != 2) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		this.context = context;
		adSpace = adIdParts[0];
		String apiKey = adIdParts[1];
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.flurry.android.FlurryAdListener");
			Class.forName("com.flurry.android.FlurryAdSize");
			Class.forName("com.flurry.android.FlurryAdType");
			Class.forName("com.flurry.android.FlurryAds");
			Class.forName("com.flurry.android.FlurryAgent");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		if (!isInitialized) {
			FlurryAgent.onStartSession(context, apiKey);
			isInitialized = true;
		}
		FlurryAds.setAdListener(this);
		FlurryAds.fetchAd(context, adSpace, bannerLayout, FlurryAdSize.BANNER_BOTTOM);
	}

	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}
	
	@Override
	public void destroy() {
		FlurryAds.setAdListener(null);
		FlurryAds.removeAd(context, adSpace, bannerLayout);
		FlurryAgent.onEndSession(context);
		super.destroy();
	}

	@Override
	public void onAdClicked(String arg0) {
	}

	@Override
	public void onAdClosed(String arg0) {
		if (listener != null && arg0.equals(adSpace)) {
			listener.onBannerClosed();
		}
	}

	@Override
	public void onAdOpened(String arg0) {
		if (listener != null && arg0.equals(adSpace)) {
			listener.onBannerExpanded();
		}
	}

	@Override
	public void onApplicationExit(String arg0) {
	}

	@Override
	public void onRenderFailed(String arg0) {
	}

	@Override
	public void onRendered(String arg0) {
	}

	@Override
	public void onVideoCompleted(String arg0) {
	}

	@Override
	public boolean shouldDisplayAd(String arg0, FlurryAdType arg1) {
		return true;
	}

	@Override
	public void spaceDidFailToReceiveAd(String arg0) {
		if (listener != null && arg0.equals(adSpace)) {
			listener.onBannerFailed();
		}
	}

	@Override
	public void spaceDidReceiveAd(String arg0) {
		if (arg0.equals(adSpace)) {
			reportImpression();
			FlurryAds.displayAd(context, adSpace, bannerLayout);
			if (listener != null) {
				listener.onBannerLoaded(bannerLayout);
			}
		}
	}

}
