package PetMoa.PetMoa.global.exception;

/**
 * 인증 실패 예외 (401)
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
