
package com.adsdk.sdk.nativeads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.view.View;

import com.adsdk.sdk.Util;
import com.adsdk.sdk.customevents.CustomEvent;

public class NativeAd {
	public static final String ICON_IMAGE_ASSET = "icon";
	public static final String MAIN_IMAGE_ASSET = "main";
	public static final String HEADLINE_TEXT_ASSET = "headline";
	public static final String DESCRIPTION_TEXT_ASSET = "description";
	public static final String CALL_TO_ACTION_TEXT_ASSET = "cta";
	public static final String ADVERTISER_TEXT_ASSET = "advertiser";
	public static final String RATING_TEXT_ASSET = "rating";
	public static final String IMPRESSION_TRACKER_TYPE = "impression";

	public static class ImageAsset {
		String url;
		Bitmap bitmap;
		int width;
		int height;

		public ImageAsset(String url, int width, int height) {
			this.url = url;
			this.width = width;
			this.height = height;
			this.bitmap = Util.loadBitmap(url);
		}

		public String getUrl() {
			return url;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

	}

	public static class Tracker {
		String type;
		String url;

		public Tracker(String type, String url) {
			this.type = type;
			this.url = url;
		}
	}

	private String clickUrl;
	private Map<String, ImageAsset> imageAssets = new HashMap<String, NativeAd.ImageAsset>();
	private Map<String, String> textAssets = new HashMap<String, String>();
	private List<Tracker> trackers = new ArrayList<NativeAd.Tracker>();
	private List<CustomEvent> customEvents;
	private boolean nativeAdValid;

	public String getClickUrl() {
		return clickUrl;
	}

	public void setClickUrl(String clickUrl) {
		this.clickUrl = clickUrl;
	}

	public void addTextAsset(String type, String asset) {
		if (type != null && asset != null) {
			textAssets.put(type, asset);
		}
	}

	public void addImageAsset(String type, ImageAsset asset) {
		if (type != null && asset != null) {
			imageAssets.put(type, asset);
		}
	}

	public String getTextAsset(String type) {
		return textAssets.get(type);
	}

	public ImageAsset getImageAsset(String type) {
		return imageAssets.get(type);
	}

	public List<Tracker> getTrackers() {
		return trackers;
	}

	public void setTrackers(List<Tracker> trackers) {
		this.trackers = trackers;
	}

	public List<CustomEvent> getCustomEvents() {
		return customEvents;
	}

	public void setCustomEvents(List<CustomEvent> customEvents) {
		this.customEvents = customEvents;
	}

	public boolean isNativeAdValid() {
		return nativeAdValid;
	}

	public void setNativeAdValid(boolean nativeAdValid) {
		this.nativeAdValid = nativeAdValid;
	}

	public void handleClick() {
		//used for reporting clicks for custom events
	}
	
	public void prepareImpression(View view) {
		//used for reporting impressions for custom events
	}
	
	public void handleImpression() {
		//used for reporting impressions for custom events	
	}

	public void unregisterListener() {
		//to be used by custom events
	}
	
}
