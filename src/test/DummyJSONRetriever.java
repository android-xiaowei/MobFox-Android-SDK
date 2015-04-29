package test;

import com.adsdk.sdk.networking.JSONRetriever;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nabriski on 4/29/15.
 */
public class DummyJSONRetriever implements JSONRetriever{
    @Override
    public void retrieve(String url, Listener listener) {
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

        try {
            JSONObject obj = new JSONObject(json);
            listener.onFinish(null,obj);
        } catch (JSONException e) {
            listener.onFinish(e,null);
        }
    }
}
