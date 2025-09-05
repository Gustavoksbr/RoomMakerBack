package com.example.roommaker.app.services.categorias.examples.tictactoe.repository;

import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.domain.exceptions.SalaNaoEncontrada;
import com.example.roommaker.app.services.categorias.examples.tictactoe.domain.models.TicTacToeSala;
import com.example.roommaker.app.services.categorias.examples.tictactoe.repository.entity.TicTacToeDaSalaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TicTacToeRepository {
    private final TicTacToeRepositoryMongo ticTacToeRepositoryMongo;
    @Autowired
    public TicTacToeRepository(TicTacToeRepositoryMongo ticTacToeRepositoryMongo) {
        this.ticTacToeRepositoryMongo = ticTacToeRepositoryMongo;
    }
    public void criar(TicTacToeSala ticTacToeSala){
        if (this.ticTacToeRepositoryMongo.existsByNomeSalaAndUsernameDono(ticTacToeSala.getNomeSala(), ticTacToeSala.getUsernameDono())) {
            throw new Erro409("Sala já existe");
        }
        this.ticTacToeRepositoryMongo.save(new TicTacToeDaSalaEntity(ticTacToeSala));
    }
    public void salvar(TicTacToeSala ticTacToeSala){
        TicTacToeDaSalaEntity ticTacToeDaSalaEntity = this.ticTacToeRepositoryMongo.findByNomeSalaAndUsernameDono(ticTacToeSala.getNomeSala(), ticTacToeSala.getUsernameDono()).orElse(null);
        if(ticTacToeDaSalaEntity==null){
            throw new SalaNaoEncontrada(ticTacToeSala.getNomeSala(), ticTacToeSala.getUsernameDono());
        }
        ticTacToeDaSalaEntity.atualizar(ticTacToeSala);
        this.ticTacToeRepositoryMongo.save(ticTacToeDaSalaEntity);
    }
//    public void save(TicTacToeSala ticTacToeDaSala){
//        this.ticTacToeRepositoryMongo.save(new TicTacToeDaSalaEntity(ticTacToeDaSala));
//    }
    public Boolean existsByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        return this.ticTacToeRepositoryMongo.existsByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }
    public TicTacToeSala findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        return this.ticTacToeRepositoryMongo.findByNomeSalaAndUsernameDono(nomeSala, usernameDono).map(TicTacToeDaSalaEntity::toDomain).orElse(null);
    }
    public void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        this.ticTacToeRepositoryMongo.deleteByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }
}
