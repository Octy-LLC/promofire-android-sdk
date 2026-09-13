# Promofire SDK — Technical Specification

Cross-platform SDK for integrating Promofire promo code functionality into client applications. Three independent implementations sharing a unified public API contract.

| Platform | Language | Package |
|----------|----------|---------|
| iOS | Swift 5.9+ | `PromofireSDK` (SPM, CocoaPods) |
| Android | Kotlin 1.9+ | координата не выбрана, см. 13.3 (Maven Central) |
| Web / React Native | TypeScript | `@promofire/sdk` (npm) |

Порядок реализации: TypeScript → Android → iOS. Обоснование и правила версионирования — в разделе 13.

---

## 0. Статус документа

Спека сверена с реальным API 13.09.2026 — с веткой `main` бэкенда и с живым `https://api.promofire.io/docs-yaml`.

**Подтверждено верным:** все девять описанных эндпоинтов существуют и допускают роль `CUSTOMER`; `secret` действительно 64 hex-символа; `410` на истёкший и полностью погашенный код соответствует `GoneException` на бэкенде; модели `Code` и `CodeRedeem` совпадают с `CodeDto` и `CodeRedeemDto` поле в поле.

**Исправлено по результатам сверки:** форма пагинированного ответа, отсутствовавшее поле `CodeTemplate.creatorId`, тип `Customer.country`.

**Добавлено:** раздел 4.4 — генерация кодов клиентом, которой в документе не было, хотя текущие SDK её умеют и бэкенд её поддерживает.

**Решения приняты** по генерации кодов, порядку выпуска и версионированию — раздел 13. Имена и координаты пакетов вернулись в открытые после проверки того, что реально опубликовано: там же.

Документ находится в состоянии, пригодном для начала реализации.

---

## 1. Scope

The SDK covers **customer-facing** functionality only:

- Authenticate as a customer (anonymous or identified)
- Retrieve available promo codes and code templates
- Redeem promo codes
- View redemption history
- Manage customer profile

The SDK does **not** cover admin, distributor, or owner operations.

---

## 2. Configuration

### Initialization

Each SDK is initialized once with a configuration object:

```
PromofireSDK.configure(
  secret:   "<64-char hex string>",       // required — per-platform SDK secret from tenant dashboard
  platform: .ios | .android | .web,       // required — must match the secret's platform
  baseUrl:  "https://api.promofire.io"    // optional — defaults to production, override for staging/self-hosted
)
```

- `secret` — 64-character hex string obtained from the Promofire dashboard (Settings → SDK Secrets). Each platform (iOS, Android, Web) has its own secret.
- `platform` — must match the platform the secret was generated for.
- `baseUrl` — configurable for staging (`https://api-stage.promofire.io`) or local development (`http://localhost:8080`).

Configuration must happen before any other SDK call. Calling any method before `configure()` throws a `PromofireNotConfigured` error.

---

## 3. Authentication

### 3.1 Flow

Authentication is handled automatically by the SDK. The host app calls `identify()` or `connect()`, the SDK exchanges the secret + device metadata for a JWT, and stores it in memory for subsequent requests.

### 3.2 Methods

#### `connect()`

Anonymous authentication. Creates a new customer without a user identifier. Useful for apps that don't have their own user system.

```
PromofireSDK.connect(
  device:     DeviceInfo    // required
) -> Customer
```

#### `identify(customerUserId)`

Identified authentication. Links the Promofire customer to the host app's user ID. If a customer with this ID already exists, the existing customer is returned; otherwise a new one is created.

```
PromofireSDK.identify(
  customerUserId: String,        // required — stable user ID from the host app
  device:         DeviceInfo,    // required
  profile:        CustomerProfile? = nil  // optional — pre-fill name, email, phone
) -> Customer
```

#### `DeviceInfo`

Collected by the SDK automatically where possible (platform APIs). The host app must provide values the SDK cannot detect.

