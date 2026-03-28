package PetMoa.PetMoa.domain.user.dto;

public record UserUpdateRequest(
        String phoneNumber,
        String address
) {
}
