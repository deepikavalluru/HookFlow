# HookFlow 🚀
### A Reliable Webhook Delivery & Monitoring Platform

HookFlow is a lightweight webhook management platform built using Java and Spring Boot. It allows applications to register webhook endpoints, submit events, deliver those events to registered destinations, and automatically retry failed deliveries.

The primary goal is to solve a common backend engineering problem: **How do applications reliably communicate with external services when network failures or temporary server outages occur?**

Instead of losing events when a destination is unavailable, HookFlow manages delivery attempts, schedules retries, and maintains a complete delivery history.

---

## 1. Problem Statement

Modern applications frequently communicate with external systems through webhooks.

For example:
- An e-commerce application notifies an inventory service when an order is placed.
- A payment application notifies another service when a transaction succeeds.
- A notification service informs external applications about important events.

However, webhook delivery can fail because:
- The receiving server is temporarily unavailable.
- Network connections time out.
- The receiving application returns HTTP 500 errors.
- The destination experiences temporary overload.

Most basic implementations simply send an HTTP request and consider the operation complete.

HookFlow addresses this problem by introducing reliable event delivery, automatic retries, and delivery monitoring.

---

## 2. Core Features

### A. User Authentication
- User registration and login.
- Password encryption using BCrypt.
- JWT-based authentication.
- Protected REST APIs.
- Users can manage only their own webhook resources.

### B. Webhook Endpoint Management
- Register webhook endpoints.
- Configure endpoint URLs and event types.
- Generate unique webhook secrets.
- View registered endpoints.
- Update endpoint configurations.
- Activate or deactivate endpoints.
- Delete endpoints.

### C. Event Management
- Accept events through REST APIs.
- Associate events with registered webhook endpoints.
- Store event payloads in PostgreSQL.
- Generate unique event identifiers.
- Create individual delivery records for matching endpoints.

### D. Webhook Delivery
- Send events using HTTP POST requests.
- Deliver events asynchronously.
- Capture HTTP response status codes.
- Handle connection failures and request timeouts.
- Maintain individual delivery attempt records.

### E. Automatic Retry Mechanism

When a delivery fails because of a temporary problem, HookFlow automatically schedules another attempt.

Retry strategy:

| Attempt | Delay |
|---|---|
| 1 | Immediate |
| 2 | 5 seconds |
| 3 | 10 seconds |
| 4 | 20 seconds |
| 5 | 40 seconds |

The delays are configurable.

Retryable failures:
- HTTP 429.
- HTTP 5xx.
- Connection failures.
- Request timeouts.

Permanent failures such as HTTP 400, 401 and 404 will not be retried automatically.

After exhausting the maximum attempts, the delivery is marked as FAILED.

### F. Delivery Monitoring

Users can monitor:
- Total events.
- Successful deliveries.
- Failed deliveries.
- Pending deliveries.
- Delivery attempt count.
- HTTP response status.
- Error messages.
- Delivery timestamps.

Users can also manually retry failed deliveries.

---

## 3. Technology Stack

| Category | Technology |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Security | Spring Security |
| Authentication | JWT |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| HTTP Client | Spring WebClient |
| Scheduling | Spring Scheduling |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |
| Containerization | Docker |
| Frontend | React |
| API Documentation | Swagger / OpenAPI |
| Version Control | Git + GitHub |

---

## 4. System Architecture

The application follows a layered architecture.

```text
             React Frontend
                   |
                   |
             REST API Layer
                   |
          Spring Boot Backend
                   |
       -------------------------
       |           |           |
    Security    Services   Controllers
                   |
             Repositories
                   |
               JPA/Hibernate
                   |
               PostgreSQL
                   |
          Delivery Scheduler
                   |
             HTTP Client
                   |
          External Webhooks
```

### Event Delivery Workflow

```text
External Application
        |
        | POST /api/events
        |
        v
   HookFlow API
        |
        v
   Validate Event
        |
        v
   Save Event
        |
        v
 Find Matching Endpoints
        |
        v
 Create Delivery Records
        |
        v
  Delivery Scheduler
        |
        v
  Send HTTP POST
        |
        v
  Check Response
        |
    +---+---+
    |       |
  Success  Failure
    |       |
    v       v
 DELIVERED  Retry Scheduler
                |
          Retry Limit Reached?
                |
           +----+----+
           |         |
          No        Yes
           |         |
           v         v
        Retry      FAILED
```

---

## 5. Database Design

