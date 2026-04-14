package PetMoa.PetMoa.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedissonClient redissonClient;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    // Refresh Token 저장
    public void saveRefreshToken(Long userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(key);

        long expiryMillis = jwtProperties.getRefreshTokenExpiry();
        bucket.set(refreshToken, Duration.ofMillis(expiryMillis));

        log.debug("Refresh token saved for userId: {}", userId);
    }

    // Refresh Token 조회
    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    // Refresh Token 삭제 (로그아웃)
    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        RBucket<String> bucket = redissonClient.getBucket(key);
        bucket.delete();

        log.debug("Refresh token deleted for userId: {}", userId);
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}