| Field | Type | Required | Auto-detected |
|-------|------|----------|---------------|
| `device` | String | yes | yes (model name) |
| `os` | String | yes | yes (OS version) |
| `appBuild` | String | yes | no |
| `appVersion` | String | yes | no |
| `sdkVersion` | String | yes | yes (SDK version constant) |

#### `CustomerProfile`

Optional profile fields for `identify()`:

| Field | Type |
|-------|------|
| `firstName` | String? |
| `lastName` | String? |
| `email` | String? |
| `phone` | String? |

### 3.3 Auth lifecycle

- JWT is stored **in memory only** (not persisted to disk).
- No token refresh mechanism — JWTs are long-lived. If a request returns `401`, the SDK must automatically re-authenticate using the same `connect()`/`identify()` parameters and retry the request once.
- `disconnect()` clears the token and customer state from memory.

### 3.4 Backend endpoint

```
POST /auth/sdk/customer
Content-Type: application/json

{
  "secret": "abc123...",
  "platform": "IOS",
  "device": "iPhone 15",
  "os": "iOS 18.2",
  "appBuild": "142",
  "appVersion": "2.1.0",
  "sdkVersion": "1.0.0",
  "customerUserId": "user_abc123",   // omitted for connect()
  "firstName": "John",               // optional
  "lastName": "Doe",                 // optional
  "email": "john@example.com",       // optional
  "phone": "+1234567890"             // optional
}

Response 200:
{
  "accessToken": "<JWT>"
}
```

---

## 4. Public API

All methods below require prior `connect()` or `identify()` call. Every method is **async** (returns a future/promise/coroutine).

### 4.1 Codes

#### Get own codes

Returns paginated list of codes owned by the current customer.

```
PromofireSDK.codes.getMine(
  limit:  Int,         // required, >= 1
  offset: Int = 0      // optional
) -> PaginatedList<Code>
```

**Backend:** `GET /codes/me?limit=N&offset=N`

#### Get code by value

```
PromofireSDK.codes.get(
  codeValue: String    // required
) -> Code
```

**Backend:** `GET /codes/{codeValue}`

#### Redeem a code

```
PromofireSDK.codes.redeem(
  codeValue: String    // required
) -> void
```

The SDK automatically attaches the current platform to the request.

**Backend:** `POST /codes/redeem` with `{ "codeValue": "...", "platform": "IOS" }`

**Errors:**
- `codeNotFound` — code does not exist (404)
- `codeExpired` — code TTL has elapsed (410)
- `codeFullyRedeemed` — all redemptions used up (410)

#### Get own redeems

Returns paginated list of the customer's redemptions.

```
PromofireSDK.codes.getMyRedeems(
  limit:  Int,         // required, >= 1
  offset: Int = 0,     // optional
  from:   Date,        // required
  to:     Date,        // required
  codeValue: String?   // optional — filter by code
) -> PaginatedList<CodeRedeem>
```

**Backend:** `GET /codes/redeems/me?limit=N&offset=N&from=ISO&to=ISO&codeValue=...`

### 4.2 Code Templates

#### Get template by ID

```
PromofireSDK.codeTemplates.get(
  id: String           // required — UUID
) -> CodeTemplate
```

**Backend:** `GET /code-templates/{id}`

#### List templates

```
PromofireSDK.codeTemplates.list(
  limit:  Int,         // required, >= 1
  offset: Int = 0      // optional
) -> PaginatedList<CodeTemplate>
```

**Backend:** `GET /code-templates?limit=N&offset=N`

### 4.3 Customer Profile

#### Get own profile

```
PromofireSDK.customer.getProfile() -> Customer
```

**Backend:** `GET /customers/me`

#### Update own profile

```
PromofireSDK.customer.updateProfile(
  firstName: String?,
  lastName:  String?,
  email:     String?,
  phone:     String?
) -> Customer
```

Only provided (non-nil) fields are sent in the request body.

**Backend:** `PATCH /customers/me`

