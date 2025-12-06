package com.example.roommaker.app.categorias.examples.whoistheimpostor.repository;

import com.example.roommaker.app.categorias.examples.whoistheimpostor.domain.models.WhoIsTheImpostor;
import com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.entity.WhoIsTheImpostorEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WhoIsTheImpostorRepository {
    private final WhoIsTheImpostorRepositoryMongo whoIsTheImpostorRepositoryMongo;
    @Autowired
    public WhoIsTheImpostorRepository(WhoIsTheImpostorRepositoryMongo whoIsTheImpostorRepositoryMongo) {
        this.whoIsTheImpostorRepositoryMongo = whoIsTheImpostorRepositoryMongo;
    }

    public WhoIsTheImpostor findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        return whoIsTheImpostorRepositoryMongo.findByNomeSalaAndUsernameDono(nomeSala,usernameDono)
                .map(WhoIsTheImpostorEntity::toDomain)
                .orElse(null);
    }
    public WhoIsTheImpostor save(WhoIsTheImpostor whoIsTheImpostor) {
        WhoIsTheImpostorEntity existing =
                whoIsTheImpostorRepositoryMongo
                        .findByNomeSalaAndUsernameDono(
                                whoIsTheImpostor.getNomeSala(),
                                whoIsTheImpostor.getUsernameDono()
                        )
                        .orElse(null);

        WhoIsTheImpostorEntity entityToSave =
                WhoIsTheImpostorEntity.fromDomain(whoIsTheImpostor);
        if (existing != null) {
            entityToSave.setId(existing.getId());
        }
        whoIsTheImpostorRepositoryMongo.save(entityToSave);
        return whoIsTheImpostor;
    }


    public void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        this.whoIsTheImpostorRepositoryMongo.deleteByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }


}
