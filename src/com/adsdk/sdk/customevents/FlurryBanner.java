package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class FlurryBanner extends CustomEventBanner {

	private String adSpace;
	private Context context;
	private FrameLayout bannerLayout;
	private Class<?> bannerClass;
	private Class<?> listenerClass;
	private Class<?> flurryAgentClass;
	private Object banner;

	@Override
	public void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, String optionalParameters, String trackingPixel, int width, int height) {
		String[] adIdParts = optionalParameters.split(";");
		if (adIdParts.length != 2) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}
		this.context = context;
		adSpace = adIdParts[0];
		String apiKey = adIdParts[1];
		listener = customEventBannerListener;
		this.trackingPixel = trackingPixel;

		try {
			listenerClass = Class.forName("com.flurry.android.ads.FlurryAdBannerListener");
			bannerClass = Class.forName("com.flurry.android.ads.FlurryAdBanner");
			flurryAgentClass = Class.forName("com.flurry.android.FlurryAgent");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onBannerFailed();
			}
			return;
		}

		try {
			Method initMethod = flurryAgentClass.getMethod("init", new Class[] { Context.class, String.class });
			initMethod.invoke(null, context, apiKey);
			
			Method onStartSessionMethod = flurryAgentClass.getMethod("onStartSession", new Class[] { Context.class, String.class });
			onStartSessionMethod.invoke(null, context, apiKey);

			bannerLayout = new FrameLayout(context);

			Constructor<?> bannerConstructor = bannerClass.getConstructor(new Class[] { Context.class, ViewGroup.class, String.class });
			banner = bannerConstructor.newInstance(context, bannerLayout, adSpace);

			Method setListenerMethod = bannerClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(banner, createListener());

			Method fetchAdMethod = bannerClass.getMethod("fetchAd");
			fetchAdMethod.invoke(banner);
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

				if (method.getName().equals("onShowFullscreen")) {
					if (listener != null) {
						listener.onBannerExpanded();
					}
				} else if (method.getName().equals("onFetched")) {
					try {
						Method displayAdMethod = bannerClass.getMethod("displayAd");
						displayAdMethod.invoke(banner);
						reportImpression();
						if (listener != null) {
							listener.onBannerLoaded(bannerLayout);
						}
					} catch (Exception e) {
						if (listener != null) {
							listener.onBannerFailed();
						}
					}
				} else if (method.getName().equals("onError")) {
					if (listener != null) {
						listener.onBannerFailed();
					}
				} else if (method.getName().equals("onCloseFullscreen")) {
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
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	@Override
	public void destroy() {
		try {
			if (context != null && flurryAgentClass != null) {
				Method onEndSessionMethod = flurryAgentClass.getMethod("onEndSession", Context.class);
				onEndSessionMethod.invoke(null, context);
			}

			if (banner != null && bannerClass != null) {
				Method destroyMethod = bannerClass.getMethod("destroy");
				destroyMethod.invoke(banner);
			}
		} catch (Exception e) {
		}
		super.destroy();
	}

}
