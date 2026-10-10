# URL Shortener

REST API для сокращения ссылок: регистрация, JWT-аутентификация, создание коротких ссылок, редирект и статистика кликов.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Build](https://github.com/AlexeyKlimanov/url-shortener/actions/workflows/ci.yml/badge.svg)](https://github.com/AlexeyKlimanov/url-shortener/actions)

## О проекте

Сервис, который превращает длинные URL в короткие коды вида `http://localhost:8080/aB3xY9q`. По короткой ссылке происходит редирект на оригинальный адрес, при этом каждый клик учитывается в статистике.

Проект реализован на **Java 21 + Spring Boot 4** с использованием **PostgreSQL**, **Flyway** для миграций и **JWT** для аутентификации.

## Стек технологий

| Категория | Технология |
|---|---|
| Язык | Java 21 |
| Фреймворк | Spring Boot 4.1.1 (Web, Data JPA, Security, Validation) |
| Сборка | Maven |
| БД | PostgreSQL 18.6 |
| Миграции | Flyway |
| Аутентификация | JWT (JJWT 0.12.6), BCrypt |
| Тестирование | JUnit 5, Mockito, AssertJ |
| CI | GitHub Actions |
| Утилиты | Lombok, Jackson |

## Функциональность

- ✅ Регистрация пользователя с валидацией email и пароля
- ✅ JWT-аутентификация (`POST /api/auth/login`)
- ✅ Создание коротких ссылок
- ✅ Редирект по короткому коду
- ✅ Список ссылок пользователя
- ✅ Счётчик кликов по каждой ссылке
- ✅ Дедупликация: один URL — одна ссылка в рамках пользователя
- ✅ Обработка ошибок: 400, 401, 404, 409

## API

### Аутентификация

| Метод | URL | Доступ | Описание |
|---|---|---|---|
| POST | `/api/auth/register` | Публичный | Регистрация нового пользователя |
| POST | `/api/auth/login` | Публичный | Логин, возвращает JWT |

### Ссылки

| Метод | URL | Доступ | Описание |
|---|---|---|---|
| POST | `/api/links` | Требует токен | Создать короткую ссылку |
| GET | `/api/links` | Требует токен | Список своих ссылок |
| GET | `/{code}` | Публичный | Редирект по короткому коду |

### Примеры запросов

**Регистрация:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Логин:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

→ 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**Создание ссылки:**
```http
POST /api/links
Authorization: Bearer <token>
Content-Type: application/json

{
  "originalUrl": "https://www.google.com"
}

→ 201 Created
{
  "id": 1,
  "shortCode": "aB3xY9q",
  "shortUrl": "http://localhost:8080/aB3xY9q",
  "originalUrl": "https://www.google.com",
  "clickCount": 0,
  "createdAt": "2026-10-10T14:30:00"
}
```

**Редирект:**
```http
GET /aB3xY9q

→ 302 Found
Location: https://www.google.com
```

## Запуск локально

### Требования

- Java 21+
- Maven 3.9+
- PostgreSQL 18.6

### 1. Клонировать репозиторий

```bash
git clone https://github.com/AlexeyKlimanov/url-shortener.git
cd url-shortener
```

### 2. Создать БД

```sql
CREATE DATABASE urlshortener;
```

### 3. Настроить секреты

Создать файл `src/main/resources/application-local.properties` (в `.gitignore`):

```properties
spring.datasource.password=ваш_пароль_от_postgres
JWT_SECRET=ваш_сгенерированный_ключ_в_base64
```

Сгенерировать `JWT_SECRET` (PowerShell):

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Max 256 }))
```

### 4. Запустить

```powershell
$env:SPRING_PROFILES_ACTIVE="local"; mvn spring-boot:run
```

Приложение будет доступно на `http://localhost:8080`.

Flyway **автоматически** применит миграции при старте.

## Структура проекта

```
src/main/java/io/github/AlexeyKlimanov/url_shortener/
├── config/
│   └── SecurityConfig.java            # Настройка Spring Security
├── controller/
│   ├── AuthController.java            # Регистрация, логин
│   ├── LinkController.java            # CRUD ссылок
│   └── LinkRedirectController.java    # Редирект по коду
├── dto/
│   ├── AuthResponse.java
│   ├── CreateLinkRequest.java
│   ├── LinkResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── UserResponse.java
├── entity/
│   ├── Link.java
│   └── User.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── LinkRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── RestAuthenticationEntryPoint.java
├── service/
│   ├── JwtService.java
│   ├── LinkService.java
│   ├── ShortCodeGenerator.java
│   └── UserService.java
└── UrlShortenerApplication.java
```

## Схема БД

```
┌─────────────────┐         ┌──────────────────────┐
│     users       │         │       links          │
├─────────────────┤         ├──────────────────────┤
│ id (PK)         │◄────────│ user_id (FK)         │
│ email (UNIQUE)  │         │ id (PK)              │
│ password_hash   │         │ short_code (UNIQUE)  │
│ created_at      │         │ original_url         │
└─────────────────┘         │ click_count          │
                            │ created_at           │
                            └──────────────────────┘
```

Миграции: `V1__create_users_table.sql`, `V2__create_links_table.sql`.

## Особенности реализации

- **JWT-аутентификация**: токен подписывается HMAC-SHA256, проверяется на каждом запросе через `JwtAuthenticationFilter`. Приложение работает в режиме `STATELESS` — HTTP-сессии не создаются.
- **BCrypt для паролей**: пароли хэшируются с автоматической солью. Плюс — защита от перебора за счёт медленности алгоритма.
- **Атомарный инкремент кликов**: `UPDATE links SET click_count = click_count + 1 WHERE id = ?` — корректно работает при конкурентных кликах, без race condition.
- **Генерация кодов**: `SecureRandom` (криптостойкий) + Base62-алфавит (a-z, A-Z, 0-9). 7 символов = ~3.5 триллиона комбинаций. Чёрный список зарезервированных слов (`error`, `login` и т.п.) защищает от конфликта с системными путями.
- **Дедупликация URL**: если пользователь повторно сокращает тот же URL — получает уже существующую ссылку.
- **Идемпотентная валидация схемы**: `ddl-auto=validate` — Hibernate сверяет сущности с БД на старте и падает при несоответствии. Flyway управляет схемой, Hibernate только проверяет.

## Тестирование

```bash
mvn test
```

Покрыто:

- `JwtServiceTest` — 6 тестов: генерация, извлечение claims, валидация токенов.
- `ShortCodeGeneratorTest` — 5 тестов: длина, формат, retry при коллизиях.
- `LinkServiceTest` — 8 тестов: создание, дедупликация, список, редирект, ошибки.

## Планы

- [ ] Refresh-токены
- [ ] Роли (`ROLE_USER`, `ROLE_ADMIN`)
- [ ] Rate limiting на `/api/auth/login` и `/api/links`
- [ ] Проверка доступности оригинального URL (HEAD-запрос)
- [ ] Кастомные короткие коды (`POST /api/links` с `customCode`)
- [ ] Интеграционные тесты с Testcontainers
- [ ] Dockerfile + docker-compose
- [ ] Деплой на Render / Railway

## Автор

**Алексей Климанов**
- GitHub: [@AlexeyKlimanov](https://github.com/AlexeyKlimanov)