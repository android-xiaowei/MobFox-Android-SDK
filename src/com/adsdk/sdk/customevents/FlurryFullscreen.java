
package com.adsdk.sdk.customevents;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;

import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;

public class FlurryFullscreen extends CustomEventFullscreen implements FlurryAdListener {
	private static boolean isInitialized;
	private Context context;
	private FrameLayout layout;
	private String adSpace;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		listener = customEventFullscreenListener;
		String[] adIdParts = optionalParameters.split(";");
		if (adIdParts.length != 2) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		this.context = activity;
		adSpace = adIdParts[0];
		String apiKey = adIdParts[1];

		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.flurry.android.FlurryAdListener");
			Class.forName("com.flurry.android.FlurryAdSize");
			Class.forName("com.flurry.android.FlurryAdType");
			Class.forName("com.flurry.android.FlurryAds");
			Class.forName("com.flurry.android.FlurryAgent");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		layout = new FrameLayout(context);
		if (!isInitialized) {
			FlurryAgent.onStartSession(activity, apiKey);
			isInitialized = true;
		}
		FlurryAds.setAdListener(this);
		FlurryAds.fetchAd(context, adSpace, layout, FlurryAdSize.FULLSCREEN);
	}

	@Override
	public void finish() {
		FlurryAds.setAdListener(null);
		FlurryAds.removeAd(context, adSpace, layout);
		FlurryAgent.onEndSession(context);
		super.finish();
	}

	@Override
	public void showFullscreen() {
		FlurryAds.displayAd(context, adSpace, layout);
	}

	@Override
	public void onAdClicked(String arg0) {
	}

	@Override
	public void onAdClosed(String arg0) {
		if (listener != null && arg0.equals(adSpace)) {
			listener.onFullscreenClosed();
		}
	}

	@Override
	public void onAdOpened(String arg0) {
		if (arg0.equals(adSpace)) {
			reportImpression();
			if (listener != null) {
				listener.onFullscreenOpened();
			}
		}
	}

	@Override
	public void onApplicationExit(String arg0) {
		if(listener != null && arg0.equals(adSpace)) {
			listener.onFullscreenLeftApplication();
		}
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
		if (listener != null) {
			listener.onFullscreenFailed();
		}
	}

	@Override
	public void spaceDidReceiveAd(String arg0) {
		if (listener != null && arg0.equals(adSpace)) {
			listener.onFullscreenLoaded(FlurryFullscreen.this);
		}
	}
}
