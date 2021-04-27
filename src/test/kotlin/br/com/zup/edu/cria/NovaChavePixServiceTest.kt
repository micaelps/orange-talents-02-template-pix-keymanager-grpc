package br.com.zup.edu.cria

import br.com.zup.edu.CriaNovaChavePixServiceGrpc
import br.com.zup.edu.CriaNovaChavePixServiceGrpc.CriaNovaChavePixServiceBlockingStub
import br.com.zup.edu.NovaChavePixRequest
import org.mockito.ArgumentMatchers.any
import br.com.zup.edu.TipoDeChave
import br.com.zup.edu.TipoDeConta
import br.com.zup.edu.clients.bcb.*
import br.com.zup.edu.clients.itau.ContasItauClient
import br.com.zup.edu.clients.itau.DadosContaItauResponse
import br.com.zup.edu.clients.itau.InstituicaoResponse
import br.com.zup.edu.clients.itau.TitularResponse
import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.ChavePixRepository
import br.com.zup.edu.compartilhado.TipoChave
import br.com.zup.edu.compartilhado.TipoConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class NovaChavePixServiceTest(
    val repository: ChavePixRepository,
    val grpcClient:CriaNovaChavePixServiceBlockingStub
){

    @Inject
    lateinit var bcbClient: BcbClient
    @Inject
    lateinit var itauClient: ContasItauClient;

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @Test
    fun `deve criar uma chave - CPF`(){

        val request = createPixKeyRequest(key = "17092692008", keyType = PixKeyType.CPF)

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(request))
            .thenReturn(HttpResponse.created(createPixKeyResponse(request.keyType, request.key)))


        val response = grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave(request.key)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())

        with(response){
            assertTrue(repository.existsByChave(request.key))

        }
    }

    @Test
    fun `nao deve criar uma nova chave repetida`(){

        repository.save(chave(
            tipo = TipoChave.CPF,
            chave = "17092692008",
            clienteId = CLIENTE_ID
        ))

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("17092692008")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `nao deve criar com chave invalida - CPF`(){

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("17092692008...")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: Chave Pix inválida.", message)
        }
    }

    @Test
    fun `deve criar uma chave - EMAIL`(){

        val request = createPixKeyRequest(PixKeyType.EMAIL, "anonimo@email.com")

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(request))
            .thenReturn(HttpResponse.created(createPixKeyResponse(request.keyType, request.key)))


        val response = grpcClient.cria(NovaChavePixRequest.newBuilder()
            .setClientID(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave(request.key)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        with(response){
            assertTrue(repository.existsByChave(request.key))

        }
    }

    @Test
    fun `nao deve criar com chave invalida - EMAIL`(){

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("email.comm")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: Chave Pix inválida.", message)
        }
    }


    @Test
    fun `nao deve criar com chave invalida - CELULAR`(){

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CELULAR)
                .setChave("8690-8766")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: Chave Pix inválida.", message)
        }
    }


    @Test
    fun `deve criar uma chave - CELULAR`(){

        val request = createPixKeyRequest(PixKeyType.PHONE, "+5585988714077")

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.create(request))
            .thenReturn(HttpResponse.created(createPixKeyResponse(request.keyType, request.key)))


        val response = grpcClient.cria(NovaChavePixRequest.newBuilder()
            .setClientID(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.CELULAR)
            .setChave(request.key)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build())

        with(response){
            assertTrue(repository.existsByChave(request.key))

        }
    }


    @Test
    fun `nao deve criar chave se nao achar o cliente`(){

        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cria(NovaChavePixRequest.newBuilder()
                .setClientID(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.CELULAR)
                .setChave("+5585988714077")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("FAILED_PRECONDITION: Cliente não encontrado no Itau.", message)
        }
    }

    @Factory
    class Clients{
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CriaNovaChavePixServiceBlockingStub? {
            return CriaNovaChavePixServiceGrpc.newBlockingStub(channel);

        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @MockBean(ContasItauClient::class)
    fun itauClient(): ContasItauClient? {
        return Mockito.mock(ContasItauClient::class.java)
    }

    private fun dadosDaContaResponse(): DadosContaItauResponse {
        return DadosContaItauResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1513",
            numero = "666874",
            titular = TitularResponse("Anonimo", "17092692008")
        )
    }

    private fun createPixKeyRequest(keyType: PixKeyType, key: String): CriaChavePixRequest {
        return CriaChavePixRequest(
            keyType = keyType,
            key = key,
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponse(keyType: PixKeyType, key: String): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = keyType,
            key = key,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1513",
            accountNumber = "666874",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Anonimo",
            taxIdNumber = "17092692008"
        )
    }

    private fun chave(
        tipo: TipoChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Anonimo",
                cpfDoTitular = "17092692008",
                agencia = "1513",
                numeroDaConta = "666874"
            )
        )
    }

}