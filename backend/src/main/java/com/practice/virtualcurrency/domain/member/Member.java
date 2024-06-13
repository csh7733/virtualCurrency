package com.practice.virtualcurrency.domain.member;

import com.practice.virtualcurrency.VirtualCurrencyConst;
import com.practice.virtualcurrency.domain.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_wallet", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyColumn(name = "coin_name")
    @Column(name = "quantity")
    private Map<String, Double> wallet = initializeWallet();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    private static Map<String, Double> initializeWallet() {
        Map<String, Double> initialWallet = new HashMap<>();
        for (String coinName : VirtualCurrencyConst.WALLET_ELEMENTS) {
            initialWallet.put(coinName, 0.0);
            if(coinName.equals("USDT"))  initialWallet.put(coinName, 10000.0);
        }
        return initialWallet;
    }

    public Member(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
