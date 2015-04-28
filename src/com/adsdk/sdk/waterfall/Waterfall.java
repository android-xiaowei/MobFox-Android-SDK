package com.adsdk.sdk.waterfall;

import java.util.ArrayList;
import java.util.List;

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

    }

    List<Type> types = new ArrayList<Type>();

    public void add(String name,double prob){
        types.add(new Type(name,prob));
    }

    public String getType(int idx){
        return types.get(idx).name;
    }


    public int getNext(int lastIndex){
         for(int i=lastIndex+1; i<types.size(); i++){
             double rand = Math.random();
             Type t = types.get(i);
             if(t.prob<=rand) return i;
         }
        return -1;
    }

    public int getNext(){
        return getNext(-1);
    }

}
