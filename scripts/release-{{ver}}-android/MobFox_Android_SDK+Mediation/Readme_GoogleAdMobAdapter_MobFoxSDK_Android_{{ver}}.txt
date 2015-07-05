Notes for using the "Google AdMob Mediation Adapter” with MobFox SDK 6.0.8.


To display MobFox ads using AdMob mediation, in addition to MobFox SDK (AdSdk_6.0.8.jar), the special Google AdMob Mediation Adapter is necessary (available as separate mobfoxmediationadapter.jar). Please add also simple-xml library (provided in this package) to your project.
Please refer to https://developers.google.com/mobile-ads-sdk/download for more details and download the latest Google AdMob SDK.


Requirement:

- Admob Mediation Account
- Eclipse 4.2.2 with Android SDK Rev. 21.1, SDK Platform Android 4.2.2 Rev. 2 (API 17)
- Target of Android v3.2 or later (set target in default.properties to at least android-13).
- Run-time of Android SDK v2.2 or later (set android:minSdkVersion to at least 8 in your AndroidManifest.xml).

Please note that these requirements are in line with https://developers.google.com/mobile-ads-sdk/docs/.

Please copy/paste this MobFox specific lines for Banner and Video/Interstitials to your Manifest:

    <uses-sdk
        android:minSdkVersion="8"
        android:targetSdkVersion="16" />

Please add the following to your activity containing the banner:
	android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize"

	<activity
            android:name="com.adsdk.sdk.banner.InAppWebView"
            android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize" />
        <activity
            android:name="com.adsdk.sdk.video.RichMediaActivity"
            android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize"
            android:hardwareAccelerated="false" />
        <activity
            android:name="com.adsdk.sdk.mraid.MraidBrowser"
            android:configChanges="keyboard|keyboardHidden|orientation|screenLayout|uiMode|screenSize|smallestScreenSize" />


Optional please add this lines for better customer experience:

Read Phone State Permission

	<uses-permission android:name="android.permission.READ_PHONE_STATE" />

Location Permissions

	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

