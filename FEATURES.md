# ByteBreakerz Utility Suite Features

## 🛠 Инструменты

### 1. Certificate Hash Grabber
- Получение хэшей SSL сертификатов для пиннинга
- Отображение информации о сертификатах
- Сохранение результатов в файл
- Копирование в буфер обмена
- Поддержка множественных сертификатов

### 2. Text Presets Manager
- Управление пресетами для замены текста
- Выбор директории для обработки
- Фильтрация по расширениям файлов
- Предпросмотр изменений
- Массовая замена текста

### 3. TOML Merger
- Слияние TOML файлов с версиями зависимостей
- Автоматическое разрешение конфликтов версий
- Сортировка зависимостей в алфавитном порядке
- Поддержка вложенных конфигураций
- Сохранение результата в отдельный файл

## 🎨 UI/UX
- Material 3 дизайн
- Адаптивный интерфейс
- Поддержка светлой/темной темы
- Сохранение настроек между сессиями
- Интуитивная навигация

## ⚙️ Технические особенности
- Kotlin Multiplatform проект
- Clean Architecture
- Jetpack Compose UI
- Сохранение настроек через DataStore
- Корутины для асинхронных операций

## 🔒 Безопасность
- Безопасная работа с файловой системой
- Обработка SSL сертификатов
- Валидация входных данных
- Обработка ошибок

## 📋 Планируемые инструменты
1. **Android Manifest Merger**
   - Слияние манифестов
   - Анализ разрешений
   - Оптимизация тегов

2. **Resource Optimizer**
   - Поиск неиспользуемых ресурсов
   - Оптимизация изображений
   - Анализ размеров

3. **Layout Inspector Pro**
   - Анализ иерархии View
   - Поиск проблем производительности
   - Конвертация в Compose

4. **Code Generator**
   - CRUD операции
   - API интеграция
   - Формы с валидацией
   - Настройки приложения

## 🔄 Последние обновления
- Добавлена сортировка в TOML Merger
- Улучшен UI сертификатов
- Добавлено сохранение настроек темы
- Оптимизирована работа с файлами

## Core Features
- KMP project setup for desktop platform
- File picker integration using mpfilepicker library
- Certificate Hash Grabber tool implementation

## UI Components
- [x] Main application window
- [x] File selection dialog
- [x] Certificate Hash Grabber screen
  - Hostname input field
  - Output file selection
  - Certificate display cards
  - Loading indicators
  - Error handling

## Utilities
- [x] File Operations
  - [x] File picker implementation
  - [x] File save functionality
- [x] Certificate Operations
  - [x] SSL certificate extraction
  - [x] Certificate hash calculation
  - [x] Certificate info display
  - [x] File output generation

## Performance Optimizations
- [x] Background processing for network operations
- [x] Coroutines integration
- [x] UI state management
- [x] Error handling and recovery

## Security Features
- [x] SSL/TLS certificate handling
- [x] Secure hash generation
- [x] File system security

## Testing
- [ ] Unit tests setup
- [ ] Integration tests
- [ ] UI tests

## Documentation
- [ ] API documentation
- [ ] User guide
- [ ] Development setup guide 