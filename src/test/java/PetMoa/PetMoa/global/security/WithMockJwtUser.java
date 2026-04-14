package PetMoa.PetMoa.global.security;

import PetMoa.PetMoa.domain.user.entity.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 테스트용 JWT 인증 사용자 어노테이션
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtUserSecurityContextFactory.class)
public @interface WithMockJwtUser {
    long id() default 1L;
    String email() default "test@example.com";
    Role role() default Role.USER;
}