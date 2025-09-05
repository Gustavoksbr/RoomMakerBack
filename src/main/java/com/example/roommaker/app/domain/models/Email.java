package com.example.roommaker.app.domain.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Email {
    private String to;
    private String subject;
    private String body;
}
