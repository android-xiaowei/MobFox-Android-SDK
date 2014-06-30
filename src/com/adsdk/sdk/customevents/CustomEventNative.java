
package com.adsdk.sdk.customevents;

import android.content.Context;

import com.adsdk.sdk.nativeads.NativeAd;

public abstract class CustomEventNative extends NativeAd {

	protected CustomEventNativeListener listener;

	public abstract void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel);

	protected boolean isNativeAdValid(NativeAd ad) {
		boolean textAssetsOK = false;
		boolean imageAssetsOK = false;

		ImageAsset iconImageAsset = ad.getImageAsset(NativeAd.ICON_IMAGE_ASSET);
		ImageAsset mainImageAsset = ad.getImageAsset(NativeAd.MAIN_IMAGE_ASSET);
		if (iconImageAsset != null && mainImageAsset != null) {
			if (iconImageAsset.getBitmap() != null && mainImageAsset.getBitmap() != null) {
				imageAssetsOK = true;
			}
		}

		if (ad.getTextAsset(NativeAd.HEADLINE_TEXT_ASSET) != null && ad.getTextAsset(NativeAd.HEADLINE_TEXT_ASSET).length() > 0 &&
				ad.getTextAsset(NativeAd.DESCRIPTION_TEXT_ASSET) != null && ad.getTextAsset(NativeAd.DESCRIPTION_TEXT_ASSET).length() > 0) {
			textAssetsOK = true;
		}

		return (textAssetsOK && imageAssetsOK);
	}

	public interface CustomEventNativeListener {
		public void onCustomEventNativeFailed();

		public void onCustomEventNativeLoaded(NativeAd customNativeAd);
	}

	protected void addImageAsset(String type, String url) {
		if (type != null && url != null) {
			ImageAsset imageAsset = new ImageAsset(url, 0, 0); // width and height are ignored by SDK
			addImageAsset(type, imageAsset);
		}
	}

	protected void addImpressionTracker(String trackingUrl) {
		if (trackingUrl != null) {
			Tracker tracker = new Tracker(NativeAd.IMPRESSION_TRACKER_TYPE, trackingUrl);
			getTrackers().add(tracker);
		}
	}
	
	protected void addExtraAsset(String type, String asset) {
		if(type.contains("image")) {
			addImageAsset(type, asset);
		} else {
			addTextAsset(type, asset);
		}
	}
}
