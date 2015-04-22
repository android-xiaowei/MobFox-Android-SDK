package com.adsdk.sdk.nativeformats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
//import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//import com.adsdk.sdk.Log;
import com.adsdk.sdk.Util;
import com.adsdk.sdk.nativeformats.creative.Creative;
import com.adsdk.sdk.nativeformats.creative.CreativesManager;
import com.adsdk.sdk.video.ResourceManager;

import org.json.JSONObject;

import java.lang.reflect.Method;

/**
 * Created by itamar on 19/03/15.
 */
public class NativeFormatView extends WebView {

	private static final String BASE_URL = "http://my.mobfox.com/request.php";
	private String publicationId;

	public interface NativeFormatAdListener {
		public void onNativeFormatLoaded(String html);

		public void onNativeFormatFailed(Exception e);

		public void onNativeFormatDismissed(NativeFormatView banner);
		// public void onNativeFormatClicked(MoPubView banner);
		// public void onNativeFormatExpanded(MoPubView banner);
		// public void onNativeFormatCollapsed(MoPubView banner);
	}

	NativeFormatAdListener listener = null;
	// int creativeId = -1;
	CreativesManager creative_manager;

	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	private void init() {

		creative_manager = CreativesManager.getInstance(this.getContext());
		Util.prepareAndroidAdId(this.getContext());
		this.setBackgroundColor(Color.TRANSPARENT);

		/*
		 * this.setBackgroundColor(Color.TRANSPARENT); this.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); this.setBackgroundColor(Color.TRANSPARENT);
		 */

		/*
		 * if (Build.VERSION.SDK_INT >= 11) { this.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); this.setBackgroundColor(0x01000000); } else {
		 * 
		 * this.setBackgroundColor(0x00000000); }
		 * 
		 * this.setWebViewClient(new WebViewClient() {
		 * 
		 * @Override public void onPageFinished(WebView view, String url) { view.setBackgroundColor(0x00000000); if (Build.VERSION.SDK_INT >= 11) { view.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); view.setBackgroundColor(0x01000000); } else {
		 * 
		 * view.setBackgroundColor(0x00000000); } } });
		 */
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);

		if (android.os.Build.VERSION.SDK_INT >= 16) {

			//webSettings.setAllowUniversalAccessFromFileURLs(true);
            try {
                Method m = WebSettings.class.getMethod("setAllowUniversalAccessFromFileURLs",boolean.class);
                m.invoke(webSettings,true);
            } catch (Exception e) {
                Log.v("html5","can't set setAllowUniversalAccessFromFileURLs",e);
            }
        }
/*
		class DismissListener {

			NativeFormatView container;

			public DismissListener(NativeFormatView container) {
				this.container = container;
			}

			@JavascriptInterface
			public void onDismiss() {
				if (this.container.listener == null)
					return;
				this.container.listener.onNativeFormatDismissed(this.container);
			}
		}
		addJavascriptInterface(new DismissListener(this), "dismissListener");
*/
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

	// public void setCreativeId(int id){
	// this.creativeId = id;
	// };

	public void setPublicationId(String id) {
		this.publicationId = id;
	}

	public void setListener(NativeFormatAdListener listener) {
		this.listener = listener;
	}

	public void loadAd() {

		NativeFormatRequest request = new NativeFormatRequest();
		request.setRequestUrl(BASE_URL);
		request.setPublisherId(this.publicationId); // TODO: check if correctly set
		String ipAddress = Utils.getIPAddress(); //TODO: can we remove it? Other requests don't send IP
		if (ipAddress.indexOf("10.") == 0 || ipAddress.length() == 0) {
		    ipAddress = "8.8.8.8";
		}
		request.ip = ipAddress;
		// request.add("o_androidid", Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID)); //TODO: we cannot use this ID anymore (only Google Advertising ID)

		// params.add("o_andadvid", "c86f7529-33e2-4346-be0d-777ac53be320");//AdvertisingIdClient.getAdvertisingIdInfo(this.getContext()).getId());
		request.setAndroidAdId(Util.getAndroidAdId());
		request.setAdDoNotTrack(Util.hasAdDoNotTrack());
		request.setUserAgent(Util.getDefaultUserAgentString(getContext()));
		request.setUserAgent2(Util.buildUserAgent());

		Log.v("html5",request.toString());
		int width = 0;
		int height = 0;

		ViewGroup.LayoutParams lp = this.getLayoutParams();

		if (lp != null) {
			float density = getResources().getDisplayMetrics().density; //TODO: why not 320x50?
			width = Math.min(320, (int) (lp.width / density));
			height = Math.min(480, (int) (lp.height / density));
		}

		if (width <= 0)
			width = 320;
		if (height <= 0)
			height = 480;
		
		Log.v("html5","dims: " + width + ", " + height);
		Log.v("html5","getting creative ...");

		Creative c = creative_manager.getCreative(width, height);

		Log.v("html5","got creative ... " + c.getTemplate().length());

		NativeFormatBuilder builder = new NativeFormatBuilder( request, c.getTemplate());

        Log.v("html5","instantiated builder");

		/*
		 * if(this.creativeId > -1){ builder = new NativeFormatBuilder(this.getContext(), params,this.creativeId); } else { builder = new NativeFormatBuilder(this.getContext(), params, R.raw.cube); }
		 */

		final NativeFormatView thisView = this;

        builder.build(new NativeFormatBuilder.NativeFormatBuilderListener() {

                    @Override
                    public void onBuildSuccess(final String template, final String data) {

                        Log.v("html5","data: "+data);

                        thisView.setWebViewClient(new WebViewClient(){

                            @Override
                            public void onPageFinished (WebView view, String url) {

                                thisView.setWebViewClient(null);
                                Log.v("html5","url: "+url);

                                try {
                                    JSONObject inp = new JSONObject();
                                    inp.put("template", template);

                                    JSONObject json =  new JSONObject(data);
                                    String libs = ResourceManager.getStringResource(thisView.getContext(),"libs.js");
                                    json.put("libs","<script type='text/javascript'>"+libs+"</script>");
                                    inp.put("data", json);

                                    //Log.d("html5", "render template : " + inp.toString());

                                    thisView.setWebChromeClient(new WebChromeClient(){
                                        @Override
                                        public boolean onConsoleMessage (ConsoleMessage consoleMessage){

                                            final String response = consoleMessage.message();

                                            thisView.setWebChromeClient(null);
                                            Log.v("html5", "render template response : " + response);


                                            thisView.loadDataWithBaseURL("file:///android_res/raw/", response, "text/html", "utf-8", null);


                                            if (thisView.listener != null) {

                                                thisView.listener.onNativeFormatLoaded(response);

                                            }

                                            return true;
                                        }
                                    });


                                    thisView.loadUrl("javascript:renderTemplate(" + inp.toString() + ")");

                                } catch (final Exception e) {


                                    if (thisView.listener != null) {
                                        thisView.listener.onNativeFormatFailed(e);
                                    }

                                }
                            }


                        });


                        String renderTemplete = ResourceManager.getStringResource(thisView.getContext(),"render_template.html");
                        Log.v("html5","render template contents:"+renderTemplete);
                        thisView.loadDataWithBaseURL(null,renderTemplete,"text/html","utf-8",null);

                    }


                    @Override
                    public void onBuildError(final Exception e) {

                                if (thisView.listener != null) {
                                    thisView.listener.onNativeFormatFailed(e);
                                }

                    }

        });


	}

}
