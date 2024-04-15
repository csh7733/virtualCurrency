package practice.virtualcurrency.service.member;

import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;

import java.util.Optional;

public interface MemberService {
    public Member join(Member member);
    public Optional<Member> findMember(String username,String password);
    public void addCoin(Member member, Coin coin);
    public void subCoin(Member member, Coin coin);
    //For Test
    public void printWallet(Member member);
    public void printOrder(Member member);
}
