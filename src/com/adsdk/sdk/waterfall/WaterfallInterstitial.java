package com.adsdk.sdk.waterfall;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;
import com.adsdk.sdk.banner.AdView;
import com.adsdk.sdk.nativeformats.NativeFormatInterstitial;
import com.adsdk.sdk.nativeformats.NativeFormatView;

/**
 * Created by nabriski on 4/30/15.
 */
public class WaterfallInterstitial {

    public interface Listener{
        public void onAdLoaded();
        public void onAdNotFound();
    };

    Listener listener = null;
    String publicationId;
    Waterfall w= null;
    Context ctx;

    public WaterfallInterstitial(Context ctx,String publicationId){
        this.publicationId = publicationId;
        this.ctx = ctx;
        WaterfallManager manager = WaterfallManager.getInstance();
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void loadAd(){
        WaterfallManager manager = WaterfallManager.getInstance();
        w = manager.getWaterfall("interstitial");
        loadAdInternal();
    }

    private void loadAdInternal(){

        String type = w.getNext();

        if("banner".equals(type)){
            Log.d("waterfall", "loading banner");
            loadBannerAd();
        }
        else if("nativeFormat".equals(type)){
            Log.d("waterfall", "loading native format");
            loadNativeFormatAd();
        }
        else if("video".equals(type)){
            Log.d("waterfall", "loading video");
            loadVideoAd();
        }
        else{
            if(this.listener==null) return;
            this.listener.onAdNotFound();
        }

    }

    protected void loadBannerAd(){

        final AdManager mgr = new AdManager(this.ctx, "http://my.mobfox.com/request.php",this.publicationId, true);
        mgr.setVideoAdsEnabled(false);
        final WaterfallInterstitial _this = this;
        mgr.setListener(new AdListener(){

            @Override
            public void adClicked() {

            }

            @Override
            public void adClosed(Ad ad, boolean completed) {

            }

            @Override
            public void adLoadSucceeded(Ad ad) {

                Log.d("waterfall","inter banner ad success");
                mgr.showAd();
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                Log.d("waterfall","inter banner ad not found");
                _this.loadAdInternal();
            }
        });
        mgr.requestAd();

    }

    protected void loadVideoAd(){

        final AdManager mgr = new AdManager(this.ctx, "http://my.mobfox.com/request.php",this.publicationId, true);
        mgr.setInterstitialAdsEnabled(false);

        final WaterfallInterstitial _this = this;
        mgr.setListener(new AdListener(){

            @Override
            public void adClicked() {

            }

            @Override
            public void adClosed(Ad ad, boolean completed) {

            }

            @Override
            public void adLoadSucceeded(Ad ad) {
                Log.d("waterfall","inter video ad success");
                mgr.showAd();
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                Log.d("waterfall","inter video ad not found");

                _this.loadAdInternal();
            }
        });
        mgr.requestAd();

    }

    protected void loadNativeFormatAd(){

        Log.d("waterfall", "load native format ad");
        final WaterfallInterstitial _this = this;
        final NativeFormatInterstitial ni = new NativeFormatInterstitial(this.ctx);
        ni.setPublicationId(this.publicationId);

        ni.setListener(new NativeFormatView.NativeFormatAdListener(){

            @Override
            public void onNativeFormatLoaded(String html) {
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void onNativeFormatFailed(Exception e) {
                Log.d("waterfall", "load native format failed");
                _this.loadAdInternal();
            }

            @Override
            public void onNativeFormatDismissed(NativeFormatView banner) {

            }
        });

        ni.loadAd();

    }

    public void setWaterfallInterstitialListener(Listener listener){
        this.listener = listener;
    }


}
