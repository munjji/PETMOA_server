package PetMoa.PetMoa.global.security;

import PetMoa.PetMoa.global.security.jwt.JwtUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

/**
 * @WithMockJwtUser 어노테이션을 위한 SecurityContext 팩토리
 */
public class WithMockJwtUserSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        JwtUserPrincipal principal = JwtUserPrincipal.of(
                annotation.id(),
                annotation.email(),
                annotation.role()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + annotation.role().name()))
                );

        context.setAuthentication(authentication);
        return context;
    }
}