package com.adsdk.sdk;

import com.adsdk.sdk.video.VAST;

import junit.framework.TestCase;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.lang.Exception;
import java.lang.System;

/**
 * Created by nabriski on 5/6/15.
 */
public class VastTest extends TestCase{

    public void testValidity() throws Exception{

        Serializer serializer = new Persister();

        VAST v;
        //try {

            v = serializer.read(VAST.class,Thread.currentThread().getContextClassLoader().getResourceAsStream("/vast.xml"));
        //} catch (Exception e) {

        //    e.printStackTrace();

           // assert(false);
       // }

        assert(true);
    }

    public void testTritonValidity() throws Exception{

        Serializer serializer = new Persister();

        VAST v;
       // try {
            v = serializer.read(VAST.class,Thread.currentThread().getContextClassLoader().getResourceAsStream("/vast-triton.xml"));

       // } catch (Exception e) {

         //   e.printStackTrace();
        //    assert(false);
       // }

        assert(true);
    }

    /*public void testMixberryValidity(){

        Serializer serializer = new Persister();
        File vastXML = new File("./testUtils/vast-mixberry.xml");
        VAST v;
        try {
            v = serializer.read(VAST.class,vastXML);
        } catch (Exception e) {

            e.printStackTrace();
            assert(false);
        }

        assert(true);
    }*/

}
