package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.QHospital;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Hospital> findByNameContaining(String name) {
        QHospital hospital = QHospital.hospital;

        return queryFactory
                .selectFrom(hospital)
                .where(hospital.name.containsIgnoreCase(name))
                .fetch();
    }

    @Override
    public List<Hospital> findByAddressContaining(String address) {
        QHospital hospital = QHospital.hospital;

        return queryFactory
                .selectFrom(hospital)
                .where(hospital.address.containsIgnoreCase(address))
                .fetch();
    }

    @Override
    public List<Hospital> findByAvailablePetType(PetType petType) {
        QHospital hospital = QHospital.hospital;

        return queryFactory
                .selectFrom(hospital)
                .where(hospital.availablePetTypes.contains(petType))
                .fetch();
    }

    @Override
    public List<Hospital> findNearbyHospitals(Double latitude, Double longitude, Double radiusKm) {
        QHospital hospital = QHospital.hospital;

        // 하버사인 공식을 사용한 거리 계산
        // 간단한 구현: 위도/경도 범위로 필터링 (정확도는 낮지만 빠름)
        // 정확한 구현은 Native Query나 PostGIS 사용 권장

        double latDiff = radiusKm / 111.0; // 위도 1도 = 약 111km
        double lonDiff = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        return queryFactory
                .selectFrom(hospital)
                .where(hospital.latitude.isNotNull()
                        .and(hospital.longitude.isNotNull())
                        .and(hospital.latitude.between(latitude - latDiff, latitude + latDiff))
                        .and(hospital.longitude.between(longitude - lonDiff, longitude + lonDiff)))
                .fetch();
    }
}
