//package com.example.roommaker.games.examples.chat.repository;
//
//import com.example.roommaker.games.examples.chat.core.Message;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.mapping.Field;
//
//import java.time.LocalDateTime;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Document(collection = "message")
//public class MessageEntity {
//    @Field("ordem")
//    private Long ordem;
//    @Field("message")
//    private String message;
//    @Field("from")
//    private String from;
//    @Field("to")
//    private Long to;
//    @Field("timestamp")
//    private LocalDateTime timestamp;
//
//
//    public MessageEntity (Message message) {
//        this.message = message.getMessage();
//        this.from = message.getFrom();
//        this.to = message.getTo();
//        this.timestamp = message.getTimestamp();
//        this.ordem = message.getOrdem();
//    }
//    public Message toDomain() {
//        return Message.builder().message(message).from(from).to(to).timestamp(timestamp).ordem(ordem).build();
//    }
//}
