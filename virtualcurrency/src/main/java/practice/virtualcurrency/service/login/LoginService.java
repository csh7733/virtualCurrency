package practice.virtualcurrency.service.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.domain.order.Order;
import practice.virtualcurrency.repository.order.OrderRepository;
import practice.virtualcurrency.service.member.MemberService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {
    private final MemberService memberService;

    public Optional<Member> login(String username,String password){
        return memberService.findMember(username,password);
    }

}
