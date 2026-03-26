# CLAUDE.md - PetMoa 프로젝트

## 📌 프로젝트 개요
동물병원 예약 + 펫택시 예약을 하나로 통합한 예약 플랫폼

**핵심 기능:**
- 병원 예약 + 택시 예약 통합 처리 (단일 트랜잭션)
- 토스페이먼츠 API 연동
- Redis 분산락 기반 동시성 제어

---

## 🧱 기술 스택

**Server**
- Java + Spring Boot
- MySQL 8 + Redis 7
- Spring Data JPA
- Gradle

---

## 🏗️ 프로젝트 구조 (도메인형)
com.petcare/
├── global/                      # 전역 설정 및 공통 처리
│   ├── config/                 # 설정 (Redis, Swagger, 초기 데이터 등)
│   └── exception/              # 커스텀 예외, GlobalExceptionHandler
│
├── hospital/                   # 병원 도메인
│   ├── controller/             # REST API
│   ├── service/                # 비즈니스 로직
│   ├── repository/             # JPA Repository
│   ├── entity/                 # Entity
│   └── dto/                    # Request/Response DTO
│
├── reservation/                # 예약 도메인 (핵심)
│   ├── controller/
│   ├── service/                # 통합 예약 로직 (트랜잭션, 동시성 처리)
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── payment/                    # 결제 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
└── taxi/                       # 펫택시 도메인
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

### 레이어 원칙
- Controller → Service → Repository → Entity
- 비즈니스 로직은 Service에서 처리
- Controller는 요청/응답만 담당

---

## ⚙️ 핵심 비즈니스 규칙

### 1. 통합 예약 (핵심)
- 병원 예약 + 택시 예약 = 하나의 트랜잭션
- 하나라도 실패 시 전체 롤백
- Redis 분산락으로 동시 요청 제어

### 2. 동시성 제어
- 동일 시간대 동일 수의사 예약: 1건만 허용
- 정원 초과 절대 불가

### 3. 결제
- 선결제: 예약금 10,000원 + 택시비
- 환불 정책:
    - 24시간 전: 100%
    - 12시간 전: 50%
    - 당일: 환불 불가

---

## 🧠 AI 활용 원칙

### 1. 요구사항 → 설계 → 구현 → 검증 순서로 진행
- AI에게 바로 구현 요청 금지
- 반드시 요구사항 정리 후 설계 진행

### 2. 작업은 단계별로 분리
- 하나의 기능을 여러 단계(md)로 나누어 진행
- 각 단계 완료 후 검증 후 다음 단계 진행

### 3. 불확실한 요구사항은 반드시 질문
- 추측 기반 구현 금지
- 모호한 조건은 별도 문서로 정리

### 4. TDD 기반 개발
- 테스트 코드 먼저 작성
- 테스트 통과를 위한 최소 구현만 수행
- 불필요한 클래스/메서드 생성 금지

### 5. 결과보다 과정 기록
- 설계 이유, 기술 선택 근거 반드시 설명
- 모든 논의는 md 문서로 기록

### 6. AI는 제안, 사람은 결정
- 기술 선택, 락 전략, 트랜잭션 범위는 사람이 결정
- AI는 비교/정리 역할

---

## 🧾 코딩 원칙

### 네이밍 규칙
- Entity: Hospital, Veterinarian
- Service: HospitalService
- Repository: HospitalRepository
- Controller: HospitalController

### 코드 작성 기준
- 불필요한 추상화 금지
- 하나의 메서드는 하나의 책임만
- 예외는 명확한 메시지로 처리

---

## 🧪 테스트 원칙

- 동시성 문제 반드시 테스트로 검증
- 실패 케이스 포함 필수
- “100명 동시 요청 → 정원만큼만 성공” 시나리오 테스트

---

## 📌 현재 작업 상태

### 완료
- [x] 프로젝트 구조 설계
- [x] 기술 스택 결정

### 다음 작업
- [ ] Hospital, Veterinarian 엔티티 구현
- [ ] 초기 데이터 생성
- [ ] 병원 조회 API

---

## 🤖 AI 협업 가이드

**요청 예시**
- "CLAUDE.md 기준으로 Hospital 엔티티 설계해줘"
- "동시성 제어 방식 비교하고 이 프로젝트에 적합한 방법 제안해줘"
- "테스트 코드 먼저 작성해줘 (TDD)"

**응답 요구사항**
- 선택 이유 설명 필수
- 대안이 있다면 비교 제시
- 주의사항 포함
- 비즈니스 로직은 반드시 Service에 위치
- Controller는 요청/응답만 담당