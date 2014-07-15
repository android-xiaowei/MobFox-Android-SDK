package com.adsdk.sdk.customevents;

import java.lang.reflect.Constructor;

public class CustomEventNativeFactory {
	private static CustomEventNativeFactory instance = new CustomEventNativeFactory();

    public static CustomEventNative create(String className) throws Exception {
        return instance.internalCreate(className);
    }

    protected CustomEventNative internalCreate(String className) throws Exception {
    	className = "com.adsdk.sdk.customevents." + className + "Native";
        Class<? extends CustomEventNative> nativeClass = Class.forName(className)
                .asSubclass(CustomEventNative.class);
        Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomEventNative) nativeConstructor.newInstance();
    }
}
