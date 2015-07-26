package com.adsdk.sdk.inlinevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Debug;
import android.util.AttributeSet;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
//import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.R;
import com.adsdk.sdk.Util;

import com.adsdk.sdk.inlinevideo.VideoEnabledWebView;
import com.adsdk.sdk.nativeformats.NativeFormatRequest;
import com.adsdk.sdk.video.ResourceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import android.view.Window;

/**
 * Created by itamar on 19/03/15.
 */
public class InlineVideoView extends VideoEnabledWebView {

    private String publicationId;
    int adWidth = 0;
    int adHeight = 0;

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
                    adClick(value);
                }

                if (key.equals("close")) {
                    adClose();
                }

                if (key.equals("finished")) {
                    adFinish();
                }

                if (key.equals("fullscreen")) {
                    toggleFullscreen(value);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private void visible() {

        setVisibility(this.getRootView().VISIBLE);

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

    private void adClick(String clickURL) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickURL));
        this.getContext().startActivity(intent);

    }

    private void adClose() {

        setVisibility(this.getRootView().INVISIBLE);

    }

    private void adFinish() {

        //video finsihed
        android.util.Log.d("adFinished", "adFinished");

    }

    private void toggleFullscreen(String event) {

        //toggle fullscreen
        android.util.Log.d("fullscreen", event);

    }

    public void loadAdInternal(){

        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                setWebViewClient(null);

                JSONObject params = new JSONObject();

                String BASE_URL = "http://my.mobfox.com/request.php";

                String userAgent = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
                String ipAddress = "2.122.29.194";
                String o_andadvid = "68753A44-4D6F-1226-9C60-0050E4C00067";
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

                //init Android bridge
                loadUrl("javascript:initAndroidBridge()");

                //load Ad
                android.util.Log.d("javascriptLoadAd", "javascript:loadAd(\"" + BASE_URL + "\", " + params.toString() + ")");
                loadUrl("javascript:loadAd(\"" + BASE_URL + "\", " + params.toString() + ")");
            }


        });

        String inlineVideo = ResourceManager.getStringResource(getContext(), "inline-video.html");

        String inlineVideoString = ResourceManager.getStringResource(getContext(), "inline-video.js");
        String inlineName = "inline-video.js";

        String srcPath = getSrc(inlineVideoString, inlineName);

        loadDataWithBaseURL(srcPath, inlineVideo, "text/html", "utf-8", null);

    }

    public void loadAd() {

        visible();

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
