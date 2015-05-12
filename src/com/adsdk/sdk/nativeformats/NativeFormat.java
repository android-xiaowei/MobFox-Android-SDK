package com.adsdk.sdk.nativeformats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.util.Log;

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

    public interface Listener {
        public void onSuccess(String template,String data);
        public void onError(Exception e);
    }

	NativeFormat(Context ctx,int width,int height,String publicationId) {
        this.ctx            = ctx;
		this.width          = width;
        this.height         = height;
        this.publicationId  = publicationId;
        this.creative_manager = CreativesManager.getInstance(this.ctx);
	}

	// ---------------------------------------------------------

	public void loadAd(final Listener listener) {

        final Creative creative = creative_manager.getCreative(width, height);
        final NativeFormatRequest request = new NativeFormatRequest();

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
        request.setUserAgent(Util.getDefaultUserAgentString(ctx));
        request.setUserAgent2(Util.buildUserAgent());
        request.setTemplateName(creative.getName());



        Log.v("html5", "starting build");
        Log.d("html5","native req: "+request.toUri());

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
                    Log.v("html5", "sent request");

					StatusLine statusLine = response.getStatusLine();

					int statusCode = statusLine.getStatusCode();

                   /* final String data = "{\"imageassets\":{\"icon\":{\"url\":\"http:\\/\\/creative2cdn.mobfox.com\\/0419a76afdc67a97cb8e9ea44418bfe4.gif\",\"width\":\"512\",\"height\":\"512\"},\"main\":{\"url\":\"http:\\/\\/creative2cdn.mobfox.com\\/48b1eed8e5e7242f6248852a40c7a033.gif\",\"width\":\"1200\",\"height\":\"627\"}},\"textassets\":{\"headline\":\"Insta Likes & Followers!\",\"description\":\"The best app to get REAL Instagram Likes and Followers!\",\"cta\":\"Install Now\",\"rating\":\"5\",\"advertiser\":\"Polar Labs UG\"},\"trackers\":[{\"type\":\"impression\",\"url\":\"http:\\/\\/my.mobfox.com\\/rtb.impression.pixel.php?rid=7d94376702321ad625f0c0832903f8e2&price=0.10\"},{\"type\":\"impression\",\"url\":\"http:\\/\\/my.mobfox.com\\/exchange.pixel.php?h=36da1d9e3bace8f01a975f66211e8528\"}],\"click_url\":\"http:\\/\\/my.mobfox.com\\/exchange.click.php?h=36da1d9e3bace8f01a975f66211e8528\"}";
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBuildSuccess(creative,data);
                        }
                    });*/


					if (statusCode == 200) {

                        Log.v("html5", "start build response");
						StringBuilder builder = new StringBuilder();
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							builder.append(line + "\n");
						}
						final String data = builder.toString();

                        Log.v("html5", "build got data");

						if (data.length() == 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onError(new RequestException("empty response from: " + url));
                                }
                            });
							return;
						}
						Log.v("html5","builder: "+data);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(creative.getTemplate(),data);
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
