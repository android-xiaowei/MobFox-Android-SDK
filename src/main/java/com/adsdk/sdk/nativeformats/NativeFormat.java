package com.adsdk.sdk.nativeformats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Handler;
import android.view.ViewGroup;


import com.adsdk.sdk.Log;
import com.adsdk.sdk.RequestException;
import com.adsdk.sdk.Util;
import com.adsdk.sdk.nativeformats.creative.Creative;
import com.adsdk.sdk.nativeformats.creative.CreativesManager;


/**
 * Created by itamar on 16/03/15.
 */
public class NativeFormat {


    private static final String BASE_URL = "http://my.mobfox.com/request.php";


    private Handler handler;
    String publicationId;
    CreativesManager creative_manager;
    int width;
    int height;
    Context ctx;

    final static String TYPE_BLOCK = "block";
    final static String TYPE_STRIPE = "stripe";

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

    public interface Listener {
        public void onSuccess(String template, String data);
        public void onError(Exception e);
    }

	NativeFormat(Context ctx, int width, int height, String publicationId) {
        this.ctx                = ctx;
        this.width              = width;
        this.height             = height;
        this.publicationId      = publicationId;
        this.creative_manager   = CreativesManager.getInstance(this.ctx,publicationId);
	}

	// ---------------------------------------------------------

	public void loadAd(String webviewUserAgent, final Listener listener) {

        float ratio = height / width;

        String type = NativeFormat.TYPE_BLOCK;

        if ( ratio < 0.5 ) {
            type = NativeFormat.TYPE_STRIPE;
        }

        if(Build.FINGERPRINT.startsWith("generic")){
            webviewUserAgent = "";
        }
        final Creative creative = creative_manager.getCreative(type,webviewUserAgent);

        final NativeFormatRequest request = new NativeFormatRequest();

        request.setRequestUrl(BASE_URL);
        request.setPublisherId(this.publicationId); // TODO: check if correctly set
        String ipAddress = Utils.getIPAddress(); //TODO: can we remove it? Other requests don't send IP
        if (ipAddress.indexOf("10.") == 0 || ipAddress.length() == 0) {
            ipAddress = "2.122.29.194";
        }
        request.ip = ipAddress;
        // request.add("o_androidid", Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID)); //TODO: we cannot use this ID anymore (only Google Advertising ID)

        // params.add("o_andadvid", "c86f7529-33e2-4346-be0d-777ac53be320");//AdvertisingIdClient.getAdvertisingIdInfo(this.getContext()).getId());
        request.setAndroidAdId(Util.getAndroidAdId());
        request.setAdDoNotTrack(Util.hasAdDoNotTrack());
        request.setUserAgent(Util.getDefaultUserAgentString(ctx));
        request.setUserAgent2(Util.buildUserAgent());
        request.setTemplateName(creative.getName());



        Log.d("starting build");
        Log.d("native req: "+request.toUri());

        handler = new Handler();

		Thread requestThread = new Thread(new Runnable() {

			@Override
			public void run() {
				AndroidHttpClient client = null;
				try {
					client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
					final String url = request.toString();

					HttpGet request = new HttpGet(url);
					request.setHeader("User-Agent", System.getProperty("http.agent"));

					HttpResponse response = client.execute(request);
                    Log.v("sent request");

					StatusLine statusLine = response.getStatusLine();

					int statusCode = statusLine.getStatusCode();

					if (statusCode == 200) {

                        Log.v("start build response");
						StringBuilder builder = new StringBuilder();
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line + "\n");
						}
						final String data = builder.toString();

                        android.util.Log.d("builder.toString()", builder.toString());

                        Log.v("build got data");

						if (data.length() == 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(new RequestException("empty response from: " + url));
                                }
                            });
							return;
						}
						Log.v("builder: "+data);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(creative.getTemplate(), data);
                            }
                        });

					} else {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onError(new RequestException("request failed: " + url));
                            }
                        });
						return;
					}

				} catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e);
                        }
                    });


				} finally {
					if (client != null) {
						client.close();
					}
				}

			}
		});
		requestThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				listener.onError(new Exception(ex));
			}
		});
		requestThread.start();

	};
	// ---------------------------------------------------------

}
