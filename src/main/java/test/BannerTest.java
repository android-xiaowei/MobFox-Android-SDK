package test;

import android.test.AndroidTestCase;
import com.adsdk.sdk.waterfall.Banner;
import com.adsdk.sdk.waterfall.WaterfallManager;

/**
 * Created by nabriski on 4/29/15.
 */
public class BannerTest extends AndroidTestCase {


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
        Banner banner = new Banner(this.getContext());
        banner.setPublicationId("111");
        banner.setWaterfallBannerListener(new Banner.Listener(){

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
