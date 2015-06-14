package test;

import android.test.AndroidTestCase;
import com.adsdk.sdk.waterfall.WaterfallBanner;
import com.adsdk.sdk.waterfall.WaterfallManager;

/**
 * Created by nabriski on 4/29/15.
 */
public class WaterfallBannerTest extends AndroidTestCase {


    public void testBasic(){

        WaterfallManager.setRetriever(new DummyJSONRetriever());
        WaterfallBanner banner = new WaterfallBanner(this.getContext());
        banner.setPublicationId("111");
        banner.setWaterfallBannerListener(new WaterfallBanner.Listener(){

            @Override
            public void onAdLoaded() {
                assertNotNull(null);
            }

            @Override
            public void onAdNotFound() {
                assertNull(null);
            }
        });
        banner.loadAd();

    }

}
