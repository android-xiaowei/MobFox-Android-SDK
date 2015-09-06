#!/usr/bin/env bash
if [ -z "$1" ]
  then
    echo "No version supplied"
    exit
fi

OUT=$2
if [ -z "$2" ]
  then
    OUT="."
fi


VER=$1
TEMP=$(mktemp -d /tmp/sdk.XXXXXXXX)
cp -a release-{{ver}}-android $TEMP/ 
mv $TEMP/release-{{ver}}-android $TEMP/release-$VER-android
DIR=$TEMP/release-$VER-android
mv $DIR/MobFox-Android-SDK-{{ver}} $DIR/MobFox-Android-SDK-$VER
mv $DIR/MobFox_Android_SDK+Mediation/Readme_GoogleAdMobAdapter_MobFoxSDK_Android_{{ver}}.txt $DIR/MobFox_Android_SDK+Mediation/Readme_GoogleAdMobAdapter_MobFoxSDK_Android_$VER.txt
cp ../build/outputs/MobFox-Android-SDK-$VER.jar $DIR/MobFox-Android-SDK-$VER/AdSdk_$VER.jar

cp $DIR/MobFox-Android-SDK-$VER/AdSdk_$VER.jar $DIR/MobFox-Android-SDK-$VER/AdSDK_Demo/libs/
cp $DIR/MobFox-Android-SDK-$VER/AdSdk_$VER.jar $DIR/MobFox-Android-SDK-$VER/AdSDK_NativeAdsDemo/libs/
cp $DIR/MobFox-Android-SDK-$VER/AdSdk_$VER.jar $DIR/MobFox-Android-SDK-$VER/AdSDK_XML_Demo/libs/

cp ../out/artifacts/Mobfox_Android_SDK/Mobfox-Android-SDK.jar $DIR/MobFox_Android_SDK+Mediation/AdSdk_$VER.jar

pushd $TEMP
zip -r MobFox-Android-SDK-$VER.zip release-$VER-android
popd

mv $TEMP/MobFox-Android-SDK-$VER.zip $OUT/
