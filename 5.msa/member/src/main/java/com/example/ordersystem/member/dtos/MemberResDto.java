package com.example.ordersystem.member.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberResDto {
    private Long id;
    private String name;
    private String email;
    private Integer orderingCount;
}
