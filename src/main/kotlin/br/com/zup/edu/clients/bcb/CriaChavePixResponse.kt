package br.com.zup.edu.clients.bcb

import br.com.zup.edu.compartilhado.TipoConta
import java.time.LocalDateTime

data class CreatePixKeyResponse (
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

data class BankAccount(

    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {


    enum class AccountType() {

        CACC, // corrente
        SVGS; // poupança

        companion object {
            fun by(domainType: TipoConta): AccountType {
                return when (domainType) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }

}