### 4.4 Генерация кодов

Клиент может выпускать себе коды из шаблонов, помеченных `isUsableByCustomers`. Это механика реферальной программы: пользователь получает собственный код и раздаёт его друзьям. Раздел 1 ограничивает SDK клиентскими сценариями — генерация кодов клиентом является таковым.

Владелец кода проставляется сервером из токена. Передавать его не нужно и нельзя.

#### Выпустить код

```
PromofireSDK.codes.create(
  value:      String,              // required — значение кода, должно быть уникальным
  templateId: String,              // required — UUID шаблона
  payload:    [String: String]     // required — произвольные данные кода
) -> Code
```

**Backend:** `POST /codes` → `201`

#### Выпустить несколько кодов

Значения генерируются сервером.

```
PromofireSDK.codes.createBatch(
  templateId: String,              // required — UUID шаблона
  payload:    [String: String],    // required
  count:      Int                  // required, >= 1
) -> [Code]
```

**Backend:** `POST /codes/batch` → `201`

#### Обновить код

```
PromofireSDK.codes.update(
  codeValue: String,               // required
  payload:   [String: String]?,    // optional — только если шаблон разрешает
  active:    Bool?                 // optional — включить или отключить код
) -> Code
```

**Backend:** `PATCH /codes/{codeValue}`

Поле `status` в этом эндпоинте помечено на бэкенде как deprecated — использовать `active`.

#### Правила, которые применяет сервер

Их стоит отразить в документации SDK, потому что без этого ошибки выглядят необъяснимыми:

| Условие | Ответ | Ошибка SDK |
|---------|-------|------------|
| Шаблон не существует или не в статусе `ACTIVE` | `404` | `notFound` |
| У шаблона `isUsableByCustomers = false` | `403` | `forbidden` |
| Значение кода уже занято | `409` | `conflict` |
| Правка `payload` при `hasMutablePayload = false` | `403` | `forbidden` |
| Правка чужого кода | `403` | `forbidden` |

Срок жизни и число погашений кода наследуются от шаблона: `expiresAt = now + template.ttl`, `amount = template.amount`. Задать их при выпуске нельзя.

Перед показом кнопки «получить код» имеет смысл проверять доступность генерации — в текущих SDK для этого есть `isCodeGenerationAvailable()`. Метод стоит сохранить: он отвечает на вопрос, есть ли у тенанта хоть один шаблон с `isUsableByCustomers`.

---

## 5. Data Models

### Code

| Field | Type | Description |
|-------|------|-------------|
| `value` | String | Unique code string |
| `status` | CodeStatus | `ACTIVE`, `FULLY_REDEEMED`, `DEACTIVATED`, `EXPIRED` |
| `templateId` | String (UUID) | Parent code template |
| `createdAt` | Date | Creation timestamp |
| `updatedAt` | Date | Last update timestamp |
| `expiresAt` | Int | Expiration unix timestamp |
| `ownerId` | String (UUID) | Customer who owns the code |
| `payload` | JSON object | Arbitrary template-defined payload |
| `amount` | String | Remaining redemptions (`"Infinity"` or integer string) |

### CodeRedeem

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Redeem record ID |
| `redeemerId` | String (UUID) | Customer who redeemed |
| `code` | String | Code value |
| `platform` | Platform? | `ANDROID`, `IOS`, `WEB`, or null |
| `country` | String? | ISO 3166-1 alpha-2 country code |
| `templateId` | String (UUID) | Parent code template |
| `redeemedAt` | Date | Redemption timestamp |

### CodeTemplate

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Template ID |
| `name` | String | Template name |
| `creatorId` | String (UUID) | Сотрудник, создавший шаблон |
| `status` | TemplateStatus | `ACTIVE`, `DEACTIVATED`, `ARCHIVED` |
| `createdAt` | Date | Creation timestamp |
| `ttl` | Int | Time-to-live in seconds for codes |
| `amount` | String | Max redemptions per code (`"Infinity"` or integer string) |
| `hasMutablePayload` | Bool | Whether code payload can be changed |
| `isUsableByCustomers` | Bool | Whether customers can create codes from this template |
| `description` | String? | Template description |
| `payload` | JSON object | Default payload for codes |

