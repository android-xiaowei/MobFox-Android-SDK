package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;

import com.adsdk.sdk.nativeads.NativeAd;

public class MoPubNative extends CustomEventNative {

	private Class<?> listenerClass;
	private Class<?> responseClass;

	@Override
	public void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel) {
		this.listener = listener;

		Class<?> moPubNativeClass;

		try {
			moPubNativeClass = Class.forName("com.mopub.nativeads.MoPubNative");
			responseClass = Class.forName("com.mopub.nativeads.NativeResponse");
			listenerClass = Class.forName("com.mopub.nativeads.MoPubNative$MoPubNativeListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		try {
			Constructor<?> moPubNativeConstructor = moPubNativeClass.getConstructor(new Class[] { Context.class, String.class, listenerClass });
			Object moPubNative = moPubNativeConstructor.newInstance(context, optionalParameters, createListener());

			Method makeRequestMethod = moPubNativeClass.getMethod("makeRequest");
			makeRequestMethod.invoke(moPubNative);
		} catch (Exception e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
		}
	}

	private Object createListener() {

		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onNativeLoad")) {

					final Object response = args[0];
					if (response == null) {
						if (listener != null) {
							listener.onCustomEventNativeFailed();
						}
						return null;
					}

					Thread t = new Thread(new Runnable() {

						@SuppressWarnings("unchecked")
						@Override
						public void run() {

							try {
								Method getClickDestinationUrlMethod = responseClass.getMethod("getClickDestinationUrl");
								setClickUrl((String) getClickDestinationUrlMethod.invoke(response));
								
								Method getImpressionTrackersMethod = responseClass.getMethod("getImpressionTrackers");
								List<String> impressionTrackers = (List<String>) getImpressionTrackersMethod.invoke(response);
								
								if (impressionTrackers != null) {
									for (String impressionTrackerUrl : impressionTrackers) {
										addImpressionTracker(impressionTrackerUrl);
									}
								}

								Method getMainImageUrlMethod = responseClass.getMethod("getMainImageUrl");
								Method getIconImageUrlMethod = responseClass.getMethod("getIconImageUrl");
								Method getCallToActionMethod = responseClass.getMethod("getCallToAction");
								Method getTextMethod = responseClass.getMethod("getText");
								Method getTitleMethod = responseClass.getMethod("getTitle");
								
								
								addImageAsset(NativeAd.MAIN_IMAGE_ASSET, (String)getMainImageUrlMethod.invoke(response));
								addImageAsset(NativeAd.ICON_IMAGE_ASSET, (String)getIconImageUrlMethod.invoke(response));
								addTextAsset(NativeAd.CALL_TO_ACTION_TEXT_ASSET, (String)getCallToActionMethod.invoke(response));
								addTextAsset(NativeAd.DESCRIPTION_TEXT_ASSET, (String)getTextMethod.invoke(response));
								addTextAsset(NativeAd.HEADLINE_TEXT_ASSET, (String)getTitleMethod.invoke(response));

								Method getExtrasMethod = responseClass.getMethod("getExtras");
								Map<String, Object> extras = (Map<String, Object>) getExtrasMethod.invoke(response);
								for (Entry<String, Object> entry : extras.entrySet()) {
									if (entry.getValue() != null && entry.getValue() instanceof String) {
										addExtraAsset(entry.getKey(), (String) entry.getValue());
									}
								}

								if (isNativeAdValid(MoPubNative.this)) {
									if (listener != null) {
										listener.onCustomEventNativeLoaded(MoPubNative.this);
									}
								} else {
									if (listener != null) {
										listener.onCustomEventNativeFailed();
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								if (listener != null) {
									listener.onCustomEventNativeFailed();
								}
							}
						}
					});
					t.start();
				} else if (method.getName().equals("onNativeFail")) {
					if (listener != null) {
						listener.onCustomEventNativeFailed();
					}
				}
				return null;
			}
		});

		return instance;

	}

}
