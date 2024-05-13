package practice.virtualcurrency.controller.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import practice.virtualcurrency.VirtualCurrencyConst;
import practice.virtualcurrency.domain.form.LoginForm;
import practice.virtualcurrency.domain.member.Member;
import practice.virtualcurrency.service.login.LoginService;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String login(@ModelAttribute("loginForm") LoginForm loginForm){
        return "login/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult,
                        @RequestParam(defaultValue = "/") String redirectURL, HttpServletRequest request) {

        if (bindingResult.hasErrors()) { //validation
            return "login/loginForm";
        }

        Optional<Member> loginMember = loginService.login(loginForm.getUsername(),loginForm.getPassword());

        if (loginMember.isEmpty()) {
            bindingResult.reject("loginFail");
            return "login/loginForm";
        }

        HttpSession session = request.getSession();
        session.setAttribute(VirtualCurrencyConst.LOGIN_MEMBER, loginMember.get());

        return "redirect:" + redirectURL;
    }
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
