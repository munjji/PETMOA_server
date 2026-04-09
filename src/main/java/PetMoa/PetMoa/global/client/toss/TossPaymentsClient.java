package PetMoa.PetMoa.global.client.toss;

import PetMoa.PetMoa.global.client.toss.dto.TossPaymentCancelRequest;
import PetMoa.PetMoa.global.client.toss.dto.TossPaymentConfirmRequest;
import PetMoa.PetMoa.global.client.toss.dto.TossPaymentResponse;
import PetMoa.PetMoa.global.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final RestClient tossPaymentsRestClient;

    /**
     * 결제 승인
     * POST /v1/payments/confirm
     */
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, Integer amount) {
        log.info("토스페이먼츠 결제 승인 요청 - orderId: {}, amount: {}", orderId, amount);

        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.of(paymentKey, orderId, amount);

        try {
            TossPaymentResponse response = tossPaymentsRestClient.post()
                    .uri("/v1/payments/confirm")
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

            log.info("토스페이먼츠 결제 승인 성공 - paymentKey: {}, status: {}",
                    response.paymentKey(), response.status());

            return response;
        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 승인 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("PAYMENT_CONFIRM_FAILED", "결제 승인에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 결제 취소 (전액)
     * POST /v1/payments/{paymentKey}/cancel
     */
    public TossPaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        log.info("토스페이먼츠 결제 취소 요청 - paymentKey: {}", paymentKey);

        TossPaymentCancelRequest request = TossPaymentCancelRequest.fullCancel(cancelReason);

        return executeCancelRequest(paymentKey, request);
    }

    /**
     * 결제 부분 취소
     * POST /v1/payments/{paymentKey}/cancel
     */
    public TossPaymentResponse cancelPaymentPartially(String paymentKey, String cancelReason, Integer cancelAmount) {
        log.info("토스페이먼츠 부분 취소 요청 - paymentKey: {}, cancelAmount: {}", paymentKey, cancelAmount);

        TossPaymentCancelRequest request = TossPaymentCancelRequest.partialCancel(cancelReason, cancelAmount);

        return executeCancelRequest(paymentKey, request);
    }

    /**
     * 결제 조회
     * GET /v1/payments/{paymentKey}
     */
    public TossPaymentResponse getPayment(String paymentKey) {
        log.info("토스페이먼츠 결제 조회 - paymentKey: {}", paymentKey);

        try {
            return tossPaymentsRestClient.get()
                    .uri("/v1/payments/{paymentKey}", paymentKey)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 조회 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("PAYMENT_QUERY_FAILED", "결제 조회에 실패했습니다: " + e.getMessage());
        }
    }

    private TossPaymentResponse executeCancelRequest(String paymentKey, TossPaymentCancelRequest request) {
        try {
            TossPaymentResponse response = tossPaymentsRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

            log.info("토스페이먼츠 결제 취소 성공 - paymentKey: {}, status: {}",
                    response.paymentKey(), response.status());

            return response;
        } catch (HttpClientErrorException e) {
            log.error("토스페이먼츠 결제 취소 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentException("PAYMENT_CANCEL_FAILED", "결제 취소에 실패했습니다: " + e.getMessage());
        }
    }
}
