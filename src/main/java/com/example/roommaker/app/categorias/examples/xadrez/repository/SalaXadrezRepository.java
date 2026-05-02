package com.example.roommaker.app.categorias.examples.xadrez.repository;

import com.example.roommaker.app.categorias.examples.xadrez.domain.model.SalaXadrez;
import com.example.roommaker.app.categorias.examples.xadrez.repository.entity.SalaXadrezEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SalaXadrezRepository {

    private final SalaXadrezRepositoryMongo mongo;

    @Autowired
    public SalaXadrezRepository(SalaXadrezRepositoryMongo mongo) {
        this.mongo = mongo;
    }

    public SalaXadrez findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono) {
        return mongo.findByNomeSalaAndUsernameDono(nomeSala, usernameDono)
                .map(SalaXadrezEntity::toDomain)
                .orElse(null);
    }

    public SalaXadrez save(SalaXadrez salaXadrez) {
        SalaXadrezEntity existing = mongo
                .findByNomeSalaAndUsernameDono(salaXadrez.getNomeSala(), salaXadrez.getUsernameDono())
                .orElse(null);

        SalaXadrezEntity entity = SalaXadrezEntity.fromDomain(salaXadrez);
        if (existing != null) {
            entity.setId(existing.getId());
        }
        mongo.save(entity);
        return salaXadrez;
    }

    public void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono) {
        mongo.deleteByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }
}
