package io.promofire

import io.promofire.logger.PromofireLogLevel
import io.promofire.models.Code
import io.promofire.models.Customer
import io.promofire.models.DeviceInfo
import io.promofire.models.Platform
import io.promofire.models.params.CreateCodeParams
import io.promofire.models.params.UpdateCodeParams
import io.promofire.models.params.UpdateCustomerParams
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.util.Date

/**
 * Прогон SDK против настоящего бэкенда.
 *
 * Не входит в обычный `:promofire:test`: требует поднятого приложения и
 * заведённого тенанта. Запускается отдельно —
 *
 *   # в promofire-backend/app
 *   DB_HOST=127.0.0.1 DB_PORT=5433 node scripts/seed-dev-tenant.js
 *
 *   # здесь, с ANDROID-секретом из вывода
 *   ./gradlew :promofire:testDebugUnitTest -Ppromofire.live \
 *     -Ppromofire.secret=<секрет> [-Ppromofire.url=http://127.0.0.1:3000]
 *
 * Набор перенесён из `promofire-ts-sdk/test/e2e/live.test.js` — того самого,
 * который нашёл три дефекта бэкенда, невидимых для подставного транспорта:
 * асимметричный `ownerId`, 502 на погашении при неопределившемся IP и
 * проверку владения, сравнивавшую идентификаторы из разных таблиц.
 *
 * Две проверки добавлены сверх переноса и специфичны для Android: [t08] и [t19].
 *
 * Тесты делят состояние и выполняются по порядку номеров: [Promofire] —
 * синглтон, разнести их по независимым экземплярам, как в TypeScript,
 * нельзя.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LiveTest {

    companion object {
        private val SECRET: String? = System.getenv("PROMOFIRE_E2E_SECRET")
        private val BASE_URL: String = System.getenv("PROMOFIRE_E2E_URL") ?: "http://127.0.0.1:3000"

        private val DEVICE = DeviceInfo(appBuild = "1", appVersion = "1.0.0-e2e")

        /** Состояние, которое тесты передают друг другу по цепочке. */
        private lateinit var customer: Customer
        private lateinit var templateId: String
        private lateinit var codeValue: String

        @BeforeClass
        @JvmStatic
        fun connectOnce() {
            assumeTrue("PROMOFIRE_E2E_SECRET не задан — живой набор пропущен", SECRET != null)

            Promofire.configure(
                PromofireConfig(
                    secret = SECRET!!,
                    baseUrl = BASE_URL,
                    logLevel = PromofireLogLevel.NONE,
                )
            )

            customer = runBlocking { Promofire.connect(DEVICE) }
        }

        @AfterClass
        @JvmStatic
        fun disconnectOnce() {
            Promofire.disconnect()
        }

        private fun uniqueValue(prefix: String): String =
            "$prefix-${System.currentTimeMillis().toString(36).uppercase()}"
    }

    /**
     * SDK бросает исключения, а не возвращает результат-обёртку, поэтому
     * ожидаемый отказ ловится здесь.
     *
     * Функция встроенная намеренно: только у встроенной функции внутри блока
     * можно вызывать приостанавливаемые методы. `Assert.assertThrows` для
     * этого не годится — его `ThrowingRunnable` встраиванию не подлежит.
     */
    private inline fun failure(block: () -> Unit): PromofireException {
        val error = try {
            block()
            null
        } catch (e: PromofireException) {
            e
        }

        return error ?: throw AssertionError("ожидалось PromofireException, вызов прошёл успешно")
    }

    // MARK: - Сессия

    @Test
    fun `t01 connect создаёт клиента и возвращает его профиль`() {
        assertTrue(customer.id.isNotEmpty())
        assertEquals(Platform.ANDROID, customer.platform)
        assertEquals("1.0.0-e2e", customer.appVersion)
        assertTrue("дата разобрана", customer.createdAt.time > 0)
    }

    @Test
    fun `t02 getProfile отдаёт того же клиента`() = runBlocking {
        assertEquals(customer.id, Promofire.customer.getProfile().id)
    }

    // MARK: - Кампании

    @Test
    fun `t03 кампании приходят в едином формате items`() = runBlocking {
        val page = Promofire.codeTemplates.list(limit = 10)

        assertTrue("нормализация в items сработала", page.items.isNotEmpty())
        assertTrue(page.total >= 1)

        val template = page.items.first()

        templateId = template.id

        assertTrue(
            "клиенту отдаются только доступные ему кампании",
            template.isUsableByCustomers,
        )
        assertTrue("дата разобрана", template.createdAt.time > 0)
    }

    @Test
    fun `t04 offset необязателен`() = runBlocking {
        assertEquals(1, Promofire.codeTemplates.list(limit = 1).items.size)
    }

    @Test
    fun `t05 isCodeGenerationAvailable отвечает утвердительно`() = runBlocking {
        assertTrue(Promofire.isCodeGenerationAvailable())
    }

    @Test
    fun `t06 получить кампанию по идентификатору`() = runBlocking {
        assertEquals(templateId, Promofire.codeTemplates.get(templateId).id)
    }

    // MARK: - Коды

    @Test
    fun `t07 клиент выпускает себе код`() = runBlocking {
        codeValue = uniqueValue("E2E")

        val code = Promofire.codes.create(
            CreateCodeParams(
                value = codeValue,
                templateId = templateId,
                payload = mapOf("source" to "e2e"),
            )
        )

        assertEquals(codeValue, code.value)
        assertEquals(Code.Status.ACTIVE, code.status)
        assertEquals(customer.id, code.ownerId)
        assertEquals(mapOf("source" to "e2e"), code.payload)
    }

    @Test
    fun `t08 срок жизни приходит в миллисекундах`() = runBlocking {
        // Сверх переноса: старый SDK трактовал expiresAt как секунды, и демо
        // показывало неверные даты истечения. Кампания из seed-скрипта живёт
        // 30 дней, поэтому значение в секундах вылезло бы далеко за границу.
        val code = Promofire.codes.get(codeValue)
        val now = System.currentTimeMillis()
        val month = 31L * 24 * 60 * 60 * 1000

        assertTrue("срок ещё не истёк", code.expiresAt > now)
        assertTrue("значение не в секундах", code.expiresAt < now + month)
    }

    @Test
    fun `t09 повторный выпуск того же значения даёт conflict`() = runBlocking {
        val error = failure {
            Promofire.codes.create(CreateCodeParams(value = codeValue, templateId = templateId))
        }

        assertEquals(PromofireException.Code.CONFLICT, error.code)
        assertEquals(409, error.status)
    }

    @Test
    fun `t10 несуществующий код даёт notFound`() = runBlocking {
        val error = failure { Promofire.codes.get("NO-SUCH-CODE-12345") }

        assertEquals(PromofireException.Code.NOT_FOUND, error.code)
        assertEquals(404, error.status)
    }

    @Test
    fun `t11 код находится по значению и попадает в свои`() = runBlocking {
        assertEquals(codeValue, Promofire.codes.get(codeValue).value)

        val mine = Promofire.codes.getMine(limit = 50)

        assertTrue("выпущенный код виден в getMine", mine.items.any { it.value == codeValue })
    }

    @Test
    fun `t12 код погашается и появляется в истории`() = runBlocking {
        Promofire.codes.redeem(codeValue)

        val redeems = Promofire.codes.getMyRedeems(
            limit = 50,
            from = Date(System.currentTimeMillis() - 60_000),
            to = Date(System.currentTimeMillis() + 60_000),
            codeValue = codeValue,
        )

        assertEquals(1, redeems.total)

        val redeem = redeems.items.first()

        assertEquals(codeValue, redeem.code)
        assertEquals("платформа проставлена SDK, а не осталась пустой", Platform.ANDROID, redeem.platform)
        assertTrue("дата разобрана", redeem.redeemedAt.time > 0)
    }

    @Test
    fun `t13 исчерпание погашений даёт codeFullyRedeemed`() = runBlocking {
        // Кампания выдаёт пять погашений, одно уже израсходовано.
        repeat(4) { Promofire.codes.redeem(codeValue) }

        val error = failure { Promofire.codes.redeem(codeValue) }

        assertEquals(PromofireException.Code.CODE_FULLY_REDEEMED, error.code)
        assertEquals(410, error.status)
    }

    @Test
    fun `t14 код отключается через update`() = runBlocking {
        val other = uniqueValue("E2E-OFF")

        Promofire.codes.create(CreateCodeParams(value = other, templateId = templateId))

        assertEquals(
            Code.Status.DEACTIVATED,
            Promofire.codes.update(other, UpdateCodeParams(active = false)).status,
        )
    }

    @Test
    fun `t15 клиент не может включить код обратно`() = runBlocking {
        // Владелец гасит кампанию, деактивируя все её коды. Если бы клиент мог
        // выставить active = true, он отменял бы это решение и снова погашал код.
        val code = uniqueValue("E2E-BACK")

        Promofire.codes.create(CreateCodeParams(value = code, templateId = templateId))
        Promofire.codes.update(code, UpdateCodeParams(active = false))

        val error = failure { Promofire.codes.update(code, UpdateCodeParams(active = true)) }

        assertEquals(PromofireException.Code.FORBIDDEN, error.code)
        assertEquals(403, error.status)
    }

    @Test
    fun `t16 payload неизменяемой кампании клиент не переписывает`() = runBlocking {
        // Кампания из seed-скрипта создаётся с hasMutablePayload, поэтому
        // проверяем через кампанию, где он снят. Если такой нет, тест
        // пропускается, а не врёт об успехе.
        val immutable = Promofire.codeTemplates.list(limit = 50)
            .items
            .firstOrNull { !it.hasMutablePayload }

        assumeTrue("нет кампании с неизменяемым payload", immutable != null)

        val code = uniqueValue("E2E-IMM")

        Promofire.codes.create(CreateCodeParams(value = code, templateId = immutable!!.id))

        val error = failure {
            Promofire.codes.update(code, UpdateCodeParams(payload = mapOf("changed" to "yes")))
        }

        assertEquals(PromofireException.Code.FORBIDDEN, error.code)
    }

    // MARK: - Клиент

    @Test
    fun `t17 identify связывает клиента с пользователем приложения`() = runBlocking {
        val appUserId = "e2e-user-${System.currentTimeMillis()}"

        val identified = Promofire.identify(appUserId, DEVICE)

        assertEquals(appUserId, identified.customerUserId)

        val again = Promofire.identify(appUserId, DEVICE)

        assertEquals("повторный identify не плодит клиентов", identified.id, again.id)
    }

    @Test
    fun `t18 updateProfile меняет только переданные поля`() = runBlocking {
        val updated = Promofire.customer.updateProfile(UpdateCustomerParams(firstName = "Ada"))

        assertEquals("Ada", updated.firstName)
        assertNull("непереданное поле не затёрлось", updated.lastName)
    }

    // MARK: - Границы

    @Test
    fun `t19 секрет не той длины отвергается до сетевого вызова`() {
        // Сверх переноса: проверка длины синхронная, и ошибка приходит из
        // configure, а не из первого запроса. Опечатка в секрете видна сразу
        // при старте приложения, а не когда пользователь дойдёт до экрана.
        //
        // Порядок безопасен: configure проверяет секрет до того, как что-либо
        // записать, поэтому рабочая настройка выше остаётся на месте.
        val error = failure {
            Promofire.configure(PromofireConfig(secret = "a".repeat(63), baseUrl = BASE_URL))
        }

        assertEquals(PromofireException.Code.VALIDATION_ERROR, error.code)
        assertNull("сетевого вызова не было, статуса нет", error.status)
    }

    @Test
    fun `t20 без активной сессии запрос не уходит`() = runBlocking {
        Promofire.disconnect()

        assertTrue(!Promofire.isAuthenticated)

        val error = failure { Promofire.customer.getProfile() }

        assertEquals(PromofireException.Code.NOT_AUTHENTICATED, error.code)
        assertNull("сетевого вызова не было, статуса нет", error.status)
    }

    @Test
    fun `t21 после disconnect можно войти заново`() = runBlocking {
        // Путь выхода и входа другим пользователем в демо-приложении.
        assertNotNull(Promofire.connect(DEVICE).id)
        assertTrue(Promofire.isAuthenticated)
    }
}
