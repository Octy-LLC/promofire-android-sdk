# Promofire Android SDK

Промокоды и реферальные кампании в вашем приложении без собственного бэкенда.

Kotlin, корутины, Ktor. Минимальная версия — Android 7.0 (API 24).

Полный контракт API — в [MobileSDK.md](MobileSDK.md). Инструкция для ИИ-агентов — в [AGENTS.md](AGENTS.md).

> **Статус:** `1.0.0-beta.1`, в Maven Central пока не опубликован. Предыдущая версия `0.2.0` имела другой, несовместимый API и тоже не публиковалась, несмотря на то что прежний README это утверждал.

## Установка

После публикации:

```kotlin
implementation("io.promofire:sdk:1.0.0-beta.1")
```

Пока не опубликовано — из локального репозитория Maven. В каталоге SDK:

```bash
./gradlew :promofire:publishToMavenLocal
```

В `settings.gradle.kts` приложения — `mavenLocal()` последним в списке, чтобы он не перекрывал Maven Central:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
```

Строка зависимости та же, что и после публикации. `includeBuild` не подойдёт: Gradle подставляет проект по его имени, `promofire`, а координата пакета — `io.promofire:sdk`.

## Быстрый старт

Порядок обязателен: `configure` → `connect` или `identify` → всё остальное. Обращение к SDK до `configure` бросает `NOT_CONFIGURED`, запрос до аутентификации — `NOT_AUTHENTICATED`.

```kotlin
// 1. Настройка. Секрет — в панели, в настройках Android-SDK.
Promofire.configure(
    PromofireConfig(secret = BuildConfig.PROMOFIRE_SECRET)
)

// 2. Аутентификация. В корутине: методы приостанавливаемые.
val customer = Promofire.identify(
    customerUserId = currentUser.id,
    device = DeviceInfo.from(context),
)

