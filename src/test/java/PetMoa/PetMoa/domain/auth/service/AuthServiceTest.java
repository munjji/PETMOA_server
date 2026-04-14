package PetMoa.PetMoa.domain.auth.service;

import PetMoa.PetMoa.domain.user.entity.AuthProvider;
import PetMoa.PetMoa.domain.user.entity.Role;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import PetMoa.PetMoa.global.exception.UnauthorizedException;
import PetMoa.PetMoa.global.security.jwt.JwtTokenProvider;
import PetMoa.PetMoa.global.security.jwt.RefreshTokenService;
import PetMoa.PetMoa.global.security.jwt.TokenResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private static final Long USER_ID = 1L;
    private static final String REFRESH_TOKEN = "valid-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.KAKAO)
                .providerId("12345")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", USER_ID);
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshToken {

        @Test
        @DisplayName("성공: 토큰 갱신")
        void refreshToken_Success() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(USER_ID);
            given(refreshTokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).willReturn(true);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.createAccessToken(testUser)).willReturn(NEW_ACCESS_TOKEN);
            given(jwtTokenProvider.createRefreshToken(testUser)).willReturn(NEW_REFRESH_TOKEN);
            given(jwtTokenProvider.getAccessTokenExpiry()).willReturn(1800000L);

            // when
            TokenResponse result = authService.refreshToken(REFRESH_TOKEN);

            // then
            assertThat(result.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(result.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);
            assertThat(result.expiresIn()).isEqualTo(1800000L);

            // Refresh Token Rotation 확인
            verify(refreshTokenService).saveRefreshToken(USER_ID, NEW_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("실패: 유효하지 않은 Refresh Token")
        void refreshToken_InvalidToken() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("유효하지 않은 Refresh Token");
        }

        @Test
        @DisplayName("실패: Redis에 저장된 토큰과 불일치")
        void refreshToken_TokenMismatch() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(USER_ID);
            given(refreshTokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Refresh Token이 일치하지 않습니다");
        }

        @Test
        @DisplayName("실패: 사용자를 찾을 수 없음")
        void refreshToken_UserNotFound() {
            // given
            given(jwtTokenProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(USER_ID);
            given(refreshTokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN)).willReturn(true);
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공: 로그아웃")
        void logout_Success() {
            // when
            authService.logout(USER_ID);

            // then
            verify(refreshTokenService).deleteRefreshToken(USER_ID);
        }
    }
}