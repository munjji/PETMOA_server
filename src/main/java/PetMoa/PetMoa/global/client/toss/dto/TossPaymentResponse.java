package PetMoa.PetMoa.global.client.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String orderName,
        String status,
        Integer totalAmount,
        Integer balanceAmount,
        String method,
        OffsetDateTime approvedAt,
        OffsetDateTime requestedAt,
        Cancels[] cancels
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancels(
            Integer cancelAmount,
            String cancelReason,
            OffsetDateTime canceledAt,
            String transactionKey
    ) {}

    public boolean isApproved() {
        return "DONE".equals(status);
    }

    public boolean isCanceled() {
        return "CANCELED".equals(status);
    }

    public boolean isPartialCanceled() {
        return "PARTIAL_CANCELED".equals(status);
    }
}
