package com.adsdk.sdk;

import com.adsdk.sdk.waterfall.Waterfall;

import junit.framework.TestCase;

/**
 * Created by nabriski on 4/28/15.
 */
public class WaterfallTest extends TestCase {

    public void testBasic() {

        Waterfall w = new Waterfall();
        w.add("koko", 1);
        w.add("yoyo", 0);
        w.add("loko", 1);

        String name = w.getNext();
        assertEquals(name, "koko");
        name = w.getNext();
        assertEquals(name, "loko");
        name = w.getNext();
        assertEquals(name, "yoyo");


    }

    public void testCopy(){
        Waterfall w = new Waterfall();
        w.add("koko",1);
        w.add("loko",0);
        w.add("momo",1);
        Waterfall copy = new Waterfall(w);
        assertEquals(copy.getNext(),"koko");
        assertEquals(w.getNext(),"koko");
        while(w.getNext()!=null){};

        assertEquals(copy.getNext(),"momo");
    }

}