// 3. Погашение кода.
try {
    Promofire.codes.redeem("SUMMER2026")
} catch (e: PromofireException) {
    when (e.code) {
        PromofireException.Code.NOT_FOUND -> showMessage("Такого кода нет")
        PromofireException.Code.CODE_EXPIRED -> showMessage("Срок действия истёк")
        PromofireException.Code.CODE_FULLY_REDEEMED -> showMessage("Код уже использован")
        else -> throw e
    }
}
```

`DeviceInfo.from(context)` достаёт версию и номер сборки из манифеста. Их можно передать и напрямую: `DeviceInfo(appBuild = "142", appVersion = "2.1.0")`. Модель устройства и версию ОС SDK определяет сам.

Платформа нигде не задаётся: эта библиотека работает только на Android, и подставлять её должен не хост.

## Аутентификация

| Ситуация | Метод |
|---|---|
| В приложении есть свои пользователи | `identify(userId, device, profile?)` |
| Пользователей нет, приложение анонимное | `connect(device)` |

Повторный `identify` с тем же идентификатором возвращает того же клиента. `connect` при каждом вызове создаёт нового — не зовите его на каждый запуск экрана.

При смене пользователя вызывайте `disconnect()`, затем `identify` заново.

## Реферальная программа

Если тенант пометил кампанию доступной клиентам, пользователь может выпустить себе код и поделиться им.

```kotlin
if (Promofire.isCodeGenerationAvailable()) {
    val campaigns = Promofire.codeTemplates.list(limit = 20)

    val code = Promofire.codes.create(
        CreateCodeParams(
            value = "REF-${currentUser.id.take(8).uppercase()}",
            templateId = campaigns.items.first().id,
            payload = mapOf("source" to "referral"),
        )
    )

    share(code.value)
}
```

Срок жизни и число погашений наследуются от кампании — при выпуске не задаются.

## API

| Метод | Что делает |
|---|---|
| `configure(config)` | Секрет и необязательные `baseUrl`, `logLevel`, `timeoutMillis` |
| `connect(device)` | Анонимная аутентификация |
| `identify(userId, device, profile?)` | Аутентификация с привязкой к пользователю |
| `disconnect()` | Забыть токен и сессию |
| `isCodeGenerationAvailable()` | Доступен ли клиенту выпуск кодов |
| `codes.getMine(limit, offset?)` | Коды текущего клиента |
| `codes.get(value)` | Код по значению |
| `codes.redeem(value)` | Погасить код |
| `codes.getMyRedeems(limit, from, to, offset?, codeValue?)` | История погашений |
| `codes.create(params)` | Выпустить код |
| `codes.createBatch(params)` | Выпустить несколько кодов |
| `codes.update(value, params)` | Изменить или отключить код |
| `codeTemplates.list(limit, offset?)` | Доступные кампании |
| `codeTemplates.get(id)` | Кампания по идентификатору |
| `customer.getProfile()` | Профиль клиента |
| `customer.updateProfile(params)` | Обновить профиль |

Списочные методы возвращают `PaginatedList<T>` с полями `items` и `total`.

## Ошибки

Всё, что бросает SDK, — `PromofireException` с полями `code`, `message` и `status`. Разветвляйтесь по `code`:

`NOT_CONFIGURED`, `NOT_AUTHENTICATED`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `CODE_EXPIRED`, `CODE_FULLY_REDEEMED`, `VALIDATION_ERROR`, `NETWORK_ERROR`, `SERVER_ERROR`, `UNKNOWN`.

## Поведение сети

- Токен хранится только в памяти и на диск не пишется. После перезапуска приложения нужен новый `connect` или `identify`.
- При `401` SDK один раз тихо переаутентифицируется теми же параметрами и повторяет запрос.
- Сетевые сбои и `5xx` повторяются дважды с паузами 1 и 3 секунды.
- Остальные `4xx` бросаются сразу.
- Таймаут запроса — 30 секунд, меняется через `timeoutMillis`.

Все публичные методы, кроме `configure` и `disconnect`, приостанавливаемые. Внутреннее состояние защищено мьютексом — вызывать можно из любого потока.

## Логи

```kotlin
Promofire.configure(
    PromofireConfig(secret = secret, logLevel = PromofireLogLevel.DEBUG)
)
```

`NONE`, `ERROR` (по умолчанию), `INFO`, `DEBUG`. Уровень `DEBUG` печатает тела запросов и ответов — там бывают персональные данные, в продакшене включать не стоит. SDK-секрет и токен клиента в журнал не попадают: они маскируются. Если задан свой `baseUrl`, уровень по умолчанию становится `DEBUG`.

## Разработка

```bash
./gradlew :promofire:testDebugUnitTest
./gradlew :promofire:detekt
```

Для сборки нужна JDK 21. Если основная в системе старее, подойдёт та, что поставляется с Android Studio:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :promofire:detekt
```

### Прогон против живого бэкенда

`:promofire:testDebugUnitTest` герметичен: он работает без сети, на подставном
транспорте. Отдельно есть `LiveTest` — тот же набор проверок, что и у
TypeScript-SDK, но против настоящего API. Он находит то, чего подставной
транспорт показать не может: расхождения в форме ответа, коды ошибок,
единицы измерения дат.

В обычный прогон он не входит и включается свойством `promofire.live`:

```bash
# в promofire-backend/app — поднять бэкенд и завести тенанта
DB_HOST=127.0.0.1 DB_PORT=5433 node scripts/seed-dev-tenant.js

# здесь, с ANDROID-секретом из вывода скрипта
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew :promofire:testDebugUnitTest \
  -Ppromofire.live \
  -Ppromofire.secret=<секрет> \
  -Ppromofire.url=http://127.0.0.1:3000
```

Без `-Ppromofire.url` набор идёт на `http://127.0.0.1:3000`; чтобы проверить
стейдж, передайте `https://api.stage.promofire.io` и секрет его тенанта.

Скрипт засева заводит две кампании, различающиеся только `hasMutablePayload`:
иначе проверку «payload неизменяемой кампании клиент не переписывает» не на чем
выполнить и она пропускается. Против тенанта, заведённого не скриптом, она
может пропуститься по той же причине.
