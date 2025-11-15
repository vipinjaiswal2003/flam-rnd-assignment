cmake_minimum_required(VERSION 3.22.1)

project("flam_edge_viewer")

# TODO: Set this to your actual OpenCV Android SDK path
set(OpenCV_DIR "PATH/TO/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)

add_library(
        native-lib
        SHARED
        native-lib.cpp
        opencv_processing.cpp)

target_include_directories(
        native-lib PRIVATE
        ${OpenCV_INCLUDE_DIRS})

find_library(
        log-lib
        log)

target_link_libraries(
        native-lib
        ${OpenCV_LIBS}
        ${log-lib})
