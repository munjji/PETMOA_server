package PetMoa.PetMoa.global.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RBucket<String> bucket;

    private RefreshTokenService refreshTokenService;

    private static final Long USER_ID = 1L;
    private static final String REFRESH_TOKEN = "test-refresh-token";
    private static final String KEY = "refresh_token:1";

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(redissonClient, jwtProperties);
    }

    @Nested
    @DisplayName("Refresh Token 저장")
    class SaveRefreshToken {

        @Test
        @DisplayName("성공: Refresh Token 저장")
        void saveRefreshToken_Success() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(jwtProperties.getRefreshTokenExpiry()).willReturn(604800000L);

            // when
            refreshTokenService.saveRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            verify(bucket).set(REFRESH_TOKEN, Duration.ofMillis(604800000L));
        }
    }

    @Nested
    @DisplayName("Refresh Token 조회")
    class GetRefreshToken {

        @Test
        @DisplayName("성공: Refresh Token 조회")
        void getRefreshToken_Success() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(bucket.get()).willReturn(REFRESH_TOKEN);

            // when
            String result = refreshTokenService.getRefreshToken(USER_ID);

            // then
            assertThat(result).isEqualTo(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("성공: 존재하지 않는 토큰 조회 시 null 반환")
        void getRefreshToken_NotFound() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(bucket.get()).willReturn(null);

            // when
            String result = refreshTokenService.getRefreshToken(USER_ID);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Refresh Token 삭제")
    class DeleteRefreshToken {

        @Test
        @DisplayName("성공: Refresh Token 삭제")
        void deleteRefreshToken_Success() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);

            // when
            refreshTokenService.deleteRefreshToken(USER_ID);

            // then
            verify(bucket).delete();
        }
    }

    @Nested
    @DisplayName("Refresh Token 검증")
    class ValidateRefreshToken {

        @Test
        @DisplayName("성공: 유효한 Refresh Token 검증")
        void validateRefreshToken_Success() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(bucket.get()).willReturn(REFRESH_TOKEN);

            // when
            boolean isValid = refreshTokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("실패: 토큰 불일치")
        void validateRefreshToken_TokenMismatch() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(bucket.get()).willReturn(REFRESH_TOKEN);

            // when
            boolean isValid = refreshTokenService.validateRefreshToken(USER_ID, "wrong-token");

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("실패: 저장된 토큰 없음")
        void validateRefreshToken_NoStoredToken() {
            // given
            given(redissonClient.<String>getBucket(KEY)).willReturn(bucket);
            given(bucket.get()).willReturn(null);

            // when
            boolean isValid = refreshTokenService.validateRefreshToken(USER_ID, REFRESH_TOKEN);

            // then
            assertThat(isValid).isFalse();
        }
    }
}