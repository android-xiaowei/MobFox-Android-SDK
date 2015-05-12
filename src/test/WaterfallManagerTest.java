package test;

import com.adsdk.sdk.networking.JSONRetriever;
import com.adsdk.sdk.waterfall.Waterfall;
import com.adsdk.sdk.waterfall.WaterfallManager;

import junit.framework.TestCase;

/**
 * Created by nabriski on 4/29/15.
 */
public class WaterfallManagerTest extends TestCase {

    public void testInit(){
        WaterfallManager.setRetriever(new DummyJSONRetriever());
        WaterfallManager manager = WaterfallManager.getInstance("111");
        Waterfall w = manager.getWaterfall("banner");
        assertNotNull(w);
        assertEquals(w.getNext(),"nativeFormat");
        assertEquals(w.getNext(),"banner");
        w = manager.getWaterfall("interstitial");
        assertEquals(w.getNext(),"nativeFormat");
        assertEquals(w.getNext(),"video");
        assertEquals(w.getNext(),"banner");
        assertNotNull(w);
    }

    public void testGetCopy(){
        DummyJSONRetriever ret = new DummyJSONRetriever();
        WaterfallManager.setRetriever(ret);

        WaterfallManager manager = WaterfallManager.getInstance("111");

        Waterfall w = manager.getWaterfall("interstitial");

        assertEquals(w.getNext(),"nativeFormat");
        assertEquals(w.getNext(),"video");
        assertEquals(w.getNext(),"banner");
        assertNull(w.getNext());

        w = manager.getWaterfall("interstitial");
        assertEquals(w.getNext(),"nativeFormat");

    }
}
