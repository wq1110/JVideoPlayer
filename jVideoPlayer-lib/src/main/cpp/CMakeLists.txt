cmake_minimum_required(VERSION 3.10.2)

project("libass")
set(libsPath ${CMAKE_HOME_DIRECTORY}/${ANDROID_ABI})
set(LIBRARY_OUTPUT_PATH ${PROJECT_BINARY_DIR}/libs)
include_directories(${CMAKE_SOURCE_DIR}/include)
add_library( # Sets the name of the library.
        assAndroid
        SHARED
        native-lib.cpp)

add_library(
        ass-lib
        STATIC
        IMPORTED)

set_target_properties(
        ass-lib
        PROPERTIES
        IMPORTED_LOCATION
        ${libsPath}/libass.a)

add_library(
        freetype-lib
        STATIC
        IMPORTED)

set_target_properties(
        freetype-lib
        PROPERTIES IMPORTED_LOCATION
        ${libsPath}/libfreetype.a)

add_library(
        fribidi-lib
        STATIC
        IMPORTED)

set_target_properties(
        fribidi-lib
        PROPERTIES IMPORTED_LOCATION
        ${libsPath}/libfribidi.a)

add_library(
        png-lib
        STATIC
        IMPORTED)

set_target_properties(
        png-lib
        PROPERTIES IMPORTED_LOCATION
        ${libsPath}/libpng.a)

add_library(
        expat-lib
        STATIC
        IMPORTED)

set_target_properties(
        expat-lib
        PROPERTIES IMPORTED_LOCATION
        ${libsPath}/libexpat.a)

add_library(
        fontconfig-lib
        STATIC
        IMPORTED)

set_target_properties(
        fontconfig-lib
        PROPERTIES IMPORTED_LOCATION
        ${libsPath}/libfontconfig.a)



target_link_libraries( # Specifies the target library.
        assAndroid
        c
        m
        z
        ass-lib
        freetype-lib
        png-lib
        fribidi-lib
        fontconfig-lib
        freetype-lib
        expat-lib
        c
        jnigraphics
        log)