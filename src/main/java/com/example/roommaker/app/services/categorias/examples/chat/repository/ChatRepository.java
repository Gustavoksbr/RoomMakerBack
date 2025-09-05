package com.example.roommaker.app.services.categorias.examples.chat.repository;

import com.example.roommaker.app.domain.exceptions.Erro404;
import com.example.roommaker.app.domain.exceptions.Erro409;
import com.example.roommaker.app.services.categorias.examples.chat.core.Chat;
import com.example.roommaker.app.services.categorias.examples.chat.core.Message;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    private final ChatRepositoryMongo chatRepositoryMongo;
    public ChatRepository(ChatRepositoryMongo chatRepositoryMongo) {
        this.chatRepositoryMongo = chatRepositoryMongo;
    }
    public Chat criar(Chat chat) {
        if(this.chatRepositoryMongo.existsByUsernameDonoAndNomeSala(chat.getUsernameDono(), chat.getNome())) {
            throw new Erro409("Chat já existe");
        }
      return this.chatRepositoryMongo.save(new ChatEntity(chat)).toDomain();
    }

//    public Chat salvar(Chat chat) {
//        ChatEntity chatEntity = this.chatRepositoryMongo.findByUsernameDonoAndNomeSala(chat.getUsernameDono(), chat.getNomeSala()).orElse(null);
//        if(chatEntity == null) {
//            throw new Erro404("Chat não encontrado");
//        }
//        chatEntity.atualizar(chat);
//        return this.chatRepositoryMongo.save(chatEntity).toDomain();
//    }
    public void adicionarMensagem(String usernameDono, String nomeSala, Message message) {
        ChatEntity chatEntity = this.chatRepositoryMongo.findByUsernameDonoAndNomeSala(usernameDono, nomeSala).orElse(null);
        if(chatEntity == null) {
            throw new Erro404("Chat não encontrado");
        }
        Long respondendoTal = message.getTo();
        if (respondendoTal != null) {
            // verificar o intervalo de ordem maior que 0 e menor que chat.getContador(). Nao é necessary mas melhora a performance
//            boolean found = false;
//            for (Message messageEntity : chatEntity.getMessages()) {
//                if (messageEntity.getOrdem().equals(message.getTo())) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                throw new Erro404("Mensagem não encontrada");
//            }
            if((respondendoTal> chatEntity.getContador()) || (respondendoTal < 1)){
                throw new Erro404("Mensagem não encontrada");
            }
        }
        chatEntity.adicionarMensagem(message);
        this.chatRepositoryMongo.save(chatEntity);
    }

//    public void adicionarResposta(String usernameDono, String nomeSala, Message message) {
//
//    }

    public Boolean existsByUsernameDonoAndNomeSala(String usernameDono, String nomeSala) {
        return this.chatRepositoryMongo.existsByUsernameDonoAndNomeSala(usernameDono, nomeSala);
    }
    public Chat findByUsernameDonoAndNomeSala(String usernameDono, String nomeSala) {
        return this.chatRepositoryMongo.findByUsernameDonoAndNomeSala(usernameDono, nomeSala).map(ChatEntity::toDomain).orElse(null);
    }
    public void deleteByUsernameDonoAndNomeSala(String usernameDono, String nomeSala) {
        this.chatRepositoryMongo.deleteByUsernameDonoAndNomeSala(usernameDono, nomeSala);
    }
}
