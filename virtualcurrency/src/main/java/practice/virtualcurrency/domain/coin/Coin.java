package practice.virtualcurrency.domain.coin;

import jakarta.persistence.*;
import lombok.Data;
import practice.virtualcurrency.domain.member.Member;

import java.util.concurrent.atomic.AtomicLong;

@Data
@Embeddable
public class Coin {
    private String name;
    private Double quantity;
    public Coin() {
    }

    public Coin(String name, Double quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
