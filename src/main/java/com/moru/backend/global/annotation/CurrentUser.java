package com.moru.backend.global.annotation;

import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// "여기에 현재 사용자 주입"
@Parameter(hidden = true)
@Target(ElementType.PARAMETER) // 스웨거에서 루틴 스텝이 안보이는거 해결하기 위해
@Retention(RetentionPolicy.RUNTIME) // 	Spring이 애노테이션을 인식하여 ArgumentResolver 등에서 처리 가능하게 하기 위해
public @interface CurrentUser {
}
