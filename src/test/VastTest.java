package test;

import com.adsdk.sdk.video.VAST;

import junit.framework.TestCase;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

/**
 * Created by nabriski on 5/6/15.
 */
public class VastTest extends TestCase{

    public void testValidity(){

        Serializer serializer = new Persister();
        File vastXML = new File("./testUtils/vast.xml");
        VAST v;
        try {
            v = serializer.read(VAST.class,vastXML);
        } catch (Exception e) {

            e.printStackTrace();
            assert(false);
        }

        assert(true);
    }

    public void testTritonValidity(){

        Serializer serializer = new Persister();
        File vastXML = new File("./testUtils/vast-triton.xml");
        VAST v;
        try {
            v = serializer.read(VAST.class,vastXML);
        } catch (Exception e) {

            e.printStackTrace();
            assert(false);
        }

        assert(true);
    }

}
