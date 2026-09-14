package io.promofire.models

/**
 * Единая форма списочного ответа.
 *
 * API возвращает массив под именем ресурса — `codes`, `redeems`, `templates`, —
 * а не под общим ключом. SDK приводит все три к [items].
 */
public data class PaginatedList<out T>(
    val items: List<T>,
    val total: Int,
)
