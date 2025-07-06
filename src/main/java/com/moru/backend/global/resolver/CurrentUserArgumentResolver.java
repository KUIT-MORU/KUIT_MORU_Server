package com.moru.backend.global.resolver;

import com.moru.backend.domain.auth.application.LoginService;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final LoginService authService;

    public CurrentUserArgumentResolver(LoginService authService) {
        this.authService = authService;
    }

    // @CurrentUser 파라미터 인식
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    // 실제 유저 반환
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return authService.getCurrentUser();
    }
}