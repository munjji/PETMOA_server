
# PetMoa API 스펙 문서

## 개요

| 항목 | 내용 |
|------|------|
| Base URL | `/api/v1` |
| 인증 | `X-User-Id` 헤더 (JWT 전환 예정) |
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
| TAXI4002 | 400 | 배차 가능한 택시 없음 |
| TAXI4003 | 400 | 해당 시간대 배차 불가 |
| RESERVATION4001 | 404 | 예약 없음 |
| RESERVATION4091 | 409 | 예약 시간 충돌 |
| PAYMENT4001 | 400 | 결제 실패 |
| REFUND4001 | 400 | 환불 불가 (당일 취소) |

---

## 1. 사용자 API (User)

### 1.1 내 정보 조회
```
GET /api/v1/users/me
X-User-Id: {userId}
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
    "address": "서울시 강남구"
  }
}
```

### 1.2 내 정보 수정
```
PATCH /api/v1/users/me
X-User-Id: {userId}
```

**Request**
```json
{
  "phoneNumber": "010-9999-8888",
  "address": "서울시 서초구"
}
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
    "phoneNumber": "010-9999-8888",
    "address": "서울시 서초구"
  }
}
```

---

## 2. 반려동물 API (Pet)

### 2.1 내 반려동물 목록 조회
```
GET /api/v1/pets
X-User-Id: {userId}
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
X-User-Id: {userId}
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

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "id": 1,
    "name": "뽀삐",
    "type": "DOG",
    "size": "SMALL",
    "breed": "말티즈",
    "age": 3,
    "weight": 4.5
  }
}
```

### 2.3 반려동물 정보 수정
```
PATCH /api/v1/pets/{petId}
X-User-Id: {userId}
```

**Request**
```json
{
  "name": "뽀삐",
  "size": "MEDIUM",
  "age": 4,
  "weight": 5.0,
  "breed": "말티즈"
}
```

### 2.4 반려동물 삭제
```
DELETE /api/v1/pets/{petId}
X-User-Id: {userId}
```

---

## 3. 병원 API (Hospital)

### 3.1 병원 목록 조회
```
GET /api/v1/hospitals
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| petType | string | N | 진료 가능 동물 (DOG, CAT, BIRD 등) |
| lat | double | N | 위도 (근처 검색용) |
| lng | double | N | 경도 (근처 검색용) |
| radius | double | N | 검색 반경 km (기본값: 5) |
| name | string | N | 병원 이름 검색 |
| address | string | N | 주소 검색 |

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
        "distance": null
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
        "workStartTime": "09:00:00",
        "workEndTime": "18:00:00"
      }
    ]
  }
}
```

### 3.3 수의사 목록 조회
```
GET /api/v1/hospitals/{hospitalId}/veterinarians
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "veterinarians": [
      {
        "id": 1,
        "name": "김수의",
        "department": "GENERAL",
        "workStartTime": "09:00:00",
        "workEndTime": "18:00:00"
      }
    ]
  }
}
```

