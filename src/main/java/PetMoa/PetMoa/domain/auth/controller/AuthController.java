package PetMoa.PetMoa.domain.auth.controller;

import PetMoa.PetMoa.domain.auth.dto.TokenRefreshRequest;
import PetMoa.PetMoa.domain.auth.service.AuthService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import PetMoa.PetMoa.global.security.CurrentUser;
import PetMoa.PetMoa.global.security.jwt.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인 URL", description = "카카오 OAuth2 로그인 URL을 반환합니다.")
    @GetMapping("/kakao/login-url")
    public ApiResponse<String> getKakaoLoginUrl() {
        return ApiResponse.onSuccess("/oauth2/authorization/kakao");
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request.refreshToken());
        return ApiResponse.onSuccess(tokenResponse);
    }

    @Operation(summary = "로그아웃", description = "현재 사용자의 Refresh Token을 삭제합니다.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CurrentUser User user) {
        authService.logout(user.getId());
        return ApiResponse.onSuccess(null);
    }
}
