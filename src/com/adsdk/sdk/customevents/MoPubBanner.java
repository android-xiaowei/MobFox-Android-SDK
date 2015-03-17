package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.view.View;

public class MoPubBanner extends CustomEventBanner {

	private Object banner;
	private boolean reportedClick;
	private Class<?> bannerClass;
	private Class<?> listenerClass;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String adId = optionalParameters;
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			bannerClass = Class.forName("com.mopub.mobileads.MoPubView");
			listenerClass = Class.forName("com.mopub.mobileads.MoPubView$BannerAdListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		
		reportedClick = false;

		try {
			Constructor<?> bannerConstructor = bannerClass.getConstructor(Context.class);
			banner = bannerConstructor.newInstance(context);

			Method setAdUnitIdMethod = bannerClass.getMethod("setAdUnitId", String.class);
			setAdUnitIdMethod.invoke(banner, adId);

			Method setAutorefreshEnabledMethod = bannerClass.getMethod("setAutorefreshEnabled", boolean.class);
			setAutorefreshEnabledMethod.invoke(banner, false);

			Method setListenerMethod = bannerClass.getMethod("setBannerAdListener", listenerClass);
			setListenerMethod.invoke(banner, createListener());

			Method loadAdMethod = bannerClass.getMethod("loadAd");
			loadAdMethod.invoke(banner);
		} catch (Exception e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
		}

	}

	private Object createListener() {
		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onBannerLoaded")) {
					reportImpression();
					if (listener != null) {
						listener.onBannerLoaded((View)banner);
					}
				} else if (method.getName().equals("onBannerFailed")) {
					if (listener != null) {
						listener.onBannerFailed();
					}
				} else if (method.getName().equals("onBannerExpanded")) {
					if (listener != null && !reportedClick) {
						reportedClick = true;
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("onBannerCollapsed")) {
					if (listener != null) {
						reportedClick = false;
						listener.onBannerClosed();
					}
				}
				else if (method.getName().equals("onBannerClicked")) {
					if (listener != null && !reportedClick) {
						reportedClick = true;
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
		if (banner != null && bannerClass != null) {
			try {
				Method destroyMethod = bannerClass.getMethod("destroy");
				destroyMethod.invoke(banner);
			} catch (Exception e) {
			}
		}
		super.destroy();
	}
}
