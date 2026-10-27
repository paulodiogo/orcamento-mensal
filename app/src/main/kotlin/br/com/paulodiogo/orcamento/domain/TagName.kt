package br.com.paulodiogo.orcamento.domain

import java.util.Locale

fun normalizeTagName(name: String): String {
    val normalized = name.trim().replace(Regex("\\s+"), " ")
    if (normalized.isEmpty()) throw DomainError.EmptyTagName
    return normalized.lowercase(Locale.ROOT)
}

fun displayTagName(name: String): String {
    val normalized = name.trim().replace(Regex("\\s+"), " ")
    if (normalized.isEmpty()) throw DomainError.EmptyTagName
    return normalized
}
