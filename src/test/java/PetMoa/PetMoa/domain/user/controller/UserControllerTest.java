package PetMoa.PetMoa.domain.user.controller;

import PetMoa.PetMoa.domain.user.dto.UserUpdateRequest;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.service.UserCommandService;
import PetMoa.PetMoa.domain.user.service.UserQueryService;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
import PetMoa.PetMoa.global.security.MockJwtUserArgumentResolver;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserCommandService userCommandService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new MockJwtUserArgumentResolver())
                .setControllerAdvice(new ExceptionAdvice())
                .build();
        objectMapper = JsonMapper.builder().build();
    }

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetMyInfo {

        @Test
        @DisplayName("성공: 내 정보 조회")
        void success() throws Exception {
            // given
            User user = User.builder()
                    .name("홍길동")
                    .phoneNumber("010-1234-5678")
                    .address("서울시 강남구")
                    .email("hong@example.com")
                    .build();

            given(userQueryService.getUserById(1L)).willReturn(user);

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.name").value("홍길동"))
                    .andExpect(jsonPath("$.result.phoneNumber").value("010-1234-5678"))
                    .andExpect(jsonPath("$.result.address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.result.email").value("hong@example.com"));
        }

        @Test
        @DisplayName("실패: X-User-Id 헤더 없음")
        void failWithoutHeader() throws Exception {
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void failUserNotFound() throws Exception {
            // given
            given(userQueryService.getUserById(999L))
                    .willThrow(new EntityNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/users/me")
                            .header("X-User-Id", "999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/me")
    class UpdateMyInfo {

        @Test
        @DisplayName("성공: 내 정보 수정")
        void success() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest("010-9999-8888", "서울시 서초구");

            User updatedUser = User.builder()
                    .name("홍길동")
                    .phoneNumber("010-9999-8888")
                    .address("서울시 서초구")
                    .email("hong@example.com")
                    .build();

            given(userCommandService.updateUser(eq(1L), any(UserUpdateRequest.class)))
                    .willReturn(updatedUser);

            // when & then
            mockMvc.perform(patch("/api/v1/users/me")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.phoneNumber").value("010-9999-8888"))
                    .andExpect(jsonPath("$.result.address").value("서울시 서초구"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void failUserNotFound() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest("010-9999-8888", "서울시 서초구");

            given(userCommandService.updateUser(eq(999L), any(UserUpdateRequest.class)))
                    .willThrow(new EntityNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(patch("/api/v1/users/me")
                            .header("X-User-Id", "999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
