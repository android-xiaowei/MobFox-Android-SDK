package com.adsdk.sdk.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nabriski on 7/15/15.
 */
public class UserAgent {

    String ua;

    public UserAgent(String userAgentStr){
        ua = userAgentStr;
    }

    public boolean isChrome(){
        return ua.indexOf("Chrome") >= 0;
    }

    public double getAndroidVersion(){
        Pattern p = Pattern.compile("\\bAndroid ([0-9]+\\.[0-9]+)");
        Matcher m = p.matcher(ua);
        m.find();
        String verStr = m.group(1);
        return Double.parseDouble(verStr);
    }

    public int getChromeVersion(){
        if(!isChrome()) return -1;
        Pattern p = Pattern.compile("\\bChrome/([0-9]+)");
        Matcher m = p.matcher(ua);
        m.find();
        String verStr = m.group(1);
        return Integer.parseInt(verStr);
    }
}
