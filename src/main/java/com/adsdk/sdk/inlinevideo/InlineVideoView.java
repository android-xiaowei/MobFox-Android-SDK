package com.adsdk.sdk.inlinevideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
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
public class InlineVideoView extends VideoEnabledWebView implements InlineVideoBridge.Listener{

    static final String BASE_URL = "http://my.mobfox.com/request.php";

    public interface Listener{

        public void onAdClicked(String href);

        public void onAdClosed();

        public void onAdFinished();

        public void onAdError(Exception e);

        public void onAdLoaded();

    }

    String publicationId;
    int adWidth = 0;
    int adHeight = 0;
    Listener listener;
    boolean autoplay =true,skip =true;
    Handler handler;

    protected String getSrc(String data, String fileName) {

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

    @SuppressLint("JavascriptInterface")
    protected void init() {

        handler = new Handler();
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

        InlineVideoBridge bridge = new InlineVideoBridge(getContext(),this);
        this.addJavascriptInterface(bridge, "Android");

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

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void loadAdInternal(){

        clearView();

        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                setWebViewClient(null);

                String ipAddress = Utils.getIPAddress(); //TODO: can we remove it? Other requests don't send IP
                if (ipAddress.indexOf("10.") == 0 || ipAddress.length() == 0) {
                    ipAddress = "2.122.29.194";
                }

                String userAgent = Util.getDefaultUserAgentString(getContext());
                String o_andadvid = Util.getAndroidAdId();

                JSONObject params = new JSONObject();

                try {
                    params.put("s", publicationId);
                    params.put("u", userAgent);
                    params.put("i", ipAddress);
                    params.put("o_andadvid", o_andadvid);
                    params.put("autoplay", autoplay);
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

    @Override
    public void onAdClicked(String href) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(href));
        getContext().startActivity(intent);
        if(listener!=null) listener.onAdClicked(href);
    }

    @Override
    public void onAdClosed() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
                if(listener!=null) listener.onAdClosed();
            }
        });
    }

    @Override
    public void onAdFinished() {
        if(listener!=null) listener.onAdFinished();
    }

    @Override
    public void onAdError(Exception e) {
        if(listener!=null) listener.onAdError(e);
    }

    @Override
    public void onAdLoaded() {

        handler.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
                if(listener!=null) listener.onAdLoaded();
            }
        });

    }
}
