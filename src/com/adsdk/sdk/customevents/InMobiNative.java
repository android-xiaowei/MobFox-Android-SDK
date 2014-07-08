
package com.adsdk.sdk.customevents;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.adsdk.sdk.nativeads.NativeAd;
import com.inmobi.commons.InMobi;
import com.inmobi.monetization.IMErrorCode;
import com.inmobi.monetization.IMNative;
import com.inmobi.monetization.IMNativeListener;

public class InMobiNative extends CustomEventNative {
	
	private IMNative loadedNative;
	private static boolean isInitialized;

	@Override
	public void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			Class.forName("com.inmobi.commons.InMobi");
			Class.forName("com.inmobi.monetization.IMErrorCode");
			Class.forName("com.inmobi.monetization.IMNative");
			Class.forName("com.inmobi.monetization.IMNativeListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		if(!isInitialized) {
			InMobi.initialize(context, optionalParameters);
			isInitialized = true;
		}
		IMNative inMobiNative = new IMNative(optionalParameters, createListener());
		inMobiNative.loadAd();
	}

	private IMNativeListener createListener() {
		return new IMNativeListener() {

			@Override
			public void onNativeRequestSucceeded(IMNative response) {
				final JSONTokener jsonTokener = new JSONTokener(response.getContent());
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(jsonTokener);

					addTextAsset(NativeAd.HEADLINE_TEXT_ASSET, jsonObject.getString("title"));
					addTextAsset(NativeAd.DESCRIPTION_TEXT_ASSET, jsonObject.getString("description"));
					addTextAsset(NativeAd.CALL_TO_ACTION_TEXT_ASSET, jsonObject.optString("cta"));
					addTextAsset(NativeAd.RATING_TEXT_ASSET, jsonObject.optString("rating"));

					JSONObject screenshotJsonObject = jsonObject.getJSONObject("screenshots");
					if (screenshotJsonObject != null) {
						String imgUrl = screenshotJsonObject.getString("url");
						addImageAsset(NativeAd.MAIN_IMAGE_ASSET, imgUrl);
					}

					JSONObject iconJsonObject = jsonObject.getJSONObject("icon");
					if (iconJsonObject != null) {
						String imgUrl = iconJsonObject.getString("url");
						addImageAsset(NativeAd.ICON_IMAGE_ASSET, imgUrl);
					}
					loadedNative = response;

				} catch (JSONException e) {
					listener.onCustomEventNativeFailed();
					return;
				}
				if (isNativeAdValid(InMobiNative.this)) {
					listener.onCustomEventNativeLoaded(InMobiNative.this);
				} else {
					listener.onCustomEventNativeFailed();
				}
			}

			@Override
			public void onNativeRequestFailed(IMErrorCode arg0) {
				listener.onCustomEventNativeFailed();
			}
		};
	}
	
	@Override
	public void prepareImpression(View view) {
		if (view != null && view instanceof ViewGroup) {
            loadedNative.attachToView((ViewGroup) view);
        } else if (view != null && view.getParent() instanceof ViewGroup) {
            loadedNative.attachToView((ViewGroup) view.getParent());
        } 
	}
	
	@Override
	public void handleClick() {
		loadedNative.handleClick(null);
	}

}
