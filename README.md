<div align="center">

# 🎬 YouTube Shorts Agent

**Полностью автоматизированное приложение для создания и публикации YouTube Shorts с помощью ИИ**

[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-47%20passed-brightgreen)]()

</div>

---

## 🚀 Возможности

| Функция | Описание |
|---------|----------|
| 🎯 **Автоматические тренды** | Groq AI + YouTube Search + Google Trends |
| 📝 **Генерация сценариев** | Groq LLM (бесплатно) с SEO-оптимизацией |
| 🎙️ **Озвучка** | Edge TTS (бесплатно) на русском языке |
| 🎬 **Видео** | Стоковые клипы Pexels + монтаж FFmpeg |
| 📊 **Аналитика** | Просмотры, лайки, engagement rate |
| 🧪 **A/B тестирование** | Тест заголовков, превью, времени публикации |
| 🎞️ **Мини-сериалы** | Многосерийные проекты с AI-генерацией |
| 🌐 **Дашборд** | Красивый веб-интерфейс для управления |

---

## ⚡ Быстрый старт

### 1. Клонируй репозиторий

```bash
git clone https://github.com/Pycukvsu/AI-YoutubeAgent.git
cd AI-YoutubeAgent
```

### 2. Настрой API ключи

Создай файл `.env`:

```bash
GROQ_API_KEY=gsk_...          # https://console.groq.com (бесплатно)
PEXELS_API_KEY=...            # https://www.pexels.com/api/ (бесплатно)
YOUTUBE_CLIENT_ID=...         # https://console.cloud.google.com
YOUTUBE_CLIENT_SECRET=...
YOUTUBE_REFRESH_TOKEN=...
YOUTUBE_CHANNEL_ID=...
```

### 3. Запусти через Docker

```bash
docker-compose up --build
```

### 4. Открой дашборд

🌐 **http://localhost:8080**

---

## 🏗️ Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                    YouTube Shorts Agent                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐               │
│  │  Тренды   │───▶│ Сценарий │───▶│ Озвучка  │               │
│  │  Groq AI  │    │  Groq AI │    │ Edge TTS │               │
│  └──────────┘    └──────────┘    └──────────┘               │
│       │              │                │                       │
│       ▼              ▼                ▼                       │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐               │
│  │  Поиск    │───▶│  SEO     │───▶│  FFmpeg  │               │
│  │  Pexels   │    │  Оптим.  │    │  Монтаж  │               │
│  └──────────┘    └──────────┘    └──────────┘               │
│                                          │                   │
│                                          ▼                   │
│                                   ┌──────────┐              │
│                                   │ YouTube  │              │
│                                   │  Upload  │              │
│                                   └──────────┘              │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 API Endpoints

### Видео
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/api/pipeline/start` | Запустить генерацию видео |
| `GET` | `/api/videos` | Список видео |
| `GET` | `/api/videos/{id}` | Детали видео |

### Тренды
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/trends` | Список трендов |
| `POST` | `/api/trends` | Добавить тренд |
| `POST` | `/api/trends/discover` | Поиск трендов через AI |
| `DELETE` | `/api/trends/{id}` | Удалить тренд |

### Мини-сериалы
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/api/series` | Создать сериал |
| `GET` | `/api/series` | Список сериалов |
| `GET` | `/api/series/{id}` | Детали сериала |
| `POST` | `/api/series/{id}/generate` | Сгенерировать эпизод |

### A/B Тестирование
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/api/ab-tests/title` | Тест заголовков |
| `POST` | `/api/ab-tests/thumbnail` | Тест превью |
| `GET` | `/api/ab-tests/{id}/analyze` | Анализ результатов |

