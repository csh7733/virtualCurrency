package practice.virtualcurrency.service.member;

import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;

import java.util.Optional;

public interface MemberService {
    public Member join(Member member);
    public Optional<Member> findMember(String username,String password);
    public Double getCoin(Member member,String coinName);
    public void addCoin(Member member, Coin coin);
    public void subCoin(Member member, Coin coin);
    public void resetCoin(Member member, String coinName);
    //For Test
    public void printWallet(Member member);
    public void printOrder(Member member);
}
