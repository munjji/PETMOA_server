package PetMoa.PetMoa.global.security.oauth2;

import PetMoa.PetMoa.domain.user.entity.AuthProvider;
import PetMoa.PetMoa.domain.user.entity.Role;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 로그인 시 사용자 정보를 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("kakao".equals(registrationId)) {
            return processKakaoUser(oAuth2User);
        }

        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 사용자 정보 추출
        String providerId = String.valueOf(attributes.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String nickname = (String) profile.get("nickname");
        String email = (String) kakaoAccount.get("email");

        log.debug("Kakao user info - providerId: {}, nickname: {}, email: {}",
                providerId, nickname, email);

        // 기존 사용자 조회 또는 신규 생성
        User user = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, providerId)
                .orElseGet(() -> createNewUser(providerId, nickname, email));

        // 기존 사용자라면 프로필 업데이트
        if (user.getId() != null) {
            user.updateSocialProfile(nickname, email);
        }

        return new CustomOAuth2User(user, attributes);
    }

    private User createNewUser(String providerId, String nickname, String email) {
        log.info("Creating new user - providerId: {}, nickname: {}", providerId, nickname);

        User newUser = User.builder()
                .name(nickname)
                .email(email)
                .provider(AuthProvider.KAKAO)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userRepository.save(newUser);
    }
}