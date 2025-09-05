package com.example.roommaker.app.services.persistence.usuario.repository;

import com.example.roommaker.app.services.persistence.usuario.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoUsuarioRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByUsernameAndAtivoTrue(String username);
    List<UserEntity> findAllByAtivoTrue();
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    @Query("{ 'ativo': true, '$or': [ { 'nome': { '$regex': ?0, '$options': 'i' } }, { 'username': { '$regex': ?0, '$options': 'i' } } ] }")
    Optional<List<UserEntity>> encontrarPorSubstring(String procurada);

    Optional<UserEntity> findByEmailAndAtivoTrue(String email);
}
