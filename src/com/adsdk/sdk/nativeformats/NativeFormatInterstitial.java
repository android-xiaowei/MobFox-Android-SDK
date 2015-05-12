package com.adsdk.sdk.nativeformats;

import android.content.Context;
import android.content.Intent;

import com.adsdk.sdk.nativeformats.creative.CreativesManager;

/**
 * Created by itamar on 29/03/15.
 */
public class NativeFormatInterstitial {

    String publicationId;
    Context ctx;
    NativeFormatInterstitialActivity activity;
    NativeFormatView.NativeFormatAdListener listener;

    public NativeFormatInterstitial(Context ctx,String publicationId){
        CreativesManager.getInstance(ctx,publicationId);
        this.publicationId = publicationId;
        this.ctx = ctx;
    };


    public void setListener(NativeFormatView.NativeFormatAdListener listener){
        this.listener = listener;
    }

    public void loadAd(){
        final Intent intent = new Intent(ctx, NativeFormatInterstitialActivity.class);
        NativeFormat nf = new NativeFormat(ctx,320,480,this.publicationId);

        nf.loadAd(new NativeFormat.Listener(){

            @Override
            public void onSuccess(String template, String data) {
                intent.putExtra("TEMPLATE", template);
                intent.putExtra("DATA", data);
                ctx.startActivity(intent);
                listener.onNativeFormatLoaded(template);
            }

            @Override
            public void onError(Exception e) {
                listener.onNativeFormatFailed(e);
            }
        });

    };
}
