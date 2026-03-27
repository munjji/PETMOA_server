package PetMoa.PetMoa.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.Set;

public interface TaxiReservationRepositoryCustom {

    /**
     * 특정 시간대에 이미 예약된 택시 ID 목록 조회
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 해당 시간대에 예약된 택시 ID 집합
     */
    Set<Long> findReservedTaxiIdsBetween(LocalDateTime startTime, LocalDateTime endTime);
}
