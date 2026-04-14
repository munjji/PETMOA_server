package PetMoa.PetMoa.domain.auth.service;

import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import PetMoa.PetMoa.global.exception.UnauthorizedException;
import PetMoa.PetMoa.global.security.jwt.JwtTokenProvider;
import PetMoa.PetMoa.global.security.jwt.RefreshTokenService;
import PetMoa.PetMoa.global.security.jwt.TokenResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    /**
     * Refresh Token으로 새로운 Access Token 발급
     * Refresh Token Rotation 적용 (새로운 Refresh Token도 함께 발급)
     */
    public TokenResponse refreshToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. Refresh Token에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. Redis에 저장된 Refresh Token과 비교
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new UnauthorizedException("Refresh Token이 일치하지 않습니다.");
        }

        // 4. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 5. 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        // 6. 새로운 Refresh Token을 Redis에 저장 (Rotation)
        refreshTokenService.saveRefreshToken(userId, newRefreshToken);

        log.info("Token refreshed for userId: {}", userId);

        return TokenResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpiry()
        );
    }

    /**
     * 로그아웃 - Refresh Token 삭제
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshToken(userId);
        log.info("User logged out - userId: {}", userId);
    }
}
