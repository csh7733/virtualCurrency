package com.practice.virtualcurrency.service.member;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    @Override
    public Member join(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Optional<Member> findMember(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public Optional<Member> findMemberByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Override
    public Optional<Member> findMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Double getCoin(Member member, String coinName) {
        return member.getWallet().get(coinName);
    }

    @Override
    public void addCoin(Member member, String coinName, Double quantity) {
        Double totalQuantity = member.getWallet().get(coinName) + quantity;
        member.getWallet().put(coinName, totalQuantity);
    }

    @Override
    public void subCoin(Member member, String coinName, Double quantity) {
        Double totalQuantity = member.getWallet().get(coinName) - quantity;
        member.getWallet().put(coinName, totalQuantity);
    }


}
