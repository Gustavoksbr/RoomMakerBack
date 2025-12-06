package com.example.roommaker.app.categorias.examples.jokenpo.repository;

import com.example.roommaker.app.domain.exceptions.Erro404;
import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.categorias.examples.jokenpo.domain.model.JokenpoSala;
import com.example.roommaker.app.categorias.examples.jokenpo.repository.entity.JokenpoDaSalaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class JokenpoRepository {
    private JokenpoMongo jokenpoMongoRepository;
    @Autowired
    public JokenpoRepository(JokenpoMongo jokenpoMongoRepository) {
        this.jokenpoMongoRepository = jokenpoMongoRepository;
    }
    public JokenpoSala criar(JokenpoSala jokenpoDaSala){
        if (this.jokenpoMongoRepository.existsByNomeSalaAndUsernameDono(jokenpoDaSala.getNomeSala(), jokenpoDaSala.getUsernameDono())) {
            throw new Erro409("Sala jokenpo já existe");
        }
       return this.jokenpoMongoRepository.save(new JokenpoDaSalaEntity(jokenpoDaSala)).toDomain();
    }
    public JokenpoSala salvar(JokenpoSala jokenpoDaSala){
        JokenpoDaSalaEntity jokenpoDaSalaEntity = this.jokenpoMongoRepository.findByNomeSalaAndUsernameDono(jokenpoDaSala.getNomeSala(),jokenpoDaSala.getUsernameDono()).orElse(null);
        if(jokenpoDaSalaEntity==null){
            throw new Erro404("Sala jokenpo não encontrada");
        }
        jokenpoDaSalaEntity.atualizar(jokenpoDaSala);
       return   this.jokenpoMongoRepository.save(jokenpoDaSalaEntity).toDomain();
    }
    public Boolean existsByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        return this.jokenpoMongoRepository.existsByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }
    public JokenpoSala findByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        return this.jokenpoMongoRepository.findByNomeSalaAndUsernameDono(nomeSala, usernameDono).map(JokenpoDaSalaEntity::toDomain).orElse(null);
    }
    public void deleteByNomeSalaAndUsernameDono(String nomeSala, String usernameDono){
        this.jokenpoMongoRepository.deleteByNomeSalaAndUsernameDono(nomeSala, usernameDono);
    }
}


