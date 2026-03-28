package PetMoa.PetMoa.domain.hospital.repository;


import PetMoa.PetMoa.domain.hospital.dto.HospitalSearchCondition;
import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.QHospital;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private static final QHospital hospital = QHospital.hospital;
    private static final double KILOMETERS_PER_DEGREE_LATITUDE = 111.0;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Hospital> findByNameContaining(String name) {
        return queryFactory
                .selectFrom(hospital)
                .where(hospital.name.containsIgnoreCase(name))
                .fetch();
    }

    @Override
    public List<Hospital> findByAddressContaining(String address) {
        return queryFactory
                .selectFrom(hospital)
                .where(hospital.address.containsIgnoreCase(address))
                .fetch();
    }

    @Override
    public List<Hospital> findByAvailablePetType(PetType petType) {
        return queryFactory
                .selectFrom(hospital)
                .where(hospital.availablePetTypes.contains(petType))
                .fetch();
    }

    @Override
    public List<Hospital> findNearbyHospitals(Double latitude, Double longitude, Double radiusKm) {
        if (latitude == null || longitude == null || radiusKm == null || radiusKm <= 0) {
            return java.util.Collections.emptyList();
        }

        // 하버사인 공식을 사용한 거리 계산
        // 간단한 구현: 위도/경도 범위로 필터링 (정확도는 낮지만 빠름)
        // 정확한 구현은 Native Query나 PostGIS 사용 권장

        double latDiff = radiusKm / KILOMETERS_PER_DEGREE_LATITUDE;
        double lonDiff = radiusKm / (KILOMETERS_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(latitude)));

        return queryFactory
                .selectFrom(hospital)
                .where(hospital.latitude.isNotNull()
                        .and(hospital.longitude.isNotNull())
                        .and(hospital.latitude.between(latitude - latDiff, latitude + latDiff))
                        .and(hospital.longitude.between(longitude - lonDiff, longitude + lonDiff)))
                .fetch();
    }

    @Override
    public List<Hospital> searchWithConditions(HospitalSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건 1: 동물 종류
        if (condition.petType() != null) {
            builder.and(hospital.availablePetTypes.contains(condition.petType()));
        }

        // 기본 조건 2: 위치 기반
        if (condition.hasLocation()) {
            double radiusKm = condition.getRadiusOrDefault();
            double latDiff = radiusKm / KILOMETERS_PER_DEGREE_LATITUDE;
            double lonDiff = radiusKm / (KILOMETERS_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(condition.latitude())));

            builder.and(hospital.latitude.isNotNull())
                    .and(hospital.longitude.isNotNull())
                    .and(hospital.latitude.between(condition.latitude() - latDiff, condition.latitude() + latDiff))
                    .and(hospital.longitude.between(condition.longitude() - lonDiff, condition.longitude() + lonDiff));
        }

        // 추가 필터: 이름 검색
        if (condition.hasName()) {
            builder.and(hospital.name.containsIgnoreCase(condition.name()));
        }

        // 추가 필터: 주소 검색
        if (condition.hasAddress()) {
            builder.and(hospital.address.containsIgnoreCase(condition.address()));
        }

        return queryFactory
                .selectFrom(hospital)
                .where(builder)
                .fetch();
    }
}
