package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;

public class MillennialFullscreen extends CustomEventFullscreen {

	private Object interstitial;
	private Class<?> listenerClass;
	private Class<?> interstitialClass;
	private boolean wasTapped;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;
		Class<?> requestClass;

		try {
			interstitialClass = Class.forName("com.millennialmedia.android.MMInterstitial");
			requestClass = Class.forName("com.millennialmedia.android.MMRequest");
			listenerClass = Class.forName("com.millennialmedia.android.RequestListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {
			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(Context.class);
			interstitial = interstitialConstructor.newInstance(activity);

			Method setListenerMethod = interstitialClass.getMethod("setListener", listenerClass);
			setListenerMethod.invoke(interstitial, createListener());

			Method setApidMethod = interstitialClass.getMethod("setApid", String.class);
			setApidMethod.invoke(interstitial, adId);

			Constructor<?> requestConstructor = requestClass.getConstructor();
			Object request = requestConstructor.newInstance();

			Method setMMRequestMethod = interstitialClass.getMethod("setMMRequest", requestClass);
			setMMRequestMethod.invoke(interstitial, request);

			Method isAdAvailableMethod = interstitialClass.getMethod("isAdAvailable");

			if ((Boolean) isAdAvailableMethod.invoke(interstitial)) {
				if (listener != null) {
					listener.onFullscreenLoaded(this);
				}
			} else {
				Method fetchMethod = interstitialClass.getMethod("fetch");
				fetchMethod.invoke(interstitial);
			}
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

				if (method.getName().equals("requestFailed")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("requestCompleted")) {
					if (listener != null) {
						listener.onFullscreenLoaded(MillennialFullscreen.this);
					}
				} else if (method.getName().equals("onSingleTap")) {
					if (listener != null && wasTapped) { // millennial reports tap also on close button "X" click
						listener.onFullscreenLeftApplication();
					}
					wasTapped = true;
				} else if (method.getName().equals("MMAdOverlayLaunched")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("MMAdOverlayClosed")) {
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
				Method isAdAvailableMethod = interstitialClass.getMethod("isAdAvailable");
				if ((Boolean) isAdAvailableMethod.invoke(interstitial)) {
					Method displayMethod = interstitialClass.getMethod("display");
					displayMethod.invoke(interstitial);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
