package test;

import com.adsdk.sdk.utils.UserAgent;

import junit.framework.TestCase;


/**
 * Created by nabriski on 7/15/15.
 */
public class UserAgentTest extends TestCase {

    public void testIsChrome(){
        UserAgent ua = new UserAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
        assert(ua.isChrome());

        ua = new UserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30");
        assert(!ua.isChrome());

    }

    public void testAndroidVersion(){
        UserAgent ua = new UserAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
        assertEquals(ua.getAndroidVersion(),4.4);

        ua = new UserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30");
        assertEquals(ua.getAndroidVersion(),4.1);
    }

    public void testChromeVersion(){
        UserAgent ua = new UserAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
        assertEquals(ua.getChromeVersion(),30);

        ua = new UserAgent("Mozilla/5.0 (Linux; U; Android 4.1.1; en-gb; Build/KLP) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30");
        assertEquals(ua.getChromeVersion(),-1);
    }

    public void testEmualtorUA(){

        UserAgent ua = new UserAgent("Mozilla/5.0 (Linux; Android 5.1; Android SDK built for x86 Build/LKY45) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/39.0.0.0 Mobile Safari/537.36");
        assert(ua.isChrome());
        assertEquals(ua.getChromeVersion(),39);
        assertEquals(ua.getAndroidVersion(),5.1);
    }
}