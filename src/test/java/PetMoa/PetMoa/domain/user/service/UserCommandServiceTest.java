package PetMoa.PetMoa.domain.user.service;

import PetMoa.PetMoa.domain.user.dto.UserCreateRequest;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCommandService userCommandService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();
    }

    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("성공: 유효한 정보로 사용자 생성")
        void createUser_Success() {
            // given
            UserCreateRequest request = UserCreateRequest.builder()
                    .name("홍길동")
                    .phoneNumber("010-1234-5678")
                    .address("서울시 강남구")
                    .email("hong@example.com")
                    .build();

            given(userRepository.existsByPhoneNumber("010-1234-5678")).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(testUser);

            // when
            User result = userCommandService.createUser(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("홍길동");
            assertThat(result.getPhoneNumber()).isEqualTo("010-1234-5678");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 중복된 전화번호")
        void createUser_DuplicatePhoneNumber() {
            // given
            UserCreateRequest request = UserCreateRequest.builder()
                    .name("홍길동")
                    .phoneNumber("010-1234-5678")
                    .address("서울시 강남구")
                    .build();

            given(userRepository.existsByPhoneNumber("010-1234-5678")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userCommandService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 등록된 전화번호");
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("성공: 사용자 삭제")
        void deleteUser_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

            // when
            userCommandService.deleteUser(1L);

            // then
            verify(userRepository).delete(testUser);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자 삭제")
        void deleteUser_NotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCommandService.deleteUser(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }
}
