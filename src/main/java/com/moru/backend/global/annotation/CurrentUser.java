package com.moru.backend.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// "여기에 현재 사용자 주입"
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME) // 	Spring이 애노테이션을 인식하여 ArgumentResolver 등에서 처리 가능하게 하기 위해
public @interface CurrentUser {
}
