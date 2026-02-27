package PetMoa.PetMoa.global.apiPayload.code.status;

import PetMoa.PetMoa.global.apiPayload.code.BaseErrorCode;
import PetMoa.PetMoa.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 회원 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4001", "사용자가 없습니다."),

    // 병원 관련 에러
    HOSPITAL_NOT_FOUND(HttpStatus.NOT_FOUND, "HOSPITAL4001", "병원을 찾을 수 없습니다."),

    // 예약 관련 에러
    APPOINTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "APPOINTMENT4001", "예약을 찾을 수 없습니다."),
    APPOINTMENT_TIME_CONFLICT(HttpStatus.CONFLICT, "APPOINTMENT4091", "이미 예약된 시간입니다."),

    // 결제 관련 에러
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT4001", "결제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
