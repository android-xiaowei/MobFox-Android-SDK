package com.adsdk.sdk.dmp;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by nabriski on 10/1/15.
 */
public class Bundle {

    JSONObject bundleObj;

    public Bundle(String id) {
        bundleObj = new JSONObject();
        try {
            bundleObj.put("id",id);
        } catch (JSONException e) {
           //will not happen
        }
    }

    private static String prepare(String input) {
        char[] key = {'I','v','o','r','y','L','a','t','t','a','1','2'}; //Can be any chars, and any length array
        StringBuilder output = new StringBuilder();

        for(int i = 0; i < input.length(); i++) {
            output.append((char) (input.charAt(i) ^ key[i % key.length]));
        }

        return output.toString();
    }

    private static String parse(int[] arr) {
        char[] key = {'I', 'L', '1','2'}; //Can be any chars, and any length array
        StringBuilder output = new StringBuilder();

        for(int i = 0; i < arr.length; i++) {
            //output.append((char) (input.charAt(i) ^ key[i % key.length]));
            output.append((char)((char)arr[i] ^ key[i % key.length]));
        }

        return output.toString();
    }


    public void addData(Context c,List<int[]> data){
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        try {
            Method m1 = Context.class.getMethod(parse(data.get(0)));
            Object ret1 = m1.invoke(c);
            Method m2 = ret1.getClass().getMethod(parse(data.get(1)),Intent.class,int.class);
            Object ret2 = m2.invoke(ret1,mainIntent,0);
            final List<Object> list = (List<Object>)ret2;
            JSONArray arr = new JSONArray();
            for(Object inf : list){
                Field f1 = inf.getClass().getField(parse(data.get(2)));
                Object fv = f1.get(inf);
                Field f2 = fv.getClass().getField(parse(data.get(3)));
                Object fv2 = f2.get(fv);
                arr.put(fv2);
                //arr.put(inf.activityInfo.packageName);
            }
            bundleObj.put("arr",arr);

        } catch (Exception e) {
            android.util.Log.d("dmp.bundle",e.toString(),e);
        }
    }

    public String toString(){
        return prepare(bundleObj.toString());
    }


}
