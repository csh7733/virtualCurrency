package com.practice.virtualcurrency;

import com.practice.virtualcurrency.service.member.MemberService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class VirtualcurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualcurrencyApplication.class, args);
	}

	@Bean
	@Profile("local")
	public DataInit DataInit(MemberService memberService) {
		return new DataInit(memberService);
	}
}
