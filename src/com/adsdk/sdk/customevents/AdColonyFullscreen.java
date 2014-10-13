package com.adsdk.sdk.customevents;

import android.app.Activity;
import android.os.Handler;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyVideoAd;

public class AdColonyFullscreen extends CustomEventFullscreen {
	
	private static boolean initialized;
	private AdColonyVideoAd videoAd;
	private boolean reported;
	

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String[] adIdParts = optionalParameters.split(";");
		String clientOptions = adIdParts[0];
		String appId = adIdParts[1];
		String zoneIds = adIdParts[2];
		
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;
		reported = false;

		try {
			Class.forName("com.jirbo.adcolony.AdColony");
			Class.forName("com.jirbo.adcolony.AdColonyAd");
			Class.forName("com.jirbo.adcolony.AdColonyAdAvailabilityListener");
			Class.forName("com.jirbo.adcolony.AdColonyAdListener");
			Class.forName("com.jirbo.adcolony.AdColonyVideoAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		
		if(!initialized) {
			AdColony.configure(activity, clientOptions, appId, zoneIds);
			initialized = true;
		}
		
		videoAd = new AdColonyVideoAd().withListener(createListener());
		if(videoAd.isReady()) {
			if (listener != null && !reported) {
				reported = true;
				listener.onFullscreenLoaded(AdColonyFullscreen.this);
			}
		} else {
			Handler h = new Handler();
			h.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if(videoAd.isReady()) {
						if (listener != null && !reported) {
							reported = true;
							listener.onFullscreenLoaded(AdColonyFullscreen.this);
						}
					} else {
						if (listener != null && !reported) {
							reported = true;
							listener.onFullscreenFailed();
						}
					}
					
				}
			}, 5000);
		}
	}

	private AdColonyAdListener createListener() {
		return new AdColonyAdListener() {
			
			@Override
			public void onAdColonyAdStarted(AdColonyAd arg0) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}
			
			@Override
			public void onAdColonyAdAttemptFinished(AdColonyAd ad) {
				if(ad.notShown() || ad.noFill()) {
					if (listener != null && !reported) {
						reported = true;
						listener.onFullscreenFailed();
					}
				} else if (listener != null) {
					listener.onFullscreenClosed();
				}
			}
		};
	}

	@Override
	public void showFullscreen() {
		if(videoAd != null && videoAd.isReady()) {
			videoAd.show();
		}
	}

}
