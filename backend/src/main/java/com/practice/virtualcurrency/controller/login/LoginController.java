package com.practice.virtualcurrency.controller.login;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.dto.login.RequestLoginDto;
import com.practice.virtualcurrency.dto.login.RequestRegisterDto;
import com.practice.virtualcurrency.jwt.JwtUtil;
import com.practice.virtualcurrency.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class LoginController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody RequestLoginDto requestLoginDto) {
        //아이디 비밀번호 확인
        Optional<Member> findMember = memberService.findMember(requestLoginDto.getEmail(), requestLoginDto.getPassword());
        if(findMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Member member = findMember.get();
        String jwt = JwtUtil.generateToken(member.getUsername(),member.getEmail());

        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RequestRegisterDto requestRegisterDto) {
        Optional<Member> findMember = memberService.findMemberByEmail(requestRegisterDto.getEmail());
        // 이메일 중복 확인
        if (findMember.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        // 유저 생성
        Member member = new Member(requestRegisterDto.getUserName(),requestRegisterDto.getEmail(),requestRegisterDto.getPassword());
        // 계정 생성
        memberService.join(member);
        return ResponseEntity.ok("ok");
    }

}
