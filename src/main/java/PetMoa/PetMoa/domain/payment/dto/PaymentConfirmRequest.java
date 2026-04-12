package PetMoa.PetMoa.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
        @NotBlank(message = "결제키는 필수입니다.")
        String paymentKey,

        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId,

        @NotNull(message = "결제 금액은 필수입니다.")
        Integer amount
) {
}
