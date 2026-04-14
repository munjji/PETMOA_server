package PetMoa.PetMoa.global.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final JwtProperties jwtProperties;

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * Access Token 쿠키 추가
     */
    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        int maxAge = (int) (jwtProperties.getAccessTokenExpiry() / 1000);
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, maxAge);
    }

    /**
     * Refresh Token 쿠키 추가
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        int maxAge = (int) (jwtProperties.getRefreshTokenExpiry() / 1000);
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, maxAge);
    }

    /**
     * 쿠키 추가 (HttpOnly, Secure, SameSite=Strict)
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 개발환경: false, 운영환경: true
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // SameSite는 Cookie 객체에서 직접 지원하지 않으므로 헤더로 설정
        response.addHeader("Set-Cookie",
                String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=Strict",
                        name, value, maxAge));
    }

    /**
     * 쿠키에서 값 읽기
     */
    public Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Access Token 쿠키 삭제
     */
    public void deleteAccessTokenCookie(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
    }

    /**
     * Refresh Token 쿠키 삭제
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    /**
     * 모든 인증 쿠키 삭제
     */
    public void deleteAllAuthCookies(HttpServletResponse response) {
        deleteAccessTokenCookie(response);
        deleteRefreshTokenCookie(response);
    }

    /**
     * 쿠키 삭제
     */
    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}