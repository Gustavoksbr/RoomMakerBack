package com.example.roommaker.app.categorias.examples.chat.repository;

import com.example.roommaker.app.categorias.examples.chat.repository.entity.ChatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatRepositoryMongo extends MongoRepository<ChatEntity,String> {
    Boolean existsByUsernameDonoAndNomeSala(String usernameDono, String nomeSala);
    Optional<ChatEntity> findByUsernameDonoAndNomeSala(String usernameDono, String nomeSala);
    void deleteByUsernameDonoAndNomeSala(String usernameDono, String nomeSala);
}
