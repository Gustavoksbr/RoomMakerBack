# 📌 RoomMakerBack

API em **Spring Boot** para gerenciamento de **salas virtuais**, utilizando **MongoDB**, autenticação via **JWT** e **WebSockets** para comunicação em tempo real

O sistema permite que usuários **criem, procurem, entrem, saiam e excluam salas**. Cada sala pode ser de diferentes categorias, como **Jogo da Velha**, **Jokenpô** ou só **Bate-papo**, e todas possuem um **chat em tempo real via WebSocket**. O dono também pode escolher ou não uma senha para entrar na sala

Também é possível recuperar senha da conta por email

Veja a aplicação completa hospedada [aqui](https://room-maker-front.vercel.app/)

Veja o código do Front-End [aqui](https://github.com/Gustavoksbr/RoomMakerFront)

---

### Documentação da API

- Requisições http para a entidade Sala: [./sala-requests.http](./sala-requests.http)
- Requisições http para a entidade Usuario: [./usuario-requests.http](./usuario-requests.http)