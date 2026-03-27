package PetMoa.PetMoa.domain.user.dto;

public record UserCreateRequest(
        String name,
        String phoneNumber,
        String address,
        String email
) {
}