### Аналитика
| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/api/analytics/overview` | Общая статистика |
| `GET` | `/api/analytics/video/{id}` | Статистика видео |
| `POST` | `/api/analytics/refresh` | Обновить аналитику |

---

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Описание | Обязательна |
|-----------|----------|-------------|
| `GROQ_API_KEY` | API ключ Groq (бесплатно) | ✅ |
| `PEXELS_API_KEY` | API ключ Pexels | ✅ |
| `YOUTUBE_CLIENT_ID` | YouTube OAuth Client ID | ✅ |
| `YOUTUBE_CLIENT_SECRET` | YouTube OAuth Client Secret | ✅ |
| `YOUTUBE_REFRESH_TOKEN` | YouTube Refresh Token | ✅ |
| `YOUTUBE_CHANNEL_ID` | ID YouTube канала | ✅ |
| `DALL_E_API_KEY` | OpenAI API ключ для изображений | ❌ |
| `OPENAI_API_KEY` | OpenAI API ключ (альтернатива Groq) | ❌ |

### Автоматическое расписание

| Задача | Расписание | Описание |
|--------|-----------|----------|
| 🎯 Тренды | 06:00 | Поиск новых трендов через Groq + YouTube |
| 🎬 Генерация | 10:00, 14:00, 18:00 | Создание новых видео |
| 📊 Аналитика | 02:00 | Сбор статистики с YouTube |

---

## 🧪 Тесты

```bash
mvn test
```

**47 тестов** по 11 классам:

| Класс | Тесты | Что проверяется |
|-------|-------|-----------------|
| `OpenAiServiceTest` | 1 | API подключение |
| `EdgeTtsServiceTest` | 3 | Генерация аудио |
| `PexelsServiceTest` | 3 | Поиск и скачивание видео |
| `FfmpegServiceTest` | 7 | Монтаж видео |
| `YoutubeUploadServiceTest` | 4 | Загрузка на YouTube |
| `TrendServiceTest` | 6 | Управление трендами |
| `TrendDiscoveryServiceTest` | 4 | AI поиск трендов |
| `VideoManagementServiceTest` | 5 | CRUD видео |
| `SeoServiceTest` | 3 | SEO оптимизация |
| `VideoQualityServiceTest` | 6 | Проверка качества |
| `PipelineOrchestratorTest` | 2 | Оркестратор пайплайна |
| `SubtitleGeneratorTest` | 3 | Генерация субтитров |

---

## 📁 Структура проекта

```
AI-YotubeAgent/
├── docker-compose.yml          # Docker конфигурация
├── Dockerfile                  # Сборка приложения
├── pom.xml                     # Maven зависимости
├── .env                        # API ключи (не коммитить!)
├── src/
│   ├── main/
│   │   ├── java/com/youtubeagent/
│   │   │   ├── config/         # Конфигурация
│   │   │   ├── entity/         # JPA сущности
│   │   │   ├── repository/     # Spring Data репозитории
│   │   │   ├── dto/            # DTO объекты
│   │   │   ├── pipeline/       # Пайплайн генерации
│   │   │   │   ├── stages/     # 7 стадий пайплайна
│   │   │   │   ├── PipelineOrchestrator.java
│   │   │   │   └── PipelineContext.java
│   │   │   ├── service/        # Бизнес-логика
│   │   │   ├── controller/     # REST API
│   │   │   ├── scheduler/      # Автоматическое расписание
│   │   │   └── util/           # Утилиты
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── templates/      # HTML шаблоны
│   │       ├── db/migration/   # Flyway миграции
│   │       └── scripts/        # Python скрипты
│   └── test/                   # Unit тесты
```

---

## 🛠️ Технологии

| Компонент | Технология |
|-----------|-----------|
| Backend | Java 21 + Spring Boot 3.4 |
| Database | PostgreSQL + Flyway |
| AI (сценарии) | Groq (бесплатно) |
| AI (изображения) | OpenAI DALL-E 3 |
| TTS (озвучка) | Edge TTS (бесплатно) |
| Видео | Pexels API (бесплатно) |
| Монтаж | FFmpeg |
| YouTube | YouTube Data API v3 |
| Docker | Docker Compose |

---

## 📄 Лицензия

MIT License

---

<div align="center">

**Сделано с ❤️ для автоматизации контента**

</div>
