package br.com.zup.edu.clients.bcb

import br.com.zup.edu.cria.ContaAssociada

data class DeletaPixKeyRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)

