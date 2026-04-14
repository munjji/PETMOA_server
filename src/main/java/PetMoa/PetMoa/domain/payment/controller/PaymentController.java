package PetMoa.PetMoa.domain.payment.controller;

import PetMoa.PetMoa.domain.payment.dto.PaymentConfirmRequest;
import PetMoa.PetMoa.domain.payment.dto.PaymentCreateRequest;
import PetMoa.PetMoa.domain.payment.dto.PaymentResponse;
import PetMoa.PetMoa.domain.payment.dto.RefundRequest;
import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.service.PaymentQueryService;
import PetMoa.PetMoa.domain.payment.service.PaymentService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import PetMoa.PetMoa.global.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentQueryService paymentQueryService;

    @Operation(summary = "결제 요청 생성", description = "예약에 대한 결제 요청을 생성하고 orderId를 발급합니다.")
    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(
            @CurrentUser User user,
            @Valid @RequestBody PaymentCreateRequest request) {
        Payment payment = paymentService.createPayment(user.getId(), request);
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }

    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 처리합니다. 클라이언트에서 결제 완료 후 호출합니다.")
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponse> confirmPayment(
            @CurrentUser User user,
            @Valid @RequestBody PaymentConfirmRequest request) {
        Payment payment = paymentService.confirmPayment(user.getId(), request);
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }

    @Operation(summary = "결제 조회", description = "결제 상세 정보를 조회합니다.")
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(
            @CurrentUser User user,
            @PathVariable Long paymentId) {
        Payment payment = paymentQueryService.getPaymentById(paymentId, user.getId());
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }

    @Operation(summary = "주문 ID로 결제 조회", description = "주문 ID로 결제 정보를 조회합니다.")
    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentByOrderId(
            @CurrentUser User user,
            @PathVariable String orderId) {
        Payment payment = paymentQueryService.getPaymentByOrderId(orderId, user.getId());
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }

    @Operation(summary = "결제 환불", description = "결제를 환불합니다. 환불 정책이 적용됩니다. (24시간 전: 100%, 12시간 전: 50%, 당일: 0%)")
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refundPayment(
            @CurrentUser User user,
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundRequest request) {
        Payment payment = paymentService.refundPayment(user.getId(), paymentId, request.cancelReason());
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }

    @Operation(summary = "예약 ID로 결제 조회", description = "예약 ID로 결제 정보를 조회합니다.")
    @GetMapping("/reservations/{reservationId}")
    public ApiResponse<PaymentResponse> getPaymentByReservationId(
            @CurrentUser User user,
            @PathVariable Long reservationId) {
        Payment payment = paymentQueryService.getPaymentByReservationId(reservationId, user.getId());
        return ApiResponse.onSuccess(PaymentResponse.from(payment));
    }
}
