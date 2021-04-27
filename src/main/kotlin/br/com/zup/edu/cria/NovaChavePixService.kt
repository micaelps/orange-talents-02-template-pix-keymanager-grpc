package br.com.zup.edu.cria

import br.com.zup.edu.clients.bcb.BcbClient
import br.com.zup.edu.clients.bcb.CriaChavePixRequest
import br.com.zup.edu.clients.itau.ContasItauClient
import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.ChavePixRepository
import br.com.zup.edu.cria.exceptions.ChavePixExistenteException
import br.com.zup.edu.cria.exceptions.ChavePixInvalidaException
import io.micronaut.http.HttpStatus
//import br.com.zup.edu.compartilhado.ChavePixRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class NovaChavePixService(
    @Inject val bcbClient: BcbClient,
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasItauClient
){
    val logger = LoggerFactory.getLogger(this::class.java)

    fun criaChavePix(@Valid chavePixValidavel: ChavePixValidavel): ChavePix {

       val chave = validaChave(chavePixValidavel)
            .also { logger.info("Chave válida informada.") }
            .let(::buscaContaItau)
            .let(chavePixValidavel::toModel)
            .also { logger.info("Cliente encontrado.") }

       val bcbResponse = chave
            .let(repository::save)
            .let(CriaChavePixRequest::of)
           .also { criaChavePixRequest -> println(criaChavePixRequest) }
            .let(bcbClient::create)
            .also { logger.info("Chave Pix criada com sucesso.") }

        if (bcbResponse.status != HttpStatus.CREATED)
            throw IllegalStateException("Erro ao registrar chave Pix no (BCB)")

        chave.atualiza(bcbResponse.body()!!.key)
        return chave
    }

    fun buscaContaItau(chavePixValidavel: ChavePixValidavel): ContaAssociada {
        val response = itauClient.buscaContaPorTipo(chavePixValidavel.clienteId!!, chavePixValidavel.tipoConta!!.name)
        return response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau.")
    }

    fun validaChave(chavePixValidavel: ChavePixValidavel): ChavePixValidavel {

        if(!(chavePixValidavel.tipoChave!!.valida(chavePixValidavel.chave)))
            throw ChavePixInvalidaException("Chave Pix inválida.")

        if (repository.existsByChave(chavePixValidavel.chave))
            throw ChavePixExistenteException("Chave Pix já existe.")

        return chavePixValidavel
    }
}

