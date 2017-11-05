APP_MODULES := wikitudePlugins
APP_PLATFORM := android-15
APP_STL := c++_static
NDK_TOOLCHAIN_VERSION := clang

APP_CPPFLAGS := \
	-frtti \
	-fexceptions \
	-std=c++14 \


# ========= RELEASE =========
ifeq ($(BUILD_TYPE), release)
APP_OPTIM := release
endif

# ========= DEBUG =========
ifeq ($(BUILD_TYPE), debug)
APP_OPTIM := debug
endif

