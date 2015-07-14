package com.adsdk.sdk.nativeformats;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.nativeformats.NativeFormatView.NativeFormatAdListener;

/**
 * Created by itamar on 26/03/15.
 */
public class NativeFormatInterstitialActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		Bundle extras = getIntent().getExtras();

		String template = "";
		String data     = "";

		if (extras != null) {
			template    = extras.getString("TEMPLATE");
			data        = extras.getString("DATA");
		}

		//Log.v("creative id: " + String.valueOf(creativeId));
		NativeFormatView nfv = new NativeFormatView(this);



		nfv.setListener(new NativeFormatAdListener() {

			@Override
			public void onNativeFormatLoaded(String html) {

			}

			@Override
			public void onNativeFormatFailed(Exception e) {
				NativeFormatInterstitialActivity.this.finish();
			}

			@Override
			public void onNativeFormatDismissed(NativeFormatView banner) {
				NativeFormatInterstitialActivity.this.finish();
			}
		});

		/*
		 * nfv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)); LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)nfv.getLayoutParams(); params.gravity = Gravity.CENTER;
		 * 
		 * nfv.setLayoutParams(params);
		 */

		// and set your layout like main content

		setContentView(nfv);

		// this.addContentView(nfv,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        nfv.loadAd(template, data);


    }
}
