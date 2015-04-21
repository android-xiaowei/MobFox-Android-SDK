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

    public NativeFormatInterstitial(Context ctx){
        CreativesManager.getInstance(ctx);
        this.ctx = ctx;
    };

    public void setPublicationId(String publicationId){
        this.publicationId = publicationId;
    }

    public void loadAd(){
        Intent intent = new Intent(ctx, NativeFormatInterstitialActivity.class);
        intent.putExtra("PUBLICATION_ID", publicationId);

        ctx.startActivity(intent);
        //TODO: perhaps preload ad before actually starting activity?
        // Yes, I'll do this later
    };
}
