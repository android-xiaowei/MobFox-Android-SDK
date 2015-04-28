package com.adsdk.sdk.waterfall;

import android.content.Context;
import android.widget.FrameLayout;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.adsdk.sdk.banner.AdView;
import com.adsdk.sdk.nativeformats.NativeFormatView;

/**
 * Created by nabriski on 4/28/15.
 */
public class WaterfallBanner extends FrameLayout {

    int lastIndex = -1;

    public interface Listener{
        public void onAdLoaded();
        public void onAdNotFound();
    }

    Listener listener = null;
    String publicationId;

    public WaterfallBanner(Context context,String publicationId) {
        super(context);
        this.publicationId = publicationId;
    }

    public void loadAd(){

        WaterfallManager manager = WaterfallManager.getInstance();
        Waterfall w = manager.getWaterfall("banner");
        if(this.lastIndex < 0){
            this.lastIndex = w.getNext();
        }
        String type = w.getType(this.lastIndex);

        if(type=="banner"){
            loadBannerAd();
        }
        else if(type=="nativeFormat"){
            loadNativeFormatAd();
        }

    }

    protected void loadBannerAd(){

        final WaterfallBanner _this = this;

        AdView view = new AdView(this.getContext(), "http://my.mobfox.com/request.php",this.publicationId, true, true);
        float density = getResources().getDisplayMetrics().density;
        int width = (int)(this.getWidth() / density);
        int height =  (int)(this.getWidth() / density);

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
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void adShown(Ad ad, boolean succeeded) {

            }

            @Override
            public void noAdFound() {
                _this.loadAd();
            }
        });
        //view.loadNextAd();
        this.addView(view);

    }

    protected void loadNativeFormatAd(){

        final WaterfallBanner _this = this;
        NativeFormatView nfw = new NativeFormatView(this.getContext());
        nfw.setPublicationId(this.publicationId);
        nfw.setListener(new NativeFormatView.NativeFormatAdListener(){

            @Override
            public void onNativeFormatLoaded(String html) {
                if(_this.listener==null) return;
                _this.listener.onAdLoaded();
            }

            @Override
            public void onNativeFormatFailed(Exception e) {
                _this.loadAd();
            }

            @Override
            public void onNativeFormatDismissed(NativeFormatView banner) {

            }
        });
        nfw.loadAd();
    }

}
