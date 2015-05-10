package com.adsdk.sdk.networking;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.adsdk.sdk.AdRequest;

import com.adsdk.sdk.Gender;

import com.adsdk.sdk.RequestAd;
import com.adsdk.sdk.RequestException;

import com.adsdk.sdk.Util;



import org.apache.http.Header;

import java.io.InputStream;
import java.util.List;

/**
 * Created by nabriski on 5/7/15.
 */
public class VASTRequest {

    private static final String TAG = "vastreq";
    private Gender userGender;
    private int userAge;
    private List<String> keywords;


    Context ctx;
    String publicationId;

    String BASE_URL = "http://my.mobfox.com/request.php";
    int videoMinimalDuration;
    int videoMaxDuration;

    Listener listener;

    public void setUserAge(int userAge) {
        this.userAge = userAge;
    }

    public void setUserGender(Gender userGender) {
        this.userGender = userGender;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public interface Listener{
        public void onVASTLoaded(String xml);

        public void onNoAd();

        public void onFailed(Exception e);
    }


    public VASTRequest(Context ctx,String publicationId){
        this.publicationId = publicationId;
        this.ctx = ctx;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void loadAd(){

        Log.d(TAG, "start loading");
        final AdRequest request = new AdRequest();

        request.setAndroidAdId(Util.getAndroidAdId());
        request.setAdDoNotTrack(Util.hasAdDoNotTrack());
        request.setPublisherId(this.publicationId);
        request.setUserAgent(Util.getDefaultUserAgentString(this.ctx));
        request.setUserAgent2(Util.buildUserAgent());

        request.setGender(userGender);
        request.setUserAge(userAge);
        request.setKeywords(keywords);

        request.setVideoRequest(true);
        request.setVideoMaxDuration(videoMaxDuration);
        request.setVideoMinDuration(videoMinimalDuration);

        Location location  = Util.getLocation(this.ctx);

        if (location != null) {
            request.setLatitude(location.getLatitude());
            request.setLongitude(location.getLongitude());
        } else {
            request.setLatitude(0.0);
            request.setLongitude(0.0);
        }

        request.setAdspaceHeight(480);
        request.setAdspaceWidth(320);
        request.setAdspaceStrict(false);

        request.setConnectionType(Util.getConnectionType(this.ctx));
        request.setIpAddress(Util.getLocalIpAddress());
        request.setTimestamp(System.currentTimeMillis());

        request.setRequestURL(this.BASE_URL);

        Log.d(TAG, "finished setting params");

        Thread requestThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    RequestAd<String> requestAd = new RequestAd<String>(){

                        @Override
                        public String parseTestString() throws RequestException {
                            return "";
                        }

                        @Override
                        public String parse(InputStream inputStream, Header[] headers, boolean isVideo) throws RequestException {
                            try {
                                return new java.util.Scanner(inputStream).useDelimiter("\\A").next();
                            } catch (java.util.NoSuchElementException e) {
                                return "";
                            }
                        }
                    };

                    String response;
                    try {
                        response = requestAd.sendRequest(request);
                    } catch (RequestException e) {
                        Log.e(TAG, "req exception",e);
                        if(listener!=null) listener.onFailed(e);
                        return;
                    }

                    if (response.length() == 0){
                        if(listener!=null) listener.onNoAd();
                        Log.d(TAG, "no ad");
                        return;
                    }

                    if(listener!=null) listener.onVASTLoaded(response);
                    Log.d(TAG,response);

                }
        });

        requestThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread thread, Throwable ex) {

                    if(listener!=null) listener.onFailed(new Exception(ex));
                    Log.e(TAG, "thread exception",ex);
                }
        });
        requestThread.start();
        Log.d(TAG, "request sent");
    }


}
