package com.rits.site.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KeyCloakUserDetails {
    protected String username;
    protected String firstName;
    protected String lastName;
    protected String email;
}
