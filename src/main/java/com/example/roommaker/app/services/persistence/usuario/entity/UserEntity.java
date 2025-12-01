package com.example.roommaker.app.services.persistence.usuario.entity;

import com.example.roommaker.app.domain.models.Usuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Getter
@Setter
@Document(collection = "users")
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    private String id;

    @Field("username")
    @Indexed(unique = true)
    private String username;

    @Field("password")
    private String password;

    @Field("descricao")
    private String descricao;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    @Field("ativo")
    private Boolean ativo;

    @Field("dois_fatores")
    private Boolean doisFatores;

    @Field("data_nascimento")
    private LocalDate dataNascimento;

    public UserEntity() {
    }

    public UserEntity(Usuario usuario) {
        this.username = usuario.getUsername();
        this.password = usuario.getPassword();
        this.descricao = usuario.getDescricao();
        this.email = usuario.getEmail();
        this.ativo = true;
        this.doisFatores = false;
        this.dataNascimento = usuario.getDataNascimento();
    }

    public Usuario toUsuario() {
        return new Usuario(username, password, descricao, email, ativo, doisFatores,dataNascimento);
    }

    public Usuario toUsuarioSemSenha() {
        return new Usuario(username, null, descricao, email, null, null,dataNascimento);
    }
}
