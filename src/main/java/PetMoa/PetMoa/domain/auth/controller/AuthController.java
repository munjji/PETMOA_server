package PetMoa.PetMoa.domain.auth.controller;

import PetMoa.PetMoa.domain.auth.service.AuthService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import PetMoa.PetMoa.global.security.CurrentUser;
import PetMoa.PetMoa.global.security.jwt.CookieUtils;
import PetMoa.PetMoa.global.security.jwt.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Operation(summary = "카카오 로그인 URL", description = "카카오 OAuth2 로그인 URL을 반환합니다.")
    @GetMapping("/kakao/login-url")
    public ApiResponse<String> getKakaoLoginUrl() {
        return ApiResponse.onSuccess("/oauth2/authorization/kakao");
    }

    @Operation(summary = "토큰 갱신", description = "쿠키의 Refresh Token으로 새로운 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponse<Void> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtils.getCookieValue(request, CookieUtils.REFRESH_TOKEN_COOKIE)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 없습니다."));

        // 토큰 갱신
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);

        // 새 토큰을 쿠키로 설정
        cookieUtils.addAccessTokenCookie(response, tokenResponse.accessToken());
        cookieUtils.addRefreshTokenCookie(response, tokenResponse.refreshToken());

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 쿠키를 제거합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CurrentUser User user,
            HttpServletResponse response) {
        // Redis에서 Refresh Token 삭제
        authService.logout(user.getId());

        // 쿠키 삭제
        cookieUtils.deleteAllAuthCookies(response);

        return ApiResponse.onSuccess(null);
    }
}
