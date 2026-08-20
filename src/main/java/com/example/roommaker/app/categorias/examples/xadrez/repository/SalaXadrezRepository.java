package com.example.roommaker.app.categorias.examples.xadrez.repository;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.SalaXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.repository.entity.SalaXadrezEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SalaXadrezRepository {

    private final SalaXadrezRepositoryMongo mongo;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SalaXadrezRepository(SalaXadrezRepositoryMongo mongo, MongoTemplate mongoTemplate) {
        this.mongo = mongo;
        this.mongoTemplate = mongoTemplate;
    }

    public SalaXadrez findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono) {
        return mongo.findByNomeSalaAndUsernameDono(nomeSala, usernameDono)
                .map(SalaXadrezEntity::toDomain)
                .orElse(null);
    }

    /**
     * Grava a sala inteira num único round-trip ao Mongo.
     *
     * Antes disto, salvar fazia uma LEITURA (só para descobrir o {@code _id} do
     * documento existente — o objeto de domínio não carrega isso, então sem essa
     * leitura o {@code save()} do Spring Data criaria um documento duplicado em
     * vez de atualizar) e SÓ DEPOIS a escrita. Um round-trip a mais em TODO
     * lance, TODO pré-lance, toda configuração de partida.
     *
     * A dupla (usernameDono, nomeSala) já é a chave natural da sala — já existe
     * um índice único (`unique_xadrez_sala`) garantindo isso —, então dá pra
     * usar {@code findAndReplace} com upsert: o próprio Mongo decide "é update
     * ou insert?" e troca o documento inteiro numa única operação atômica, sem
     * a aplicação precisar saber o {@code _id} de antemão nem arriscar uma
     * corrida entre o "achei o id" e o "salvei".
     */
    public SalaXadrez save(SalaXadrez salaXadrez) {
        SalaXadrezEntity entity = SalaXadrezEntity.fromDomain(salaXadrez);
        Query chaveNatural = Query.query(Criteria
                .where("usernameDono").is(salaXadrez.getUsernameDono())
                .and("nomeSala").is(salaXadrez.getNomeSala()));

        mongoTemplate.findAndReplace(chaveNatural, entity, FindAndReplaceOptions.options().upsert());
        return salaXadrez;
    }

    public void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono) {
        mongo.deleteByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }

    /**
     * As salas com uma partida em andamento E com relógio configurado — exatamente
     * o que {@link com.example.roommaker.app.categorias.examples.xadrez.scheduler.XadrezTimeoutScheduler}
     * varre a cada segundo pra achar quem estourou o tempo.
     *
     * Antes disto o scheduler chamava {@code findAll()}: baixava a coleção
     * INTEIRA — toda sala de xadrez já criada, com ou sem partida, com ou sem
     * relógio — e filtrava em memória, uma vez por segundo, pra sempre. Numa
     * coleção pequena isso passa despercebido; é o tipo de custo que cresce
     * junto com o produto e só aparece quando incomoda. Filtrar aqui, no
     * banco, é o mesmo resultado com uma fração do tráfego — e sem competir
     * por I/O com os lances de verdade, que é a queixa original.
     */
    public java.util.List<SalaXadrez> buscarPartidasAtivasComTempo() {
        Query query = Query.query(Criteria
                .where("partidaAtual.resultado").is("EM_ANDAMENTO")
                .orOperator(
                        Criteria.where("partidaAtual.tempoInicialBrancas").ne(null),
                        Criteria.where("partidaAtual.tempoInicialPretas").ne(null)));

        return mongoTemplate.find(query, SalaXadrezEntity.class).stream()
                .map(SalaXadrezEntity::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
}
