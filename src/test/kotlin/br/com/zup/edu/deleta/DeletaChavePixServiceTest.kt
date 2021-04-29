package br.com.zup.edu.deleta

import br.com.zup.edu.*
import br.com.zup.edu.clients.bcb.BcbClient
import br.com.zup.edu.clients.bcb.DeletaChavePixBcbRequest
import br.com.zup.edu.clients.bcb.DeletaPixKeyResponse
import br.com.zup.edu.compartilhado.ChavePix
import br.com.zup.edu.compartilhado.ChavePixRepository
import br.com.zup.edu.compartilhado.TipoChave
import br.com.zup.edu.compartilhado.TipoConta
import br.com.zup.edu.cria.ContaAssociada
import br.com.zup.edu.cria.NovaChavePixServiceTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@MicronautTest(transactional = false)
internal class DeletaChavePixServiceTest(
    val repository: ChavePixRepository,
    val grpcClient: DeletaNovaChavePixServiceGrpc.DeletaNovaChavePixServiceBlockingStub){

    @Inject
    lateinit var bcbClient: BcbClient

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup(){

        CHAVE_EXISTENTE = repository.save(chave(
            tipo = TipoChave.EMAIL,
            chave = "anonimo@gmail.com",
            clienteId = UUID.randomUUID()
        ))
        val  conta = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "anonimo",
            cpfDoTitular = "64370752019",
            agencia = "1513",
            numeroDaConta = "666874"
        )
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }


    @Test
    fun `deve deletar chave pix`(){

        `when`(bcbClient.delete(CHAVE_EXISTENTE.chave, DeletaChavePixBcbRequest(CHAVE_EXISTENTE.chave, ContaAssociada.ITAU_UNIBANCO_ISPB))).thenReturn(
            HttpResponse.ok(DeletaPixKeyResponse(
                key = CHAVE_EXISTENTE.chave,
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                deletedAt = LocalDateTime.now()
            )))

        val response = grpcClient.deleta(
            DeletaChavePixRequest.newBuilder()
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build()
        )

        Assertions.assertEquals(0, repository.findAll().size)
    }

    @Test
    fun `nao deve deletar chave pix inexistente`(){

        repository.deleteAll()
        val thrown = assertThrows<StatusRuntimeException> {

            val uuid = UUID.randomUUID().toString()

            grpcClient.deleta(DeletaChavePixRequest.newBuilder()
                .setClienteId(uuid)
                .setPixId(uuid)
                .build())
        }

        with(thrown) {
            Assertions.assertEquals(Status.NOT_FOUND.code, status.code)
            Assertions.assertEquals("NOT_FOUND: Cliente não encontrado", message)
        }

    }

    @Test
    fun `nao deve remover se houver erro no bcb`(){

        `when`(bcbClient.delete(CHAVE_EXISTENTE.chave, DeletaChavePixBcbRequest(CHAVE_EXISTENTE.chave, ContaAssociada.ITAU_UNIBANCO_ISPB)))
            .thenReturn(HttpResponse.unprocessableEntity())

        val thrown = assertThrows<StatusRuntimeException> {
            val uuid = UUID.randomUUID().toString()

            grpcClient.deleta(DeletaChavePixRequest.newBuilder()
                .setClienteId(CHAVE_EXISTENTE.clienteId.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build())
        }

        with(thrown) {
            Assertions.assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            Assertions.assertEquals("Erro ao remover chave Pix no BCB", status.description)
        }
    }

    @Factory
    class Client{
        @Singleton
        fun blogStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletaNovaChavePixServiceGrpc.DeletaNovaChavePixServiceBlockingStub {
            return DeletaNovaChavePixServiceGrpc.newBlockingStub(channel);

        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return mock(BcbClient::class.java)
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
                cpfDoTitular = "00343116022",
                agencia = "1248",
                numeroDaConta = "123456"
            )
        )
    }

}