package PetMoa.PetMoa.global.security;

import PetMoa.PetMoa.domain.user.entity.Role;
import PetMoa.PetMoa.global.security.jwt.JwtUserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 테스트용 @CurrentUser 파라미터 리졸버
 * X-User-Id 헤더에서 userId를 읽어 JwtUserPrincipal을 생성
 */
public class MockJwtUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(JwtUserPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String userIdHeader = webRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            throw new IllegalArgumentException("X-User-Id 헤더가 필요합니다.");
        }

        Long userId = Long.parseLong(userIdHeader);
        return JwtUserPrincipal.of(userId, "test@example.com", Role.USER);
    }
}