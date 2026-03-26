package PetMoa.PetMoa.domain.payment.entity;

/**
 * 결제 수단
 */
public enum PaymentMethod {
    CARD,               // 카드
    VIRTUAL_ACCOUNT,    // 가상계좌
    TRANSFER,           // 계좌이체
    MOBILE_PHONE,       // 휴대폰
    TOSS_PAY,           // 토스페이
    KAKAO_PAY,          // 카카오페이
    NAVER_PAY           // 네이버페이
}
