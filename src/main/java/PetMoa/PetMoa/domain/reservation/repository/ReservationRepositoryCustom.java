package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;

import java.util.List;

public interface ReservationRepositoryCustom {

    /**
     * 사용자별 예약 목록 조회 (fetch join)
     */
    List<Reservation> findByUserIdWithDetails(Long userId);

    /**
     * 예약 상세 조회 (모든 연관관계 fetch join)
     */
    Reservation findByIdWithDetails(Long reservationId);
}
