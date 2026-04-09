package PetMoa.PetMoa.global.client.toss.dto;

public record TossPaymentCancelRequest(
        String cancelReason,
        Integer cancelAmount
) {
    public static TossPaymentCancelRequest fullCancel(String cancelReason) {
        return new TossPaymentCancelRequest(cancelReason, null);
    }

    public static TossPaymentCancelRequest partialCancel(String cancelReason, Integer cancelAmount) {
        return new TossPaymentCancelRequest(cancelReason, cancelAmount);
    }
}
