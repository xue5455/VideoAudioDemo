# $Id: Application.mk 212 2015-05-15 10:22:36Z oparviai $
#
# Build library bilaries for all supported architectures
#

APP_ABI := armeabi
APP_OPTIM := release
APP_STL := stlport_static
APP_CPPFLAGS := -fexceptions # -D SOUNDTOUCH_DISABLE_X86_OPTIMIZATIONS

