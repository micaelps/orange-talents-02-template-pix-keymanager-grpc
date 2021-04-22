package br.com.zup.edu.compartilhado

import br.com.zup.edu.cria.ContaAssociada
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(@field:NotNull
               @Column(nullable = false)
               val clienteId: UUID,

               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipo: TipoChave,

               @Column(unique = true, nullable = false)
               var chave: String,

               @Enumerated(EnumType.STRING)
               @Column(nullable = false)
               val tipoConta: TipoConta,

               @Embedded
               val conta: ContaAssociada
){

    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)

    fun isAleatoria(): Boolean {
        return tipo == TipoChave.ALEATORIA
    }

    fun atualiza(chave: String){
        if (isAleatoria())
            this.chave = chave
    }

}
