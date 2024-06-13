package com.practice.virtualcurrency.service.member;



import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface MemberService {
    public Member join(Member member);
    public Optional<Member> findMember(String email,String password);
    public Optional<Member> findMemberByUsername(String username);
    public Optional<Member> findMemberByUsernameWithOrders(String username);
    public Optional<Member> findMemberByEmail(String email);
    public Double getCoin(Member member,String coinName);
    public void addCoin(Member member,String coinName,Double quantity);
    public void subCoin(Member member,String coinName,Double quantity);
    public Map<String, Double> getWallet(String username);

}
