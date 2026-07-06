# YouTube Shorts Agent

Автоматизированное приложение для создания и публикации YouTube Shorts.

## Архитектура

```
Trend → Сценарий (OpenAI) → Озвучка (Edge TTS) → Стоковые видео (Pexels) → Монтаж (FFmpeg) → YouTube
```

## Требования

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- FFmpeg
- Python 3.10+ + `pip install edge-tts`

## Быстрый старт

### 1. Настройка переменных окружения

```bash
export OPENAI_API_KEY="sk-..."
export PEXELS_API_KEY="..."
export YOUTUBE_CLIENT_ID="..."
export YOUTUBE_CLIENT_SECRET="..."
export YOUTUBE_REFRESH_TOKEN="..."
export YOUTUBE_CHANNEL_ID="..."
```

### 2. Запуск через Docker Compose (рекомендуется)

```bash
docker-compose up
```

Приложение: `http://localhost:8080`

### 3. Запуск локально

Создать базу данных:

```bash
createdb youtube_agent
```

Запустить:

```bash
mvn spring-boot:run
```

Или собрать JAR:

```bash
mvn clean package -DskipTests
java -jar target/youtube-agent-0.1.0-SNAPSHOT.jar
```

## API

### Пайплайн

```bash
# Запустить генерацию видео
curl -X POST http://localhost:8080/api/pipeline/start

# Статус пайплайна
curl http://localhost:8080/api/pipeline/{runId}/status

# Перезапустить для существующего видео
curl -X POST http://localhost:8080/api/pipeline/start/{videoId}
```

### Видео

```bash
# Список видео
curl http://localhost:8080/api/videos

# Детали видео
curl http://localhost:8080/api/videos/{id}

# Перезаливать на YouTube
curl -X POST http://localhost:8080/api/videos/{id}/upload
```

### Тренды

```bash
# Список трендов
curl http://localhost:8080/api/trends

# Добавить тренд
curl -X POST http://localhost:8080/api/trends \
  -H "Content-Type: application/json" \
  -d '{"topic": "Интересные факты о космосе", "source": "manual"}'
```

### Аналитика

```bash
# Статистика по видео
curl http://localhost:8080/api/analytics/video/{videoId}

# Обновить аналитику
curl -X POST http://localhost:8080/api/analytics/refresh
```

## Автоматическое расписание

| Задача | Расписание | Описание |
|--------|-----------|----------|
| Тренды | 06:00 ежедневно | Обновление и поиск трендов |
| Генерация | 10:00, 14:00, 18:00 | Создание новых видео |
| Аналитика | 02:00 ежедневно | Сбор статистики |

## Тесты

```bash
mvn test
```

34 теста: OpenAI, EdgeTTS, Pexels, FFmpeg, YouTube, Trends, Video Management, Subtitles, Pipeline.

## Структура проекта

```
src/main/java/com/youtubeagent/
├── config/          — конфигурация (OpenAI, Pexels, YouTube, EdgeTTS, FFmpeg)
├── entity/          — JPA сущности (Video, Script, Trend, PipelineRun, AnalyticsSnapshot)
├── repository/      — Spring Data репозитории
├── dto/             — запросы и ответы API
├── pipeline/
│   ├── PipelineStage.java      — интерфейс стадии
│   ├── PipelineContext.java    — носитель данных между стадиями
│   ├── PipelineOrchestrator.java — оркестратор с retry
│   └── stages/                 — 7 стадий пайплайна
├── service/         — бизнес-логика (OpenAI, EdgeTTS, Pexels, FFmpeg, YouTube)
├── controller/      — REST API
├── scheduler/       — автоматическое расписание
└── util/            — ProcessExecutor, SubtitleGenerator, FileUtils
```
