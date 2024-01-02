
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("medicationtracker")
set(CMAKE_CXX_STANDARD 17)
set(SQLITE3_PATH $ENV{SQLITE3_LIB_PATH})

if (NOT SQLITE3_PATH)
    message(SEND_ERROR "Missing environment variable: \"SQLITE3_LIB_PATH\".")
endif ()

include_directories(${SQLITE3_PATH})
add_subdirectory(DbManager)
add_subdirectory(DatabaseController)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             medicationtracker

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             medicationtracker.cpp
        )

add_library(
        sqlite3 SHARED ${SQLITE3_PATH}/sqlite3.h ${SQLITE3_PATH}/sqlite3.c
)

add_definitions( -DSQLITE_ENABLE_JSON1 )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       medicationtracker
                       DbManager
                       android
                       sqlite3
                       DatabaseController
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

target_include_directories(sqlite3 PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")