### Customer

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (UUID) | Promofire customer ID |
| `customerUserId` | String? | Host app's user ID |
| `country` | Country? | Определяется по IP. Это **enum с фиксированным набором** (`Countries` на бэкенде), а не произвольная строка — в Swift и Kotlin типизировать как enum с fallback на неизвестное значение |
| `platform` | Platform | `ANDROID`, `IOS`, `WEB` |
| `device` | String? | Device model |
| `os` | String? | OS version |
| `appBuild` | String? | App build number |
| `appVersion` | String? | App version string |
| `sdkVersion` | String? | SDK version |
| `firstName` | String? | First name |
| `lastName` | String? | Last name |
| `email` | String? | Email |
| `phone` | String? | Phone number |
| `createdAt` | Date | Registration timestamp |
| `lastSession` | Date | Last activity timestamp |
| `description` | String? | Admin-assigned description |

### PaginatedList\<T\>

| Field | Type |
|-------|------|
| `items` | [T] |
| `total` | Int |

**Внимание: API возвращает не `items`.** Имя поля со списком зависит от ресурса:

| Эндпоинт | Поле в ответе |
|----------|---------------|
| `GET /codes/me` | `codes` |
| `GET /codes/redeems/me` | `redeems` |
| `GET /code-templates` | `templates` |

Поле `total` присутствует везде. SDK обязан нормализовать это в единый `PaginatedList<T>` с полем `items` — приводить публичный API к форме бэкенда не нужно, но при разборе ответа помнить о разнице обязательно.

### Platform (enum)

`ANDROID`, `IOS`, `WEB`

### CodeStatus (enum)

`ACTIVE`, `FULLY_REDEEMED`, `DEACTIVATED`, `EXPIRED`

### TemplateStatus (enum)

`ACTIVE`, `DEACTIVATED`, `ARCHIVED`

---

## 6. Error Handling

All SDK methods throw typed errors. Each error has a `code` (machine-readable) and `message` (human-readable).

### Error types

| Error code | HTTP | Description |
|------------|------|-------------|
| `notConfigured` | — | SDK not initialized, call `configure()` first |
| `notAuthenticated` | — | No active session, call `connect()` or `identify()` first |
| `unauthorized` | 401 | Invalid or expired token (SDK retries auth once automatically) |
| `forbidden` | 403 | Operation not allowed for this user |
| `notFound` | 404 | Resource not found (code, template, customer) |
| `conflict` | 409 | Resource already exists |
| `codeExpired` | 410 | Code TTL has elapsed |
| `codeFullyRedeemed` | 410 | All redemptions consumed |
| `validationError` | 400 | Invalid request parameters |
| `networkError` | — | Connection failure, timeout |
| `serverError` | 5xx | Unexpected server error |

### Error structure

```
PromofireError {
  code:    String      // machine-readable error code from the table above
  message: String      // human-readable message from the API or a default
  status:  Int?        // HTTP status code, if applicable
}
```

---

## 7. HTTP Layer

### Base requirements

- All requests use `Content-Type: application/json`.
- Auth token is sent as `Authorization: Bearer <JWT>`.
- Request timeout: **30 seconds** default, configurable.
- The SDK must set a `User-Agent` header: `PromofireSDK/<version> <platform>/<os>`.

### Automatic retry

- On `401` response: re-authenticate silently and retry the request once.
- On network error or `5xx`: retry up to **2 times** with exponential backoff (1s, 3s).
- On `4xx` (except 401): do not retry, throw immediately.

---

## 8. Thread Safety & Concurrency

