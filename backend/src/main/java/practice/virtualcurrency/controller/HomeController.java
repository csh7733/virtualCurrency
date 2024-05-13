package practice.virtualcurrency.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import practice.virtualcurrency.argumentResolver.LoginMember;
import practice.virtualcurrency.domain.member.Member;

import java.util.Optional;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home(@LoginMember Member member, Model model){
        if(member != null) {
            model.addAttribute("member",member);
        }
        return "home";
    }
}
