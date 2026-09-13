package io.promofire

import io.ktor.http.HttpMethod
import io.promofire.internal.ApiClient
import io.promofire.internal.AuthRequestDto
import io.promofire.internal.AuthResponseDto
import io.promofire.internal.PromofireJson
import io.promofire.logger.Logger
import io.promofire.logger.PromofireLogLevel
import io.promofire.models.Customer
import io.promofire.models.CustomerProfile
import io.promofire.models.DeviceInfo
import io.promofire.models.Platform
import io.promofire.resources.CodeTemplatesResource
import io.promofire.resources.CodesResource
import io.promofire.resources.CustomerResource
import io.promofire.utils.AndroidDeviceSpecsProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val SECRET_LENGTH = 64
private val HEX = Regex("^[0-9a-fA-F]+$")

/**
 * Точка входа в SDK.
 *
 * Порядок обязателен: [configure], затем [connect] или [identify], и только
 * потом всё остальное. Любое обращение до [configure] бросает
 * `NOT_CONFIGURED`, любой запрос до аутентификации — `NOT_AUTHENTICATED`.
 *
 * Все публичные методы, кроме [configure] и [disconnect], приостанавливаемые.
 * Внутреннее состояние защищено мьютексом, вызывать можно из любого потока.
 */
public object Promofire {

    private val mutex = Mutex()
    private val logger = Logger()
    private val deviceSpecs = AndroidDeviceSpecsProvider()

    private var config: PromofireConfig? = null
    private var api: ApiClient? = null
    private var lastAuth: AuthParams? = null

    /** Токен живёт только в памяти и на диск не попадает. */
    @Volatile
    private var token: String? = null

    private var codesResource: CodesResource? = null
    private var templatesResource: CodeTemplatesResource? = null
    private var customerResource: CustomerResource? = null

    public val isConfigured: Boolean get() = config != null

    public val isAuthenticated: Boolean get() = token != null

    public val codes: CodesResource
        get() = codesResource ?: throw promofireError(PromofireException.Code.NOT_CONFIGURED)

    public val codeTemplates: CodeTemplatesResource
        get() = templatesResource ?: throw promofireError(PromofireException.Code.NOT_CONFIGURED)

    public val customer: CustomerResource
        get() = customerResource ?: throw promofireError(PromofireException.Code.NOT_CONFIGURED)

    /**
     * Настраивает SDK. Синхронен и ничего не отправляет по сети — можно
     * вызывать при старте приложения.
     */
    public fun configure(config: PromofireConfig) {
        if (config.secret.length != SECRET_LENGTH || !HEX.matches(config.secret)) {
            throw promofireError(
                code = PromofireException.Code.VALIDATION_ERROR,
                message = "secret must be a $SECRET_LENGTH-character hex string, " +
                    "got ${config.secret.length} characters",
            )
        }

        val baseUrl = config.baseUrl.trimEnd('/')

        // Спека: debug по умолчанию, когда baseUrl переопределён, иначе error.
        logger.level = config.logLevel ?: when (baseUrl) {
            PromofireConfig.DEFAULT_BASE_URL -> PromofireLogLevel.ERROR
            else -> PromofireLogLevel.DEBUG
        }

        val client = ApiClient(
            baseUrl = baseUrl,
            timeoutMillis = config.timeoutMillis,
            userAgent = "PromofireSDK/${BuildConfig.VERSION_NAME} ANDROID/${deviceSpecs.osVersion}",
            logger = logger,
            tokenProvider = { token },
            reauthenticate = ::reauthenticate,
        )

        this.config = config
        this.api = client
        this.codesResource = CodesResource(client)
        this.templatesResource = CodeTemplatesResource(client)
        this.customerResource = CustomerResource(client)

        logger.log(PromofireLogLevel.INFO, "configured for $baseUrl")
    }

    /** Анонимная аутентификация: создаёт клиента без идентификатора. */
    public suspend fun connect(device: DeviceInfo): Customer =
        authenticate(AuthParams(device = device))

    /**
     * Аутентификация с привязкой к пользователю приложения.
     *
     * Если клиент с таким [customerUserId] уже есть, возвращается он;
     * иначе создаётся новый.
     */
    public suspend fun identify(
        customerUserId: String,
        device: DeviceInfo,
        profile: CustomerProfile? = null,
    ): Customer = authenticate(
        AuthParams(device = device, customerUserId = customerUserId, profile = profile),
    )

    /** Забывает токен и параметры сессии. */
    public fun disconnect() {
        token = null
        lastAuth = null
        logger.log(PromofireLogLevel.INFO, "disconnected")
    }

    /**
     * Есть ли у тенанта хоть один шаблон, из которого клиент может выпустить
     * себе код. Бэкенд отдаёт клиенту только активные шаблоны с
     * `isUsableByCustomers`, поэтому достаточно проверить, что список непуст.
     */
    public suspend fun isCodeGenerationAvailable(): Boolean =
        codeTemplates.list(limit = 1).total > 0

    private suspend fun authenticate(params: AuthParams): Customer {
        requestToken(params)
        mutex.withLock { lastAuth = params }

        return customer.getProfile()
    }

    /**
     * Повторяет последнюю аутентификацию теми же параметрами.
     * Вызывается HTTP-слоем при 401.
     */
    private suspend fun reauthenticate() {
        val params = mutex.withLock { lastAuth }
            ?: throw promofireError(PromofireException.Code.NOT_AUTHENTICATED)

        token = null
        requestToken(params)
    }

    private suspend fun requestToken(params: AuthParams) {
        val currentConfig = config ?: throw promofireError(PromofireException.Code.NOT_CONFIGURED)
        val client = api ?: throw promofireError(PromofireException.Code.NOT_CONFIGURED)

        val request = AuthRequestDto(
            secret = currentConfig.secret,
            platform = Platform.ANDROID,
            device = deviceSpecs.deviceName,
            os = deviceSpecs.osVersion,
            appBuild = params.device.appBuild,
            appVersion = params.device.appVersion,
            sdkVersion = BuildConfig.VERSION_NAME,
            customerUserId = params.customerUserId,
            firstName = params.profile?.firstName,
            lastName = params.profile?.lastName,
            email = params.profile?.email,
            phone = params.profile?.phone,
        )

        // anonymous: запрос идёт без токена, и 401 на нём не должен запускать
        // повторную аутентификацию — иначе получится бесконечный цикл.
        val raw = client.request(
            method = HttpMethod.Post,
            path = "/auth/sdk/customer",
            body = request,
            anonymous = true,
        )

        token = PromofireJson.decodeFromString<AuthResponseDto>(raw).accessToken

        logger.log(PromofireLogLevel.INFO, "authenticated")
    }

    private data class AuthParams(
        val device: DeviceInfo,
        val customerUserId: String? = null,
        val profile: CustomerProfile? = null,
    )
}
