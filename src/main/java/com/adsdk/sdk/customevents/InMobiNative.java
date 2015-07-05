package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.adsdk.sdk.nativeads.NativeAd;

public class InMobiNative extends CustomEventNative {

	private Object loadedNative;
	private static boolean isInitialized;
	private Class<?> inMobiClass;
	private Class<?> nativeAdClass;
	private Class<?> listenerClass;

	@Override
	public void createNativeAd(Context context, CustomEventNativeListener listener, String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			inMobiClass = Class.forName("com.inmobi.commons.InMobi");
			nativeAdClass = Class.forName("com.inmobi.monetization.IMNative");
			listenerClass = Class.forName("com.inmobi.monetization.IMNativeListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		try {
			if (!isInitialized) {
				Method initializeMethod = inMobiClass.getMethod("initialize", new Class[] { Context.class, String.class });
				initializeMethod.invoke(null, context, optionalParameters);
				isInitialized = true;
			}

			Constructor<?> nativeAdConstructor = nativeAdClass.getConstructor(new Class[] { String.class, listenerClass });
			Object inMobiNative = nativeAdConstructor.newInstance(optionalParameters, createListener());

			Method loadAdMethod = nativeAdClass.getMethod("loadAd");
			loadAdMethod.invoke(inMobiNative);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
		}
	}

	private Object createListener() {

		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onNativeRequestSucceeded")) {

					final Object response = args[0];
					if (response == null) {
						if (listener != null) {
							listener.onCustomEventNativeFailed();
						}
						return null;
					}

					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Method getContentMethod = nativeAdClass.getMethod("getContent");
								String content = (String)getContentMethod.invoke(response);
								final JSONTokener jsonTokener = new JSONTokener(content);
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
									setClickUrl(jsonObject.optString("landingURL"));

									loadedNative = response;

								} catch (JSONException e) {
									if (listener != null) {

										listener.onCustomEventNativeFailed();
									}
									return;
								}
								if (isNativeAdValid(InMobiNative.this)) {
									if (listener != null) {
										listener.onCustomEventNativeLoaded(InMobiNative.this);
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
				} else if (method.getName().equals("onNativeRequestFailed")) {
					if (listener != null) {
						listener.onCustomEventNativeFailed();
					}
				}
				return null;
			}
		});


		return instance;
	}

	@Override
	public void prepareImpression(View view) {
		try {
			Method attachToViewMethod = nativeAdClass.getMethod("attachToView", ViewGroup.class);

			if (view != null && view instanceof ViewGroup) {
				attachToViewMethod.invoke(loadedNative, (ViewGroup) view);
			} else if (view != null && view.getParent() instanceof ViewGroup) {
				attachToViewMethod.invoke(loadedNative, (ViewGroup) view.getParent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleClick() {
		try {
			Method handleClickMethod = nativeAdClass.getMethod("handleClick", HashMap.class);
			handleClickMethod.invoke(loadedNative);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
