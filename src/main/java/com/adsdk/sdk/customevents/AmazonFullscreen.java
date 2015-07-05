package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;

public class AmazonFullscreen extends CustomEventFullscreen {

	private Object interstitial;
	private Class<?> interstitialClass;
	private Class<?> listenerClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		Class<?> adRegistrationClass;

		try {
			listenerClass = Class.forName("com.amazon.device.ads.AdListener");
			adRegistrationClass = Class.forName("com.amazon.device.ads.AdRegistration");
			interstitialClass = Class.forName("com.amazon.device.ads.InterstitialAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {

			Method setAppKeyMethod = adRegistrationClass.getMethod("setAppKey", String.class);
			setAppKeyMethod.invoke(null, adId);

			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(Activity.class);
			interstitial = interstitialConstructor.newInstance(activity);

			Method setListenerMethod = interstitialClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(interstitial, createListener());

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

				if (method.getName().equals("onAdLoaded")) {
					if (listener != null) {
						listener.onFullscreenLoaded(AmazonFullscreen.this);
					}
				} else if (method.getName().equals("onAdFailedToLoad")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onAdExpanded")) {
					if (listener != null) {
						listener.onFullscreenLeftApplication();
					}
				} else if (method.getName().equals("onAdDismissed")) {
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
			boolean shown = false;
			try {
				Method showAdMethod = interstitialClass.getMethod("showAd");
				shown = (Boolean) showAdMethod.invoke(interstitial);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (shown) {
				reportImpression();
				if (listener != null) {
					listener.onFullscreenOpened();
				}
			}
		}

	}

}
