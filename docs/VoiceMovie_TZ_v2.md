# VoiceMovie — техническое задание (MVP)

**Цель:** Spring Boot backend, который по голосовому или текстовому описанию подбирает фильмы: распознаёт речь через AssemblyAI, извлекает фильтры с помощью Claude Haiku и ищет фильмы через TMDB.

**Стек:** Java 21+, Spring Boot, Spring Web MVC, Spring Data JPA, PostgreSQL, Flyway, Spring Scheduler, AWS SDK for Java v2, SeaweedFS (S3 API), Docker Compose. Frontend отдельно. Авторизация в MVP не нужна.

## 1. Модели данных (JPA Entity)

### `SearchJob` — задача поиска

| Поле | Тип / смысл |
|---|---|
| `id` | UUID, PK |
| `inputType` | enum `VOICE` / `TEXT` |
| `status` | enum состояния задачи |
| `objectKey` | nullable String — ключ аудиофайла в S3 |
| `transcriptId` | nullable String — ID задачи в AssemblyAI |
| `text` | nullable TEXT — исходный текст или транскрипция |
| `criteria` | nullable JSONB — критерии, полученные от Haiku |
| `retryCount`, `nextRetryAt` | счётчик и время следующей попытки |
| `errorMessage` | nullable TEXT — причина ошибки |
| `createdAt`, `updatedAt` | даты создания и изменения |

Статусы: `PENDING → TRANSCRIBING → EXTRACTING_CRITERIA → SEARCHING_MOVIES → COMPLETED`. Из рабочих состояний возможен переход в `FAILED`.

Для текстового запроса шаг `TRANSCRIBING` пропускается. Аудиофайл не хранится в БД: только `objectKey`.

### `SearchResult` — фильм в выдаче задачи

| Поле | Тип / смысл |
|---|---|
| `id` | PK |
| `job` | связь Many-to-One с `SearchJob` |
| `tmdbMovieId` | ID фильма в TMDB |
| `title`, `description` | название и описание |
| `rating` | рейтинг TMDB |
| `releaseDate` | дата выхода |
| `posterPath` | путь к постеру в TMDB |
| `position` | порядок фильма в выдаче |

У одной задачи может быть несколько результатов. Схему БД создавать миграциями Flyway.

## 2. REST API нашего backend

Base path: `/api/v1/search`. Все ответы — JSON, кроме загрузки файла.

### `POST /voice` — голосовой поиск

`Content-Type: multipart/form-data`, поле `file` (например, `.webm`, `.mp3`, `.wav`).

Backend сохраняет аудио в S3, создаёт задачу и возвращает **HTTP 202 Accepted**:

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING"
}
```

### `POST /text` — текстовый поиск

```json
{
  "text": "Хочу фантастику про космос после 2015 года с рейтингом от 7"
}
```

Ответ: **HTTP 202 Accepted**, тот же формат `jobId` + `status`. Обработка начинается сразу с извлечения критериев.

### `GET /{jobId}` — состояние и результат

Пока задача выполняется:

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "TRANSCRIBING",
  "text": null,
  "criteria": null,
  "movies": [],
  "errorMessage": null
}
```

После завершения (пример условный, не реальная выдача TMDB):

```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "text": "Хочу фантастику про космос после 2015 года",
  "criteria": {
    "title": null,
    "genres": ["science_fiction"],
    "releaseYearFrom": 2015,
    "releaseYearTo": null,
    "minRating": 7.0,
    "maxDurationMinutes": null,
    "keywords": ["space"],
    "sortBy": "popularity.desc"
  },
  "movies": [
    {
      "id": 157336,
      "title": "Interstellar",
      "description": "Описание фильма",
      "rating": 8.4,
      "releaseDate": "2014-11-05",
      "posterUrl": "https://image.tmdb.org/t/p/w500/example.jpg"
    }
  ],
  "errorMessage": null
}
```

При `FAILED` возвращать статус задачи и `errorMessage`. Для несуществующего `jobId` — HTTP 404. Отсутствие подходящих фильмов означает `COMPLETED` с пустым `movies`, а не ошибку.

### `GET /history?page=0&size=20` — история поисков

Постраничный список задач: `jobId`, `inputType`, `status`, `text`, `createdAt`. Без авторизации история в MVP общая.

## 3. Внешние интеграции

### 3.1. SeaweedFS — хранение аудио

- Docker image: `chrislusf/seaweedfs:4.48`; запуск локально с S3 gateway (`weed server -s3`), постоянным Docker volume и S3 credentials.
- Bucket: `voice-recordings`; объект: `voice/{jobId}.{extension}`.
- Для Spring Boot: **AWS SDK for Java v2 / S3Client**, endpoint `http://localhost:8333` при запуске backend на хосте или `http://seaweedfs:8333` из Docker Compose, `region=us-east-1`, `pathStyleAccessEnabled=true`.
- Создать bucket при первоначальной настройке. В PostgreSQL сохранять только `objectKey`.
- **Важно:** локальный S3 URL недоступен AssemblyAI из интернета. Worker скачивает аудио из SeaweedFS и загружает байты через AssemblyAI Upload API.

