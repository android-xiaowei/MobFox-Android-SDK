package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;

public class MoPubFullscreen extends CustomEventFullscreen {

	private Object interstitial;
	private Class<?> interstitialClass;
	private Class<?> listenerClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			interstitialClass = Class.forName("com.mopub.mobileads.MoPubInterstitial");
			listenerClass = Class.forName("com.mopub.mobileads.MoPubInterstitial$InterstitialAdListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {
			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(new Class[] { Activity.class, String.class });
			interstitial = interstitialConstructor.newInstance(activity, adId);

			Method setListenerMethod = interstitialClass.getMethod("setInterstitialAdListener", listenerClass);
			setListenerMethod.invoke(interstitial, createListener());

			Method loadMethod = interstitialClass.getMethod("load");
			loadMethod.invoke(interstitial);
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

				if (method.getName().equals("onInterstitialShown")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onInterstitialLoaded")) {
					if (listener != null) {
						listener.onFullscreenLoaded(MoPubFullscreen.this);
					}
				} else if (method.getName().equals("onInterstitialFailed")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onInterstitialDismissed")) {
					if (listener != null) {
						listener.onFullscreenClosed();
					}
				}
				else if (method.getName().equals("onInterstitialClicked")) {
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
	public void showFullscreen() {
		if (interstitial != null && interstitialClass != null) {
			try {
				Method isReadyMethod = interstitialClass.getMethod("isReady");
				if ((Boolean)isReadyMethod.invoke(interstitial)) {
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

	@Override
	public void finish() {
		if (interstitial != null && interstitialClass != null) {
			try {
				Method destroyMethod = interstitialClass.getMethod("destroy");
				destroyMethod.invoke(interstitial);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.finish();
	}

}
