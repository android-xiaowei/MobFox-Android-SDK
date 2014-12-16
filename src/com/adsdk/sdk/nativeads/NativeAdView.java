package com.adsdk.sdk.nativeads;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;
import com.adsdk.sdk.nativeads.NativeAd.Tracker;

@SuppressLint("ViewConstructor")
public class NativeAdView extends FrameLayout {
	private boolean impressionReported;
	private View adView;
	private View overlayView;
	private NativeAdListener listener;
	private NativeAd nativeAd;
	private Handler handler;
	private List<Tracker> trackers;
	private Context context;

	public NativeAdView(Context context, NativeAd ad, NativeViewBinder binder, NativeAdListener listener) {
		super(context);
		if (ad == null || binder == null) {
			return;
		}
		this.context = context;
		adView = inflate(context, binder.getAdLayoutId(), null);
		overlayView = new View(context);
		trackers = ad.getTrackers();
		handler = new Handler();
		this.listener = listener;
		fillAdView(ad, binder);
		ad.prepareImpression(adView);
		overlayView.setOnClickListener(createOnNativeAdClickListener(ad));
		this.addView(adView);
		
		overlayView.setLayoutParams(new LayoutParams(adView.getWidth(), adView.getHeight()));
		this.addView(overlayView);
	}

	public void fillAdView(NativeAd ad, NativeViewBinder binder) {
		for (String key : binder.getTextAssetsBindingsKeySet()) {
			int resId = binder.getIdForTextAsset(key);
			if (resId == 0) {
				continue;
			}
			try {
				if (key.equals("rating")) { // rating is special, not displayed as normal text view.
					RatingBar bar = (RatingBar) adView.findViewById(resId);
					if (bar != null) {
						String ratingString = ad.getTextAsset(key);
						if (ratingString != null && ratingString.length() > 0) {
							try {
								int rating = Integer.parseInt(ratingString);
								bar.setIsIndicator(true);
								bar.setRating(rating);
							} catch (NumberFormatException e) {
								Log.d("Cannot parse rating string: " + ratingString);
								bar.setVisibility(INVISIBLE);
							}
						} else {
							bar.setVisibility(INVISIBLE);
						}
					}
				} else {
					TextView view = (TextView) adView.findViewById(resId);
					String text = ad.getTextAsset(key);
					if (view != null && text != null) {
						view.setText(text);
					}
				}
			} catch (ClassCastException e) {
				Log.e("Cannot fill view for " + key);
			}
		}

		for (String key : binder.getImageAssetsBindingsKeySet()) {
			int resId = binder.getIdForImageAsset(key);
			if (resId == 0) {
				continue;
			}
			try {
				ImageView view = (ImageView) adView.findViewById(resId);
				Bitmap imageBitmap = ad.getImageAsset(key).bitmap;
				if (view != null && imageBitmap != null) {
					view.setImageBitmap(imageBitmap);
				}
			} catch (ClassCastException e) {
				Log.e("Cannot fill view for " + key);
			}
		}
		nativeAd = ad;

	}
	
	private OnClickListener createOnNativeAdClickListener(final NativeAd ad) {
		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyAdClicked();
				ad.handleClick();
				adView.performClick();
				if (ad.getClickUrl() != null && !ad.getClickUrl().equals("")) {
					new LoadUrlTask().execute(ad.getClickUrl());
				}

			}
			
		};
		return clickListener;
	}
	
	private class LoadUrlTask extends AsyncTask<String, Void, String> {

		String userAgent;

		public LoadUrlTask(){
			userAgent = Util.getDefaultUserAgentString(context);
		}

		@Override
		protected String doInBackground(String... urls) {
			String loadingUrl = urls[0];
			URL url = null;
			try {
				url = new URL(loadingUrl);
			} catch (MalformedURLException e) {
				return (loadingUrl != null) ? loadingUrl : "";
			}
			Log.d("Checking URL redirect:" + loadingUrl);

			int statusCode = -1;
			HttpURLConnection connection = null;
			String nextLocation = url.toString();

			Set<String> redirectLocations = new HashSet<String>();
			redirectLocations.add(nextLocation);

			try {
				do {
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestProperty("User-Agent",
							userAgent);
					connection.setInstanceFollowRedirects(false);

					statusCode = connection.getResponseCode();
					if (statusCode == HttpStatus.SC_OK) {
						connection.disconnect();
						break;
					} else {
						nextLocation = connection.getHeaderField("location");
						connection.disconnect();
						if (!redirectLocations.add(nextLocation)) {
							Log.d("URL redirect cycle detected");
							return "";
						}

						url = new URL(nextLocation);
					}
				} while (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
						|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY
						|| statusCode == HttpStatus.SC_TEMPORARY_REDIRECT
						|| statusCode == HttpStatus.SC_SEE_OTHER);
			} catch (IOException e) {
				return (nextLocation != null) ? nextLocation : "";
			} finally {
				if (connection != null)
					connection.disconnect();
			}

			return nextLocation;
		}

		@Override
		protected void onPostExecute(String url) {
			if (url == null || url.equals("")) {
				url = "about:blank";
				return;
			}
			
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	private void notifyAdClicked() {
		if (listener != null) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					listener.adClicked();
				}
			});
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		overlayView.setLayoutParams(new LayoutParams(adView.getWidth(), adView.getHeight()));
		if (!impressionReported) {
			impressionReported = true;
			if(nativeAd != null) {				
				nativeAd.handleImpression();
			}
			notifyImpression();

			for (Tracker t : trackers) {
				if (t.type.equals("impression")) {
					trackImpression(t.url);
				}
			}
		}
		super.dispatchDraw(canvas);
	}

	private void notifyImpression() {
		if (listener != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.impression();
				}
			});
		}
	}

	private void trackImpression(final String url) {
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					AndroidHttpClient client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
					HttpGet request = new HttpGet();
					request.setHeader("User-Agent", System.getProperty("http.agent"));
					request.setURI(new URI(url));
					client.execute(request);
					client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

		};
		task.execute();
	}

}
