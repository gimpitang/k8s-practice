package com.example.ordersystem.member.dtos;

import com.example.ordersystem.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class MemberSaveReqDto {
    @NotEmpty
    private String name;

    private String email;

    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder().name(this.name).email(this.email).password(encodedPassword).build();
    }
}
