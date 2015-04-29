package test;

import com.adsdk.sdk.waterfall.Waterfall;
import com.adsdk.sdk.waterfall.WaterfallManager;

import junit.framework.TestCase;

/**
 * Created by nabriski on 4/29/15.
 */
public class WaterfallManagerTest extends TestCase {

    public void testInit(){
        WaterfallManager.setRetriever(new DummyJSONRetriever());
        WaterfallManager manager = WaterfallManager.getInstance();
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
        WaterfallManager.setRetriever(new DummyJSONRetriever());
        WaterfallManager manager = WaterfallManager.getInstance();
        Waterfall w = manager.getWaterfall("interstitial");
        assertEquals(w.getNext(),"nativeFormat");
        assertEquals(w.getNext(),"video");
        assertEquals(w.getNext(),"banner");
        assertNull(w.getNext());

        w = manager.getWaterfall("interstitial");
        assertEquals(w.getNext(),"nativeFormat");

    }
}
