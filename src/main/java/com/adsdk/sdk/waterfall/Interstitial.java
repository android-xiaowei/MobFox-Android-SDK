package com.adsdk.sdk.waterfall;

import android.content.Context;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.AdManager;

import com.adsdk.sdk.Log;
import com.adsdk.sdk.dmp.DMP;
import com.adsdk.sdk.nativeformats.NativeFormatInterstitial;
import com.adsdk.sdk.nativeformats.NativeFormatView;

/**
 * Created by nabriski on 4/30/15.
 */
public class Interstitial {

    public interface Listener{
        public void onAdLoaded();
        public void onAdNotFound();
    };

    Listener listener = null;
    String publicationId;
    Waterfall w= null;
    Context ctx;

    AdManager bannerMgr;
    AdManager videoMgr;

    public Interstitial(Context ctx, String publicationId){

        final Interstitial _this = this;

        this.publicationId = publicationId;
        this.ctx = ctx;

        DMP dmp = DMP.getInstance(this.ctx);
        dmp.update(ctx);

        WaterfallManager manager = WaterfallManager.getInstance(publicationId);

        bannerMgr = new AdManager(this.ctx, "http://my.mobfox.com/request.php",this.publicationId, true);
        bannerMgr.setVideoAdsEnabled(false);

        bannerMgr.setListener(new AdListener(){

            @Override
            public void adClicked() {

            }

            @Override
            public void adClosed(Ad ad, boolean completed) {

            }

            @Override
            public void adLoadSucceeded(Ad ad) {

                Log.d("waterfall inter banner ad success");
                android.util.Log.d("water","banner loaded !!!");
                if(!_this.bannerMgr.isAdLoaded()){
                    _this.loadAdInternal();
                    return;
                }

                _this.bannerMgr.showAd();
                if(_this.listener == null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                Log.d("waterfall inter banner ad not found");
                android.util.Log.d("water","banner ad not found.");
                _this.loadAdInternal();
            }
        });

        videoMgr = new AdManager(this.ctx, "http://my.mobfox.com/request.php",this.publicationId, true);
        videoMgr.setInterstitialAdsEnabled(false);
        videoMgr.setVideoAdsEnabled(true);

        videoMgr.setListener(new AdListener(){

            @Override
            public void adClicked() {

            }

            @Override
            public void adClosed(Ad ad, boolean completed) {

            }

            @Override
            public void adLoadSucceeded(Ad ad) {
                Log.d("waterfall inter video ad success");
                android.util.Log.d("water","video loaded !!!");
                if(!_this.videoMgr.isAdLoaded()){
                    _this.loadAdInternal();
                    return;
                }

                _this.videoMgr.showAd();
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                Log.d("waterfall inter video ad not found");
                android.util.Log.d("water","video ad not found.");
                _this.loadAdInternal();
            }
        });
    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void loadAd(){
        WaterfallManager manager = WaterfallManager.getInstance(publicationId);
        w = manager.getWaterfall("interstitial");
        loadAdInternal();
    }

    private void loadAdInternal(){

        String type = w.getNext();
        android.util.Log.d("water","type from wfall "+type);
        if("banner".equals(type)){
            Log.d("waterfall loading banner");
            android.util.Log.d("water","waterfall loading banner");
            loadBannerAd();
        }
        else if("nativeFormat".equals(type)){
            Log.d("waterfall loading native format");
            android.util.Log.d("water","waterfall native format");
            loadNativeFormatAd();
        }
        else if("video".equals(type)){
            Log.d("waterfall loading video");
            android.util.Log.d("water","waterfall video format");
            loadVideoAd();
        }
        else{
            if(this.listener==null) return;
            this.listener.onAdNotFound();
        }

    }

    protected void loadBannerAd(){

        bannerMgr.setPublisherId(this.publicationId);
        android.util.Log.d("water","request banner");
        bannerMgr.requestAd();

    }

    protected void loadVideoAd(){

        videoMgr.setPublisherId(this.publicationId);
        android.util.Log.d("water","request video");
        videoMgr.requestAd();

    }

    protected void loadNativeFormatAd(){

        Log.d("waterfall load native format ad");
        final Interstitial _this = this;
        final NativeFormatInterstitial ni = new NativeFormatInterstitial(this.ctx,this.publicationId);

        ni.setListener(new NativeFormatView.NativeFormatAdListener(){

            @Override
            public void onNativeFormatLoaded(String html) {
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void onNativeFormatFailed(Exception e) {
                _this.loadAdInternal();
            }

            @Override
            public void onNativeFormatDismissed(NativeFormatView banner) {

            }
        });

        ni.loadAd();
    }

}
