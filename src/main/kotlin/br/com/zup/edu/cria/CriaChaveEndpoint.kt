package br.com.zup.edu.cria

import br.com.zup.edu.CriaNovaChavePixServiceGrpc
import br.com.zup.edu.NovaChavePixRequest
import br.com.zup.edu.NovaChavePixResponse
import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.TipoChave
import br.com.zup.edu.compartilhado.TipoConta
import br.com.zup.edu.exceptions.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class CriaChavePixEndpoint(@Inject val service: NovaChavePixService) : CriaNovaChavePixServiceGrpc.CriaNovaChavePixServiceImplBase() {

    override fun cria(request: NovaChavePixRequest, responseObserver: StreamObserver<NovaChavePixResponse>){

       request.toChavePixValidavel()
           .let(service::criaChavePix)
           .let(::montaResponse)
           .let(responseObserver::onNext)
           .let{ _ -> responseObserver.onCompleted() }
    }

    fun montaResponse(chavePix: ChavePix):NovaChavePixResponse{
        return NovaChavePixResponse.newBuilder()
            .setClienteId(chavePix.clienteId.toString())
            .setPixId(chavePix.id.toString())
            .build()
    }
}

private fun NovaChavePixRequest.toChavePixValidavel(): ChavePixValidavel {
    return ChavePixValidavel(this.clienteID, TipoChave.valueOf(this.tipoDeChave.name), this.chave, TipoConta.valueOf( this.tipoDeConta.name))
}
