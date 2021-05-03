package br.com.zup.edu.consulta

import br.com.zup.edu.ConsultaChavePixRequest
import br.com.zup.edu.ConsultaChavePixResponse
import br.com.zup.edu.ConsultaChavePixServiceGrpc
import br.com.zup.edu.clients.bcb.BcbClient
import br.com.zup.edu.compartilhado.ChavePixRepository
import br.com.zup.edu.exceptions.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ConsultaChaveEndpoint(
    @Inject private val repository: ChavePixRepository, // 1
    @Inject private val bcbClient:BcbClient, // 1
    @Inject private val validator: Validator
):ConsultaChavePixServiceGrpc.ConsultaChavePixServiceImplBase(){

    override fun consulta(
        request: ConsultaChavePixRequest?,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?
    ) {
        val filtro = request?.toModel(validator) // 2
        val chaveInfo = filtro?.filtra(repository = repository, bcbClient = bcbClient)


    }
}
