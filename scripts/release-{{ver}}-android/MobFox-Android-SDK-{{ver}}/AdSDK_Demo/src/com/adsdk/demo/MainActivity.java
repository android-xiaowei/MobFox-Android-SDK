package com.adsdk.demo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.Gender;
import com.adsdk.sdk.banner.AdView;

public class MainActivity extends Activity implements AdListener {
	private RelativeLayout layout;
	private AdView mAdView;
	private AdManager mManager;

	public void onClickShowBanner(View view) {
		if (mAdView != null) {
			removeBanner();
		}
		mAdView = new AdView(this, "http://my.mobfox.com/request.php",
				"ENTER_PUBLISHER_ID_HERE", true, true);
		
		mAdView.setAdspaceWidth(320); //optional, used to set the custom size of banner placement. Without setting it, the SDK will use default sizes.
		mAdView.setAdspaceHeight(50);  
		mAdView.setAdspaceStrict(false); //optional, tells the server to only supply banners that are exactly of desired size. Without setting it, the server could also supply smaller ads when no ad of desired size is available.

		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("sports");
		keywords.add("football");
		mAdView.setKeywords(keywords); //optional, to send list of keywords (user interests) to ad server.
		mAdView.setUserAge(18); //optional, sends user's age
		mAdView.setUserGender(Gender.MALE); //optional, sends user's gender

		
		mAdView.setAdListener(this);
		layout.addView(mAdView);
	}

	private void removeBanner(){
		if(mAdView!=null){
			layout.removeView(mAdView);
			mAdView = null;
		}
	}

	public void onClickShowVideoInterstitial(View v) {
		mManager.requestAd();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		layout = (RelativeLayout) findViewById(R.id.adsdkContent);
		mManager = new AdManager(this,"http://my.mobfox.com/request.php",
				"ENTER_PUBLISHER_ID_HERE", true);
		
		mManager.setInterstitialAdsEnabled(true); //enabled by default. Allows the SDK to request static interstitial ads.
		mManager.setVideoAdsEnabled(true); //disabled by default. Allows the SDK to request video fullscreen ads.
		mManager.setPrioritizeVideoAds(true); //disabled by default. If enabled, indicates that SDK should request video ads first, and only if there is no video request a static interstitial (if they are enabled).
		
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.add("cars");
		keywords.add("money");
		mManager.setKeywords(keywords); //optional, to send list of keywords (user interests) to ad server.
		mManager.setUserAge(37); //optional, sends user's age
		mManager.setUserGender(Gender.FEMALE); //optional, sends user's gender
		
		mManager.setListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void adClicked() {
		Toast.makeText(MainActivity.this, "Ad clicked!", Toast.LENGTH_LONG)
		.show();
	}

	public void adClosed(Ad arg0, boolean arg1) {
		Toast.makeText(MainActivity.this, "Ad closed!", Toast.LENGTH_LONG)
		.show();
	}

	public void adLoadSucceeded(Ad arg0) {
		Toast.makeText(MainActivity.this, "Ad load succeeded!", Toast.LENGTH_LONG)
		.show();
		if (mManager != null && mManager.isAdLoaded())
			mManager.showAd();
	}

	public void adShown(Ad arg0, boolean arg1) {
		Toast.makeText(MainActivity.this, "Ad shown!", Toast.LENGTH_LONG)
		.show();
	}

	public void noAdFound() {
		Toast.makeText(MainActivity.this, "No ad found!", Toast.LENGTH_LONG)
		.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManager.release();
		if(mAdView!=null)
			mAdView.release();
	}
}
