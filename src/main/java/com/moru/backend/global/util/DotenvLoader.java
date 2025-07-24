package com.moru.backend.global.util;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvLoader {
    public static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing() // .env 없을 때에도 에러 없이 통과
                .load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