| Platform | Model |
|----------|-------|
| **Swift** | All public methods are `async`. SDK is `@Sendable`-safe. Internal state protected by an actor. |
| **Kotlin** | All public methods are `suspend`. SDK is thread-safe. Internal state protected by `Mutex`. |
| **TypeScript** | All public methods return `Promise`. No special concurrency handling needed. |

---

## 9. Logging

The SDK provides a configurable log level:

```
PromofireSDK.configure(
  ...
  logLevel: .none | .error | .info | .debug
)
```

| Level | Output |
|-------|--------|
| `none` | Silent |
| `error` | Errors only |
| `info` | Auth events, API calls (method + path + status) |
| `debug` | Full request/response bodies (WARNING: may contain sensitive data) |

Default: `error` in production, `debug` when `baseUrl` is overridden.

Logs go to the platform's standard output (`os_log` / `Logcat` / `console`).

---

## 10. Distribution

### Swift (iOS)

- **Swift Package Manager** (primary): новый репозиторий под организацией — адрес зависит от вопроса 13.4
- **CocoaPods**: `pod 'PromofireSDK'`
- Minimum deployment target: **iOS 15.0**
- Dependencies: none (uses `URLSession`)

Существующий `github.com/morozbogdn/promofire-swift` не используется: он размещён на личном аккаунте подрядчика. Переносить историю оттуда не нужно — код переписывается целиком.

### Kotlin (Android)

- **Maven Central**: `io.promofire:promofire:<version>` — координата сохраняется, см. раздел 13
- Minimum SDK: **API 24** (Android 7.0)
- Dependencies: `kotlinx-coroutines`, `kotlinx-serialization-json`, `okhttp` (or `ktor-client`)

### TypeScript (Web / React Native)

- **npm**: `@promofire/sdk`
- Target: **ES2020**, ships with type declarations
- Works in: browser, Node.js 18+, React Native 0.72+
- Dependencies: none (uses `fetch`)

---

## 11. Usage Example (pseudocode)

```
// 1. Configure
PromofireSDK.configure(
  secret: "a1b2c3d4...",
  platform: .ios,
  baseUrl: "https://api.promofire.io"
)

// 2. Authenticate
let customer = await PromofireSDK.identify(
  customerUserId: "user_123",
  device: DeviceInfo(appBuild: "142", appVersion: "2.1.0")
)

// 3. Get available code templates
let templates = await PromofireSDK.codeTemplates.list(limit: 20)

// 4. Redeem a code
try {
  await PromofireSDK.codes.redeem(codeValue: "SUMMER2026")
} catch PromofireError.codeExpired {
  // handle expired code
} catch PromofireError.codeFullyRedeemed {
  // handle fully redeemed code
}

// 5. Check redemption history
let redeems = await PromofireSDK.codes.getMyRedeems(
  limit: 50, from: startOfMonth, to: now
)

// 6. Update profile
await PromofireSDK.customer.updateProfile(email: "new@email.com")

// 7. Disconnect
PromofireSDK.disconnect()
```

---

## 12. Acceptance Criteria

Each SDK implementation must pass these requirements:

1. **Configuration** — `configure()` sets secret, platform, base URL; calling methods before configure throws `notConfigured`.
2. **Anonymous auth** — `connect()` creates a new customer and returns a valid `Customer` object.
3. **Identified auth** — `identify()` with a new `customerUserId` creates a customer; with an existing ID returns the same customer.
4. **Token management** — JWT stored in memory; `401` triggers silent re-auth + retry; `disconnect()` clears state.
5. **Code retrieval** — `codes.getMine()` returns paginated owned codes; `codes.get(value)` returns a single code.
6. **Code redemption** — `codes.redeem(value)` succeeds for valid codes; throws `codeExpired` / `codeFullyRedeemed` for consumed codes.
7. **Redeems history** — `codes.getMyRedeems()` returns paginated redeems with date filtering.
8. **Code templates** — `codeTemplates.list()` and `codeTemplates.get(id)` return correct data.
9. **Customer profile** — `customer.getProfile()` returns full profile; `customer.updateProfile()` updates only provided fields.
10. **Code generation** — `codes.create()` выпускает код из шаблона с `isUsableByCustomers`; `codes.createBatch()` возвращает запрошенное число кодов; `codes.update()` меняет `payload` и `active`. Шаблон без `isUsableByCustomers` даёт `forbidden`, занятое значение — `conflict`, неизвестный шаблон — `notFound`.
11. **Pagination shape** — списочные ответы читаются корректно, несмотря на разные имена полей в API (`codes`, `redeems`, `templates`), и приводятся к единому `PaginatedList<T>`.
12. **Error mapping** — all API errors are mapped to typed `PromofireError` values.
13. **Retry logic** — network errors and 5xx are retried with backoff; 401 triggers re-auth.
14. **Logging** — log levels work; `debug` shows request/response; `none` is silent.
15. **Platform distribution** — published to SPM + CocoaPods / Maven Central / npm.

