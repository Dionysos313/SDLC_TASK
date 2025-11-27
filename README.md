# Task Manager

A full-stack Task Manager web application with React + TypeScript frontend, Spring Boot backend, and comprehensive testing including Playwright E2E tests.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen) ![React](https://img.shields.io/badge/React-18.2-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Overview

Task Manager is a modern, production-ready task management application featuring:

- **Frontend**: React 18 + TypeScript + Vite
- **Backend**: Spring Boot 3.2 + Java 17
- **Database**: H2 (in-memory) / PostgreSQL (production)
- **Testing**: JUnit 5, Playwright, JaCoCo coverage
- **Architecture**: Service layer, DTO pattern, RESTful API

---

## ✨ Features

### Core Functionality
- ✅ **CRUD Operations**: Create, Read, Update, Delete tasks
- ✅ **Status Management**: TODO, IN_PROGRESS, DONE
- ✅ **Due Date Tracking**: Set and track task deadlines
- ✅ **Overdue Detection**: Automatic identification of overdue tasks

### UI Features
- 🎨 **Grouping**: By status or due date
- 🔍 **Search**: Filter tasks by title
- 📊 **Sorting**: Ascending/descending by due date
- 📱 **Responsive Design**: Works on desktop and mobile

### Backend Features
- 🔒 **Validation**: Jakarta Bean Validation
- 📝 **Audit Trails**: Automatic timestamps (created/updated)
- 🎯 **Service Layer**: Separation of concerns
- 🚀 **Performance**: Database indexes on status and due date
- 📋 **Structured Errors**: Consistent error responses

### Testing
- 🧪 **Unit Tests**: Model validation, business logic
- 🔗 **Integration Tests**: Full Spring context with TestRestTemplate
- 🌐 **E2E Tests**: Playwright browser automation
- 📊 **Code Coverage**: JaCoCo reports

---

## 🏗️ Architecture

...