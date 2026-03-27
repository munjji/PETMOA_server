
# PetMoa API 스펙 문서

## 개요

| 항목 | 내용 |
|------|------|
| Base URL | `/api/v1` |
| 인증 | JWT Bearer Token (추후 구현) |
| 역할 | USER (일반 사용자), ADMIN (관리자) |

## 공통 응답 형식

### 성공 응답
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": { ... }
}
```

### 에러 응답
```json
{
  "isSuccess": false,
  "code": "MEMBER4001",
  "message": "사용자가 없습니다.",
  "result": null
}
```

### 응답 코드 체계

#### 성공 코드
| 코드 | HTTP 상태 | 설명 |
|------|----------|------|
| COMMON200 | 200 | 성공 |
| COMMON201 | 201 | 생성 성공 |

#### 에러 코드
| 코드 | HTTP 상태 | 설명 |
|------|----------|------|
| COMMON400 | 400 | 잘못된 요청 |
| COMMON401 | 401 | 인증 필요 |
| COMMON403 | 403 | 권한 없음 |
| COMMON500 | 500 | 서버 에러 |
| MEMBER4001 | 404 | 사용자 없음 |
| PET4001 | 404 | 반려동물 없음 |
| HOSPITAL4001 | 404 | 병원 없음 |
| VETERINARIAN4001 | 404 | 수의사 없음 |
| TIMESLOT4001 | 404 | 타임슬롯 없음 |
| TIMESLOT4091 | 409 | 타임슬롯 정원 초과 |
| TAXI4001 | 404 | 펫택시 없음 |
| TAXI4002 | 400 | 펫택시 이용 불가 |
| RESERVATION4001 | 404 | 예약 없음 |
| RESERVATION4091 | 409 | 예약 시간 충돌 |
| PAYMENT4001 | 400 | 결제 실패 |
| REFUND4001 | 400 | 환불 불가 (당일 취소) |

---

## 1. 사용자 API (User)

### 1.1 내 정보 조회
```
GET /api/v1/users/me
Authorization: Bearer {token}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "phoneNumber": "010-1234-5678",
    "address": "서울시 강남구",
    "role": "USER"
  }
}
```

### 1.2 내 정보 수정
```
PATCH /api/v1/users/me
Authorization: Bearer {token}
```

**Request**
```json
{
  "phoneNumber": "010-9999-8888",
  "address": "서울시 서초구"
}
```

---

## 2. 반려동물 API (Pet)

### 2.1 내 반려동물 목록 조회
```
GET /api/v1/pets
Authorization: Bearer {token}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "pets": [
      {
        "id": 1,
        "name": "뽀삐",
        "type": "DOG",
        "size": "SMALL",
        "breed": "말티즈",
        "age": 3,
        "weight": 4.5
      }
    ]
  }
}
```

### 2.2 반려동물 등록
```
POST /api/v1/pets
Authorization: Bearer {token}
```

**Request**
```json
{
  "name": "뽀삐",
  "type": "DOG",
  "size": "SMALL",
  "breed": "말티즈",
  "age": 3,
  "weight": 4.5
}
```

### 2.3 반려동물 정보 수정
```
PATCH /api/v1/pets/{petId}
Authorization: Bearer {token}
```

### 2.4 반려동물 삭제
```
DELETE /api/v1/pets/{petId}
Authorization: Bearer {token}
```

---

## 3. 병원 API (Hospital)

### 3.1 병원 목록 조회
```
GET /api/v1/hospitals
```

**Query Parameters**
```
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| name | string | 병원 이름 검색 |
| address | string | 주소 검색 |
| petType | string | 진료 가능 동물 (DOG, CAT, BIRD 등) |
| lat | double | 위도 (근처 검색용) |
| lng | double | 경도 (근처 검색용) |
| radius | double | 검색 반경 km (기본값: 5) |
```
**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "hospitals": [
      {
        "id": 1,
        "name": "강남동물병원",
        "address": "서울시 강남구 테헤란로 123",
        "phoneNumber": "02-1234-5678",
        "latitude": 37.5,
        "longitude": 127.0,
        "availablePetTypes": ["DOG", "CAT"],
        "distance": 1.2
      }
    ]
  }
}
```

