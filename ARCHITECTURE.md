# Architecture Documentation — Task Manager

## Table of Contents
- [System Overview](#system-overview)
- [Architecture Principles](#architecture-principles)
- [Component Architecture](#component-architecture)
- [Data Model](#data-model)
- [API Design](#api-design)
- [Security](#security)
- [Performance Considerations](#performance-considerations)
- [Testing Strategy](#testing-strategy)
- [Deployment Architecture](#deployment-architecture)

---

## System Overview

Task Manager is a modern full-stack web application following best practices for enterprise Java and React development.

### Technology Stack

**Frontend**
- React 18.2 with TypeScript
- Vite 5.0 (build tool)
- Native fetch API for HTTP requests
- CSS3 for styling

**Backend**
- Spring Boot 3.2.0
- Java 17 (LTS)
- Spring Data JPA
- Jakarta Bean Validation
- SLF4J + Logback for logging

**Database**
- H2 (development - in-memory)
- PostgreSQL (production - recommended)

**Testing**
- JUnit 5 (unit & integration tests)
- Playwright (E2E tests)
- JaCoCo (code coverage)

---

## Architecture Principles

### 1. **Separation of Concerns**
Each layer has a single responsibility:
- **Controller**: HTTP handling, request/response mapping
- **Service**: Business logic, transaction management
- **Repository**: Data access, queries
- **Entity**: Domain model, persistence mapping

### 2. **Dependency Injection**
- Constructor-based injection (immutable dependencies)
- No field injection
- Clear dependencies visible in constructors

### 3. **SOLID Principles**
- Single Responsibility: Each class has one reason to change
- Open/Closed: Extensible without modification
- Liskov Substitution: Interfaces over implementations
- Interface Segregation: Small, focused interfaces
- Dependency Inversion: Depend on abstractions

### 4. **API-First Design**
- RESTful endpoints
- JSON as data format
- Proper HTTP status codes
- Structured error responses

---

## Component Architecture

### Backend Layered Architecture