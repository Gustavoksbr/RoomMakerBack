package com.example.roommaker.app.categorias.examples.jokenpo.repository;

import com.example.roommaker.app.categorias.examples.jokenpo.repository.entity.JokenpoDaSalaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JokenpoMongo extends MongoRepository<JokenpoDaSalaEntity, String> {
    boolean existsByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
    Optional<JokenpoDaSalaEntity> findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
    void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
}
