package com.adsdk.sdk.customevents;

import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyVideoAd;

import android.app.Activity;

public class AdColonyFullscreen extends CustomEventFullscreen {
	
	private static boolean initialized;
	private AdColonyAdAvailabilityListener availabilityListener;
	private AdColonyVideoAd videoAd;
	

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String[] adIdParts = optionalParameters.split(";");
		String clientOptions = adIdParts[0];
		String appId = adIdParts[1];
		String zoneIds = adIdParts[2];
		
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

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
		availabilityListener = createAvailabilityListener();
		AdColony.addAdAvailabilityListener(availabilityListener);
		
		videoAd = new AdColonyVideoAd().withListener(createListener());
	}

	private AdColonyAdAvailabilityListener createAvailabilityListener() { //TODO: make sure listener methods work properly!
		return new AdColonyAdAvailabilityListener() {
			
			@Override
			public void onAdColonyAdAvailabilityChange(boolean available, String arg1) {
				if(available) {
					AdColony.removeAdAvailabilityListener(availabilityListener);
					if (listener != null) {
						listener.onFullscreenLoaded(AdColonyFullscreen.this);
					}
				}
				
			}
		};
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
					AdColony.removeAdAvailabilityListener(availabilityListener);
					if (listener != null) {
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
