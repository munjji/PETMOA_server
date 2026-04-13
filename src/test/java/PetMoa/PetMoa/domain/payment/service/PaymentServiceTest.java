package PetMoa.PetMoa.domain.payment.service;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.payment.dto.PaymentConfirmRequest;
import PetMoa.PetMoa.domain.payment.dto.PaymentCreateRequest;
import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.entity.PaymentMethod;
import PetMoa.PetMoa.domain.payment.entity.PaymentStatus;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.entity.VisitType;
import PetMoa.PetMoa.domain.reservation.service.ReservationQueryService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.client.toss.TossPaymentsClient;
import PetMoa.PetMoa.global.client.toss.dto.TossPaymentResponse;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import PetMoa.PetMoa.global.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentQueryService paymentQueryService;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private TossPaymentsClient tossPaymentsClient;

    @Mock
    private Clock clock;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private Pet testPet;
    private Reservation testReservation;
    private Payment testPayment;
    private Hospital testHospital;
    private Veterinarian testVet;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testPet = Pet.builder()
                .name("멍멍이")
                .type(PetType.DOG)
                .size(PetSize.SMALL)
                .owner(testUser)
                .build();

        testReservation = Reservation.builder()
                .user(testUser)
                .pet(testPet)
                .build();
        ReflectionTestUtils.setField(testReservation, "id", 1L);

        testHospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .build();

        testVet = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.GENERAL)
                .hospital(testHospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        // 24시간 이후 예약으로 설정
        testTimeSlot = TimeSlot.builder()
                .veterinarian(testVet)
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(3)
                .build();

        testPayment = Payment.builder()
                .reservation(testReservation)
                .orderId("PETMOA_TEST123456789")
                .depositAmount(10000)
                .taxiFare(5000)
                .method(PaymentMethod.CARD)
                .build();
        ReflectionTestUtils.setField(testPayment, "id", 1L);

        // @Value 필드 설정
        ReflectionTestUtils.setField(paymentService, "depositAmount", 10000);
    }

    @Nested
    @DisplayName("결제 생성")
    class CreatePayment {

        @Test
        @DisplayName("성공: 유효한 예약으로 결제 생성")
        void createPayment_Success() {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(reservationQueryService.getReservationById(1L)).willReturn(testReservation);
            given(paymentRepository.findByReservationId(1L)).willReturn(null);
            given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                ReflectionTestUtils.setField(payment, "id", 1L);
                return payment;
            });

            // when
            Payment result = paymentService.createPayment(1L, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getReservation()).isEqualTo(testReservation);
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
            assertThat(result.getDepositAmount()).isEqualTo(10000);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패: 이미 결제가 존재하는 경우")
        void createPayment_AlreadyExists() {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(reservationQueryService.getReservationById(1L)).willReturn(testReservation);
            given(paymentRepository.findByReservationId(1L)).willReturn(testPayment);

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(1L, request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("이미 결제가 존재합니다");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("실패: 다른 사용자의 예약에 대해 결제 생성 시도")
        void createPayment_NotOwner() {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(reservationQueryService.getReservationById(1L)).willReturn(testReservation);

            // when & then
            assertThatThrownBy(() -> paymentService.createPayment(999L, request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("소유자가 아닙니다");
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class ConfirmPayment {

        @Test
        @DisplayName("성공: 결제 승인")
        void confirmPayment_Success() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    15000
            );

            TossPaymentResponse tossResponse = new TossPaymentResponse(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    "예약금 + 택시비",
                    "DONE",
                    15000,
                    15000,
                    "카드",
                    null,
                    null,
                    null
            );

            given(paymentQueryService.getPaymentByOrderIdInternal("PETMOA_TEST123456789")).willReturn(testPayment);
            given(tossPaymentsClient.confirmPayment(anyString(), anyString(), anyInt())).willReturn(tossResponse);

            // when
            Payment result = paymentService.confirmPayment(1L, request);

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(result.getPaymentKey()).isEqualTo("test_payment_key_123");
            verify(tossPaymentsClient).confirmPayment("test_payment_key_123", "PETMOA_TEST123456789", 15000);
        }

        @Test
        @DisplayName("실패: 금액 불일치")
        void confirmPayment_AmountMismatch() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    20000  // 실제 금액은 15000
            );

            given(paymentQueryService.getPaymentByOrderIdInternal("PETMOA_TEST123456789")).willReturn(testPayment);

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(1L, request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("결제 금액이 일치하지 않습니다");

            verify(tossPaymentsClient, never()).confirmPayment(anyString(), anyString(), anyInt());
        }

        @Test
        @DisplayName("실패: 토스페이먼츠 승인 실패")
        void confirmPayment_TossNotApproved() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    15000
            );

            TossPaymentResponse tossResponse = new TossPaymentResponse(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    "예약금 + 택시비",
                    "FAILED",  // 승인 실패
                    15000,
                    15000,
                    "카드",
                    null,
                    null,
                    null
            );

            given(paymentQueryService.getPaymentByOrderIdInternal("PETMOA_TEST123456789")).willReturn(testPayment);
            given(tossPaymentsClient.confirmPayment(anyString(), anyString(), anyInt())).willReturn(tossResponse);

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(1L, request))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("결제가 승인되지 않았습니다");

            assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("실패: 다른 사용자의 결제 승인 시도")
        void confirmPayment_NotOwner() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    15000
            );

            given(paymentQueryService.getPaymentByOrderIdInternal("PETMOA_TEST123456789")).willReturn(testPayment);

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(999L, request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("소유자가 아닙니다");

            verify(tossPaymentsClient, never()).confirmPayment(anyString(), anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("결제 환불")
    class RefundPayment {

        @BeforeEach
        void setUpForRefund() {
            // 환불 테스트를 위해 결제 승인 상태로 변경
            testPayment.approve("test_payment_key_123");

            // HospitalReservation 설정
            HospitalReservation hospitalReservation = HospitalReservation.builder()
                    .reservation(testReservation)
                    .veterinarian(testVet)
                    .timeSlot(testTimeSlot)
                    .visitType(VisitType.FIRST_VISIT)
                    .symptoms("테스트 증상")
                    .depositAmount(10000)
                    .build();
            ReflectionTestUtils.setField(testReservation, "hospitalReservation", hospitalReservation);
        }

        @Test
        @DisplayName("성공: 24시간 전 전액 환불")
        void refundPayment_FullRefund() {
            // given: 예약 시간 24시간 전으로 현재 시간 설정
            LocalDateTime reservationTime = testTimeSlot.getDate().atTime(testTimeSlot.getStartTime());
            LocalDateTime now = reservationTime.minusHours(25);
            setupClock(now);

            given(paymentQueryService.getPaymentByIdInternal(1L)).willReturn(testPayment);
            given(tossPaymentsClient.cancelPayment(anyString(), anyString()))
                    .willReturn(createCancelResponse());

            // when
            Payment result = paymentService.refundPayment(1L, 1L, "고객 요청");

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(result.getRefundAmount()).isEqualTo(15000); // 전액 환불
            verify(tossPaymentsClient).cancelPayment("test_payment_key_123", "고객 요청");
        }

        @Test
        @DisplayName("실패: 환불할 수 없는 상태")
        void refundPayment_NotRefundable() {
            // given
            Payment pendingPayment = Payment.builder()
                    .reservation(testReservation)
                    .orderId("PETMOA_TEST123456789")
                    .depositAmount(10000)
                    .taxiFare(5000)
                    .method(PaymentMethod.CARD)
                    .build();
            ReflectionTestUtils.setField(pendingPayment, "id", 2L);

            given(paymentQueryService.getPaymentByIdInternal(2L)).willReturn(pendingPayment);

            // when & then
            assertThatThrownBy(() -> paymentService.refundPayment(1L, 2L, "고객 요청"))
                    .isInstanceOf(PaymentException.class)
                    .hasMessageContaining("환불할 수 없는 상태");

            verify(tossPaymentsClient, never()).cancelPayment(anyString(), anyString());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 결제 환불 시도")
        void refundPayment_NotOwner() {
            // given
            given(paymentQueryService.getPaymentByIdInternal(1L)).willReturn(testPayment);

            // when & then
            assertThatThrownBy(() -> paymentService.refundPayment(999L, 1L, "고객 요청"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("소유자가 아닙니다");
        }

        @Test
        @DisplayName("성공: 12시간 전 50% 부분 환불")
        void refundPayment_PartialRefund_12HoursBefore() {
            // given: 예약 시간 12시간 전으로 현재 시간 설정
            LocalDateTime reservationTime = testTimeSlot.getDate().atTime(testTimeSlot.getStartTime());
            LocalDateTime now = reservationTime.minusHours(12);
            setupClock(now);

            given(paymentQueryService.getPaymentByIdInternal(1L)).willReturn(testPayment);
            given(tossPaymentsClient.cancelPaymentPartially(anyString(), anyString(), anyInt()))
                    .willReturn(createPartialCancelResponse(7500));

            // when
            Payment result = paymentService.refundPayment(1L, 1L, "고객 요청");

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCELLED);
            assertThat(result.getRefundAmount()).isEqualTo(7500); // 15000 * 50%
            verify(tossPaymentsClient).cancelPaymentPartially("test_payment_key_123", "고객 요청", 7500);
        }

        @Test
        @DisplayName("성공: 당일 취소 - 환불 불가")
        void refundPayment_NoRefund_SameDay() {
            // given: 예약 시간 6시간 전으로 현재 시간 설정 (당일)
            LocalDateTime reservationTime = testTimeSlot.getDate().atTime(testTimeSlot.getStartTime());
            LocalDateTime now = reservationTime.minusHours(6);
            setupClock(now);

            given(paymentQueryService.getPaymentByIdInternal(1L)).willReturn(testPayment);

            // when
            Payment result = paymentService.refundPayment(1L, 1L, "고객 요청");

            // then
            assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(result.getRefundAmount()).isEqualTo(0);
            verify(tossPaymentsClient, never()).cancelPayment(anyString(), anyString());
            verify(tossPaymentsClient, never()).cancelPaymentPartially(anyString(), anyString(), anyInt());
        }

        private void setupClock(LocalDateTime dateTime) {
            Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
            given(clock.instant()).willReturn(instant);
            given(clock.getZone()).willReturn(ZoneId.systemDefault());
        }

        private TossPaymentResponse createCancelResponse() {
            return new TossPaymentResponse(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    "예약금 + 택시비",
                    "CANCELED",
                    15000,
                    0,
                    "카드",
                    null,
                    null,
                    new TossPaymentResponse.Cancels[]{
                            new TossPaymentResponse.Cancels(15000, "고객 요청", null, "txn_key")
                    }
            );
        }

        private TossPaymentResponse createPartialCancelResponse(int refundAmount) {
            return new TossPaymentResponse(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    "예약금 + 택시비",
                    "PARTIAL_CANCELED",
                    15000,
                    15000 - refundAmount,
                    "카드",
                    null,
                    null,
                    new TossPaymentResponse.Cancels[]{
                            new TossPaymentResponse.Cancels(refundAmount, "고객 요청", null, "txn_key")
                    }
            );
        }
    }
}
