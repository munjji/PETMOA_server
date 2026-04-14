package PetMoa.PetMoa.domain.user.controller;

import PetMoa.PetMoa.domain.user.dto.UserResponse;
import PetMoa.PetMoa.domain.user.dto.UserUpdateRequest;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.service.UserCommandService;
import PetMoa.PetMoa.domain.user.service.UserQueryService;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import PetMoa.PetMoa.global.security.CurrentUser;
import PetMoa.PetMoa.global.security.jwt.JwtUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(@CurrentUser JwtUserPrincipal principal) {
        User user = userQueryService.getUserById(principal.getId());
        return ApiResponse.onSuccess(UserResponse.from(user));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMyInfo(
            @CurrentUser JwtUserPrincipal principal,
            @RequestBody UserUpdateRequest request) {
        User updatedUser = userCommandService.updateUser(principal.getId(), request);
        return ApiResponse.onSuccess(UserResponse.from(updatedUser));
    }
}
