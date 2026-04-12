package PetMoa.PetMoa.domain.payment.controller;

import PetMoa.PetMoa.domain.payment.dto.PaymentConfirmRequest;
import PetMoa.PetMoa.domain.payment.dto.PaymentCreateRequest;
import PetMoa.PetMoa.domain.payment.dto.RefundRequest;
import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.entity.PaymentMethod;
import PetMoa.PetMoa.domain.payment.entity.PaymentStatus;
import PetMoa.PetMoa.domain.payment.service.PaymentQueryService;
import PetMoa.PetMoa.domain.payment.service.PaymentService;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import PetMoa.PetMoa.global.exception.PaymentException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentQueryService paymentQueryService;

    @InjectMocks
    private PaymentController paymentController;

    private User testUser;
    private Pet testPet;
    private Reservation testReservation;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
        objectMapper = new ObjectMapper();

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

        testPayment = Payment.builder()
                .reservation(testReservation)
                .orderId("PETMOA_TEST123456789")
                .depositAmount(10000)
                .taxiFare(5000)
                .method(PaymentMethod.CARD)
                .build();
        ReflectionTestUtils.setField(testPayment, "id", 1L);
    }

    @Nested
    @DisplayName("POST /api/v1/payments")
    class CreatePayment {

        @Test
        @DisplayName("성공: 결제 요청 생성")
        void success() throws Exception {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(paymentService.createPayment(eq(1L), any(PaymentCreateRequest.class)))
                    .willReturn(testPayment);

            // when & then
            mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.orderId").value("PETMOA_TEST123456789"))
                    .andExpect(jsonPath("$.result.totalAmount").value(15000))
                    .andExpect(jsonPath("$.result.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패: 이미 결제 존재")
        void failAlreadyExists() throws Exception {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(paymentService.createPayment(eq(1L), any(PaymentCreateRequest.class)))
                    .willThrow(new PaymentException("PAYMENT_ALREADY_EXISTS", "이미 결제가 존재합니다."));

            // when & then
            mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 예약")
        void failNotOwner() throws Exception {
            // given
            PaymentCreateRequest request = new PaymentCreateRequest(1L, PaymentMethod.CARD);

            given(paymentService.createPayment(eq(999L), any(PaymentCreateRequest.class)))
                    .willThrow(new ForbiddenException("해당 예약의 소유자가 아닙니다."));

            // when & then
            mockMvc.perform(post("/api/v1/payments")
                            .header("X-User-Id", "999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/confirm")
    class ConfirmPayment {

        @Test
        @DisplayName("성공: 결제 승인")
        void success() throws Exception {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    15000
            );

            Payment approvedPayment = Payment.builder()
                    .reservation(testReservation)
                    .orderId("PETMOA_TEST123456789")
                    .depositAmount(10000)
                    .taxiFare(5000)
                    .method(PaymentMethod.CARD)
                    .build();
            approvedPayment.approve("test_payment_key_123");
            ReflectionTestUtils.setField(approvedPayment, "id", 1L);

            given(paymentService.confirmPayment(any(PaymentConfirmRequest.class)))
                    .willReturn(approvedPayment);

            // when & then
            mockMvc.perform(post("/api/v1/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.status").value("APPROVED"))
                    .andExpect(jsonPath("$.result.paymentKey").value("test_payment_key_123"));
        }

        @Test
        @DisplayName("실패: 금액 불일치")
        void failAmountMismatch() throws Exception {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                    "test_payment_key_123",
                    "PETMOA_TEST123456789",
                    20000
            );

            given(paymentService.confirmPayment(any(PaymentConfirmRequest.class)))
                    .willThrow(new PaymentException("AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다."));

            // when & then
            mockMvc.perform(post("/api/v1/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/{paymentId}")
    class GetPayment {

        @Test
        @DisplayName("성공: 결제 조회")
        void success() throws Exception {
            // given
            given(paymentQueryService.getPaymentById(1L)).willReturn(testPayment);

            // when & then
            mockMvc.perform(get("/api/v1/payments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.orderId").value("PETMOA_TEST123456789"))
                    .andExpect(jsonPath("$.result.totalAmount").value(15000));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 결제")
        void failNotFound() throws Exception {
            // given
            given(paymentQueryService.getPaymentById(999L))
                    .willThrow(new EntityNotFoundException("결제를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/payments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/orders/{orderId}")
    class GetPaymentByOrderId {

        @Test
        @DisplayName("성공: 주문 ID로 결제 조회")
        void success() throws Exception {
            // given
            given(paymentQueryService.getPaymentByOrderId("PETMOA_TEST123456789"))
                    .willReturn(testPayment);

            // when & then
            mockMvc.perform(get("/api/v1/payments/orders/PETMOA_TEST123456789"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.orderId").value("PETMOA_TEST123456789"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/{paymentId}/refund")
    class RefundPayment {

        @Test
        @DisplayName("성공: 결제 환불")
        void success() throws Exception {
            // given
            RefundRequest request = new RefundRequest("고객 요청으로 취소");

            Payment cancelledPayment = Payment.builder()
                    .reservation(testReservation)
                    .orderId("PETMOA_TEST123456789")
                    .depositAmount(10000)
                    .taxiFare(5000)
                    .method(PaymentMethod.CARD)
                    .build();
            cancelledPayment.approve("test_payment_key_123");
            cancelledPayment.cancel("고객 요청으로 취소");
            ReflectionTestUtils.setField(cancelledPayment, "id", 1L);

            given(paymentService.refundPayment(eq(1L), eq(1L), anyString()))
                    .willReturn(cancelledPayment);

            // when & then
            mockMvc.perform(post("/api/v1/payments/1/refund")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("실패: 환불 불가 상태")
        void failNotRefundable() throws Exception {
            // given
            RefundRequest request = new RefundRequest("고객 요청으로 취소");

            given(paymentService.refundPayment(eq(1L), eq(1L), anyString()))
                    .willThrow(new PaymentException("REFUND_NOT_ALLOWED", "환불할 수 없는 상태입니다."));

            // when & then
            mockMvc.perform(post("/api/v1/payments/1/refund")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 결제 환불 시도")
        void failNotOwner() throws Exception {
            // given
            RefundRequest request = new RefundRequest("고객 요청으로 취소");

            given(paymentService.refundPayment(eq(999L), eq(1L), anyString()))
                    .willThrow(new ForbiddenException("해당 예약의 소유자가 아닙니다."));

            // when & then
            mockMvc.perform(post("/api/v1/payments/1/refund")
                            .header("X-User-Id", "999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/payments/reservations/{reservationId}")
    class GetPaymentByReservationId {

        @Test
        @DisplayName("성공: 예약 ID로 결제 조회")
        void success() throws Exception {
            // given
            given(paymentQueryService.getPaymentByReservationId(1L)).willReturn(testPayment);

            // when & then
            mockMvc.perform(get("/api/v1/payments/reservations/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.orderId").value("PETMOA_TEST123456789"));
        }
    }
}
