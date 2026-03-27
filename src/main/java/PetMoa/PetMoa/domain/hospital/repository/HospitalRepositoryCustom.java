package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.pet.entity.PetType;

import java.util.List;

public interface HospitalRepositoryCustom {

    /**
     * 병원 이름으로 검색 (부분 일치)
     */
    List<Hospital> findByNameContaining(String name);

    /**
     * 주소로 검색 (부분 일치)
     */
    List<Hospital> findByAddressContaining(String address);

    /**
     * 특정 반려동물 타입 진료 가능한 병원 조회
     */
    List<Hospital> findByAvailablePetType(PetType petType);

    /**
     * 위치 기반 병원 검색 (거리 계산 - 추후 구현 가능)
     */
    List<Hospital> findNearbyHospitals(Double latitude, Double longitude, Double radiusKm);
}
