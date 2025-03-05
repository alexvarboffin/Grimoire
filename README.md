



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



:composeApp:run
gradle nativeBinaries
gradlew :composeApp:packageDistributionForCurrentOS
https://material-foundation.github.io/material-theme-builder/

fun compareJSON(json1: String, json2: String): String {
return try {
val comparison = JSONCompare.compareJSON(json1, json2, JSONCompareMode.STRICT)
if (comparison.passed()) "✅ Same JSON" else "❗ Differences :\n ${comparison.message}"
} catch (e: JSONException) {
"❌ Format Error"
}
}