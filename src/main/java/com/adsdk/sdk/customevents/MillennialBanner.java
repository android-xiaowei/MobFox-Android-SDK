package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.view.View;

public class MillennialBanner extends CustomEventBanner {

	private Object millenialAdView;
	private Class<?> listenerClass;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		Class<?> sdkClass;
		Class<?> bannerClass;
		Class<?> requestClass;

		try {
			bannerClass = Class.forName("com.millennialmedia.android.MMAdView");
			requestClass = Class.forName("com.millennialmedia.android.MMRequest");
			sdkClass = Class.forName("com.millennialmedia.android.MMSDK");
			listenerClass = Class.forName("com.millennialmedia.android.RequestListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		try {
			Constructor<?> adViewConstructor = bannerClass.getConstructor(Context.class);
			millenialAdView = adViewConstructor.newInstance(context);

			Method getDefaultAdIdMethod = sdkClass.getMethod("getDefaultAdId");
			int id = (Integer) getDefaultAdIdMethod.invoke(null);

			Method setIdMethod = bannerClass.getMethod("setId", int.class);
			setIdMethod.invoke(millenialAdView, id);

			Method setWidthMethod = bannerClass.getMethod("setWidth", int.class);
			Method setHeightMethod = bannerClass.getMethod("setHeight", int.class);
			setWidthMethod.invoke(millenialAdView, width);
			setHeightMethod.invoke(millenialAdView, height);

			Method setApidMethod = bannerClass.getMethod("setApid", String.class);
			setApidMethod.invoke(millenialAdView, adId);

			Constructor<?> requestConstructor = requestClass.getConstructor();
			Object request = requestConstructor.newInstance();

			Method setMMRequestMethod = bannerClass.getMethod("setMMRequest", requestClass);
			setMMRequestMethod.invoke(millenialAdView, request);

			Method setListenerMethod = bannerClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(millenialAdView, createAdListener());

			Method getAdMethod = bannerClass.getMethod("getAd");
			getAdMethod.invoke(millenialAdView);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onBannerFailed();
			}
		}

	}

	private Object createAdListener() {
		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("requestFailed")) {
					if (listener != null) {
						listener.onBannerFailed();
					}
				} else if (method.getName().equals("requestCompleted")) {
					reportImpression();
					if (listener != null) {
						listener.onBannerLoaded((View) millenialAdView);
					}
				} else if (method.getName().equals("MMAdOverlayLaunched")) {
					if (listener != null) {
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("MMAdOverlayClosed")) {
					if (listener != null) {
						listener.onBannerClosed();
					}
				}
				return null;
			}
		});

		return instance;
	}

}
