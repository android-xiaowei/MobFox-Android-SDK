package test;

import android.test.AndroidTestCase;
import com.adsdk.sdk.waterfall.WaterfallBanner;
import com.adsdk.sdk.waterfall.WaterfallManager;

/**
 * Created by nabriski on 4/29/15.
 */
public class WaterfallBannerTest extends AndroidTestCase {


    String json = "{"+
            "\"waterfalls\":{"+
            "\"banner\" :["+
            "{\"name\":\"nativeFormat\",\"prob\":1.0},"+
            "{\"name\":\"banner\",\"prob\":1.0}"+
            "],"+
            "\"interstitial\":["+
            "{\"name\":\"nativeFormat\",\"prob\":1.0},"+
            "{\"name\":\"video\",\"prob\":1.0},"+
            "{\"name\":\"banner\",\"prob\":1.0}"+
            "]"+
            "}"+
            "}";

    public void testBasic(){

        WaterfallManager.setRetriever(new DummyJSONRetriever(json));
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
