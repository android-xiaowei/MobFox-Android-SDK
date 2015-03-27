package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;

public class FlurryFullscreen extends CustomEventFullscreen {
	private Context context;
	private String adSpace;
	private Object interstitial;
	private Class<?> interstitialClass;
	private Class<?> listenerClass;
	private Class<?> flurryAgentClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		listener = customEventFullscreenListener;
		String[] adIdParts = optionalParameters.split(";");
		if (adIdParts.length != 2) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}
		this.context = activity;
		adSpace = adIdParts[0];
		String apiKey = adIdParts[1];

		this.trackingPixel = trackingPixel;

		try {
			flurryAgentClass = Class.forName("com.flurry.android.FlurryAgent");
			interstitialClass = Class.forName("com.flurry.android.ads.FlurryAdInterstitial");
			listenerClass = Class.forName("com.flurry.android.ads.FlurryAdInterstitialListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {
			Method initMethod = flurryAgentClass.getMethod("init", new Class[] { Context.class, String.class });
			initMethod.invoke(null, context, apiKey);

			Method onStartSessionMethod = flurryAgentClass.getMethod("onStartSession", new Class[] { Context.class, String.class });
			onStartSessionMethod.invoke(null, context, apiKey);

			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(new Class[] { Context.class, String.class });
			interstitial = interstitialConstructor.newInstance(context, adSpace);

			Method setListenerMethod = interstitialClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(interstitial, createListener());

			Method fetchAdMethod = interstitialClass.getMethod("fetchAd");
			fetchAdMethod.invoke(interstitial);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}
	}

	private Object createListener() {
		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onFetched")) {
					if (listener != null) {
						listener.onFullscreenLoaded(FlurryFullscreen.this);
					}
				} else if (method.getName().equals("onError")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onDisplay")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onClose")) {
					if (listener != null) {
						listener.onFullscreenClosed();
					}
				} else if (method.getName().equals("onAppExit")) {
					if (listener != null) {
						listener.onFullscreenLeftApplication();
					}
				}
				return null;
			}
		});
		
		return instance;
	}

	@Override
	public void finish() {

		try {
			if (interstitial != null && interstitialClass != null) {
				Method destroyMethod = interstitialClass.getMethod("destroy");
				destroyMethod.invoke(interstitial);
			}
			interstitial = null;
			
			if(flurryAgentClass != null) {
				Method onEndSessionMethod = flurryAgentClass.getMethod("onEndSession", Context.class);
				onEndSessionMethod.invoke(null, context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.finish();
	}

	@Override
	public void showFullscreen() {
		try {
			Method displayAdMethod = interstitialClass.getMethod("displayAd");
			displayAdMethod.invoke(interstitial);
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}
		
		
	}
}
