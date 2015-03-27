package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;

public class AmazonBanner extends CustomEventBanner {

	private Object banner;
	private Class<?> listenerClass;
	private Class<?> adLayoutClass;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		Class<?> adRegistrationClass;
		Class<?> adSizeClass;

		try {
			adLayoutClass = Class.forName("com.amazon.device.ads.AdLayout");
			listenerClass = Class.forName("com.amazon.device.ads.AdListener");
			adRegistrationClass = Class.forName("com.amazon.device.ads.AdRegistration");
			adSizeClass = Class.forName("com.amazon.device.ads.AdSize");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		if (!(context instanceof Activity)) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		try {
			Method setAppKeyMethod = adRegistrationClass.getMethod("setAppKey", String.class);
			setAppKeyMethod.invoke(null, adId);
			Activity activity = (Activity) context;

			Object adSize;
			Constructor<?> adSizeConstructor = adSizeClass.getConstructor(new Class[] { int.class, int.class });
			adSize = adSizeConstructor.newInstance(width, height);
			 
			Constructor<?> adLayoutConstructor = adLayoutClass.getConstructor(new Class[] { Activity.class, adSizeClass });
			banner = adLayoutConstructor.newInstance(activity, adSize);

			Method setListenerMethod = adLayoutClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(banner, createListener());

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			Method setLayoutParamsMethod = adLayoutClass.getMethod("setLayoutParams", LayoutParams.class);
			setLayoutParamsMethod.invoke(banner, params);

			Method loadAdMethod = adLayoutClass.getMethod("loadAd");
			loadAdMethod.invoke(banner);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onBannerFailed();
			}
		}

	}

	private Object createListener() {
		
		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onAdLoaded")) {
					reportImpression();
					if (listener != null) {
						listener.onBannerLoaded((android.view.View)banner);
					}
				} else if (method.getName().equals("onAdFailedToLoad")) {
					if (listener != null) {
						listener.onBannerFailed();
					}

				} else if (method.getName().equals("onAdExpanded")) {
					if (listener != null) {
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("onAdCollapsed")) {
					if (listener != null) {
						listener.onBannerClosed();
					}
				}
				return null;
			}
		});

		return instance;
		
	}

	@Override
	public void destroy() {
		if (banner != null && adLayoutClass != null) {
			try {
				Method destroyMethod = adLayoutClass.getMethod("destroy");
				destroyMethod.invoke(banner);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.destroy();
	}

}