### 3.2 병원 상세 조회
```
GET /api/v1/hospitals/{hospitalId}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "id": 1,
    "name": "강남동물병원",
    "address": "서울시 강남구 테헤란로 123",
    "phoneNumber": "02-1234-5678",
    "latitude": 37.5,
    "longitude": 127.0,
    "availablePetTypes": ["DOG", "CAT"],
    "veterinarians": [
      {
        "id": 1,
        "name": "김수의",
        "department": "GENERAL",
        "workStartTime": "09:00",
        "workEndTime": "18:00"
      }
    ]
  }
}
```

### 3.3 수의사 목록 조회
```
GET /api/v1/hospitals/{hospitalId}/veterinarians
```

### 3.4 수의사 예약 가능 시간 조회
```
GET /api/v1/hospitals/{hospitalId}/veterinarians/{vetId}/time-slots
```

**Query Parameters**
```
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| date | string | 조회 날짜 (YYYY-MM-DD) |
```
**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "date": "2024-01-15",
    "veterinarian": {
      "id": 1,
      "name": "김수의",
      "department": "GENERAL"
    },
    "timeSlots": [
      {
        "id": 1,
        "startTime": "09:00",
        "endTime": "10:00",
        "capacity": 3,
        "currentReservations": 1,
        "available": true
      },
      {
        "id": 2,
        "startTime": "10:00",
        "endTime": "11:00",
        "capacity": 3,
        "currentReservations": 3,
        "available": false
      }
    ]
  }
}
```

---

## 4. 펫택시 API (PetTaxi)

### 4.1 이용 가능한 펫택시 조회
```
GET /api/v1/pet-taxis/available
```

**Query Parameters**
```
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| petSize | string | 반려동물 크기 (SMALL, MEDIUM, LARGE) |
| pickupTime | string | 픽업 희망 시간 (ISO 8601) |
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "petTaxis": [
      {
        "id": 1,
        "licensePlate": "서울12가3456",
        "driverName": "홍길동",
        "driverPhoneNumber": "010-1234-5678",
        "vehicleSize": "MEDIUM",
        "status": "AVAILABLE"
      }
    ]
  }
}
```

---

## 5. 예약 API (Reservation) - 핵심

### 5.1 통합 예약 생성
```
POST /api/v1/reservations
Authorization: Bearer {token}
```

**Request**
```json
{
  "petId": 1,
  "hospitalReservation": {
    "timeSlotId": 1,
    "symptomDescription": "기침을 자주 해요"
  },
  "taxiReservations": [
    {
      "petTaxiId": 1,
      "type": "PICKUP",
      "pickupAddress": "서울시 강남구 역삼동 123",
      "dropoffAddress": "강남동물병원",
      "scheduledTime": "2024-01-15T08:30:00"
    },
    {
      "petTaxiId": 2,
      "type": "RETURN",
      "pickupAddress": "강남동물병원",
      "dropoffAddress": "서울시 강남구 역삼동 123",
      "scheduledTime": "2024-01-15T11:00:00"
    }
  ]
}
```

**비즈니스 규칙**
- `hospitalReservation`: 필수
- `taxiReservations`: 선택 (0~2개)
  - PICKUP: 집 → 병원
  - RETURN: 병원 → 집
- 택시만 예약은 불가

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON201",
  "message": "요청 성공 및 리소스 생성됨",
  "result": {
    "reservationId": 1,
    "status": "PENDING_PAYMENT",
    "payment": {
      "totalAmount": 35000,
      "breakdown": {
        "depositAmount": 10000,
        "pickupTaxiFee": 15000,
        "returnTaxiFee": 10000
      },
      "paymentUrl": "https://pay.toss.im/..."
    }
  }
}
```

### 5.2 내 예약 목록 조회
```
GET /api/v1/reservations
Authorization: Bearer {token}
```

