package com.practice.virtualcurrency.dto.member;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseUserInfoDto {
    private String username;
    private String email;
}
