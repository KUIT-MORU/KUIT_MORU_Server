package com.moru.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dummy.data")
@Getter
@Setter
public class DummyDataProperties {
    private int userCount;
    private int routineCount;
    private int followCount;
    private int favoriteTagCount;
}