### 3.4 수의사 예약 가능 시간 조회
```
GET /api/v1/hospitals/{hospitalId}/veterinarians/{vetId}/time-slots?date={date}
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| date | string | Y | 조회 날짜 (YYYY-MM-DD) |

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
        "startTime": "09:00:00",
        "endTime": "10:00:00",
        "capacity": 3,
        "currentReservations": 1,
        "available": true
      },
      {
        "id": 2,
        "startTime": "10:00:00",
        "endTime": "11:00:00",
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

> 펫택시는 카카오택시처럼 "호출" 방식으로 동작합니다.
> 사용자가 조건만 입력하면 시스템이 자동으로 배차합니다.

### 4.1 펫택시 이용 가능 여부 확인
```
GET /api/v1/pet-taxis/check-availability
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| petSize | string | Y | 반려동물 크기 (SMALL, MEDIUM, LARGE) |
| pickupTime | string | Y | 픽업 희망 시간 (ISO 8601) |
| pickupAddress | string | Y | 출발지 주소 |

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "available": true,
    "estimatedFee": 15000,
    "estimatedArrivalMinutes": 10,
    "availableVehicleCount": 3
  }
}
```

**비즈니스 규칙**
- 해당 시간대에 배차 가능한 택시가 있는지 확인
- 반려동물 크기에 맞는 차량만 검색
- 예상 요금은 거리 기반으로 계산

---

## 5. 예약 API (Reservation) - 핵심

### 5.1 통합 예약 생성
```
POST /api/v1/reservations
X-User-Id: {userId}
```

**Request**
```json
{
  "petId": 1,
  "hospitalReservation": {
    "timeSlotId": 1,
    "symptomDescription": "기침을 자주 해요"
  },
  "taxiRequests": [
    {
      "type": "PICKUP",
      "pickupAddress": "서울시 강남구 역삼동 123",
      "dropoffAddress": "강남동물병원",
      "scheduledTime": "2024-01-15T08:30:00"
    },
    {
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
- `taxiRequests`: 선택 (0~2개)
  - PICKUP: 집 → 병원
  - RETURN: 병원 → 집
- 택시만 예약은 불가
- **택시는 시스템이 자동 배차** (petTaxiId 불필요)
  - 반려동물 크기에 맞는 차량 자동 선택
  - 해당 시간대 가용한 택시 중 배차
  - 배차 불가 시 예약 실패

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "id": 1,
    "status": "PENDING_PAYMENT",
    "createdAt": "2024-01-10T14:30:00",
    "pet": {
      "id": 1,
      "name": "뽀삐"
    },
    "hospitalReservation": {
      "hospitalName": "강남동물병원",
      "veterinarianName": "김수의",
      "date": "2024-01-15",
      "time": "09:00:00-10:00:00"
    },
    "taxiDispatch": [
      {
        "type": "PICKUP",
        "status": "PENDING",
        "driverName": "홍길동",
        "driverPhoneNumber": "010-1234-5678",
        "vehicleNumber": "서울12가3456",
        "scheduledTime": "2024-01-15T08:30:00",
        "estimatedFee": 15000
      }
    ]
  }
}
```

### 5.2 내 예약 목록 조회
```
GET /api/v1/reservations
X-User-Id: {userId}
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
          "hospitalName": "강남동물병원",
          "veterinarianName": "김수의",
          "date": "2024-01-15",
          "time": "09:00:00-10:00:00"
        },
        "taxiDispatch": [
          {
            "type": "PICKUP",
            "status": "ASSIGNED",
            "driverName": "홍길동",
            "driverPhoneNumber": "010-1234-5678",
            "vehicleNumber": "서울12가3456",
            "scheduledTime": "2024-01-15T08:30:00",
            "estimatedFee": 15000
          }
        ]
      }
    ]
  }
}
```

### 5.3 예약 상세 조회
```
GET /api/v1/reservations/{reservationId}
X-User-Id: {userId}
```

### 5.4 예약 취소
```
POST /api/v1/reservations/{reservationId}/cancel
X-User-Id: {userId}
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
      "reason": "24시간 전 취소",
      "refundProcessed": true
    }
  }
}
```

**환불 정책**
| 취소 시점 | 환불률 |
|----------|--------|
| 24시간 전 | 100% |
| 12시간 전 | 50% |
| 당일 | 0% |

---

## 6. 결제 API (Payment)

### 6.1 결제 요청 생성
```
POST /api/v1/payments
X-User-Id: {userId}
```

**Request**
```json
{
  "reservationId": 1,
  "method": "CARD"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "paymentId": 1,
    "reservationId": 1,
    "orderId": "ORDER_20240115_ABC123",
    "paymentKey": null,
    "totalAmount": 25000,
    "depositAmount": 10000,
    "taxiFare": 15000,
    "status": "PENDING",
    "method": "CARD",
    "paidAt": null,
    "cancelledAt": null,
    "refundAmount": null,
    "cancelReason": null
  }
}
```

### 6.2 결제 승인 (토스페이먼츠 콜백)
```
POST /api/v1/payments/confirm
X-User-Id: {userId}
```

**Request** (토스페이먼츠에서 전달)
```json
{
  "paymentKey": "toss_payment_key",
  "orderId": "ORDER_20240115_ABC123",
  "amount": 25000
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "paymentId": 1,
    "reservationId": 1,
    "orderId": "ORDER_20240115_ABC123",
    "paymentKey": "toss_payment_key",
    "totalAmount": 25000,
    "depositAmount": 10000,
    "taxiFare": 15000,
    "status": "APPROVED",
    "method": "CARD",
    "paidAt": "2024-01-15T14:35:00",
    "cancelledAt": null,
    "refundAmount": null,
    "cancelReason": null
  }
}
```

### 6.3 결제 조회
```
GET /api/v1/payments/{paymentId}
X-User-Id: {userId}
```

### 6.4 주문 ID로 결제 조회
```
GET /api/v1/payments/orders/{orderId}
X-User-Id: {userId}
```

### 6.5 예약 ID로 결제 조회
```
GET /api/v1/payments/reservations/{reservationId}
X-User-Id: {userId}
```

### 6.6 결제 환불
```
POST /api/v1/payments/{paymentId}/refund
X-User-Id: {userId}
```

**Request**
```json
{
  "cancelReason": "고객 요청으로 취소"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공입니다.",
  "result": {
    "paymentId": 1,
    "reservationId": 1,
    "orderId": "ORDER_20240115_ABC123",
    "paymentKey": "toss_payment_key",
    "totalAmount": 25000,
    "depositAmount": 10000,
    "taxiFare": 15000,
    "status": "CANCELLED",
    "method": "CARD",
    "paidAt": "2024-01-15T14:35:00",
    "cancelledAt": "2024-01-15T15:00:00",
    "refundAmount": 25000,
    "cancelReason": "고객 요청으로 취소"
  }
}
```

---

## 7. 관리자 API (Admin) - 미구현

> 모든 관리자 API는 ADMIN 역할 필요 (추후 구현 예정)

### 7.1 병원 관리

```
POST   /api/v1/admin/hospitals           # 병원 등록
PATCH  /api/v1/admin/hospitals/{hospitalId}      # 병원 수정
DELETE /api/v1/admin/hospitals/{hospitalId}      # 병원 삭제
```

### 7.2 수의사 관리

```
POST   /api/v1/admin/hospitals/{hospitalId}/veterinarians           # 수의사 등록
PATCH  /api/v1/admin/hospitals/{hospitalId}/veterinarians/{vetId}      # 수의사 수정
DELETE /api/v1/admin/hospitals/{hospitalId}/veterinarians/{vetId}      # 수의사 삭제
```

### 7.3 타임슬롯 관리

```
POST   /api/v1/admin/veterinarians/{vetId}/time-slots    # 타임슬롯 생성
DELETE /api/v1/admin/time-slots/{timeSlotId}                      # 타임슬롯 삭제
```

### 7.4 펫택시 관리

```
POST   /api/v1/admin/pet-taxis           # 펫택시(차량) 등록
PATCH  /api/v1/admin/pet-taxis/{petTaxiId}      # 펫택시 수정 (운행 상태 변경 포함)
DELETE /api/v1/admin/pet-taxis/{petTaxiId}      # 펫택시 삭제
```

### 7.5 택시 배차 관리

```
GET    /api/v1/admin/taxi-dispatches                    # 배차 목록 조회
GET    /api/v1/admin/taxi-dispatches/{id}               # 배차 상세 조회
PATCH  /api/v1/admin/taxi-dispatches/{id}/status        # 배차 상태 변경
PATCH  /api/v1/admin/taxi-dispatches/{id}/reassign      # 택시 재배차 (수동)
```

### 7.6 예약 관리

```
GET    /api/v1/admin/reservations                    # 전체 예약 조회
PATCH  /api/v1/admin/reservations/{reservationId}/status        # 예약 상태 변경
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
| 값 | 설명                         |
|----|----------------------------|
| SMALL | 소형 (소형견만 탑승 가능)            |
| MEDIUM | 중형 (소형견, 중형견 탑승 가능)        |
| LARGE | 대형 (소형견, 중형견, 대형견까지 탑승 가능) |

