package com.example.roommaker.app.services.persistence.sala.repository;

import com.example.roommaker.app.domain.exceptions.*;
import com.example.roommaker.app.services.persistence.sala.entity.SalaEntity;
import com.example.roommaker.app.domain.models.Sala;
import com.example.roommaker.app.domain.ports.repository.SalaRepository;
import com.example.roommaker.app.domain.exceptions.ErroDeRequisicaoGeral;
import com.example.roommaker.app.domain.exceptions.UsuarioNaoAutorizado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class SalaRepositoryImpl implements SalaRepository {

    @Autowired
    MongoSalaRepository mongoSalaRepository;

    //metodos privados

    private SalaEntity entityFindByNomeAndUsernameDono(String nome, String usernameDono) {
        return this.mongoSalaRepository.findByNomeAndUsernameDono(nome,usernameDono).orElseThrow(() -> new SalaNaoEncontrada(nome, usernameDono));
    }

    @Override
    public List<Sala> listar(String usernameDono, String nomeSala, String categoria) {
//        return this.mongoSalaRepository.findByUsernameDonoAndNomeAndCategoria(usernameDono, nomeSala, categoria).stream().map(SalaEntity::toSala).toList();

//        return this.mongoSalaRepository.findAll().stream()
//                .filter(sala -> (usernameDono == null || usernameDono.isEmpty() || sala.getUsernameDono().equals(usernameDono)) &&
//                        (nomeSala == null || nomeSala.isEmpty() || sala.getNome().equals(nomeSala)) &&
//                        (categoria == null || categoria.isEmpty() || sala.getCategoria().equals(categoria)))
//                .map(SalaEntity::toSala)
//                .toList();

        return this.mongoSalaRepository.findByUsernameDonoAndNomeAndCategoriaSubstring(usernameDono, nomeSala, categoria).stream()
                .map(SalaEntity::toSala)
                .toList();
    }

    @Override
    public List<Sala> listarPorParticipante(String usernameParticipante) {
        return this.mongoSalaRepository.findByParticipante(usernameParticipante).stream().map(SalaEntity::toSala).toList();
    }

    public List<Sala> listarPorDono(String usernameDono){
        return this.mongoSalaRepository.findByUsernameDono(usernameDono).stream().map(SalaEntity::toSala).toList();
    }

    @Override
    public Sala criar(Sala sala) {
        if(this.mongoSalaRepository.existsByNomeAndUsernameDono(sala.getNome(), sala.getUsernameDono())){
            throw new SalaJaExiste(sala.getNome());
        }
        return this.mongoSalaRepository
                .save(new SalaEntity(sala))
                .toSala();
    }

    @Override
    public Sala mostrarSala(String nomeSala, String usernameDono) {
        return this.entityFindByNomeAndUsernameDono(nomeSala, usernameDono).toSala();
    }


//    @Override
//    public Sala editar(Sala sala) {
//        SalaEntity salaEntity = mongoSalaRepository.findById(sala.getOrdem()).orElseThrow(()->new SalaNaoEncontrada(sala.getOrdem()));
//        salaEntity.salvar(sala);
//        return mongoSalaRepository.save(salaEntity).toSala();
//    }
//    @Override
//    public void deletar(Long ordem) {
//        SalaEntity salaEntity = mongoSalaRepository.findById(ordem).orElseThrow(()->new SalaNaoEncontrada(ordem));
//        salaEntity.deletar();
//        mongoSalaRepository.save(salaEntity);
//    }
//
//    @Override
//    public Sala encontrarPorId(Long ordem) {
//        return mongoSalaRepository.findById(ordem).orElseThrow(()->new SalaNaoEncontrada(ordem)).toSala();
//    }

    @Override
    public Sala
    adicionarParticipante(String nomeSala, String usernameDono, String senha, String usernameParticipante) {
        SalaEntity salaEntity = this.entityFindByNomeAndUsernameDono(nomeSala, usernameDono);

        if(salaEntity.getUsernameParticipantes().contains(usernameParticipante)){
            throw new ErroDeRequisicaoGeral("Você já está na sala!");
        }
        if(salaEntity.getUsernameParticipantes().size() + 1 >= salaEntity.getQtdCapacidade()){ // + 1 pq inclui o dono
            throw new ErroDeRequisicaoGeral("Sala cheia!");
        }
        if(!salaEntity.getDisponivel()){
            throw new ErroDeRequisicaoGeral("Sala fechada!");
        }
        if(!(salaEntity.getSenha()==null || salaEntity.getSenha().isEmpty())){ // se tem senha. Se nao tiver, nao precisa verificar e pode adicionar
            if(!salaEntity.getSenha().equals(senha)){
                throw new UsuarioNaoAutorizado("Senha incorreta!");
        }}

        salaEntity.addParticipante(usernameParticipante);
        return mongoSalaRepository.save(salaEntity).toSala();
    }

    @Override
    public Sala verificarSeUsuarioEstaNaSalaERetornarSala(String nomeSala, String usernameDono, String usernameParticipante) {
        SalaEntity salaEntity = this.entityFindByNomeAndUsernameDono(nomeSala, usernameDono);
        if(!salaEntity.getUsernameParticipantes().contains(usernameParticipante) && !salaEntity.getUsernameDono().equals(usernameParticipante)){
            throw new ErroDeRequisicaoGeral("Usuário não está na sala!");
        }
        return salaEntity.toSala();
    }

    @Override
    public void excluirSala(String usernameDono,String nomeSala ) {
        SalaEntity salaEntity = this.entityFindByNomeAndUsernameDono(nomeSala, usernameDono);
        this.mongoSalaRepository.delete(salaEntity);
    }

    @Override
    public Sala sairDaSala(String usernameDono, String nomeSala, String usernameSaindo) {
        SalaEntity salaEntity = this.entityFindByNomeAndUsernameDono(nomeSala, usernameDono);
        salaEntity.removeParticipante(usernameSaindo);
        SalaEntity a = this.mongoSalaRepository.save(salaEntity);
        System.out.println("salaEntity: "+salaEntity);
        System.out.println("a: "+a);
        return salaEntity.toSala();
    }

}
