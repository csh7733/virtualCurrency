package practice.virtualcurrency.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.domain.order.State;
import practice.virtualcurrency.repository.member.MemberRepository;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    public Optional<Member> findMember(String username, String password) {
        return memberRepository.findByUsernameAndPassword(username, password);
    }

    @Override
    public void addCoin(Member member, Coin coin) {
        Double totalQuantity = member.getWallet().get(coin.getName()) + coin.getQuantity();
        member.getWallet().put(coin.getName(), totalQuantity);

    }

    @Override
    public void subCoin(Member member, Coin coin) {
        Double totalQuantity = member.getWallet().get(coin.getName()) - coin.getQuantity();
        member.getWallet().put(coin.getName(), totalQuantity);
    }

    //For Test
    @Override
    public void printWallet(Member member){
        log.info(member.getUsername() + "wallet start");
        member.getWallet().forEach((key, value) -> log.info("Coin: " + key + ", Quantity: " + value));
    }

    @Override
    public void printOrder(Member member) {
        List<Order> orders = member.getOrders();
        log.info("print {}'s order",member.getUsername());
        for(Order order : orders){
            log.info(order.toString());
        }
    }


}
