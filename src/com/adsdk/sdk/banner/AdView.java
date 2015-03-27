package com.adsdk.sdk.banner;

import static com.adsdk.sdk.Const.TAG;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.Timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdRequest;
import com.adsdk.sdk.AdResponse;
import com.adsdk.sdk.Const;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.Log;
import com.adsdk.sdk.RequestGeneralAd;
import com.adsdk.sdk.Util;
import com.adsdk.sdk.banner.BannerAdView.BannerAdViewListener;
import com.adsdk.sdk.customevents.CustomEvent;
import com.adsdk.sdk.customevents.CustomEventBanner;
import com.adsdk.sdk.customevents.CustomEventBanner.CustomEventBannerListener;
import com.adsdk.sdk.customevents.CustomEventBannerFactory;
import com.adsdk.sdk.mraid.MraidView;
import com.adsdk.sdk.mraid.MraidView.MraidListener;
import com.adsdk.sdk.mraid.MraidView.ViewState;

public class AdView extends FrameLayout {

	private static final int CUSTOM_EVENT_RELOAD_INTERVAL = 30;
	public static final int LIVE = 0;
	public static final int TEST = 1;

	private boolean includeLocation = false;
	private String publisherId;
	private boolean animation;
	private boolean shouldNotifyClose;

	private int adspaceWidth;
	private int adspaceHeight;
	private boolean adspaceStrict;
	private Gender userGender;
	private int userAge;
	private List<String> keywords;

	private BannerAdView mBannerView;
	private MraidView mMRAIDView;
	private CustomEventBanner customEventBanner;
	private View customEventBannerView;

	private Timer reloadTimer;
	private int customReloadTime;
	private boolean isAutoreloadingActive = true;
	private boolean isInternalBrowser = false;

	private AdResponse response;
	private AdRequest request;

	private String requestURL = null;

	private BroadcastReceiver mScreenStateReceiver;
	private Context mContext = null;
	protected boolean mIsInForeground;

	private AdListener adListener;

	private Thread loadContentThread;

	private InputStream xml;

	private final Handler handler = new Handler();

	private final Runnable showContent = new Runnable() {
		@Override
		public void run() {
			try {
				AdView.this.showContent();
			} catch (Exception e) {
				notifyLoadAdFailed(e);
			}
		}
	};
	private FrameLayout MRAIDFrame;
	private CustomEventBannerListener customAdListener;

	public void setAdspaceWidth(int width) {
		adspaceWidth = width;
	}

	public void setAdspaceHeight(int height) {
		adspaceHeight = height;
	}

	public void setAdspaceStrict(boolean strict) {
		adspaceStrict = strict;
	}

	public AdView(final Context context, final AttributeSet attributes) {
		super(context, attributes);
		mContext = context;
		if (attributes != null) {
			int count = attributes.getAttributeCount();
			for (int i = 0; i < count; i++) {
				String name = attributes.getAttributeName(i);
				if (name.equals("publisherId")) {
					this.publisherId = attributes.getAttributeValue(i);
				} else if (name.equals("request_url")) {
					this.requestURL = attributes.getAttributeValue(i);
				} else if (name.equals("animation")) {
					this.animation = attributes.getAttributeBooleanValue(i, false);
				} else if (name.equals("location")) {
					this.includeLocation = attributes.getAttributeBooleanValue(i, false);
				} else if (name.equals("adspaceStrict")) {
					this.adspaceStrict = attributes.getAttributeBooleanValue(i, false);
				} else if (name.equals("adspaceWidth")) {
					this.adspaceWidth = attributes.getAttributeIntValue(i, 0);
				} else if (name.equals("adspaceHeight")) {
					this.adspaceHeight = attributes.getAttributeIntValue(i, 0);
				}
			}
		}

		initialize(context);
	}

	public AdView(final Context context, final String requestURL, final String publisherId) {
		this(context, requestURL, publisherId, false, false);
	}

	public AdView(final Context context, final String requestURL, final InputStream xml, final String publisherId, final boolean includeLocation, final boolean animation) {
		this(context, xml, requestURL, publisherId, includeLocation, animation);
	}

	public AdView(final Context context, final InputStream xml, final String requestURL, final String publisherId, final boolean includeLocation, final boolean animation) {
		super(context);
		this.xml = xml;
		this.requestURL = requestURL;
		mContext = context;
		this.publisherId = publisherId;
		this.includeLocation = includeLocation;
		this.animation = animation;
		this.initialize(context);
	}

	public AdView(final Context context, final String requestURL, final String publisherId, final boolean includeLocation, final boolean animation) {
		this(context, requestURL, publisherId, includeLocation, animation, null);
	}

