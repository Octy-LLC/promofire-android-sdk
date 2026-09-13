# Инструкция для ИИ-агентов: подключение Promofire в Android-приложение

Документ написан для агента, который встраивает Promofire в чужое приложение. Читается сверху вниз; шаги выполняются по порядку.

Человеческая документация — в [README.md](README.md). Полный контракт API — в [MobileSDK.md](MobileSDK.md).

> **Статус:** `io.promofire:sdk:1.0.0-beta.1` ещё не опубликован. До публикации подключается из локальной сборки — см. «Установка». Прежняя версия `0.2.0` имела несовместимый API и в Maven Central никогда не выкладывалась, хотя старый README это утверждал: инструкции вида `implementation("io.promofire:promofire:0.2.0")` не работают.

---

## 0. Чего агент сделать не может

**SDK-секрет нужно запросить у человека.** Он выдаётся в панели Promofire и лежит в аккаунте владельца проекта. Агент не может его вычислить, угадать или достать из кода.

Если секрета нет — остановитесь и спросите:

> Для подключения Promofire нужен SDK-секрет: 64 шестнадцатеричных символа, выпущенный для Android. Он в панели Promofire, в настройках, в разделе Android-SDK. Если доступа к панели нет, его можно запросить у владельца проекта.

Не оставляйте `YOUR_SECRET` в коде: приложение соберётся, а при первом запросе упадёт с `VALIDATION_ERROR`.

**Секрет именно для Android.** Веб-секрет и iOS-секрет не подойдут. Платформу SDK подставляет сам, ошибиться ею нельзя — но взять чужой секрет можно, и это даст `NOT_FOUND` при аутентификации.

---

## 1. Установка

После публикации — в `build.gradle.kts` модуля приложения:

```kotlin
implementation("io.promofire:sdk:1.0.0-beta.1")
```

Пока не опубликовано — подключением локального модуля. В `settings.gradle.kts`:

```kotlin
includeBuild("../promofire-android-sdk")
```

Требуется `minSdk` не ниже 24 и JDK 21 для сборки.

---

## 2. Куда положить секрет

Секрет попадает в собранный APK и извлекаем оттуда — это заложено в схему, он не равносилен серверному ключу. В репозиторий его класть всё равно не надо.

```kotlin
// build.gradle.kts модуля
android {
    defaultConfig {
        buildConfigField(
            "String",
            "PROMOFIRE_SECRET",
            "\"${project.findProperty("promofireSecret") ?: ""}\"",
        )
    }
    buildFeatures { buildConfig = true }
}
```

Значение — в `local.properties` или `~/.gradle/gradle.properties`, которые не коммитятся. В `gradle.properties` проекта добавьте пустую заготовку с комментарием, чтобы было видно, какое свойство требуется.

---

## 3. Минимальная интеграция

Порядок обязателен: `configure` → `connect` или `identify` → всё остальное.

`configure` синхронен и ничего не шлёт по сети — место ему в `Application.onCreate`:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Promofire.configure(
            PromofireConfig(secret = BuildConfig.PROMOFIRE_SECRET)
        )
    }
}
```

Аутентификация асинхронна и требует корутины — вызывайте после входа пользователя:

```kotlin
viewModelScope.launch {
    runCatching {
        Promofire.identify(
            customerUserId = currentUser.id,
            device = DeviceInfo.from(applicationContext),
        )
    }.onFailure { /* см. раздел 5 */ }
}
```

### Что выбрать: `connect` или `identify`

| Ситуация | Метод |
|---|---|
| В приложении есть свои пользователи | `identify(userId, device)` |
| Пользователей нет, приложение анонимное | `connect(device)` |

Берите `identify`, если есть хоть какой-то стабильный идентификатор пользователя. Повторный `identify` с тем же идентификатором возвращает того же клиента; `connect` при каждом вызове создаёт нового, и статистика размажется.

При выходе пользователя вызывайте `Promofire.disconnect()`, при входе другого — `identify` заново.

### Токен не переживает перезапуск

Он хранится только в памяти. После холодного старта приложения нужен новый `connect` или `identify` — не считайте прошлую сессию действующей и не пытайтесь сохранять токен сами.

---

## 4. Типовые сценарии

### Погасить промокод

```kotlin
try {
    Promofire.codes.redeem(codeFromUser)
    // выдайте пользователю то, что обещает кампания
} catch (e: PromofireException) {
    when (e.code) {
        PromofireException.Code.NOT_FOUND -> show("Такого кода не существует")
        PromofireException.Code.CODE_EXPIRED -> show("Срок действия кода истёк")
        PromofireException.Code.CODE_FULLY_REDEEMED -> show("Код уже использован полностью")
        PromofireException.Code.NETWORK_ERROR -> show("Нет связи, попробуйте позже")
        else -> throw e
    }
}
```

Погашение **не возвращает** данные кампании. Что получает пользователь, решает ваше приложение; `payload` кода при необходимости берите через `codes.get(value)` до погашения.

### Реферальная программа

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
}
```

Проверяйте `isCodeGenerationAvailable()` перед показом кнопки: без доступных кампаний выпуск вернёт `FORBIDDEN`, а не пустой список. Значение кода должно быть уникальным в пределах тенанта, иначе `CONFLICT`.

---

## 5. Чего делать не нужно

| Не делайте | Почему |
|---|---|
| Не оборачивайте SDK в собственные ретраи | Повторы уже встроены: `401` — тихая переаутентификация, сеть и `5xx` — две попытки с паузами 1 и 3 секунды |
| Не сохраняйте токен в `SharedPreferences` | Он намеренно живёт только в памяти; вынос на диск создаёт утечку и ничего не ускоряет |
| Не зовите `connect()` в `onCreate` каждой Activity | Каждый вызов создаёт нового клиента и портит статистику |
| Не вызывайте методы SDK на главном потоке через `runBlocking` | Они приостанавливаемые: используйте `viewModelScope` или `lifecycleScope` |
| Не сравнивайте `code.ownerId` с идентификатором вашего пользователя | Это идентификатор клиента внутри Promofire, а не ваш. Сопоставляйте через `customer.customerUserId` |
| Не ставьте `logLevel = DEBUG` в релизной сборке | Печатает тела запросов и ответов, включая персональные данные |
| Не ловите `Exception` целиком | У `PromofireException` есть `code` — разветвляйтесь по нему |

---

## 6. Проверка, что всё подключено верно

1. Секрет берётся из `BuildConfig`, значение — в некоммитируемом файле свойств.
2. `configure` вызывается один раз, в `Application.onCreate`.
3. `identify` вызывается после входа пользователя, `disconnect` — при выходе.
4. Аутентификация повторяется после холодного старта приложения.
5. Вызовы SDK идут из корутины, а не из `runBlocking` на главном потоке.
6. Погашение разветвляется как минимум по `NOT_FOUND`, `CODE_EXPIRED`, `CODE_FULLY_REDEEMED`.
7. Приложение запускается, и первый `identify` проходит без исключения.

Если пункт 7 не выполняется, смотрите `code` у исключения:

| Код | Причина |
|---|---|
| `VALIDATION_ERROR` при `configure` | Секрет не 64 шестнадцатеричных символа — вероятно, скопирован не полностью или не подставился из `BuildConfig` |
| `NOT_FOUND` при аутентификации | Секрет не от этого проекта или не для Android |
| `NETWORK_ERROR` | Нет сети, не выдано разрешение `INTERNET`, либо `baseUrl` указывает не туда |
| `NOT_CONFIGURED` | Обращение к SDK раньше `configure` |
