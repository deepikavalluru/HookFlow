# HookFlow 🚀
### A Reliable Webhook Delivery & Monitoring Platform

[![Backend Tests](https://img.shields.io/badge/Backend_Tests-100_Passing-success)](https://github.com/DeepikaValluru/HookFlow/actions)
[![Frontend](https://img.shields.io/badge/Frontend-React%20%2B%20Vite-blue)](https://github.com/DeepikaValluru/HookFlow)

**Live Demo:** https://hookflow-frontend-five.vercel.app/  
**Backend:** https://hookflow-backend.onrender.com

HookFlow is a webhook delivery and monitoring platform built with Java, Spring Boot, and React. It enables users to register webhook endpoints, submit events, deliver those events asynchronously, automatically retry eligible failures, and monitor delivery activity from a dashboard.

The platform addresses a common backend engineering challenge: how can applications reliably communicate with external services when network failures, timeouts, or temporary server outages interrupt webhook delivery?

Instead of treating a failed HTTP request as the end of the process, HookFlow records delivery attempts, schedules retries, and maintains delivery history.

---

## 1. Problem Statement

Modern applications use webhooks to notify external systems when important events occur. For example, an e-commerce application may notify an inventory service when an order is placed, or a payment application may notify another service when a transaction succeeds.

Webhook delivery can fail when:
- The receiving server is temporarily unavailable.
- A network connection times out.
- The receiving application returns an HTTP 5xx response.
- The destination is temporarily overloaded.

A basic implementation that sends a single HTTP request may lose an event when the receiving service is unavailable. Without delivery history and failure visibility, developers may also struggle to identify and recover failed requests.

HookFlow addresses these challenges with asynchronous delivery, automatic retries, persistent attempt history, and centralized monitoring.

---

## 2. Proposed Solution

HookFlow provides a centralized platform for managing webhook endpoints and tracking event delivery.

Users can register endpoints, configure the event types they want to receive, and submit events through REST APIs. HookFlow creates delivery records for matching endpoints and processes deliveries asynchronously.

When a delivery encounters a retryable failure, HookFlow schedules another attempt using a configurable backoff schedule. Each attempt is recorded so users can inspect its outcome. A dashboard summarizes delivery activity, and users can manually retry failed deliveries.

The delivery guarantee is **at-least-once**, not exactly-once. A destination may receive a duplicate if it processes a request but HookFlow does not receive the response.

---

## 3. Core Features

### A. Authentication and Authorization
- User registration and login.
- Password hashing using BCrypt.
- JWT-based authentication.
- Protected REST APIs.
- User-specific resource access.

### B. Webhook Endpoint Management
- Register webhook endpoints.
- Configure endpoint URLs and event types.
- Generate unique webhook secrets.
- View and update registered endpoints.
- Activate or deactivate endpoints.
- Delete endpoints.

### C. Event Management
- Submit events through REST APIs.
- Associate events with registered webhook endpoints.
- Store event payloads in PostgreSQL.
- Generate unique event identifiers.
- Create delivery records for matching endpoints.

### D. Asynchronous Webhook Delivery
- Send event payloads using HTTP POST requests.
- Process webhook deliveries asynchronously.
- Capture HTTP response status codes.
- Handle connection failures and request timeouts.
- Maintain individual delivery-attempt records.

### E. Automatic Retry Mechanism

HookFlow automatically schedules another attempt when a delivery fails due to a retryable condition.

| Attempt | Delay |
|---|---:|
| 1 | Immediate |
| 2 | 5 seconds |
| 3 | 10 seconds |
| 4 | 20 seconds |
| 5 | 40 seconds |

The retry delays are configurable.

**Retryable failures:**
- HTTP 429.
- HTTP 5xx.
- Connection failures.
- Request timeouts.

Permanent failures such as HTTP 400, 401, and 404 are not automatically retried. After the configured maximum number of attempts is exhausted, the delivery is marked as `FAILED`.

### F. Delivery Monitoring and Manual Retry
- View total events and delivery counts.
- Track successful, failed, and pending deliveries.
- Inspect individual delivery details.
- View delivery-attempt history.
- Inspect HTTP response codes, error details, and timestamps.
- Manually retry failed deliveries.

---

## 4. Technology Stack

| Category | Technologies |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Security | Spring Security |
| Authentication | JWT, BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| HTTP Client | Spring WebClient |
| Scheduling and Async Processing | Spring Scheduling, Spring Async |
| Build Tool | Maven |
| Backend Testing | JUnit 5, Mockito |
| Frontend | React 19, Vite |
| Styling | Tailwind CSS |
| HTTP Requests | Axios |
| Charts | Recharts |
| Icons | lucide-react |
| API Documentation | Swagger / OpenAPI |
| Containerization | Docker, Docker Compose |
| Web Server / Reverse Proxy | Nginx |
| CI | GitHub Actions |
| Deployment | Render, Vercel |
| Version Control | Git, GitHub |

---

## 5. System Architecture

HookFlow uses a layered Spring Boot backend with a React frontend.

```text
                   React Frontend
                         |
                         | REST API requests
                         v
                  Spring Boot Backend
                         |
          +--------------+---------------+
          |              |               |
       Security      Controllers      Services
                         |               |
                         |          Delivery Logic
                         |               |
                    Repositories    Scheduler / Async
                         |               |
                    JPA / Hibernate     WebClient
                         |               |
                     PostgreSQL    External Endpoints
