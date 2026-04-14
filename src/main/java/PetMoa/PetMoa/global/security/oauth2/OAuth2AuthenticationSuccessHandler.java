package PetMoa.PetMoa.global.security.oauth2;

import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.security.jwt.JwtTokenProvider;
import PetMoa.PetMoa.global.security.jwt.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 JWT 토큰을 발급하고 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // Refresh Token Redis 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        log.info("OAuth2 login success - userId: {}, name: {}", user.getId(), user.getName());

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
