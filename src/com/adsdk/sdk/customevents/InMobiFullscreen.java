package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;

public class InMobiFullscreen extends CustomEventFullscreen {

	private Object interstitial;
	private static boolean isInitialized;
	private Class<?> interstitialClass;
	private Class<?> listenerClass;
	private Class<?> inMobiClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;

		try {
			inMobiClass = Class.forName("com.inmobi.commons.InMobi");
			interstitialClass = Class.forName("com.inmobi.monetization.IMInterstitial");
			listenerClass = Class.forName("com.inmobi.monetization.IMInterstitialListener");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {

			if (!isInitialized) {
				Method initializeMethod = inMobiClass.getMethod("initialize", new Class[] {Context.class, String.class});
				initializeMethod.invoke(null, activity, optionalParameters);
				isInitialized = true;
			}
			
			Constructor<?> interstitialConstructor = interstitialClass.getConstructor(new Class[] {Activity.class, String.class});
			interstitial = interstitialConstructor.newInstance(activity, optionalParameters);
			
			Method setListenerMethod = interstitialClass.getMethod("setIMInterstitialListener", listenerClass);
			setListenerMethod.invoke(interstitial, createListener());
			
			Method loadInterstitialMethod = interstitialClass.getMethod("loadInterstitial");
			loadInterstitialMethod.invoke(interstitial);
		} catch (Exception e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}
	}

	private Object createListener() {
		Object instance = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] { listenerClass }, new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				if (method.getName().equals("onShowInterstitialScreen")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onInterstitialLoaded")) {
					if (listener != null) {
						listener.onFullscreenLoaded(InMobiFullscreen.this);
					}
				} else if (method.getName().equals("onInterstitialInteraction")) {
					if (listener != null) {
						listener.onFullscreenLeftApplication();
					}
				} else if (method.getName().equals("onInterstitialFailed")) {
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onDismissInterstitialScreen")) {
					if (listener != null) {
						listener.onFullscreenClosed();
					}
				}
				return null;
			}
		});
		
		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void showFullscreen() {
		if(interstitial != null && interstitialClass != null) {
			try {
				Class<?> stateClass = Class.forName("com.inmobi.monetization.IMInterstitial$State");
				Object readyState = Enum.valueOf((Class<Enum>)stateClass, "READY");
				Method getStateMethod = interstitialClass.getMethod("getState");
				boolean ready = (getStateMethod.invoke(interstitial) == readyState);
				
				if (ready) {
					Method showMethod = interstitialClass.getMethod("show");
					showMethod.invoke(interstitial);
				}
				
			} catch (Exception e) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}
		}
	}

}
