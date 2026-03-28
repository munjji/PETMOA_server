package PetMoa.PetMoa.domain.user.dto;

import PetMoa.PetMoa.domain.user.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String address
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }
}
