package PetMoa.PetMoa.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("User 객체 생성 성공")
    void createUser() {
        // given
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";
        String address = "서울시 강남구";
        String email = "hong@example.com";

        // when
        User user = User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .address(address)
                .email(email)
                .build();

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getAddress()).isEqualTo(address);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(이름) 누락 시 예외 발생")
    void createUser_withoutName() {
        // given & when & then
        assertThatThrownBy(() -> User.builder()
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름");
    }

    @Test
    @DisplayName("필수 필드(전화번호) 누락 시 예외 발생")
    void createUser_withoutPhoneNumber() {
        // given & when & then
        assertThatThrownBy(() -> User.builder()
                .name("홍길동")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전화번호");
    }

    @Test
    @DisplayName("필수 필드(주소) 누락 시 예외 발생")
    void createUser_withoutAddress() {
        // given & when & then
        assertThatThrownBy(() -> User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .email("hong@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주소");
    }

    @Test
    @DisplayName("전화번호 형식 검증 - 유효하지 않은 형식")
    void validatePhoneNumber_invalid() {
        // given
        String invalidPhoneNumber = "123";

        // when & then
        assertThatThrownBy(() -> User.builder()
                .name("홍길동")
                .phoneNumber(invalidPhoneNumber)
                .address("서울시 강남구")
                .email("hong@example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전화번호 형식");
    }

    @Test
    @DisplayName("전화번호 형식 검증 - 유효한 형식")
    void validatePhoneNumber_valid() {
        // given & when
        User user1 = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        User user2 = User.builder()
                .name("김철수")
                .phoneNumber("01012345678")
                .address("서울시 강남구")
                .email("kim@example.com")
                .build();

        // then
        assertThat(user1.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(user2.getPhoneNumber()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("이메일 형식 검증 - 유효하지 않은 형식")
    void validateEmail_invalid() {
        // given
        String invalidEmail = "invalid-email";

        // when & then
        assertThatThrownBy(() -> User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email(invalidEmail)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이메일 형식");
    }

    @Test
    @DisplayName("이메일 선택 필드 - null 허용")
    void createUser_withoutEmail() {
        // given & when
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        // then
        assertThat(user.getEmail()).isNull();
    }

    @Test
    @DisplayName("반려동물 목록 초기화")
    void initializePets() {
        // given & when
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        // then
        assertThat(user.getPets()).isNotNull();
        assertThat(user.getPets()).isEmpty();
    }

    @Test
    @DisplayName("예약 목록 초기화")
    void initializeReservations() {
        // given & when
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        // then
        assertThat(user.getReservations()).isNotNull();
        assertThat(user.getReservations()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(user.getCreatedAt()).isBetween(before, after);
    }
}
