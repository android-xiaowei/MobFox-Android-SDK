package com.adsdk.sdk.nativeformats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;

import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
//import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;

import com.adsdk.sdk.video.ResourceManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * Created by itamar on 19/03/15.
 */
public class NativeFormatView extends WebView {

	private String publicationId;
    int adWidth = 0;
    int adHeight = 0;

	public interface NativeFormatAdListener {
		public void onNativeFormatLoaded(String html);

		public void onNativeFormatFailed(Exception e);

		public void onNativeFormatDismissed(NativeFormatView banner);
	}

	NativeFormatAdListener listener = null;
	// int creativeId = -1;


	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	private void init() {

		Util.prepareAndroidAdId(this.getContext());
		this.setBackgroundColor(Color.TRANSPARENT);

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.setWebContentsDebuggingEnabled(true);
//        }

		if (android.os.Build.VERSION.SDK_INT >= 16) {

			//webSettings.setAllowUniversalAccessFromFileURLs(true);
            try {
                Method m = WebSettings.class.getMethod("setAllowUniversalAccessFromFileURLs",boolean.class);
                m.invoke(webSettings,true);
            } catch (Exception e) {
                Log.v("can't set setAllowUniversalAccessFromFileURLs",e);
            }
        }
	}

	public NativeFormatView(Context context) {
		super(context);
		init();
	}

	public NativeFormatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NativeFormatView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

//    public void WriteTemp(String data) {
//
//        FileOutputStream fop = null;
//
//        try {
//
//            File temp = File.createTempFile("creative", ".html");
//            fop = new FileOutputStream(temp);
//
//            fop.write(data.getBytes(Charset.forName("UTF-8")));
//
//            android.util.Log.d("FilePath", temp.getAbsolutePath());
//            android.util.Log.d("FileData", data);
//
//        } catch(IOException e) {
//
//            e.printStackTrace();
//
//        }
//    }

    public void setAdWidth(int width){
        this.adWidth = width;
    }

    public void setAdHeight(int height){
        this.adHeight = height;
    }

	public void setPublicationId(String id) {
		this.publicationId = id;
	}

	public void setListener(NativeFormatAdListener listener) {
		this.listener = listener;
	}

    public void loadAd(final String template,final String data){

        Log.v("data: "+data);

        setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished (WebView view, String url) {

                setWebViewClient(null);
                Log.v("url: "+url);

                try {
                    JSONObject inp = new JSONObject();
                    inp.put("template", template);

                    JSONObject json =  new JSONObject(data);
                    String libs = ResourceManager.getStringResource(getContext(),"libs.js");
                    json.put("libs","<script type='text/javascript'>"+libs+"</script>");
                    inp.put("data", json);

                    setWebChromeClient(new WebChromeClient(){
                        @Override
                        public boolean onConsoleMessage (ConsoleMessage consoleMessage){

                            final String response = consoleMessage.message();

                            setWebChromeClient(null);
                            Log.v("render template response : " + response);
//                            WriteTemp( response );


                            loadDataWithBaseURL("file:///android_res/raw/", response, "text/html", "utf-8", null);


                            if (listener != null) {

                                listener.onNativeFormatLoaded(response);

                            }

                            return true;
                        }
                    });


                    loadUrl("javascript:renderTemplate(" + inp.toString() + ")");

                } catch (final Exception e) {


                    if (listener != null) {
                        listener.onNativeFormatFailed(e);
                    }

                }
            }


        });


        String renderTemplete = ResourceManager.getStringResource(getContext(),"render_template.html");
        Log.v("render template contents:"+renderTemplete);
        loadDataWithBaseURL(null,renderTemplete,"text/html","utf-8",null);
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


		NativeFormat nf = new NativeFormat(this.getContext(), width, height, this.publicationId);

        Log.v("instantiated builder");

		/*
		 * if(this.creativeId > -1){ builder = new NativeFormatBuilder(this.getContext(), params,this.creativeId); } else { builder = new NativeFormatBuilder(this.getContext(), params, R.raw.cube); }
		 */

		final NativeFormatView thisView = this;

        nf.loadAd(this.getSettings().getUserAgentString(),new NativeFormat.Listener() {

                    @Override
                    public void onSuccess(final String template, final String data) {
                        thisView.loadAd(template, data);
                    }

                    @Override
                    public void onError(final Exception e) {

                                if (thisView.listener != null) {
                                    thisView.listener.onNativeFormatFailed(e);
                                }

                    }

        });


	}

}
