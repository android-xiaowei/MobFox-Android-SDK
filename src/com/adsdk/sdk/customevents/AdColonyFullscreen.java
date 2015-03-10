package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.app.Activity;
import android.os.Handler;

public class AdColonyFullscreen extends CustomEventFullscreen {

	private static boolean initialized;
	private boolean reported;
	private Class<?> adColonyClass;
	private Class<?> listenerClass;
	private Class<?> videoAdClass;
	private Method isReadyMethod;
	private Object videoAd;

	@Override
	public void loadFullscreen(Activity activity, CustomEventFullscreenListener customEventFullscreenListener, String optionalParameters, String trackingPixel) {
		String[] adIdParts = optionalParameters.split(";");
		String clientOptions = adIdParts[0];
		String appId = adIdParts[1];
		String zoneIds = adIdParts[2];

		listener = customEventFullscreenListener;
		this.trackingPixel = trackingPixel;
		reported = false;

		try {
			adColonyClass = Class.forName("com.jirbo.adcolony.AdColony");
			listenerClass = Class.forName("com.jirbo.adcolony.AdColonyAdListener");
			videoAdClass = Class.forName("com.jirbo.adcolony.AdColonyVideoAd");
		} catch (ClassNotFoundException e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
			return;
		}

		try {

			if (!initialized) {
				Method configureMethod = adColonyClass.getMethod("configure", new Class[] { Activity.class, String.class, String.class, String[].class });
				configureMethod.invoke(null, activity, clientOptions, appId, new String[] { zoneIds });

				initialized = true;
			}
			isReadyMethod = videoAdClass.getMethod("isReady");

			Constructor<?> videoAdConstructor = videoAdClass.getConstructor();
			Method withListenerMethod = videoAdClass.getMethod("withListener", listenerClass);
			videoAd = videoAdConstructor.newInstance();
			withListenerMethod.invoke(videoAd, createListener());

			boolean isReady = (Boolean) isReadyMethod.invoke(videoAd);

			if (isReady) {
				if (listener != null && !reported) {
					reported = true;
					listener.onFullscreenLoaded(AdColonyFullscreen.this);
				}
			} else {
				Handler h = new Handler();
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						boolean isReady = false;

						try {
							isReady = (Boolean) isReadyMethod.invoke(videoAd);
						} catch (Exception e) {

						}

						if (isReady) {
							if (listener != null && !reported) {
								reported = true;
								listener.onFullscreenLoaded(AdColonyFullscreen.this);
							}
						} else {
							if (listener != null && !reported) {
								reported = true;
								listener.onFullscreenFailed();
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

				if (method.getName().equals("onAdColonyAdStarted")) {
					reportImpression();
					if (listener != null) {
						listener.onFullscreenOpened();
					}
				} else if (method.getName().equals("onAdColonyAdAttemptFinished")) {
					Method notShownMethod = videoAdClass.getMethod("notShown");
					Method noFillMethod = videoAdClass.getMethod("noFill");

					boolean notShown = (Boolean) notShownMethod.invoke(videoAd);
					boolean noFill = (Boolean) noFillMethod.invoke(videoAd);

					if (notShown || noFill) {
						if (listener != null && !reported) {
							reported = true;
							listener.onFullscreenFailed();
						}
					} else if (listener != null) {
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
		try {
			boolean isReady = (Boolean) isReadyMethod.invoke(videoAd);

			if (videoAd != null && isReady) {
				Method showMethod = videoAdClass.getMethod("show");
				showMethod.invoke(videoAd);
			}
		} catch (Exception e) {
			if (listener != null) {
				listener.onFullscreenFailed();
			}
		}
	}

}
