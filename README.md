# 단안 영상 기반 3D 공간 분석 및 부하 최적화 서비스

단안 카메라로 찍은 영상을 분석해서 공간 정보를 뽑아내고, 자연어로 질문하면 답변해주는 서비스입니다.
예) "냉장고랑 소파 사이 거리 얼마야?" → AI가 영상 분석 결과를 바탕으로 답변

---

## 아키텍처

```
클라이언트 (React Web / React Native)
        ↕ REST API · SSE
Spring 서버 (Java)
        ↕ REST API
AI 서버 (FastAPI · LangChain)
        ↕ RabbitMQ
Spring 서버 → MariaDB / MongoDB
```

영상은 S3에 직접 업로드하고, 분석 결과는 RabbitMQ를 통해 Spring으로 전달합니다. 챗봇 응답은 SSE로 스트리밍합니다.

---

## 기술 스택

**백엔드**
- Java 21, Spring Boot 4.0.6
- Spring WebFlux (WebClient), Spring AMQP
- MariaDB 11.8, MongoDB 8.0
- RabbitMQ 4.2.6, AWS S3 SDK 2.25.70, Firebase Admin 9.2.0

**AI 서버**
- Python 3.12, FastAPI 0.115, LangChain 0.3

**프론트엔드**
- React 19, React Native 0.79, TypeScript 5

**기타**
- Docker, k6 (부하테스트)

---

## 프로젝트 구조

```
src/main/java/com/example/
├── config/
│   ├── S3Config.java
│   ├── WebClientConfig.java
│   └── RabbitMQConfig.java
├── controller/
│   ├── S3Controller.java
│   └── VideoController.java
├── service/
│   ├── S3Service.java
│   └── AiService.java
└── consumer/
    └── AnalysisConsumer.java
```

---

## 로컬 실행

**1. RabbitMQ 띄우기**
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

**2. .env 파일 생성**
```
AWS_ACCESS_KEY=your_key
AWS_SECRET_KEY=your_secret
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your_bucket
```

**3. application.properties**
```properties
cloud.aws.credentials.access-key=${AWS_ACCESS_KEY}
cloud.aws.credentials.secret-key=${AWS_SECRET_KEY}
cloud.aws.region.static=${AWS_REGION}
cloud.aws.s3.bucket=${AWS_S3_BUCKET}

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

ai.server.url=https://your-ai-server-url
```

**4. 실행**
```bash
./gradlew bootRun
```

RabbitMQ 관리자 UI → http://localhost:15672 (guest / guest)

---

## API

| Method | URL | 설명 |
|---|---|---|
| GET | `/api/s3/presigned-url?fileName={name}` | S3 업로드 URL 발급 |
| POST | `/api/video/analyze` | 영상 분석 요청 |
| GET | `/api/video/status/{jobId}` | 작업 상태 조회 |

---

## 진행 상황

- [x] 프로젝트 세팅
- [x] S3 연동
- [x] AI 서버 연동
- [ ] RabbitMQ 연동
- [ ] SSE 챗봇
- [ ] FCM 푸시알림
- [ ] Swagger, k6 부하테스트
