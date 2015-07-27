package com.adsdk.sdk.inlinevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;

import com.adsdk.sdk.nativeformats.NativeFormatRequest;
import com.adsdk.sdk.video.ResourceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import com.adsdk.sdk.nativeformats.Utils;

/**
 * Created by itamar on 19/03/15.
 */
public class InlineVideoView extends VideoEnabledWebView {

    private static final String BASE_URL = "http://my.mobfox.com/request.php";

    private String publicationId;
    int adWidth = 0;
    int adHeight = 0;

    final Context ctx = this.getContext();

    NativeFormatRequest nfr = new NativeFormatRequest();

    public class BridgeInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        BridgeInterface(Context c) {
            mContext = c;
        }

        @SuppressLint("JavascriptInterface")
        public void send(String json) {

            try {
                JSONObject action = new JSONObject(json);
                JSONArray keys = action.names();
                String key = keys.get(0).toString();
                String value = action.get(key).toString();

                if (key.equals("clickURL")) {
                    adClick(value, key);
                }

                if (key.equals("close")) {
                    adClose(value, key);
                }

                if (key.equals("finished")) {
                    adFinish(value, key);
                }

//                if (key.equals("fullscreen")) {
//                    toggleFullscreen(value);
//                }

                if (key.equals("error")) {
                    adError(value, key);
                }

                if (key.equals("adLoaded")) {
                    adLoaded(value, key);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public String getSrc(String data, String fileName) {

        String tDir = System.getProperty("java.io.tmpdir");

        try {
            PrintWriter srcFile = new PrintWriter(tDir + "/" + fileName);
            srcFile.println(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return e.toString();
        }

        String path = "file://" + tDir + "/" + fileName;

        return path;

    }

    @SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
    private void init() {

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

        this.addJavascriptInterface(new BridgeInterface(this.getContext()), "Android");

        if (android.os.Build.VERSION.SDK_INT >= 16) {

            try {
                Method m = WebSettings.class.getMethod("setAllowUniversalAccessFromFileURLs",boolean.class);
                m.invoke(webSettings,true);
            } catch (Exception e) {
                Log.v("can't set setAllowUniversalAccessFromFileURLs",e);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= 17) {

            try {
                Method m = WebSettings.class.getMethod("setMediaPlaybackRequiresUserGesture",boolean.class);
                m.invoke(webSettings,false);
            } catch (Exception e) {
                Log.v("can't set setMediaPlaybackRequiresUserGesture",e);
            }
        }
    }

    public InlineVideoView(Context context) {
        super(context);
        init();
    }

    public InlineVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InlineVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setPublicationId(String id) {
        this.publicationId = id;
    }

    private void adClick(String value, String key) {

        //video clicked
        android.util.Log.d("userAction", "key: " + key + ", value: " + value);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(value));
        getContext().startActivity(intent);

    }

    private void adClose(String value, String key) {

        //video closed
        android.util.Log.d("userAction", "key: " + key + ", value: " + value);

        setVisibility(View.GONE);

    }

    private void adFinish(String value, String key) {

        //video finsihed
        android.util.Log.d("userAction", "key: " + key + ", value: " + value);

    }

//    private void toggleFullscreen(String value, String key) {
//
//        //toggle fullscreen
//        android.util.Log.d("userAction", "key: " + key + ", value: " + value);
//
//    }

    private void adError(String value, String key) {

        //loadAd error
        android.util.Log.d("userAction", "key: " + key + ", value: " + value);

    }

    private void adLoaded(String value, String key) {

        //ad Loaded
        android.util.Log.d("userAction", "key: " + key + ", value: " + value);

        setVisibility(View.VISIBLE);

    }

    public void loadAdInternal(){

        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                setWebViewClient(null);

                JSONObject params = new JSONObject();

                String ipAddress = Utils.getIPAddress(); //TODO: can we remove it? Other requests don't send IP
                if (ipAddress.indexOf("10.") == 0 || ipAddress.length() == 0) {
                    ipAddress = "2.122.29.194";
                }

                String userAgent = Util.getDefaultUserAgentString(ctx);
                String o_andadvid = Util.getAndroidAdId();
                Boolean autoplay = true;
                Boolean skip = true;

                try {
                    params.put("s", publicationId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    params.put("u", userAgent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    params.put("i", ipAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    params.put("o_andadvid", o_andadvid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    params.put("autoplay", autoplay);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    params.put("skip", skip);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //init Android bridge and load Ad
                loadUrl("javascript:initAndroidBridge(" + params.toString() + ")");

            }


        });

        String inlineVideo = ResourceManager.getStringResource(getContext(), "inline-video.html");

        String inlineVideoString = ResourceManager.getStringResource(getContext(), "inline-video.js");
        String inlineName = "inline-video.js";

        String srcPath = getSrc(inlineVideoString, inlineName);

        loadDataWithBaseURL(srcPath, inlineVideo, "text/html", "utf-8", null);

    }

    public void loadAd() {

        int width = this.adWidth;
        int height = this.adHeight;

        ViewGroup.LayoutParams lp = this.getLayoutParams();

        int orient = this.getContext().getResources().getConfiguration().orientation;
        int defaultWidth = 320, defaultHeight = 480;

        if(orient == this.getContext().getResources().getConfiguration().ORIENTATION_LANDSCAPE){
            int temp = defaultWidth;
            defaultWidth = defaultHeight;
            defaultHeight = temp;
        }

        if (lp != null) {
            float density = getResources().getDisplayMetrics().density; //TODO: why not 320x50?
            int lpWidth = Math.min(defaultWidth, (int) (lp.width / density));
            int lpHeight = Math.min(defaultHeight, (int) (lp.height / density));
            if(lpWidth > 0) width = lpWidth;
            if(lpHeight > 0) height = lpHeight;
        }

        if (width <= 0)
            width = defaultWidth;
        if (height <= 0)
            height = defaultHeight;

        Log.v("dims: " + width + ", " + height);
        Log.v("getting creative ...");

        final InlineVideoView thisView = this;

        thisView.loadAdInternal();

    }

}
