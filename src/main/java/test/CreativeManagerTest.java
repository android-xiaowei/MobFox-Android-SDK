package test;

import com.adsdk.sdk.nativeformats.creative.Creative;
import com.adsdk.sdk.nativeformats.creative.CreativesManager;

import junit.framework.TestCase;


public class CreativeManagerTest extends TestCase {

    String json =
    "{"+
        "\"creatives\": ["+
        "{"+
            "\"name\": \"particles\","+
                "\"webgl\": true,"+
                "\"type\": \"block\","+
                "\"template\": \"<html></html>\","+
                "\"prob\": 1"+
        "},"+
        "{"+
            "\"name\": \"rotate\","+
                "\"webgl\": true,"+
                "\"type\": \"stripe\","+
                "\"template\": \"<html></html>\","+
                "\"prob\": 1"+
        "},"+
            "{"+
            "\"name\": \"fallback_block\","+
            "\"webgl\": false,"+
            "\"type\": \"block\","+
            "\"template\": \"<html></html>\","+
            "\"prob\": 1"+
            "},"+
            "{"+
            "\"name\": \"fallback_stripe\","+
            "\"webgl\": false,"+
            "\"type\": \"stripe\","+
            "\"template\": \"<html></html>\","+
            "\"prob\": 1"+
            "}"+
        "]"+
    "}";

    public void testBasic(){
        CreativesManager.setRetriever(new DummyJSONRetriever(json));
        CreativesManager manager = CreativesManager.getInstance(null,"111");

        Creative c = manager.getCreative("block","user-agent");
        //System.out.println(c.toString());
        assert(c!=null);
        assert(!c.getWebgl());
        assertEquals("fallback_block",c.getName());
        assertEquals("block",c.getType());

        c = manager.getCreative("stripe","user-agent");
        assert(!c.getWebgl());
        assertEquals(c.getName(),"fallback_stripe");
        assertEquals(c.getType(),"stripe");

    }

    public void testGetWebGL(){

        CreativesManager.setRetriever(new DummyJSONRetriever(json));
        CreativesManager manager = CreativesManager.getInstance(null,"111");

        Creative c = manager.getCreative("block","Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36");
        //System.out.println(c.toString());
        assert(c!=null);
        assert(c.getWebgl());
        assertEquals("particles",c.getName());
        assertEquals("block",c.getType());

        c = manager.getCreative("stripe","Mozilla/5.0 (Linux; Android 5.1.1; Nexus 5 Build/LMY48B; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36");
        assert(c.getWebgl());
        assertEquals(c.getName(),"rotate");
        assertEquals(c.getType(),"stripe");

    }
}