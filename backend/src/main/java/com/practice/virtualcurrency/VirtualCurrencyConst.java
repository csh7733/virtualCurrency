package com.practice.virtualcurrency;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

public class VirtualCurrencyConst {
    public static final Integer EXPIRED_TIME = 1000 * 60 * 60 * 10;
    public static final List<String> WALLET_ELEMENTS = Arrays.asList("BTC", "ETH", "XRP", "CSH", "USDT");

    public static String getCurrentMemberUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails currentUser = (UserDetails)  authentication.getPrincipal();
        return currentUser.getUsername();
    }
}
