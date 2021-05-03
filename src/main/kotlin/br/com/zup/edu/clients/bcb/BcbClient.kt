package br.com.zup.edu.clients.bcb

import br.com.zup.edu.compartilhado.Instituicoes
import br.com.zup.edu.compartilhado.TipoConta
import br.com.zup.edu.consulta.ChavePixInfo
import br.com.zup.edu.cria.ContaAssociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime


@Client("\${bcb.pix.url}")
interface BcbClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun create(@Body request: CriaChavePixRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun delete(@PathVariable key: String, @Body requestChaveBcb: DeletaChavePixBcbRequest): HttpResponse<DeletaPixKeyResponse>

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

    data class PixKeyDetailsResponse (
        val keyType: PixKeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime
    ) {

        fun toModel(): ChavePixInfo {
            return ChavePixInfo(
                tipo = keyType.domainType!!,
                chave = this.key,
                tipoDeConta = when (this.bankAccount.accountType) {
                    BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                    BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                },
                conta = ContaAssociada(
                    instituicao = Instituicoes.nome(bankAccount.participant),
                    nomeDoTitular = owner.name,
                    cpfDoTitular = owner.taxIdNumber,
                    agencia = bankAccount.branch,
                    numeroDaConta = bankAccount.accountNumber
                )
            )
        }
    }

}