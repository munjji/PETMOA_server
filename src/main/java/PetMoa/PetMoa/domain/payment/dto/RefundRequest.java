package PetMoa.PetMoa.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "취소 사유는 필수입니다.")
        String cancelReason
) {
}
