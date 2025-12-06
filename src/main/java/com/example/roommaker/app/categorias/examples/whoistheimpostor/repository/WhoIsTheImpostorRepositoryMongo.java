package com.example.roommaker.app.categorias.examples.whoistheimpostor.repository;


import com.example.roommaker.app.categorias.examples.whoistheimpostor.repository.entity.WhoIsTheImpostorEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WhoIsTheImpostorRepositoryMongo extends MongoRepository<WhoIsTheImpostorEntity,String> {
   Optional<WhoIsTheImpostorEntity> findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
   void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
}
