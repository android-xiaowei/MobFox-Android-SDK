package com.adsdk.sdk.waterfall;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.banner.AdView;
import com.adsdk.sdk.nativeformats.NativeFormatView;

/**
 * Created by nabriski on 4/28/15.
 */
public class WaterfallBanner extends FrameLayout {

    public interface Listener{
        public void onAdLoaded();
        public void onAdNotFound();
    }

    Listener listener = null;
    String publicationId;
    Waterfall w= null;

    public WaterfallBanner(Context context){
        super(context);
    }
    public WaterfallBanner(Context context, AttributeSet attrs){
        super(context,attrs);

    }

    public void setPublicationId(String publicationId) {
        this.publicationId = publicationId;
        WaterfallManager manager = WaterfallManager.getInstance(publicationId);
    }

    public void setWaterfallBannerListener(Listener listener){
        this.listener = listener;
    }

    public void loadAd(){
        WaterfallManager manager = WaterfallManager.getInstance(this.publicationId);
        w = manager.getWaterfall("banner");
        loadAdInternal();
    }

    private void loadAdInternal(){

        String type = w.getNext();
        Log.d("waterfall","Got from waterfall > "+type);

        if("banner".equals(type)){
            Log.d("waterfall", "loading banner");
            loadBannerAd();
        }
        else if("nativeFormat".equals(type)){
            Log.d("waterfall", "loading native format");
            loadNativeFormatAd();
        }
        else{
            if(this.listener==null) return;
            this.listener.onAdNotFound();
        }

    }

    protected void loadBannerAd(){

        final WaterfallBanner _this = this;
        Log.d("waterfall","banner pub id: "+this.publicationId);
        AdView view = new AdView(this.getContext(), "http://my.mobfox.com/request.php",this.publicationId, true, true);
        float density = getResources().getDisplayMetrics().density;
        int width = (int)(this.getWidth() / density);
        int height =  (int)(this.getHeight() / density);

        Log.d("waterfall","banner width: "+width);
        Log.d("waterfall","banner height: "+height);

        view.setAdspaceWidth(width);
        view.setAdspaceHeight(height);
        view.setAdspaceStrict(false);

        view.setAdListener(new AdListener() {

            @Override
            public void adClicked() {

            }

            @Override
            public void adClosed(Ad ad, boolean completed) {

            }

            @Override
            public void adLoadSucceeded(Ad ad) {
                Log.d("waterfall", "banner load succeeded");
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                Log.d("waterfall", "banner no ad found");
                _this.loadAdInternal();
            }
        });
        //view.loadNextAd();
        Log.d("waterfall", "init banner view");
        this.removeAllViews();
        this.addView(view);

    }

    protected void loadNativeFormatAd(){

        Log.d("waterfall", "load native format ad");
        final WaterfallBanner _this = this;
        final NativeFormatView nfw = new NativeFormatView(this.getContext());
        nfw.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        nfw.setPublicationId(this.publicationId);

        float density = getResources().getDisplayMetrics().density;
        int width = (int)(this.getWidth() / density);
        nfw.setAdHeight(width);
        int height =  (int)(this.getHeight() / density);
        nfw.setAdHeight(height);

        nfw.setListener(new NativeFormatView.NativeFormatAdListener(){

            @Override
            public void onNativeFormatLoaded(String html) {

                float density = getResources().getDisplayMetrics().density;
                int width = (int)(nfw.getWidth() / density);
                int height =  (int)(nfw.getHeight() / density);
                Log.d("waterfall","nfw width2: "+width);
                Log.d("waterfall","nfw height2: "+height);

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

        Log.d("waterfall", "init native format view");

        this.removeAllViews();
        this.addView(nfw);
        nfw.loadAd();

    }

}
