package com.moru.backend.global.util;

public enum S3Directory {
    ROUTINE("routine/"),
    ROUTINE_SNAPSHOT("routineSnapshot/"),
    PROFILE("profile/"),
    TEMP("temp/");

    private final String dirName;

    S3Directory(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}
