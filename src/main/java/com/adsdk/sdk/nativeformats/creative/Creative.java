package com.adsdk.sdk.nativeformats.creative;

/**
 * Created by itamar on 07/04/15.
 */
public class Creative {

    String name;
    boolean webgl;
    String type;
    String template;
    double prob;

    public Creative(String name, boolean webgl, String type, String template, double prob) {
        this.name       = name;
        this.webgl      = webgl;
        this.type       = type;
        this.template   = template;
        this.prob       = prob;
    }

    public String getName() {
        return name;
    }

    public boolean getWebgl() {
        return webgl;
    }

    public String getType() {
        return type;
    }

    public String getTemplate() {
        return template;
    }

    public double getProb() {
        return prob;
    }

    public String toString(){
        return name+",webgl:"+webgl+","+type+","+prob;
    }
}
