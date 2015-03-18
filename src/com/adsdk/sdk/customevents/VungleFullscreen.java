package com.adsdk.sdk.customevents;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

//Based on Vungle SDK 3.3.0
public class VungleFullscreen extends CustomEventFullscreen {

	private Object vunglePub;
	private Object eventListenerArray;
	private Class<?> vungleClass;
	private Class<?> listenerClass;

	private boolean alreadyReportedAdLoadStatus;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String adId = optionalParameters;
		listener = customEventFullscreenListener;
		alreadyReportedAdLoadStatus = false;
		this.trackingPixel = trackingPixel;

		try {
			listenerClass = Class.forName("com.vungle.publisher.EventListener");
			vungleClass = Class.forName("com.vungle.publisher.VunglePub");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {

			Method getInstanceMethod = vungleClass.getMethod("getInstance");
			vunglePub = getInstanceMethod.invoke(null);

			Method initMethod = vungleClass.getMethod("init", new Class[] { Context.class, String.class });
			initMethod.invoke(vunglePub, activity, adId);

			Object eventListener = createListener();
			eventListenerArray = Array.newInstance(listenerClass, 1);
			Array.set(eventListenerArray, 0, eventListener);
			Method addEventListenersMethod = vungleClass.getMethod("addEventListeners", eventListenerArray.getClass());

			addEventListenersMethod.invoke(vunglePub, eventListenerArray);

			final Method isAdPlayableMethod = vungleClass.getMethod("isAdPlayable");

			if ((Boolean) isAdPlayableMethod.invoke(vunglePub)) {
				if (listener != null) {
					listener.onFullscreenLoaded(this);
				}
			} else {
				Handler h = new Handler();
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						try {
							if ((Boolean) isAdPlayableMethod.invoke(vunglePub)) {
								if (listener != null && !alreadyReportedAdLoadStatus) {
									listener.onFullscreenLoaded(VungleFullscreen.this);
									alreadyReportedAdLoadStatus = true;
								}
							} else {
								if (listener != null && !alreadyReportedAdLoadStatus) {
									listener.onFullscreenFailed();
									alreadyReportedAdLoadStatus = true;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							if (listener != null && !alreadyReportedAdLoadStatus) {
								listener.onFullscreenFailed();
								alreadyReportedAdLoadStatus = true;
							}
						}

					}
				}, 5000);
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

				if (method.getName().equals("onAdEnd")) {
					if (listener != null && args[0] != null) {
						if ((Boolean) args[0]) {
							listener.onFullscreenLeftApplication();
						}
						listener.onFullscreenClosed();
					}
				} else if (method.getName().equals("onAdStart")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onAdUnavailable")) {
					if (listener != null && !alreadyReportedAdLoadStatus) {
						listener.onFullscreenFailed();
						alreadyReportedAdLoadStatus = true;
					}
				} else if (method.getName().equals("onAdPlayableChanged")) {
					if (listener != null && !alreadyReportedAdLoadStatus && args[0] != null && (Boolean) args[0]) {
						listener.onFullscreenLoaded(VungleFullscreen.this);
						alreadyReportedAdLoadStatus = true;
					}
				} else if (method.getName().equals("hashCode")) {
					return hashCode();
				} else if (method.getName().equals("equals")) {
					return equals(args[0]);
				}
				return null;
			}

		});

		return instance;

	}

	@Override
	public void finish() {
		super.finish();
		if (vunglePub != null && vungleClass != null) {
			try {
				Method onPauseMethod = vungleClass.getMethod("onPause");
				onPauseMethod.invoke(vunglePub);
				if (eventListenerArray != null) {
					Method removeEventListenersMethod = vungleClass.getMethod("removeEventListeners", eventListenerArray.getClass());
					removeEventListenersMethod.invoke(vunglePub, eventListenerArray);
				}
			} catch (Exception e) {
			}
		}
		eventListenerArray = null;
		vunglePub = null;
	}

	@Override
	public void showFullscreen() {
		if (vunglePub != null && vungleClass != null) {
			try {
				final Method isAdPlayableMethod = vungleClass.getMethod("isAdPlayable");
				if ((Boolean) isAdPlayableMethod.invoke(vunglePub)) {
					Method playAdMethod = vungleClass.getMethod("playAd");
					playAdMethod.invoke(vunglePub);
				}
			} catch (Exception e) {
				if (listener != null) {
					listener.onFullscreenFailed();
				}
			}
		}

	}

}
