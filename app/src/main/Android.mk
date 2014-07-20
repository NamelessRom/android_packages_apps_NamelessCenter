# Copyright (C) 2014 Alexander "Evisceration" Martinz
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13-r20 butterknife ion otto

LOCAL_SRC_FILES    := $(call all-java-files-under, java)

LOCAL_ASSET_DIR    := $(LOCAL_PATH)/assets
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_PACKAGE_NAME := NamelessCenter
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

#LOCAL_SDK_VERSION := 19

LOCAL_JNI_SHARED_LIBRARIES := libopendelta
LOCAL_REQUIRED_MODULES     := libopendelta

######

library_src_files += ../../../../../../external/cardslib/library/src/main/java
LOCAL_SRC_FILES += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../../../../external/cardslib/library/src/main/res

######

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages it.gmariotti.cardslib.library \

######

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
