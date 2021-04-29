package br.com.zup.edu.deleta

import br.com.zup.edu.clients.bcb.BcbClient
import br.com.zup.edu.clients.bcb.DeletaChavePixBcbRequest
import br.com.zup.edu.exceptions.ChavePixNaoEncontradaException
import br.com.zup.edu.compartilhado.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Singleton
@Validated
class DeletaChavePixService(@Inject val repository: ChavePixRepository,
                            @Inject val bcbClient: BcbClient,) {

    @Transactional
    fun remove(
        @NotBlank clienteId: String?,
        @NotBlank pixId: String?,
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chavePix = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
        .orElseThrow{ ChavePixNaoEncontradaException("Cliente não encontrado") }

        val bcbResponse = with(chavePix){
            repository.delete(this)
            val request = DeletaChavePixBcbRequest(chave)
            bcbClient.delete(key = chave, requestChaveBcb = request)
        }

        if (bcbResponse.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao remover chave Pix no BCB")
        }
    }
}