package com.example.roommaker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guarda de segurança: os testes de integração escrevem no banco de TESTES.
 *
 * Este teste existe porque o contrário já aconteceu sem ninguém perceber. Os
 * testes de integração diziam no Javadoc que usavam ROOMMAKER_MONGODB_URI_TESTES,
 * mas não havia application-test.properties: o profile "test" não trocava nada, a
 * variável nunca era lida, e todos eles escreviam no banco de produção. Como cada
 * teste limpa só as próprias salas, nada quebrou de forma visível — que é
 * exatamente o motivo de ter passado despercebido.
 *
 * Se este teste falhar, PARE: alguma coisa apontou a suíte para o banco errado.
 */
@SpringBootTest(classes = RoommakerApplication.class)
@ActiveProfiles("test")
class BancoDeTestesTest {

    static {
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("a suíte não está conectada no banco de produção")
    void naoEhProducao() {
        String banco = mongoTemplate.getDb().getName();

        assertTrue(banco.contains("test"),
                "Os testes deveriam usar um banco de testes, mas estão em '" + banco
                        + "'. Confira src/test/resources/application-test.properties e a variável "
                        + "ROOMMAKER_MONGODB_URI_TESTES.");
    }

    @Test
    @DisplayName("o profile de teste aponta para o mesmo banco que o .env declara")
    void bateComOEnv() {
        String uriDeTestes = System.getProperty("ROOMMAKER_MONGODB_URI_TESTES");
        // Sem a variável, o default de application-test.properties é localhost —
        // seguro, e não é o caso que este teste precisa checar.
        if (uriDeTestes == null || uriDeTestes.isBlank()) {
            return;
        }

        String bancoEsperado = nomeDoBanco(uriDeTestes);
        assertEquals(bancoEsperado, mongoTemplate.getDb().getName(),
                "A conexão em uso não é a de ROOMMAKER_MONGODB_URI_TESTES.");
    }

    /** Extrai o nome do banco de uma URI do Mongo (o trecho entre '/' e '?'). */
    private String nomeDoBanco(String uri) {
        int inicio = uri.indexOf('/', uri.indexOf("://") + 3);
        if (inicio < 0)
            return "";
        int fim = uri.indexOf('?', inicio);
        return fim < 0 ? uri.substring(inicio + 1) : uri.substring(inicio + 1, fim);
    }
}
