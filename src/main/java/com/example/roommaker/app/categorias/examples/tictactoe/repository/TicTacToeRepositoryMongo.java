package com.example.roommaker.app.categorias.examples.tictactoe.repository;

import com.example.roommaker.app.categorias.examples.tictactoe.repository.entity.TicTacToeDaSalaEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TicTacToeRepositoryMongo extends MongoRepository<TicTacToeDaSalaEntity, String> {
    boolean existsByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
     Optional<TicTacToeDaSalaEntity> findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
     void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono);
}
