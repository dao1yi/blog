package com.xmq.model;

import lombok.Data;

@Data
public class LoginUser {
    private Long id;
    private String username;
    private String role;
    private String token;
} 