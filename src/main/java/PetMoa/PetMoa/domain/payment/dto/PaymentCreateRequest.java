package PetMoa.PetMoa.domain.payment.dto;

import PetMoa.PetMoa.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
        @NotNull(message = "예약 ID는 필수입니다.")
        Long reservationId,

        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentMethod method
) {
}
