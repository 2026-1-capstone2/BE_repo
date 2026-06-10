<div align="center">

# 🎥 단안 영상 기반 3D 공간 분석 AI 챗봇

**카메라 한 대로 찍은 영상에서 3D 공간을 분석하고, 자연어로 대화하는 서비스**

별도의 LiDAR·깊이 센서 없이, 일반 영상만으로 공간을 이해합니다.

<br>

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)
![AWS](https://img.shields.io/badge/AWS_EC2_·_S3-232F3E?style=flat-square&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![k6](https://img.shields.io/badge/k6-7D64FF?style=flat-square&logo=k6&logoColor=white)

</div>

<br>

> 💬 *"냉장고랑 소파 사이 거리 얼마야?"* → AI가 영상 분석 결과를 바탕으로 답변

이 저장소는 **백엔드** 파트입니다. 느린 AI 추론(45초+)을 비동기로 처리하면서도 안정적인 서비스를 보장하는 데 집중했고, **모든 설계 결정을 부하테스트로 검증**했습니다.

<div align="center">

| 역할 | 환경 | 핵심 과제 |
|:---:|:---:|:---:|
| 백엔드 · 인프라 · 부하테스트 | EC2 `1GB RAM · 2 vCPU` | 외부 AI 의존성 · 데이터 정합성 · 자원 제약 안정성 |

</div>

<br>

---

## 📊 부하테스트로 검증한 결과

> 제한된 자원(1GB·2코어)에서 한계와 병목을 **측정으로 규명**하고, 설정을 데이터 기반으로 결정했습니다.

<table>
<tr>
<th align="left">🗄️ Redis 캐싱 <code>ON vs OFF</code></th>
<th align="left">🔗 커넥션 풀 튜닝</th>
</tr>
<tr>
<td>

```
처리량   697  →  1,644 req/s   ▲ 2.4배
p95      277  →  76 ms         ▲ 3.6배
DB CPU   36%  →  0%            ★
```
**캐시는 속도가 아니라 `DB 보호 계층`**

</td>
<td>

```
공식값(5) → 경합 발생 → 측정 후 20
최악 지연  21.79s → 4.36s   ▼ 80%
```
**공식을 측정으로 반증하고 재결정**

</td>
</tr>
<tr>
<th align="left">📤 업로드 처리 <code>S3 우회</code></th>
<th align="left">📡 SSE 동시 연결</th>
</tr>
<tr>
<td>

```
2,558건 · 에러 0% · p95 227ms
MariaDB CPU         3.83%
```
**WAS가 바이너리를 직접 받지 않는 설계**

</td>
<td>

```
약 2,980 연결에서 포화
병목 = 스레드·FD ✗  →  메모리 ✓
```
**두 가설을 기각하고 진짜 병목 규명**

</td>
</tr>
</table>

> 📌 지표는 평균이 아닌 **p95 / p99**를 사용했습니다. 최댓값(p100)은 GC·네트워크 같은 우연에 좌우돼 재현이 어렵지만, p95/p99는 재현 가능해 **SLA의 기준**이 되기 때문입니다.

<br>

---

## 🏗️ 아키텍처

```
        ┌─────────────────────────┐
        │  Client (React · Vercel) │
        └───────────┬─────────────┘
              REST API │ SSE
        ┌───────────▼─────────────┐         ┌──────────────────┐
        │   Spring Boot (EC2)      │──REST──▶│  AI 서버 (FastAPI) │
        │                          │         │  CUT3R · VLM-3R   │
        │   ┌──────────────────┐   │◀──┐     └────────┬─────────┘
        │   │ Redis (캐싱)      │   │   │              │
        │   │ MariaDB (영속)    │   │   └── RabbitMQ ──┘
        │   └──────────────────┘   │     (분석 완료 메시지)
        └───────────┬─────────────┘
                    │
              ┌─────▼─────┐
              │  AWS S3    │  영상 · 결과 파일
              └───────────┘
```

<details>
<summary><b>📁 처리 흐름 자세히 보기</b></summary>

<br>

1. 클라이언트가 S3 **Presigned URL**을 발급받아 영상을 직접 업로드 *(서버 부하 최소화)*
2. 업로드 완료 알림 → 서버가 `jobId` 즉시 반환 *(수십 ms)*
3. 분석 요청을 **RabbitMQ**로 비동기 전달 → AI 서버 처리 *(45초+)*
4. 분석 완료 메시지를 Consumer가 수신 → DB 저장
5. 결과를 **SSE**로 클라이언트에 실시간 푸시 *(폴링 제거)*
6. 챗봇 질의는 대화 이력과 함께 AI 서버로 전달

</details>

<br>

---

## 🛡️ 안정성을 위한 설계 결정

<table>
<tr><td width="50%" valign="top">

### ⚡ 비동기 처리
느린 AI가 사용자 응답을 막지 않도록

AI 분석은 45초+. 동기로 처리하면 요청 하나가 스레드를 45초간 점유해 풀이 고갈됩니다. `jobId`만 즉시 반환하고 분석은 큐로 분리해 **외부 지연을 사용자 요청과 격리**했습니다.

</td><td width="50%" valign="top">

### 🔁 이중 멱등성
중복 요청에도 데이터는 한 번만

- **1차** `s3Key` 기준 애플리케이션 체크
- **2차** DB `UNIQUE` 제약 (동시성 방어)

쓰기 로직은 `VideoCommandService`로 분리해 AOP 프록시가 정상 동작하도록 구성. → 동시 5건 요청에도 DB에 **단 1건**만 생성됨을 검증

</td></tr>
<tr><td width="50%" valign="top">

### 🔒 트랜잭션 경계 분리
외부 장애가 데이터를 오염시키지 않도록

외부 호출이 트랜잭션 안에 있으면 AI 장애 시 `rollback-only`로 정상 저장까지 롤백됩니다.

```
진입점(Tx X) → DB 커밋(Tx) → 외부 호출(Tx 밖)
```

</td><td width="50%" valign="top">

### 📨 메시지 신뢰성
실패가 시스템을 마비시키지 않도록

- `requeue-rejected=false` 무한 재배달 차단
- Consumer 멱등성 (at-least-once 대비)
- 컨테이너 로그 상한 (연쇄 장애 방지)

</td></tr>
</table>

<br>

---

## 🧰 기술 스택

**Backend**
`Java 21` `Spring Boot 4.0.6` `Spring AMQP` `Spring WebFlux (WebClient)` `MariaDB 11.8` `Redis` `Scalar (API 문서)`

**Infra**
`AWS EC2 · S3` `Docker / Compose` `Nginx` `k6`

**Frontend**
`React 19` `Vite` `TypeScript 5` `Vercel`

**AI Server** *(협업)*
`Python 3.12` `FastAPI` `CUT3R · VLM-3R`

<br>

---

## 📂 프로젝트 구조

```
src/main/java/com/example/capstoneproject220261/
├── CapstoneProject220261Application.java
├── config/
│   ├── S3Config.java · RedisConfig.java · RabbitMQConfig.java
│   ├── WebClientConfig.java        # 외부 AI 호출 클라이언트
│   ├── CorsConfig.java
│   └── SwaggerConfig.java          # API 문서 (Scalar)
├── controller/
│   ├── S3Controller.java           # Presigned URL 발급
│   ├── VideoController.java         # 업로드 알림 · SSE · 결과 조회
│   └── ChatController.java          # 챗봇 질의
├── service/
│   ├── VideoService.java            # 조회 · 멱등성 · 트랜잭션 경계
│   ├── VideoCommandService.java     # 쓰기 전용 (프록시 분리)
│   ├── AiService.java               # 외부 AI 호출
│   ├── S3Service.java
│   ├── SseEmitterService.java       # SSE 연결 관리
│   └── AnalysisResultConsumer.java  # RabbitMQ 분석 완료 수신
├── domain/
│   ├── Video.java                   # 영상 · 작업 상태
│   └── AnalysisResult.java          # 분석 결과
├── repository/
│   ├── VideoRepository.java
│   └── AnalysisResultRepository.java
└── dto/
    ├── VideoUploadedRequestDto.java · VideoUploadedResponseDto.java
    ├── AiPreprocessRequestDto.java · AiPreprocessResponseDto.java
    ├── AnalysisCompletedMessageDto.java   # RabbitMQ 메시지 수신
    ├── AnalysisResultResponseDto.java
    └── ChatRequestDto.java · ChatResponseDto.java
```

<br>

---

## 🔌 API

| Method | Endpoint | 설명 |
|:------:|----------|------|
| `GET`  | `/api/s3/presigned-url?fileName={name}` | S3 업로드용 Presigned URL 발급 |
| `POST` | `/api/videos/uploaded` | 업로드 완료 알림 → `jobId` 반환 |
| `GET`  | `/api/videos/{jobId}/stream` | 분석 결과 SSE 구독 |
| `GET`  | `/api/videos/{jobId}/result` | 분석 결과 조회 *(캐싱 적용)* |
| `POST` | `/api/chat` | 챗봇 질의 *(대화 이력 포함)* |

> 📖 전체 API 문서는 **Scalar**로 제공합니다 → [`/scalar`](https://seongchan-spring.store/scalar)

<br>

---

## 🚀 로컬 실행

```bash
# 1. 의존 서비스
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name redis -p 6379:6379 redis

# 2. 실행
./gradlew bootRun
```

<details>
<summary><b>⚙️ 환경 설정 (.env · application.properties)</b></summary>

<br>

```env
AWS_ACCESS_KEY=your_key
AWS_SECRET_KEY=your_secret
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your_bucket
AI_SERVER_URL=https://your-ai-server-url
```

```properties
cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
cloud.aws.region.static=${AWS_REGION}
cloud.aws.s3.bucket=${AWS_S3_BUCKET}

spring.rabbitmq.host=localhost
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.data.redis.host=localhost

ai.server.url=${AI_SERVER_URL}

# 처리 실패 메시지의 무한 재배달 방지
spring.rabbitmq.listener.simple.default-requeue-rejected=false
```

RabbitMQ 관리 UI → `http://localhost:15672` (guest / guest)

</details>

<br>

---

## 🔧 트러블슈팅

> 통합 단계의 문제는 대부분 **시스템 간 인터페이스 계약 불일치**였습니다.

<details>
<summary><b>💥 독성 메시지 → 디스크 풀 → 배포 마비 (연쇄 장애)</b></summary>

<br>

메시지 스키마 불일치로 처리에 실패한 메시지가, 기본 requeue 정책과 만나 **초당 300회 이상 재배달**됐습니다. 매 재시도마다 예외 로그가 쌓여 단일 로그 파일이 16GB까지 커졌고, **디스크가 가득 차 배포 자체가 불가능한 상태**가 됐습니다.

→ requeue 정책 차단 · Consumer 멱등성 · 로그 상한으로 **각 증폭 지점을 차단**.

</details>

<details>
<summary><b>🔒 트랜잭션 rollback-only</b></summary>

<br>

외부 AI 호출이 트랜잭션 내부에 있어, 호출 실패 시 정상 저장까지 롤백되는 문제. 코드베이스 전체에 **트랜잭션 경계 분리** 원칙을 적용해 해결.

</details>

<details>
<summary><b>🌐 Nginx 프록시 캐시로 인한 502</b></summary>

<br>

컨테이너 재생성 시 IP가 바뀌었지만 Nginx가 옛 IP로 프록시. 브라우저엔 CORS 에러로 위장돼 진단이 늦어짐. 배포 루틴에 `nginx restart`를 추가.

</details>

> 💡 **핵심 교훈** — 장애는 연쇄·증폭된다. 1차 원인(작은 버그)보다 **증폭 장치(무제한 재시도·무제한 로그)**가 피해를 키운다.

<br>

---

## ✅ 진행 현황

```
[x] 프로젝트 세팅 · CI/CD          [x] SSE 실시간 결과 푸시
[x] S3 Presigned URL 업로드        [x] 챗봇 질의응답 (대화 이력)
[x] AI 서버 연동 (REST)            [x] Redis 캐싱 · 멱등성 · Tx 경계 분리
[x] RabbitMQ 비동기 메시징         [x] k6 부하테스트 (캐싱/풀/업로드/SSE)
```

<div align="center">
<br>

**제한된 자원에서, 추측이 아닌 측정으로 안정성을 증명한 백엔드**

</div>
