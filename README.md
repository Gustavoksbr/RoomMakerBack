# 📌 RoomMakerBack

API em **Spring Boot** para gerenciamento de **salas virtuais**, utilizando **MongoDB**, autenticação via **JWT** e **WebSockets** para comunicação em tempo real

O sistema permite que usuários **criem, procurem, entrem, saiam e excluam salas**. Cada sala pode ser de diferentes categorias, como **Jogo da Velha**, **Jokenpô** ou só **Bate-papo**, e todas possuem um **chat em tempo real via WebSocket**. O dono também pode escolher ou não uma senha para entrar na sala

Também é possível recuperar senha da conta por email

Veja a aplicação completa hospedada [aqui](https://room-maker-front.vercel.app/)

Veja o código do Front-End [aqui](https://github.com/Gustavoksbr/RoomMakerFront)

---

## 🚀 Tecnologias

- **Java 21** (LTS) com Virtual Threads
- **Spring Boot 3.3.4**
- **MongoDB**
- **JWT** (autenticação)
- **WebSockets** (STOMP)
- **Gradle**

---

## 📋 Requisitos

- **JDK 21** ou superior
- **MongoDB** rodando localmente ou URI configurada
- **Gradle** (incluído via wrapper)

---

## ⚙️ Configuração

### 1. Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com:

```env
ROOMMAKER_MONGODB_URI=mongodb://localhost:27017/roommaker?retryWrites=false
ROOMMAKER_JWT_SECRET=seu-secret-aqui
BREVO_API_KEY=sua-chave-brevo
BREVO_EMAIL_FROM=seu-email@exemplo.com
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
SELF_PING_URL=http://localhost:8080/ping
```

### 2. Rodar a Aplicação

```bash
# Desenvolvimento
./gradlew bootRun

# Build
./gradlew build

# Rodar JAR
java -jar build/libs/roommaker-0.0.1-SNAPSHOT.jar
```

### 3. Rodar Testes

```bash
./gradlew test
```

---

## 🎮 Features

### Virtual Threads (Java 21)
- ✅ Suporte a milhares de conexões WebSocket simultâneas
- ✅ Scheduler otimizado para verificação de timeout
- ✅ Melhor performance em operações I/O (MongoDB, HTTP)

### Categorias de Salas
- **Chat**: Bate-papo em tempo real
- **Jogo da Velha**: Jogo multiplayer
- **Jokenpô**: Pedra, papel, tesoura
- **Xadrez**: Xadrez às cegas com controle de tempo
- **Who is the Impostor**: Jogo de dedução social
Futuramente também será adicionado Coup

---

## 📚 Documentação da API

- Requisições http para a entidade Sala: [./sala-requests.http](./sala-requests.http)
- Requisições http para a entidade Usuario: [./usuario-requests.http](./usuario-requests.http)

---

## 🐳 Docker

```bash
# Build
docker build -t roommaker-back .

# Run
docker run -p 8080:8080 --env-file .env roommaker-back
```

---

## 📊 Performance

Com Java 21 e Virtual Threads:
- **10x mais jogadores simultâneos** comparado a Java 17
- **Menor latência** em WebSockets
- **Menor uso de memória** para threads
- **Melhor throughput** no scheduler de timeout