---

## 13. Решённое и открытое

### Решено 13.09.2026

**Генерация кодов остаётся в SDK.** Механика реферальной программы — пользователь выпускает себе код и раздаёт друзьям — признана нужной функциональностью. Описана в разделе 4.4. Раздел 1 продолжает ограничивать SDK клиентскими сценариями; генерация клиентом является таковым и admin/distributor-операций не открывает.

**Три расхождения устранены на бэкенде, а не в спеке:**

- `offset` в пагинации стал необязательным со значением по умолчанию `0` — спека и API теперь совпадают.
- Все даты приведены к `format: 'date-time'`. Ранее `CustomerDto.createdAt`, `CustomerDto.lastSession` и параметры `from`/`to` использовали `format: 'timestamp'`, которого в OpenAPI не существует: генераторы типизировали такие поля как обычные строки, из-за чего в текущем iOS SDK появились ручные конверсии через `ISODateFormatter`.
- `platform` в `POST /codes/redeem` стал обязательным. Раньше он был помечен `@IsOptional()`, при том что колонка `code_redeems.platform` объявлена `NOT NULL` — запрос без платформы падал нарушением ограничения и возвращал `500` вместо `400`. Заодно `CodeRedeemDto.platform` и `CodeRedeemDto.country` перестали быть `nullable`: значениями `null` они быть не могут.

Изменения затрагивают все списочные эндпоинты и требуют выкатки до начала работы над SDK.

**Имя Android-пакета — вопрос снова открыт, см. 13.3.** Решение сохранить `io.promofire:promofire` принималось из предпосылки, что сломанная `0.2.0` опубликована на Maven Central и останется там навсегда. Проверка 13.09.2026 показала, что это не так: по адресам `io/promofire/`, `io/promofire/promofire/` и `io/promofire/sdk/` на `repo1.maven.org` пусто. Публиковать нечего было перекрывать, и аргумент отпал.

**iOS SDK переезжает в новый репозиторий под организацией.** Текущий `promofire-swift` размещён на `github.com/morozbogdn/promofire-swift` — личный аккаунт подрядчика, а не `Octy-LLC`, под которой лежат все остальные репозитории. Это стоит закрыть независимо от переписывания: канонический репозиторий боевого SDK не принадлежит компании.

`PromofireSDK` как кандидат отпадает — это заглушка 2024 года без единого коммита.

**Порядок: TypeScript → Android → iOS, последовательно.**

Первая написанная библиотека неизбежно вскроет ошибки в спеке, поэтому первой идёт самая дешёвая в переделке. TypeScript публикуется в npm мгновенно, проще всех тестируется, не имеет существующих пользователей и быстрее всех покажет, где документ расходится с реальностью. Android второй — у него самая развитая текущая реализация, есть откуда портировать структуру. iOS последний: там всё сгенерировано из OpenAPI и переписывать придётся больше всего, что разумно делать по уже устоявшейся спеке.

После первой реализации спека дорабатывается, и две оставшиеся платформы пишутся по исправленной версии.

