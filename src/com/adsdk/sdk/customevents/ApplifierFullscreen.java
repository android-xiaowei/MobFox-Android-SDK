package com.adsdk.sdk.customevents;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;

public class ApplifierFullscreen extends CustomEventFullscreen {
	private static boolean initialized;
	private boolean shouldReportAvailability;
	private Class<?> unityClass;
	private Class<?> listenerClass;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		shouldReportAvailability = true;
		this.trackingPixel = trackingPixel;

		try {
			listenerClass = Class.forName("com.unity3d.ads.android.IUnityAdsListener");
			unityClass = Class.forName("com.unity3d.ads.android.UnityAds");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {
			if (!initialized) {
				Method initMethod;
				initMethod = unityClass.getMethod("init", new Class[] { Activity.class, String.class, listenerClass });

				initMethod.invoke(null, activity, adId, createListener());

				initialized = true;
			} else {
				Method canShowAdsMethod = unityClass.getMethod("canShowAds");
				boolean canShow = (Boolean) canShowAdsMethod.invoke(null);

				if (canShow) {
					shouldReportAvailability = false;
					if (listener != null) {
						listener.onFullscreenLoaded(this);
					}
					Method setListenerMethod = unityClass.getMethod("setListener", listenerClass);
					setListenerMethod.invoke(null, createListener());
				} else {
					shouldReportAvailability = false;
					if (listener != null) {
						listener.onFullscreenFailed();
					}
				}
			}
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

				if (method.getName().equals("onShow")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onHide")) {
					if (listener != null) {
						listener.onFullscreenClosed();
					}

				} else if (method.getName().equals("onFetchFailed")) {
					if (listener != null && shouldReportAvailability) {
						listener.onFullscreenFailed();
					}
				} else if (method.getName().equals("onFetchCompleted")) {
					if (listener != null && shouldReportAvailability) {
						listener.onFullscreenLoaded(ApplifierFullscreen.this);
					}
				}
				return null;
			}
		});

		return instance;

	}

	@Override
	public void showFullscreen() {
		try {

			Method canShowAdsMethod = unityClass.getMethod("canShowAds");
			boolean canShowAds = (Boolean) canShowAdsMethod.invoke(null);

			Method canShowMethod = unityClass.getMethod("canShow");
			boolean canShow = (Boolean) canShowMethod.invoke(null);

			if (canShow && canShowAds) {
				Method showMethod = unityClass.getMethod("show");
				showMethod.invoke(null);
			} else if (listener != null) {
				listener.onFullscreenFailed();
			}
		} catch (Exception e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}

	}

}
