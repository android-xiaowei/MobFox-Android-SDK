package com.adsdk.sdk.nativeformats.creative;

/**
 * Created by itamar on 07/04/15.
 */
public class Creative {

    int width;
    int height;
    String template;
    String name;
    double prob;

    public Creative(String name,String template,int width,int height,double prob){
        this.name       = name;
        this.template   = template;
        this.width      = width;
        this.height     = height;
        this.prob       = prob;
    }

    public String getName(){
        return name;
    }

    public String getTemplate(){
        return template;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public double getProb(){
        return prob;
    }
}
