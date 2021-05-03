package br.com.zup.edu.consulta

import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.TipoChave
import br.com.zup.edu.compartilhado.TipoConta
import br.com.zup.edu.cria.ContaAssociada
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipo,
                chave = chave.chave,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}