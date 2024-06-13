package com.practice.virtualcurrency.controller.member;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.domain.order.Order;
import com.practice.virtualcurrency.dto.member.ResponseOrderDto;
import com.practice.virtualcurrency.dto.member.ResponseUserInfoDto;
import com.practice.virtualcurrency.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.practice.virtualcurrency.VirtualCurrencyConst.getCurrentMemberUsername;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping()
    public ResponseEntity<String> getMemberUsername() {
        String username = getCurrentMemberUsername();
        return ResponseEntity.ok(username);
    }
    @GetMapping("/wallet")
    public Map<String,Double> getMemberWallet() {
        String username = getCurrentMemberUsername();

        return memberService.getWallet(username);
    }

    @GetMapping("/info")
    public ResponseEntity<ResponseUserInfoDto> getMemberInfo() {
        String username = getCurrentMemberUsername();
        Optional<Member> optionalMember = memberService.findMemberByUsername(username);

        if (optionalMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 사용자 정보를 찾을 수 없을 때 404 상태 코드 반환
        }

        Member member = optionalMember.get();
        String email = member.getEmail();

        ResponseUserInfoDto response = ResponseUserInfoDto.builder()
                .username(username)
                .email(email)
                .build();

        return ResponseEntity.ok(response); // 200 상태 코드와 함께 사용자 정보 반환
    }

    @GetMapping("/orders")
    public ResponseEntity<List<ResponseOrderDto>> getMemberOrders() {

        String username = getCurrentMemberUsername();
        Optional<Member> optionalMember = memberService.findMemberByUsername(username);

        if (optionalMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Member member = optionalMember.get();
        List<ResponseOrderDto> orders = member.getOrders().stream()
                .map(order -> new ResponseOrderDto(order.getId(), order.getCoinName(), order.getTime(),
                        order.getOrderType(), order.getPrice(), order.getQuantity(), order.getState()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }
}
