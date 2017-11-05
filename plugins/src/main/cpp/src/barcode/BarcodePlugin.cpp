//
//  BarcodePlugin.cpp
//  DevApplication
//
//  Created by Andreas Schacherbauer on 15/05/15.
//  Copyright (c) 2015 Wikitude. All rights reserved.
//

#include "BarcodePlugin.h"
#include "jniHelper.h"
#include "jni.h"

#include <iostream>
#include <sstream>

jobject barcodeActivityObj;

extern "C" JNIEXPORT void JNICALL
Java_com_wikitude_samples_plugins_BarcodePluginActivity_initNative(JNIEnv* env, jobject obj)
{
    env->GetJavaVM(&pluginJavaVM);
    barcodeActivityObj = env->NewGlobalRef(obj);
}

BarcodePlugin::BarcodePlugin(int cameraFrameWidth, int cameraFrameHeight) :
Plugin("com.wikitude.android.barcodePlugin"),
_worldNeedsUpdate(0),
_image(cameraFrameWidth, cameraFrameHeight, "Y800", nullptr, 0),
_jniInitialized(false)
{
}

BarcodePlugin::~BarcodePlugin()
{
	JavaVMResource vm(pluginJavaVM);
	vm.env->DeleteGlobalRef(barcodeActivityObj);
}


void BarcodePlugin::initialize() {
    _imageScanner.set_config(zbar::ZBAR_NONE, zbar::ZBAR_CFG_ENABLE, 1);
}

void BarcodePlugin::destroy() {
    _image.set_data(nullptr, 0);
}

void BarcodePlugin::cameraFrameAvailable(const wikitude::sdk::Frame& cameraFrame_) {
	if (!_jniInitialized) {
        JavaVMResource vm(pluginJavaVM);
        jclass clazz = vm.env->FindClass("com/wikitude/samples/plugins/BarcodePluginActivity");
        _methodId = vm.env->GetMethodID(clazz, "onBarcodeDetected", "(Ljava/lang/String;)V");
        _jniInitialized = true;
	}

	int frameWidth = cameraFrame_.getSize().width;
    int frameHeight = cameraFrame_.getSize().height;

    _image.set_data(cameraFrame_.getData(), frameWidth * frameHeight);

    int n = _imageScanner.scan(_image);

    if ( n != _worldNeedsUpdate ) {
        if ( n ) {
            JavaVMResource vm(pluginJavaVM);
            zbar::Image::SymbolIterator symbol = _image.symbol_begin();
            jstring codeContent = vm.env->NewStringUTF(symbol->get_data().c_str());
            vm.env->CallVoidMethod(barcodeActivityObj, _methodId, codeContent);

        }
    }

    _worldNeedsUpdate = n;
}

void BarcodePlugin::update(const wikitude::sdk::RecognizedTargetsBucket& recognizedTargetsBucket_) {
}
