package br.com.zup.edu.cria

import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.TipoChave
import br.com.zup.edu.compartilhado.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ChavePixValidavel(

    @field:NotBlank
    val clienteId: String?,

    @field:NotNull
    val tipoChave: TipoChave?,

    val chave: String?,

    @field:NotNull
    val tipoConta: TipoConta?) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipo = TipoChave.valueOf(this.tipoChave!!.name),
            chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoConta = TipoConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }
}