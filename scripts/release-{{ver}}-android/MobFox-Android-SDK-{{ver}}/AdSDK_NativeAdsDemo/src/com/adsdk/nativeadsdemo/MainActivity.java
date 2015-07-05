
package com.adsdk.nativeadsdemo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.adsdk.sdk.nativeads.BaseAdapterUtil;
import com.adsdk.sdk.nativeads.NativeAd;
import com.adsdk.sdk.nativeads.NativeAdListener;
import com.adsdk.sdk.nativeads.NativeAdManager;
import com.adsdk.sdk.nativeads.NativeAdView;
import com.adsdk.sdk.nativeads.NativeViewBinder;

public class MainActivity extends Activity implements NativeAdListener {

	private static final int NUMBER_OF_ADS_TO_BE_LOADED = 5;

	private int requestsInProgress;
	private NativeAdManager nativeAdManager;
	private NativeViewBinder bigNativeAdBinder;
	private View bigNativeAdView;
	private NativeViewBinder smallNativeAdBinder;
	private BaseAdapterUtil baseAdapterUtil;
	private Queue<NativeAd> nativeAds;
	private LinearLayout mainLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		nativeAds = new LinkedList<NativeAd>();

		mainLayout = (LinearLayout) findViewById(R.id.mainLayout);

//		ArrayList<String> adTypes = new ArrayList<String>(); // optional, list of ad types that are allowed. You can pass null instead of this.
//		adTypes.add("app");

		nativeAdManager = new NativeAdManager(this, "http://my.mobfox.com/request.php", true, "ENTER_PUBLISHER_ID_HERE", this, null);
		
		bigNativeAdBinder = new NativeViewBinder(R.layout.native_ad_layout);
		bigNativeAdBinder.bindTextAsset("headline", R.id.headlineView);
		bigNativeAdBinder.bindTextAsset("description", R.id.descriptionView);
		bigNativeAdBinder.bindImageAsset("icon", R.id.iconView);
		bigNativeAdBinder.bindImageAsset("main", R.id.mainImageView);
		bigNativeAdBinder.bindTextAsset("rating", R.id.ratingBar); // NOTE: "rating" asset is special, RatingBar should be used instead of TextView.

	}

	public void onShowButtonClick(View v) {
		if (bigNativeAdView != null) {
			mainLayout.removeView(bigNativeAdView);
			bigNativeAdView = null;
		}
		NativeAd nativeAd = nativeAds.poll();
		if (nativeAd == null) {
			Toast.makeText(this, "no native ad loaded!", Toast.LENGTH_SHORT).show();
			return;
		}
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setVisibility(View.GONE);
		Button prepareListViewButton = (Button) findViewById(R.id.prepareListButton);
		prepareListViewButton.setEnabled(true);

		bigNativeAdView = nativeAdManager.getNativeAdView(nativeAd, bigNativeAdBinder);
		if (mainLayout != null) {
			mainLayout.addView(bigNativeAdView);
		}
	}

	public void onLoadButtonClick(View v) {
		if (bigNativeAdView != null) {
			mainLayout.removeView(bigNativeAdView);
			bigNativeAdView = null;
		}
		fillNativeAdsQueue();
	}

	private void fillNativeAdsQueue() {
		int adsToBeRequested = NUMBER_OF_ADS_TO_BE_LOADED - nativeAds.size() - requestsInProgress;

		for (int i = 0; i < adsToBeRequested; i++) {
			requestsInProgress++;
			nativeAdManager.requestAd();
		}

	}

	public void onPrepareListButtonClick(View v) {
		if (bigNativeAdView != null) {
			mainLayout.removeView(bigNativeAdView);
			bigNativeAdView = null;
		}
		View showAdButtonView = findViewById(R.id.showAdButton);
		if (showAdButtonView != null) {
			showAdButtonView.setEnabled(false);
		}
		View loadAdButtonView = findViewById(R.id.loadNativeAdButton);
		if (loadAdButtonView != null) {
			loadAdButtonView.setEnabled(false);
		}

		smallNativeAdBinder = new NativeViewBinder(R.layout.small_native_ad_layout);
		smallNativeAdBinder.bindTextAsset("headline", R.id.headlineView);
		smallNativeAdBinder.bindImageAsset("icon", R.id.iconView);
		smallNativeAdBinder.bindTextAsset("rating", R.id.ratingBar); // NOTE: "rating" asset is special, RatingBar should be used instead of TextView.

		baseAdapterUtil = new BaseAdapterUtil(3, 5);

		ListView listView = (ListView) findViewById(R.id.listView);
		ArrayAdapterWithAds adapter = new ArrayAdapterWithAds(this, android.R.layout.simple_list_item_1);
		for (int i = 0; i < 55; i++) {
			adapter.add("some text nr: " + i);
		}
		listView.setAdapter(adapter);

		listView.setVisibility(View.VISIBLE);

		v.setEnabled(false);
	}

	@Override
	public void adLoaded(NativeAd ad) {
		Toast.makeText(this, "Native ad loaded", Toast.LENGTH_SHORT).show();
		requestsInProgress--;
		nativeAds.add(ad);

		View showAdButtonView = findViewById(R.id.showAdButton);
		if (showAdButtonView != null) {
			showAdButtonView.setEnabled(true);
		}

		View loadAdButtonView = findViewById(R.id.loadNativeAdButton);
		if (loadAdButtonView != null) {
			loadAdButtonView.setEnabled(true);
		}

	}

	@Override
	public void adFailedToLoad() {
		Toast.makeText(this, "Ad failed to load", Toast.LENGTH_SHORT).show();
		requestsInProgress--;
	}

	@Override
	public void impression() {
		Toast.makeText(this, "Tracked ad impression", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void adClicked() {
		Toast.makeText(this, "Tracked ad click", Toast.LENGTH_SHORT).show();
	}

	public class ArrayAdapterWithAds extends ArrayAdapter<String> {
		private SparseArray<NativeAdView> nativeAdViews;

		public ArrayAdapterWithAds(Context context, int resourceId) {
			super(context, resourceId);
			nativeAdViews = new SparseArray<NativeAdView>();
		}

		@Override
		public int getCount() {
			int originalCount = super.getCount();
			return baseAdapterUtil.calculateShiftedCount(originalCount);
		}

		@Override
		public int getViewTypeCount() {
			int originalCount = super.getViewTypeCount();
			return originalCount + 1; // +1 for native ad view type.
		}

		@Override
		public int getItemViewType(int position) {
			if (baseAdapterUtil.isAdPosition(position)) {
				return getViewTypeCount() - 1; // to return native ad view as last type.
			} else {
				return super.getItemViewType(position); // return your original view type. If you need position index, use shifted position obtained by baseAdapterUtil.calculateShiftedPosition(originalPosition)
			}
		}

		@Override
		public String getItem(int position) {
			int shiftedPosition = baseAdapterUtil.calculateShiftedPosition(position);
			return super.getItem(shiftedPosition);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (baseAdapterUtil.isAdPosition(position)) {
				NativeAdView view;
				view = nativeAdViews.get(position); // we don't want to recreate the view every time user scrolls back the list view
				if (view == null) {
					NativeAd nativeAd = nativeAds.poll();
					view = nativeAdManager.getNativeAdView(nativeAd, smallNativeAdBinder);
					if (nativeAd != null) {
						nativeAdViews.put(position, view);
					}
					fillNativeAdsQueue();
				}
				return view;
			} else {
				return super.getView(position, convertView, parent); //return original view
			}
		}
	}

}
