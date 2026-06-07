# order-processor

[![build](https://github.com/Mohan91002/order-processor/actions/workflows/build.yml/badge.svg)](https://github.com/Mohan91002/order-processor/actions/workflows/build.yml)

A single Spring Boot 3.2 (Java 17) application built as a cohesive **order processing pipeline** that, in service of real flows, exercises:

- almost every `java.util.concurrent` primitive,
- most everyday Spring features (Boot, MVC, Data JPA, Security/JWT, AOP, Cache, Async, Scheduling, Retry, Validation, Application Events, Actuator + Micrometer, WebClient, `@ConfigurationProperties`),
- a full Kafka loop with KRaft (producer → topic → multiple consumers → DLT → confirmation).

Drop the folder into IntelliJ on any machine, `docker compose up`, then `./mvnw spring-boot:run`.

---

## 1. Run it

```bash
# 1. Start Kafka (KRaft, single broker), Kafka UI, and Postgres
docker compose up -d

# 2. Run the app (dev profile = H2 in-memory; prod profile = Postgres)
./mvnw spring-boot:run

# 3. (alternative) prod profile against the dockerised Postgres
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Open:
- App:        http://localhost:8080
- H2 console: http://localhost:8080/h2  (JDBC URL `jdbc:h2:mem:orderprocessor`, user `sa`, no pw)
- Swagger:    http://localhost:8080/swagger-ui.html
- Kafka UI:   http://localhost:8085
- Actuator:   http://localhost:8080/actuator/health, /actuator/prometheus, /actuator/metrics

Seeded admin login: `admin / admin123` (role `ADMIN`) and `staff / admin123` (role `STAFF`).

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .accessToken)

# Create an order — triggers the full pipeline: DB → Spring event → Kafka → inventory + enrichment → confirmation
curl -X POST localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"customerEmail":"a@b.com","items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'

curl -s -H "Authorization: Bearer $TOKEN" localhost:8080/api/orders/1 | jq
curl -s -H "Authorization: Bearer $TOKEN" localhost:8080/api/stats/counters | jq
```

Trigger any single concurrency demo (no auth needed):

```
GET /api/demo/executor
GET /api/demo/callable-future
GET /api/demo/completable-future
GET /api/demo/scheduled-executor
GET /api/demo/fork-join
GET /api/demo/reentrant-lock
GET /api/demo/read-write-lock
GET /api/demo/stamped-lock
GET /api/demo/semaphore
GET /api/demo/count-down-latch
GET /api/demo/cyclic-barrier
GET /api/demo/phaser
GET /api/demo/exchanger
GET /api/demo/atomics
GET /api/demo/concurrent-collections
GET /api/demo/blocking-queue
GET /api/demo/producer-consumer
GET /api/demo/synchronized-volatile
GET /api/demo/thread-local
GET /api/demo/deadlock?force=false
```

---

## 2. The pipeline (where the topics meet)

```
POST /api/orders
        │
        ▼
 OrderService.createOrder()         ── @Transactional, ApplicationEventPublisher
        │     (writes Order + Items, increments AtomicLong counters)
        ▼
 OrderCreatedDomainEvent  ── Spring application event
        │     @TransactionalEventListener(AFTER_COMMIT)
        ▼
 OrderEventListener  ──────────────  publishes to Kafka
        │
        ▼
 Kafka topic: orders.created (3 partitions)
        ├── consumer group "inventory-consumer"     → InventoryService (ReentrantLock + JPA pessimistic decrement)
        └── consumer group "enrichment-consumer"    → EnrichmentService (CompletableFuture + Semaphore)
                                                          │
                                                          ▼
                                                Kafka topic: orders.enriched
                                                          │
                                                          ▼
                                            "confirmation-consumer"
                                                          │   marks status CONFIRMED
                                                          ▼
                                            NotificationService (BlockingQueue + fixed pool)

   Failures → DefaultErrorHandler retries → DeadLetterPublishingRecoverer → orders.created.DLT
                                                                                │
                                                                                ▼
                                                                          DltConsumer (logs)
```

---

## 3. Multi-threading topics map

| Topic | Used in real flow | Standalone demo |
|---|---|---|
| `Thread` / `Runnable` | every executor | every demo |
| `Callable` / `Future` | many places | `concurrency/CallableFutureDemo.java` |
| `ExecutorService` (fixed/cached/single) | `NotificationService`, demos | `concurrency/ExecutorDemo.java` |
| `ThreadPoolExecutor` tuning + `RejectedExecutionHandler` | `config/AsyncConfig.java` (CallerRunsPolicy) | — |
| `ScheduledExecutorService` | — | `concurrency/ScheduledExecutorDemo.java` |
| `CompletableFuture` (supplyAsync, thenCombine, allOf, exceptionally) | `service/EnrichmentService.java`, `kafka/OrderEnrichmentConsumer.java` | `concurrency/CompletableFutureDemo.java` |
| `ForkJoinPool` + `RecursiveTask` | `service/ReportingService.java` | `concurrency/ForkJoinDemo.java` |
| `ReentrantLock` + `Condition` | `service/InventoryService.java` (per-product lock) | `concurrency/ReentrantLockDemo.java` |
| `ReentrantReadWriteLock` | — | `concurrency/ReadWriteLockDemo.java` |
| `StampedLock` (optimistic read) | `service/PricingService.java` | `concurrency/StampedLockDemo.java` |
| `Semaphore` | `service/EnrichmentService.java` (external-call gate) | `concurrency/SemaphoreDemo.java` |
| `CountDownLatch` | — | `concurrency/CountDownLatchDemo.java` |
| `CyclicBarrier` | — | `concurrency/CyclicBarrierDemo.java` |
| `Phaser` | — | `concurrency/PhaserDemo.java` |
| `Exchanger` | — | `concurrency/ExchangerDemo.java` |
| `AtomicInteger` / `LongAdder` / `AtomicReference` | `service/StatsService.java` | `concurrency/AtomicsDemo.java` |
| `ConcurrentHashMap` (`compute`/`merge`) | `service/InventoryService.java` | `concurrency/ConcurrentCollectionsDemo.java` |
| `CopyOnWriteArrayList` | — | `concurrency/ConcurrentCollectionsDemo.java` |
| `ArrayBlockingQueue` (bounded) | — | `concurrency/BlockingQueueDemo.java` |
| `LinkedBlockingQueue` + worker pool | `service/NotificationService.java` | — |
| `synchronized` + `wait/notifyAll` | — | `concurrency/ProducerConsumerWaitNotifyDemo.java` |
| `volatile` flag | `service/NotificationService.java` (`running`) | `concurrency/SynchronizedVolatileDemo.java` |
| `ThreadLocal` (with cleanup) | — | `concurrency/ThreadLocalDemo.java` |
| Deadlock (and ordered-lock fix) | — | `concurrency/DeadlockDemo.java` |
| Graceful shutdown (`@PreDestroy`, `awaitTermination`) | `service/NotificationService.java` | — |

---

## 4. Spring topics map

| Topic | Where |
|---|---|
| `@SpringBootApplication` + auto-config | `OrderProcessorApplication.java` |
| `@ConfigurationProperties` (records) | `config/AppProperties.java` |
| Profiles (`dev` / `prod` / `test`) | `application*.yml` |
| Spring MVC + REST controllers | `controller/*` |
| `@Valid` + custom validator | `dto/CreateOrderRequest.java`, `validation/PositiveQuantity.java` |
| `@RestControllerAdvice` | `controller/ApiExceptionHandler.java` |
| Spring Data JPA repositories + `@Query` + `@Modifying` + `@Lock(PESSIMISTIC_WRITE)` | `repo/*` |
| `@Transactional` / `@Transactional(readOnly = true)` | `service/*` |
| Spring Security (stateless JWT + RBAC + `@PreAuthorize`) | `security/*`, `controller/OrderController.java` |
| Spring AOP (`@Before`, `@AfterReturning`, `@AfterThrowing`, `@Around`) | `aop/LoggingAspect.java`, `aop/TimingAspect.java` |
| Spring Cache (`@EnableCaching`, `@Cacheable`, `@CacheEvict`) | `config/CachingConfig.java`, `service/ProductService.java`, `service/PricingService.java` |
| Spring Async (`@EnableAsync` + custom `ThreadPoolTaskExecutor`) | `config/AsyncConfig.java`, `service/EnrichmentService.java` |
| `AsyncUncaughtExceptionHandler` | `config/AsyncConfig.java` |
| Spring Scheduling (`@Scheduled` cron + fixedDelay) + `ThreadPoolTaskScheduler` | `config/SchedulingConfig.java`, `scheduled/*` |
| Spring Retry (`@Retryable` + `@Recover`) | `client/ShippingClient.java`, `config/RetryConfig.java` |
| Spring Application Events + `@TransactionalEventListener(AFTER_COMMIT)` | `events/OrderEventListener.java` |
| Flyway migrations | `src/main/resources/db/migration/V1__init.sql` |
| Actuator + custom `HealthIndicator` + Micrometer/Prometheus | `actuator/OrdersHealthIndicator.java`, `aop/TimingAspect.java` |
| WebClient (reactive HTTP) used from MVC | `client/ShippingClient.java`, `config/WebClientConfig.java` |
| `@DataJpaTest`, `@SpringBootTest`, `@EmbeddedKafka`, MockMvc + Spring Security Test | `src/test/java/**` |
| Swagger / OpenAPI | dependency `springdoc-openapi-starter-webmvc-ui` → `/swagger-ui.html` |

---

## 5. Kafka topics map

| Topic | Where |
|---|---|
| `KafkaTemplate.send` returning `CompletableFuture<SendResult>` | `kafka/OrderEventProducer.java` |
| `@KafkaListener` (3 consumer groups on `orders.created`) | `kafka/OrderCreatedConsumer.java`, `kafka/OrderEnrichmentConsumer.java`, `kafka/OrderEnrichedConsumer.java` |
| Manual ack (`AckMode.MANUAL_IMMEDIATE`) | `config/KafkaConfig.java`, all consumers |
| Concurrency on listener container | `config/KafkaConfig.java` (`factory.setConcurrency(3)`) |
| `DefaultErrorHandler` + `FixedBackOff` (retry) | `config/KafkaConfig.java` |
| Dead Letter Topic (`DeadLetterPublishingRecoverer`) | `config/KafkaConfig.java` + `kafka/DltConsumer.java` |
| Idempotent + acked producer | `application.yml` (`enable.idempotence=true`, `acks=all`) |
| JSON serializer/deserializer (`JsonSerializer`, `ErrorHandlingDeserializer`) | `application.yml` |
| Topic auto-creation via `NewTopic` beans | `config/KafkaConfig.java` |
| `EmbeddedKafkaBroker` for tests | `OrderProcessorApplicationTests`, `DemoControllerTest` |

---

## 6. Project layout

```
order-processor/
├── pom.xml
├── docker-compose.yml                 ← Kafka (KRaft) + Kafka UI + Postgres
├── mvnw, mvnw.cmd, .mvn/wrapper/
├── .github/workflows/build.yml        ← CI: mvn verify on every push
└── src/main/java/com/learning/orderprocessor/
    ├── OrderProcessorApplication.java
    ├── config/        ← AppProperties, AsyncConfig, SchedulingConfig, CachingConfig, RetryConfig, KafkaConfig, WebClientConfig
    ├── security/      ← JwtService, JwtAuthFilter, SecurityConfig
    ├── domain/        ← @Entity classes (Order, OrderItem, Product, AppUser, OrderStatus)
    ├── repo/          ← Spring Data JPA repositories
    ├── dto/           ← request/response records + Bean Validation
    ├── validation/    ← @PositiveQuantity custom constraint
    ├── service/       ← OrderService, InventoryService, PricingService, EnrichmentService, NotificationService, StatsService, ReportingService, ProductService
    ├── concurrency/   ← 20 standalone demos + DemoController
    ├── kafka/         ← producer + 3 consumers + DLT consumer + event records
    ├── events/        ← Spring application event + AFTER_COMMIT bridge
    ├── aop/           ← LoggingAspect, TimingAspect
    ├── scheduled/     ← @Scheduled jobs
    ├── actuator/      ← custom HealthIndicator
    ├── client/        ← WebClient + @Retryable
    └── controller/    ← REST controllers + @RestControllerAdvice
```

---

## 7. Cheat sheet: the wiring you'll be quizzed on

- The `appTaskExecutor` bean in `AsyncConfig` is **the one** that backs `@Async("appTaskExecutor")` calls and the `Executor` injected into `EnrichmentService`. Tune `app.executor.*` in `application.yml`.
- All `@TransactionalEventListener(AFTER_COMMIT)` listeners run on the publishing thread *after* the JPA tx commits — so the Kafka send happens *only* if the order is persisted.
- `DefaultErrorHandler` retries each `@KafkaListener` invocation per `FixedBackOff` then hands the record to the DLT recoverer.
- `MANUAL_IMMEDIATE` ack means a consumer that throws **does not** commit the offset — the broker hands the same record back, triggering retry → DLT.
- `EnrichmentService.enrich` returns `CompletableFuture<…>` and uses a `Semaphore(10)` to cap concurrent calls to the external shipping client.
