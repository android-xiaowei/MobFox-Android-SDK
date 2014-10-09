package com.adsdk.sdk.customevents;

import android.app.Activity;

import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError.CBImpressionError;

public class ChartboostFullscreen extends CustomEventFullscreen {

	private Activity activity;
	private boolean shouldDisplay;
	private boolean shouldReportAvailability;
	private String locationName = CBLocation.LOCATION_DEFAULT;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		this.activity = activity;

		String[] adIdParts = optionalParameters.split(";");
		String appId = adIdParts[0];
		String appSignature = adIdParts[1];
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.chartboost.sdk.Chartboost");
			Class.forName("com.chartboost.sdk.ChartboostDelegate");
			Class.forName("com.chartboost.sdk.Model.CBError");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		Chartboost.startWithAppId(activity, appId, appSignature);
		Chartboost.setDelegate(createListener());
		Chartboost.setAutoCacheAds(false);

		Chartboost.onCreate(activity);

		Chartboost.onStart(activity);

		Chartboost.onResume(activity);

		shouldReportAvailability = true;
		Chartboost.cacheInterstitial(locationName);
	}

	private ChartboostDelegate createListener() {
		return new ChartboostDelegate() {

			@Override
			public boolean shouldDisplayInterstitial(String arg0) {
				return shouldDisplay;
			}

			@Override
			public void didDisplayInterstitial(String arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
				shouldDisplay = false;
			}

			@Override
			public void didFailToLoadInterstitial(String arg0, CBImpressionError arg1) {
				if (listener != null && shouldReportAvailability) {
					shouldReportAvailability = false;
					listener.onFullscreenFailed();
				}
			}

			@Override
			public void didDismissInterstitial(String arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}

			@Override
			public void didClickInterstitial(String arg0) {
				if (listener != null) {
					listener.onFullscreenLeftApplication();
				}
			}

			@Override
			public void didCacheInterstitial(String arg0) {
				if (listener != null && shouldReportAvailability) {
					shouldReportAvailability = false;
					listener.onFullscreenLoaded(ChartboostFullscreen.this);
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if (Chartboost.hasInterstitial(locationName)) {
			shouldDisplay = true;
			Chartboost.showInterstitial(locationName);
		}
	}

	@Override
	public void finish() {
		if (activity != null) {
			Chartboost.onPause(activity);
			Chartboost.onStop(activity);
			Chartboost.onDestroy(activity);
		}
		activity = null;
		super.finish();
	}

}
