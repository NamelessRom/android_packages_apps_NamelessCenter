LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13-r20 butterknife ion

LOCAL_SRC_FILES    := $(call all-java-files-under, java)

LOCAL_ASSET_DIR    := $(LOCAL_PATH)/assets
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME := NamelessCenter
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_SDK_VERSION := 19

LOCAL_JNI_SHARED_LIBRARIES := libopendelta
LOCAL_REQUIRED_MODULES     := libopendelta

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
