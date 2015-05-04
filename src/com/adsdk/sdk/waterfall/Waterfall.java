package com.adsdk.sdk.waterfall;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by nabriski on 4/28/15.
 */
public class Waterfall {

    protected class Type{

        public Type(String name, double prob){

            this.name = name;
            this.prob = prob;

        }

        public String name;
        public double prob;

        @Override
        public String toString(){
          return "("+name+","+prob+")";
        };

    }

    Queue<Type> types = new LinkedList<Type>();

    public void add(String name,double prob){
        types.add(new Type(name,prob));
    }

    public Waterfall(){};
    public Waterfall(Waterfall src){
        types = new LinkedList<Type>(src.types);
    }

    public String getNext(){
        while(types.size() > 0) {
            //System.out.println(types.toString());
            Log.d("waterfall","waterfall is : " + types.toString());
            Type t = types.peek();
            types.remove();

            double rand = Math.random();
            Log.d("waterfall","waterfall is : rand: " + rand + " , prob: "+t.prob);
            if (t.prob > rand) return t.name;
            else append(t.name);
        }
        return null;
    };

    protected void append(String name){
        types.add(new Type(name,1.0));
    };

    @Override
    public String toString(){
        return types.toString();
    };

}
