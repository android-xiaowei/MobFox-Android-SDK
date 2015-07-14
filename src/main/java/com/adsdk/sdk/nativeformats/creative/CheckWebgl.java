package com.adsdk.sdk.nativeformats.creative;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.video.ResourceManager;

import java.lang.reflect.Method;

/**
 * Created by asafg84 on 09/07/15.
 */
public class CheckWebgl extends WebView {

    public interface Listener {

        public void onWebgl(boolean isWebgl);

    }

    public CheckWebgl(Context context, ViewGroup parent) {

        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (android.os.Build.VERSION.SDK_INT >= 16) {

            //webSettings.setAllowUniversalAccessFromFileURLs(true);
            try {
                Method m = WebSettings.class.getMethod("setAllowUniversalAccessFromFileURLs",boolean.class);
                m.invoke(webSettings,true);
            } catch (Exception e) {
                Log.v("can't set setAllowUniversalAccessFromFileURLs", e);
            }
        }

        parent.addView(this);

    }

    public void isWebgl(final Listener listener) {

        setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                setWebViewClient(null);

                setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                        final String response = consoleMessage.message();
                        android.util.Log.d("checkWebglRes", response);
                        listener.onWebgl(response.equals("true"));

                        return true;

                    }
                });

                loadUrl("javascript:webglAvailable()");

            }

        });

        String checkWebgl = ResourceManager.getStringResource(getContext(), "checkWebgl.html");
        loadDataWithBaseURL(null, checkWebgl, "text/html", "utf-8", null);

    }

}
