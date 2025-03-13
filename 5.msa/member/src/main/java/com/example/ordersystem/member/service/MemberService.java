package com.example.ordersystem.member.service;

import com.example.ordersystem.member.dtos.LoginDto;
import com.example.ordersystem.member.dtos.MemberResDto;
import com.example.ordersystem.member.dtos.MemberSaveReqDto;
import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long save(MemberSaveReqDto memberSaveReqDto) {
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("존재하는 이메일입니다.");
        }
        if(memberSaveReqDto.getPassword().length()<8){
            throw new IllegalArgumentException("비번 너무 짧아요");
        }

        String password = passwordEncoder.encode(memberSaveReqDto.getPassword());
        Member member = memberRepository.save(memberSaveReqDto.toEntity(password));
        return member.getId();
    }

    public List<MemberResDto> findAll () {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(m->m.fromEntity()).toList();
    }

    public MemberResDto myInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("email not found"));
        return member.fromEntity();
    }

    public Member login(LoginDto dto){
        boolean check = true;
        //        email존재여부
        Optional<Member> optionalMember = memberRepository.findByEmail(dto.getEmail());
        if(!optionalMember.isPresent()){
            check = false;
        }
        //        password일치 여부
        if(!passwordEncoder.matches(dto.getPassword(), optionalMember.get().getPassword())){
            check =false;
        }
        if(!check){
            throw new IllegalArgumentException("email 또는 비밀번호가 일치하지 않습니다.");
        }
        return optionalMember.get();
    }

    public MemberResDto findByEmail() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("email not found"));
        return member.fromEntity();
    }


}
