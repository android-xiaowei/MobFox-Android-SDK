package com.adsdk.sdk.networking;

import android.net.http.AndroidHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by nabriski on 4/29/15.
 */
public class JSONRetrieverImpl implements JSONRetriever{

    @Override
    public void retrieve(final String url, final Listener listener) {
        Thread requestThread = new Thread(new Runnable() {

            @Override
            public void run() {
                AndroidHttpClient client = null;
                try {
                    client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
                    HttpGet request = new HttpGet();
                    request.setHeader("User-Agent", System.getProperty("http.agent"));
                    request.setURI(new URI(url));
                    HttpResponse response = client.execute(request);
                    StatusLine statusLine = response.getStatusLine();

                    int statusCode = statusLine.getStatusCode();
                    if(statusCode!=200){
                        listener.onFinish(new Exception("failed, got status "+statusCode),null);
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    String responseString = builder.toString();
                    listener.onFinish(null,new JSONObject(responseString));


                } catch (Exception e) {
                    listener.onFinish(e,null);
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }

            }
        });
        requestThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                listener.onFinish(new Exception(ex),null);
            }
        });
        requestThread.start();
    }

    @Override
    public void post(final String url, final JSONObject data, final Listener listener) {
        Thread requestThread = new Thread(new Runnable() {

            @Override
            public void run() {
                AndroidHttpClient client = null;
                try {
                    client = AndroidHttpClient.newInstance(System.getProperty("http.agent"));
                    HttpPost request = new HttpPost();
                    request.setHeader("User-Agent", System.getProperty("http.agent"));
                    request.setURI(new URI(url));
                    request.setEntity(new StringEntity(data.toString(4)));
                    HttpResponse response = client.execute(request);
                    StatusLine statusLine = response.getStatusLine();

                    int statusCode = statusLine.getStatusCode();
                    if(statusCode!=200){
                        listener.onFinish(new Exception("failed, got status "+statusCode),null);
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    String responseString = builder.toString();
                    listener.onFinish(null,new JSONObject(responseString));


                } catch (Exception e) {
                    listener.onFinish(e,null);
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }

            }
        });
        requestThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                listener.onFinish(new Exception(ex),null);
            }
        });
        requestThread.start();
    }
}
