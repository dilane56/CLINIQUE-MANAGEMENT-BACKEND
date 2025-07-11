package org.kfokam48.cliniquemanagementbackend.dto.auth;

import lombok.Data;


@Data
public class LoginResponse {
    private String token;
   // private String role;
    private UserDTO user;
    // + Getters/Setters
}
