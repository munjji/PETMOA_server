package PetMoa.PetMoa.global.security.jwt;

import PetMoa.PetMoa.domain.user.entity.AuthProvider;
import PetMoa.PetMoa.domain.user.entity.Role;
import PetMoa.PetMoa.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JwtTokenProviderTest {

    private JwtProperties jwtProperties;
    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        // JwtProperties Mock 생성
        jwtProperties = mock(JwtProperties.class);
        given(jwtProperties.getSecret()).willReturn("petmoa-jwt-secret-key-minimum-32-characters-long");
        given(jwtProperties.getAccessTokenExpiry()).willReturn(1800000L); // 30분
        given(jwtProperties.getRefreshTokenExpiry()).willReturn(604800000L); // 7일

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        // @PostConstruct 수동 호출
        jwtTokenProvider.init();

        // 테스트 사용자 생성
        testUser = User.builder()
                .name("테스트유저")
                .email("test@example.com")
                .provider(AuthProvider.KAKAO)
                .providerId("12345")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    @Nested
    @DisplayName("Access Token 생성")
    class CreateAccessToken {

        @Test
        @DisplayName("성공: Access Token 생성")
        void createAccessToken_Success() {
            // when
            String token = jwtTokenProvider.createAccessToken(testUser);

            // then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3); // JWT 형식 (header.payload.signature)
        }

        @Test
        @DisplayName("성공: Access Token에 사용자 정보 포함")
        void createAccessToken_ContainsUserInfo() {
            // when
            String token = jwtTokenProvider.createAccessToken(testUser);
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);

            // then
            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get("role", String.class)).isEqualTo("USER");
            assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    class CreateRefreshToken {

        @Test
        @DisplayName("성공: Refresh Token 생성")
        void createRefreshToken_Success() {
            // when
            String token = jwtTokenProvider.createRefreshToken(testUser);

            // then
            assertThat(token).isNotNull();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("성공: Refresh Token에 사용자 ID만 포함")
        void createRefreshToken_ContainsOnlyUserId() {
            // when
            String token = jwtTokenProvider.createRefreshToken(testUser);
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);

            // then
            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get("role")).isNull(); // Refresh Token에는 role 없음
            assertThat(claims.get("email")).isNull(); // Refresh Token에는 email 없음
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class ValidateToken {

        @Test
        @DisplayName("성공: 유효한 토큰 검증")
        void validateToken_Success() {
            // given
            String token = jwtTokenProvider.createAccessToken(testUser);

            // when
            boolean isValid = jwtTokenProvider.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("실패: 잘못된 형식의 토큰")
        void validateToken_InvalidFormat() {
            // given
            String invalidToken = "invalid.token.format";

            // when
            boolean isValid = jwtTokenProvider.validateToken(invalidToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("실패: 빈 토큰")
        void validateToken_EmptyToken() {
            // when
            boolean isValid = jwtTokenProvider.validateToken("");

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("실패: null 토큰")
        void validateToken_NullToken() {
            // when
            boolean isValid = jwtTokenProvider.validateToken(null);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰에서 사용자 ID 추출")
    class GetUserIdFromToken {

        @Test
        @DisplayName("성공: 토큰에서 사용자 ID 추출")
        void getUserIdFromToken_Success() {
            // given
            String token = jwtTokenProvider.createAccessToken(testUser);

            // when
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // then
            assertThat(userId).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("만료 시간 조회")
    class GetExpiry {

        @Test
        @DisplayName("성공: Access Token 만료 시간 조회")
        void getAccessTokenExpiry_Success() {
            // when
            long expiry = jwtTokenProvider.getAccessTokenExpiry();

            // then
            assertThat(expiry).isEqualTo(1800000L);
        }

        @Test
        @DisplayName("성공: Refresh Token 만료 시간 조회")
        void getRefreshTokenExpiry_Success() {
            // when
            long expiry = jwtTokenProvider.getRefreshTokenExpiry();

            // then
            assertThat(expiry).isEqualTo(604800000L);
        }
    }
}