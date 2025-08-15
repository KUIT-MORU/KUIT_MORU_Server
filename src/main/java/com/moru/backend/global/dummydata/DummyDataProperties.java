package com.moru.backend.global.dummydata;

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
    private int scrapCount;

    private String targetUserEmail;              // 사용자 기준 알림 타깃(없으면 test@example.com)
    private int userCentricFollowNotif = 5;      // FOLLOW_RECEIVED 개수
    private int userCentricRoutineCreatedNotif = 10; // ROUTINE_CREATED 개수
    private int searchHistoryPerUser = 10;
}
