package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.view.View;

public class FacebookBanner extends CustomEventBanner {
	
	private Object banner;
	private Class<?> bannerClass;
	private Class<?> listenerClass;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		Class<?> adSizeClass;
		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			listenerClass = Class.forName("com.facebook.ads.AdListener");
			adSizeClass = Class.forName("com.facebook.ads.AdSize");
			bannerClass = Class.forName("com.facebook.ads.AdView");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		
		try {
			Constructor<?> bannerConstructor = bannerClass.getConstructor(new Class[] {Context.class, String.class, adSizeClass});
			Object adSize = Enum.valueOf((Class<Enum>)adSizeClass, "BANNER_320_50");
			banner = bannerConstructor.newInstance(context, adId, adSize);
			
			Method setListenerMethod = bannerClass.getMethod("setAdListener", listenerClass);
			setListenerMethod.invoke(banner, createListener());
			
			Method loadAdMethod = bannerClass.getMethod("loadAd");
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

				if (method.getName().equals("onError")) {
					if (listener != null) {
						listener.onBannerFailed();
					}
				} else if (method.getName().equals("onAdLoaded")) {
					reportImpression();
					if (listener != null) {
						listener.onBannerLoaded((View)banner);
					}
				} else if (method.getName().equals("onAdClicked")) {
					if (listener != null) {
						listener.onBannerExpanded();
					}
				}
				return null;
			}
		});

		return instance;
	}
	
	@Override
	public void destroy() {
		if(banner != null && bannerClass != null) {
			try {
				Method destroyMethod = bannerClass.getMethod("destroy");
				destroyMethod.invoke(banner);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.destroy();
	}

}