Настройка и примеры: [SeaweedFS S3 Configuration](https://github.com/seaweedfs/seaweedfs/wiki/S3-Configuration), [SeaweedFS GitHub](https://github.com/seaweedfs/seaweedfs), [AWS SDK S3Client](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-s3.html).

### 3.2. AssemblyAI — асинхронная транскрипция

**Base URL:** `https://api.assemblyai.com`

| Метод | Endpoint | Назначение |
|---|---|---|
| `POST` | `/v2/upload` | Загрузить байты аудио, получить `upload_url` |
| `POST` | `/v2/transcript` | Создать задачу по `audio_url`, получить `id` |
| `GET` | `/v2/transcript/{id}` | Проверить статус, получить текст |

Короткий пример запроса на транскрипцию:

```json
{
  "audio_url": "https://cdn.assemblyai.com/upload/example",
  "speech_models": ["universal-3-5-pro", "universal-2"],
  "language_detection": true
}
```

Статусы внешней транскрипции: `queued`, `processing`, `completed`, `error`. При `completed` использовать поле `text`.

Документация и ключ: [AssemblyAI Docs](https://www.assemblyai.com/docs), [AssemblyAI Dashboard](https://www.assemblyai.com/dashboard).

### 3.3. Anthropic Claude Haiku — извлечение фильтров

**Endpoint:** `POST https://api.anthropic.com/v1/messages`.

Вход — текст пользователя. Выход — JSON критериев, например:

```json
{
  "title": null,
  "genres": ["science_fiction"],
  "releaseYearFrom": 2015,
  "releaseYearTo": null,
  "minRating": 7.0,
  "maxDurationMinutes": null,
  "keywords": ["space"],
  "sortBy": "popularity.desc"
}
```

Использовать **Structured Outputs** (`output_config.format` с JSON Schema), модель семейства Haiku указать конфигурацией. Не выдумывать фильтры, которые пользователь не называл. Haiku **не вызывает TMDB**, а только формирует критерии.

Документация: [Messages API](https://platform.claude.com/docs/en/api/messages), [Structured Outputs](https://platform.claude.com/docs/en/build-with-claude/structured-outputs). Ключ: [Claude Console](https://platform.claude.com/).

### 3.4. TMDB — поиск фильмов

**Base URL:** `https://api.themoviedb.org/3`. Авторизация: `Authorization: Bearer <API Read Access Token>`.

| Метод | Endpoint | Назначение |
|---|---|---|
| `GET` | `/genre/movie/list` | Справочник жанров и их ID |
| `GET` | `/search/keyword` | Поиск ID ключевых слов |
| `GET` | `/search/movie` | Поиск по названию фильма |
| `GET` | `/discover/movie` | Поиск по фильтрам и сортировка |

Пример запроса к Discover:

```http
GET /3/discover/movie?with_genres=878&primary_release_date.gte=2015-01-01&vote_average.gte=7&sort_by=popularity.desc&page=1
```

Задача backend — преобразовать критерии Haiku в параметры TMDB: `science_fiction → genre ID 878`; `keywords` → TMDB keyword IDs; год → даты; ограничение длительности → `with_runtime.lte`. Если `title` задан, применить `/search/movie`. Поддержать пагинацию и пустую выдачу. TMDB не гарантирует семантический поиск по свободному описанию сюжета; AI-ранжирование найденных фильмов пока **не входит в MVP**.

Документация: [Discover Movie](https://developer.themoviedb.org/reference/discover-movie), [Search Movie](https://developer.themoviedb.org/reference/search-movie), [Genre List](https://developer.themoviedb.org/reference/genre-movie-list), [Search Keyword](https://developer.themoviedb.org/reference/search-keyword). Ключ: [TMDB API Getting Started](https://developer.themoviedb.org/docs/getting-started).

## 4. Полная цепочка обработки

```text
Frontend
  ├─ POST /api/v1/search/text ────────────────┐
  └─ POST /api/v1/search/voice → SeaweedFS ──┤
                                              ↓
                                     SearchJob: PENDING
                                              ↓
                                      Scheduled Worker
                                              ↓
                         VOICE? → AssemblyAI: TRANSCRIBING
                                              ↓
                           Claude Haiku: EXTRACTING_CRITERIA
                                              ↓
                              TMDB: SEARCHING_MOVIES
                                              ↓
                            Save SearchResult → COMPLETED
                                              ↓
                            GET /api/v1/search/{jobId}
```

- Фоновый worker берёт ожидающие задачи и продолжает уже начатые; проверку AssemblyAI проводить через polling, **не блокируя поток до завершения**.
- Состояние и внешние идентификаторы сохраняются, чтобы после перезапуска не начинать всё с нуля.
- Небольшой retry для временных сбоев; постоянная ошибка → `FAILED`. Предусмотреть защиту от одновременной обработки одной задачи.
- Отдельная периодическая очистка старых аудиофайлов из S3; результаты поиска остаются в БД.
- Внешние токены и S3 credentials передавать через конфигурацию / переменные окружения, не коммитить секреты.

## 5. В каком порядке реализовывать

1. **Каркас:** Spring Boot, PostgreSQL, Flyway, JPA сущности, Docker Compose с SeaweedFS.
2. **Текстовый MVP:** `POST /text` → Haiku → TMDB → сохранение и `GET /{jobId}`. Проверить, что подбор фильмов работает без аудио.
3. **Голос:** `POST /voice`, загрузка аудио в S3, AssemblyAI Upload/Transcript, polling.
4. **Фоновые задачи:** все статусы, восстановление после рестарта, базовый retry и очистка старых файлов.
5. **Завершение:** история запросов, Swagger/OpenAPI и тесты основных сценариев (включая ошибки внешних API).

**Критерий готовности:** текстовые и голосовые запросы создают задачи, их статусы доступны по `jobId`, транскрипция и фильтры сохраняются, найденные фильмы возвращаются в JSON, данные переживают перезапуск.

**Не делаем в MVP:** frontend, JWT/пользователей, Kafka, Redis, WebSocket, streaming STT, отдельные микросервисы.
