package com.rits.site.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserWithPassword {
    private KeyCloakUserDetails user;
    private String password;
}
