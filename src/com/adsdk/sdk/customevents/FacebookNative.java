package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.view.View;

public class FacebookNative extends CustomEventNative {

	private Object facebookNative;
	private Class<?> listenerClass;
	private Class<?> nativeAdClass;

	@Override
	public void createNativeAd(final Context context, CustomEventNativeListener listener, final String optionalParameters, String trackingPixel) {
		this.listener = listener;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			listenerClass = Class.forName("com.facebook.ads.AdListener");
			nativeAdClass = Class.forName("com.facebook.ads.NativeAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
			return;
		}

		addImpressionTracker(trackingPixel);

		try {
			Constructor<?> nativeAdConstructor = nativeAdClass.getConstructor(new Class[] { Context.class, String.class });
			facebookNative = nativeAdConstructor.newInstance(context, optionalParameters);

			Method setAdListenerMethod = nativeAdClass.getMethod("setAdListener", listenerClass);
			setAdListenerMethod.invoke(facebookNative, createListener());

			Method loadAdMethod = nativeAdClass.getMethod("loadAd");
			loadAdMethod.invoke(facebookNative);
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

				if (method.getName().equals("onError")) {
					if (listener != null) {
						listener.onCustomEventNativeFailed();
					}
				} else if (method.getName().equals("onAdLoaded")) {
					final Object ad = args[0];
					if (ad == null) {
						if (listener != null) {
							listener.onCustomEventNativeFailed();
						}
						return null;
					}
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							if (!facebookNative.equals(ad)) {
								if (listener != null) {
									listener.onCustomEventNativeFailed();
								}
								return;
							}

							try {

								Method isAdLoadedMethod = nativeAdClass.getMethod("isAdLoaded");
								boolean isAdLoaded = (Boolean) isAdLoadedMethod.invoke(facebookNative);
								if (!isAdLoaded) {
									if (listener != null) {
										listener.onCustomEventNativeFailed();
									}
									return;
								}
								Method getAdTitleMethod = nativeAdClass.getMethod("getAdTitle");
								String adTitle = (String) getAdTitleMethod.invoke(facebookNative);

								Method getAdBodyMethod = nativeAdClass.getMethod("getAdBody");
								String adBody = (String) getAdBodyMethod.invoke(facebookNative);

								Method getAdCTAMethod = nativeAdClass.getMethod("getAdCallToAction");
								String adCTA = (String) getAdCTAMethod.invoke(facebookNative);

								Method getAdSocialContextMethod = nativeAdClass.getMethod("getAdSocialContext");
								String adSocialContext = (String) getAdSocialContextMethod.invoke(facebookNative);

								Method getStarRatingMethod = nativeAdClass.getMethod("getAdStarRating");
								Object starRating = getStarRatingMethod.invoke(facebookNative);

								addTextAsset(HEADLINE_TEXT_ASSET, adTitle);
								addTextAsset(DESCRIPTION_TEXT_ASSET, adBody);
								addTextAsset(CALL_TO_ACTION_TEXT_ASSET, adCTA);
								addTextAsset(RATING_TEXT_ASSET, readRating(starRating));
								addTextAsset("socialContextForAd", adSocialContext);

								Class<?> imageClass = Class.forName("com.facebook.ads.NativeAd$Image");
								Method getAdIconMethod = nativeAdClass.getMethod("getAdIcon");
								Method getAdCoverImageMethod = nativeAdClass.getMethod("getAdCoverImage");
								Object iconObject = getAdIconMethod.invoke(facebookNative);
								Object coverImageObject = getAdCoverImageMethod.invoke(facebookNative);
								Method getUrlMethod = imageClass.getMethod("getUrl");

								String adIconUrl = (String) getUrlMethod.invoke(iconObject);
								String adCoverImageUrl = (String) getUrlMethod.invoke(coverImageObject);

								addImageAsset(ICON_IMAGE_ASSET, adIconUrl);
								addImageAsset(MAIN_IMAGE_ASSET, adCoverImageUrl);

								if (isNativeAdValid(FacebookNative.this)) {
									if (listener != null) {
										listener.onCustomEventNativeLoaded(FacebookNative.this);
									}
								} else {
									if (listener != null) {
										listener.onCustomEventNativeFailed();
									}
								}

							} catch (Exception e) {
								if (listener != null) {
									listener.onCustomEventNativeFailed();
								}
							}

						}
					});
					t.start();

				}
				return null;
			}
		});

		return instance;

	}

	private String readRating(Object rating) {
		if (rating != null) {
			try {
				Class<?> ratingClass = Class.forName("com.facebook.ads.NativeAd$Rating");
				Method getValueMethod = ratingClass.getMethod("getValue");
				Method getScaleMethod = ratingClass.getMethod("getScale");
				double value = (Double) getValueMethod.invoke(rating);
				double scale = (Double) getScaleMethod.invoke(rating);

				int stars = (int) Math.round(5 * value / scale);

				return Integer.toString(stars);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	public void prepareImpression(View view) {
		try {
			Method prepareImpressionMethod = nativeAdClass.getMethod("registerViewForInteraction", View.class);
			prepareImpressionMethod.invoke(facebookNative, view);
		} catch (Exception e) {
			if (listener != null) {
				listener.onCustomEventNativeFailed();
			}
		}
	}

}
