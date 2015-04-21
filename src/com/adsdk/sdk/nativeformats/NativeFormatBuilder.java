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


/**
 * Created by itamar on 16/03/15.
 */
public class NativeFormatBuilder {

	private NativeFormatRequest params;
	private String creative;
    private Handler handler;

    public interface NativeFormatBuilderListener {
        public void onBuildSuccess(String template,String data);
        public void onBuildError(Exception e);
    }

	NativeFormatBuilder(NativeFormatRequest params, String creative) {

		this.params = params;
		this.creative = creative;
	}

	// ---------------------------------------------------------

	public void build(final NativeFormatBuilderListener listener) {

        Log.v("html5", "starting build");
        handler = new Handler();

		Thread requestThread = new Thread(new Runnable() {

			@Override
			public void run() {
				AndroidHttpClient client = null;
				try {
					client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
					final String url = params.toString();
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
                                    listener.onBuildError(new RequestException("empty response from: " + url));
                                }
                            });
							return;
						}
						Log.v("html5","builder: "+data);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onBuildSuccess(creative,data);
                            }
                        });

					} else {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onBuildError(new RequestException("request failed: " + url));
                            }
                        });
						return;
					}

				} catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onBuildError(e);
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
				listener.onBuildError(new Exception(ex));
			}
		});
		requestThread.start();

	};
	// ---------------------------------------------------------

}
