package com.moru.backend.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = false)
public class DurationToLongConverter implements AttributeConverter<Duration, Long> {


    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        return (duration == null) ? null : duration.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long seconds) {
        return (seconds == null) ? null : Duration.ofSeconds(seconds);
    }
}
