cmake_minimum_required(VERSION 3.22.1)
project("musicverse_keys")

add_library(
    musicverse_keys
    SHARED
    src/main/cpp/keys.cpp
)

find_library(log-lib log)

target_link_libraries(musicverse_keys ${log-lib})
