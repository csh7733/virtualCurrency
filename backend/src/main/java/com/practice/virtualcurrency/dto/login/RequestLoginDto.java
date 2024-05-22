package com.practice.virtualcurrency.dto.login;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestLoginDto {
    private String email;
    private String password;
}
