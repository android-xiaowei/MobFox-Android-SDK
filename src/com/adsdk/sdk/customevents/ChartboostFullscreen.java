package com.adsdk.sdk.customevents;

import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Chartboost.CBAgeGateConfirmation;
import com.chartboost.sdk.Model.CBError.CBClickError;
import com.chartboost.sdk.Model.CBError.CBImpressionError;

import android.app.Activity;

public class ChartboostFullscreen extends CustomEventFullscreen {
	
	private Chartboost chartboost;
	private Activity activity;
	private boolean shouldDisplay;

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
		chartboost = Chartboost.sharedChartboost();
		chartboost.onCreate(activity, appId, appSignature, createListener());
		chartboost.onStart(activity);

		chartboost.cacheInterstitial();
	}
	
	private ChartboostDelegate createListener() {
		return new ChartboostDelegate() {
			
			@Override
			public boolean shouldRequestMoreApps() {
				return false;
			}
			
			@Override
			public boolean shouldRequestInterstitialsInFirstSession() {
				return true;
			}
			
			@Override
			public boolean shouldRequestInterstitial(String arg0) {
				return true;
			}
			
			@Override
			public boolean shouldPauseClickForConfirmation(CBAgeGateConfirmation arg0) {
				return false;
			}
			
			@Override
			public boolean shouldDisplayMoreApps() {
				return false;
			}
			
			@Override
			public boolean shouldDisplayLoadingViewForMoreApps() {
				return false;
			}
			
			@Override
			public boolean shouldDisplayInterstitial(String arg0) {
				return shouldDisplay;
			}
			
			@Override
			public void didShowMoreApps() {
			}
			
			@Override
			public void didShowInterstitial(String arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
				shouldDisplay = false;
			}
			
			@Override
			public void didFailToRecordClick(String arg0, CBClickError arg1) {
			}
			
			@Override
			public void didFailToLoadMoreApps(CBImpressionError arg0) {
			}
			
			@Override
			public void didFailToLoadInterstitial(String arg0, CBImpressionError arg1) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}
			
			@Override
			public void didDismissMoreApps() {
			}
			
			@Override
			public void didDismissInterstitial(String arg0) {
				if (listener != null) {
					listener.onFullscreenClosed();
				}
			}
			
			@Override
			public void didCloseMoreApps() {
			}
			
			@Override
			public void didCloseInterstitial(String arg0) {
			}
			
			@Override
			public void didClickMoreApps() {
			}
			
			@Override
			public void didClickInterstitial(String arg0) {
				if (listener != null) {
					listener.onFullscreenLeftApplication();
				}
			}
			
			@Override
			public void didCacheMoreApps() {
			}
			
			@Override
			public void didCacheInterstitial(String arg0) {
				if (listener != null) {
					listener.onFullscreenLoaded(ChartboostFullscreen.this);
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if(chartboost!=null && chartboost.hasCachedInterstitial()) {
			shouldDisplay = true;
			chartboost.showInterstitial();
		}
	}
	
	@Override
	public void finish() {
		if(chartboost != null) {			
			chartboost.onStop(activity);
			chartboost.onDestroy(activity);
		}
		chartboost = null;
		activity = null;
		super.finish();
	}

}
