package PetMoa.PetMoa.global.security.jwt;

import PetMoa.PetMoa.domain.user.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰에서 추출한 사용자 정보를 담는 Principal 객체
 * DB 조회 없이 인증 정보를 제공
 */
@Getter
@RequiredArgsConstructor
public class JwtUserPrincipal {

    private final Long id;
    private final String email;
    private final Role role;

    public static JwtUserPrincipal of(Long id, String email, Role role) {
        return new JwtUserPrincipal(id, email, role);
    }
}