### ReservationStatus (예약 상태)
| 값 | 설명 |
|----|------|
| PENDING_PAYMENT | 결제 대기 |
| CONFIRMED | 예약 확정 |
| COMPLETED | 완료 |
| CANCELLED | 취소됨 |
| NO_SHOW | 노쇼 |

### TaxiReservationType (택시 요청 유형)
| 값 | 설명 |
|----|------|
| PICKUP | 픽업 (집 → 병원) |
| RETURN | 귀가 (병원 → 집) |

### TaxiReservationStatus (택시 배차 상태)
| 값 | 설명 |
|----|------|
| PENDING | 배차 대기 |
| ASSIGNED | 배차 완료 |
| IN_PROGRESS | 운행 중 |
| COMPLETED | 운행 완료 |
| CANCELLED | 취소됨 |

### PaymentStatus (결제 상태)
| 값 | 설명 |
|----|------|
| PENDING | 결제 대기 |
| APPROVED | 승인 완료 |
| CANCELLED | 취소 |
| PARTIAL_CANCELLED | 부분 취소 |
| FAILED | 실패 |

### PaymentMethod (결제 수단)
| 값 | 설명 |
|----|------|
| CARD | 카드 |
| VIRTUAL_ACCOUNT | 가상계좌 |
| TRANSFER | 계좌이체 |
| MOBILE_PHONE | 휴대폰 |
| TOSS_PAY | 토스페이 |
| KAKAO_PAY | 카카오페이 |
| NAVER_PAY | 네이버페이 |

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
