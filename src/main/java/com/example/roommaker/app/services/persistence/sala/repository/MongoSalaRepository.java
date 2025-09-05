package com.example.roommaker.app.services.persistence.sala.repository;

import com.example.roommaker.app.services.persistence.sala.entity.SalaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MongoSalaRepository extends MongoRepository<SalaEntity, Long> {
    Optional<SalaEntity> findByNomeAndUsernameDono(String nome, String usernameDono);

    Boolean existsByNomeAndUsernameDono(String nome, String usernameDono);

    @Query("{ '$or': [ { 'nome': { '$regex': ?0, '$options': 'i' } }, { 'username_dono': { '$regex': ?0, '$options': 'i' } }, { 'categoria': { '$regex': ?0, '$options': 'i' } } ] }")
    List<SalaEntity> findSubstring(@Param("procurado") String procurado);

    @Query("{ 'username_dono': ?0, 'nome': ?1, 'categoria': ?2 }")
    List<SalaEntity> findByUsernameDonoAndNomeAndCategoria(@Param("usernameDono") String usernameDono, @Param("nome") String nome, @Param("categoria") String categoria);

    List<SalaEntity> findByUsernameDono(String usernameDono);

    @Query("{ 'username_participantes': { '$in': [?0] } }")
    List<SalaEntity> findByParticipante(String participante);

    @Query("{ 'username_dono': { '$regex': ?0, '$options': 'i' }, 'nome': { '$regex': ?1, '$options': 'i' }, 'categoria': { '$regex': ?2, '$options': 'i' } }")
    List<SalaEntity> findByUsernameDonoAndNomeAndCategoriaSubstring(@Param("usernameDono") String usernameDono, @Param("nome") String nome, @Param("categoria") String categoria);

//    @Query("{ 'username_dono': { '$regex': ?0, '$options': 'i' }, 'nome': { '$regex': ?1, '$options': 'i' }, 'categoria': { '$regex': ?2, '$options': 'i' } }")
//    List<SalaEntity> findByUsernameDonoAndNomeAndCategoriaSubstring(@Param("usernameDono") String usernameDono, @Param("nome") String nome, @Param("categoria") String categoria);
}
