# CLAUDE.md - PetMoa 프로젝트

## 프로젝트 개요
동물병원 예약 + 펫택시 예약을 하나로 통합한 예약 플랫폼

**핵심 기능:**
- 병원 예약과 택시 예약을 한 번에 처리 (통합 예약)
- 토스페이먼츠 실제 API 연동
- Redis 분산락으로 동시성 제어

---

## 기술 스택

**Backend:**
- Java 17 + Spring Boot
- MySQL 8 + Redis 7
- Spring Data JPA + QueryDSL
- Swagger (SpringDoc OpenAPI)
- Gradle

---

## 프로젝트 구조

**도메인형 패키지 구조:**
```
com.petmoa/
├── domain/
│   ├── member/              # 회원
│   ├── pet/                 # 반려동물
│   ├── hospital/            # 병원
│   ├── taxi/                # 펫택시
│   ├── appointment/         # 통합 예약 (핵심!)
│   ├── payment/             # 결제
│   ├── review/              # 리뷰
│   └── notification/        # 알림
│
└── global/                  # 공통 유틸, 설정 등
    ├── apiPayload/          # API 공통 응답
    ├── config/
    ├── exception/
    └── util/
```

**각 도메인 내부 구조:**
```
domain/hospital/
├── entity/
├── repository/
├── service/
├── controller/
├── dto/
└── converter/
```

---

## 핵심 비즈니스 규칙

### 통합 예약 (가장 중요!)
- 병원 예약 + 택시 예약 = 하나의 트랜잭션
- 하나라도 실패하면 전체 롤백
- Redis 분산락으로 동시 예약 방지

### 동시성 제어
- 같은 시간대 수의사 예약: 1명만
- 정원 초과 절대 불가

### 결제
- 선결제: 예약금 10,000원 + 택시비
- 환불: 24시간 전 100%, 12시간 전 50%, 당일 불가

---

## 코딩 원칙

**Java 스타일:**
```java
// ✅ Good: 생성자 주입 + interface
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {
    private final HospitalRepository hospitalRepository;
}

// DTO는 record 사용
public record HospitalRequest(
    String name,
    String address
) {}

// Repository는 interface + 구현체
public interface HospitalRepository {
    Hospital save(Hospital hospital);
    Optional<Hospital> findById(Long id);
}

@Repository
@RequiredArgsConstructor
public class HospitalRepositoryImpl implements HospitalRepository {
    private final JPAQueryFactory queryFactory;
    // 구현...
}
```

**명명 규칙:**
- Entity: Hospital, Veterinarian
- Repository: HospitalRepository, HospitalRepositoryImpl
- Service: HospitalService, HospitalServiceImpl
- Controller: HospitalController
- DTO: HospitalRequest, HospitalResponse (record)
- Converter: HospitalConverter

---

## 현재 작업 상태

### 완료
- [x] 프로젝트 구조 설계
- [x] 기술 스택 결정

### 다음 작업
- [ ] 프로젝트 초기 세팅 (build.gradle, application.yml)
- [ ] Member, Pet 엔티티 구현
- [ ] Hospital, Veterinarian 엔티티 구현
- [ ] 초기 데이터 생성 로직

---

## AI 협업 시 참고

**요청할 때:**
- "CLAUDE.md 보고 Hospital 엔티티 만들어줘"
- "통합 예약 로직에서 롤백 어떻게 처리하면 좋을까?"

**설명해줄 때:**
- 왜 그렇게 했는지
- 주의사항이 있다면
- 다른 방법과 비교했을 때 장단점
