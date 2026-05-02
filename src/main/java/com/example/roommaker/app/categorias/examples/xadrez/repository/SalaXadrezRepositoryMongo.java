package com.example.roommaker.app.categorias.examples.xadrez.repository;

import com.example.roommaker.app.categorias.examples.xadrez.repository.entity.SalaXadrezEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaXadrezRepositoryMongo extends MongoRepository<SalaXadrezEntity, String> {
    Optional<SalaXadrezEntity> findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);

    void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
}