	public AdView(final Context context, final String requestURL, final String publisherId, final boolean includeLocation, final boolean animation, final AdListener listener) {
		super(context);
		this.requestURL = requestURL;
		mContext = context;
		this.publisherId = publisherId;
		this.includeLocation = includeLocation;
		this.animation = animation;
		this.adListener = listener;
		Log.d("AdListener: " + (adListener == null));
		this.initialize(context);
	}

	@Override
	protected void onAttachedToWindow() {

		super.onAttachedToWindow();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		mContext.registerReceiver(mScreenStateReceiver, filter);
		Log.v(Const.TAG, "onAttachedToWindow");
	}

	@Override
	protected void onDetachedFromWindow() {

		super.onDetachedFromWindow();
		unregisterScreenStateBroadcastReceiver();
		Log.v(Const.TAG, "onDetachedFromWindow");
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public int getRefreshRate() {
		if (this.response != null)
			return this.response.getRefresh();
		return -1;
	}

	private AdRequest getRequest() {
		if (this.request == null) {
			this.request = new AdRequest();
			this.request.setAndroidAdId(Util.getAndroidAdId());
			this.request.setAdDoNotTrack(Util.hasAdDoNotTrack());
			this.request.setPublisherId(this.publisherId);
			this.request.setUserAgent(Util.getDefaultUserAgentString(mContext));
			this.request.setUserAgent2(Util.buildUserAgent());
			Log.d(Const.TAG, "WebKit UserAgent:" + this.request.getUserAgent());
			Log.d(Const.TAG, "SDK built UserAgent:" + this.request.getUserAgent2());
		}
		request.setGender(userGender);
		request.setUserAge(userAge);
		request.setKeywords(keywords);
		Location location = null;
		if (this.includeLocation)
			location = Util.getLocation(mContext);
		if (location != null) {
			Log.d(Const.TAG, "location is longitude: " + location.getLongitude() + ", latitude: " + location.getLatitude());
			this.request.setLatitude(location.getLatitude());
			this.request.setLongitude(location.getLongitude());
		} else {
			this.request.setLatitude(0.0);
			this.request.setLongitude(0.0);
		}
		this.request.setAdspaceHeight(adspaceHeight);
		this.request.setAdspaceWidth(adspaceWidth);
		this.request.setAdspaceStrict(adspaceStrict);

		this.request.setRequestURL(requestURL);
		return this.request;
	}

	private void initialize(final Context context) {
		Log.LOGGING_ENABLED = Log.isLoggingEnabled(mContext);
		Log.d(Const.TAG, "SDK Version:" + Const.VERSION);
		registerScreenStateBroadcastReceiver();
		Util.prepareAndroidAdId(context);
		customAdListener = createCustomAdListener();
	}

	public boolean isInternalBrowser() {
		return this.isInternalBrowser;
	}

	private void registerScreenStateBroadcastReceiver() {
		mScreenStateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					if (mIsInForeground) {
						Log.d(Const.TAG, "Screen sleep with ad in foreground, disable refresh");
						pause();
					} else {
						Log.d(Const.TAG, "Screen sleep but ad in background; " + "refresh should already be disabled");
					}
				} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
					if (mIsInForeground) {
						resume();
						Log.d(Const.TAG, "Screen wake / ad in foreground, reset refresh");
					} else {
						Log.d(Const.TAG, "Screen wake but ad in background; don't enable refresh");
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		mContext.registerReceiver(mScreenStateReceiver, filter);
	}

	private void loadContent() {
		Log.d(Const.TAG, "load content");

		if (this.loadContentThread == null) {
			this.loadContentThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d(Const.TAG, "starting request thread");
					final RequestGeneralAd requestAd;
					if (xml == null)
						requestAd = new RequestGeneralAd();
					else
						requestAd = new RequestGeneralAd(xml);

					try {
						AdView.this.response = requestAd.sendRequest(AdView.this.getRequest());
						if (AdView.this.response != null) {
							Log.d(Const.TAG, "response received");
							Log.d(Const.TAG, "getVisibility: " + AdView.this.getVisibility());
							AdView.this.handler.post(AdView.this.showContent);
						}
					} catch (final Throwable e) {
						AdView.this.notifyLoadAdFailed(e);
					}
					AdView.this.loadContentThread = null;
					Log.d(Const.TAG, "finishing request thread");
				}

			});
			this.loadContentThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(final Thread thread, final Throwable ex) {
					Log.e(Const.TAG, "Exception in request thread", ex);
					AdView.this.loadContentThread = null;
				}
			});
			this.loadContentThread.start();
		}

	}

	public void loadNextAd() {
		Log.d(Const.TAG, "load next ad");
		this.loadContent();
	}

	private void notifyNoAd() {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(Const.TAG, "No Ad");

				if (adListener != null)
					adListener.noAdFound();
			}
		});
	}

	private void notifyLoadAdFailed(final Throwable e) {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				Log.e(Const.TAG, "Exception when building ad:", e);
				if (AdView.this.adListener != null) {
					Log.d(Const.TAG, "notify bannerListener: " + AdView.this.adListener.getClass().getName());
					adListener.noAdFound();
				}
			}
		});
	}

	private void notifyAdClicked() {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					adListener.adClicked();
				}
			}
		});
	}

	private void notifyAdShown() {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					adListener.adShown(response, true);
				}
			}
		});
	}

	private void notifyAdClosed() {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					adListener.adClosed(response, true);
				}
			}
		});
	}

	private void notifyLoadAdSucceeded() {
		this.handler.post(new Runnable() {

			@Override
			public void run() {
				if (adListener != null) {
					adListener.adLoadSucceeded(response);
				}
			}
		});
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);

		if (visibility == VISIBLE) {
			mIsInForeground = true;
			resume();
		} else {
			mIsInForeground = false;
			pause();
		}
		Log.d(TAG, "onWindowVisibilityChanged: " + visibility);
	}

	private void showContent() {
		shouldNotifyClose = false;
		if (mBannerView != null) {
			this.removeView(mBannerView);
			mBannerView = null;
		}
		if (mMRAIDView != null) {
			mMRAIDView.destroy();
			this.removeView(mMRAIDView);
			mMRAIDView = null;
		}
		if (MRAIDFrame != null) {
			this.removeView(MRAIDFrame);
			MRAIDFrame = null;
		}
		if (customEventBannerView != null) {
			this.removeView(customEventBannerView);
			customEventBannerView = null;
		}
		destroyCustomEventBanner();

		if (response.getType() == Const.TEXT || response.getType() == Const.IMAGE) {
			mBannerView = new BannerAdView(mContext, response, adspaceWidth, adspaceHeight, animation, createBannerAdViewListener());
			if (response.getCustomEvents().isEmpty()) {
				mBannerView.showContent();
				this.addView(mBannerView);
			}
		}
		if (response.getType() == Const.MRAID) {
			mMRAIDView = new MraidView(mContext);
			MRAIDFrame = new FrameLayout(mContext);
			MRAIDFrame.addView(mMRAIDView);

			if (response.getCustomEvents().isEmpty()) {
				addMRAIDBannerView();
			}

			mMRAIDView.setMraidListener(createMraidListener());
			mMRAIDView.loadHtmlData(response.getText());

		}
		if (response.getType() == Const.NO_AD) {
			if (response.getCustomEvents().isEmpty()) {
				notifyNoAd();
			}
		}

		if (!response.getCustomEvents().isEmpty()) {
			loadCustomEventBanner();
			if (customEventBanner == null) {
				response.getCustomEvents().clear();
				customAdListener.onBannerFailed();
			} else {
				response.setRefresh(CUSTOM_EVENT_RELOAD_INTERVAL);
			}
		}

		this.startReloadTimerInternal();
	}

	private BannerAdViewListener createBannerAdViewListener() {
		return new BannerAdViewListener() {

			@Override
			public void onLoad() {
				notifyLoadAdSucceeded();
			}

			@Override
			public void onClick() {
				shouldNotifyClose = true;
				notifyAdClicked();
				notifyAdShown();
			}
		};
	}

	private void addMRAIDBannerView() {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		if (adspaceHeight != 0 && adspaceWidth != 0) {
			AdView.this.addView(MRAIDFrame, new FrameLayout.LayoutParams((int) (adspaceWidth * scale + 0.5f), (int) (adspaceHeight * scale + 0.5f)));
		} else {
			AdView.this.addView(MRAIDFrame, new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, (int) (50 * scale + 0.5f)));
		}
	}

	private void loadCustomEventBanner() {
		customEventBanner = null;
		while (!response.getCustomEvents().isEmpty() && customEventBanner == null) {
			try {
				final CustomEvent event = response.getCustomEvents().get(0);
				response.getCustomEvents().remove(event);
				customEventBanner = CustomEventBannerFactory.create(event.getClassName());
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (adspaceHeight != 0 && adspaceWidth != 0) {
							customEventBanner.loadBanner(mContext, customAdListener, event.getOptionalParameter(), event.getPixelUrl(), adspaceWidth, adspaceHeight);
						} else {
							customEventBanner.loadBanner(mContext, customAdListener, event.getOptionalParameter(), event.getPixelUrl(), 300, 50);
						}

					}
				});

			} catch (Exception e) {
				customEventBanner = null;
				Log.d("Failed to create Custom Event Banner.");
			}
		}

	}

	private CustomEventBannerListener createCustomAdListener() {
		return new CustomEventBannerListener() {

			@Override
			public void onBannerLoaded(View bannerView) {
				if (customEventBannerView != null) {
					AdView.this.removeView(customEventBannerView);
				}

				customEventBannerView = bannerView;
				AdView.this.addView(bannerView);
				notifyLoadAdSucceeded();
			}

			@Override
			public void onBannerFailed() {
				destroyCustomEventBanner();
				loadCustomEventBanner();
				if (customEventBanner != null) {
					return;
				} else if (mBannerView != null) {
					mBannerView.showContent();
					AdView.this.addView(mBannerView);
				} else if (mMRAIDView != null) {
					addMRAIDBannerView();
				} else {
					notifyNoAd();
				}
			}

			@Override
			public void onBannerExpanded() {
				shouldNotifyClose = true; // for custom events not notifying about ad close
				notifyAdClicked();
				notifyAdShown();
			}

			@Override
			public void onBannerClosed() {
				if (shouldNotifyClose) {
					shouldNotifyClose = false;
					notifyAdClosed();
				}
			}
		};
	}

	private void destroyCustomEventBanner() {
		if (customEventBanner != null) {
			customEventBanner.destroy();
		}
	}

	private MraidListener createMraidListener() {
		return new MraidListener() {

			@Override
			public void onReady(MraidView arg0) {
				notifyLoadAdSucceeded();
			}

			@Override
			public void onFailure(MraidView arg0) {
				notifyNoAd();
			}

			@Override
			public void onExpand(MraidView arg0) {
				notifyAdClicked();
				notifyAdShown();
			}

			@Override
			public void onClose(MraidView arg0, ViewState arg1) {
				notifyAdClosed();
			}
		};
	}

	public void release() {
		unregisterScreenStateBroadcastReceiver();
		destroyCustomEventBanner();
		if (mMRAIDView != null)
			mMRAIDView.destroy();
	}

	private void unregisterScreenStateBroadcastReceiver() {
		try {
			mContext.unregisterReceiver(mScreenStateReceiver);
		} catch (Exception IllegalArgumentException) {
			Log.d("Failed to unregister screen state broadcast receiver (never registered).");
		}
	}

	public void setAdListener(final AdListener bannerListener) {
		this.adListener = bannerListener;
		if (mMRAIDView != null) {
			mMRAIDView.setMraidListener(createMraidListener());
		}
		if (mBannerView != null) {
			mBannerView.setAdListener(createBannerAdViewListener());
		}
	}

	public void setInternalBrowser(final boolean isInternalBrowser) {
		this.isInternalBrowser = isInternalBrowser;
	}

	public int getRefreshTime() {
		if (this.response == null) {
			return -1;
		}
		return this.response.getRefresh() * 1000;
	}

	public void resume() {

		if (shouldNotifyClose) {
			shouldNotifyClose = false;
			notifyAdClosed();
		}

		Log.d(Const.TAG, "response: " + this.response);

		if (this.response != null && (this.response.getRefresh() > 0 || customReloadTime > 0))
			this.startReloadTimerInternal();
		else if (this.response == null || (mMRAIDView == null && mBannerView == null))
			this.loadContent();
	}

	public void pause() {
		if (this.reloadTimer != null)
			try {
				Log.d(Const.TAG, "cancel reload timer");
				stopReloadTimerInternal();
			} catch (final Exception e) {
				Log.e(Const.TAG, "unable to cancel reloadTimer", e);
			}
	}

	public void setRefreshTime(int time) {
		customReloadTime = time;
		if (time > 0 && response != null) {
			stopReloadTimerInternal();
			startReloadTimerInternal();
		}

	}

	public void startReloadTimer() {
		isAutoreloadingActive = true;
		startReloadTimerInternal();
	}

	private void startReloadTimerInternal() {
		Log.d(Const.TAG, "start reload timer");
		if ((response.getRefresh() <= 0 && customReloadTime <= 0) || !isAutoreloadingActive) {
			return;
		}
		if (this.reloadTimer == null) {
			this.reloadTimer = new Timer();
		}

		final int refreshTime;
		if (customReloadTime > 0) {
			refreshTime = customReloadTime * 1000;
		} else {
			refreshTime = this.response.getRefresh() * 1000;
		}

		Log.d(Const.TAG, "set timer: " + refreshTime);

		final ReloadTask reloadTask = new ReloadTask(AdView.this);
		this.reloadTimer.schedule(reloadTask, refreshTime);
	}

	public void stopReloadTimer() {
		isAutoreloadingActive = false;
		stopReloadTimerInternal();
	}

	private void stopReloadTimerInternal() {
		Log.d(Const.TAG, "stop reload timer");
		if (this.reloadTimer == null) {
			return;
		}
		this.reloadTimer.cancel();
		this.reloadTimer = null;
	}

	public void setUserGender(Gender userGender) {
		this.userGender = userGender;
	}

	public void setUserAge(int userAge) {
		this.userAge = userAge;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

}
