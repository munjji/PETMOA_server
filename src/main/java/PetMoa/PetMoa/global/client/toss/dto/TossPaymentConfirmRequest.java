package PetMoa.PetMoa.global.client.toss.dto;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {
    public static TossPaymentConfirmRequest of(String paymentKey, String orderId, Integer amount) {
        return new TossPaymentConfirmRequest(paymentKey, orderId, amount);
    }
}
