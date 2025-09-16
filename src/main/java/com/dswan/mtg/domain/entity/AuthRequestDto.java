package com.dswan.mtg.domain.entity;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String email;
    private String username;
    private String password;
}