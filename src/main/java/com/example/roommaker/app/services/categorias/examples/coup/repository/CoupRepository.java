package com.example.roommaker.app.services.categorias.examples.coup.repository;

import com.example.roommaker.app.domain.exceptions.Erro404;
import com.example.roommaker.app.services.categorias.examples.coup.domain.model.SalaDosCoups;
import com.example.roommaker.app.services.categorias.examples.coup.repository.entity.SalaDosCoupsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CoupRepository {
    private final SalasDosCoupsMongoRepository salasDosCoupsMongoRepository;
    @Autowired
    public CoupRepository(SalasDosCoupsMongoRepository salasDosCoupsMongoRepository) {
        this.salasDosCoupsMongoRepository = salasDosCoupsMongoRepository;
    }
    public SalaDosCoups criarSalaDosCoups(SalaDosCoups salaDosCoups){
        if(this.salasDosCoupsMongoRepository.existsByNomeSalaAndUsernameDono(salaDosCoups.getNomeSala(), salaDosCoups.getUsernameDono())){
            throw new RuntimeException("Sala de coup já existe");
        }
        SalaDosCoupsEntity salaDosCoupsEntity = new SalaDosCoupsEntity(salaDosCoups);
        return this.salasDosCoupsMongoRepository.save(salaDosCoupsEntity).toModel();
    }
    public SalaDosCoups buscarSalaDosCoups(String nomeSala, String usernameDono){
        return this.salasDosCoupsMongoRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono)
                .orElseThrow(() -> new Erro404("Sala não encontrada"))
                .toModel();
    }
    public SalaDosCoups salvarSalaDosCoups(SalaDosCoups salaDosCoups){
        SalaDosCoupsEntity salaDosCoupsEntity = this.salasDosCoupsMongoRepository.findByNomeSalaAndUsernameDono(salaDosCoups.getNomeSala(), salaDosCoups.getUsernameDono())
                .orElseThrow(() -> new Erro404("Sala não encontrada"));
        salaDosCoupsEntity.atualizar(salaDosCoups);
       return this.salasDosCoupsMongoRepository.save(salaDosCoupsEntity).toModel();
    }
}