The application will initially use four primary entities.

### User
- id
- name
- email
- password
- createdAt

### WebhookEndpoint
- id
- userId
- name
- url
- secret
- eventType
- active
- createdAt

### Event
- id
- eventType
- payload
- createdAt

### Delivery
- id
- eventId
- endpointId
- status
- attemptCount
- nextAttemptAt
- lastResponseCode
- lastError
- createdAt
- updatedAt

Each delivery attempt should also have a separate record containing its attempt number, timestamp, response status, response body where appropriate, and error details.

Use appropriate foreign keys, indexes, database constraints, and transactions.

---

## 6. REST API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Authenticate user |

### Webhook Management

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/webhooks | Register endpoint |
| GET | /api/webhooks | List endpoints |
| GET | /api/webhooks/{id} | Get endpoint |
| PUT | /api/webhooks/{id} | Update endpoint |
| PATCH | /api/webhooks/{id}/status | Activate/deactivate |
| DELETE | /api/webhooks/{id} | Delete endpoint |

### Event Management

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/events | Submit event |
| GET | /api/events | List events |
| GET | /api/events/{id} | Get event details |

### Delivery Management

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/deliveries | List deliveries |
| GET | /api/deliveries/{id} | Get delivery details |
| GET | /api/deliveries/{id}/attempts | View attempt history |
| POST | /api/deliveries/{id}/retry | Manually retry failed delivery |

All resource endpoints must enforce user ownership.

---

## 7. Project Structure

```text
hookflow/
│
├── src/main/java/com/hookflow/
│   │
│   ├── config/
│   │
│   ├── controller/
│   │
│   ├── service/
│   │
│   ├── repository/
│   │
│   ├── entity/
│   │
│   ├── dto/
│   │
│   ├── security/
│   │
│   ├── scheduler/
│   │
│   ├── exception/
│   │
│   └── HookFlowApplication.java
│
├── src/main/resources/
│   └── application.properties
│
├── src/test/
│
├── frontend/
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 8. Important Engineering Considerations

The MVP should demonstrate proper backend engineering practices.

1. Use DTOs instead of exposing database entities directly.
2. Implement centralized exception handling.
3. Validate all incoming requests.
4. Use database transactions where necessary.
5. Prevent users from accessing other users' resources.
6. Implement configurable connection and response timeouts.
7. Persist delivery attempts and retry schedules.
8. Avoid duplicate delivery creation for the same event and endpoint.
9. Use database indexes for frequently queried fields.
10. Protect against SSRF by validating destination URLs and blocking localhost, private IP ranges, and internal network destinations.
11. Never expose webhook secrets or sensitive response data through APIs.
12. Use appropriate logging.

The delivery guarantee is at-least-once, not exactly-once. A destination might receive a duplicate if it processes a request but HookFlow fails to receive its response.

---

## 9. Testing

Test the following scenarios:

- Successful webhook delivery.
- Receiving server returns HTTP 500.
- Receiving server returns HTTP 400.
- Connection timeout.
- Receiving server becomes available after a failure.
- Maximum retry attempts reached.
- Manual retry of failed delivery.
- Invalid webhook URL.
- Unauthorized API access.
- Duplicate event submission.
- Application restart with pending deliveries.

Use JUnit 5 and Mockito for unit tests and integration testing for the complete delivery workflow.

---

## 10. Deployment

The final application should support:

- Dockerized Spring Boot backend.
- PostgreSQL using Docker Compose.
- Environment-based configuration.
- Production database configuration.
- Swagger API documentation.
- Publicly accessible backend and frontend.

---

## 11. Out of Scope

To keep the project manageable as an individual development project, the following are intentionally excluded:

- Microservices architecture.
- Apache Kafka.
- RabbitMQ.
- Kubernetes.
- Distributed tracing.
- Complex distributed locking.
- Multi-region deployment.
- Enterprise billing and subscription management.

---

## 12. Expected Outcome

At completion, HookFlow should be a functional webhook reliability platform that demonstrates:

- Java backend development.
- Spring Boot application architecture.
- REST API development.
- Authentication and authorization.
- Relational database design.
- Asynchronous processing.
- HTTP communication.
- Automatic retry mechanisms.
- Failure handling.
- Unit and integration testing.
- Docker-based deployment.

**Project Goal:** Build a small, reliable, maintainable backend application that solves a genuine engineering problem and demonstrates practical Java and Spring Boot development skills.
