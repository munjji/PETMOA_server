package PetMoa.PetMoa.domain.reservation.controller;

import PetMoa.PetMoa.domain.reservation.dto.CancellationResult;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCancelResponse;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.dto.ReservationListResponse;
import PetMoa.PetMoa.domain.reservation.dto.ReservationResponse;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.service.ReservationFacade;
import PetMoa.PetMoa.domain.reservation.service.ReservationQueryService;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservation", description = "예약 API")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationQueryService reservationQueryService;
    private final ReservationFacade reservationFacade;

    @Operation(summary = "통합 예약 생성", description = "병원 예약 + 택시 예약(선택)을 생성합니다.")
    @PostMapping
    public ApiResponse<ReservationResponse> createReservation(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReservationCreateRequest request) {
        Reservation reservation = reservationFacade.createReservation(userId, request);
        return ApiResponse.onSuccess(ReservationResponse.from(reservation));
    }

    @Operation(summary = "내 예약 목록 조회", description = "현재 로그인한 사용자의 예약 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<ReservationListResponse> getMyReservations(
            @RequestHeader("X-User-Id") Long userId) {
        List<Reservation> reservations = reservationQueryService.getReservationsByUserId(userId);
        return ApiResponse.onSuccess(ReservationListResponse.from(reservations));
    }

    @Operation(summary = "예약 상세 조회", description = "예약 상세 정보를 조회합니다.")
    @GetMapping("/{reservationId}")
    public ApiResponse<ReservationResponse> getReservation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long reservationId) {
        Reservation reservation = reservationQueryService.getReservationByIdAndUserId(reservationId, userId);
        return ApiResponse.onSuccess(ReservationResponse.from(reservation));
    }

    @Operation(summary = "예약 취소", description = "예약을 취소합니다. 결제가 존재하면 환불 정책에 따라 자동 환불됩니다.")
    @PostMapping("/{reservationId}/cancel")
    public ApiResponse<ReservationCancelResponse> cancelReservation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long reservationId) {
        CancellationResult result = reservationFacade.cancelReservation(userId, reservationId);
        return ApiResponse.onSuccess(ReservationCancelResponse.from(
                result.reservation(),
                result.refundRate(),
                result.refundAmount()
        ));
    }
}
