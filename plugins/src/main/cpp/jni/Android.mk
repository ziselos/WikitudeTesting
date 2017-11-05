LOCAL_PATH := $(call my-dir)/..
LOCAL_PATH_SAVE := $(LOCAL_PATH)
SRC_DIR := $(LOCAL_PATH)/src
LIB_DIR := $(LOCAL_PATH)/lib
INCLUDE_DIR := $(LOCAL_PATH)/include

include $(CLEAR_VARS)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE    := iconv
LOCAL_SRC_FILES := libiconv.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE    := zbar
LOCAL_SRC_FILES := libzbar.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := opencv_core
LOCAL_SRC_FILES := libopencv_core.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := opencv_imgproc
LOCAL_SRC_FILES := libopencv_imgproc.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := opencv_objdetect
LOCAL_SRC_FILES := libopencv_objdetect.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := opencv_calib3d
LOCAL_SRC_FILES := libopencv_calib3d.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := tbb
LOCAL_SRC_FILES := libtbb.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE := z
LOCAL_SRC_FILES := libz.a
include $(PREBUILT_STATIC_LIBRARY)

LOCAL_PATH := $(LIB_DIR)/$(TARGET_ARCH_ABI)
include $(CLEAR_VARS)
LOCAL_MODULE    := WikitudePlugins
LOCAL_SRC_FILES := libWikitudePlugins.a
include $(PREBUILT_STATIC_LIBRARY)

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_PATH := $(LIB_DIR)/x86
	include $(CLEAR_VARS)
	LOCAL_MODULE    := ippicv
	LOCAL_SRC_FILES := libippicv.a
	include $(PREBUILT_STATIC_LIBRARY)
endif

LOCAL_PATH := $(SRC_DIR)
include $(CLEAR_VARS)

LOCAL_MODULE := wikitudePlugins

LOCAL_C_INCLUDES := $(INCLUDE_DIR)/wikitude \
	$(INCLUDE_DIR)/zbar \
	$(INCLUDE_DIR) \
	$(INCLUDE_DIR)/opencv2 \
	$(INCLUDE_DIR)/opencv2/core \
	$(INCLUDE_DIR)/opencv2/imgproc \
	$(INCLUDE_DIR)/opencv2/objdetect \
	$(SRC_DIR)/barcode \
	$(SRC_DIR)/custom_camera \
	$(SRC_DIR)/face_detection \
	$(SRC_DIR)/simple_input_plugin

LOCAL_SRC_FILES := jniHelper.cpp JniRegistration.cpp barcode/BarcodePlugin.cpp face_detection/FaceDetectionPlugin.cpp face_detection/FaceDetectionPluginConnector.cpp custom_camera/YUVFrameInputPlugin.cpp custom_camera/YUVFrameShaderSourceObject.cpp simple_input_plugin/SimpleInputPlugin.cpp

LOCAL_STATIC_LIBRARIES += zbar iconv opencv_objdetect opencv_calib3d opencv_imgproc opencv_core tbb z WikitudePlugins

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_STATIC_LIBRARIES += ippicv
endif

LOCAL_LDLIBS += -llog -lGLESv2

include $(BUILD_SHARED_LIBRARY)
