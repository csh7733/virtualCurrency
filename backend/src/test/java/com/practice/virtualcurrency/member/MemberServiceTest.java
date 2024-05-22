package com.practice.virtualcurrency.member;

import com.practice.virtualcurrency.domain.member.Member;
import com.practice.virtualcurrency.service.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
@Slf4j
class MemberServiceTest {

    @Autowired
    private MemberService memberService;
    @Test
    void joinAndFindTest() {
        log.info("Member create start");
        Member newMember = new Member("test","test@gmail.com","test123");
        memberService.join(newMember);

        log.info("Case 1 : Success to find member");
        Member findMember = memberService.findMember("test@gmail.com", "test123").get();
        assertThat(newMember).isEqualTo(findMember);

        log.info("Case 2 : Fail to find member");
        assertThatThrownBy(() -> {
            Member findMember2 = memberService.findMember("test@gmail.com", "test1234").get();
        }).isInstanceOf(NoSuchElementException.class);
    }
}