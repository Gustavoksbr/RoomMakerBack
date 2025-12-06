package com.example.roommaker.app.categorias.examples.coup.repository;

import com.example.roommaker.app.categorias.examples.coup.repository.entity.SalaDosCoupsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SalasDosCoupsMongoRepository extends MongoRepository<SalaDosCoupsEntity,String> {
    Optional<SalaDosCoupsEntity> findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
    Boolean existsByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
//    Optional<SalaDosCoupsEntity> findByCoupAtual
}
