package br.com.zup.edu.deleta

import br.com.zup.edu.DeletaChavePixRequest
import br.com.zup.edu.DeletaChavePixResponse
import br.com.zup.edu.DeletaNovaChavePixServiceGrpc
import br.com.zup.edu.compartilhado.ChavePixRepository
import br.com.zup.edu.exceptions.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@Singleton
@ErrorHandler
class DeletaChavePixEndpoint(val deleteService: DeletaChavePixService): DeletaNovaChavePixServiceGrpc.DeletaNovaChavePixServiceImplBase() {

    override fun deleta(request: DeletaChavePixRequest, responseObserver: StreamObserver<DeletaChavePixResponse>) {

        deleteService.remove(request.clienteId, request.pixId)
        responseObserver.onNext(
            DeletaChavePixResponse.newBuilder() // 1
            .setClienteId(request.clienteId)
            .setPixId(request.pixId)
            .build())
        responseObserver.onCompleted()



    }
}