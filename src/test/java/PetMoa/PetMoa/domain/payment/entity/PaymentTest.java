package PetMoa.PetMoa.domain.payment.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment 엔티티 테스트")
class PaymentTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        Pet pet = Pet.builder()
                .name("먼지")
                .type(PetType.CAT)
                .size(PetSize.MEDIUM)
                .owner(user)
                .build();

        reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
    }

    @Test
    @DisplayName("Payment 객체 생성 성공")
    void createPayment() {
        // given
        String orderId = UUID.randomUUID().toString();
        Integer depositAmount = 10000;
        Integer taxiFare = 10500;
        PaymentMethod method = PaymentMethod.CARD;

        // when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(orderId)
                .depositAmount(depositAmount)
                .taxiFare(taxiFare)
                .method(method)
                .build();

        // then
        assertThat(payment).isNotNull();
        assertThat(payment.getReservation()).isEqualTo(reservation);
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getDepositAmount()).isEqualTo(depositAmount);
        assertThat(payment.getTaxiFare()).isEqualTo(taxiFare);
        assertThat(payment.getTotalAmount()).isEqualTo(20500);
        assertThat(payment.getMethod()).isEqualTo(method);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(예약) 누락 시 예외 발생")
    void createPayment_withoutReservation() {
        // given & when & then
        assertThatThrownBy(() -> Payment.builder()
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약");
    }

    @Test
    @DisplayName("필수 필드(주문 ID) 누락 시 예외 발생")
    void createPayment_withoutOrderId() {
        // given & when & then
        assertThatThrownBy(() -> Payment.builder()
                .reservation(reservation)
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID");
    }

    @Test
    @DisplayName("필수 필드(예약금) 누락 시 예외 발생")
    void createPayment_withoutDepositAmount() {
        // given & when & then
        assertThatThrownBy(() -> Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약금");
    }

    @Test
    @DisplayName("필수 필드(택시비) 누락 시 예외 발생")
    void createPayment_withoutTaxiFare() {
        // given & when & then
        assertThatThrownBy(() -> Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .method(PaymentMethod.CARD)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("택시비");
    }

    @Test
    @DisplayName("필수 필드(결제 수단) 누락 시 예외 발생")
    void createPayment_withoutMethod() {
        // given & when & then
        assertThatThrownBy(() -> Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 수단");
    }

    @Test
    @DisplayName("총 결제 금액 자동 계산")
    void calculateTotalAmount() {
        // given & when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(15000)
                .method(PaymentMethod.CARD)
                .build();

        // then
        assertThat(payment.getTotalAmount()).isEqualTo(25000);
    }

    @Test
    @DisplayName("상태 기본값 설정 - PENDING")
    void defaultStatus() {
        // given & when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제 승인")
    void approve() {
        // given
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();

        String paymentKey = "test_payment_key_123";

        // when
        payment.approve(paymentKey);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 취소")
    void cancel() {
        // given
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();
        payment.approve("test_payment_key");

        String cancelReason = "고객 요청";

        // when
        payment.cancel(cancelReason);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getRefundAmount()).isEqualTo(20500);
        assertThat(payment.getCancelReason()).isEqualTo(cancelReason);
        assertThat(payment.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("부분 취소")
    void partialCancel() {
        // given
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();
        payment.approve("test_payment_key");

        Integer partialRefundAmount = 10500;
        String cancelReason = "택시 예약만 취소";

        // when
        payment.partialCancel(partialRefundAmount, cancelReason);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
        assertThat(payment.getRefundAmount()).isEqualTo(partialRefundAmount);
        assertThat(payment.getCancelReason()).isEqualTo(cancelReason);
        assertThat(payment.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("PaymentStatus Enum 값 검증")
    void validatePaymentStatus() {
        // given & when & then
        assertThat(PaymentStatus.PENDING).isNotNull();
        assertThat(PaymentStatus.APPROVED).isNotNull();
        assertThat(PaymentStatus.CANCELLED).isNotNull();
        assertThat(PaymentStatus.PARTIAL_CANCELLED).isNotNull();
        assertThat(PaymentStatus.FAILED).isNotNull();
    }

    @Test
    @DisplayName("PaymentMethod Enum 값 검증")
    void validatePaymentMethod() {
        // given & when & then
        assertThat(PaymentMethod.CARD).isNotNull();
        assertThat(PaymentMethod.VIRTUAL_ACCOUNT).isNotNull();
        assertThat(PaymentMethod.TRANSFER).isNotNull();
        assertThat(PaymentMethod.MOBILE_PHONE).isNotNull();
        assertThat(PaymentMethod.TOSS_PAY).isNotNull();
        assertThat(PaymentMethod.KAKAO_PAY).isNotNull();
        assertThat(PaymentMethod.NAVER_PAY).isNotNull();
    }

    @Test
    @DisplayName("토스페이로 결제 성공")
    void payWithTossPay() {
        // given & when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.TOSS_PAY)
                .build();

        // then
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.TOSS_PAY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("카카오페이로 결제 성공")
    void payWithKakaoPay() {
        // given & when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.KAKAO_PAY)
                .build();

        // then
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.KAKAO_PAY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("네이버페이로 결제 성공")
    void payWithNaverPay() {
        // given & when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.NAVER_PAY)
                .build();

        // then
        assertThat(payment.getMethod()).isEqualTo(PaymentMethod.NAVER_PAY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(payment.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("승인된 결제인지 확인")
    void isApproved() {
        // given
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();

        // when & then
        assertThat(payment.isApproved()).isFalse();

        payment.approve("test_payment_key");
        assertThat(payment.isApproved()).isTrue();
    }

    @Test
    @DisplayName("환불 가능 여부 확인")
    void canRefund() {
        // given
        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(UUID.randomUUID().toString())
                .depositAmount(10000)
                .taxiFare(10500)
                .method(PaymentMethod.CARD)
                .build();

        // when & then
        assertThat(payment.canRefund()).isFalse();

        payment.approve("test_payment_key");
        assertThat(payment.canRefund()).isTrue();

        payment.cancel("고객 요청");
        assertThat(payment.canRefund()).isFalse();
    }
}