**Версионирование: `1.0.0-beta.N` в ходе разработки, `1.0.0` — одновременно у всех трёх.**

Иначе первая платформа получит `1.0.0`, то есть обещание стабильного публичного API, ровно в тот момент, когда две следующие это API ещё будут менять. Одновременный `1.0.0` означает, что номер версии значит одно и то же на всех платформах.

### Открытое

### 13.3 Имена и координаты пакетов

Вернулось в открытые после проверки реального положения дел 13.09.2026.

- **Android.** В Maven Central артефактов `io.promofire` нет, при том что README Android-SDK предлагает `implementation("io.promofire:promofire:0.2.0")` — то есть инструкция ведёт в пустоту. Выбор координаты свободен; `io.promofire:sdk` согласуется с `@promofire/sdk` и `PromofireSDK` лучше, чем `io.promofire:promofire`.
- **npm.** Scope `@promofire` пуст: ни одного пакета. Публиковать, вероятно, можно, но владение scope нужно подтвердить. Текущее поколение веб-SDK лежит под другим именем — `@octy/promofire-web-sdk`.

### 13.4 Предыдущее поколение веб-SDK выводится из обращения

**Решено 13.09.2026.** `@octy/promofire-web-sdk` (последняя версия `0.6.1` от 11.07.2025, исходники в `Octy-LLC/promofire-js-sdk`, API вида `new Promofire(sdkData).activate(customerData)`) помечается устаревшим, а онбординг переводится на новый пакет.

**Порядок обязателен, иначе клиенты окажутся в пустоте:**

1. Опубликовать новый SDK.
2. Обновить инструкцию в панели — `promofire-ui/src/components/business/settings/WebSdkSettings.tsx`, там сейчас зашит код со старым пакетом и старым API.
3. Выкатить панель.
4. Только теперь пометить старый пакет устаревшим:
   `npm deprecate @octy/promofire-web-sdk "Устарел, используйте @promofire/sdk"`

Пометить раньше означает показать предупреждение тем, кому ещё некуда переходить. Выкатить панель раньше публикации — отправить людей ставить несуществующий пакет.

Права на `deprecate` есть у сопровождающего пакета (`tesla022`); с другой учётной записи команда не пройдёт.

### 13.5 Репозитории и владение именами

**GitHub — `Octy-LLC`, решено 13.09.2026.** Отдельная организация `promofire` не заводится, адреса в документе читаются как `github.com/Octy-LLC/...`.

| SDK | Репозиторий |
|---|---|
| TypeScript | `Octy-LLC/promofire-ts-sdk` — заведён 13.09.2026, приватный |
| Android | `Octy-LLC/promofire-android-sdk` — существующий, переписывается на месте |
| iOS | `Octy-LLC/PromofireSDK-iOS` — существующий, пустой |

Предыдущее поколение веб-SDK остаётся в `Octy-LLC/promofire-js-sdk` и не смешивается с новым: две несовместимые библиотеки в одной истории читались бы как одна.

Новый iOS-SDK занимает существующий `Octy-LLC/PromofireSDK-iOS` — он пуст, создан в марте 2024 и не содержит ни одного коммита, так что переносить оттуда нечего. Плодить ещё один репозиторий не нужно. Текущий рабочий `promofire-swift` остаётся на личном аккаунте подрядчика и в работе больше не участвует.

**npm — за владельцем проекта.** Scope `@promofire` пуст, организация не заведена. Создаётся вручную на npmjs.com под учётной записью владельца: агент этого сделать не может и не должен. До этого момента публиковать некуда.

### 13.6 Совместимость со старыми версиями SDK

`platform` стал обязательным в `POST /codes/redeem`. Опубликованные `0.2.0` для Android и текущий iOS его передают, так что фактической поломки нет — но если где-то существует интеграция напрямую через API без платформы, она начнёт получать `400` вместо прежнего `500`. Проверить перед выкаткой.