**Query Parameters**
```
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| status | string | 상태 필터 (PENDING, CONFIRMED, COMPLETED, CANCELLED) |
| from | string | 시작 날짜 |
| to | string | 종료 날짜 |
```
**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "reservations": [
      {
        "id": 1,
        "status": "CONFIRMED",
        "createdAt": "2024-01-10T14:30:00",
        "pet": {
          "id": 1,
          "name": "뽀삐"
        },
        "hospitalReservation": {
          "hospital": "강남동물병원",
          "veterinarian": "김수의",
          "date": "2024-01-15",
          "time": "09:00-10:00"
        },
        "taxiReservations": [
          {
            "type": "PICKUP",
            "scheduledTime": "2024-01-15T08:30:00"
          }
        ],
        "payment": {
          "totalAmount": 25000,
          "status": "PAID"
        }
      }
    ]
  }
}
```

### 5.3 예약 상세 조회
```
GET /api/v1/reservations/{reservationId}
Authorization: Bearer {token}
```

### 5.4 예약 취소
```
POST /api/v1/reservations/{reservationId}/cancel
Authorization: Bearer {token}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "reservationId": 1,
    "status": "CANCELLED",
    "refund": {
      "refundAmount": 25000,
      "refundRate": 100,
      "reason": "24시간 전 취소"
    }
  }
}
```

**환불 정책**
```
| 취소 시점 | 환불률 |
|----------|--------|
| 24시간 전 | 100% |
| 12시간 전 | 50% |
| 당일 | 0% |
```
---

## 6. 결제 API (Payment)

### 6.1 결제 확인 (토스페이먼츠 콜백)
```
POST /api/v1/payments/confirm
```

**Request** (토스페이먼츠에서 전달)
```json
{
  "paymentKey": "toss_payment_key",
  "orderId": "order_123",
  "amount": 25000
}
```

### 6.2 결제 내역 조회
```
GET /api/v1/payments/{paymentId}
Authorization: Bearer {token}
```

---

## 7. 관리자 API (Admin)

> 모든 관리자 API는 ADMIN 역할 필요

### 7.1 병원 관리

```
POST   /api/v1/admin/hospitals           # 병원 등록
PATCH  /api/v1/admin/hospitals/{id}      # 병원 수정
DELETE /api/v1/admin/hospitals/{id}      # 병원 삭제
```

### 7.2 수의사 관리

```
POST   /api/v1/admin/hospitals/{hospitalId}/veterinarians           # 수의사 등록
PATCH  /api/v1/admin/hospitals/{hospitalId}/veterinarians/{id}      # 수의사 수정
DELETE /api/v1/admin/hospitals/{hospitalId}/veterinarians/{id}      # 수의사 삭제
```

### 7.3 타임슬롯 관리

```
POST   /api/v1/admin/veterinarians/{vetId}/time-slots    # 타임슬롯 생성
DELETE /api/v1/admin/time-slots/{id}                      # 타임슬롯 삭제
```

### 7.4 펫택시 관리

```
POST   /api/v1/admin/pet-taxis           # 펫택시 등록
PATCH  /api/v1/admin/pet-taxis/{id}      # 펫택시 수정 (상태 변경 포함)
DELETE /api/v1/admin/pet-taxis/{id}      # 펫택시 삭제
```

### 7.5 예약 관리

```
GET    /api/v1/admin/reservations                    # 전체 예약 조회
PATCH  /api/v1/admin/reservations/{id}/status        # 예약 상태 변경
```

---

## Enum 정의

### PetType (반려동물 종류)
| 값 | 설명 |
|----|------|
| DOG | 강아지 |
| CAT | 고양이 |
| BIRD | 새 |
| RABBIT | 토끼 |
| HAMSTER | 햄스터 |
| ETC | 기타 |

### PetSize (반려동물 크기)
| 값 | 설명 |
|----|------|
| SMALL | 소형 (10kg 미만) |
| MEDIUM | 중형 (10-25kg) |
| LARGE | 대형 (25kg 이상) |

### VehicleSize (차량 크기)
| 값 | 설명 |
|----|------|
| SMALL | 소형 (소형견만 탑승 가능) |
| MEDIUM | 중형 (중형견까지 탑승 가능) |
| LARGE | 대형 (대형견까지 탑승 가능) |

### ReservationStatus (예약 상태)
| 값 | 설명 |
|----|------|
| PENDING_PAYMENT | 결제 대기 |
| CONFIRMED | 예약 확정 |
| COMPLETED | 완료 |
| CANCELLED | 취소됨 |
| NO_SHOW | 노쇼 |

### TaxiReservationType (택시 예약 유형)
| 값 | 설명 |
|----|------|
| PICKUP | 픽업 (집 → 병원) |
| RETURN | 귀가 (병원 → 집) |

### PaymentStatus (결제 상태)
| 값 | 설명 |
|----|------|
| PENDING | 결제 대기 |
| PAID | 결제 완료 |
| REFUNDED | 환불됨 |
| PARTIALLY_REFUNDED | 부분 환불 |

### MedicalDepartment (진료과목)
| 값 | 설명 |
|----|------|
| GENERAL | 일반 진료 |
| SURGERY | 외과 |
| INTERNAL | 내과 |
| DERMATOLOGY | 피부과 |
| OPHTHALMOLOGY | 안과 |
| DENTISTRY | 치과 |
| EMERGENCY | 응급 |

