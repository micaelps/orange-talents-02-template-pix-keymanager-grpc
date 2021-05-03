package br.com.zup.edu.compartilhado

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, UUID> {
   fun existsByChave(chave: String?): Boolean

   fun findByIdAndClienteId(uuidPixId: UUID?, uuidClienteId: UUID?): Optional<ChavePix>

    fun findAllByClienteId(clienteId: UUID): List<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>
}