package PetMoa.PetMoa.domain.pet.controller;

import PetMoa.PetMoa.domain.pet.dto.PetCreateRequest;
import PetMoa.PetMoa.domain.pet.dto.PetUpdateRequest;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.pet.service.PetCommandService;
import PetMoa.PetMoa.domain.pet.service.PetQueryService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
import PetMoa.PetMoa.global.exception.ForbiddenException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PetQueryService petQueryService;

    @Mock
    private PetCommandService petCommandService;

    @InjectMocks
    private PetController petController;

    private User testUser;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petController)
                .setCustomArgumentResolvers(new MockJwtUserArgumentResolver())
                .setControllerAdvice(new ExceptionAdvice())
                .build();
        objectMapper = JsonMapper.builder().build();

        testUser = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        testPet = Pet.builder()
                .name("뽀삐")
                .type(PetType.DOG)
                .size(PetSize.SMALL)
                .breed("말티즈")
                .age(3)
                .weight(4.5)
                .owner(testUser)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/pets")
    class GetMyPets {

        @Test
        @DisplayName("성공: 내 반려동물 목록 조회")
        void success() throws Exception {
            // given
            given(petQueryService.getPetsByOwnerId(1L)).willReturn(List.of(testPet));

            // when & then
            mockMvc.perform(get("/api/v1/pets")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.pets").isArray())
                    .andExpect(jsonPath("$.result.pets[0].name").value("뽀삐"))
                    .andExpect(jsonPath("$.result.pets[0].type").value("DOG"))
                    .andExpect(jsonPath("$.result.pets[0].size").value("SMALL"))
                    .andExpect(jsonPath("$.result.pets[0].breed").value("말티즈"))
                    .andExpect(jsonPath("$.result.pets[0].age").value(3))
                    .andExpect(jsonPath("$.result.pets[0].weight").value(4.5));
        }

        @Test
        @DisplayName("성공: 반려동물 없는 경우 빈 배열")
        void successEmpty() throws Exception {
            // given
            given(petQueryService.getPetsByOwnerId(1L)).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/v1/pets")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.pets").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/pets")
    class CreatePet {

        @Test
        @DisplayName("성공: 반려동물 등록")
        void success() throws Exception {
            // given
            PetCreateRequest request = new PetCreateRequest(
                    "뽀삐", PetType.DOG, PetSize.SMALL, 3, 4.5, "말티즈"
            );

            given(petCommandService.createPet(eq(1L), any(PetCreateRequest.class)))
                    .willReturn(testPet);

            // when & then
            mockMvc.perform(post("/api/v1/pets")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.name").value("뽀삐"))
                    .andExpect(jsonPath("$.result.type").value("DOG"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자")
        void failUserNotFound() throws Exception {
            // given
            PetCreateRequest request = new PetCreateRequest(
                    "뽀삐", PetType.DOG, PetSize.SMALL, 3, 4.5, "말티즈"
            );

            given(petCommandService.createPet(eq(999L), any(PetCreateRequest.class)))
                    .willThrow(new EntityNotFoundException("사용자를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/v1/pets")
                            .header("X-User-Id", "999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/pets/{petId}")
    class UpdatePet {

        @Test
        @DisplayName("성공: 반려동물 정보 수정")
        void success() throws Exception {
            // given
            PetUpdateRequest request = new PetUpdateRequest(
                    "뽀삐2", PetSize.MEDIUM, 4, 5.0, "말티즈"
            );

            Pet updatedPet = Pet.builder()
                    .name("뽀삐2")
                    .type(PetType.DOG)
                    .size(PetSize.MEDIUM)
                    .breed("말티즈")
                    .age(4)
                    .weight(5.0)
                    .owner(testUser)
                    .build();

            given(petCommandService.updatePet(eq(1L), eq(1L), any(PetUpdateRequest.class)))
                    .willReturn(updatedPet);

            // when & then
            mockMvc.perform(patch("/api/v1/pets/1")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.name").value("뽀삐2"))
                    .andExpect(jsonPath("$.result.size").value("MEDIUM"))
                    .andExpect(jsonPath("$.result.age").value(4));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void failPetNotFound() throws Exception {
            // given
            PetUpdateRequest request = new PetUpdateRequest(
                    "뽀삐2", PetSize.MEDIUM, 4, 5.0, "말티즈"
            );

            given(petCommandService.updatePet(eq(1L), eq(999L), any(PetUpdateRequest.class)))
                    .willThrow(new EntityNotFoundException("반려동물을 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(patch("/api/v1/pets/999")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 반려동물 수정 시도")
        void failNotOwner() throws Exception {
            // given
            PetUpdateRequest request = new PetUpdateRequest(
                    "뽀삐2", PetSize.MEDIUM, 4, 5.0, "말티즈"
            );

            given(petCommandService.updatePet(eq(2L), eq(1L), any(PetUpdateRequest.class)))
                    .willThrow(new ForbiddenException("해당 반려동물의 소유자가 아닙니다."));

            // when & then
            mockMvc.perform(patch("/api/v1/pets/1")
                            .header("X-User-Id", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/pets/{petId}")
    class DeletePet {

        @Test
        @DisplayName("성공: 반려동물 삭제")
        void success() throws Exception {
            // given
            doNothing().when(petCommandService).deletePet(1L, 1L);

            // when & then
            mockMvc.perform(delete("/api/v1/pets/1")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물")
        void failPetNotFound() throws Exception {
            // given
            doThrow(new EntityNotFoundException("반려동물을 찾을 수 없습니다."))
                    .when(petCommandService).deletePet(1L, 999L);

            // when & then
            mockMvc.perform(delete("/api/v1/pets/999")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패: 다른 사용자의 반려동물 삭제 시도")
        void failNotOwner() throws Exception {
            // given
            doThrow(new ForbiddenException("해당 반려동물의 소유자가 아닙니다."))
                    .when(petCommandService).deletePet(2L, 1L);

            // when & then
            mockMvc.perform(delete("/api/v1/pets/1")
                            .header("X-User-Id", "2"))
                    .andExpect(status().isForbidden());
        }
    }
}
