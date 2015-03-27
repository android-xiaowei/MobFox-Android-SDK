package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;

public class FacebookFullscreen extends CustomEventFullscreen {

	private Object interstitial;
	private Class<?> interstitialClass;
	private Class<?> listenerClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			Class.forName("com.facebook.ads.Ad");
			Class.forName("com.facebook.ads.AdError");
			interstitialClass = Class.forName("com.facebook.ads.InterstitialAd");
			listenerClass = Class.forName("com.facebook.ads.InterstitialAdListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {
			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(new Class[] { Context.class, String.class });
			interstitial = interstitialConstructor.newInstance(activity, adId);

			Method setAdListenerMethod = interstitialClass.getMethod("setAdListener", listenerClass);
			setAdListenerMethod.invoke(interstitial, createListener());

			Method loadAdMethod = interstitialClass.getMethod("loadAd");
			loadAdMethod.invoke(interstitial);
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

				if (method.getName().equals("onError")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onAdLoaded")) {
					if (listener != null) {
						listener.onFullscreenLoaded(FacebookFullscreen.this);
					}
				} else if (method.getName().equals("onAdClicked")) {
					if (listener != null) {
						listener.onFullscreenLeftApplication();
					}
				} else if (method.getName().equals("onInterstitialDisplayed")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onInterstitialDismissed")) {
					if (listener != null) {
						listener.onFullscreenClosed();
					}
				}
				return null;
			}
		});

		return instance;

	}

	@Override
	public void showFullscreen() {
		if (interstitial != null && interstitialClass != null) {
			try {

				Method isAdLoadedMethod = interstitialClass.getMethod("isAdLoaded");
				boolean isLoaded = (Boolean) isAdLoadedMethod.invoke(interstitial);
				if (isLoaded) {
					Method showMethod = interstitialClass.getMethod("show");
					showMethod.invoke(interstitial);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}

		}

	}

}
