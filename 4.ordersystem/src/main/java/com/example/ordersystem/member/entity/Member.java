package com.example.ordersystem.member.entity;

import com.example.ordersystem.common.entity.BaseTimeEntity;
import com.example.ordersystem.member.dtos.MemberResDto;
import com.example.ordersystem.ordering.entity.Ordering;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "member") //", cascade = CascadeType.ALL 없는 이유는 회원가입과 주문을 바로 하지 않기 때문임"
    private List<Ordering> orderingList;

    public MemberResDto fromEntity() {
        return MemberResDto.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .orderingCount(this.orderingList.size())
                .build();
    }
}
