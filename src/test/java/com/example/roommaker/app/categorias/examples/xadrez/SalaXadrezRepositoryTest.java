package com.example.roommaker.app.categorias.examples.xadrez;

import com.example.roommaker.RoommakerApplication;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ControleTempoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.NotacaoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.PartidaXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.ResultadoXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.domain.model.SalaXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.repository.SalaXadrezRepository;
import com.example.roommaker.app.categorias.examples.xadrez.repository.entity.SalaXadrezEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prova que {@link SalaXadrezRepository#save} continua fazendo UPDATE, e não
 * INSERT, na segunda vez em diante.
 *
 * Existe porque o save() passou a usar findAndReplace com upsert casando por
 * (usernameDono, nomeSala) via Criteria — em vez de embutir o _id do
 * documento existente, que era descoberto com uma leitura extra a cada save.
 * Se o mapeamento dos nomes de campo (usernameDono -> username_dono, nomeSala
 * -> nome_sala) falhar silenciosamente, a Query não casa com nada, e cada
 * save() cria um documento NOVO em vez de atualizar o existente — perda de
 * dados silenciosa em produção, exatamente o tipo de bug que passa
 * despercebido na compilação. Este teste teria pego isso.
 */
@SpringBootTest(classes = RoommakerApplication.class)
@ActiveProfiles("test")
class SalaXadrezRepositoryTest {

    static {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

    @Autowired
    private SalaXadrezRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String DONO = "donoRepoTest";
    private static final String SALA = "salaRepoTest";

    @BeforeEach
    void limpar() {
        repository.deleteByNomeSalaAndUsernameDono(SALA, DONO);
    }

    @AfterEach
    void limparDepois() {
        repository.deleteByNomeSalaAndUsernameDono(SALA, DONO);
    }

    private long contarDocumentosNoMongo() {
        Query chave = Query.query(Criteria
                .where("usernameDono").is(DONO)
                .and("nomeSala").is(SALA));
        return mongoTemplate.count(chave, SalaXadrezEntity.class);
    }

    @Test
    @DisplayName("save() repetido atualiza o mesmo documento, não cria um novo a cada vez")
    void naoDuplicaDocumento() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA)
                .usernameDono(DONO)
                .notacao(NotacaoXadrez.INGLESA)
                .build();

        repository.save(sala); // insert
        assertEquals(1, contarDocumentosNoMongo(), "primeiro save deveria criar exatamente 1 documento");

        sala.setNotacao(NotacaoXadrez.PORTUGUESA);
        repository.save(sala); // devia ser update
        assertEquals(1, contarDocumentosNoMongo(),
                "segundo save deveria ATUALIZAR o mesmo documento, não criar um segundo");

        sala.setNotacao(NotacaoXadrez.INGLESA);
        repository.save(sala); // e de novo
        repository.save(sala);
        assertEquals(1, contarDocumentosNoMongo(),
                "vários saves seguidos continuam sendo o mesmo documento único");
    }

    @Test
    @DisplayName("save() persiste de verdade a mudança mais recente — não fica preso na versão antiga")
    void persisteAMudancaMaisRecente() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA)
                .usernameDono(DONO)
                .notacao(NotacaoXadrez.INGLESA)
                .build();
        repository.save(sala);

        sala.setNotacao(NotacaoXadrez.PORTUGUESA);
        sala.setModoVisual(true);
        repository.save(sala);

        SalaXadrez relido = repository.findByNomeSalaAndUsernameDono(SALA, DONO);
        assertNotNull(relido);
        assertEquals(NotacaoXadrez.PORTUGUESA, relido.getNotacao());
        assertEquals(Boolean.TRUE, relido.getModoVisual());
    }

    // =========================================================================
    // buscarPartidasAtivasComTempo — a query que substituiu o findAll() do
    // XadrezTimeoutScheduler
    // =========================================================================

    private ControleTempoXadrez tempoFinito() {
        return ControleTempoXadrez.builder()
                .tempoInicialBrancas(60_000L)
                .tempoInicialPretas(60_000L)
                .build();
    }

    private PartidaXadrez.PartidaXadrezBuilder partidaBase() {
        return PartidaXadrez.builder().id(1L).resultado(ResultadoXadrez.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("acha partida em andamento COM relógio — o caso que o scheduler precisa detectar")
    void achaPartidaAtivaComTempo() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA).usernameDono(DONO).notacao(NotacaoXadrez.INGLESA)
                .partidaAtual(partidaBase().controleTempo(tempoFinito()).build())
                .build();
        repository.save(sala);

        List<SalaXadrez> ativas = repository.buscarPartidasAtivasComTempo();

        assertTrue(ativas.stream().anyMatch(s -> DONO.equals(s.getUsernameDono()) && SALA.equals(s.getNomeSala())),
                "a query deveria ter achado esta sala — se não achou, o mapeamento dos campos "
                        + "aninhados (partidaAtual.resultado / .tempoInicialBrancas) está errado");
    }

    @Test
    @DisplayName("NÃO acha partida com tempo infinito — não há o que estourar")
    void naoAchaTempoInfinito() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA).usernameDono(DONO).notacao(NotacaoXadrez.INGLESA)
                .partidaAtual(partidaBase().controleTempo(null).build())
                .build();
        repository.save(sala);

        List<SalaXadrez> ativas = repository.buscarPartidasAtivasComTempo();

        assertFalse(ativas.stream().anyMatch(s -> DONO.equals(s.getUsernameDono()) && SALA.equals(s.getNomeSala())));
    }

    @Test
    @DisplayName("NÃO acha partida já encerrada, mesmo com relógio")
    void naoAchaPartidaEncerrada() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA).usernameDono(DONO).notacao(NotacaoXadrez.INGLESA)
                .partidaAtual(partidaBase()
                        .resultado(ResultadoXadrez.VITORIA_BRANCAS)
                        .controleTempo(tempoFinito())
                        .build())
                .build();
        repository.save(sala);

        List<SalaXadrez> ativas = repository.buscarPartidasAtivasComTempo();

        assertFalse(ativas.stream().anyMatch(s -> DONO.equals(s.getUsernameDono()) && SALA.equals(s.getNomeSala())));
    }

    @Test
    @DisplayName("NÃO acha sala sem partida nenhuma")
    void naoAchaSemPartida() {
        SalaXadrez sala = SalaXadrez.builder()
                .nomeSala(SALA).usernameDono(DONO).notacao(NotacaoXadrez.INGLESA)
                .build();
        repository.save(sala);

        List<SalaXadrez> ativas = repository.buscarPartidasAtivasComTempo();

        assertFalse(ativas.stream().anyMatch(s -> DONO.equals(s.getUsernameDono()) && SALA.equals(s.getNomeSala())));
    }

    @Test
    @DisplayName("duas salas diferentes não colidem uma com a outra")
    void naoColideEntreSalas() {
        SalaXadrez salaA = SalaXadrez.builder()
                .nomeSala(SALA).usernameDono(DONO).notacao(NotacaoXadrez.INGLESA).build();
        SalaXadrez salaB = SalaXadrez.builder()
                .nomeSala(SALA + "B").usernameDono(DONO).notacao(NotacaoXadrez.PORTUGUESA).build();

        try {
            repository.save(salaA);
            repository.save(salaB);

            assertEquals(1, contarDocumentosNoMongo());
            assertEquals(NotacaoXadrez.INGLESA,
                    repository.findByNomeSalaAndUsernameDono(SALA, DONO).getNotacao());
            assertEquals(NotacaoXadrez.PORTUGUESA,
                    repository.findByNomeSalaAndUsernameDono(SALA + "B", DONO).getNotacao());
        } finally {
            repository.deleteByNomeSalaAndUsernameDono(SALA + "B", DONO);
        }
    }
}
