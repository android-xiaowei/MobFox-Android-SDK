package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

public class InMobiBanner extends CustomEventBanner {

	private Object banner;
	private FrameLayout bannerLayout;
	private static boolean isInitialized;
	private boolean reportedClick;
	private Class<?> inMobiClass;
	private Class<?> bannerClass;
	private Class<?> listenerClass;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			inMobiClass = Class.forName("com.inmobi.commons.InMobi");
			bannerClass = Class.forName("com.inmobi.monetization.IMBanner");
			listenerClass = Class.forName("com.inmobi.monetization.IMBannerListener");
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

		bannerLayout = new FrameLayout(context);

		try {

			if (!isInitialized) {
				Method initializeMethod = inMobiClass.getMethod("initialize", new Class[] {Context.class, String.class});
				initializeMethod.invoke(null, context, optionalParameters);
				isInitialized = true;
			}
			int adSize;
			
			if (width >= 728 && height >= 90) {
				adSize = bannerClass.getDeclaredField("INMOBI_AD_UNIT_728X90").getInt(null);
			} else if (width >= 300 && height >= 250) {
				adSize = bannerClass.getDeclaredField("INMOBI_AD_UNIT_300X250").getInt(null);
			} else if (width >= 468 && height >= 60) {
				adSize = bannerClass.getDeclaredField("INMOBI_AD_UNIT_468X60").getInt(null);
			} else {
				adSize = bannerClass.getDeclaredField("INMOBI_AD_UNIT_320X50").getInt(null);
			}
			Constructor<?> bannerConstructor = bannerClass.getConstructor(new Class[] {Activity.class, String.class, int.class});
			banner = bannerConstructor.newInstance((Activity)context, optionalParameters, adSize);			
			
			Method setIMBannerListenerMethod = bannerClass.getMethod("setIMBannerListener", listenerClass);
			setIMBannerListenerMethod.invoke(banner, createListener());
			
			int refreshIntervalOff = bannerClass.getDeclaredField("REFRESH_INTERVAL_OFF").getInt(null);
			Method setRefreshIntervalMethod = bannerClass.getMethod("setRefreshInterval", int.class);
			setRefreshIntervalMethod.invoke(banner, refreshIntervalOff);

			final float scale = context.getResources().getDisplayMetrics().density;
			bannerLayout.addView((View)banner, new LayoutParams((int) (width * scale + 0.5f), (int) (height * scale + 0.5f)));

			Method loadBannerMethod = bannerClass.getMethod("loadBanner");
			loadBannerMethod.invoke(banner);
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

				if (method.getName().equals("onShowBannerScreen")) {
					if (listener != null && !reportedClick) {
						reportedClick = true;
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("onLeaveApplication")) {
					if (listener != null && !reportedClick) {
						reportedClick = true;
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("onDismissBannerScreen")) {
					reportedClick = false;
					if (listener != null) {
						listener.onBannerClosed();
					}
				} else if (method.getName().equals("onBannerRequestSucceeded")) {
					reportImpression();
					if (listener != null) {
						listener.onBannerLoaded(bannerLayout);
					}
				} else if (method.getName().equals("onBannerRequestFailed")) {
					if (listener != null) {
						listener.onBannerFailed();
					}
				}
				return null;
			}
		});
		
		return instance;
	}

	@Override
	public void destroy() {
		if (bannerLayout != null) {
			bannerLayout.removeAllViews();
			bannerLayout = null;
		}

		banner = null;
		super.destroy();
	}

}
