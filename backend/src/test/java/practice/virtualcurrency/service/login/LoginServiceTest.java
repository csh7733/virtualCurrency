package practice.virtualcurrency.service.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import practice.virtualcurrency.service.member.MemberService;

import static org.junit.jupiter.api.Assertions.*;
@Transactional
@SpringBootTest
@Slf4j
class LoginServiceTest {

    @Autowired
    private MemberService memberService;

}