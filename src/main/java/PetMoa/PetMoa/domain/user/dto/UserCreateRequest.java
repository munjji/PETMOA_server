package PetMoa.PetMoa.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCreateRequest {

    private final String name;
    private final String phoneNumber;
    private final String address;
    private final String email;
}
