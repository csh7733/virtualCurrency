package com.practice.virtualcurrency.service.userDetail;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberService.findMemberByUsername(username);

        if (optionalMember.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        Member member = optionalMember.get();
        return new MyUserDetails(member.getUsername(),member.getPassword(),member.getEmail(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
