package practice.virtualcurrency.domain.member;

import jakarta.persistence.*;
import lombok.Data;
import practice.virtualcurrency.VirtualCurrencyConst;
import practice.virtualcurrency.domain.coin.Coin;
import practice.virtualcurrency.domain.order.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @ElementCollection
    private Map<String,Double> wallet = new HashMap();
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    private void initWallet() {
        for (String coinName : VirtualCurrencyConst.WALLET_ELEMENTS) {
            wallet.put(coinName,0.0);
        }
    }

    public Member() {
        initWallet();
    }

    public Member(String username, String password) {
        initWallet();
        this.username = username;
        this.password = password;
    }
}
