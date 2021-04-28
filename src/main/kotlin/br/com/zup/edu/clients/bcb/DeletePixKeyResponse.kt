package br.com.zup.edu.clients.bcb

import java.time.LocalDateTime

data class DeletaPixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

